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

package org.apache.ignite.internal.processors.cache.checker.processor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteException;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.cache.CacheEntry;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.IgniteEx;
import org.apache.ignite.internal.pagemem.wal.record.DataEntry;
import org.apache.ignite.internal.processors.cache.CacheObjectImpl;
import org.apache.ignite.internal.processors.cache.GridCacheAdapter;
import org.apache.ignite.internal.processors.cache.GridCacheContext;
import org.apache.ignite.internal.processors.cache.GridCacheOperation;
import org.apache.ignite.internal.processors.cache.KeyCacheObject;
import org.apache.ignite.internal.processors.cache.KeyCacheObjectImpl;
import org.apache.ignite.internal.processors.cache.checker.objects.ReconciliationResult;
import org.apache.ignite.internal.processors.cache.persistence.GridCacheDatabaseSharedManager;
import org.apache.ignite.internal.processors.cache.persistence.IgniteCacheDatabaseSharedManager;
import org.apache.ignite.internal.processors.cache.verify.PartitionReconciliationDataRowMeta;
import org.apache.ignite.internal.processors.cache.verify.PartitionReconciliationKeyMeta;
import org.apache.ignite.internal.processors.cache.verify.RepairAlgorithm;
import org.apache.ignite.internal.processors.cache.verify.checker.tasks.PartitionReconciliationProcessorTask;
import org.apache.ignite.internal.processors.cache.version.GridCacheVersion;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.internal.visor.checker.VisorPartitionReconciliationTaskArg;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public class PartitionReconciliationAbstractTest extends GridCommonAbstractTest {
    /**
     *
     */
    public static ReconciliationResult partitionReconciliation(Ignite ig, boolean fixMode,
        @Nullable RepairAlgorithm repairAlgorithm, int parallelism, String... caches) {
        return partitionReconciliation(
            ig,
            new VisorPartitionReconciliationTaskArg.Builder()
                .caches(new HashSet<>(Arrays.asList(caches)))
                .recheckDelay(1)
                .parallelism(parallelism)
                .fixMode(fixMode)
                .repairAlg(repairAlgorithm)
        );
    }

    /**
     *
     */
    public static ReconciliationResult partitionReconciliation(
        Ignite ig,
        VisorPartitionReconciliationTaskArg.Builder argBuilder
    ) {
        IgniteEx ig0 = (IgniteEx)ig;

        ClusterNode node = !ig0.localNode().isClient() ? ig0.localNode() : ig0.cluster().forServers().forRandom().node();

        if (node == null)
            throw new IgniteException("No server node for verification.");

        return ig.compute().execute(
            PartitionReconciliationProcessorTask.class.getName(),
            argBuilder.build()
        );
    }

    /**
     *
     */
    public static Set<Integer> conflictKeys(ReconciliationResult res, String cacheName) {
        return res.partitionReconciliationResult().inconsistentKeys().get(cacheName)
            .values()
            .stream()
            .flatMap(Collection::stream)
            .map(PartitionReconciliationDataRowMeta::keyMeta)
            .map(k -> (String)U.field(k, "strView"))
            .map(Integer::valueOf)
            .collect(Collectors.toSet());
    }

    /**
     * @param res Response.
     * @param cacheName Cache name.
     */
    public static Set<PartitionReconciliationKeyMeta> conflictKeyMetas(ReconciliationResult res, String cacheName) {
        return res.partitionReconciliationResult().inconsistentKeys().get(cacheName)
            .values()
            .stream()
            .flatMap(Collection::stream)
            .map(PartitionReconciliationDataRowMeta::keyMeta)
            .collect(Collectors.toSet());
    }

    /**
     *
     */
    public static void assertResultContainsConflictKeys(
        ReconciliationResult res,
        String cacheName,
        Set<Integer> keys
    ) {
        for (Integer key : keys)
            assertTrue("Key doesn't contain: " + key, conflictKeys(res, cacheName).contains(key));
    }

    /**
     *
     */
    public static void simulateOutdatedVersionCorruption(GridCacheContext<?, ?> ctx, Object key) {
        corruptDataEntry(ctx, key, false, true, new GridCacheVersion(0, 0, 0L), "_broken");
    }

    /**
     *
     */
    public static void simulateMissingEntryCorruption(GridCacheContext<?, ?> ctx, Object key) {
        GridCacheAdapter<Object, Object> cache = (GridCacheAdapter<Object, Object>)ctx.cache();

        cache.clearLocally(key);
    }

    /**
     * TODO we need to stop copypasting this code to every test.
     *
     * Corrupts data entry.
     *
     * @param ctx Context.
     * @param key Key.
     * @param breakCntr Break counter.
     * @param breakData Break data.
     * @param ver GridCacheVersion to use.
     * @param brokenValPostfix Postfix to add to value if breakData flag is set to true.
     */
    private static void corruptDataEntry(
        GridCacheContext<?, ?> ctx,
        Object key,
        boolean breakCntr,
        boolean breakData,
        GridCacheVersion ver,
        String brokenValPostfix
    ) {
        int partId = ctx.affinity().partition(key);

        try {
            long updateCntr = ctx.topology().localPartition(partId).updateCounter();

            CacheEntry<Object, Object> e = ctx.cache().keepBinary().getEntry(key);

            Object valToPut = e.getValue();

            KeyCacheObject keyCacheObj = e.getKey() instanceof BinaryObject ?
                (KeyCacheObject)e.getKey() :
                new KeyCacheObjectImpl(e.getKey(), null, partId);

            if (breakCntr)
                updateCntr++;

            if (breakData)
                valToPut = e.getValue().toString() + brokenValPostfix;

            // Create data entry

            DataEntry dataEntry = new DataEntry(
                ctx.cacheId(),
                keyCacheObj,
                new CacheObjectImpl(valToPut, null),
                GridCacheOperation.UPDATE,
                new GridCacheVersion(),
                ver,
                0L,
                partId,
                updateCntr
            );

            IgniteCacheDatabaseSharedManager db = ctx.shared().database();

            db.checkpointReadLock();

            try {
                U.invoke(GridCacheDatabaseSharedManager.class, db, "applyUpdate", ctx, dataEntry,
                    false);
            }
            finally {
                db.checkpointReadUnlock();
            }
        }
        catch (IgniteCheckedException e) {
            e.printStackTrace();
        }
    }
}
