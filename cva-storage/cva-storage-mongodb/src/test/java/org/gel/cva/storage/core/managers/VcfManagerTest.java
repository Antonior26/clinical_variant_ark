package org.gel.cva.storage.core.managers;

import org.gel.cva.storage.core.config.CvaConfiguration;
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
import static org.junit.Assert.assertTrue;

/**
 * Created by priesgo on 03/02/17.
 */
public class VcfManagerTest extends GenericManagerTest<VcfManager> {

    String collection;
    KnownVariantManager knownVariantManager;

    @Before
    public void setUp() throws IllegalCvaConfigurationException, IllegalCvaCredentialsException {
        // Intialize managers to test
        super.setUp();
        this.dropCollection(collection);
        this.manager = new VcfManager(CvaConfiguration.getInstance());
        this.knownVariantManager = new KnownVariantManager(CvaConfiguration.getInstance());
        this.collection = this.cvaConfiguration.getStorageEngines().get(0).getOptions().get("collection.knownvariants");
    }

    @After
    public void tearDown() {
        // Drops the testing database
        this.dropCollection(collection);
        super.tearDown();
    }

    @Test
    public void test1() throws VariantAnnotatorException, CvaException {
        this.manager.loadVcf(
                new File(VcfManagerTest.class.getResource("/vcfs/clinvar_20170130.only83variants.vcf").getFile()));
        assertEquals(new Long(83), this.knownVariantManager.count());
    }

    @Test
    public void test2() throws VariantAnnotatorException, CvaException, MalformedURLException {
        this.manager.loadVcf(
                new URL("ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/vcf_GRCh38/common_and_clinical_20170130.vcf.gz"));
        assertEquals(new Long(4851), this.knownVariantManager.count());
    }
}
