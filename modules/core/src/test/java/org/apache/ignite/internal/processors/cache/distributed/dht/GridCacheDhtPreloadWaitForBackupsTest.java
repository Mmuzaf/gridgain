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

package org.apache.ignite.internal.processors.cache.distributed.dht;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheRebalanceMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
import org.apache.ignite.cache.eviction.lru.LruEvictionPolicyFactory;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.events.Event;
import org.apache.ignite.events.EventType;
import org.apache.ignite.internal.util.typedef.G;
import org.apache.ignite.lang.IgnitePredicate;
import org.apache.ignite.testframework.GridTestUtils;
import org.apache.ignite.testframework.MvccFeatureChecker;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

/**
 * MultiThreaded load test for DHT preloader.
 */
public class GridCacheDhtPreloadWaitForBackupsTest extends GridCommonAbstractTest {
    /** */
    public static final int CACHE_SIZE = 10000;

    /** */
    public static final int ITERATIONS = 20;

    /**
     * Creates new test.
     */
    public GridCacheDhtPreloadWaitForBackupsTest() {
        super(false);
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testNodeLeavesRebalanceCompletes() throws Exception {
        try {
            startGrids(4);

            for (int i = 0; i < CACHE_SIZE; i++)
                grid(i % 4).cache("partitioned" + (1 + (i >> 3) % 3)).put(i, new byte[i]);

            int nextGrid = 4;

            Thread th0 = null;

            Thread th1 = null;

            for (int n = 0; n < ITERATIONS; n++) {
                int startGrid = nextGrid;
                int stopPerm = (nextGrid + 1) % 5;
                int stopTmp = (nextGrid + 2) % 5;

                startGrid(startGrid);

                stopGrid(stopTmp, false);

                (th0 = new Thread (() -> grid(stopPerm).close())).start();

                Thread.sleep(1000);

                (th1 = new Thread (() ->
                {
                    try {
                        startGrid(stopTmp);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                })).start();

                nextGrid = stopPerm;

                th1.join();
                th0.join();
            }

            for (int i = 0; i < CACHE_SIZE; i++) {
                byte[] val = (byte[])grid((i >> 2) % 4).cache("partitioned" + (1 + (i >> 3) % 3)).get(i);

                assertNotNull(Integer.toString(i), val);
                assertEquals(i, val.length);
            }
        }
        finally {
            // Intentionally used this method. See startGrid(String, String).
            G.stopAll(false, false);
        }
    }

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);

        CacheConfiguration[] ccfgs = new CacheConfiguration[3];
        for (int i = 1; i <= 3; i++) {
            CacheConfiguration ccfg = new CacheConfiguration("partitioned" + i);

            ccfg.setCacheMode(CacheMode.PARTITIONED);
            ccfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
            ccfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);
            ccfg.setBackups(1);
            ccfg.setAffinity(new RendezvousAffinityFunction().setPartitions(32));

            ccfgs[i - 1] = ccfg;
        }

        cfg.setWaitForBackupsOnShutdown(true);

        cfg.setCacheConfiguration(ccfgs);

        return cfg;
    }
}
