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

import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.testframework.MvccFeatureChecker;
import org.junit.Before;

/**
 * Tests {@link org.apache.ignite.cache.CacheInterceptor}.
 */
public class GridCacheInterceptorLocalWithStoreSelfTest extends GridCacheInterceptorLocalSelfTest {
    /** */
    @Before
    public void beforeGridCacheInterceptorLocalWithStoreSelfTest() {
        MvccFeatureChecker.skipIfNotSupported(MvccFeatureChecker.Feature.CACHE_STORE);
    }

    /** {@inheritDoc} */
    @Override protected CacheConfiguration cacheConfiguration(String igniteInstanceName) throws Exception {
        MvccFeatureChecker.skipIfNotSupported(MvccFeatureChecker.Feature.CACHE_STORE);

        return super.cacheConfiguration(igniteInstanceName);
    }

    /** {@inheritDoc} */
    @Override protected boolean storeEnabled() {
        return true;
    }
}
