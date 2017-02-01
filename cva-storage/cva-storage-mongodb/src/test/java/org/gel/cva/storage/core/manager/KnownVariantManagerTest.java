package org.gel.cva.storage.core.manager;

import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;
import org.opencb.opencga.core.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;
import org.opencb.opencga.storage.mongodb.auth.MongoCredentials;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertNotNull;

/**
 * Created by priesgo on 31/01/17.
 */
public class KnownVariantManagerTest {

    KnownVariantManager knownVariantManager;
    CvaConfiguration cvaConfiguration;
    MongoDataStore db;
    String submitter = "theSubmitter";
    String chromosome = "chr19";
    String chromosomeNormalized = "19";  // OpenCB normalizes chromosome identifiers
    Integer position = 44908684;
    String reference = "T";
    String alternate = "C";

    @Before
    public void setUp() throws IllegalCvaConfigurationException, IllegalOpenCGACredentialsException,
            ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            InvocationTargetException, CvaException{

        //NOTE: authenticated loin does not work, don't know why...
        // Loads the testing configuration
        InputStream configStream = KnownVariantManagerTest.class.getResourceAsStream(
                "/config/cva.test.yml");
        this.cvaConfiguration = CvaConfiguration.load(configStream, "yaml");
        // Initilize mongo client
        MongoCredentials credentials = this.cvaConfiguration.getMongoCredentials();
        MongoDataStoreManager mongoManager = new MongoDataStoreManager(credentials.getDataStoreServerAddresses());
        this.db = mongoManager.get(credentials.getMongoDbName(), credentials.getMongoDBConfiguration());
        // drops the testing collection before starting tests
        String collection = this.cvaConfiguration.getStorageEngines().get(0).getOptions().get("collection.knownvariants");
        db.dropCollection(collection);
        // Intialize manager to test
        this.knownVariantManager = new KnownVariantManager(CvaConfiguration.getInstance());
    }

    @After
    public void tearDown() {
        // Drops the testing database
        String collection = this.cvaConfiguration.getStorageEngines().get(0).getOptions().get("collection.knownvariants");
        db.dropCollection(collection);
    }

    @Test
    public void test1() throws VariantAnnotatorException, CvaException{

        String variantId = this.knownVariantManager.createKnownVariant(
                submitter,
                chromosome,
                position,
                reference,
                alternate
        );
        assertNotNull(variantId);
    }
}
