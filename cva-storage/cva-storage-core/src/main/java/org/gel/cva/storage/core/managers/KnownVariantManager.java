package org.gel.cva.storage.core.managers;

import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaArgumentException;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.knownvariant.adaptors.KnownVariantDBAdaptor;
import org.gel.cva.storage.core.knownvariant.wrappers.KnownVariantWrapper;
import org.gel.models.cva.avro.*;
import org.gel.models.report.avro.EthnicCategory;
import org.gel.models.report.avro.ReportedModeOfInheritance;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Created by priesgo on 19/01/17.
 */
public class KnownVariantManager extends CvaManager implements IKnownVariantManager {

    private KnownVariantDBAdaptor knownVariantDBAdaptor;

    public KnownVariantManager(CvaConfiguration cvaConfiguration)
            throws IllegalCvaConfigurationException {
        super(cvaConfiguration);
        String adaptorImplClass = cvaConfiguration.getStorageEngines().get(0).getOptions().get("adaptor.knownvariants");
        try {
            Class<?> clazz = Class.forName(adaptorImplClass);
            Constructor<?> ctor = clazz.getConstructor(CvaConfiguration.class);
            this.knownVariantDBAdaptor = (KnownVariantDBAdaptor) ctor.newInstance(new Object[]{cvaConfiguration});
        }
        catch (ReflectiveOperationException ex) {
            throw new IllegalCvaConfigurationException("Error setting KnownVariantDBAdaptor implementation: " +
                    ex.getMessage());
        }
    }

    /**
     * Returns the number of documents in the KnownVariants collection
     * @return
     */
    @Override
    public Long count() {
        return this.knownVariantDBAdaptor.count();
    }

    /**
     * Registers a known variant in CVA, if the variant already exists it does nothing.
     * Always returns the id of the known variant.
     * @param submitter     the submitter of the variant
     * @param chromosome    the chromosome
     * @param position      the position
     * @param reference     the reference bases
     * @param alternate     the alternate bases
     * @return
     */
    @Override
    public KnownVariantWrapper createKnownVariant(
            String submitter,
            String chromosome,
            Integer position,
            String reference,
            String alternate) throws VariantAnnotatorException, CvaException {

        // Creates the variant, normalize it and annotate
        KnownVariantWrapper knownVariantWrapper =
                new KnownVariantWrapper(submitter, chromosome, position, reference, alternate);
        // Inserts the variants in mongoDB
        this.knownVariantDBAdaptor.insert(knownVariantWrapper, null);
        return knownVariantWrapper;
    }

    /**
     * Retrieves a known variant from CVA.
     * Returns null if the variant does not exist.
     * @param chromosome    the chromosome
     * @param position      the position
     * @param reference     the reference bases
     * @param alternate     the alternate bases
     * @return
     */
    @Override
    public KnownVariantWrapper findKnownVariant(
            String chromosome,
            Integer position,
            String reference,
            String alternate) throws CvaException {

        // Search for the variant in mongoDB
        KnownVariantWrapper knownVariantWrapper = this.knownVariantDBAdaptor.find(chromosome, position, reference, alternate);
        return knownVariantWrapper;
    }

    /**
     * Adds a curation to an existing variant.
     * If the variant does not exist it throws an exception.
     * @param chromosome                    the chromosome
     * @param position                      the position
     * @param reference                     the reference base/s
     * @param alternate                     the alternate base/s
     * @param curator                       the curator
     * @param phenotype                     the phenotype
     * @param modeOfInheritance             the mode of inheritance
     * @param transcript                    the transcript
     * @param curationClassification        the cuartion classification
     * @param manualCurationConfidence      the manual curation classification
     * @param consistencyStatus             the consistency status
     * @param penetrance                    the penetrance
     * @param variableExpressivity          the variable expressivity
     * @return                              the updated KnownVariant
     * @throws CvaException                 when the variant does not exist
     */
    @Override
    public KnownVariantWrapper addCuration(
            String chromosome, Integer position, String reference, String alternate,
            String curator,
            String phenotype,
            ReportedModeOfInheritance modeOfInheritance,
            String transcript,
            CurationClassification curationClassification,
            ManualCurationConfidence manualCurationConfidence,
            ConsistencyStatus consistencyStatus,
            Float penetrance,
            Boolean variableExpressivity) throws CvaException {

        KnownVariantWrapper knownVariantWrapper =
                this.knownVariantDBAdaptor.find(chromosome, position, reference, alternate);
        if (knownVariantWrapper == null) {
            throw new CvaException("Cannot add a curation to a non registered variant");
        }
        knownVariantWrapper.addCuration(curator, phenotype, modeOfInheritance, transcript,
                curationClassification, manualCurationConfidence, consistencyStatus, penetrance, variableExpressivity);
        Boolean isUpdateCorrect = this.knownVariantDBAdaptor.update(knownVariantWrapper);
        if (! isUpdateCorrect) {
            throw new CvaException("Adding a curation failed");
        }
        return knownVariantWrapper;
    }

    /**
     *
     * @param chromosome
     * @param position
     * @param reference
     * @param alternate
     * @param submitter
     * @param sourceName
     * @param sourceType
     * @param sourceVersion
     * @param sourceUrl
     * @param sourceId
     * @param alleleOrigin
     * @param heritablePhenotypes
     * @param transcript
     * @param evidencePathogenicity
     * @param evidenceBenignity
     * @param pubmedId
     * @param study
     * @param numberOfIndividuals
     * @param ethnicCategory
     * @param description
     * @return
     * @throws IllegalCvaArgumentException
     */
    @Override
    public KnownVariantWrapper addEvidence(
            String chromosome, Integer position, String reference, String alternate,
            String submitter,
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
    ) throws CvaException {

        KnownVariantWrapper knownVariantWrapper =
                this.knownVariantDBAdaptor.find(chromosome, position, reference, alternate);
        if (knownVariantWrapper == null) {
            throw new CvaException("Cannot add an evidence to a non registered variant");
        }
        knownVariantWrapper.addEvidence(submitter, sourceName, sourceType, sourceVersion, sourceUrl, sourceId,
                alleleOrigin, heritablePhenotypes, transcript, evidencePathogenicity, evidenceBenignity,
                pubmedId, study, numberOfIndividuals, ethnicCategory, description);
        Boolean isUpdateCorrect = this.knownVariantDBAdaptor.update(knownVariantWrapper);
        if (! isUpdateCorrect) {
            throw new CvaException("Adding an evidence failed");
        }
        return knownVariantWrapper;
    }
}
