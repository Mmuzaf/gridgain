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

package org.apache.ignite.ml.tree;

import org.apache.ignite.ml.dataset.feature.extractor.impl.ArraysVectorizer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link DecisionTreeRegressionTrainer}.
 */
@RunWith(Parameterized.class)
public class DecisionTreeRegressionTrainerTest {
    /** Number of parts to be tested. */
    private static final int[] partsToBeTested = new int[] {1, 2, 3, 4, 5, 7};

    /** Number of partitions. */
    @Parameterized.Parameter()
    public int parts;

    /** Use index [= 1 if true]. */
    @Parameterized.Parameter(1)
    public int useIdx;

    /** Test parameters. */
    @Parameterized.Parameters(name = "Data divided on {0} partitions. Use index = {1}.")
    public static Iterable<Integer[]> data() {
        List<Integer[]> res = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            for (int part : partsToBeTested)
                res.add(new Integer[] {part, i});
        }

        return res;
    }

    /** */
    @Test
    public void testFit() {
        int size = 100;

        Map<Integer, double[]> data = new HashMap<>();

        Random rnd = new Random(0);
        for (int i = 0; i < size; i++) {
            double x = rnd.nextDouble() - 0.5;
            data.put(i, new double[]{x, x > 0 ? 1 : 0});
        }

        DecisionTreeRegressionTrainer trainer = new DecisionTreeRegressionTrainer(1, 0)
            .withUsingIdx(useIdx == 1);

        DecisionTreeNode tree = trainer.fit(data, parts, new ArraysVectorizer<Integer>().labeled(1));

        assertTrue(tree instanceof DecisionTreeConditionalNode);

        DecisionTreeConditionalNode node = (DecisionTreeConditionalNode) tree;

        assertEquals(0, node.getThreshold(), 1e-3);

        assertTrue(node.getThenNode() instanceof DecisionTreeLeafNode);
        assertTrue(node.getElseNode() instanceof DecisionTreeLeafNode);

        DecisionTreeLeafNode thenNode = (DecisionTreeLeafNode) node.getThenNode();
        DecisionTreeLeafNode elseNode = (DecisionTreeLeafNode) node.getElseNode();

        assertEquals(1, thenNode.getVal(), 1e-10);
        assertEquals(0, elseNode.getVal(), 1e-10);
    }
}
