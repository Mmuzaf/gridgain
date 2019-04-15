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

package org.apache.ignite.internal.processors.cache;

import java.lang.reflect.Field;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.cache.affinity.AffinityKey;
import org.apache.ignite.cache.affinity.AffinityKeyMapped;
import org.apache.ignite.cache.affinity.AffinityKeyMapper;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.internal.util.GridArgumentCheck;
import org.apache.ignite.internal.util.GridReflectionCache;
import org.apache.ignite.internal.util.typedef.P1;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.resources.LoggerResource;
import org.jetbrains.annotations.Nullable;

/**
 * Default key affinity mapper. If key class has annotation {@link AffinityKeyMapped},
 * then the value of annotated field will be used to get affinity value instead
 * of the key itself. If there is no annotation, then the key is used as is.
 * <p>
 * Convenience affinity key adapter, {@link AffinityKey} can be used in
 * conjunction with this mapper to automatically provide custom affinity keys for cache keys.
 * <p>
 * If non-default affinity mapper is used, is should be provided via
 * {@link CacheConfiguration#getAffinityMapper()} configuration property.
 */
public class GridCacheDefaultAffinityKeyMapper implements AffinityKeyMapper {
    /** */
    private static final long serialVersionUID = 0L;

    /** Injected ignite instance. */
    protected transient Ignite ignite;

    /** Reflection cache. */
    private GridReflectionCache reflectCache = new GridReflectionCache(
        new P1<Field>() {
            @Override public boolean apply(Field f) {
                // Account for anonymous inner classes.
                return f.getAnnotation(AffinityKeyMapped.class) != null;
            }
        },
        null
    );

    /** Logger. */
    @LoggerResource
    protected transient IgniteLogger log;

    /**
     * If key class has annotation {@link AffinityKeyMapped},
     * then the value of annotated method or field will be used to get affinity value instead
     * of the key itself. If there is no annotation, then the key is returned as is.
     *
     * @param key Key to get affinity key for.
     * @return Affinity key for given key.
     */
    @Override public Object affinityKey(Object key) {
        GridArgumentCheck.notNull(key, "key");

        try {
            Object o = reflectCache.firstFieldValue(key);

            if (o != null)
                return o;
        }
        catch (IgniteCheckedException e) {
            U.error(log, "Failed to access affinity field for key [field=" +
                reflectCache.firstField(key.getClass()) + ", key=" + key + ']', e);
        }

        return key;
    }

    /**
     * @param cls Key class.
     * @return Name of
     */
    @Nullable public String affinityKeyPropertyName(Class<?> cls) {
        Field field = reflectCache.firstField(cls);

        if (field != null)
            return field.getName();

        return null;
    }

    /**
     * @param ignite Ignite.
     */
    @IgniteInstanceResource
    public void ignite(Ignite ignite) {
        this.ignite = ignite;
    }

    /** {@inheritDoc} */
    @Override public void reset() {
        // No-op.
    }
}