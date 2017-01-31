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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaArgumentException;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.helpers.AvroHelper;
import org.gel.cva.storage.core.helpers.CvaDateFormatter;
import org.gel.models.cva.avro.*;
import org.gel.models.report.avro.EthnicCategory;
import org.gel.models.report.avro.ReportedModeOfInheritance;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantNormalizer;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;
import org.opencb.opencga.storage.core.variant.annotation.annotators.CellBaseDirectVariantAnnotator;

import java.io.Serializable;
import java.util.*;

/**
 *
 * This is a wrapper class for the CuratedVariantAvro record in Avro.
 *
 * @author Pablo Riesgo Ferreiro &lt;pablo.ferreiro@genomicsengland.co.uk&gt;
 */
@JsonIgnoreProperties({"impl", "variant"})
public class KnownVariantWrapper implements Serializable, IKnownVariantWrapper {

    private KnownVariant impl;
    private Variant variant;
    private List<String> transcripts = null;
    private static CellBaseDirectVariantAnnotator cellBaseDirectVariantAnnotator;
    private static VariantNormalizer variantNormalizer;

    {
        // Initializes static elements to annotate and normalize
        KnownVariantWrapper.cellBaseDirectVariantAnnotator =
                CvaConfiguration.getCellBaseDirectVariantAnnotator();
        KnownVariantWrapper.variantNormalizer = new VariantNormalizer(
                true, true, true);
    }

    /**
     * Constructor from the avro object
     * @param avro the avro object
     */
    //TODO: do we need this for reading the DB???
    /*
    public KnownVariantWrapper(KnownVariantAvro avro) {
        Objects.requireNonNull(avro);
        this.variant = new Variant(avro.getVariant());
        this.impl = avro;
    }
    */

    /**
     * Constructor for KnownVariantWrapper
     * @param submitter         the submitter of the variant
     * @param variant           the Variant wrapper
     */
    public KnownVariantWrapper(
            String submitter,
            Variant variant)
            throws VariantAnnotatorException,
            CvaException
    {
        // normalizes the variant before storing it
        List<Variant> variants = KnownVariantWrapper.variantNormalizer.apply(Collections.singletonList(variant));
        if (variants == null || variants.size() == 0) {
            throw new CvaException("Unexpected error normalizing variants.");
        }
        if (variants.size() > 1) {
            throw new IllegalCvaArgumentException("Cannot register a multi-allelic variant in CVA. " +
                    "You need to split the variant and register each of them separately.");
        }
        // stores a reference to the OpenCB variant
        this.variant = variant;
        // creates the underlying KnownVariantAvro model with default values
        this.impl = new KnownVariant(
                submitter,
                this.variant.getImpl(),
                new LinkedList<>(),
                new LinkedList<>(),
                new LinkedList<>()
        );
        // annotates the variant
        this.annotateVariant();
    }

    /**
     * Constructor for KnownVariantWrapper
     * @param submitter     the user name for the submitter
     * @param chromosome    the chromosome identifier, it will be normalized
     * @param position      the genomic coordinate
     * @param reference     the reference base/s
     * @param alternate     the alternate base/s
     * @throws VariantAnnotatorException            wrong annotation (this excceptions need to be managed...)
     * @throws IllegalCvaConfigurationException     Wrong CVA settings
     */
    public KnownVariantWrapper(
            String submitter,
            String chromosome,
            int position,
            String reference,
            String alternate)
            throws VariantAnnotatorException, CvaException{
        this(submitter, new Variant(chromosome, position, reference, alternate));
    }

    /**
     * Getter for Variant, no setter available as it should be passed in the constructor
     * @return  the variant
     */
    public Variant getVariant() {
        return variant;
    }

    /**
     * Getter for CuratedVariantAvro, no setter available as it should be passed in the constructor
     * @return  the Avro serialized KnownVariantWrapper
     */
    public KnownVariant getImpl() {
        return impl;
    }

