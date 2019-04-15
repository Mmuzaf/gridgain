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

package org.apache.ignite.spi.collision.jobstealing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.apache.ignite.GridTestTaskSession;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.internal.ClusterMetricsSnapshot;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.lang.IgniteUuid;
import org.apache.ignite.spi.collision.CollisionJobContext;
import org.apache.ignite.spi.collision.GridCollisionTestContext;
import org.apache.ignite.spi.collision.GridTestCollisionJobContext;
import org.apache.ignite.spi.failover.jobstealing.JobStealingFailoverSpi;
import org.apache.ignite.testframework.GridSpiTestContext;
import org.apache.ignite.testframework.GridTestNode;
import org.apache.ignite.testframework.junits.spi.GridSpiAbstractTest;
import org.apache.ignite.testframework.junits.spi.GridSpiTest;
import org.apache.ignite.testframework.junits.spi.GridSpiTestConfig;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import static org.apache.ignite.internal.IgniteNodeAttributes.ATTR_SPI_CLASS;
import static org.apache.ignite.spi.collision.jobstealing.JobStealingCollisionSpi.THIEF_NODE_ATTR;

/**
 * Job stealing collision SPI topology test.
 */
@GridSpiTest(spi = JobStealingCollisionSpi.class, group = "Collision SPI")
public class GridJobStealingCollisionSpiCustomTopologySelfTest extends
    GridSpiAbstractTest<JobStealingCollisionSpi> {
    /** */
    private static GridTestNode rmtNode1;

    /** */
    private static GridTestNode rmtNode2;

    /** */
    public GridJobStealingCollisionSpiCustomTopologySelfTest() {
        super(true /*start spi*/);
    }

    /**
     * @return Wait jobs threshold.
     */
    @GridSpiTestConfig
    public int getWaitJobsThreshold() {
        return 0;
    }

    /**
     * @return Active jobs threshold.
     */
    @GridSpiTestConfig
    public int getActiveJobsThreshold() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override protected GridSpiTestContext initSpiContext() throws Exception {
        GridSpiTestContext ctx = super.initSpiContext();

        GridTestNode locNode = new GridTestNode(UUID.randomUUID());

        ctx.setLocalNode(locNode);

        rmtNode1 = new GridTestNode(UUID.randomUUID());
        rmtNode2 = new GridTestNode(UUID.randomUUID());

        addSpiDependency(locNode);
        addSpiDependency(rmtNode1);
        addSpiDependency(rmtNode2);

        ClusterMetricsSnapshot metrics = new ClusterMetricsSnapshot();

        metrics.setCurrentWaitingJobs(2);

        rmtNode1.setMetrics(metrics);
        rmtNode2.setMetrics(metrics);

        ctx.addNode(rmtNode1);
        ctx.addNode(rmtNode2);

        return ctx;
    }

    /**
     * Adds Failover SPI attribute.
     *
     * @param node Node to add attribute to.
     * @throws Exception If failed.
     */
    private void addSpiDependency(GridTestNode node) throws Exception {
        node.addAttribute(U.spiAttribute(getSpi(), ATTR_SPI_CLASS), JobStealingFailoverSpi.class.getName());
    }

    /**
     * @param ctx Collision job context.
     */
    private void checkNoAction(GridTestCollisionJobContext ctx) {
        assert !ctx.isActivated();
        assert !ctx.isCanceled();
        assert ctx.getJobContext().getAttribute(THIEF_NODE_ATTR) == null;
    }

    /**
     * @throws Exception If test failed.
     */
    @Test
    public void testThiefNodeNotInTopology() throws Exception {
        List<CollisionJobContext> waitCtxs = new ArrayList<>(2);

        final ClusterNode node = getSpiContext().nodes().iterator().next();

        Collections.addAll(waitCtxs,
            new GridTestCollisionJobContext(createTaskSession(node), IgniteUuid.randomUuid()),
            new GridTestCollisionJobContext(createTaskSession(node), IgniteUuid.randomUuid()),
            new GridTestCollisionJobContext(createTaskSession(node), IgniteUuid.randomUuid()));

        Collection<CollisionJobContext> activeCtxs = new ArrayList<>(1);

        // Add active.
        Collections.addAll(
            activeCtxs,
            new GridTestCollisionJobContext(createTaskSession(node), IgniteUuid.randomUuid()));

        // Emulate message to steal 2 jobs.
        getSpiContext().triggerMessage(rmtNode2, new JobStealingRequest(2));

        getSpi().onCollision(new GridCollisionTestContext(activeCtxs, waitCtxs));

        // Check no action.
        checkNoAction((GridTestCollisionJobContext)waitCtxs.get(0));
        checkNoAction((GridTestCollisionJobContext)waitCtxs.get(1));
        checkNoAction((GridTestCollisionJobContext)waitCtxs.get(2));

        // Make sure that no message was sent.
        Serializable msg1 = getSpiContext().removeSentMessage(getSpiContext().localNode());

        assert msg1 == null;

        Serializable mgs2 = getSpiContext().removeSentMessage(rmtNode1);

        assert mgs2 == null;

        Serializable msg3 = getSpiContext().removeSentMessage(rmtNode2);

        assert msg3 == null;
    }

    /**
     * @param node Node.
     * @return Session.
     */
    private GridTestTaskSession createTaskSession(final ClusterNode node) {
        return new GridTestTaskSession() {
            @Nullable @Override public Collection<UUID> getTopology() {
                return Collections.singleton(node.id());
            }
        };
    }
}
