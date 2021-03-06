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
package org.apache.ignite.internal.processors.cache.verify;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.Map;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.internal.visor.VisorDataTransferObject;

/**
 * Encapsulates result of {@link VerifyBackupPartitionsDumpTask}.
 */
public class IdleVerifyDumpResult extends VisorDataTransferObject {
    /** */
    private static final long serialVersionUID = 0L;

    /** Cluster hashes. */
    private Map<PartitionKeyV2, List<PartitionHashRecordV2>> clusterHashes;

    /**
     * @param clusterHashes Cluster hashes.
     */
    public IdleVerifyDumpResult(Map<PartitionKeyV2, List<PartitionHashRecordV2>> clusterHashes) {
        this.clusterHashes = clusterHashes;
    }

    /**
     * Default constructor for Externalizable.
     */
    public IdleVerifyDumpResult() {
    }

    /** {@inheritDoc} */
    @Override protected void writeExternalData(ObjectOutput out) throws IOException {
        U.writeMap(out, clusterHashes);
    }

    /** {@inheritDoc} */
    @Override protected void readExternalData(byte protoVer,
        ObjectInput in) throws IOException, ClassNotFoundException {
        clusterHashes = U.readLinkedMap(in);
    }

    /**
     * @return Cluster hashes.
     */
    public Map<PartitionKeyV2, List<PartitionHashRecordV2>> clusterHashes() {
        return clusterHashes;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(IdleVerifyDumpResult.class, this);
    }
}