    /**
     * Adds a curation to this KnownVariant
     * @param curator                       the curator's user name
     * @param phenotype                     the phenotype to which the curation is associated
     * @param modeOfInheritance             the mode of inheritance
     * @param transcript                    the transcript to which the curation refers specifically
     * @param curationClassification        the curation classification
     * @param manualCurationConfidence      the manual curation confidence
     * @param consistencyStatus             the consistency status of the curation
     * @throws IllegalCvaArgumentException  wrong parameters in the call
     */
    @Override
    public void addCuration(String curator,
                            String phenotype,
                            ReportedModeOfInheritance modeOfInheritance,
                            String transcript,
                            CurationClassification curationClassification,
                            ManualCurationConfidence manualCurationConfidence,
                            ConsistencyStatus consistencyStatus,
                            Float penetrance,
                            Boolean variableExpressivity)
            throws IllegalCvaArgumentException {

        // Create a new Curation
        HeritablePhenotype heritablePhenotype = new HeritablePhenotype(phenotype, modeOfInheritance);
        Curation newCuration = new Curation(
                heritablePhenotype,
                transcript,
                curationClassification,
                null,
                this.getSOClassificationFromClassification(curationClassification),
                manualCurationConfidence,
                consistencyStatus,
                penetrance,
                variableExpressivity
        );
        // Sanity checks on the curation
        newCuration = this.curationSanityChecks(newCuration, curator);
        // Checks if there are previous curations for this heritablePhenotype and transcript
        CurationEntry curationEntry = this.getCurationEntryByHeritablePhenotype(heritablePhenotype, transcript);
        if (curationEntry == null) {
            // First curation
            CurationHistoryEntry curationHistoryEntry = new CurationHistoryEntry(
                    CvaDateFormatter.getCurrentFormattedDate(),
                    null,
                    newCuration,
                    curator,
                    Collections.emptyList());
            List<CurationHistoryEntry> curationHistory = new LinkedList<>();
            curationHistory.add(curationHistoryEntry);
            curationEntry = new CurationEntry(newCuration, curationHistory);
        }
        else {
            // Add curation to existing
            CurationHistoryEntry curationHistoryEntry = new CurationHistoryEntry(
                    CvaDateFormatter.getCurrentFormattedDate(),
                    curationEntry.getCuration(),
                    newCuration,
                    curator,
                    Collections.emptyList());
            List<CurationHistoryEntry> curationHistory = curationEntry.getHistory();
            curationHistory.add(curationHistoryEntry);
            curationEntry.setHistory(curationHistory);
            curationEntry.setCuration(newCuration);
        }
        // Store the CurationEntry
        this.setCurationEntry(curationEntry);
        // Updates consistency status
        if (consistencyStatus == null) {
            this.updateConsistencyStatus(heritablePhenotype, transcript);
        }
    }

    /**
     * Adds an evidence to the list of evidences
     * @param submitter                 the submitter of the evidence
     * @param sourceName                the name of the evidence source
     * @param sourceType                the type of the evidence source
     * @param sourceVersion             the version of the evidence source
     * @param sourceUrl                 the URL of the evidence source
     * @param sourceId                  the ID of the evidence source
     * @param alleleOrigin              the allele origin
     * @param heritablePhenotypes       the list of heritable phenotypes
     * @param transcript                the transcript to which the evidence specifically refers
     * @param evidencePathogenicity     the pathogenicity of the evidence
     * @param evidenceBenignity         the benignity of the evidence
     * @param pubmedId                  the PubMed id
     * @param study                     the study
     * @param numberOfIndividuals       the number of individuals
     * @param ethnicCategory            the ethnic category of assessed individuals
     * @param description               the evidence description
     * @throws IllegalCvaArgumentException      wrong parameters in the call
     */
    @Override
    public void addEvidence(String submitter,
                            String sourceName,
                            SourceType sourceType,
                            String sourceVersion,
                            String sourceUrl,
                            String sourceId,
                            AlleleOrigin alleleOrigin,
                            List<HeritablePhenotype> heritablePhenotypes,
                            String transcript,
                            EvidencePathogenicity evidencePathogenicity,
                            EvidenceBenignity evidenceBenignity,
                            String pubmedId,
                            String study,
                            Integer numberOfIndividuals,
                            EthnicCategory ethnicCategory,
                            String description
    )
            throws IllegalCvaArgumentException {

        // Creates a new EvidenceEntry
        EvidenceSource evidenceSource = new EvidenceSource(
                sourceName, sourceType, sourceVersion, sourceUrl, sourceId);
        EvidenceEntry evidenceEntry = new EvidenceEntry(
                CvaDateFormatter.getCurrentFormattedDate(),
                submitter,
                evidenceSource,
                alleleOrigin,
                heritablePhenotypes,
                transcript,
                evidencePathogenicity,
                evidenceBenignity,
                pubmedId,
                study,
                numberOfIndividuals,
                ethnicCategory,
                description,
                Collections.emptyList()
        );
        // Perform sanity checks
        evidenceEntry = this.evidenceSanityChecks(evidenceEntry);
        // Adds evidence to the list of evidences
        List<EvidenceEntry> evidences = this.impl.getEvidences();
        evidences.add(evidenceEntry);
        impl.setEvidences(evidences);
        // Update the consistency status for all affected curations
        for (HeritablePhenotype heritablePhenotype: heritablePhenotypes) {
            this.updateConsistencyStatus(heritablePhenotype, transcript);
        }
    }

