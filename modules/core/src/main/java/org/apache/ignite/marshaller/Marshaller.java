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

package org.apache.ignite.marshaller;

import java.io.InputStream;
import java.io.OutputStream;
import org.apache.ignite.IgniteBinary;
import org.apache.ignite.IgniteCheckedException;
import org.jetbrains.annotations.Nullable;

/**
 * {@code Marshaller} allows to marshal or unmarshal objects in grid. It provides
 * serialization/deserialization mechanism for all instances that are sent across networks
 * or are otherwise serialized.
 * <p>
 * Ignite provides the following {@code Marshaller} implementations:
 * <ul>
 * <li>Default binary marshaller. Will be used when no other marshaller is explicitly set to the
 * configuration. For more information, see {@link IgniteBinary}.</li>
 * <li>{@link org.apache.ignite.marshaller.jdk.JdkMarshaller}</li>
 * </ul>
 * <p>
 * Below are examples of marshaller configuration, usage, and injection into tasks, jobs,
 * and SPI's.
 * <h2 class="header">Java Example</h2>
 * {@code Marshaller} can be explicitly configured in code.
 * <pre name="code" class="java">
 * JdkMarshaller marshaller = new JdkMarshaller();
 *
 * IgniteConfiguration cfg = new IgniteConfiguration();
 *
 * // Override marshaller.
 * cfg.setMarshaller(marshaller);
 *
 * // Starts grid.
 * G.start(cfg);
 * </pre>
 * <h2 class="header">Spring Example</h2>
 * Marshaller can be configured from Spring XML configuration file:
 * <pre name="code" class="xml">
 * &lt;bean id="grid.custom.cfg" class="org.apache.ignite.configuration.IgniteConfiguration" singleton="true"&gt;
 *     ...
 *     &lt;property name="marshaller"&gt;
 *         &lt;bean class="org.apache.ignite.marshaller.jdk.JdkMarshaller"/&gt;
 *     &lt;/property&gt;
 *     ...
 * &lt;/bean&gt;
 * </pre>
 * <p>
 * <img src="http://ignite.apache.org/images/spring-small.png">
 * <br>
 * For information about Spring framework visit <a href="http://www.springframework.org/">www.springframework.org</a>
 */
public interface Marshaller {
    /**
     * Sets marshaller context.
     *
     * @param ctx Marshaller context.
     */
    public void setContext(MarshallerContext ctx);

    /**
     * Marshals object to the output stream. This method should not close
     * given output stream.
     *
     * @param obj Object to marshal.
     * @param out Output stream to marshal into.
     * @throws IgniteCheckedException If marshalling failed.
     */
    public void marshal(@Nullable Object obj, OutputStream out) throws IgniteCheckedException;

    /**
     * Marshals object to byte array.
     *
     * @param obj Object to marshal.
     * @return Byte array.
     * @throws IgniteCheckedException If marshalling failed.
     */
    public byte[] marshal(@Nullable Object obj) throws IgniteCheckedException;

    /**
     * Unmarshals object from the input stream using given class loader.
     * This method should not close given input stream.
     *
     * @param <T> Type of unmarshalled object.
     * @param in Input stream.
     * @param clsLdr Class loader to use.
     * @return Unmarshalled object.
     * @throws IgniteCheckedException If unmarshalling failed.
     */
    public <T> T unmarshal(InputStream in, @Nullable ClassLoader clsLdr) throws IgniteCheckedException;

    /**
     * Unmarshals object from byte array using given class loader.
     *
     * @param <T> Type of unmarshalled object.
     * @param arr Byte array.
     * @param clsLdr Class loader to use.
     * @return Unmarshalled object.
     * @throws IgniteCheckedException If unmarshalling failed.
     */
    public <T> T unmarshal(byte[] arr, @Nullable ClassLoader clsLdr) throws IgniteCheckedException;
}