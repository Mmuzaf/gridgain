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

package org.apache.ignite.internal.processors.cache.distributed.near;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.GridDirectCollection;
import org.apache.ignite.internal.GridDirectTransient;
import org.apache.ignite.internal.processors.cache.GridCacheContext;
import org.apache.ignite.internal.processors.cache.GridCacheIdMessage;
import org.apache.ignite.internal.processors.cache.GridCacheReturn;
import org.apache.ignite.internal.processors.cache.GridCacheSharedContext;
import org.apache.ignite.internal.processors.cache.distributed.dht.ExceptionAware;
import org.apache.ignite.internal.processors.cache.version.GridCacheVersion;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.lang.IgniteUuid;
import org.apache.ignite.plugin.extensions.communication.MessageCollectionItemType;
import org.apache.ignite.plugin.extensions.communication.MessageReader;
import org.apache.ignite.plugin.extensions.communication.MessageWriter;
import org.apache.ignite.plugin.extensions.communication.ProcessingTimeLoggableResponse;
import org.apache.ignite.plugin.extensions.communication.TimeLoggableResponse;
import org.jetbrains.annotations.Nullable;

/**
 * A response to {@link GridNearTxEnlistRequest}.
 */
public class GridNearTxEnlistResponse extends GridCacheIdMessage implements ExceptionAware, ProcessingTimeLoggableResponse {
    /** */
    private static final long serialVersionUID = 0L;

    /** Future ID. */
    private IgniteUuid futId;

    /** Error. */
    @GridDirectTransient
    private Throwable err;

    /** Serialized error. */
    private byte[] errBytes;

    /** Mini future id. */
    private int miniId;

    /** Result. */
    private GridCacheReturn res;

    /** */
    private GridCacheVersion lockVer;

    /** */
    private GridCacheVersion dhtVer;

    /** */
    private IgniteUuid dhtFutId;

    /** New DHT nodes involved into transaction. */
    @GridDirectCollection(UUID.class)
    private Collection<UUID> newDhtNodes;

    /** @see ProcessingTimeLoggableResponse#reqSentTimestamp(). */
    @GridDirectTransient
    private long reqSentTimestamp = INVALID_TIMESTAMP;

    /** @see ProcessingTimeLoggableResponse#reqReceivedTimestamp(). */
    @GridDirectTransient
    private long reqReceivedTimestamp = INVALID_TIMESTAMP;

    /** @see TimeLoggableResponse#reqTimeData(). */
    private long reqTimeData = INVALID_TIMESTAMP;

    /**
     * Default constructor.
     */
    public GridNearTxEnlistResponse() {
        // No-op.
    }

    /**
     * Constructor for normal result.
     *
     * @param cacheId Cache id.
     * @param futId Future id.
     * @param miniId Mini future id.
     * @param lockVer Lock version.
     * @param res Result.
     * @param dhtVer Dht version.
     * @param dhtFutId Dht future id.
     * @param newDhtNodes New DHT nodes involved into transaction.
     * @param reqReceivedTimestamp Request receive timestamp.
     * @param reqSentTimestamp Request send timestamp.
     */
    public GridNearTxEnlistResponse(int cacheId,
        IgniteUuid futId,
        int miniId,
        GridCacheVersion lockVer,
        GridCacheReturn res,
        GridCacheVersion dhtVer,
        IgniteUuid dhtFutId,
        Set<UUID> newDhtNodes,
        long reqReceivedTimestamp,
        long reqSentTimestamp)
    {
        this(cacheId, futId, miniId, lockVer, reqReceivedTimestamp, reqSentTimestamp);

        this.res = res;
        this.dhtVer = dhtVer;
        this.dhtFutId = dhtFutId;
        this.newDhtNodes = newDhtNodes;
    }

    /**
     * Constructor for error result.
     *
     * @param cacheId Cache id.
     * @param futId Future id.
     * @param miniId Mini future id.
     * @param lockVer Lock version.
     * @param err Error.
     * @param reqReceivedTimestamp Request receive timestamp.
     * @param reqSentTimestamp Request send timestamp.
     */
    public GridNearTxEnlistResponse(
        int cacheId,
        IgniteUuid futId,
        int miniId,
        GridCacheVersion lockVer,
        Throwable err,
        long reqReceivedTimestamp,
        long reqSentTimestamp)
    {
        this(cacheId, futId, miniId, lockVer, reqReceivedTimestamp, reqSentTimestamp);

        this.err = err;
    }

    /**
     * Common constructor
     *
     * @param cacheId Cache id.
     * @param futId Future id.
     * @param miniId Mini id.
     * @param lockVer Lock version.
     * @param reqReceivedTimestamp Request receive timestamp.
     * @param reqSentTimestamp Request send timestamp.
     */
    private GridNearTxEnlistResponse(
        int cacheId,
        IgniteUuid futId,
        int miniId,
        GridCacheVersion lockVer,
        long reqReceivedTimestamp,
        long reqSentTimestamp)
    {
        this.cacheId = cacheId;
        this.futId = futId;
        this.miniId = miniId;
        this.lockVer = lockVer;

        reqReceivedTimestamp(reqReceivedTimestamp);
        reqSentTimestamp(reqSentTimestamp);
    }

    /**
     * @return Loc version.
     */
    public GridCacheVersion version() {
        return lockVer;
    }

    /**
     * @return Future id.
     */
    public IgniteUuid futureId() {
        return futId;
    }

