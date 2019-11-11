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

package org.apache.ignite.internal.processors.cache.distributed.dht.atomic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import javax.cache.CacheException;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.IgniteInternalFuture;
import org.apache.ignite.internal.NodeStoppingException;
import org.apache.ignite.internal.TestRecordingCommunicationSpi;
import org.apache.ignite.internal.client.util.GridConcurrentHashSet;
import org.apache.ignite.internal.managers.communication.GridIoMessage;
import org.apache.ignite.internal.util.typedef.T2;
import org.apache.ignite.internal.util.typedef.X;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.ignite.plugin.extensions.communication.Message;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.testframework.GridTestUtils;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.apache.ignite.transactions.TransactionException;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.ignite.configuration.WALMode.LOG_ONLY;

/**
 */
public class AtomicUpdateCounterStateTest extends GridCommonAbstractTest {
    /** IP finder. */
    protected static final TcpDiscoveryVmIpFinder IP_FINDER = new TcpDiscoveryVmIpFinder(true);

    /** */
    private static final int MB = 1024 * 1024;

    /** */
    private int backups = 1;

    /** */
    private static final int PARTS = 32;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);

        cfg.setFailureDetectionTimeout(1000000000L);
        cfg.setClientFailureDetectionTimeout(1000000000L);

        cfg.setConsistentId(igniteInstanceName);
        //cfg.setFailureHandler(new StopNodeFailureHandler());
        //cfg.setRebalanceThreadPoolSize(4); // Necessary to reproduce some issues.

        ((TcpDiscoverySpi)cfg.getDiscoverySpi()).setIpFinder(IP_FINDER);

        boolean client = igniteInstanceName.startsWith("client");

        cfg.setClientMode(client);

        cfg.setCommunicationSpi(new TestRecordingCommunicationSpi());

        cfg.setCacheConfiguration(new CacheConfiguration(DEFAULT_CACHE_NAME).
            setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC).
            setBackups(backups).
            setAtomicityMode(CacheAtomicityMode.ATOMIC).
            setAffinity(new RendezvousAffinityFunction(false, PARTS)));

        cfg.setDataStorageConfiguration(new DataStorageConfiguration().
            setWalHistorySize(1000).
            setWalSegmentSize(8 * MB).setWalMode(LOG_ONLY).setPageSize(1024).
            setCheckpointFrequency(MILLISECONDS.convert(365, DAYS)). // All checkpoints will be manual.
            setDefaultDataRegionConfiguration(new DataRegionConfiguration().setPersistenceEnabled(persistenceEnabled()).
            setInitialSize(100 * MB).setMaxSize(100 * MB)));

        return cfg;
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        super.beforeTest();

        cleanPersistenceDir();
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        super.afterTest();

        cleanPersistenceDir();
    }

    @Test
    public void testBackupFailCrd() throws Exception {

    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testSinglePutReorderNearIsPrimaryNode() throws Exception {
        doTestPrimaryFail();
    }

    @Test
    public void testPrimaryFailCrd() throws Exception {

    }

    @Test
    public void testPrimaryFailNotCrd() throws Exception {

    }

    @Test
    public void testPrimaryAndNearFailCrd() throws Exception {

    }

    @Test
    public void testPrimaryAndNearFailNotCrd() throws Exception {

    }

    private void doTestPrimaryFail() throws Exception {
        try {
            IgniteEx crd = startGrids(backups + 1);

            crd.cluster().active(true);

            IgniteEx client = startGrid("client");

            assertNotNull(client.cache(DEFAULT_CACHE_NAME));

            IgniteCache<Object, Object> cache = crd.cache(DEFAULT_CACHE_NAME);

            final int part = crd.affinity(DEFAULT_CACHE_NAME).primaryPartitions(crd.localNode())[1];

            List<Integer> keys = partitionKeys(cache, part, 10, 0);

            Set<Thread> senderThreads = new GridConcurrentHashSet<>();

            TestRecordingCommunicationSpi.spi(crd).blockMessages((node, msg) -> {
                if (msg instanceof GridDhtAtomicSingleUpdateRequest) {
                    senderThreads.add(Thread.currentThread());

                    GridDhtAtomicSingleUpdateRequest r = (GridDhtAtomicSingleUpdateRequest)msg;

                    return r.updateCntr == 1;
                }

                return false;
            });

            IgniteInternalFuture putFut = GridTestUtils.runAsync(() -> {
                try {
                    cache.put(keys.get(0), keys.get(0));
                }
                catch (Exception e) {
                    assertTrue(X.hasCause(e, NodeStoppingException.class));
                }
            });

            TestRecordingCommunicationSpi.spi(crd).waitForBlocked();

            // Update out of counter order.
            // IMPORTANT: reordering is possible only if messages are send to backup from different threads.
            cache.put(keys.get(1), keys.get(1));

            forceCheckpoint();

            assertEquals(2, senderThreads.size());

            stopGrid(1);

            TestRecordingCommunicationSpi.spi(crd).stopBlock();

            putFut.get();

            startGrid(1);

            awaitPartitionMapExchange();

            assertCountersSame(part, false);

            assertPartitionsSame(idleVerify(crd, DEFAULT_CACHE_NAME));
        }
        finally {
            stopAllGrids();
        }
    }

    /**
     * @return {@code True} if persistence is enabled for tests.
     */
    private boolean persistenceEnabled() {
        return true;
    }
}
