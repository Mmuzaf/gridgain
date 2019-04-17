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

package org.apache.ignite.internal.processors.cache;

import java.util.Collection;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test that validates {@link Ignite#cacheNames()} implementation.
 */
@RunWith(JUnit4.class)
public class CacheNamesWithSpecialCharactersTest extends GridCommonAbstractTest {
    /** */
    private static final String CACHE_NAME_1 = "--№=+:(replicated)";

    /** */
    private static final String CACHE_NAME_2 = ":_&:: (partitioned)";

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);

        CacheConfiguration cacheCfg1 = new CacheConfiguration(DEFAULT_CACHE_NAME);
        cacheCfg1.setCacheMode(CacheMode.REPLICATED);
        cacheCfg1.setName(CACHE_NAME_1);

        CacheConfiguration cacheCfg2 = new CacheConfiguration(DEFAULT_CACHE_NAME);
        cacheCfg2.setCacheMode(CacheMode.PARTITIONED);
        cacheCfg2.setName(CACHE_NAME_2);

        cfg.setCacheConfiguration(cacheCfg1, cacheCfg2);

        return cfg;
    }

    /**
     * @throws Exception In case of failure.
     */
    @Test
    public void testCacheNames() throws Exception {
        try {
            startGridsMultiThreaded(2);

            Collection<String> names = grid(0).cacheNames();

            assertEquals(2, names.size());

            for (String name : names)
                assertTrue(name.equals(CACHE_NAME_1) || name.equals(CACHE_NAME_2));
        }
        finally {
            stopAllGrids();
        }
    }
}
