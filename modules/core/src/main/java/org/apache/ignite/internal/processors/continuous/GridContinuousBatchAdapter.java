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

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.apache.ignite.util.deque.FastSizeDeque;

/**
 * Continuous routine batch adapter.
 */
public class GridContinuousBatchAdapter implements GridContinuousBatch {
    /** Buffer. */
    protected final FastSizeDeque<Object> buf = new FastSizeDeque<>(new ConcurrentLinkedDeque<>());

    /** {@inheritDoc} */
    @Override public void add(Object obj) {
        assert obj != null;

        buf.add(obj);
    }

    /** {@inheritDoc} */
    @Override public Collection<Object> collect() {
        return buf;
    }

    /** {@inheritDoc} */
    @Override public int size() {
        return buf.sizex();
    }
}