    /**
     * Gets the list of `CurationEntry` matching phenotype and mode of inheritance.
     * When mode of inheritance is provided only one `CurationEntry` will be returned.
     * When mode of inheritance is not provided all `CurationEntry` matching only the phenotype will be returned.
     * @param phenotype             the phenotype
     * @param modeOfInheritance     the mode of inheritance
     * @return                      matching list of `CurationEntry`
     * @throws IllegalCvaArgumentException      when no phenotype is provided
     */
    @Override
    public List<CurationEntry> getCurationEntryByHeritablePhenotype(
            String phenotype,
            ReportedModeOfInheritance modeOfInheritance)
            throws IllegalCvaArgumentException {

        return this.getCurationEntryByHeritablePhenotypes(phenotype,
                modeOfInheritance != null? Collections.singletonList(modeOfInheritance) : null);
    }

    /**
     * Gets the list of `CurationEntry` matching phenotype and any of the modes of inheritance.
     * When mode of inheritance is not provided all `CurationEntry` matching only the phenotype will be returned.
     * @param phenotype             the phenotype
     * @param modeOfInheritances    the list of modes of inheritance
     * @return                      matching list of `CurationEntry`
     * @throws IllegalCvaArgumentException      when no phenotype is provided
     */
    @Override
    public List<CurationEntry> getCurationEntryByHeritablePhenotypes(
            String phenotype,
            List<ReportedModeOfInheritance> modeOfInheritances)
            throws IllegalCvaArgumentException {

        if (phenotype == null || phenotype.equals("")){
            throw new IllegalCvaArgumentException("Phenotype must be provided!");
        }
        List<CurationEntry> results = new LinkedList<>();
        for (CurationEntry curationEntry : this.impl.getCurations()) {
            HeritablePhenotype heritablePhenotype = curationEntry.getCuration().getHeritablePhenotype();
            String thisPhenotype = heritablePhenotype.getPhenotype();
            ReportedModeOfInheritance thisModeOfInheritance = heritablePhenotype.getInheritanceMode();
            if (phenotype.equals(thisPhenotype) && (modeOfInheritances == null || modeOfInheritances.size() == 0)) {
                // If no mode of inheritance is provided returns all matching phenotypes
                results.add(curationEntry);
            }
            else if (phenotype.equals(thisPhenotype)) {
                // Only returns those matching any of the provided modes of inheritance
                for (ReportedModeOfInheritance modeOfInheritance : modeOfInheritances) {
                    if (thisModeOfInheritance == modeOfInheritance) {
                        results.add(curationEntry);
                    }
                }
            }
        }
        return results;
    }

    /**
     * Gets the list of `EvidenceEntry` matching phenotype and mode of inheritance.
     * When mode of inheritance is provided only one `EvidenceEntry` will be returned.
     * When mode of inheritance is not provided all `EvidenceEntry` matching only the phenotype will be returned.
     * @param phenotype             the phenotype
     * @param modeOfInheritance     the mode of inheritance
     * @return                      matching list of `EvidenceEntry`
     * @throws IllegalCvaArgumentException      when no phenotype is provided
     */
    @Override
    public List<EvidenceEntry> getEvidenceEntryByHeritablePhenotype(
            String phenotype,
            ReportedModeOfInheritance modeOfInheritance)
            throws IllegalCvaArgumentException {

        return this.getEvidenceEntryByHeritablePhenotypes(phenotype,
                modeOfInheritance != null? Collections.singletonList(modeOfInheritance) : null);
    }

