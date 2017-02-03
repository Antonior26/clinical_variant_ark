package org.gel.cva.storage.mongodb.knownvariant.adaptors;

import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.knownvariant.wrappers.KnownVariantWrapper;
import org.gel.models.cva.avro.AlleleOrigin;
import org.gel.models.cva.avro.CurationClassification;
import org.gel.models.cva.avro.EvidencePathogenicity;
import org.gel.models.cva.avro.SourceType;
import org.gel.models.report.avro.ReportedModeOfInheritance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.commons.datastore.mongodb.MongoDataStore;
import org.opencb.commons.datastore.mongodb.MongoDataStoreManager;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;
import org.opencb.opencga.storage.mongodb.auth.MongoCredentials;

import java.io.InputStream;

import static org.junit.Assert.*;


/**
 * Created by priesgo on 07/01/17.
 */
public class KnownVariantMongoDBAdapterTest {

    private KnownVariantMongoDBAdaptor knownVariantMongoDBAdaptor;
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
        this.knownVariantMongoDBAdaptor = new KnownVariantMongoDBAdaptor(cvaConfiguration);
    }

    @After
    public void tearDown() {
        // Drops the testing database
        String collection = this.cvaConfiguration.getStorageEngines().get(0).getOptions().get("collection.knownvariants");
        db.dropCollection(collection);
    }

    @Test
    public void test1()
            throws VariantAnnotatorException, CvaException {
        // Test when there are differences at the end of the sequence
        Variant variant = new Variant(this.chromosome, this.position, this.reference, this.alternate);
        KnownVariantWrapper knownVariantWrapper = new KnownVariantWrapper("submitter", variant);
        String id = this.knownVariantMongoDBAdaptor.insert(knownVariantWrapper, null);
        assertNotNull(id);
        // Search for the variant just inserted
        KnownVariantWrapper foundKnownVariantWrapper =
                this.knownVariantMongoDBAdaptor.find(this.chromosome, this.position, this.reference, this.alternate);
        assertNotNull(foundKnownVariantWrapper);
        assertEquals(knownVariantWrapper.getVariant().getChromosome(),
                foundKnownVariantWrapper.getVariant().getChromosome());
        assertEquals(knownVariantWrapper.getVariant().getStart(),
                foundKnownVariantWrapper.getVariant().getStart());
        assertEquals(knownVariantWrapper.getVariant().getReference(),
                foundKnownVariantWrapper.getVariant().getReference());
        assertEquals(knownVariantWrapper.getVariant().getAlternate(),
                foundKnownVariantWrapper.getVariant().getAlternate());
        // Search for a variant never inserted
        KnownVariantWrapper notFoundKnownVariantWrapper =
                this.knownVariantMongoDBAdaptor.find("chr1", this.position, this.reference, this.alternate);
        assertNull(notFoundKnownVariantWrapper);
        // Adds a curation
        foundKnownVariantWrapper.addCuration("theCurator", "HPO:0000001", ReportedModeOfInheritance.monoallelic_maternally_imprinted,
                null, CurationClassification.pathogenic_variant, null,
                null, null, null);
        Boolean isUpdateCorrect = this.knownVariantMongoDBAdaptor.update(foundKnownVariantWrapper);
        assertTrue(isUpdateCorrect);
        KnownVariantWrapper updatedKnownVariantWrapper =
                this.knownVariantMongoDBAdaptor.find(this.chromosome, this.position, this.reference, this.alternate);
        assertEquals(1, updatedKnownVariantWrapper.getImpl().getCurations().size());
        assertEquals(CurationClassification.pathogenic_variant,
                updatedKnownVariantWrapper.getImpl().getCurations().get(0).getCuration().getClassification());
        // Adds an additional curation
        updatedKnownVariantWrapper.addCuration("theCurator", "HPO:0000002", ReportedModeOfInheritance.monoallelic_maternally_imprinted,
                null, CurationClassification.benign_variant, null,
                null, null, null);
        isUpdateCorrect = this.knownVariantMongoDBAdaptor.update(updatedKnownVariantWrapper);
        assertTrue(isUpdateCorrect);
        updatedKnownVariantWrapper =
                this.knownVariantMongoDBAdaptor.find(this.chromosome, this.position, this.reference, this.alternate);
        assertEquals(2, updatedKnownVariantWrapper.getImpl().getCurations().size());
        assertEquals(CurationClassification.benign_variant,
                updatedKnownVariantWrapper.getImpl().getCurations().get(1).getCuration().getClassification());
        // Adds an evidence
        assertEquals(0, updatedKnownVariantWrapper.getImpl().getEvidences().size());
        updatedKnownVariantWrapper.addEvidence("theSubmitter", null,
                SourceType.literature_manual_curation, null, null, null,
                AlleleOrigin.germline, null, null, EvidencePathogenicity.moderate,
                null, null, null, null, null,
                null);
        isUpdateCorrect = this.knownVariantMongoDBAdaptor.update(updatedKnownVariantWrapper);
        assertTrue(isUpdateCorrect);
        updatedKnownVariantWrapper =
                this.knownVariantMongoDBAdaptor.find(this.chromosome, this.position, this.reference, this.alternate);
        assertEquals(1, updatedKnownVariantWrapper.getImpl().getEvidences().size());
        // Adds another evidence
        updatedKnownVariantWrapper.addEvidence("theSubmitter", null,
                SourceType.clinical_testing, null, null, null,
                AlleleOrigin.germline, null, null, EvidencePathogenicity.strong,
                null, null, null, null, null,
                null);
        isUpdateCorrect = this.knownVariantMongoDBAdaptor.update(updatedKnownVariantWrapper);
        assertTrue(isUpdateCorrect);
        updatedKnownVariantWrapper =
                this.knownVariantMongoDBAdaptor.find(this.chromosome, this.position, this.reference, this.alternate);
        assertEquals(2, updatedKnownVariantWrapper.getImpl().getEvidences().size());
    }
}
