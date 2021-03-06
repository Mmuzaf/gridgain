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

package org.apache.ignite.internal.processors.continuous;

import java.util.UUID;
import org.apache.ignite.internal.managers.discovery.DiscoveryCustomMessage;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public class StopRoutineDiscoveryMessage extends AbstractContinuousMessage {
    /** */
    private static final long serialVersionUID = 0L;

    /**
     * @param routineId Routine id.
     */
    public StopRoutineDiscoveryMessage(UUID routineId) {
        super(routineId);
    }

    /** {@inheritDoc} */
    @Nullable @Override public DiscoveryCustomMessage ackMessage() {
        return new StopRoutineAckDiscoveryMessage(routineId);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(StopRoutineDiscoveryMessage.class, this, "routineId", routineId());
    }
}
