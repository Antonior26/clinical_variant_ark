package org.gel.cva.storage.mongodb.knownvariant.adaptors;

import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.knownvariant.wrappers.KnownVariantWrapper;
import org.junit.Before;
import org.junit.Test;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.VariantFactory;
import org.opencb.biodata.models.variant.VariantVcfFactory;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.StudyEntry;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;
import org.opencb.opencga.storage.mongodb.auth.MongoCredentials;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


/**
 * Created by priesgo on 07/01/17.
 */
public class KnownVariantMongoDBAdapterTest {

    private MongoCredentials mongoCredentials;
    private VariantFactory factory;
    private VariantSource source;
    private KnownVariantMongoDBAdaptor curatedVariantMongoDBAdaptor;

    @Before
    public void setUp() throws Exception {
        //NOTE: authenticated loin does not work, don't know why...
        this.factory = new VariantVcfFactory();
        this.source = new VariantSource(
                "filename.vcf",
                "fileId",
                "studyId",
                "studyName");
        CvaConfiguration cvaConfiguration = CvaConfiguration.getInstance();
        this.curatedVariantMongoDBAdaptor = new KnownVariantMongoDBAdaptor(cvaConfiguration);
    }

    @Test
    public void testSimpleInsert()
            throws VariantAnnotatorException, IOException, IllegalCvaConfigurationException {
        // Test when there are differences at the end of the sequence
        String line = "1\t1000\t.\tTCACCC\tTGACGG\t.\t.\t.";

        List<Variant> result = this.factory.create(source, line);
        result.stream().forEach(variant -> variant.setStudies(Collections.<StudyEntry>emptyList()));

        Variant variant = result.get(0);
        KnownVariantWrapper knownVariantWrapper = new KnownVariantWrapper("submitter", variant);
        this.curatedVariantMongoDBAdaptor.insert(knownVariantWrapper, null);
    }

}
