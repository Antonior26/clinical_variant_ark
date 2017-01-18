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

package org.gel.cva.storage.core.config;

import org.opencb.commons.datastore.core.ObjectMap;
import org.opencb.opencga.storage.core.config.DatabaseCredentials;
import org.opencb.opencga.storage.core.config.StorageEtlConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by imedina on 01/05/15.
 */
public class StorageEngineConfiguration {

    private String id;
    private ObjectMap options;
    private DatabaseCredentials database;

    public StorageEngineConfiguration() {

    }

    public StorageEngineConfiguration(String id, DatabaseCredentials database, ObjectMap options) {
        this.id = id;
        this.database = database;
        this.options = options;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StorageEngineConfiguration{");
        sb.append("id='").append(id).append('\'');
        sb.append(", options=").append(options);
        sb.append(", database=").append(database);
        sb.append('}');
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ObjectMap getOptions() {
        return options;
    }

    public void setOptions(ObjectMap options) {
        this.options = options;
    }

    public DatabaseCredentials getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseCredentials database) {
        this.database = database;
    }

    public List<String> getHosts() {
        List<String> hosts = new LinkedList<String>();
        for (String host: this.getDatabase().getHosts()) {
            hosts.add(host.split(":")[0]);
        }
        return hosts;
    }
}
