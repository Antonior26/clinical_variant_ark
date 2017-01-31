/*
 * Copyright 2017 Genomics England Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gel.cva.storage.core.knownvariant.wrappers;

import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaArgumentException;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.models.cva.avro.*;
import org.gel.models.report.avro.ReportedModeOfInheritance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Pablo Riesgo Ferreiro &lt;pablo.ferreiro@genomicsengland.co.uk&gt;
 */
public class KnownVariantWrapperTest {

    public KnownVariantWrapperTest(){}

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {

    }

    private KnownVariantWrapper createKnownVariant(
            String submitter,
            String chromosome,
            Integer position,
            String reference,
            String alternate) {

        KnownVariantWrapper knownVariantWrapper = null;
        try {
            knownVariantWrapper = new KnownVariantWrapper(
                    submitter,
                    chromosome,
                    position,
                    reference,
                    alternate);
        }
        catch (CvaException ex) {
            assertTrue(false);  // this should never raise
        }
        catch (VariantAnnotatorException ex) {
            assertTrue(false);  // this should never raise
        }
        return knownVariantWrapper;
    }

    private void createCuration(
            KnownVariantWrapper knownVariantWrapper,
            String curator,
            String phenotype,
            ReportedModeOfInheritance inheritance,
            String transcript,
            CurationClassification curationClassification,
            ManualCurationConfidence manualCurationConfidence,
            ConsistencyStatus consistencyStatus,
            Float penetrance,
            Boolean variableExpressivity) {

        try {
            knownVariantWrapper.addCuration(
                    curator,
                    phenotype,
                    inheritance,
                    transcript,
                    curationClassification,
                    manualCurationConfidence,
                    consistencyStatus,
                    penetrance,
                    variableExpressivity
            );
        }
        catch (IllegalCvaArgumentException ex) {
            assertTrue(false);  // this should never raise
        }
    }

