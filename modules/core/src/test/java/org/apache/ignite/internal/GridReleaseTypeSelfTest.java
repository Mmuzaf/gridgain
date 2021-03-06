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

package org.apache.ignite.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.lang.IgniteProductVersion;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;

/**
 * Test grids starting with non compatible release types.
 */
public class GridReleaseTypeSelfTest extends GridCommonAbstractTest {
    /** */
    private String nodeVer;

    /** */
    private boolean clientMode;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);

        if (clientMode)
            cfg.setClientMode(true);

        TcpDiscoverySpi discoSpi = new TcpDiscoverySpi() {
            @Override public void setNodeAttributes(Map<String, Object> attrs,
                IgniteProductVersion ver) {
                super.setNodeAttributes(attrs, ver);

                attrs.put(IgniteNodeAttributes.ATTR_BUILD_VER, nodeVer);
            }
        };

        discoSpi.setIpFinder(sharedStaticIpFinder).setForceServerMode(true);

        cfg.setDiscoverySpi(discoSpi);

        return cfg;
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        clientMode = false;

        stopAllGrids();
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testOsEditionDoesNotSupportRollingUpdates() throws Exception {
        nodeVer = "1.0.0";

        startGrid(0);

        try {
            nodeVer = "1.0.1";

            startGrid(1);

            fail("Exception has not been thrown.");
        }
        catch (IgniteCheckedException e) {
            StringWriter errors = new StringWriter();

            e.printStackTrace(new PrintWriter(errors));

            String stackTrace = errors.toString();

            if (!stackTrace.contains("Local node and remote node have different version numbers"))
                throw e;
        }
    }

    /**
     * @throws Exception If failed.
     */
    @Test
    public void testOsEditionDoesNotSupportRollingUpdatesClientMode() throws Exception {
        nodeVer = "1.0.0";

        startGrid(0);

        try {
            nodeVer = "1.0.1";
            clientMode = true;

            startGrid(1);

            fail("Exception has not been thrown.");
        }
        catch (IgniteCheckedException e) {
            StringWriter errors = new StringWriter();

            e.printStackTrace(new PrintWriter(errors));

            String stackTrace = errors.toString();

            if (!stackTrace.contains("Local node and remote node have different version numbers"))
                throw e;
        }
    }
}
