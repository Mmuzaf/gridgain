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

import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteException;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.events.Event;
import org.apache.ignite.internal.util.typedef.F;
import org.apache.ignite.spi.eventstorage.NoopEventStorageSpi;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.apache.ignite.testframework.junits.common.GridCommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Event storage tests with default no-op spi.
 */
@GridCommonTest(group = "Kernal Self")
@RunWith(JUnit4.class)
public class GridEventStorageDefaultExceptionTest  extends GridCommonAbstractTest {
    /** */
    public GridEventStorageDefaultExceptionTest() {
        super(/*start grid*/true);
    }

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(final String gridName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(gridName);

        cfg.setEventStorageSpi(new NoopEventStorageSpi());

        return cfg;
    }

    /**
     * @throws Exception In case of error.
     */
    @Test
    public void testLocalNodeEventStorage() throws Exception {
        try {
            grid().events().localQuery(F.<Event>alwaysTrue());

            assert false : "Exception must be thrown.";
        }
        catch (IgniteException e) {
            assertTrue(
                "Wrong exception message: " + e.getMessage(),
                e.getMessage().startsWith("Failed to query events because default no-op event storage SPI is used."));
        }
    }

    /**
     * @throws Exception In case of error.
     */
    @Test
    public void testRemoteNodeEventStorage() throws Exception {
        try {
            grid().events().remoteQuery(F.<Event>alwaysTrue(), 0);

            assert false : "Exception should be thrown";
        }
        catch (IgniteException e) {
            assertTrue(
                "Wrong exception message: " + e.getMessage(),
                e.getMessage().startsWith("Failed to query events due to exception on remote node."));

            boolean found = false;

            Throwable t = e;

            while ((t = t.getCause()) != null) {
                if (t instanceof IgniteCheckedException && t.getMessage().startsWith(
                        "Failed to query events because default no-op event storage SPI is used.")) {
                    found = true;

                    break;
                }
            }

            assertTrue("Incorrect exception thrown.", found);
        }
    }
}
