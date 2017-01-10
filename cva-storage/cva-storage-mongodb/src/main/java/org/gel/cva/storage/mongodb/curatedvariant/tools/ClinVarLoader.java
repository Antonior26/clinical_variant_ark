package org.gel.cva.storage.mongodb.curatedvariant.tools;

import com.mongodb.MongoWriteException;
import org.gel.cva.storage.mongodb.curatedvariant.adaptors.CuratedVariantMongoDBAdaptor;
import org.opencb.biodata.models.variant.*;
import org.opencb.biodata.tools.variant.VariantVcfHtsjdkReader;
import org.opencb.opencga.core.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.mongodb.auth.MongoCredentials;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

/**
 * Created by priesgo on 08/01/17.
 */
public class ClinVarLoader {

    public static String getCurationClassificationFromClinicalsignificance(String clinicalSignificance) {
        String curationClassification = null;
        switch (clinicalSignificance) {
            case "0":
            case "1":
            case "7":
            case "255":
                curationClassification = "VUS";
                break;
            case "2":
            case "3":
                curationClassification = "BENIGN_VARIANT";
                break;
            case "4":
            case "5":
                curationClassification = "DISEASE_CAUSING_VARIANT";
                break;
            case "6":
                curationClassification = "DISEASE_ASSOCIATED_VARIANT";
                break;
        }
        return curationClassification;
    }

    public static Integer getCurationScoreFromRevisionStatus(String clinicalRevisionStatus) {
        Integer curationScore = null;
        switch (clinicalRevisionStatus) {
            case "no_assertion":
                curationScore = 0;
                break;
            case "no_criteria":
                curationScore = 1;
                break;
            case "single":
            case "conf":
                curationScore = 2;
                break;
            case "mult":
                curationScore = 3;
                break;
            case "exp":
                curationScore = 4;
                break;
            case "guideline":
                curationScore = 5;
                break;
        }
        return curationScore;
    }


    public static void main(String [] args) throws FileNotFoundException, IllegalOpenCGACredentialsException, UnknownHostException {

        MongoCredentials mongoCredentials = new MongoCredentials(
                "localhost",
                27017,
                "clinvar",
                "",
                "",
                false
        );

        CuratedVariantMongoDBAdaptor curatedVariantMongoDBAdaptor = new CuratedVariantMongoDBAdaptor(
                mongoCredentials,
                "curated_variants");

        InputStream inputStream = new FileInputStream("/home/priesgo/data/clinvar/clinvar_20170104.vcf");
        VariantSource source = new VariantSource("/home/priesgo/data/clinvar/clinvar_20170104.vcf", "2", "1", "myStudy", VariantStudy.StudyType.FAMILY, VariantSource.Aggregation.NONE);
        VariantVcfHtsjdkReader reader = new VariantVcfHtsjdkReader(inputStream, source);
        reader.open();
        reader.pre();

        List<Variant> read;
        int i = 0;
        Integer duplicatedVariants = 0;
        do {
            read = reader.read();
            for (Variant variant : read) {
                i++;
                System.out.println(" Processing variant = " + variant.getId());
                Map annotations = variant.getStudies().get(0).getFiles().get(0).getAttributes();
                // 0 - Uncertain significance,
                // 1 - not provided,
                // 2 - Benign,
                // 3 - Likely benign,
                // 4 - Likely pathogenic,
                // 5 - Pathogenic,
                // 6 - drug response,
                // 7 - histocompatibility,
                // 255 - other
                String clinicalSignificance = (String) annotations.get("CLNSIG");

                // no_assertion - No assertion provided,
                // no_criteria - No assertion criteria provided,
                // single - Criteria provided single submitter,
                // mult - Criteria provided multiple submitters no conflicts,
                // conf - Criteria provided conflicting interpretations,
                // exp - Reviewed by expert panel,
                // guideline - Practice guideline
                String clinicalRevisionStatus = (String) annotations.get("CLNREVSTAT");

                // Creates a curated variant
                CuratedVariant curatedVariant = new CuratedVariant(variant);
                curatedVariant.setCurationClassification(getCurationClassificationFromClinicalsignificance(clinicalSignificance));
                curatedVariant.setCurationScore(getCurationScoreFromRevisionStatus(clinicalRevisionStatus));
                // Inserts in Mongo
                try {
                    curatedVariantMongoDBAdaptor.insert(curatedVariant, null);
                }
                catch (MongoWriteException e) {
                    duplicatedVariants ++;
                }
            }
        } while (!read.isEmpty());

        System.out.println(" Found duplicated variants = " + duplicatedVariants.toString());
        reader.close();
    }
}
