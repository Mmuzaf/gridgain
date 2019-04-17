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

package org.apache.ignite.plugin;

import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.util.typedef.X;
import org.apache.ignite.spi.IgniteSpiException;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test node validation on join by plugin.
 */
@RunWith(JUnit4.class)
public class PluginNodeValidationTest extends GridCommonAbstractTest {

    /** */
    private volatile String token;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);

        cfg.setDataStorageConfiguration(new DataStorageConfiguration()
            .setDefaultDataRegionConfiguration(new DataRegionConfiguration()
                .setMaxSize(100L * 1024 * 1024)
                .setPersistenceEnabled(true)));

        cfg.setConsistentId(igniteInstanceName);

        cfg.setPluginConfigurations(new NodeValidationPluginProvider.NodeValidationPluginConfiguration(token));

        return cfg;
    }

    /** Tests that node join fails due failure in node validation. */
    @Test
    public void testValidationException() throws Exception {
        token = "123456";

        startGrid(0);

        token = "abcdef";

        try {
            startGrid(1);
        } catch (Exception ex) {
            assertTrue("Wrong exception type for validation error", X.hasCause(ex, IgniteSpiException.class));

            return;
        }

        fail("Exception is expected due validation error in plugin");
    }

    /** Tests that node joins on successful node validation by plugin. */
    @Test
    public void testSuccessfulValidation() throws Exception {
        token = "123456";

        startGrid(0);
        startGrid(1);
    }

    /** Stop all nodes after each test. */
    @After
    public void after() {
        stopAllGrids();
    }

    /** Enables plugin before test start. */
    @BeforeClass
    public static void enablePlugin() {
        NodeValidationPluginProvider.setEnabled(true);
    }

    /** Disable plugin after test end. */
    @AfterClass
    public static void disablePlugin() {
        NodeValidationPluginProvider.setEnabled(false);
    }
}
