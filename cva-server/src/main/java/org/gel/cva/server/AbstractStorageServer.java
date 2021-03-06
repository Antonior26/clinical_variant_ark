/*
 * Copyright 2015-2016 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gel.cva.server;

import org.apache.commons.lang3.StringUtils;
import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.opencb.opencga.storage.core.config.StorageConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by imedina on 02/01/16.
 */
public abstract class AbstractStorageServer {

    protected int port;
    protected Path configDir;

    protected CvaConfiguration configuration;
    protected StorageConfiguration storageConfiguration;

    /**
     * This is the default StorageEngine to use when it is not provided by the client.
     */
    @Deprecated
    protected String defaultStorageEngine;

    protected static Logger logger = LoggerFactory.getLogger("org.opencb.opencga.server.AbstractStorageServer");

    public AbstractStorageServer(CvaConfiguration configuration, StorageConfiguration storageConfiguration) {
        logger.info("Loading configuration files");
        this.configuration = configuration;
        this.storageConfiguration = storageConfiguration;
        this.port = configuration.getServer().getRest().getPort();
    }

    private void initDefaultConfigurationFiles() throws IllegalCvaConfigurationException {
        try {
            if (System.getenv("OPENCGA_HOME") != null) {
                initConfigurationFiles(Paths.get(System.getenv("OPENCGA_HOME") + "/conf"));
            } else {
                logger.info("Loading configuration files from inside JAR file");
//                generalConfiguration = GeneralConfiguration.load(GeneralConfiguration.class.getClassLoader().getResourceAsStream("configuration.yml"));
                configuration = CvaConfiguration
                        .load(CvaConfiguration.class.getClassLoader().getResourceAsStream("configuration.yml"), "yaml");
                storageConfiguration = StorageConfiguration
                        .load(StorageConfiguration.class.getClassLoader().getResourceAsStream("storage-configuration.yml"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initConfigurationFiles(Path configDir) throws IllegalCvaConfigurationException {
        try {
            if (configDir != null && Files.exists(configDir) && Files.isDirectory(configDir)) {
                logger.info("Loading configuration files from '{}'", configDir.toString());
//                generalConfiguration = GeneralConfiguration.load(GeneralConfiguration.class.getClassLoader().getResourceAsStream("configuration.yml"));
                configuration = CvaConfiguration
                        .load(new FileInputStream(new File(configDir.toFile().getAbsolutePath() + "/configuration.yml")), "yaml");
                storageConfiguration = StorageConfiguration
                        .load(new FileInputStream(new File(configDir.toFile().getAbsolutePath() + "/storage-configuration.yml")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void start() throws Exception;

    public abstract void stop() throws Exception;

    public abstract void blockUntilShutdown() throws Exception;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StorageServer{");
        sb.append("port=").append(port);
        sb.append(", defaultStorageEngine='").append(defaultStorageEngine).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDefaultStorageEngine() {
        return defaultStorageEngine;
    }

    public void setDefaultStorageEngine(String defaultStorageEngine) {
        this.defaultStorageEngine = defaultStorageEngine;
    }

}
