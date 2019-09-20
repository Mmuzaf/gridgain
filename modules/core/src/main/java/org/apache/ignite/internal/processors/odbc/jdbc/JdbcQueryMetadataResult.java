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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.ignite.binary.BinaryObjectException;
import org.apache.ignite.internal.binary.BinaryAbstractReaderEx;
import org.apache.ignite.internal.binary.BinaryAbstractWriterEx;
import org.apache.ignite.internal.processors.odbc.ClientListenerProtocolVersion;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.internal.util.typedef.internal.S;

/**
 * JDBC query metadata result.
 */
public class JdbcQueryMetadataResult extends JdbcResult {
    /** Fields metadata. */
    private List<JdbcColumnMeta> meta;

    /**
     * Default constructor is used for deserialization.
     */
    JdbcQueryMetadataResult() {
        super(QRY_META);
    }

    /**
     * @param queryId Query ID.
     * @param meta Query metadata.
     */
    JdbcQueryMetadataResult(long queryId, List<JdbcColumnMeta> meta){
        super(QRY_META);

        this.meta = meta;
    }

    /**
     * @return Query result metadata.
     */
    public List<JdbcColumnMeta> meta() {
        return meta;
    }

    /** {@inheritDoc} */
    @Override public void writeBinary(BinaryAbstractWriterEx writer,
        ClientListenerProtocolVersion ver) throws BinaryObjectException {
        super.writeBinary(writer, ver);

        if (F.isEmpty(meta))
            writer.writeInt(0);
        else {
            writer.writeInt(meta.size());

            for (JdbcColumnMeta m : meta)
                m.writeBinary(writer, ver);
        }
    }

    /** {@inheritDoc} */
    @Override public void readBinary(BinaryAbstractReaderEx reader,
        ClientListenerProtocolVersion ver) throws BinaryObjectException {
        super.readBinary(reader, ver);

        int size = reader.readInt();

        if (size == 0)
            meta = Collections.emptyList();
        else {
            meta = new ArrayList<>(size);

            for (int i = 0; i < size; ++i) {
                JdbcColumnMeta m = new JdbcColumnMeta();

                m.readBinary(reader, ver);

                meta.add(m);
            }
        }
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(JdbcQueryMetadataResult.class, this);
    }
}
