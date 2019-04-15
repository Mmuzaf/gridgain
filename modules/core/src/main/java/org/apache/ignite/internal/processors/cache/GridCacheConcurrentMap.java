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

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.ignite.internal.processors.affinity.AffinityTopologyVersion;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.jetbrains.annotations.Nullable;

/**
 * Concurrent cache map.
 */
public interface GridCacheConcurrentMap {
    /**
     * Returns the entry associated with the specified key in the
     * HashMap. Returns null if the HashMap contains no mapping
     * for this key.
     *
     * @param ctx Cache context.
     * @param key Key.
     * @return Entry.
     */
    @Nullable public GridCacheMapEntry getEntry(GridCacheContext ctx, KeyCacheObject key);

    /**
     * @param ctx Cache context.
     * @param topVer Topology version.
     * @param key Key.
     * @param create Create flag.
     * @return Existing or new GridCacheMapEntry. Will return {@code null} if entry is obsolete or absent and create
     * flag is set to {@code false}. Will also return {@code null} if create flag is set to {@code true}, but entry
     * couldn't be created.
     */
    @Nullable public GridCacheMapEntry putEntryIfObsoleteOrAbsent(
        GridCacheContext ctx,
        AffinityTopologyVersion topVer,
        KeyCacheObject key,
        boolean create,
        boolean touch);

    /**
     * Removes passed in entry if it presents in the map.
     *
     * @param entry Entry to remove.
     * @return {@code True} if remove happened.
     */
    public boolean removeEntry(GridCacheEntryEx entry);

    /**
     * Returns the number of key-value mappings in this map.
     * It does not include entries from underlying data store.
     *
     * @return the number of key-value mappings in this map.
     */
    public int internalSize();

    /**
     * Returns the number of publicly available key-value mappings in this map.
     * It excludes entries that are marked as deleted.
     * It also does not include entries from underlying data store.
     *
     * @param cacheId Cache ID.
     * @return the number of publicly available key-value mappings in this map.
     */
    public int publicSize(int cacheId);

    /**
     * Increments public size.
     *
     * @param e Entry that caused public size change.
     * @param hld Cache map (passed as optimization to avoid cache map lookup for shared groups).
     */
    public void incrementPublicSize(@Nullable CacheMapHolder hld, GridCacheEntryEx e);

    /**
     * Decrements public size.
     *
     * @param e Entry that caused public size change.
     * @param hld Cache map (passed as optimization to avoid cache map lookup for shared groups).
     */
    public void decrementPublicSize(@Nullable CacheMapHolder hld, GridCacheEntryEx e);

    /**
     * @param cacheId Cache ID.
     * @param filter Filter.
     * @return Iterable of the mappings contained in this map, excluding entries in unvisitable state.
     */
    public Iterable<GridCacheMapEntry> entries(int cacheId, CacheEntryPredicate... filter);

    /**
     * @param cacheId Cache ID.
     * @param filter Filter.
     * @return Set of the mappings contained in this map.
     */
    public Set<GridCacheMapEntry> entrySet(int cacheId, CacheEntryPredicate... filter);

    /**
     *
     */
    static class CacheMapHolder {
        /** */
        public final GridCacheContext cctx;

        /** */
        public final AtomicInteger size = new AtomicInteger();

        /** */
        public final ConcurrentMap<KeyCacheObject, GridCacheMapEntry> map;

        /**
         * @param cctx Cache context.
         * @param map Map.
         */
        public CacheMapHolder(GridCacheContext cctx, ConcurrentMap<KeyCacheObject, GridCacheMapEntry> map) {
            this.cctx = cctx;
            this.map = map;
        }

        /** {@inheritDoc} */
        @Override public String toString() {
            return S.toString(CacheMapHolder.class, this);
        }
    }
}