    @Test
    public void testKnownVariantWrapper1() {

        /*
        Creates a KnownVariantWrapper
         */
        String submitter = "theSubmitter";
        String chromosome = "chr19";
        String chromosomeNormalized = "19";  // OpenCB normalizes chromosome identifiers
        Integer position = 44908684;
        String reference = "T";
        String alternate = "C";
        KnownVariantWrapper knownVariantWrapper = this.createKnownVariant(
                submitter,
                chromosome,
                position,
                reference,
                alternate
        );
        assertNotEquals(chromosome, knownVariantWrapper.getImpl().getVariant().getChromosome());
        assertEquals(chromosomeNormalized, knownVariantWrapper.getImpl().getVariant().getChromosome());
        assertEquals(position, knownVariantWrapper.getImpl().getVariant().getStart());
        assertEquals(reference, knownVariantWrapper.getImpl().getVariant().getReference());
        assertEquals(alternate, knownVariantWrapper.getImpl().getVariant().getAlternate());
        assertNotNull(knownVariantWrapper.getImpl().getVariant().getAnnotation());
        assertEquals(0, knownVariantWrapper.getImpl().getCurations().size());
        assertEquals(0, knownVariantWrapper.getImpl().getEvidences().size());

        /*
        Adds a curation to the variant
         */
        String curator = "theCurator";
        String phenotype = "HPO:000001";
        ReportedModeOfInheritance inheritance = ReportedModeOfInheritance.monoallelic_maternally_imprinted;
        String transcript = null;
        ManualCurationConfidence manualCurationConfidence = ManualCurationConfidence.high_confidence;
        ConsistencyStatus consistencyStatus = ConsistencyStatus.consensus;
        Float penetrance = 0.99f;
        Boolean variableExpressivity = true;
        this.createCuration(
                knownVariantWrapper,
                curator,
                phenotype,
                inheritance,
                transcript,
                CurationClassification.benign_variant,
                manualCurationConfidence,
                consistencyStatus,
                penetrance,
                variableExpressivity
        );
        assertEquals(1, knownVariantWrapper.getImpl().getCurations().size());
        List<CurationEntry> curationEntriesEmpty = null;
        try {
            curationEntriesEmpty = knownVariantWrapper.getCurationEntryByHeritablePhenotype(phenotype, ReportedModeOfInheritance.biallelic);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(0, curationEntriesEmpty.size());
        List<CurationEntry> curationEntries = null;
        try {
            curationEntries = knownVariantWrapper.getCurationEntryByHeritablePhenotype(phenotype, inheritance);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(1, curationEntries.size());
        try {
            curationEntries = knownVariantWrapper.getCurationEntryByHeritablePhenotype(phenotype, null);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(1, curationEntries.size());
        CurationEntry curationEntry = curationEntries.get(0);
        assertEquals(1, curationEntry.getHistory().size());
        assertEquals(curator, curationEntry.getHistory().get(0).getCurator());
        assertEquals(phenotype,
                curationEntry.getCuration().getHeritablePhenotype().getPhenotype());
        assertEquals(inheritance,
                curationEntry.getCuration().getHeritablePhenotype()
                        .getInheritanceMode());
        assertEquals(CurationClassification.benign_variant,
                curationEntry.getCuration().getClassification());
        assertEquals(CurationSOClassification.benign_variant,
                curationEntry.getCuration().getSoClassification());
        assertEquals(manualCurationConfidence,
                curationEntry.getCuration().getManualCurationConfidence());
        assertEquals(consistencyStatus,
                curationEntry.getCuration().getConsistencyStatus());
        /*
        Adds a second curation to the same phenotype changing from benign to pathogenic
         */
        this.createCuration(
                knownVariantWrapper,
                curator,
                phenotype,
                inheritance,
                transcript,
                CurationClassification.pathogenic_variant,
                manualCurationConfidence,
                null,
                penetrance,
                variableExpressivity
        );
        assertEquals(1, knownVariantWrapper.getImpl().getCurations().size());
        try {
            curationEntries = knownVariantWrapper.getCurationEntryByHeritablePhenotype(phenotype, null);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(1, curationEntries.size());
        curationEntry = curationEntries.get(0);
        assertEquals(2, curationEntry.getHistory().size());
        assertEquals(curator, curationEntry.getHistory().get(0).getCurator());
        assertEquals(phenotype,
                curationEntry.getCuration().getHeritablePhenotype().getPhenotype());
        assertEquals(inheritance,
                curationEntry.getCuration().getHeritablePhenotype()
                        .getInheritanceMode());
        assertEquals(CurationClassification.pathogenic_variant,
                curationEntry.getCuration().getClassification());
        assertEquals(CurationClassification.benign_variant,
                curationEntry.getHistory().get(1)
                        .getPreviousCuration().getClassification());
        assertEquals(CurationClassification.pathogenic_variant,
                curationEntry.getHistory().get(1)
                        .getNewCuration().getClassification());
        assertEquals(CurationSOClassification.disease_causing_variant,
                curationEntry.getCuration().getSoClassification());
        assertEquals(manualCurationConfidence,
                curationEntry.getCuration().getManualCurationConfidence());
        assertEquals(ConsistencyStatus.consensus,
                curationEntry.getCuration().getConsistencyStatus());
        /*
        Adds a third curation to another phenotype
         */
        String phenotype2 = "HPO:000002";
        this.createCuration(
                knownVariantWrapper,
                curator,
                phenotype2,
                inheritance,
                transcript,
                CurationClassification.established_risk_allele,
                manualCurationConfidence,
                null,
                penetrance,
                variableExpressivity
        );
        assertEquals(2, knownVariantWrapper.getImpl().getCurations().size());
        try {
            curationEntries = knownVariantWrapper.getCurationEntryByHeritablePhenotype(phenotype2, null);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(1, curationEntries.size());
        curationEntry = curationEntries.get(0);
        assertEquals(1, curationEntry.getHistory().size());
        assertEquals(curator, curationEntry.getHistory().get(0).getCurator());
        assertEquals(phenotype2,
                curationEntry.getCuration().getHeritablePhenotype().getPhenotype());
        assertEquals(inheritance,
                curationEntry.getCuration().getHeritablePhenotype()
                        .getInheritanceMode());
        assertEquals(CurationClassification.established_risk_allele,
                curationEntry.getCuration().getClassification());
        assertEquals(null,
                curationEntry.getHistory().get(0)
                        .getPreviousCuration());
        assertEquals(CurationClassification.established_risk_allele,
                curationEntry.getHistory().get(0)
                        .getNewCuration().getClassification());
        assertEquals(CurationSOClassification.disease_associated_variant,
                curationEntry.getCuration().getSoClassification());
        assertEquals(manualCurationConfidence,
                curationEntry.getCuration().getManualCurationConfidence());
        assertEquals(ConsistencyStatus.consensus,
                curationEntry.getCuration().getConsistencyStatus());
        /*
        Adds a first evidence to the first phenotype indication of pathogenicity
         */
        List<HeritablePhenotype> heritablePhenotypes = new LinkedList<>();
        HeritablePhenotype heritablePhenotype = new HeritablePhenotype();
        heritablePhenotype.setPhenotype(phenotype);
        heritablePhenotype.setInheritanceMode(ReportedModeOfInheritance.monoallelic_maternally_imprinted);
        heritablePhenotypes.add(heritablePhenotype);
        try {
            knownVariantWrapper.addEvidence(
                    submitter,
                    null,
                    SourceType.literature_manual_curation,
                    null,
                    null,
                    null,
                    AlleleOrigin.germline,
                    heritablePhenotypes,
                    transcript,
                    EvidencePathogenicity.strong,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "A very bad evidence"
            );
        }
        catch (IllegalCvaArgumentException ex) {
            assertTrue(false);
        }
        assertEquals(1, knownVariantWrapper.getImpl().getEvidences().size());
        List<EvidenceEntry> evidenceEntriesEmpty =
                null;
        try {
            evidenceEntriesEmpty = knownVariantWrapper.getEvidenceEntryByHeritablePhenotype(
                    phenotype, ReportedModeOfInheritance.biallelic);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(0, evidenceEntriesEmpty.size());
        List<EvidenceEntry> evidenceEntries = null;
        try {
            evidenceEntries = knownVariantWrapper.getEvidenceEntryByHeritablePhenotype(
                    phenotype, ReportedModeOfInheritance.monoallelic_maternally_imprinted);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(1, evidenceEntries.size());
        try {
            evidenceEntries = knownVariantWrapper.getEvidenceEntryByHeritablePhenotype(phenotype, null);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(1, evidenceEntries.size());
        EvidenceEntry evidenceEntry = evidenceEntries.get(0);
        assertEquals(submitter, evidenceEntry.getSubmitter());
        assertEquals(SourceType.literature_manual_curation,
                evidenceEntry.getSource().getType());
        assertEquals(AlleleOrigin.germline,
                evidenceEntry.getAlleleOrigin());
        assertEquals(1,
                evidenceEntry.getHeritablePhenotypes().size());
        assertEquals(phenotype,
                evidenceEntry.getHeritablePhenotypes().get(0).getPhenotype());
        assertEquals(ReportedModeOfInheritance.monoallelic_maternally_imprinted,
                evidenceEntry.getHeritablePhenotypes().get(0).getInheritanceMode());
        assertEquals(EvidencePathogenicity.strong,
                evidenceEntry.getPathogenicity());
        try {
            curationEntries = knownVariantWrapper.getCurationEntryByHeritablePhenotype(phenotype, null);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(ConsistencyStatus.consensus,
                curationEntries.get(0).getCuration().getConsistencyStatus());
        /*
        Adds a second evidence to the first phenotype indication of benignity
         */
        List<HeritablePhenotype> heritablePhenotypes2 = new LinkedList<>();
        HeritablePhenotype heritablePhenotype2 = new HeritablePhenotype();
        heritablePhenotype2.setPhenotype(phenotype2);
        heritablePhenotype2.setInheritanceMode(ReportedModeOfInheritance.monoallelic);
        heritablePhenotypes2.add(heritablePhenotype);
        heritablePhenotypes2.add(heritablePhenotype2);
        try {
            knownVariantWrapper.addEvidence(
                    submitter,
                    null,
                    SourceType.literature_manual_curation,
                    null,
                    null,
                    null,
                    AlleleOrigin.germline,
                    heritablePhenotypes2,
                    transcript,
                    null,
                    EvidenceBenignity.strong,
                    null,
                    null,
                    null,
                    null,
                    "A very bad evidence"
            );
        }
        catch (IllegalCvaArgumentException ex) {
            assertTrue(false);
        }
        assertEquals(2, knownVariantWrapper.getImpl().getEvidences().size());
        try {
            evidenceEntriesEmpty =
                    knownVariantWrapper.getEvidenceEntryByHeritablePhenotype(phenotype, ReportedModeOfInheritance.biallelic);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(0, evidenceEntriesEmpty.size());
        try {
            evidenceEntries = knownVariantWrapper.getEvidenceEntryByHeritablePhenotype(
                    phenotype, ReportedModeOfInheritance.monoallelic_maternally_imprinted);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(2, evidenceEntries.size());
        try {
            evidenceEntries = knownVariantWrapper.getEvidenceEntryByHeritablePhenotype(phenotype, null);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(2, evidenceEntries.size());
        evidenceEntry = evidenceEntries.get(0);
        List<EvidenceEntry> evidenceEntries2 = null;
        try {
            evidenceEntries2 = knownVariantWrapper.getEvidenceEntryByHeritablePhenotype(
                    phenotype2, null);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(1, evidenceEntries2.size());
        EvidenceEntry evidenceEntry2 = evidenceEntries2.get(0);
        assertEquals(submitter, evidenceEntry2.getSubmitter());
        assertEquals(SourceType.literature_manual_curation,
                evidenceEntry2.getSource().getType());
        assertEquals(AlleleOrigin.germline,
                evidenceEntry2.getAlleleOrigin());
        assertEquals(2,
                evidenceEntry2.getHeritablePhenotypes().size());
        assertEquals(phenotype,
                evidenceEntry2.getHeritablePhenotypes().get(0).getPhenotype());
        assertEquals(ReportedModeOfInheritance.monoallelic_maternally_imprinted,
                evidenceEntry2.getHeritablePhenotypes().get(0).getInheritanceMode());
        assertEquals(phenotype2,
                evidenceEntry2.getHeritablePhenotypes().get(1).getPhenotype());
        assertEquals(ReportedModeOfInheritance.monoallelic,
                evidenceEntry2.getHeritablePhenotypes().get(1).getInheritanceMode());
        assertEquals(null,
                evidenceEntry2.getPathogenicity());
        assertEquals(EvidenceBenignity.strong,
                evidenceEntry2.getBenignity());
        // curation is now in conflict as there are two conflicting evidences
        try {
            curationEntries = knownVariantWrapper.getCurationEntryByHeritablePhenotype(phenotype, null);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(ConsistencyStatus.conflict,
                curationEntries.get(0).getCuration().getConsistencyStatus());
    }

    /**
     * Test for null curator
     */
    @Test(expected = IllegalCvaArgumentException.class)
    public void testKnownVariantWrapper2() throws IllegalCvaArgumentException {

        String submitter = "theSubmitter";
        String chromosome = "chr19";
        Integer position = 44908684;
        String reference = "T";
        String alternate = "C";
        KnownVariantWrapper knownVariantWrapper = this.createKnownVariant(
                submitter,
                chromosome,
                position,
                reference,
                alternate
        );
        String curator = null;
        String phenotype = "HPO:000001";
        ReportedModeOfInheritance inheritance = ReportedModeOfInheritance.monoallelic_maternally_imprinted;
        ManualCurationConfidence manualCurationConfidence = ManualCurationConfidence.high_confidence;
        ConsistencyStatus consistencyStatus = ConsistencyStatus.consensus;
        Float penetrance = 0.99f;
        Boolean variableExpressivity = true;
        String transcript = null;
        knownVariantWrapper.addCuration(
                curator,
                phenotype,
                inheritance,
                transcript,
                CurationClassification.pathogenic_variant,
                manualCurationConfidence,
                consistencyStatus,
                penetrance,
                variableExpressivity
        );
    }

    /**
     * Test for "" curator
     */
    @Test(expected = IllegalCvaArgumentException.class)
    public void testKnownVariantWrapper3() throws IllegalCvaArgumentException {

        String submitter = "theSubmitter";
        String chromosome = "chr19";
        Integer position = 44908684;
        String reference = "T";
        String alternate = "C";
        KnownVariantWrapper knownVariantWrapper = this.createKnownVariant(
                submitter,
                chromosome,
                position,
                reference,
                alternate
        );
        String curator = "";
        String phenotype = "HPO:000001";
        ReportedModeOfInheritance inheritance = ReportedModeOfInheritance.monoallelic_maternally_imprinted;
        ManualCurationConfidence manualCurationConfidence = ManualCurationConfidence.high_confidence;
        ConsistencyStatus consistencyStatus = ConsistencyStatus.consensus;
        Float penetrance = 0.99f;
        Boolean variableExpressivity = true;
        String transcript = null;
        knownVariantWrapper.addCuration(
                curator,
                phenotype,
                inheritance,
                transcript,
                CurationClassification.pathogenic_variant,
                manualCurationConfidence,
                consistencyStatus,
                penetrance,
                variableExpressivity
        );
    }

    /**
     * Test for null phenotype
     */
    @Test(expected = IllegalCvaArgumentException.class)
    public void testKnownVariantWrapper4() throws IllegalCvaArgumentException {

        String submitter = "theSubmitter";
        String chromosome = "chr19";
        Integer position = 44908684;
        String reference = "T";
        String alternate = "C";
        KnownVariantWrapper knownVariantWrapper = this.createKnownVariant(
                submitter,
                chromosome,
                position,
                reference,
                alternate
        );
        String curator = "theCurator";
        String phenotype = null;
        ReportedModeOfInheritance inheritance = ReportedModeOfInheritance.monoallelic_maternally_imprinted;
        ManualCurationConfidence manualCurationConfidence = ManualCurationConfidence.high_confidence;
        ConsistencyStatus consistencyStatus = ConsistencyStatus.consensus;
        Float penetrance = 0.99f;
        Boolean variableExpressivity = true;
        String transcript = null;
        knownVariantWrapper.addCuration(
                curator,
                phenotype,
                inheritance,
                transcript,
                CurationClassification.pathogenic_variant,
                manualCurationConfidence,
                consistencyStatus,
                penetrance,
                variableExpressivity
        );
    }

    /**
     * Test for "" phenotype
     */
    @Test(expected = IllegalCvaArgumentException.class)
    public void testKnownVariantWrapper5() throws IllegalCvaArgumentException {

        String submitter = "theSubmitter";
        String chromosome = "chr19";
        Integer position = 44908684;
        String reference = "T";
        String alternate = "C";
        KnownVariantWrapper knownVariantWrapper = this.createKnownVariant(
                submitter,
                chromosome,
                position,
                reference,
                alternate
        );
        String curator = "theCurator";
        String phenotype = "";
        ReportedModeOfInheritance inheritance = ReportedModeOfInheritance.monoallelic_maternally_imprinted;
        ManualCurationConfidence manualCurationConfidence = ManualCurationConfidence.high_confidence;
        ConsistencyStatus consistencyStatus = ConsistencyStatus.consensus;
        Float penetrance = 0.99f;
        Boolean variableExpressivity = true;
        String transcript = null;
        knownVariantWrapper.addCuration(
                curator,
                phenotype,
                inheritance,
                transcript,
                CurationClassification.pathogenic_variant,
                manualCurationConfidence,
                consistencyStatus,
                penetrance,
                variableExpressivity
        );
    }

    /**
     * Test for null inheritance mode being set to NA
     */
    @Test
    public void testKnownVariantWrapper6() throws IllegalCvaArgumentException {

        String submitter = "theSubmitter";
        String chromosome = "chr19";
        Integer position = 44908684;
        String reference = "T";
        String alternate = "C";
        KnownVariantWrapper knownVariantWrapper = this.createKnownVariant(
                submitter,
                chromosome,
                position,
                reference,
                alternate
        );
        String curator = "theCurator";
        String phenotype = "HP:0000001";
        ReportedModeOfInheritance inheritance = null;
        ManualCurationConfidence manualCurationConfidence = ManualCurationConfidence.high_confidence;
        ConsistencyStatus consistencyStatus = ConsistencyStatus.consensus;
        Float penetrance = 0.99f;
        Boolean variableExpressivity = true;
        String transcript = null;
        createCuration(
                knownVariantWrapper,
                curator,
                phenotype,
                inheritance,
                transcript,
                CurationClassification.pathogenic_variant,
                manualCurationConfidence,
                consistencyStatus,
                penetrance,
                variableExpressivity
        );
        List<CurationEntry> curationEntries = null;
        try {
            curationEntries = knownVariantWrapper.getCurationEntryByHeritablePhenotype(phenotype, inheritance);
        }
        catch (IllegalCvaArgumentException ex) {
            assertTrue(false);
        }
        assertEquals(ReportedModeOfInheritance.NA,
                curationEntries.get(0).getCuration().getHeritablePhenotype().getInheritanceMode());
    }

    /**
     * Test for null submitter
     */
    @Test(expected = IllegalCvaArgumentException.class)
    public void testKnownVariantWrapper7() throws IllegalCvaArgumentException {

        String submitter = "theSubmitter";
        String chromosome = "chr19";
        Integer position = 44908684;
        String reference = "T";
        String alternate = "C";
        KnownVariantWrapper knownVariantWrapper = this.createKnownVariant(
                submitter,
                chromosome,
                position,
                reference,
                alternate
        );
        List<HeritablePhenotype> heritablePhenotypes = new LinkedList<>();
        HeritablePhenotype heritablePhenotype = new HeritablePhenotype();
        submitter = null;
        String phenotype = "HP:0000001";
        String transcript = null;
        heritablePhenotype.setPhenotype(phenotype);
        heritablePhenotype.setInheritanceMode(ReportedModeOfInheritance.monoallelic_maternally_imprinted);
        heritablePhenotypes.add(heritablePhenotype);
            knownVariantWrapper.addEvidence(
                    submitter,
                    null,
                    SourceType.literature_manual_curation,
                    null,
                    null,
                    null,
                    AlleleOrigin.germline,
                    heritablePhenotypes,
                    transcript,
                    EvidencePathogenicity.strong,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "A very bad evidence"
        );
    }

    /**
     * Test for "" submitter
     */
    @Test(expected = IllegalCvaArgumentException.class)
    public void testKnownVariantWrapper8() throws IllegalCvaArgumentException {

        String submitter = "theSubmitter";
        String chromosome = "chr19";
        Integer position = 44908684;
        String reference = "T";
        String alternate = "C";
        KnownVariantWrapper knownVariantWrapper = this.createKnownVariant(
                submitter,
                chromosome,
                position,
                reference,
                alternate
        );
        List<HeritablePhenotype> heritablePhenotypes = new LinkedList<>();
        HeritablePhenotype heritablePhenotype = new HeritablePhenotype();
        submitter = "";
        String phenotype = "HP:0000001";
        String transcript = null;
        heritablePhenotype.setPhenotype(phenotype);
        heritablePhenotype.setInheritanceMode(ReportedModeOfInheritance.monoallelic_maternally_imprinted);
        heritablePhenotypes.add(heritablePhenotype);
        knownVariantWrapper.addEvidence(
                submitter,
                null,
                SourceType.literature_manual_curation,
                null,
                null,
                null,
                AlleleOrigin.germline,
                heritablePhenotypes,
                transcript,
                EvidencePathogenicity.strong,
                null,
                null,
                null,
                null,
                null,
                "A very bad evidence"
        );
    }

    /**
     * Test for non null pathogenicity and benignity
     */
    @Test(expected = IllegalCvaArgumentException.class)
    public void testKnownVariantWrapper9() throws IllegalCvaArgumentException {

        String submitter = "theSubmitter";
        String chromosome = "chr19";
        Integer position = 44908684;
        String reference = "T";
        String alternate = "C";
        KnownVariantWrapper knownVariantWrapper = this.createKnownVariant(
                submitter,
                chromosome,
                position,
                reference,
                alternate
        );
        List<HeritablePhenotype> heritablePhenotypes = new LinkedList<>();
        HeritablePhenotype heritablePhenotype = new HeritablePhenotype();
        submitter = "theSubmitter";
        String phenotype = "HP:0000001";
        String transcript = null;
        heritablePhenotype.setPhenotype(phenotype);
        heritablePhenotype.setInheritanceMode(ReportedModeOfInheritance.monoallelic_maternally_imprinted);
        heritablePhenotypes.add(heritablePhenotype);
        knownVariantWrapper.addEvidence(
                submitter,
                null,
                SourceType.literature_manual_curation,
                null,
                null,
                null,
                AlleleOrigin.germline,
                heritablePhenotypes,
                transcript,
                EvidencePathogenicity.strong,
                EvidenceBenignity.strong,
                null,
                null,
                null,
                null,
                "A very bad evidence"
        );
    }

    /**
     * Test for both null pathogenicity and benignity
     */
    @Test(expected = IllegalCvaArgumentException.class)
    public void testKnownVariantWrapper10() throws IllegalCvaArgumentException {

        String submitter = "theSubmitter";
        String chromosome = "chr19";
        Integer position = 44908684;
        String reference = "T";
        String alternate = "C";
        KnownVariantWrapper knownVariantWrapper = this.createKnownVariant(
                submitter,
                chromosome,
                position,
                reference,
                alternate
        );
        List<HeritablePhenotype> heritablePhenotypes = new LinkedList<>();
        HeritablePhenotype heritablePhenotype = new HeritablePhenotype();
        submitter = "theSubmitter";
        String phenotype = "HP:0000001";
        String transcript = null;
        heritablePhenotype.setPhenotype(phenotype);
        heritablePhenotype.setInheritanceMode(ReportedModeOfInheritance.monoallelic_maternally_imprinted);
        heritablePhenotypes.add(heritablePhenotype);
        knownVariantWrapper.addEvidence(
                submitter,
                null,
                SourceType.literature_manual_curation,
                null,
                null,
                null,
                AlleleOrigin.germline,
                heritablePhenotypes,
                transcript,
                null,
                null,
                null,
                null,
                null,
                null,
                "A very bad evidence"
        );
    }

    /**
     * Test for null AlleleOrigin being set to `unknown`
     */
    @Test
    public void testKnownVariantWrapper11() {

        String submitter = "theSubmitter";
        String chromosome = "chr19";
        Integer position = 44908684;
        String reference = "T";
        String alternate = "C";
        KnownVariantWrapper knownVariantWrapper = this.createKnownVariant(
                submitter,
                chromosome,
                position,
                reference,
                alternate
        );
        List<HeritablePhenotype> heritablePhenotypes = new LinkedList<>();
        HeritablePhenotype heritablePhenotype = new HeritablePhenotype();
        submitter = "theSubmitter";
        String phenotype = "HP:0000001";
        String transcript = null;
        heritablePhenotype.setPhenotype(phenotype);
        heritablePhenotype.setInheritanceMode(ReportedModeOfInheritance.monoallelic_maternally_imprinted);
        heritablePhenotypes.add(heritablePhenotype);
        try {
            knownVariantWrapper.addEvidence(
                    submitter,
                    null,
                    SourceType.literature_manual_curation,
                    null,
                    null,
                    null,
                    null,
                    heritablePhenotypes,
                    transcript,
                    EvidencePathogenicity.moderate,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "A very bad evidence"
            );
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        List<EvidenceEntry> evidenceEntries = null;
        try {
            evidenceEntries = knownVariantWrapper.getEvidenceEntryByHeritablePhenotype(
                    phenotype, null);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(AlleleOrigin.unknown,
                evidenceEntries.get(0).getAlleleOrigin());
    }

    /**
     * Test for null source type
     */
    @Test(expected = IllegalCvaArgumentException.class)
    public void testKnownVariantWrapper12() throws IllegalCvaArgumentException {

        String submitter = "theSubmitter";
        String chromosome = "chr19";
        Integer position = 44908684;
        String reference = "T";
        String alternate = "C";
        KnownVariantWrapper knownVariantWrapper = this.createKnownVariant(
                submitter,
                chromosome,
                position,
                reference,
                alternate
        );
        List<HeritablePhenotype> heritablePhenotypes = new LinkedList<>();
        HeritablePhenotype heritablePhenotype = new HeritablePhenotype();
        submitter = "theSubmitter";
        String phenotype = "HP:0000001";
        String transcript = null;
        heritablePhenotype.setPhenotype(phenotype);
        heritablePhenotype.setInheritanceMode(ReportedModeOfInheritance.monoallelic_maternally_imprinted);
        heritablePhenotypes.add(heritablePhenotype);
        knownVariantWrapper.addEvidence(
                submitter,
                null,
                null,
                null,
                null,
                null,
                AlleleOrigin.germline,
                heritablePhenotypes,
                transcript,
                EvidencePathogenicity.moderate,
                null,
                null,
                null,
                null,
                null,
                "A very bad evidence"
        );
    }

    /**
     * Test for penetrance > 1.0
     */
    @Test(expected = IllegalCvaArgumentException.class)
    public void testKnownVariantWrapper13() throws IllegalCvaArgumentException {

        String submitter = "theSubmitter";
        String chromosome = "chr19";
        Integer position = 44908684;
        String reference = "T";
        String alternate = "C";
        KnownVariantWrapper knownVariantWrapper = this.createKnownVariant(
                submitter,
                chromosome,
                position,
                reference,
                alternate
        );
        String curator = "theCurator";
        String phenotype = "HPO:000001";
        ReportedModeOfInheritance inheritance = ReportedModeOfInheritance.monoallelic_maternally_imprinted;
        ManualCurationConfidence manualCurationConfidence = ManualCurationConfidence.high_confidence;
        ConsistencyStatus consistencyStatus = ConsistencyStatus.consensus;
        Float penetrance = 1.1f;
        Boolean variableExpressivity = true;
        String transcript = null;
        knownVariantWrapper.addCuration(
                curator,
                phenotype,
                inheritance,
                transcript,
                CurationClassification.pathogenic_variant,
                manualCurationConfidence,
                consistencyStatus,
                penetrance,
                variableExpressivity
        );
    }

    /**
     * Test for penetrance < 0.0
     */
    @Test(expected = IllegalCvaArgumentException.class)
    public void testKnownVariantWrapper14() throws IllegalCvaArgumentException {

        String submitter = "theSubmitter";
        String chromosome = "chr19";
        Integer position = 44908684;
        String reference = "T";
        String alternate = "C";
        KnownVariantWrapper knownVariantWrapper = this.createKnownVariant(
                submitter,
                chromosome,
                position,
                reference,
                alternate
        );
        String curator = "theCurator";
        String phenotype = "HPO:000001";
        ReportedModeOfInheritance inheritance = ReportedModeOfInheritance.monoallelic_maternally_imprinted;
        ManualCurationConfidence manualCurationConfidence = ManualCurationConfidence.high_confidence;
        ConsistencyStatus consistencyStatus = ConsistencyStatus.consensus;
        Float penetrance = -0.1f;
        Boolean variableExpressivity = true;
        String transcript = null;
        knownVariantWrapper.addCuration(
                curator,
                phenotype,
                inheritance,
                transcript,
                CurationClassification.pathogenic_variant,
                manualCurationConfidence,
                consistencyStatus,
                penetrance,
                variableExpressivity
        );
    }

    /**
     * Test for curations associated to different transcripts
     */
    @Test
    public void testKnownVariantWrapper15() {

        /*
        Creates a KnownVariantWrapper
         */
        String submitter = "theSubmitter";
        String chromosome = "chr19";
        String chromosomeNormalized = "19";  // OpenCB normalizes chromosome identifiers
        Integer position = 44908684;
        String reference = "T";
        String alternate = "C";
        KnownVariantWrapper knownVariantWrapper = this.createKnownVariant(
                submitter,
                chromosome,
                position,
                reference,
                alternate
        );
        assertNotEquals(chromosome, knownVariantWrapper.getImpl().getVariant().getChromosome());
        assertEquals(chromosomeNormalized, knownVariantWrapper.getImpl().getVariant().getChromosome());
        assertEquals(position, knownVariantWrapper.getImpl().getVariant().getStart());
        assertEquals(reference, knownVariantWrapper.getImpl().getVariant().getReference());
        assertEquals(alternate, knownVariantWrapper.getImpl().getVariant().getAlternate());
        assertNotNull(knownVariantWrapper.getImpl().getVariant().getAnnotation());
        assertEquals(0, knownVariantWrapper.getImpl().getCurations().size());
        assertEquals(0, knownVariantWrapper.getImpl().getEvidences().size());

        /*
        Adds a curation to the variant
         */
        String curator = "theCurator";
        String phenotype = "HPO:000001";
        ReportedModeOfInheritance inheritance = ReportedModeOfInheritance.monoallelic_maternally_imprinted;
        String transcript = knownVariantWrapper.getVariant()
                .getAnnotation().getConsequenceTypes().get(0).getEnsemblTranscriptId();
        ManualCurationConfidence manualCurationConfidence = ManualCurationConfidence.high_confidence;
        ConsistencyStatus consistencyStatus = ConsistencyStatus.consensus;
        Float penetrance = 0.99f;
        Boolean variableExpressivity = true;
        this.createCuration(
                knownVariantWrapper,
                curator,
                phenotype,
                inheritance,
                transcript,
                CurationClassification.benign_variant,
                manualCurationConfidence,
                consistencyStatus,
                penetrance,
                variableExpressivity
        );
        assertEquals(1, knownVariantWrapper.getImpl().getCurations().size());
        List<CurationEntry> curationEntriesEmpty = null;
        try {
            curationEntriesEmpty = knownVariantWrapper.getCurationEntryByHeritablePhenotype(phenotype, ReportedModeOfInheritance.biallelic);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(0, curationEntriesEmpty.size());
        List<CurationEntry> curationEntries = null;
        try {
            curationEntries = knownVariantWrapper.getCurationEntryByHeritablePhenotype(phenotype, inheritance);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(1, curationEntries.size());
        try {
            curationEntries = knownVariantWrapper.getCurationEntryByHeritablePhenotype(phenotype, null);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(1, curationEntries.size());
        CurationEntry curationEntry = curationEntries.get(0);
        assertEquals(1, curationEntry.getHistory().size());
        assertEquals(curator, curationEntry.getHistory().get(0).getCurator());
        assertEquals(phenotype,
                curationEntry.getCuration().getHeritablePhenotype().getPhenotype());
        assertEquals(inheritance,
                curationEntry.getCuration().getHeritablePhenotype()
                        .getInheritanceMode());
        assertEquals(CurationClassification.benign_variant,
                curationEntry.getCuration().getClassification());
        assertEquals(CurationSOClassification.benign_variant,
                curationEntry.getCuration().getSoClassification());
        assertEquals(manualCurationConfidence,
                curationEntry.getCuration().getManualCurationConfidence());
        assertEquals(consistencyStatus,
                curationEntry.getCuration().getConsistencyStatus());
        /*
        Adds a second curation to the same phenotype changing from benign to pathogenic
         */
        this.createCuration(
                knownVariantWrapper,
                curator,
                phenotype,
                inheritance,
                transcript,
                CurationClassification.pathogenic_variant,
                manualCurationConfidence,
                null,
                penetrance,
                variableExpressivity
        );
        assertEquals(1, knownVariantWrapper.getImpl().getCurations().size());
        try {
            curationEntries = knownVariantWrapper.getCurationEntryByHeritablePhenotype(phenotype, null);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(1, curationEntries.size());
        curationEntry = curationEntries.get(0);
        assertEquals(2, curationEntry.getHistory().size());
        assertEquals(curator, curationEntry.getHistory().get(0).getCurator());
        assertEquals(phenotype,
                curationEntry.getCuration().getHeritablePhenotype().getPhenotype());
        assertEquals(inheritance,
                curationEntry.getCuration().getHeritablePhenotype()
                        .getInheritanceMode());
        assertEquals(CurationClassification.pathogenic_variant,
                curationEntry.getCuration().getClassification());
        assertEquals(CurationClassification.benign_variant,
                curationEntry.getHistory().get(1)
                        .getPreviousCuration().getClassification());
        assertEquals(CurationClassification.pathogenic_variant,
                curationEntry.getHistory().get(1)
                        .getNewCuration().getClassification());
        assertEquals(CurationSOClassification.disease_causing_variant,
                curationEntry.getCuration().getSoClassification());
        assertEquals(manualCurationConfidence,
                curationEntry.getCuration().getManualCurationConfidence());
        assertEquals(ConsistencyStatus.consensus,
                curationEntry.getCuration().getConsistencyStatus());
        /*
        Adds a third curation to the same phenotype but not associated to any transcript
         */
        this.createCuration(
                knownVariantWrapper,
                curator,
                phenotype,
                inheritance,
                null,
                CurationClassification.pathogenic_variant,
                manualCurationConfidence,
                null,
                penetrance,
                variableExpressivity
        );
        assertEquals(2, knownVariantWrapper.getImpl().getCurations().size());
        try {
            curationEntries = knownVariantWrapper.getCurationEntryByHeritablePhenotype(phenotype, null);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(2, curationEntries.size());
        curationEntry = curationEntries.get(0);
        assertEquals(2, curationEntry.getHistory().size());
        CurationEntry curationEntry2 = curationEntries.get(1);
        assertEquals(1, curationEntry2.getHistory().size());
    }
}