    /**
     * Gets the list of `EvidenceEntry` matching phenotype and any of the modes of inheritance.
     * When mode of inheritance is not provided all `EvidenceEntry` matching only the phenotype will be returned.
     * @param phenotype             the phenotype
     * @param modeOfInheritances    the mode of inheritance
     * @return                      matching list of `EvidenceEntry`
     * @throws IllegalCvaArgumentException      when no phenotype is provided
     */
    @Override
    public List<EvidenceEntry> getEvidenceEntryByHeritablePhenotypes(
            String phenotype,
            List<ReportedModeOfInheritance> modeOfInheritances)
            throws IllegalCvaArgumentException {

        if (phenotype == null || phenotype.equals("")){
            throw new IllegalCvaArgumentException("Phenotype must be provided!");
        }
        List<EvidenceEntry> results = new LinkedList<>();
        for (EvidenceEntry evidenceEntry : this.impl.getEvidences()) {
            List<HeritablePhenotype> heritablePhenotypes = evidenceEntry.getHeritablePhenotypes();
            for (HeritablePhenotype heritablePhenotype : heritablePhenotypes) {
                String thisPhenotype = heritablePhenotype.getPhenotype();
                if (phenotype.equals(thisPhenotype) &&
                        (modeOfInheritances == null || modeOfInheritances.size() == 0)) {
                    results.add(evidenceEntry);
                    break;
                }
                else if (phenotype.equals(thisPhenotype)) {
                    ReportedModeOfInheritance thisModeOfInheritance = heritablePhenotype.getInheritanceMode();
                    Boolean match = false;
                    for (ReportedModeOfInheritance modeOfInheritance : modeOfInheritances) {
                        if (thisModeOfInheritance == modeOfInheritance) {
                            results.add(evidenceEntry);
                            match = true;
                            break;
                        }
                    }
                    if (match) {
                        break;
                    }
                }
            }
        }
        return results;
    }

    /**
     * Checks if a given transcript exists in the annotations
     * @param transcript       the transcript to be checked for existence
     * @return                  boolean indicating if the transcript exists or not
     * PRE: the variant is annotated
     */
    private Boolean isTranscriptInAnnotations(String transcript) {

        Boolean match = false;
        if (this.transcripts != null) {
            match = this.transcripts.contains(transcript);
        }
        return match;
    }

    /**
     * Checks that values in a Curation event object are valid
     * @param curation      the curation data to be checked
     * @param curator       the curator to be checked
     * @return              the curation object after checking
     * @throws IllegalCvaArgumentException      wrong parameters to call
     */
    private Curation curationSanityChecks(Curation curation, String curator)
            throws IllegalCvaArgumentException {

        // checks required fields at curation
        if (curation == null) {
            throw new IllegalCvaArgumentException("Curation is required!");
        }
        if (curator == null || curator.equals("")) {
            throw new IllegalCvaArgumentException("An unknown curator cannot register a variant");
        }
        if (curation.getHeritablePhenotype() == null) {
            throw new IllegalCvaArgumentException("Curation must be associated to an heritable phenotype");
        }
        // Checks the heritable phenotype values
        HeritablePhenotype heritablePhenotype = curation.getHeritablePhenotype();
        if (heritablePhenotype.getPhenotype() == null || heritablePhenotype.getPhenotype().equals("")) {
            throw new IllegalCvaArgumentException("An heritable phenotype must have a non empty phenotype");
        }
        if (heritablePhenotype.getInheritanceMode() == null) {
            // Sets the inheritance phenotype to NA when null
            heritablePhenotype.setInheritanceMode(ReportedModeOfInheritance.NA);
        }
        // checks correct value of penetrance
        if (curation.getPenetrance() != null &&
                (curation.getPenetrance() < 0 || curation.getPenetrance() > 1)) {
            throw new IllegalCvaArgumentException("Incorrect penetrance value provided. " +
                    "It must be a value in the range [0, 1]");
        }
        // checks if the transcript exists in the annotations if we have annotations
        if (curation.getTranscript() != null) {
            if (! this.isTranscriptInAnnotations(curation.getTranscript())) {
                throw new IllegalCvaArgumentException("Non existing transcript was referenced when trying to add a " +
                        "curation!");
            }
        }
        return curation;
    }

