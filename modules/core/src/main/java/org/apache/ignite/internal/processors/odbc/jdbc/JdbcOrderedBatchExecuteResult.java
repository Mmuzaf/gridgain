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

package org.apache.ignite.internal.processors.odbc.jdbc;

import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.internal.binary.BinaryReaderExImpl;
import org.apache.ignite.internal.binary.BinaryWriterExImpl;
import org.apache.ignite.internal.processors.odbc.ClientListenerProtocolVersion;
import org.apache.ignite.internal.util.typedef.internal.S;

/**
 * JDBC batch execute ordered result.
 */
public class JdbcOrderedBatchExecuteResult extends JdbcBatchExecuteResult {
    /** Order. */
    private long order;

    /**
     * Constructor.
     */
    public JdbcOrderedBatchExecuteResult() {
        super(BATCH_EXEC_ORDERED);
    }

    /**
     * @param res Result.
     * @param order Order.
     */
    public JdbcOrderedBatchExecuteResult(JdbcBatchExecuteResult res, long order) {
        super(BATCH_EXEC_ORDERED, res);

        this.order = order;
    }

    /**
     * @return Order.
     */
    public long order() {
        return order;
    }

    /** {@inheritDoc} */
    @Override public void writeBinary(BinaryWriterExImpl writer,
        ClientListenerProtocolVersion ver) throws BinaryObjectException {
        super.writeBinary(writer, ver);

        writer.writeLong(order);
    }


    /** {@inheritDoc} */
    @Override public void readBinary(BinaryReaderExImpl reader,
        ClientListenerProtocolVersion ver) throws BinaryObjectException {
        super.readBinary(reader, ver);

        order = reader.readLong();
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(JdbcOrderedBatchExecuteResult.class, this);
    }
}
