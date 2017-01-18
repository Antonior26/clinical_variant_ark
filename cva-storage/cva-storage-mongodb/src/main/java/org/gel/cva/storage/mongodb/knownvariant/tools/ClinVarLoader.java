package org.gel.cva.storage.mongodb.knownvariant.tools;

import com.mongodb.MongoWriteException;
import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.config.StorageEngineConfiguration;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.models.cva.avro.AlleleOrigin;
import org.gel.models.cva.avro.EvidenceEntry;
import org.gel.models.cva.avro.EvidenceSource;
import org.gel.cva.storage.mongodb.knownvariant.adaptors.KnownVariantMongoDBAdaptor;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.VariantStudy;
import org.gel.cva.storage.core.knownvariant.dto.KnownVariant;
import org.opencb.biodata.tools.variant.VariantVcfHtsjdkReader;
import org.opencb.opencga.core.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.config.DatabaseCredentials;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;
import org.opencb.opencga.storage.mongodb.auth.MongoCredentials;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by priesgo on 08/01/17.
 */
public class ClinVarLoader {

    public static String getCurationClassificationFromClinicalsignificance(String clinicalSignificance) {
        String curationClassification = null;
        // 0 - Uncertain significance,
        // 1 - not provided,
        // 2 - Benign,
        // 3 - Likely benign,
        // 4 - Likely pathogenic,
        // 5 - Pathogenic,
        // 6 - drug response,
        // 7 - histocompatibility,
        // 255 - other
        switch (clinicalSignificance) {
            case "0":
            case "1":
            case "7":
            case "255":
                curationClassification = "VUS";
                break;
            case "2":
            case "3":
                curationClassification = "benign_variant";
                break;
            case "4":
            case "5":
                curationClassification = "disease_causing_variant";
                break;
            case "6":
                curationClassification = "disease_associated_variant";
                break;
        }
        return curationClassification;
    }

    public static Integer getCurationScoreFromRevisionStatus(String clinicalRevisionStatus) {
        Integer curationScore = null;
        // no_assertion - No assertion provided,
        // no_criteria - No assertion criteria provided,
        // single - Criteria provided single submitter,
        // mult - Criteria provided multiple submitters no conflicts,
        // conf - Criteria provided conflicting interpretations,
        // exp - Reviewed by expert panel,
        // guideline - Practice guideline
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

    public static AlleleOrigin getAlleleOriginFromSAO(String sao) {
        AlleleOrigin alleleOrigin = AlleleOrigin.unknown;
        switch (sao) {
            case "0":
                alleleOrigin = AlleleOrigin.unknown;
            case "1":
                alleleOrigin = AlleleOrigin.germline;
            case "2":
                alleleOrigin = AlleleOrigin.somatic;
            case "3":
                alleleOrigin = AlleleOrigin.both;
        }
        return alleleOrigin;
    }


    public static void main(String [] args) throws FileNotFoundException,
            IllegalOpenCGACredentialsException,
            UnknownHostException,
            VariantAnnotatorException,
            IOException,
            IllegalCvaConfigurationException
    {

        // Creates db adaptor
        KnownVariantMongoDBAdaptor knownVariantMongoDBAdaptor = KnownVariantMongoDBAdaptor.getInstance();

        // Reads ClinVar input VCF
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
                String clinicalSignificance = (String) annotations.get("CLNSIG");
                String clinicalRevisionStatus = (String) annotations.get("CLNREVSTAT");
                String sao = (String) annotations.get("SAO");
                String dbsnpId = (String) annotations.get("RS");
                // Creates a ClinVar adhoc evidence
                EvidenceEntry evidenceEntry = new EvidenceEntry();
                evidenceEntry.setSource(EvidenceSource.database);
                evidenceEntry.setAlleleOrigin(getAlleleOriginFromSAO(sao));
                evidenceEntry.setDatabaseName("ClinVar");
                evidenceEntry.setVersion("20170104");
                evidenceEntry.setSubmitter("ClinVar loader");
                String clinVarUrl = String.format(
                        "https://www.ncbi.nlm.nih.gov/clinvar?term=%1$s[External allele ID]", dbsnpId);
                evidenceEntry.setUrl(clinVarUrl);
                List<EvidenceEntry> evidences = new ArrayList<EvidenceEntry>();
                evidences.add(evidenceEntry);
                // Creates a curated variant
                KnownVariant knownVariant = new KnownVariant(
                        variant,
                        getCurationClassificationFromClinicalsignificance(clinicalSignificance),
                        getCurationScoreFromRevisionStatus(clinicalRevisionStatus),
                        null,
                        evidences,
                        null
                        );
                // Inserts in Mongo
                try {
                    knownVariantMongoDBAdaptor.insert(knownVariant, null);
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
