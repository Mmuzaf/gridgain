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

package org.apache.ignite.resources;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a field or a setter method for injection of {@link org.apache.ignite.compute.ComputeLoadBalancer}.
 * Specific implementation for grid load balancer is defined by
 * {@link org.apache.ignite.spi.loadbalancing.LoadBalancingSpi}
 * which is provided to grid via {@link org.apache.ignite.configuration.IgniteConfiguration}..
 * <p>
 * Load balancer can be injected into instances of following classes:
 * <ul>
 * <li>{@link org.apache.ignite.compute.ComputeTask}</li>
 * </ul>
 * <p>
 * Here is how injection would typically happen:
 * <pre name="code" class="java">
 * public class MyGridTask extends ComputeTask&lt;String, Integer&gt; {
 *    &#64;LoadBalancerResource
 *    private ComputeLoadBalancer balancer;
 * }
 * </pre>
 * or
 * <pre name="code" class="java">
 * public class MyGridTask extends ComputeTask&lt;String, Integer&gt; {
 *     ...
 *     private ComputeLoadBalancer balancer;
 *     ...
 *     &#64;LoadBalancerResource
 *     public void setBalancer(ComputeLoadBalancer balancer) {
 *         this.balancer = balancer;
 *     }
 *     ...
 * }
 * </pre>
 * <p>
 * See {@link org.apache.ignite.configuration.IgniteConfiguration#getLoadBalancingSpi()} for Grid configuration details.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface LoadBalancerResource {
    // No-op.
}