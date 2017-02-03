package org.gel.cva.storage.core.managers;

import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.exceptions.IllegalCvaCredentialsException;
import org.gel.cva.storage.core.knownvariant.wrappers.KnownVariantWrapper;
import org.gel.models.cva.avro.*;
import org.gel.models.report.avro.ReportedModeOfInheritance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by priesgo on 31/01/17.
 */
public class KnownVariantManagerTest extends GenericManagerTest<KnownVariantManager> {

    String collection;
    String submitter = "theSubmitter";
    String chromosome = "chr19";
    String chromosomeNormalized = "19";  // OpenCB normalizes chromosome identifiers
    Integer position = 44908684;
    String reference = "T";
    String alternate = "C";
    String curator = "theCurator";
    String phenotype1 = "HPO:000001";
    String phenotype2 = "HPO:000002";
    ReportedModeOfInheritance modeOfInheritance1 = ReportedModeOfInheritance.mitochondrial;
    HeritablePhenotype heritablePhenotype = new HeritablePhenotype(phenotype1, modeOfInheritance1);

    @Before
    public void setUp() throws IllegalCvaConfigurationException, IllegalCvaCredentialsException {
        // Intialize managers to test
        super.setUp();
        this.dropCollection(collection);
        this.manager = new KnownVariantManager(CvaConfiguration.getInstance());
        this.collection = this.cvaConfiguration.getStorageEngines().get(0).getOptions().get("collection.knownvariants");
    }

    @After
    public void tearDown() {
        // Drops the testing database
        this.dropCollection(collection);
        super.tearDown();
    }

