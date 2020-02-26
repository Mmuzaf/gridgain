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

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;

/**
 * This annotation marks API elements (such as interfaces, methods, annotations and whole API packages) as experimental
 * meaning that the API is not finalized yet and may be changed or replaced in future Ignite releases.
 * <p>
 * Such APIs are exposed so that users can make use of a feature before the API has been stabilized. The expectation is
 * that an API element should be "eventually" stabilized. Incompatible changes are allowed for such APIs: API may be
 * removed, changed or stabilized in future Ignite releases (both minor and maintenance).
 */
@Target(value = {TYPE, METHOD, ANNOTATION_TYPE, PACKAGE, FIELD})
public @interface IgniteExperimental {
}
