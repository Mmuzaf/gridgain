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

package org.apache.ignite.scalar.pimps

import org.apache.ignite.cluster.ClusterGroup
import org.apache.ignite.scalar.ScalarConversions
import org.jetbrains.annotations._

/**
 * This trait provide mixin for properly typed version of `GridProjection#with...()` methods.
 *
 * Method on `GridProjection` always returns an instance of type `GridProjection` even when
 * called on a sub-class. This trait's methods return the instance of the same type
 * it was called on.
 */
trait ScalarTaskThreadContext[T <: ClusterGroup] extends ScalarConversions { this: PimpedType[T] =>
    /**
     * Properly typed version of `Compute#withName(...)` method.
     *
     * @param taskName Name of the task.
     */
    def withName$(@Nullable taskName: String): T =
        value.ignite().compute(value).withName(taskName).asInstanceOf[T]

    /**
     * Properly typed version of `Compute#withNoFailover()` method.
     */
    def withNoFailover$(): T =
        value.ignite().compute(value).withNoFailover().asInstanceOf[T]
}
