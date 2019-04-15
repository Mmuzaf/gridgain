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

package org.apache.ignite.internal.processors.cache.mvcc;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.util.GridLongList;
import org.apache.ignite.internal.util.tostring.GridToStringInclude;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
class PreviousQueries {
    /** */
    private static class Node {
        /** */
        @GridToStringInclude
        boolean init;

        /** */
        @GridToStringInclude
        Set<Long> cntrs;

        /** */
        boolean isDone() {
            return init && (cntrs == null || cntrs.stream().allMatch(l -> l < 0));
        }

        @Override public String toString() {
            return S.toString(Node.class, this);
        }
    }

    /** */
    private Map<UUID, Node> active = new HashMap<>();

    /** */
    private boolean init;

    /** */
    private volatile boolean done;

    /**
     * @param nodes Waiting nodes.
     * @param alivePredicate Alive nodes filter.
     */
    synchronized void init(Collection<ClusterNode> nodes, Predicate<UUID> alivePredicate) {
        assert !init && !done;

        nodes.stream().map(ClusterNode::id).forEach(uuid -> active.putIfAbsent(uuid, new Node()));

        active.entrySet().removeIf(e -> !alivePredicate.test(e.getKey()) || e.getValue().isDone());

        if (active.isEmpty())
            done = true;

        init = true;
    }

    /**
     * @param nodeId Node ID.
     */
    void onNodeFailed(@NotNull UUID nodeId) {
        if (done())
            return;

        synchronized (this) {
            if (init)
                removeAndCheckDone(nodeId);
        }
    }

    /**
     * @param nodeId Node ID.
     * @param qryId Query tracker Id.
     */
    void onQueryDone(@NotNull UUID nodeId, long qryId) {
        if (!done())
            onQueryDone0(nodeId, qryId);
    }

    /**
     * @param nodeId Node ID.
     * @param queryIds Query tracker Ids.
     */
    void addActiveQueries(@NotNull UUID nodeId, @NotNull GridLongList queryIds) {
        if (!done())
            addActiveQueries0(nodeId, queryIds);
    }

    /**
     * @return {@code True} if all queries mapped on previous coordinator are done.
     */
    boolean done() {
        return done;
    }

    /** */
    private synchronized void onQueryDone0(@NotNull UUID nodeId, long qryId) {
        assert qryId > 0;

        Node node = active.get(nodeId);

        if (node == null && !init)
            active.put(nodeId, node = new Node());

        if (node != null) {
            Set<Long> cntrs = node.cntrs;

            boolean wasNull = cntrs == null;

            if (cntrs == null)
                cntrs = node.cntrs = new HashSet<>();

            if (wasNull || !cntrs.remove(qryId))
                cntrs.add(-qryId);

            if (init && node.isDone())
                removeAndCheckDone(nodeId);
        }
    }

    /** */
    private synchronized void addActiveQueries0(@NotNull UUID nodeId, @NotNull GridLongList queryIds) {
        Node node = active.get(nodeId);

        if (node == null && !init)
            active.put(nodeId, node = new Node());

        if (node != null) {
            Set<Long> cntrs = node.cntrs;

            boolean wasNull = cntrs == null, hasQueries = false;

            for (int i = 0; i < queryIds.size(); i++) {
                long qryId = queryIds.get(i);

                assert qryId > 0;

                if (cntrs == null)
                    cntrs = node.cntrs = new HashSet<>();

                if (wasNull || !cntrs.remove(-qryId))
                    hasQueries |= cntrs.add(qryId);
            }

            if (init && !hasQueries)
                removeAndCheckDone(nodeId);
            else
                node.init = true;
        }
    }

    /** */
    private void removeAndCheckDone(@NotNull UUID nodeId) {
        assert init;

        active.remove(nodeId);

        if (active.isEmpty())
            done = true;
    }
}