    @Test
    public void test1() throws VariantAnnotatorException, CvaException{

        // Registers a new known variant
        KnownVariantWrapper knownVariantWrapper = this.manager.createKnownVariant(
                submitter,
                chromosome,
                position,
                reference,
                alternate
        );
        assertNotNull(knownVariantWrapper);
        assertEquals(this.chromosomeNormalized, knownVariantWrapper.getVariant().getChromosome());
        assertEquals(this.position, knownVariantWrapper.getVariant().getStart());
        assertEquals(this.reference, knownVariantWrapper.getVariant().getReference());
        assertEquals(this.alternate, knownVariantWrapper.getVariant().getAlternate());
        assertEquals(this.submitter, knownVariantWrapper.getImpl().getSubmitter());
        assertNotNull(knownVariantWrapper.getVariant().getAnnotation());
        // Retrieves the variant from the database again
        knownVariantWrapper = this.manager.findKnownVariant(
                chromosome,
                position,
                reference,
                alternate);
        assertNotNull(knownVariantWrapper);
        assertEquals(this.chromosomeNormalized, knownVariantWrapper.getVariant().getChromosome());
        assertEquals(this.position, knownVariantWrapper.getVariant().getStart());
        assertEquals(this.reference, knownVariantWrapper.getVariant().getReference());
        assertEquals(this.alternate, knownVariantWrapper.getVariant().getAlternate());
        assertEquals(this.submitter, knownVariantWrapper.getImpl().getSubmitter());
        assertNotNull(knownVariantWrapper.getVariant().getAnnotation());
        // Adds a curation
        knownVariantWrapper = this.manager.addCuration(
                chromosome,
                position,
                reference,
                alternate,
                curator,
                phenotype1,
                modeOfInheritance1,
                null,
                CurationClassification.pathogenic_variant,
                null,
                null,
                null,
                null);
        assertNotNull(knownVariantWrapper);
        assertEquals(1, knownVariantWrapper.getImpl().getCurations().size());
        assertEquals(CurationClassification.pathogenic_variant,
                knownVariantWrapper.getImpl().getCurations().get(0).getCuration().getClassification());
        assertEquals(phenotype1,
                knownVariantWrapper.getImpl().getCurations().get(0).getCuration().getHeritablePhenotype().getPhenotype());
        assertEquals(1, knownVariantWrapper.getImpl().getCurations().get(0).getHistory().size());
        // Retrieves the variant from the database again
        knownVariantWrapper = this.manager.findKnownVariant(
                chromosome,
                position,
                reference,
                alternate);
        assertNotNull(knownVariantWrapper);
        assertEquals(1, knownVariantWrapper.getImpl().getCurations().size());
        assertEquals(CurationClassification.pathogenic_variant,
                knownVariantWrapper.getImpl().getCurations().get(0).getCuration().getClassification());
        assertEquals(phenotype1,
                knownVariantWrapper.getImpl().getCurations().get(0).getCuration().getHeritablePhenotype().getPhenotype());
        assertEquals(1, knownVariantWrapper.getImpl().getCurations().get(0).getHistory().size());
        // Adds an additional curation
        knownVariantWrapper = this.manager.addCuration(
                chromosome,
                position,
                reference,
                alternate,
                curator,
                phenotype1,
                modeOfInheritance1,
                null,
                CurationClassification.benign_variant,
                null,
                null,
                null,
                null);
        assertNotNull(knownVariantWrapper);
        assertEquals(1, knownVariantWrapper.getImpl().getCurations().size());
        assertEquals(CurationClassification.benign_variant,
                knownVariantWrapper.getImpl().getCurations().get(0).getCuration().getClassification());
        assertEquals(phenotype1,
                knownVariantWrapper.getImpl().getCurations().get(0).getCuration().getHeritablePhenotype().getPhenotype());
        assertEquals(2, knownVariantWrapper.getImpl().getCurations().get(0).getHistory().size());
        // Retrieves the variant from the database again
        knownVariantWrapper = this.manager.findKnownVariant(
                chromosome,
                position,
                reference,
                alternate);
        assertNotNull(knownVariantWrapper);
        assertEquals(1, knownVariantWrapper.getImpl().getCurations().size());
        assertEquals(CurationClassification.benign_variant,
                knownVariantWrapper.getImpl().getCurations().get(0).getCuration().getClassification());
        assertEquals(phenotype1,
                knownVariantWrapper.getImpl().getCurations().get(0).getCuration().getHeritablePhenotype().getPhenotype());
        assertEquals(2, knownVariantWrapper.getImpl().getCurations().get(0).getHistory().size());
        // Adds an additional curation associated to other phenotype
        knownVariantWrapper = this.manager.addCuration(
                chromosome,
                position,
                reference,
                alternate,
                curator,
                phenotype2,
                null,
                null,
                CurationClassification.established_risk_allele,
                null,
                null,
                null,
                null);
        assertNotNull(knownVariantWrapper);
        assertEquals(2, knownVariantWrapper.getImpl().getCurations().size());
        assertEquals(CurationClassification.established_risk_allele,
                knownVariantWrapper.getImpl().getCurations().get(1).getCuration().getClassification());
        assertEquals(phenotype2,
                knownVariantWrapper.getImpl().getCurations().get(1).getCuration().getHeritablePhenotype().getPhenotype());
        assertEquals(1, knownVariantWrapper.getImpl().getCurations().get(1).getHistory().size());
        // Retrieves the variant from the database again
        knownVariantWrapper = this.manager.findKnownVariant(
                chromosome,
                position,
                reference,
                alternate);
        assertNotNull(knownVariantWrapper);
        assertEquals(2, knownVariantWrapper.getImpl().getCurations().size());
        assertEquals(CurationClassification.established_risk_allele,
                knownVariantWrapper.getImpl().getCurations().get(1).getCuration().getClassification());
        assertEquals(phenotype2,
                knownVariantWrapper.getImpl().getCurations().get(1).getCuration().getHeritablePhenotype().getPhenotype());
        assertEquals(1, knownVariantWrapper.getImpl().getCurations().get(1).getHistory().size());
        // Adds an evidence to the variant
        List<HeritablePhenotype> heritablePhenotypeList = new LinkedList<>();
        heritablePhenotypeList.add(heritablePhenotype);
        knownVariantWrapper = this.manager.addEvidence(
                chromosome,
                position,
                reference,
                alternate,
                submitter,
                null,
                SourceType.clinical_testing,
                null,
                null,
                null,
                AlleleOrigin.germline,
                heritablePhenotypeList,
                null,
                EvidencePathogenicity.strong,
                null,
                null,
                null,
                null,
                null,
                null
                );
        assertNotNull(knownVariantWrapper);
        assertEquals(1, knownVariantWrapper.getImpl().getEvidences().size());
        assertEquals(submitter, knownVariantWrapper.getImpl().getEvidences().get(0).getSubmitter());
        assertEquals(SourceType.clinical_testing, knownVariantWrapper.getImpl().getEvidences().get(0).getSource().getType());
        assertEquals(AlleleOrigin.germline, knownVariantWrapper.getImpl().getEvidences().get(0).getAlleleOrigin());
        assertEquals(ConsistencyStatus.consensus,
                knownVariantWrapper.getImpl().getCurations().get(0).getCuration().getConsistencyStatus());
        // Retrieves the variant from the database again
        knownVariantWrapper = this.manager.findKnownVariant(
                chromosome,
                position,
                reference,
                alternate);
        assertNotNull(knownVariantWrapper);
        assertEquals(1, knownVariantWrapper.getImpl().getEvidences().size());
        assertEquals(submitter, knownVariantWrapper.getImpl().getEvidences().get(0).getSubmitter());
        assertEquals(SourceType.clinical_testing, knownVariantWrapper.getImpl().getEvidences().get(0).getSource().getType());
        assertEquals(AlleleOrigin.germline, knownVariantWrapper.getImpl().getEvidences().get(0).getAlleleOrigin());
        assertEquals(ConsistencyStatus.consensus,
                knownVariantWrapper.getImpl().getCurations().get(0).getCuration().getConsistencyStatus());
        // Adds a second evidence to the variant being in conflict with the first
        knownVariantWrapper = this.manager.addEvidence(
                chromosome,
                position,
                reference,
                alternate,
                submitter,
                null,
                SourceType.literature_manual_curation,
                null,
                null,
                null,
                AlleleOrigin.germline,
                heritablePhenotypeList,
                null,
                null,
                EvidenceBenignity.strong,
                null,
                null,
                null,
                null,
                null
        );
        assertNotNull(knownVariantWrapper);
        assertEquals(2, knownVariantWrapper.getImpl().getEvidences().size());
        assertEquals(submitter, knownVariantWrapper.getImpl().getEvidences().get(1).getSubmitter());
        assertEquals(SourceType.literature_manual_curation, knownVariantWrapper.getImpl().getEvidences().get(1).getSource().getType());
        assertEquals(AlleleOrigin.germline, knownVariantWrapper.getImpl().getEvidences().get(1).getAlleleOrigin());
        assertEquals(ConsistencyStatus.conflict,
                knownVariantWrapper.getImpl().getCurations().get(0).getCuration().getConsistencyStatus());
        // Retrieves the variant from the database again
        knownVariantWrapper = this.manager.findKnownVariant(
                chromosome,
                position,
                reference,
                alternate);
        assertNotNull(knownVariantWrapper);
        assertEquals(2, knownVariantWrapper.getImpl().getEvidences().size());
        assertEquals(submitter, knownVariantWrapper.getImpl().getEvidences().get(1).getSubmitter());
        assertEquals(SourceType.literature_manual_curation, knownVariantWrapper.getImpl().getEvidences().get(1).getSource().getType());
        assertEquals(AlleleOrigin.germline, knownVariantWrapper.getImpl().getEvidences().get(1).getAlleleOrigin());
        assertEquals(ConsistencyStatus.conflict,
                knownVariantWrapper.getImpl().getCurations().get(0).getCuration().getConsistencyStatus());
    }
}
