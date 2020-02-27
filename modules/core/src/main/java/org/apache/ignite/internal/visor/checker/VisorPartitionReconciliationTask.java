/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.visor.checker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.processors.cache.checker.objects.ReconciliationResult;
import org.apache.ignite.internal.processors.cache.verify.checker.tasks.PartitionReconciliationProcessorTask;
import org.apache.ignite.internal.processors.task.GridInternal;
import org.apache.ignite.internal.visor.VisorJob;
import org.apache.ignite.internal.visor.VisorOneNodeTask;
import org.apache.ignite.internal.visor.VisorTaskArgument;

/**
 * Visor partition reconciliation task.
 */
@GridInternal
public class VisorPartitionReconciliationTask
    extends VisorOneNodeTask<VisorPartitionReconciliationTaskArg, ReconciliationResult> {
    /** */
    private static final long serialVersionUID = 0L;

    @Override protected Collection<UUID> jobNodes(VisorTaskArgument<VisorPartitionReconciliationTaskArg> arg) {
        // Perhaps we can always use coordinator node to start partition reconciliation processor.
        // In that case VisorCoordinatorNodeTask can be used instead of VisorOneNodeTask.
        if (arg.getArgument().fastCheck()) {
            ClusterNode crd = ignite.context().discovery().discoCache().oldestAliveServerNode();

            Collection<UUID> nids = new ArrayList<>(1);

            nids.add(crd == null ? ignite.localNode().id() : crd.id());

            return nids;
        }

        return arg.getNodes();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override protected VisorJob<VisorPartitionReconciliationTaskArg, ReconciliationResult> job(
        VisorPartitionReconciliationTaskArg arg
    ) {
        return new VisorPartitionReconciliationJob(arg, debug, PartitionReconciliationProcessorTask.class);
    }
}
