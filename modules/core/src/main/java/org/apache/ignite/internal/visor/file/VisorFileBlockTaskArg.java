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

package org.apache.ignite.internal.visor.file;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.internal.visor.VisorDataTransferObject;

/**
 * Arguments for {@link VisorFileBlockTask}
 */
public class VisorFileBlockTaskArg extends VisorDataTransferObject {
    /** */
    private static final long serialVersionUID = 0L;

    /** Log file path. */
    private String path;

    /** Log file offset. */
    private long off;

    /** Block size. */
    private int blockSz;

    /** Log file last modified timestamp. */
    private long lastModified;

    /**
     * Default constructor.
     */
    public VisorFileBlockTaskArg() {
        // No-op.
    }

    /**
     * @param path Log file path.
     * @param off Offset in file.
     * @param blockSz Block size.
     * @param lastModified Log file last modified timestamp.
     */
    public VisorFileBlockTaskArg(String path, long off, int blockSz, long lastModified) {
        this.path = path;
        this.off = off;
        this.blockSz = blockSz;
        this.lastModified = lastModified;
    }

    /**
     * @return Log file path.
     */
    public String getPath() {
        return path;
    }

    /**
     * @return Log file offset.
     */
    public long getOffset() {
        return off;
    }

    /**
     * @return Block size
     */
    public int getBlockSize() {
        return blockSz;
    }

    /**
     * @return Log file last modified timestamp.
     */
    public long getLastModified() {
        return lastModified;
    }

    /** {@inheritDoc} */
    @Override protected void writeExternalData(ObjectOutput out) throws IOException {
        U.writeString(out, path);
        out.writeLong(off);
        out.writeInt(blockSz);
        out.writeLong(lastModified);
    }

    /** {@inheritDoc} */
    @Override protected void readExternalData(byte protoVer, ObjectInput in) throws IOException, ClassNotFoundException {
        path = U.readString(in);
        off = in.readLong();
        blockSz = in.readInt();
        lastModified = in.readLong();
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(VisorFileBlockTaskArg.class, this);
    }
}
