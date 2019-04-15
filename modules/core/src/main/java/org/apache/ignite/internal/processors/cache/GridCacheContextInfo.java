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

package org.apache.ignite.internal.processors.cache;

import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.internal.util.tostring.GridToStringExclude;
import org.apache.ignite.internal.util.typedef.internal.CU;
import org.apache.ignite.lang.IgniteUuid;
import org.jetbrains.annotations.Nullable;

/**
 * Cache context information. Required to support query infrastructure for not started caches on non affinity nodes.
 */
@GridToStringExclude
public class GridCacheContextInfo<K, V> {
    /** Cache is client or not. */
    private final boolean clientCache;

    /** Dynamic cache deployment ID. */
    private final IgniteUuid dynamicDeploymentId;

    /** Cache configuration. */
    private final CacheConfiguration config;

    /** Cache group ID. */
    private final int groupId;

    /** Cache ID. */
    private final int cacheId;

    /** Full cache context. Can be {@code null} in case a cache is not started. */
    @Nullable private volatile GridCacheContext cctx;

    /**
     * Constructor of full cache context.
     *
     * @param cctx Cache context.
     * @param clientCache Client cache or not.
     */
    public GridCacheContextInfo(GridCacheContext<K, V> cctx, boolean clientCache) {
        config = cctx.config();
        dynamicDeploymentId = null;
        groupId = cctx.groupId();
        cacheId = cctx.cacheId();

        this.clientCache = clientCache;

        this.cctx = cctx;
    }

    /**
     * Constructor of not started cache context.
     *
     * @param cacheDesc Cache descriptor.
     */
    public GridCacheContextInfo(DynamicCacheDescriptor cacheDesc) {
        config = cacheDesc.cacheConfiguration();
        dynamicDeploymentId = cacheDesc.deploymentId();
        groupId = cacheDesc.groupId();
        cacheId = CU.cacheId(config.getName());

        clientCache = true;
    }

    /**
     * @return Cache configuration.
     */
    public CacheConfiguration config() {
        return config;
    }

    /**
     * @return Cache name.
     */
    public String name() {
        return config.getName();
    }

    /**
     * @return Cache group id.
     */
    public int groupId() {
        return groupId;
    }

    /**
     * @return Cache id.
     */
    public int cacheId() {
        return cacheId;
    }

    /**
     * @return {@code true} in case affinity node.
     */
    public boolean affinityNode() {
        return cctx != null && cctx.affinityNode();
    }

    /**
     * @return Cache context. {@code null} for not started cache.
     */
    @Nullable public GridCacheContext cacheContext() {
        return cctx;
    }

    /**
     * @return Dynamic deployment ID.
     */
    public IgniteUuid dynamicDeploymentId() {
        GridCacheContext cctx0 = cctx;

        if (cctx0 != null)
            return cctx0.dynamicDeploymentId();

        assert dynamicDeploymentId != null : "Deployment id is not set and cache context is not initialized: " + this;

        return dynamicDeploymentId;
    }

    /**
     * Set real cache context in case cache has been fully initted and start.
     *
     * @param cctx Initted cache context.
     */
    public void initCacheContext(GridCacheContext<?, ?> cctx) {
        assert this.cctx == null : this.cctx;
        assert cctx != null;

        this.cctx = cctx;
    }

    /**
     * @return {@code true} For client cache.
     */
    public boolean isClientCache() {
        return clientCache;
    }

    /**
     * @return {@code true} If Cache context is initted.
     */
    public boolean isCacheContextInited() {
        return cctx != null;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return "GridCacheContextInfo: " + name() + " " + (isCacheContextInited() ? "started" : "not started");
    }
}