    /**
     * @return Mini future id.
     */
    public int miniId() {
        return miniId;
    }

    /**
     * @return Result.
     */
    public GridCacheReturn result() {
        return res;
    }

    /** {@inheritDoc} */
    @Override public @Nullable Throwable error() {
        return err;
    }

    /** {@inheritDoc} */
    @Override public boolean addDeploymentInfo() {
        return addDepInfo;
    }

    /**
     * @return Dht version.
     */
    public GridCacheVersion dhtVersion() {
        return dhtVer;
    }

    /**
     * @return Dht future id.
     */
    public IgniteUuid dhtFutureId() {
        return dhtFutId;
    }

    /**
     * @return New DHT nodes involved into transaction.
     */
    public Collection<UUID> newDhtNodes() {
        return newDhtNodes;
    }


    /** {@inheritDoc} */
    @Override public void reqSentTimestamp(long reqSentTimestamp) {
        this.reqSentTimestamp = reqSentTimestamp;
    }

    /** {@inheritDoc} */
    @Override public long reqSentTimestamp() {
        return reqSentTimestamp;
    }

    /** {@inheritDoc} */
    @Override public void reqReceivedTimestamp(long reqReceivedTimestamp) {
        this.reqReceivedTimestamp = reqReceivedTimestamp;
    }

    /** {@inheritDoc} */
    @Override public long reqReceivedTimestamp() {
        return reqReceivedTimestamp;
    }

    /** {@inheritDoc} */
    @Override public void reqTimeData(long reqTimeData) {
        this.reqTimeData = reqTimeData;
    }

    /** {@inheritDoc} */
    @Override public long reqTimeData() {
        return reqTimeData;
    }

    /** {@inheritDoc} */
    @Override public byte fieldsCount() {
        return 13;
    }

    /** {@inheritDoc} */
    @Override public boolean writeTo(ByteBuffer buf, MessageWriter writer) {
        writer.setBuffer(buf);

        if (!super.writeTo(buf, writer))
            return false;

        if (!writer.isHeaderWritten()) {
            if (!writer.writeHeader(directType(), fieldsCount()))
                return false;

            writer.onHeaderWritten();
        }

        switch (writer.state()) {
            case 4:
                if (!writer.writeIgniteUuid("dhtFutId", dhtFutId))
                    return false;

                writer.incrementState();

            case 5:
                if (!writer.writeMessage("dhtVer", dhtVer))
                    return false;

                writer.incrementState();

            case 6:
                if (!writer.writeByteArray("errBytes", errBytes))
                    return false;

                writer.incrementState();

            case 7:
                if (!writer.writeIgniteUuid("futId", futId))
                    return false;

                writer.incrementState();

            case 8:
                if (!writer.writeMessage("lockVer", lockVer))
                    return false;

                writer.incrementState();

            case 9:
                if (!writer.writeInt("miniId", miniId))
                    return false;

                writer.incrementState();

            case 10:
                if (!writer.writeCollection("newDhtNodes", newDhtNodes, MessageCollectionItemType.UUID))
                    return false;

                writer.incrementState();

            case 11:
                if (!writer.writeLong("reqTimeData", reqTimeData))
                    return false;

                writer.incrementState();

            case 12:
                if (!writer.writeMessage("res", res))
                    return false;

                writer.incrementState();

        }

        return true;
    }

    /** {@inheritDoc} */
    @Override public boolean readFrom(ByteBuffer buf, MessageReader reader) {
        reader.setBuffer(buf);

        if (!reader.beforeMessageRead())
            return false;

        if (!super.readFrom(buf, reader))
            return false;

        switch (reader.state()) {
            case 4:
                dhtFutId = reader.readIgniteUuid("dhtFutId");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 5:
                dhtVer = reader.readMessage("dhtVer");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 6:
                errBytes = reader.readByteArray("errBytes");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 7:
                futId = reader.readIgniteUuid("futId");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 8:
                lockVer = reader.readMessage("lockVer");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 9:
                miniId = reader.readInt("miniId");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 10:
                newDhtNodes = reader.readCollection("newDhtNodes", MessageCollectionItemType.UUID);

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 11:
                reqTimeData = reader.readLong("reqTimeData");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 12:
                res = reader.readMessage("res");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

        }

        return reader.afterMessageRead(GridNearTxEnlistResponse.class);
    }

    /** {@inheritDoc} */
    @Override public short directType() {
        return 160;
    }

    /** {@inheritDoc} */
    @Override public void prepareMarshal(GridCacheSharedContext ctx) throws IgniteCheckedException {
        super.prepareMarshal(ctx);

        GridCacheContext cctx = ctx.cacheContext(cacheId);

        if (err != null && errBytes == null)
            errBytes = U.marshal(ctx.marshaller(), err);

        if (res != null)
            res.prepareMarshal(cctx);
    }

    /** {@inheritDoc} */
    @Override public void finishUnmarshal(GridCacheSharedContext ctx, ClassLoader ldr) throws IgniteCheckedException {
        super.finishUnmarshal(ctx, ldr);

        GridCacheContext cctx = ctx.cacheContext(cacheId);

        if (errBytes != null)
            err = U.unmarshal(ctx, errBytes, U.resolveClassLoader(ldr, ctx.gridConfig()));

        if (res != null)
            res.finishUnmarshal(cctx, ldr);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridNearTxEnlistResponse.class, this);
    }
}
