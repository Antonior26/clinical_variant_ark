package org.gel.cva.storage.core.knownvariant.wrappers;

import org.gel.cva.storage.core.exceptions.IllegalCvaArgumentException;
import org.gel.models.cva.avro.*;
import org.gel.models.report.avro.EthnicCategory;
import org.gel.models.report.avro.ReportedModeOfInheritance;

import java.util.List;

/**
 * Created by priesgo on 29/01/17.
 */
public interface IKnownVariantWrapper {

    void addCuration(
            String curator,
            String phenotype,
            ReportedModeOfInheritance modeOfInheritance,
            CurationClassification curationClassification,
            ManualCurationConfidence manualCurationConfidence,
            ConsistencyStatus consistencyStatus,
            Float penetrance,
            Boolean variableExpressivity)
            throws IllegalCvaArgumentException;

    void addEvidence(
            String submitter,
            String sourceName,
            SourceType sourceType,
            String sourceVersion,
            String sourceUrl,
            String sourceId,
            AlleleOrigin alleleOrigin,
            List<HeritablePhenotype> heritablePhenotypes,
            EvidencePathogenicity evidencePathogenicity,
            EvidenceBenignity evidenceBenignity,
            String pubmedId,
            String study,
            Integer numberOfIndividuals,
            EthnicCategory ethnicCategory,
            String description)
            throws IllegalCvaArgumentException;

    List<CurationEntry> getCurationEntryByHeritablePhenotype(
            String phenotype,
            ReportedModeOfInheritance modeOfInheritance)
            throws IllegalCvaArgumentException;

    List<CurationEntry> getCurationEntryByHeritablePhenotypes(
            String phenotype,
            List<ReportedModeOfInheritance> modeOfInheritances)
            throws IllegalCvaArgumentException;

    List<EvidenceEntry> getEvidenceEntryByHeritablePhenotype(
            String phenotype,
            ReportedModeOfInheritance modeOfInheritance)
            throws IllegalCvaArgumentException;

    List<EvidenceEntry> getEvidenceEntryByHeritablePhenotypes(
            String phenotype,
            List<ReportedModeOfInheritance> modeOfInheritances)
            throws IllegalCvaArgumentException;
}
