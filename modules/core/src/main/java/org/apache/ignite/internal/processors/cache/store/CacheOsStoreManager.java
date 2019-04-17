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

package org.apache.ignite.internal.processors.cache.store;

import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.cache.store.CacheStore;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.internal.GridKernalContext;
import org.apache.ignite.internal.processors.platform.PlatformProcessor;
import org.apache.ignite.internal.processors.platform.cache.store.PlatformCacheStore;
import org.apache.ignite.internal.binary.BinaryMarshaller;

/**
 * Default store manager implementation.
 */
public class CacheOsStoreManager extends GridCacheStoreManagerAdapter {
    /** Ignite context. */
    private final GridKernalContext ctx;

    /** Cache configuration. */
    private final CacheConfiguration cfg;

    /**
     * Constructor.
     *
     * @param ctx Ignite context.
     * @param cfg Cache configuration.
     */
    public CacheOsStoreManager(GridKernalContext ctx, CacheConfiguration cfg) {
        this.ctx = ctx;
        this.cfg = cfg;
    }

    /** {@inheritDoc} */
    @Override protected void start0() throws IgniteCheckedException {
        if (configured()) {
            CacheStore store = configuredStore();

            assert store != null;
            assert !(store instanceof GridCacheWriteBehindStore);

            if (store instanceof PlatformCacheStore) {
                PlatformProcessor proc = ctx.platform();

                proc.registerStore((PlatformCacheStore)store, configuredConvertBinary());
            }
        }

        super.start0();
    }


    /** {@inheritDoc} */
    @Override protected GridKernalContext igniteContext() {
        return ctx;
    }

    /** {@inheritDoc} */
    @Override protected CacheConfiguration cacheConfiguration() {
        return cfg;
    }

    /** {@inheritDoc} */
    @Override public boolean convertBinary() {
        if (alwaysKeepBinary)
            return false;

        return configuredConvertBinary() && !(cfgStore instanceof PlatformCacheStore);
    }

    /** {@inheritDoc} */
    @Override public boolean configuredConvertBinary() {
        return !(ctx.config().getMarshaller() instanceof BinaryMarshaller && cfg.isStoreKeepBinary());
    }
}
