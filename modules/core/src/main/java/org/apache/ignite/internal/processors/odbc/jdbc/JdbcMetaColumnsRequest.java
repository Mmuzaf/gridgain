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
import org.jetbrains.annotations.Nullable;

/**
 * JDBC get columns metadata request.
 */
public class JdbcMetaColumnsRequest extends JdbcRequest {
    /** Schema name pattern. */
    private String schemaName;

    /** Table name pattern. */
    private String tblName;

    /** Column name pattern. */
    private String colName;

    /**
     * Default constructor is used for deserialization.
     */
    JdbcMetaColumnsRequest() {
        super(META_COLUMNS);
    }

    /**
     * @param schemaName Schema name.
     * @param tblName Table name.
     * @param colName Column name.
     */
    public JdbcMetaColumnsRequest(String schemaName, String tblName, String colName) {
        super(META_COLUMNS);

        this.schemaName = schemaName;
        this.tblName = tblName;
        this.colName = colName;
    }

    /**
     * @return Schema name pattern.
     */
    @Nullable public String schemaName() {
        return schemaName;
    }

    /**
     * @return Table name pattern.
     */
    public String tableName() {
        return tblName;
    }

    /**
     * @return Column name pattern.
     */
    public String columnName() {
        return colName;
    }

    /** {@inheritDoc} */
    @Override public void writeBinary(BinaryAbstractWriterEx writer,
        ClientListenerProtocolVersion ver) throws BinaryObjectException {
        super.writeBinary(writer, ver);

        writer.writeString(schemaName);
        writer.writeString(tblName);
        writer.writeString(colName);
    }

    /** {@inheritDoc} */
    @Override public void readBinary(BinaryAbstractReaderEx reader,
        ClientListenerProtocolVersion ver) throws BinaryObjectException {
        super.readBinary(reader, ver);

        schemaName = reader.readString();
        tblName = reader.readString();
        colName = reader.readString();
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(JdbcMetaColumnsRequest.class, this);
    }
}