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
import org.apache.ignite.internal.binary.BinaryAbstractReaderEx;
import org.apache.ignite.internal.binary.BinaryAbstractWriterEx;
import org.apache.ignite.internal.processors.bulkload.BulkLoadAckClientParameters;
import org.apache.ignite.internal.processors.odbc.ClientListenerProtocolVersion;
import org.apache.ignite.internal.sql.command.SqlBulkLoadCommand;
import org.apache.ignite.internal.util.typedef.internal.S;

/**
 * A reply from server to SQL COPY command, which is essentially a request from server to client
 * to send files from client to server (see IGNITE-6917 for details).
 *
 * @see JdbcBulkLoadProcessor for the protocol.
 * @see SqlBulkLoadCommand
 */
public class JdbcBulkLoadAckResult extends JdbcResult {
    /** Cursor ID for matching this command on server in further {@link JdbcBulkLoadBatchRequest} commands. */
    private long cursorId;

    /**
     * Bulk load parameters, which are parsed on the server side and sent to client to specify
     * what files to send, batch size, etc.
     */
    private BulkLoadAckClientParameters params;

    /**Creates uninitialized bulk load batch request result. */
    public JdbcBulkLoadAckResult() {
        super(BULK_LOAD_ACK);

        cursorId = 0;
        params = null;
    }

    /**
     * Constructs a request from server (in form of reply) to send files from client to server.
     *
     * @param cursorId Cursor ID to send in further {@link JdbcBulkLoadBatchRequest}s.
     * @param params Various parameters for sending batches from client side.
     */
    public JdbcBulkLoadAckResult(long cursorId, BulkLoadAckClientParameters params) {
        super(BULK_LOAD_ACK);

        this.cursorId = cursorId;
        this.params = params;
    }

    /**
     * Returns the cursor ID.
     *
     * @return Cursor ID.
     */
    public long cursorId() {
        return cursorId;
    }

    /**
     * Returns the parameters for the client.
     *
     * @return The parameters for the client.
     */
    public BulkLoadAckClientParameters params() {
        return params;
    }

    /** {@inheritDoc} */
    @Override public void writeBinary(BinaryAbstractWriterEx writer,
        ClientListenerProtocolVersion ver) throws BinaryObjectException {
        super.writeBinary(writer, ver);

        writer.writeLong(cursorId);
        writer.writeString(params.localFileName());
        writer.writeInt(params.packetSize());
    }

    /** {@inheritDoc} */
    @Override public void readBinary(BinaryAbstractReaderEx reader,
        ClientListenerProtocolVersion ver) throws BinaryObjectException {
        super.readBinary(reader, ver);

        cursorId = reader.readLong();

        String locFileName = reader.readString();
        int batchSize = reader.readInt();

        if (!BulkLoadAckClientParameters.isValidPacketSize(batchSize))
            throw new BinaryObjectException(BulkLoadAckClientParameters.packetSizeErrorMesssage(batchSize));

        params = new BulkLoadAckClientParameters(locFileName, batchSize);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(JdbcBulkLoadAckResult.class, this);
    }
}
