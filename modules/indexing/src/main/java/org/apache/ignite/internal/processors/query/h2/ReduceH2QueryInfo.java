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

package org.apache.ignite.internal.processors.query.h2;

import java.sql.PreparedStatement;
import org.jetbrains.annotations.Nullable;

/**
 * Reduce query info.
 */
public class ReduceH2QueryInfo extends H2QueryInfo {
    /** Request id. */
    private final long reqId;

    /**
     * @param stmt Query statement.
     * @param sql Query statement.
     * @param reqId Request ID.
     */
    public ReduceH2QueryInfo(PreparedStatement stmt, String sql, long reqId, @Nullable Long originalQryId) {
        super(QueryType.REDUCE, stmt, sql, originalQryId);

        this.reqId= reqId;
    }

    /** {@inheritDoc} */
    @Override protected void printInfo(StringBuilder msg) {
        msg.append(", reqId=").append(reqId);
    }
}