    /**
     * Performs sanity checks on an evidence
     * @param evidenceEntry     The evidence entry to check
     * @return                  The checked evidence entry
     */
    private EvidenceEntry evidenceSanityChecks(EvidenceEntry evidenceEntry)
            throws IllegalCvaArgumentException {

        if (evidenceEntry == null) {
            throw new IllegalCvaArgumentException("EvidenceEntry is required!");
        }
        // checks the EvidenceSource
        this.sourceSanityChecks(evidenceEntry.getSource());
        // checks required parameters and default values in evidence
        if (evidenceEntry.getSubmitter() == null || evidenceEntry.getSubmitter().equals("")) {
            throw new IllegalCvaArgumentException("Submitter is required to register an evidence!");
        }
        if (evidenceEntry.getAlleleOrigin() == null) {
            evidenceEntry.setAlleleOrigin(AlleleOrigin.unknown);
        }
        if (evidenceEntry.getBenignity() != null && evidenceEntry.getPathogenicity() != null) {
            throw new IllegalCvaArgumentException("An evidence might indicate pathogenicity or benignity, never both!");
        }
        if (evidenceEntry.getBenignity() == null && evidenceEntry.getPathogenicity() == null) {
            throw new IllegalCvaArgumentException("An evidence must indicate either pathogenicity or benignity!");
        }
        // checks if the transcript exists in the annotations if we have annotations
        if (evidenceEntry.getTranscript() != null) {
            if (! this.isTranscriptInAnnotations(evidenceEntry.getTranscript())) {
                throw new IllegalCvaArgumentException("Non existing transcript was referenced when trying to add an " +
                        "evidence!");
            }
        }
        return evidenceEntry;
    }

    /**
     * Performs sanity checks on an EvidenceSource
     * @param evidenceSource    the evidence source
     * @throws IllegalCvaArgumentException      wrong parameters to call
     */
    private void sourceSanityChecks(EvidenceSource evidenceSource)
            throws IllegalCvaArgumentException {

        if (evidenceSource == null) {
            throw new IllegalCvaArgumentException("Evidence source is required!");
        }
        if (evidenceSource.getType() == null) {
            throw new IllegalCvaArgumentException("Type of evidence source is required!");
        }
    }

    /**
     * Returns a CurationEntry for the given heritablePhenotype if any.
     * Returns null when there is no match
     * PRE: there can be only one CurationEntry with this HeritablePhenotype and transcript
     * @param heritablePhenotype    A given HeritablePhenotype
     * @param transcript            The transcript to which the curation refers. This parameter is nullable.
     * @return the matching CurationEntry
     */
    private CurationEntry getCurationEntryByHeritablePhenotype(
            HeritablePhenotype heritablePhenotype,
            String transcript) {

        CurationEntry curationEntry = null;
        for (CurationEntry ce: this.impl.getCurations()) {
            HeritablePhenotype thisHeritablePhenotype = ce.getCuration().getHeritablePhenotype();
            String thisTranscript = ce.getCuration().getTranscript();
            if (AvroHelper.areHeritablePhenotypeEqual(thisHeritablePhenotype, heritablePhenotype) &&
                    (AvroHelper.areTranscriptsEqual(thisTranscript, transcript))) {
                curationEntry = ce;
                break;
            }
        }
        return curationEntry;
    }

    /**
     * Stores a new CurationEntry respecting uniqueness by HeritablePhenotype.
     * If a previous CurationEntry exists it replaces it, otherwise a new CurationEntry is stored.
     * @param newCurationEntry     the curationEntry to store
     */
    private void setCurationEntry(CurationEntry newCurationEntry) {

        CurationEntry existingCurationEntry = this.getCurationEntryByHeritablePhenotype(
                newCurationEntry.getCuration().getHeritablePhenotype(),
                newCurationEntry.getCuration().getTranscript());
        List<CurationEntry> curationEntries = impl.getCurations();
        if (existingCurationEntry != null) {
            // Replaces the existing CurationEntry
            Integer index = curationEntries.indexOf(existingCurationEntry);
            curationEntries.set(index, newCurationEntry);
        }
        else {
            // A new CurationEntry is added
            curationEntries.add(newCurationEntry);
        }
        impl.setCurations(curationEntries);
    }

