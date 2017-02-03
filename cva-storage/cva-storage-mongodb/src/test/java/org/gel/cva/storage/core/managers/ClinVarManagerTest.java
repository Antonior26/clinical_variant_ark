package org.gel.cva.storage.core.managers;

import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.ClinVarManagerException;
import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.exceptions.IllegalCvaCredentialsException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * Created by priesgo on 03/02/17.
 */
public class ClinVarManagerTest extends GenericManagerTest<ClinVarManager> {

    String collection;
    KnownVariantManager knownVariantManager;
    VcfManager vcfManager;

    @Before
    public void setUp() throws CvaException {
        // Intialize managers to test
        super.setUp();
        this.dropCollection(collection);
        this.manager = new ClinVarManager(CvaConfiguration.getInstance());
        this.knownVariantManager = new KnownVariantManager(CvaConfiguration.getInstance());
        this.vcfManager = new VcfManager(CvaConfiguration.getInstance());
        this.collection = this.cvaConfiguration.getStorageEngines().get(0).getOptions().get("collection.knownvariants");
    }

    @After
    public void tearDown() {
        // Drops the testing database
        //this.dropCollection(collection);
        //super.tearDown();
    }

    @Test
    public void test1() throws VariantAnnotatorException, CvaException {
        this.vcfManager.loadVcf(
                new File(ClinVarManagerTest.class.getResource("/vcfs/clinvar_20170130.only83variants.vcf").getFile()));
        this.manager.addEvidencesFromLatest();
        assertEquals(new Long(83), this.knownVariantManager.count());
    }
}
