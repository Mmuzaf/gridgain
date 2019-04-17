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

package org.apache.ignite.spring;

import java.io.FileInputStream;
import java.net.URL;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.util.typedef.internal.U;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Checks starts from Stream.
 */
@RunWith(JUnit4.class)
public class IgniteStartFromStreamConfigurationTest extends GridCommonAbstractTest {
    /**
     * Tests starts from stream.
     *
     * @throws Exception If failed.
     */
    @Test
    public void testStartFromStream() throws Exception {
        String cfg = "examples/config/example-cache.xml";

        URL cfgLocation = U.resolveIgniteUrl(cfg);

        try (Ignite grid = Ignition.start(new FileInputStream(cfgLocation.getFile()))) {
            grid.cache(DEFAULT_CACHE_NAME).put("1", "1");

            assert grid.cache(DEFAULT_CACHE_NAME).get("1").equals("1");

            IgniteConfiguration icfg = Ignition.loadSpringBean(new FileInputStream(cfgLocation.getFile()), "ignite.cfg");

            assert icfg.getCacheConfiguration()[0].getAtomicityMode() == CacheAtomicityMode.ATOMIC;
        }
    }

}
