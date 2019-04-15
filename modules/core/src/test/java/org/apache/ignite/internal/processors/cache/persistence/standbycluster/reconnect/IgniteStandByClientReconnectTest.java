/*
 *                   GridGain Community Edition Licensing
 *                   Copyright 2019 GridGain Systems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License") modified with Commons Clause
 * Restriction; you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * 
 * Commons Clause Restriction
 * 
 * The Software is provided to you by the Licensor under the License, as defined below, subject to
 * the following condition.
 * 
 * Without limiting other conditions in the License, the grant of rights under the License will not
 * include, and the License does not grant to you, the right to Sell the Software.
 * For purposes of the foregoing, “Sell” means practicing any or all of the rights granted to you
 * under the License to provide to third parties, for a fee or other consideration (including without
 * limitation fees for hosting or consulting/ support services related to the Software), a product or
 * service whose value derives, entirely or substantially, from the functionality of the Software.
 * Any license notice or attribution required by the License must also include this Commons Clause
 * License Condition notice.
 * 
 * For purposes of the clause above, the “Licensor” is Copyright 2019 GridGain Systems, Inc.,
 * the “License” is the Apache License, Version 2.0, and the Software is the GridGain Community
 * Edition software provided with this notice.
 */

package org.apache.ignite.internal.processors.cache.persistence.standbycluster.reconnect;

import java.util.concurrent.CountDownLatch;
import org.apache.ignite.internal.IgniteEx;
import org.junit.Test;

/**
 *
 */
