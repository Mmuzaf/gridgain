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

package org.apache.ignite.internal.util;

import java.io.Serializable;
import java.util.AbstractMap;

/**
 * Makes {@link AbstractMap} as {@link Serializable} and is
 * useful for making anonymous serializable maps. It has no extra logic or state in addition
 * to {@link AbstractMap}.
 * <b>NOTE:</b> methods {@link #get(Object)}, {@link #remove(Object)} and
 * {@link #containsKey(Object)} implemented in {@link AbstractMap} <b>fully iterate through
 * collection</b> so you need to make sure to override these methods if it's possible to create
 * efficient implementations.
 */
public abstract class GridSerializableMap<K, V> extends AbstractMap<K, V> implements Serializable {
    /** */
    private static final long serialVersionUID = 0L;

    // No-op.
}