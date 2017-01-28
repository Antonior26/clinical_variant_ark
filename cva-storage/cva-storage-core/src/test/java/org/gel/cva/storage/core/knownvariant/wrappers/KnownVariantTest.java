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

import org.gel.cva.storage.core.exceptions.IllegalCvaArgumentException;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.models.cva.avro.*;
import org.gel.models.report.avro.EthnicCategory;
import org.gel.models.report.avro.ReportedModeOfInheritance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opencb.biodata.models.variant.*;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Pablo Riesgo Ferreiro &lt;pablo.ferreiro@genomicsengland.co.uk&gt;
 */
public class KnownVariantTest {

    public KnownVariantTest(){}

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testKnownVariant1() {

        /*
        Creates a KnownVariant
         */
        String submitter = "theSubmitter";
        String chromosome = "chr19";
        String chromosomeNormalized = "19";  // OpenCB normalizes chromosome identifiers
        Integer position = 44908684;
        String reference = "T";
        String alternate = "C";
        KnownVariant knownVariant = null;
        try {
            knownVariant = new KnownVariant(
                    submitter,
                    chromosome,
                    position,
                    reference,
                    alternate);
        }
        catch (IllegalCvaConfigurationException ex) {
            assertTrue(false);  // this should never raise
        }
        catch (VariantAnnotatorException ex) {
            assertTrue(false);  // this should never raise
        }
        assertNotEquals(chromosome, knownVariant.getImpl().getVariant().getChromosome());
        assertEquals(chromosomeNormalized, knownVariant.getImpl().getVariant().getChromosome());
        assertEquals(position, knownVariant.getImpl().getVariant().getStart());
        assertEquals(reference, knownVariant.getImpl().getVariant().getReference());
        assertEquals(alternate, knownVariant.getImpl().getVariant().getAlternate());
        assertNotNull(knownVariant.getImpl().getVariant().getAnnotation());
        assertEquals(0, knownVariant.getImpl().getCurations().size());
        assertEquals(0, knownVariant.getImpl().getEvidences().size());

        /*
        Adds a curation to the variant
         */
        String curator = "theCurator";
        String phenotype = "HPO:000001";
        ReportedModeOfInheritance inheritance = ReportedModeOfInheritance.monoallelic_maternally_imprinted;
        ManualCurationConfidence manualCurationConfidence = ManualCurationConfidence.high_confidence;
        ConsistencyStatus consistencyStatus = ConsistencyStatus.consensus;
        try {
            knownVariant.addCuration(
                    curator,
                    phenotype,
                    inheritance,
                    CurationClassification.benign_variant,
                    manualCurationConfidence,
                    consistencyStatus
            );
        }
        catch (IllegalCvaArgumentException ex) {
            assertTrue(false);  // this should never raise
        }
        assertEquals(1, knownVariant.getImpl().getCurations().size());
        List<CurationEntry> curationEntriesEmpty = null;
        try {
            curationEntriesEmpty = knownVariant.getCurationEntryByHeritablePhenotype(phenotype, ReportedModeOfInheritance.biallelic);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(0, curationEntriesEmpty.size());
        List<CurationEntry> curationEntries = null;
        try {
            curationEntries = knownVariant.getCurationEntryByHeritablePhenotype(phenotype, inheritance);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(1, curationEntries.size());
        try {
            curationEntries = knownVariant.getCurationEntryByHeritablePhenotype(phenotype, null);
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
        try {
            knownVariant.addCuration(
                    curator,
                    phenotype,
                    inheritance,
                    CurationClassification.pathogenic_variant,
                    manualCurationConfidence,
                    null
            );
        }
        catch (IllegalCvaArgumentException ex) {
            assertTrue(false);  // this should never raise
        }
        assertEquals(1, knownVariant.getImpl().getCurations().size());
        try {
            curationEntries = knownVariant.getCurationEntryByHeritablePhenotype(phenotype, null);
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
        try {
            knownVariant.addCuration(
                    curator,
                    phenotype2,
                    inheritance,
                    CurationClassification.established_risk_allele,
                    manualCurationConfidence,
                    null
            );
        }
        catch (IllegalCvaArgumentException ex) {
            assertTrue(false);  // this should never raise
        }
        assertEquals(2, knownVariant.getImpl().getCurations().size());
        try {
            curationEntries = knownVariant.getCurationEntryByHeritablePhenotype(phenotype2, null);
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
            knownVariant.addEvidence(
                    submitter,
                    null,
                    SourceType.literature_manual_curation,
                    null,
                    null,
                    null,
                    AlleleOrigin.germline,
                    heritablePhenotypes,
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
        assertEquals(1, knownVariant.getImpl().getEvidences().size());
        List<EvidenceEntry> evidenceEntriesEmpty =
                null;
        try {
            evidenceEntriesEmpty = knownVariant.getEvidenceEntryByHeritablePhenotype(
                    phenotype, ReportedModeOfInheritance.biallelic);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(0, evidenceEntriesEmpty.size());
        List<EvidenceEntry> evidenceEntries = null;
        try {
            evidenceEntries = knownVariant.getEvidenceEntryByHeritablePhenotype(
                    phenotype, ReportedModeOfInheritance.monoallelic_maternally_imprinted);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(1, evidenceEntries.size());
        try {
            evidenceEntries = knownVariant.getEvidenceEntryByHeritablePhenotype(phenotype, null);
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
            curationEntries = knownVariant.getCurationEntryByHeritablePhenotype(phenotype, null);
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
            knownVariant.addEvidence(
                    submitter,
                    null,
                    SourceType.literature_manual_curation,
                    null,
                    null,
                    null,
                    AlleleOrigin.germline,
                    heritablePhenotypes2,
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
        assertEquals(2, knownVariant.getImpl().getEvidences().size());
        try {
            evidenceEntriesEmpty =
                    knownVariant.getEvidenceEntryByHeritablePhenotype(phenotype, ReportedModeOfInheritance.biallelic);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(0, evidenceEntriesEmpty.size());
        try {
            evidenceEntries = knownVariant.getEvidenceEntryByHeritablePhenotype(
                    phenotype, ReportedModeOfInheritance.monoallelic_maternally_imprinted);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(2, evidenceEntries.size());
        try {
            evidenceEntries = knownVariant.getEvidenceEntryByHeritablePhenotype(phenotype, null);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(2, evidenceEntries.size());
        evidenceEntry = evidenceEntries.get(0);
        List<EvidenceEntry> evidenceEntries2 = null;
        try {
            evidenceEntries2 = knownVariant.getEvidenceEntryByHeritablePhenotype(
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
            curationEntries = knownVariant.getCurationEntryByHeritablePhenotype(phenotype, null);
        } catch (IllegalCvaArgumentException e) {
            assertTrue(false);
        }
        assertEquals(ConsistencyStatus.conflict,
                curationEntries.get(0).getCuration().getConsistencyStatus());
    }


}
