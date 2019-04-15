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

package org.apache.ignite.client;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.ClientConnectorConfiguration;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;

/**
 * Several Ignite servers running on localhost.
 */
public class LocalIgniteCluster implements AutoCloseable {
    /** Host. */
    private static final String HOST = "127.0.0.1";

    /** Randomizer. */
    private static final Random rnd = new Random();

    /** Servers. */
    private final List<Ignite> srvs = new ArrayList<>();

    /** Configurations of the failed servers. */
    private final List<NodeConfiguration> failedCfgs = new ArrayList<>();

    /** Initial cluster size. */
    private int initSize;

    /** Private constructor: use {@link #start(int)} to create instances of {@link LocalIgniteCluster}. */
    private LocalIgniteCluster(int initSize) {
        if (initSize < 1)
            throw new IllegalArgumentException("Cluster must have at least one node.");

        this.initSize = initSize;

        for (int i = 0; i < initSize; i++) {
            IgniteConfiguration cfg = getConfiguration(
                new NodeConfiguration(TcpDiscoverySpi.DFLT_PORT + i, ClientConnectorConfiguration.DFLT_PORT + i)
            );

            Ignite ignite = Ignition.start(cfg);

            srvs.add(ignite);
        }
    }

    /**
     * Create and start start the cluster.
     */
    public static LocalIgniteCluster start(int initSize) {
        return new LocalIgniteCluster(initSize);
    }

    /** {@inheritDoc} */
    @Override public void close() {
        srvs.forEach(Ignite::close);

        srvs.clear();
    }

    /**
     * Remove one random node.
     */
    public void failNode() {
        if (srvs.isEmpty())
            throw new IllegalStateException("Cannot remove node from empty cluster");

        Ignite srv = srvs.get(rnd.nextInt(srvs.size()));

        IgniteConfiguration cfg = srv.configuration();

        NodeConfiguration nodeCfg = new NodeConfiguration(
            ((TcpDiscoverySpi)cfg.getDiscoverySpi()).getIpFinder().getRegisteredAddresses().iterator().next().getPort(),
            Objects.requireNonNull(cfg.getClientConnectorConfiguration()).getPort()
        );

        srv.close();

        srvs.remove(srv);

        failedCfgs.add(nodeCfg);
    }

    /**
     * Restore one of the failed nodes.
     */
    public void restoreNode() {
        if (failedCfgs.isEmpty())
            throw new IllegalStateException("Cannot restore nodes in healthy cluster");

        NodeConfiguration nodeCfg = failedCfgs.get(rnd.nextInt(failedCfgs.size()));

        Ignite ignite = Ignition.start(getConfiguration(nodeCfg));

        srvs.add(ignite);

        failedCfgs.remove(nodeCfg);
    }

    /**
     * @return Client connection string as defined by
     * {@link ClientConfiguration#setAddresses(String...)}.
     */
    public Collection<String> clientAddresses() {
        return srvs.stream()
            .map(s -> {
                ClientConnectorConfiguration cfg = s.configuration().getClientConnectorConfiguration();

                return cfg == null ? null : String.format("%s:%s", cfg.getHost(), cfg.getPort());
            })
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * @return Number of nodes in the cluster.
     */
    public int size() {
        return srvs.size();
    }

    /**
     * @return Initial cluster size (number of nodes in the healthy cluster).
     */
    public int getInitialSize() {
        return initSize;
    }

    /** */
    private static IgniteConfiguration getConfiguration(NodeConfiguration nodeCfg) {
        IgniteConfiguration igniteCfg = Config.getServerConfiguration();

        ((TcpDiscoverySpi)igniteCfg.getDiscoverySpi()).getIpFinder().registerAddresses(
            Collections.singletonList(new InetSocketAddress(HOST, nodeCfg.getDiscoveryPort()))
        );

        igniteCfg.setClientConnectorConfiguration(new ClientConnectorConfiguration()
            .setHost(HOST)
            .setPort(nodeCfg.getClientPort())
        );

        return igniteCfg;
    }

    /** Settings unique for each node in the cluster. */
    private static class NodeConfiguration {
        /** Discovery port. */
        private final int discoveryPort;

        /** Client port. */
        private final int clientPort;

        /** */
        NodeConfiguration(int discoveryPort, int clientPort) {
            this.discoveryPort = discoveryPort;
            this.clientPort = clientPort;
        }

        /** */
        int getDiscoveryPort() {
            return discoveryPort;
        }

        /** */
        int getClientPort() {
            return clientPort;
        }
    }
}
