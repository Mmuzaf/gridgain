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

package org.apache.ignite.internal.util.lang.gridfunc;

import java.util.UUID;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.lang.IgnitePredicate;

/**
 * {@link ClusterNode} node has NOT equal id predicate.
 */
public class HasNotEqualIdPredicate<T extends ClusterNode> implements IgnitePredicate<T> {
    /** */
    private static final long serialVersionUID = 0L;

    /** */
    private final UUID locNodeId;

    /**
     * @param locNodeId Id for check.
     */
    public HasNotEqualIdPredicate(UUID locNodeId) {
        this.locNodeId = locNodeId;
    }

    /** {@inheritDoc} */
    @Override public boolean apply(T n) {
        return !n.id().equals(locNodeId);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(HasNotEqualIdPredicate.class, this);
    }
}