public class IgniteStandByClientReconnectTest extends IgniteAbstractStandByClientReconnectTest {
    /**
     * @throws Exception If failed.
     */
    @Test
    public void testActiveClientReconnectToActiveCluster() throws Exception {
        CountDownLatch activateLatch = new CountDownLatch(1);

        startNodes(activateLatch);

        info(">>>> star grid");

        IgniteEx ig1 = grid(node1);
        IgniteEx ig2 = grid(node2);
        IgniteEx client = grid(nodeClient);

        assertTrue(!ig1.cluster().active());
        assertTrue(!ig2.cluster().active());
        assertTrue(!client.cluster().active());

        client.cluster().active(true);

        info(">>>> activate grid");

        checkDescriptors(ig1, staticCacheNames);
        checkDescriptors(ig2, staticCacheNames);
        checkDescriptors(client, staticCacheNames);

        checkStaticCaches();

        client.createCache(ccfgDynamic);

        client.createCache(ccfgDynamicWithFilter);

        info(">>>> dynamic start [" + ccfgDynamicName + ", " + ccfgDynamicWithFilterName + "]");

        assertTrue(ig1.cluster().active());
        assertTrue(ig2.cluster().active());
        assertTrue(client.cluster().active());

        checkDescriptors(ig1, allCacheNames);
        checkDescriptors(ig2, allCacheNames);
        checkDescriptors(client, allCacheNames);

        final CountDownLatch disconnectedLatch = new CountDownLatch(1);
        final CountDownLatch reconnectedLatch = new CountDownLatch(1);

        addDisconnectListener(disconnectedLatch, reconnectedLatch);

        info(">>>> stop servers");

        stopGrid(node2);

        disconnectedLatch.await();

        ig2 = startGrid(getConfiguration(node2));

        info(">>>> activate new servers");

        ig1.cluster().active(true);

        assertTrue(ig1.cluster().active());
        assertTrue(ig2.cluster().active());

        activateLatch.countDown();

        info(">>>> reconnect client");

        reconnectedLatch.await();

        info(">>>> client reconnected");

        assertTrue(ig1.cluster().active());
        assertTrue(ig2.cluster().active());
        assertTrue(client.cluster().active());

        checkDescriptors(ig1, allCacheNames);
        checkDescriptors(ig2, allCacheNames);
        checkDescriptors(client, allCacheNames);

        checkAllCaches();
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testActiveClientReconnectToInActiveCluster() throws Exception {
        CountDownLatch activateLatch = new CountDownLatch(1);

        startNodes(activateLatch);

        info(">>>> star grid");

        IgniteEx ig1 = grid(node1);
        IgniteEx ig2 = grid(node2);
        IgniteEx client = grid(nodeClient);

        assertTrue(!ig1.cluster().active());
        assertTrue(!ig2.cluster().active());
        assertTrue(!client.cluster().active());

        info(">>>> activate grid");

        client.cluster().active(true);

        checkStaticCaches();

        checkDescriptors(ig1, staticCacheNames);
        checkDescriptors(ig2, staticCacheNames);
        checkDescriptors(client, staticCacheNames);

        info(">>>> dynamic start [" + ccfgDynamicName + ", " + ccfgDynamicWithFilterName + "]");

        client.createCache(ccfgDynamic);

        client.createCache(ccfgDynamicWithFilter);

        checkDescriptors(ig1, allCacheNames);
        checkDescriptors(ig2, allCacheNames);
        checkDescriptors(client, allCacheNames);

        assertTrue(ig1.cluster().active());
        assertTrue(ig2.cluster().active());
        assertTrue(client.cluster().active());

        final CountDownLatch disconnectedLatch = new CountDownLatch(1);
        final CountDownLatch reconnectedLatch = new CountDownLatch(1);

        addDisconnectListener(disconnectedLatch, reconnectedLatch);

        info(">>>> stop " + node2);

        stopGrid(node2);

        disconnectedLatch.await();

        ig1.cluster().active(false);

        activateLatch.countDown();

        info(">>>> restart " + node2);

        ig2 = startGrid(getConfiguration(node2));

        reconnectedLatch.await();

        assertTrue(!ig1.cluster().active());
        assertTrue(!ig2.cluster().active());
        assertTrue(!client.cluster().active());

        client.cluster().active(true);

        assertTrue(ig1.cluster().active());
        assertTrue(ig2.cluster().active());
        assertTrue(client.cluster().active());

        checkDescriptors(ig1, allCacheNames);
        checkDescriptors(ig2, allCacheNames);
        checkDescriptors(client, allCacheNames);

        checkAllCaches();
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testInActiveClientReconnectToActiveCluster() throws Exception {
        CountDownLatch activateLatch = new CountDownLatch(1);

        startNodes(activateLatch);

        IgniteEx ig1 = grid(node1);
        IgniteEx ig2 = grid(node2);
        IgniteEx client = grid(nodeClient);

        assertTrue(!ig1.cluster().active());
        assertTrue(!ig2.cluster().active());
        assertTrue(!client.cluster().active());

        final CountDownLatch disconnectedLatch = new CountDownLatch(1);
        final CountDownLatch reconnectedLatch = new CountDownLatch(1);

        addDisconnectListener(disconnectedLatch, reconnectedLatch);

        stopGrid(node2);

        disconnectedLatch.await();

        ig2 = startGrid(getConfiguration(node2));

        ig1.cluster().active(true);

        assertTrue(ig1.cluster().active());
        assertTrue(ig2.cluster().active());

        checkDescriptors(ig1, staticCacheNames);
        checkDescriptors(ig2, staticCacheNames);

        activateLatch.countDown();

        reconnectedLatch.await();

        assertTrue(ig1.cluster().active());
        assertTrue(ig2.cluster().active());
        assertTrue(client.cluster().active());

        checkDescriptors(ig1, staticCacheNames);
        checkDescriptors(ig2, staticCacheNames);

        client.createCache(ccfgDynamic);

        client.createCache(ccfgDynamicWithFilter);

        checkDescriptors(ig1, allCacheNames);
        checkDescriptors(ig2, allCacheNames);
        checkDescriptors(client, allCacheNames);

        checkAllCaches();
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testInActiveClientReconnectToInActiveCluster() throws Exception {
        startNodes(null);

        IgniteEx ig1 = grid(node1);
        IgniteEx ig2 = grid(node2);
        IgniteEx client = grid(nodeClient);

        assertTrue(!ig1.cluster().active());
        assertTrue(!ig2.cluster().active());
        assertTrue(!client.cluster().active());

        final CountDownLatch disconnectedLatch = new CountDownLatch(1);
        final CountDownLatch reconnectedLatch = new CountDownLatch(1);

        addDisconnectListener(disconnectedLatch, reconnectedLatch);

        stopGrid(node2);

        disconnectedLatch.await();

        ig2 = startGrid(getConfiguration(node2));

        reconnectedLatch.await();

        assertTrue(!ig1.cluster().active());
        assertTrue(!ig2.cluster().active());
        assertTrue(!client.cluster().active());

        client.cluster().active(true);

        assertTrue(ig1.cluster().active());
        assertTrue(ig2.cluster().active());
        assertTrue(client.cluster().active());

        checkStaticCaches();

        client.createCache(ccfgDynamic);

        client.createCache(ccfgDynamicWithFilter);

        checkDescriptors(ig1, allCacheNames);
        checkDescriptors(ig2, allCacheNames);
        checkDescriptors(client, allCacheNames);

        checkAllCaches();
    }
}
