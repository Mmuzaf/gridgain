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

package org.apache.loadtests.direct.singlesplit;

import org.apache.ignite.compute.ComputeTaskSession;
import org.apache.ignite.compute.gridify.Gridify;
import org.apache.loadtests.gridify.GridifyLoadTestTask;

/**
 * Single split test job target.
 */
public class SingleSplitTestJobTarget {
    /**
     * @param level Level.
     * @param jobSes Job session.
     * @return ALways returns {@code 1}.
     */
    @SuppressWarnings("unused")
    @Gridify(taskClass = GridifyLoadTestTask.class, timeout = 10000)
    public int executeLoadTestJob(int level, ComputeTaskSession jobSes) {
        assert level > 0;
        assert jobSes != null;

        jobSes.setAttribute("1st", 10000);
        jobSes.setAttribute("2nd", 10000);

        return 1;
    }
}