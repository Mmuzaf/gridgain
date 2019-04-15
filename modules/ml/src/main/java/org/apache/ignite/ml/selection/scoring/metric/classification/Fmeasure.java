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

package org.apache.ignite.ml.selection.scoring.metric.classification;

import org.apache.ignite.ml.selection.scoring.LabelPair;

import java.util.Iterator;

/**
 * F-measure calculator.
 *
 * @param <L> Type of a label (truth or prediction).
 */
public class Fmeasure<L> extends ClassMetric<L> {
    /**
     * The class of interest or positive class.
     *
     * @param clsLb The label.
     */
    public Fmeasure(L clsLb) {
        super(clsLb);
    }

    /** {@inheritDoc} */
    @Override public double score(Iterator<LabelPair<L>> it) {
        if (clsLb != null) {
            long tp = 0;
            long fn = 0;
            long fp = 0;
            double recall;
            double precision;

            while (it.hasNext()) {
                LabelPair<L> e = it.next();

                L prediction = e.getPrediction();
                L truth = e.getTruth();

                if (clsLb.equals(truth)) {
                    if (prediction.equals(truth))
                        tp++;
                    else
                        fn++;
                }

                if (clsLb.equals(prediction) && !prediction.equals(truth))
                    fp++;
            }
            long denominatorRecall = tp + fn;

            long denominatorPrecision = tp + fp;

            // according to https://github.com/dice-group/gerbil/wiki/Precision,-Recall-and-F1-measure
            recall = denominatorRecall == 0 ? 1 : (double)tp / denominatorRecall;
            precision = denominatorPrecision == 0 ? 1 : (double)tp / denominatorPrecision;

            return 2 * (recall * precision) / (recall + precision);
        }
        else
            return Double.NaN;
    }

    /** {@inheritDoc} */
    @Override public String name() {
        return "F-measure for class with label " + clsLb;
    }
}
