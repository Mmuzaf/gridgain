/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ignite.internal.cluster;

import java.util.Arrays;
import java.util.UUID;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.processors.cluster.ClusterTagGenerator;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;

/**
 * Test for cluster id and cluster name features of IgniteCluster.
 */
public class IgniteClusterIdTagTest extends GridCommonAbstractTest {
    /** */
    private static final String CUSTOM_TAG_0 = "my_super_cluster";

    /** */
    private static final String CUSTOM_TAG_1 = "not_so_super_but_OK";

    /** */
    private static final String CLIENT_CUSTOM_TAG_0 = "client_custom_tag_0";

    /** */
    private static final String CLIENT_CUSTOM_TAG_1 = "client_custom_tag_1";

    /** */
    private boolean isPersistenceEnabled;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);

        if (igniteInstanceName.contains("client"))
            cfg.setClientMode(true);
        else {
            DataStorageConfiguration dsCfg = new DataStorageConfiguration()
                .setDefaultDataRegionConfiguration(
                    new DataRegionConfiguration()
                        .setInitialSize(128 * 1024 * 1024)
                        .setMaxSize(128 * 1024 * 1024)
                        .setPersistenceEnabled(isPersistenceEnabled)
                );

            cfg.setDataStorageConfiguration(dsCfg);
        }

        return cfg;
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        stopAllGrids();

        cleanPersistenceDir();
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        stopAllGrids();

        cleanPersistenceDir();
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testInMemoryClusterId() throws Exception {
        Ignite ig0 = startGrid(0);

        UUID id0 = ig0.cluster().id();

        assertNotNull(id0);

        Ignite ig1 = startGrid(1);

        UUID id1 = ig1.cluster().id();

        assertEquals(id0, id1);

        stopAllGrids();

        ig0 = startGrid(0);

        assertNotSame(id0, ig0.cluster().id());
    }

    /**
     *
     */
    @Test
    public void testPersistentClusterId() throws Exception {
        isPersistenceEnabled = true;

        IgniteEx ig0 = startGrid(0);

        ig0.cluster().active(true);

        UUID id0 = ig0.cluster().id();

        stopAllGrids();

        ig0 = startGrid(0);

        assertEquals(id0, ig0.cluster().id());

        awaitPartitionMapExchange();
    }

    /**
     *
     */
    @Test
    public void testInMemoryClusterTag() throws Exception {
        IgniteEx ig0 = startGrid(0);

        String tag0 = ig0.cluster().tag();

        assertNotNull(tag0);

        assertTrue(Arrays.asList(ClusterTagGenerator.IN_MEMORY_CLUSTER_TAGS).contains(tag0));

        ig0.cluster().tag(CUSTOM_TAG_0);

        IgniteEx ig1 = startGrid(1);

        String tag1 = ig1.cluster().tag();

        assertNotNull(tag1);

        assertEquals(CUSTOM_TAG_0, tag1);

        IgniteEx ig2 = startGrid(2);

        assertEquals(CUSTOM_TAG_0, ig2.cluster().tag());

        ig2.cluster().tag(CUSTOM_TAG_1);

        //tag set from one server node is applied on all other nodes
        assertEquals(CUSTOM_TAG_1, ig0.cluster().tag());

        assertEquals(CUSTOM_TAG_1, ig1.cluster().tag());

        IgniteEx cl0 = startGrid("client0");

        assertEquals(CUSTOM_TAG_1, cl0.cluster().tag());

        cl0.cluster().tag(CLIENT_CUSTOM_TAG_0);

        //tag set from client is applied on server nodes
        assertEquals(CLIENT_CUSTOM_TAG_0, ig0.cluster().tag());

        IgniteEx cl1 = startGrid("client1");

        cl1.cluster().tag(CLIENT_CUSTOM_TAG_1);

        //tag set from client is applied on other client nodes
        assertEquals(CLIENT_CUSTOM_TAG_1, cl0.cluster().tag());
    }

    /**
     *
     */
    @Test
    public void testPersistentClusterTag() throws Exception {
        isPersistenceEnabled = true;

        IgniteEx ig0 = startGrid(0);

        boolean expectedExceptionThrown = false;

        try {
            ig0.cluster().tag(CUSTOM_TAG_0);
        }
        catch (IgniteCheckedException e) {
            if (e.getMessage().contains("Can not change cluster tag on inactive cluster."))
                expectedExceptionThrown = true;
        }

        assertTrue(expectedExceptionThrown);
    }
}
