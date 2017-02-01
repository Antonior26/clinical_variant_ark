package org.gel.cva.storage.core.manager;

import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaArgumentException;
import org.gel.cva.storage.core.knownvariant.wrappers.KnownVariantWrapper;
import org.gel.models.cva.avro.*;
import org.gel.models.report.avro.EthnicCategory;
import org.gel.models.report.avro.ReportedModeOfInheritance;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;

import java.util.List;

/**
 * Created by priesgo on 31/01/17.
 */
public interface IKnownVariantManager {

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
    KnownVariantWrapper createKnownVariant(
            String submitter,
            String chromosome,
            Integer position,
            String reference,
            String alternate
    ) throws VariantAnnotatorException, CvaException;

    /**
     * Search for a variant in CVA by using the basic variant coordinates.
     * @param chromosome
     * @param position
     * @param reference
     * @param alternate
     * @return
     * @throws CvaException
     */
    KnownVariantWrapper findKnownVariant(
            String chromosome,
            Integer position,
            String reference,
            String alternate) throws CvaException;

    /**
     * Adds a curation to an existing variant. Returns true if the update was correct.
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
    KnownVariantWrapper addCuration(
            String chromosome, Integer position, String reference, String alternate,
            String curator,
            String phenotype,
            ReportedModeOfInheritance modeOfInheritance,
            String transcript,
            CurationClassification curationClassification,
            ManualCurationConfidence manualCurationConfidence,
            ConsistencyStatus consistencyStatus,
            Float penetrance,
            Boolean variableExpressivity) throws CvaException;

    /**
     * Adds an evidence to an existing variant. Returns true if the update was correct.
     * If the variant does not exist it throws an exception.
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
    KnownVariantWrapper addEvidence(
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
    ) throws CvaException;
}
