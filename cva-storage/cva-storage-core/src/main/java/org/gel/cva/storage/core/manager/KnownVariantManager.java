package org.gel.cva.storage.core.manager;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaArgumentException;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.knownvariant.adaptors.KnownVariantDBAdaptor;
import org.gel.cva.storage.core.knownvariant.wrappers.KnownVariantWrapper;
import org.gel.models.cva.avro.*;
import org.gel.models.report.avro.EthnicCategory;
import org.gel.models.report.avro.ReportedModeOfInheritance;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.opencga.core.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by priesgo on 19/01/17.
 */
public class KnownVariantManager extends CvaManager implements IKnownVariantManager {

    private KnownVariantDBAdaptor knownVariantDBAdaptor;

    public KnownVariantManager(CvaConfiguration cvaConfiguration)
            throws IllegalCvaConfigurationException, IllegalOpenCGACredentialsException,
            ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            InvocationTargetException {
        super(cvaConfiguration);
        String adaptorImplClass = cvaConfiguration.getStorageEngines().get(0).getOptions().get("adaptor.knownvariants");
        Class<?> clazz = Class.forName(adaptorImplClass);
        Constructor<?> ctor = clazz.getConstructor(CvaConfiguration.class);
        this.knownVariantDBAdaptor = (KnownVariantDBAdaptor)ctor.newInstance(new Object[] { cvaConfiguration });
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
    public String createKnownVariant(
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
        return null;
    }

    /**
     * Retrieves the known variant corresponding to knownVariantId, adds the requested curation and updates the known
     * variant. Returns true if the update was correct.
     * If the knownVariantId does not exist it returns false.
     * @param knownVariantId
     * @param curator
     * @param phenotype
     * @param modeOfInheritance
     * @param transcript
     * @param curationClassification
     * @param manualCurationConfidence
     * @param consistencyStatus
     * @param penetrance
     * @param variableExpressivity
     * @return
     * @throws IllegalCvaArgumentException
     */
    public Boolean addCuration(
            String knownVariantId,
            String curator,
            String phenotype,
            ReportedModeOfInheritance modeOfInheritance,
            String transcript,
            CurationClassification curationClassification,
            ManualCurationConfidence manualCurationConfidence,
            ConsistencyStatus consistencyStatus,
            Float penetrance,
            Boolean variableExpressivity) {
        throw new NotImplementedException();
    }

    /**
     * Retrieves the known variant corresponding to knownVariantId, adds the requested evidence and updates the known
     * variant. Returns true if the update was correct.
     * If the knownVariantId does not exist it returns false.
     * @param knownVariantId
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
    public Boolean addEvidence(
            String knownVariantId,
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
            String description)
            throws IllegalCvaArgumentException {
        throw new NotImplementedException();
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
        throw new NotImplementedException();
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
    ) throws IllegalCvaArgumentException {
        throw new NotImplementedException();
    }
}
