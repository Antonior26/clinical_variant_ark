package org.gel.cva.storage.core.managers;

import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.exceptions.IllegalCvaCredentialsException;
import org.junit.After;
import org.junit.Before;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;
import org.opencb.opencga.storage.mongodb.auth.MongoCredentials;

import java.io.InputStream;

/**
 * Created by priesgo on 03/02/17.
 */
public class GenericManagerTest<T extends CvaManager> {

    protected T manager;
    protected CvaConfiguration cvaConfiguration;
    protected MongoDataStore db;

    @Before
    public void setUp() throws IllegalCvaConfigurationException, IllegalCvaCredentialsException {

        //NOTE: authenticated loin does not work, don't know why...
        // Loads the testing configuration
        InputStream configStream = KnownVariantManagerTest.class.getResourceAsStream(
                "/config/cva.test.yml");
        this.cvaConfiguration = CvaConfiguration.load(configStream, "yaml");
        // Initilize mongo client
        MongoCredentials credentials = this.cvaConfiguration.getMongoCredentials();
        MongoDataStoreManager mongoManager = new MongoDataStoreManager(credentials.getDataStoreServerAddresses());
        this.db = mongoManager.get(credentials.getMongoDbName(), credentials.getMongoDBConfiguration());
    }

    @After
    public void tearDown() {
        // Drops the testing database
        db.getDb().drop();
    }

    protected void dropCollection(String collection) {
        // Drops the testing database
        db.dropCollection(collection);
    }
}
