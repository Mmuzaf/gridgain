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

package org.apache.zookeeper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.logger.java.JavaLogger;
import org.apache.ignite.testframework.GridTestUtils;

/**
 *
 */
public class ZkTestClientCnxnSocketNIO extends ClientCnxnSocketNIO {
    /** */
    public static final IgniteLogger log = new JavaLogger().getLogger(ZkTestClientCnxnSocketNIO.class);

    /** */
    public static volatile boolean DEBUG = false;

    /** */
    public volatile CountDownLatch blockConnectLatch;

    /** */
    public static ConcurrentHashMap<String, ZkTestClientCnxnSocketNIO> clients = new ConcurrentHashMap<>();

    /** */
    private final String nodeName;

    /**
     *
     */
    public static void reset() {
        clients.clear();
    }

    /**
     * @param node Node.
     * @return ZK client.
     */
    public static ZkTestClientCnxnSocketNIO forNode(Ignite node) {
        return clients.get(node.name());
    }

    /**
     * @param instanceName Ignite instance name.
     * @return ZK client.
     */
    public static ZkTestClientCnxnSocketNIO forNode(String instanceName) {
        return clients.get(instanceName);
    }

    /**
     * @throws IOException If failed.
     */
    public ZkTestClientCnxnSocketNIO() throws IOException {
        super();

        String threadName = Thread.currentThread().getName();

        nodeName = threadName.substring(threadName.indexOf('-') + 1);

        if (DEBUG)
            log.info("ZkTestClientCnxnSocketNIO created for node: " + nodeName);
    }

    /** {@inheritDoc} */
    @Override void connect(InetSocketAddress addr) throws IOException {
        CountDownLatch blockConnect = this.blockConnectLatch;

        if (DEBUG)
            log.info("ZkTestClientCnxnSocketNIO connect [node=" + nodeName + ", addr=" + addr + ']');

        if (blockConnect != null && blockConnect.getCount() > 0) {
            try {
                log.info("ZkTestClientCnxnSocketNIO block connect");

                blockConnect.await(60, TimeUnit.SECONDS);

                log.info("ZkTestClientCnxnSocketNIO finish block connect");
            }
            catch (Exception e) {
                log.error("Error in ZkTestClientCnxnSocketNIO: " + e, e);
            }
        }

        super.connect(addr);

        clients.put(nodeName, this);
    }

    /**
     *
     */
    public void allowConnect() {
        assert blockConnectLatch != null && blockConnectLatch.getCount() == 1 : blockConnectLatch;

        log.info("ZkTestClientCnxnSocketNIO allowConnect [node=" + nodeName + ']');

        blockConnectLatch.countDown();
    }

    /**
     * @param blockConnect {@code True} to block client reconnect.
     * @throws Exception If failed.
     */
    public void closeSocket(boolean blockConnect) throws Exception {
        if (blockConnect)
            blockConnectLatch = new CountDownLatch(1);

        log.info("ZkTestClientCnxnSocketNIO closeSocket [node=" + nodeName + ", block=" + blockConnect + ']');

        SelectionKey k = GridTestUtils.getFieldValue(this, ClientCnxnSocketNIO.class, "sockKey");

        k.channel().close();
    }
}
