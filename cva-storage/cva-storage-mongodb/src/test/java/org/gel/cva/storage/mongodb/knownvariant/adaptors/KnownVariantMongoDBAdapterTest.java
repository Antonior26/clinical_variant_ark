package org.gel.cva.storage.mongodb.knownvariant.adaptors;

import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.knownvariant.wrappers.KnownVariantWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.VariantFactory;
import org.opencb.biodata.models.variant.VariantVcfFactory;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.StudyEntry;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;
import org.opencb.opencga.storage.mongodb.auth.MongoCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;


/**
 * Created by priesgo on 07/01/17.
 */
public class KnownVariantMongoDBAdapterTest {

    private KnownVariantMongoDBAdaptor curatedVariantMongoDBAdaptor;
    private CvaConfiguration cvaConfiguration;
    private MongoDataStore db;
    String chromosome = "chr19";
    String chromosomeNormalized = "19";  // OpenCB normalizes chromosome identifiers
    Integer position = 44908684;
    String reference = "T";
    String alternate = "C";

    @Before
    public void setUp() throws Exception {
        //NOTE: authenticated loin does not work, don't know why...
        // Loads the testing configuration
        InputStream configStream = KnownVariantMongoDBAdapterTest.class.getResourceAsStream(
                "/config/cva.test.yml");
        this.cvaConfiguration = CvaConfiguration.load(configStream, "yaml");
        // Initilize mongo client
        MongoCredentials credentials = this.cvaConfiguration.getMongoCredentials();
        MongoDataStoreManager mongoManager = new MongoDataStoreManager(credentials.getDataStoreServerAddresses());
        this.db = mongoManager.get(credentials.getMongoDbName(), credentials.getMongoDBConfiguration());
        // drops the testing collection before starting tests
        String collection = this.cvaConfiguration.getStorageEngines().get(0).getOptions().get("collection.knownvariants");
        db.dropCollection(collection);
        // initialize the adaptor to be tested
        this.curatedVariantMongoDBAdaptor = new KnownVariantMongoDBAdaptor(cvaConfiguration);
    }

    @After
    public void tearDown() {
        // Drops the testing database
        String collection = this.cvaConfiguration.getStorageEngines().get(0).getOptions().get("collection.knownvariants");
        db.dropCollection(collection);
    }

    @Test
    public void testSimpleInsert()
            throws VariantAnnotatorException, CvaException {
        // Test when there are differences at the end of the sequence
        Variant variant = new Variant(this.chromosome, this.position, this.reference, this.alternate);
        KnownVariantWrapper knownVariantWrapper = new KnownVariantWrapper("submitter", variant);
        this.curatedVariantMongoDBAdaptor.insert(knownVariantWrapper, null);
    }
}
