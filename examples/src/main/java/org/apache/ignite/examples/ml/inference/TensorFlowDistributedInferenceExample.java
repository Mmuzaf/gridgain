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

package org.apache.ignite.examples.ml.inference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.internal.util.IgniteUtils;
import org.apache.ignite.ml.inference.Model;
import org.apache.ignite.ml.inference.builder.IgniteDistributedModelBuilder;
import org.apache.ignite.ml.inference.parser.ModelParser;
import org.apache.ignite.ml.inference.parser.TensorFlowSavedModelModelParser;
import org.apache.ignite.ml.inference.reader.FileSystemModelReader;
import org.apache.ignite.ml.inference.reader.ModelReader;
import org.apache.ignite.ml.util.MnistUtils;
import org.tensorflow.Tensor;

/**
 * This example demonstrates how to: load TensorFlow model into Java, make inference in distributed environment using
 * Apache Ignite services.
 */
public class TensorFlowDistributedInferenceExample {
    /** Path to the directory with saved TensorFlow model. */
    private static final String MODEL_PATH = "examples/src/main/resources/models/mnist_tf_model";

    /** Path to the MNIST images data. */
    private static final String MNIST_IMG_PATH = "examples/src/main/resources/datasets/t10k-images-idx3-ubyte";

    /** Path to the MNIST labels data. */
    private static final String MNIST_LBL_PATH = "examples/src/main/resources/datasets/t10k-labels-idx1-ubyte";

    /** Run example. */
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        try (Ignite ignite = Ignition.start("examples/config/example-ignite.xml")) {
            File mdlRsrc = IgniteUtils.resolveIgnitePath(MODEL_PATH);
            if (mdlRsrc == null)
                throw new IllegalArgumentException("Resource not found [resource_path=" + MODEL_PATH + "]");

            ModelReader reader = new FileSystemModelReader(mdlRsrc.getPath());

            ModelParser<double[], Long, ?> parser = new TensorFlowSavedModelModelParser<double[], Long>("serve")

                .withInput("Placeholder", doubles -> {
                    float[][][] reshaped = new float[1][28][28];
                    for (int i = 0; i < doubles.length; i++)
                        reshaped[0][i / 28][i % 28] = (float)doubles[i];
                    return Tensor.create(reshaped);
                })

                .withOutput(Collections.singletonList("ArgMax"), collectedTensors -> collectedTensors.get("ArgMax")
                    .copyTo(new long[1])[0]);

            List<MnistUtils.MnistLabeledImage> images = MnistUtils.mnistAsList(
                Objects.requireNonNull(IgniteUtils.resolveIgnitePath(MNIST_IMG_PATH)).getPath(),
                Objects.requireNonNull(IgniteUtils.resolveIgnitePath(MNIST_LBL_PATH)).getPath(),
                new Random(0),
                10000
            );

            long t0 = System.currentTimeMillis();

            try (Model<double[], Future<Long>> threadedMdl = new IgniteDistributedModelBuilder(ignite, 4, 4)
                .build(reader, parser)) {
                List<Future<?>> futures = new ArrayList<>(images.size());
                for (MnistUtils.MnistLabeledImage image : images)
                    futures.add(threadedMdl.predict(image.getPixels()));
                for (Future<?> f : futures)
                    f.get();
            }

            long t1 = System.currentTimeMillis();

            System.out.println("Threaded model throughput: " + images.size() / ((t1 - t0) / 1000.0) + " req/sec");
        }
    }
}
