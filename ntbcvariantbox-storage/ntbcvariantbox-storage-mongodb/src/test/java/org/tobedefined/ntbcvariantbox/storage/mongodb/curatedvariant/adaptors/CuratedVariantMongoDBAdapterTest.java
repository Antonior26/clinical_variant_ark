package org.tobedefined.ntbcvariantbox.storage.mongodb.curatedvariant.adaptors;

import org.junit.Before;
import org.junit.Test;
import org.opencb.biodata.models.variant.*;
import org.opencb.opencga.storage.core.config.DatabaseCredentials;
import org.opencb.opencga.storage.mongodb.auth.MongoCredentials;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;


/**
 * Created by priesgo on 07/01/17.
 */
public class CuratedVariantMongoDBAdapterTest {

    private MongoCredentials mongoCredentials;
    private VariantFactory factory;
    private VariantSource source;
    private CuratedVariantMongoDBAdaptor curatedVariantMongoDBAdaptor;

    @Before
    public void setUp() throws Exception {
        this.mongoCredentials = new MongoCredentials(
                "localhost",
                27017,
                "mydb",
                "",
                "",
                false
        );
        //NOTE: authenticated loin does not work, don't know why...
        this.factory = new VariantVcfFactory();
        this.source = new VariantSource(
                "filename.vcf",
                "fileId",
                "studyId",
                "studyName");
        this.curatedVariantMongoDBAdaptor = new CuratedVariantMongoDBAdaptor(
                this.mongoCredentials,
                "curated_variants");
    }

    @Test
    public void testSimpleInsert() {
        // Test when there are differences at the end of the sequence
        String line = "1\t1000\t.\tTCACCC\tTGACGG\t.\t.\t.";

        List<Variant> result = this.factory.create(source, line);
        result.stream().forEach(variant -> variant.setStudies(Collections.<StudyEntry>emptyList()));

        Variant variant = result.get(0);
        CuratedVariant curatedVariant = new CuratedVariant(variant);
        this.curatedVariantMongoDBAdaptor.insert(curatedVariant, null);
    }

}
