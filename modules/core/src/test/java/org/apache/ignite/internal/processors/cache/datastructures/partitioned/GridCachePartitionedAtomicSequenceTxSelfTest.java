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

package org.apache.ignite.internal.processors.cache.datastructures.partitioned;

import java.util.concurrent.CountDownLatch;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicSequence;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.configuration.AtomicConfiguration;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.lang.IgniteRunnable;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.apache.ignite.transactions.Transaction;
import org.junit.Test;

import static org.apache.ignite.cache.CacheAtomicityMode.TRANSACTIONAL;

/**
 * Tests {@link IgniteAtomicSequence} operations inside started user transaction.
 */
public class GridCachePartitionedAtomicSequenceTxSelfTest extends GridCommonAbstractTest {
    /** Number of threads. */
    private static final int THREAD_NUM = 8;

    /** Sequence cache size. */
    private static final int SEQ_CACHE_SIZE = 10;

    /** Iterations. */
    private static final int ITERATIONS = 100;

    /** Sequence name. */
    private static final String SEQ_NAME = "seq";

    /** Latch. */
    private static CountDownLatch latch;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);

        cfg.setPublicThreadPoolSize(THREAD_NUM);

        AtomicConfiguration atomicCfg = atomicConfiguration();

        assertNotNull(atomicCfg);

        cfg.setAtomicConfiguration(atomicCfg);

        return cfg;
    }

    /**
     * @return Atomic config for test.
     */
    protected AtomicConfiguration atomicConfiguration() {
        AtomicConfiguration cfg = new AtomicConfiguration();

        cfg.setBackups(1);
        cfg.setAtomicSequenceReserveSize(SEQ_CACHE_SIZE);

        return cfg;
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        latch = new CountDownLatch(THREAD_NUM);

        startGridsMultiThreaded(1);
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        stopAllGrids();
    }

    /**
     * Tests sequence calls inside transactions.
     *
     * @throws Exception If failed.
     */
    @Test
    public void testTransactionIncrement() throws Exception {
        ignite(0).atomicSequence(SEQ_NAME, 0, true);

        for (int i = 0; i < THREAD_NUM; i++) {
            multithreaded(new Runnable() {
                @Override public void run() {
                    ignite(0).compute().run(new IncrementClosure());

                }
            }, THREAD_NUM);
        }
    }

    /**
     * Tests isolation of system and user transactions.
     */
    @Test
    public void testIsolation() {
        IgniteAtomicSequence seq = ignite(0).atomicSequence(SEQ_NAME, 0, true);

        CacheConfiguration<Object, Object> ccfg = new CacheConfiguration<>(DEFAULT_CACHE_NAME);
        ccfg.setAtomicityMode(TRANSACTIONAL);

        IgniteCache<Object, Object> cache = ignite(0).getOrCreateCache(ccfg);

        try (Transaction tx = ignite(0).transactions().txStart()) {
            seq.getAndIncrement();

            cache.put(1, 1);

            tx.rollback();
        }

        assertEquals(0, cache.size());
        assertEquals(new Long(1L), U.field(seq, "locVal"));
        assertEquals(new Long(SEQ_CACHE_SIZE - 1), U.field(seq, "upBound"));
    }

    /**
     * Closure which does sequence increment.
     */
    private static class IncrementClosure implements IgniteRunnable {
        /** Ignite instance. */
        @IgniteInstanceResource
        private Ignite ignite;

        /** {@inheritDoc} */
        @Override public void run() {
            IgniteAtomicSequence seq = ignite.atomicSequence(SEQ_NAME, 0, false);

            latch.countDown();

            U.awaitQuiet(latch);

            for (int i = 0; i < ITERATIONS; i++)
                try (Transaction ignored = ignite.transactions().txStart()) {
                    seq.incrementAndGet();
                }
        }
    }
}
