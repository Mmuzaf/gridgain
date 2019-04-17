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

package org.apache.ignite;

import javax.swing.JOptionPane;
import org.apache.ignite.internal.util.typedef.G;

/**
 * Starts test node.
 */
public class GridTestNodeStartup {
    /**
     *
     */
    private GridTestNodeStartup() {
        // No-op.
    }

    /**
     * @param args Arguments.
     * @throws Exception If failed.
     */
    public static void main(String[] args) throws Exception {
        try {
            G.start("examples/config/example-cache.xml");

            JOptionPane.showMessageDialog(null, "Press OK to stop test node.");
        }
        finally {
            G.stopAll(false);
        }
    }
}