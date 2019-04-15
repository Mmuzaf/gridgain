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

package org.apache.ignite.internal.visor.compute;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import org.apache.ignite.compute.ComputeJobResult;
import org.apache.ignite.internal.processors.task.GridInternal;
import org.apache.ignite.internal.util.typedef.internal.S;
import org.apache.ignite.internal.visor.VisorJob;
import org.apache.ignite.internal.visor.VisorMultiNodeTask;
import org.jetbrains.annotations.Nullable;

import static org.apache.ignite.internal.visor.compute.VisorComputeMonitoringHolder.COMPUTE_MONITORING_HOLDER_KEY;
import static org.apache.ignite.internal.visor.util.VisorTaskUtils.VISOR_TASK_EVTS;
import static org.apache.ignite.internal.visor.util.VisorTaskUtils.checkExplicitTaskMonitoring;

/**
 * Task to run gc on nodes.
 */
@GridInternal
public class VisorComputeToggleMonitoringTask extends
    VisorMultiNodeTask<VisorComputeToggleMonitoringTaskArg, Boolean, Boolean> {
    /** */
    private static final long serialVersionUID = 0L;

    /** {@inheritDoc} */
    @Nullable @Override protected Boolean reduce0(List<ComputeJobResult> results) {
        Collection<Boolean> toggles = new HashSet<>();

        for (ComputeJobResult res : results)
            toggles.add(res.<Boolean>getData());

        // If all nodes return same result.
        return toggles.size() == 1;
    }

    /** {@inheritDoc} */
    @Override protected VisorComputeToggleMonitoringJob job(VisorComputeToggleMonitoringTaskArg arg) {
        return new VisorComputeToggleMonitoringJob(arg, debug);
    }

    /**
     * Job to toggle task monitoring on node.
     */
    private static class VisorComputeToggleMonitoringJob extends VisorJob<VisorComputeToggleMonitoringTaskArg, Boolean> {
        /** */
        private static final long serialVersionUID = 0L;

        /**
         * @param arg Visor ID key and monitoring state flag.
         * @param debug Debug flag.
         */
        private VisorComputeToggleMonitoringJob(VisorComputeToggleMonitoringTaskArg arg, boolean debug) {
            super(arg, debug);
        }

        /** {@inheritDoc} */
        @Override protected Boolean run(VisorComputeToggleMonitoringTaskArg arg) {
            if (checkExplicitTaskMonitoring(ignite))
                return Boolean.TRUE;

            ConcurrentMap<String, VisorComputeMonitoringHolder> storage = ignite.cluster().nodeLocalMap();

            VisorComputeMonitoringHolder holder = storage.get(COMPUTE_MONITORING_HOLDER_KEY);

            if (holder == null) {
                VisorComputeMonitoringHolder holderNew = new VisorComputeMonitoringHolder();

                VisorComputeMonitoringHolder holderOld =
                    storage.putIfAbsent(COMPUTE_MONITORING_HOLDER_KEY, holderNew);

                holder = holderOld == null ? holderNew : holderOld;
            }

            String visorKey = arg.getVisorKey();

            boolean state = arg.isEnabled();

            // Set task monitoring state.
            if (state)
                holder.startCollect(ignite, visorKey);
            else
                holder.stopCollect(ignite, visorKey);

            // Return actual state. It could stay the same if events explicitly enabled in configuration.
            return ignite.allEventsUserRecordable(VISOR_TASK_EVTS);
        }

        /** {@inheritDoc} */
        @Override public String toString() {
            return S.toString(VisorComputeToggleMonitoringJob.class, this);
        }
    }
}