    /**
     * Updates the consistency status for the curation of this variant associated to heritablePhenotype.
     * The update is based on the existing evidences associated to the heritablePhenotype and also those
     * not associated to any phenotype.
     * @param heritablePhenotype    The heritablePhenotype that requires updating consistency status
     * @param transcript            The transcript that requires updatign consistency status
     */
    private Boolean updateConsistencyStatus(HeritablePhenotype heritablePhenotype, String transcript) {

        CurationEntry curationEntry = this.getCurationEntryByHeritablePhenotype(heritablePhenotype, transcript);
        Boolean isConflict = false;
        if (curationEntry != null) {
            // Counts the evidences in both directions
            Integer countPathogenicEvidences = 0;
            Integer countBenignEvidences = 0;
            for (EvidenceEntry evidenceEntry: this.impl.getEvidences()) {
                if (this.isEvidencePathogenic(evidenceEntry)) {
                    countPathogenicEvidences ++;
                }
                if (this.isEvidenceBenignity(evidenceEntry)) {
                    countBenignEvidences ++;
                }
            }
            // Sets the curation in conflict when at least one evidence in every direction
            // TODO: what happens when status is `resolved_conflict`, we are overriding right now
            if (countBenignEvidences > 0 && countPathogenicEvidences > 0) {
                Curation curation = curationEntry.getCuration();
                curation.setConsistencyStatus(ConsistencyStatus.conflict);
                curationEntry.setCuration(curation);
                this.setCurationEntry(curationEntry);
                isConflict = true;
            }
            else {
                // When consistency status is not set it it initialized to `consensus`
                Curation curation = curationEntry.getCuration();
                if (curation.getConsistencyStatus() == null) {
                    curation.setConsistencyStatus(ConsistencyStatus.consensus);
                    curationEntry.setCuration(curation);
                    this.setCurationEntry(curationEntry);
                }
            }
        }
        return isConflict;
    }

    /**
     * Returns true if the evidence indicates pathogenicity
     * @param evidenceEntry     the evidence
     * @return                  true if the evidence indicates pathogenicity
     */
    private Boolean isEvidencePathogenic(EvidenceEntry evidenceEntry) {

        return evidenceEntry.getPathogenicity() != null;
    }

    /**
     * Returns true if the evidence indicates benignity
     * @param evidenceEntry     the evidence
     * @return                  true if the evidence indicates benignity
     */
    private Boolean isEvidenceBenignity(EvidenceEntry evidenceEntry) {
        return evidenceEntry.getBenignity() != null;
    }

    /**
     * Annotates this variant with CellBase
     * @throws VariantAnnotatorException            error in variant annotation...
     * @throws IllegalCvaConfigurationException     wrong CVA settings
     */
    private void annotateVariant() throws VariantAnnotatorException, IllegalCvaConfigurationException {

        List<VariantAnnotation> variantAnnotations = null;
        try {
            variantAnnotations = KnownVariantWrapper.cellBaseDirectVariantAnnotator.annotate(
                    Collections.singletonList(this.getVariant()));
        }
        catch (Exception e) {
            //TODO: manage these errors, write a file with conflicting variants???
        }
        VariantAnnotation variantAnnotation = null;
        if (variantAnnotations != null && variantAnnotations.size() > 0) {
            variantAnnotation = variantAnnotations.get(0);
        }
        this.impl.getVariant().setAnnotation(variantAnnotation);
        // Sets list of overlapping transcripts
        this.transcripts = new ArrayList<>();
        for (ConsequenceType consequenceType: variantAnnotation.getConsequenceTypes()) {
            transcripts.add(consequenceType.getEnsemblTranscriptId());
        }
    }

    /**
     * Maps between a curation classification and SO terms
     * @param curationClassification    the curation classification
     * @return                          the associated SO terms
     */
    private CurationSOClassification getSOClassificationFromClassification(
            CurationClassification curationClassification) {

        CurationSOClassification curationSOClassification = null;
        switch (curationClassification) {
            case benign_variant:
            case likely_benign_variant:
                curationSOClassification = CurationSOClassification.benign_variant;
                break;
            case pathogenic_variant:
            case likely_pathogenic_variant:
                curationSOClassification = CurationSOClassification.disease_causing_variant;
                break;
            case established_risk_allele:
            case likely_risk_allele:
            case uncertain_risk_allele:
                curationSOClassification = CurationSOClassification.disease_associated_variant;
                break;
        }
        //TODO: map remaining classification categories
        //TODO: support several SO terms
        return curationSOClassification;
    }
}

