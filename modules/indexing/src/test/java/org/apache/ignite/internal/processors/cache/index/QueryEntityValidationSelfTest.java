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

package org.apache.ignite.internal.processors.cache.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.cache.QueryIndex;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.testframework.GridTestUtils;
import org.junit.Test;

/**
 * Tests for query entity validation.
 */
public class QueryEntityValidationSelfTest extends AbstractIndexingCommonTest {
    /** Cache name. */
    private static final String CACHE_NAME = "cache";

    /** {@inheritDoc} */
    @Override protected void beforeTestsStarted() throws Exception {
        startGrid(0);
    }

    /**
     * Test null value type.
     *
     * @throws Exception If failed.
     */
    @Test
    public void testValueTypeNull() throws Exception {
        final CacheConfiguration ccfg = new CacheConfiguration().setName(CACHE_NAME);

        QueryEntity entity = new QueryEntity();

        entity.setKeyType("Key");

        ccfg.setQueryEntities(Collections.singleton(entity));

        GridTestUtils.assertThrows(log, new Callable<Void>() {
            @Override public Void call() throws Exception {
                grid(0).createCache(ccfg);

                return null;
            }
        }, IgniteCheckedException.class, "Value type cannot be null or empty");
    }

    /**
     * Test failure if index type is null.
     *
     * @throws Exception If failed.
     */
    @Test
    public void testIndexTypeNull() throws Exception {
        final CacheConfiguration ccfg = new CacheConfiguration().setName(CACHE_NAME);

        QueryEntity entity = new QueryEntity();

        entity.setKeyType("Key");
        entity.setValueType("Value");

        LinkedHashMap<String, String> fields = new LinkedHashMap<>();

        fields.put("a", Integer.class.getName());

        entity.setFields(fields);

        LinkedHashMap<String, Boolean> idxFields = new LinkedHashMap<>();

        idxFields.put("a", true);

        QueryIndex idx = new QueryIndex().setName("idx").setFields(idxFields).setIndexType(null);

        List<QueryIndex> idxs = new ArrayList<>();

        idxs.add(idx);

        entity.setIndexes(idxs);

        ccfg.setQueryEntities(Collections.singleton(entity));

        GridTestUtils.assertThrows(log, new Callable<Void>() {
            @Override public Void call() throws Exception {
                grid(0).createCache(ccfg);

                return null;
            }
        }, IgniteCheckedException.class, "Index type is not set");
    }

    /**
     * Test duplicated index name.
     *
     * @throws Exception If failed.
     */
    @Test
    public void testIndexNameDuplicate() throws Exception {
        final CacheConfiguration ccfg = new CacheConfiguration().setName(CACHE_NAME);

        QueryEntity entity = new QueryEntity();

        entity.setKeyType("Key");
        entity.setValueType("Value");

        LinkedHashMap<String, String> fields = new LinkedHashMap<>();

        fields.put("a", Integer.class.getName());
        fields.put("b", Integer.class.getName());

        entity.setFields(fields);

        LinkedHashMap<String, Boolean> idx1Fields = new LinkedHashMap<>();
        LinkedHashMap<String, Boolean> idx2Fields = new LinkedHashMap<>();

        idx1Fields.put("a", true);
        idx1Fields.put("b", true);

        QueryIndex idx1 = new QueryIndex().setName("idx").setFields(idx1Fields);
        QueryIndex idx2 = new QueryIndex().setName("idx").setFields(idx2Fields);

        List<QueryIndex> idxs = new ArrayList<>();

        idxs.add(idx1);
        idxs.add(idx2);

        entity.setIndexes(idxs);

        ccfg.setQueryEntities(Collections.singleton(entity));

        GridTestUtils.assertThrows(log, new Callable<Void>() {
            @Override public Void call() throws Exception {
                grid(0).createCache(ccfg);

                return null;
            }
        }, IgniteCheckedException.class, "Duplicate index name");
    }
}
