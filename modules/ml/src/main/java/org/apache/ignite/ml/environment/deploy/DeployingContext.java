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

package org.apache.ignite.ml.environment.deploy;

/**
 * Class represents user's class loading environment for specific remote job.
 * This contexst is used to determine class loader for missing classes on worker node.
 */
public interface DeployingContext {
    /**
     * @return Class represents client-defined class.
     */
    public Class<?> userClass();

    /**
     * @return Client class loader.
     */
    public ClassLoader clientClassLoader();

    /**
     * Inits context by other context.
     *
     * @param other Other context.
     */
    public void init(DeployingContext other);

    /**
     * Inits context by any client-defined object.
     *
     * @param jobObject Specific class for job.
     */
    public void initByClientObject(Object jobObject);

    /**
     * @return Unitialized deploy context.
     */
    public static DeployingContext unitialized() {
        return new DeployingContextImpl();
    }
}
