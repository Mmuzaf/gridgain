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
import org.apache.ignite.internal.processors.odbc.ClientListenerProtocolVersion;
import org.apache.ignite.internal.util.typedef.internal.S;

/**
 * JDBC query fetch request.
 */
public class JdbcQueryFetchRequest extends JdbcRequest {
    /** Cursor ID. */
    private long cursorId;

    /** Fetch size. */
    private int pageSize;

    /**
     * Constructor.
     */
    JdbcQueryFetchRequest() {
        super(QRY_FETCH);
    }

    /**
     * @param cursorId Cursor ID.
     * @param pageSize Fetch size.
     */
    public JdbcQueryFetchRequest(long cursorId, int pageSize) {
        super(QRY_FETCH);

        this.cursorId = cursorId;
        this.pageSize = pageSize;
    }

    /**
     * @return Cursor ID.
     */
    public long cursorId() {
        return cursorId;
    }

    /**
     * @return Fetch page size.
     */
    public int pageSize() {
        return pageSize;
    }

    /** {@inheritDoc} */
    @Override public void writeBinary(BinaryAbstractWriterEx writer,
        ClientListenerProtocolVersion ver) throws BinaryObjectException {
        super.writeBinary(writer, ver);

        writer.writeLong(cursorId);
        writer.writeInt(pageSize);
    }

    /** {@inheritDoc} */
    @Override public void readBinary(BinaryAbstractReaderEx reader,
        ClientListenerProtocolVersion ver) throws BinaryObjectException {
        super.readBinary(reader, ver);

        cursorId = reader.readLong();
        pageSize = reader.readInt();
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(JdbcQueryFetchRequest.class, this);
    }
}