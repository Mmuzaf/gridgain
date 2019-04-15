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

package org.apache.ignite.mesos.resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.ignite.mesos.ClusterProperties;

import static org.apache.ignite.mesos.resource.ResourceHandler.CONFIG_PREFIX;
import static org.apache.ignite.mesos.resource.ResourceHandler.DEFAULT_CONFIG;
import static org.apache.ignite.mesos.resource.ResourceHandler.IGNITE_PREFIX;
import static org.apache.ignite.mesos.resource.ResourceHandler.LIBS_PREFIX;

/**
 * Provides path to user's libs and config file.
 */
public class ResourceProvider {
    /** */
    private static final Logger log = Logger.getLogger(ResourceProvider.class.getSimpleName());

    /** Ignite url. */
    private String igniteUrl;

    /** Resources. */
    private Collection<String> libsUris;

    /** Url config. */
    private String cfgUrl;

    /** Config name. */
    private String cfgName;

    /**
     * @param props Cluster properties.
     * @param provider Ignite provider.
     * @param baseUrl Base url.
     */
    public void init(ClusterProperties props, IgniteProvider provider, String baseUrl) throws IOException {
        if (props.ignitePackageUrl() == null && props.ignitePackagePath() == null) {
            // Downloading ignite.
            try {
                igniteUrl = baseUrl + IGNITE_PREFIX + provider.getIgnite(props.igniteVer());
            }
            catch (Exception e) {
                log.log(Level.SEVERE, "Failed to download Ignite [err={0}, ver={1}].\n" +
                    "If application working behind NAT or Intranet and does not have access to external resources " +
                    "then you can use IGNITE_PACKAGE_URL or IGNITE_PACKAGE_PATH property that allow to use local " +
                    "resources.",
                    new Object[]{e, props.igniteVer()});
            }
        }

        if (props.ignitePackagePath() != null) {
            Path ignitePackPath = Paths.get(props.ignitePackagePath());

            if (Files.exists(ignitePackPath) && !Files.isDirectory(ignitePackPath)) {
                try {
                    String fileName = provider.copyToWorkDir(props.ignitePackagePath());

                    assert fileName != null;

                    igniteUrl = baseUrl + IGNITE_PREFIX + fileName;
                }
                catch (Exception e) {
                    log.log(Level.SEVERE, "Failed to copy Ignite to working directory [err={0}, path={1}].",
                        new Object[] {e, props.ignitePackagePath()});

                    throw e;
                }
            }
            else
                throw new IllegalArgumentException("Failed to find a ignite archive by path: "
                    + props.ignitePackagePath());
        }

        // Find all jar files into user folder.
        if (props.userLibs() != null && !props.userLibs().isEmpty()) {
            File libsDir = new File(props.userLibs());

            List<String> libs = new ArrayList<>();

            if (libsDir.isDirectory()) {
                File[] files = libsDir.listFiles();

                if (files != null) {
                    for (File lib : files) {
                        if (lib.isFile() && lib.canRead() &&
                            (lib.getName().endsWith(".jar") || lib.getName().endsWith(".JAR")))
                            libs.add(baseUrl + LIBS_PREFIX + lib.getName());
                    }
                }
            }

            libsUris = libs.isEmpty() ? null : libs;
        }

        // Set configuration url.
        if (props.igniteCfg() != null) {
            File cfg = new File(props.igniteCfg());

            if (cfg.isFile() && cfg.canRead()) {
                cfgUrl = baseUrl + CONFIG_PREFIX + cfg.getName();

                cfgName = cfg.getName();
            }
        }
        else {
            cfgName = "ignite-default-config.xml";

            cfgUrl = baseUrl + DEFAULT_CONFIG + cfgName;
        }
    }

    /**
     * @return Config name.
     */
    public String configName() {
        return cfgName;
    }

    /**
     * @return Ignite url.
     */
    public String igniteUrl() {
        return igniteUrl;
    }

    /**
     * @return Urls to user's libs.
     */
    public Collection<String> resourceUrl() {
        return libsUris;
    }

    /**
     * @return Url to config file.
     */
    public String igniteConfigUrl() {
        return cfgUrl;
    }
}