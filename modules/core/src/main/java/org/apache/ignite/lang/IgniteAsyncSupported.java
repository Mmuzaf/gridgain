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

package org.apache.ignite.lang;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that method can be executed asynchronously if async mode is enabled.
 * To enable async mode, invoke {@link IgniteAsyncSupport#withAsync()} method on the API.
 * The future for the async method can be retrieved via {@link IgniteAsyncSupport#future()} method
 * right after the execution of an asynchronous method.
 *
 * TODO coding example.
 *
 * @deprecated since 2.0. Please use specialized asynchronous methods.
 * @see IgniteAsyncSupport
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Deprecated
public @interface IgniteAsyncSupported {
    // No-op.
}