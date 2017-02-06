package org.gel.cva.storage.core.managers;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.ClinVarManagerException;
import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.exceptions.VcfManagerException;
import org.gel.cva.storage.core.knownvariant.adaptors.KnownVariantDBAdaptor;
import org.gel.models.cva.avro.*;
import org.gel.models.report.avro.ReportedModeOfInheritance;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.opencga.core.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * Created by priesgo on 19/01/17.
 */
public class ClinVarManager extends VcfManager implements IClinVarManager {

    private FTPClient ftpClient;

    public ClinVarManager(CvaConfiguration cvaConfiguration)
            throws ClinVarManagerException, IllegalCvaConfigurationException {

        super(cvaConfiguration);
        this.ftpClient = this.connectToClinVarFTP();
    }

    @Override
    public List<String> getVersions() {

        throw new NotImplementedException();
    }

    @Override
    public String getLatestVersion() throws ClinVarManagerException {

        URL latestVersionURL = this.getLatestURL();
        return latestVersionURL.getFile();
    }

    @Override
    public void addEvidencesFromLatest() throws CvaException {

        // Downloads the VCF
        File localVcf = this.downloadVcf(this.getLatestURL());
        this.processVcf(localVcf);
    }

    @Override
    public void addEvidencesFromVersion(String version) {

        throw new NotImplementedException();
    }

    private final String CLNSIG = "CLNSIG";
    private final String CLNDSDB = "CLNDSDB";
    private final String CLNDSDBID = "CLNDSDBID";
    private final String CLNDBN = "CLNDBN";
    private final String CLNREVSTAT = "CLNREVSTAT";

    /**
     * Registers every variant in CVA
     * @param localVcf
     * @param variant
     * @throws CvaException
     */
    @Override
    protected void processVariant(
            File localVcf,
            Variant variant) throws CvaException {
        
        try {
            Map annotations = variant.getStudies().get(0).getFiles().get(0).getAttributes();
            List<String> significances = Arrays.asList(((String) annotations.get(this.CLNSIG)).split("|"));
            List<String> dbNames = Arrays.asList(((String) annotations.get(this.CLNDSDB)).split("|"));
            List<String> dbIds = Arrays.asList(((String) annotations.get(this.CLNDSDBID)).split("|"));
            List<String> phenotypes = Arrays.asList(((String) annotations.get(this.CLNDBN)).split("|"));
            List<String> revisionStatuss = Arrays.asList(((String) annotations.get(this.CLNREVSTAT)).split("|"));
            //List<String> accessions = Arrays.asList(((String) annotations.get("CLNACC")).split("|"));
            for (int i = 0; i < significances.size();  i++) {
                String significance = significances.get(i);
                CurationClassification curationClassification = this.getCurationClassificationFromClinicalsignificance(significance);
                RevisionStatus revisionStatus = RevisionStatus.valueOf(revisionStatuss.get(i));
                EvidenceBenignity evidenceBenignity = null;
                EvidencePathogenicity evidencePathogenicity = null;
                HeritablePhenotype heritablePhenotype = new HeritablePhenotype(phenotypes.get(i),
                        ReportedModeOfInheritance.NA);
                List<HeritablePhenotype> heritablePhenotypes = new LinkedList<>();
                heritablePhenotypes.add(heritablePhenotype);
                if (curationClassification == CurationClassification.likely_benign_variant ||
                        curationClassification == CurationClassification.benign_variant) {
                    // process the evidence of benignity
                    switch (revisionStatus) {
                        case no_assertion:
                        case no_criteria:
                        case single:
                        case conf:
                            evidenceBenignity = EvidenceBenignity.supporting;
                            break;
                        case mult:
                            evidenceBenignity = EvidenceBenignity.stand_alone;
                            break;
                        case exp:
                        case guideline:
                            evidenceBenignity = EvidenceBenignity.strong;
                            break;
                    }
                }
                else if (curationClassification == CurationClassification.pathogenic_variant ||
                        curationClassification == CurationClassification.likely_pathogenic_variant) {
                    // process the evidence of pathogenicity
                    switch (revisionStatus) {
                        case no_assertion:
                        case no_criteria:
                        case single:
                        case conf:
                            evidencePathogenicity = EvidencePathogenicity.supporting;
                            break;
                        case mult:
                            evidencePathogenicity = EvidencePathogenicity.moderate;
                            break;
                        case exp:
                            evidencePathogenicity = EvidencePathogenicity.strong;
                            break;
                        case guideline:
                            evidencePathogenicity = EvidencePathogenicity.very_strong;
                            break;
                    }
                }
                // creates the evidence
                if (evidenceBenignity != null || evidencePathogenicity != null) {
                    this.knownVariantManager.addEvidence(
                            variant.getChromosome(),
                            variant.getStart(),
                            variant.getReference(),
                            variant.getAlternate(),
                            localVcf.getName(),
                            dbNames.get(i),
                            SourceType.database,
                            null,
                            "",
                            dbIds.get(i),
                            AlleleOrigin.germline,
                            heritablePhenotypes,
                            null,
                            evidencePathogenicity,
                            evidenceBenignity,
                            "",
                            "",
                            0,
                            null,
                            "Evidence extracted automatically from ClinVar: " + localVcf.getName()
                    );
                }
            }
        }
        catch (CvaException e) {
            // TODO: variant not registerd, do something with this
        }
    }

    /**
     * Connects to ClinVar FTP
     * @return
     * @throws ClinVarManagerException
     */
    private FTPClient connectToClinVarFTP () throws ClinVarManagerException{
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(cvaConfiguration.getClinVar().getFtpServer(), FTPClient.DEFAULT_PORT);
            ftpClient.login(cvaConfiguration.getClinVar().getUser(), cvaConfiguration.getClinVar().getPassword());
        }
        catch (IOException e) {
            throw new ClinVarManagerException(e.getMessage());
        }
        return ftpClient;
    }

    /**
     * Gets the path and name of the latest ClinVar VCF file in the ClinVar's FTP
     * @return
     * @throws ClinVarManagerException
     */
    private URL getLatestURL() throws ClinVarManagerException {

        String clinVarFullPath =
                String.format(cvaConfiguration.getClinVar().getAssemblyFolder(),
                        cvaConfiguration.getOrganism().getAssembly());
        FTPFile[] ftpFiles = new FTPFile[0];
        try {
            ftpFiles = this.ftpClient.listFiles(clinVarFullPath);
        } catch (IOException e) {
            throw new ClinVarManagerException(e.getMessage());
        }
        String fileName = null;
        for (FTPFile ftpFile: ftpFiles) {
             if (ftpFile.getName().matches("^clinvar_\\d{8}.vcf.gz$")) {
                 fileName = ftpFile.getName();
                 break;
             }
        }
        if (fileName == null) {
            throw new ClinVarManagerException("No ClinVar file matching expected pattern");
        }
        URL latestURL;
        try {
            latestURL = new URL("ftp://" + cvaConfiguration.getClinVar().getFtpServer() + "/" +
                    clinVarFullPath + "/" + fileName);
        } catch (MalformedURLException e) {
            throw new ClinVarManagerException(e.getMessage());
        }
        return latestURL;
    }

    /**
     * Transforms clinical significance reported in ClinVar to CVA model.
     * 0 - Uncertain significance,
     * 1 - not provided,
     * 2 - Benign,
     * 3 - Likely benign,
     * 4 - Likely pathogenic,
     * 5 - Pathogenic,
     * 6 - drug response,
     * 7 - histocompatibility,
     * 255 - other
     * @param clinicalSignificance  ClinVar's clinical significance
     * @return                      CVA's curation classification
     */
    public CurationClassification getCurationClassificationFromClinicalsignificance(String clinicalSignificance)
    {
        CurationClassification curationClassification = null;
        switch (clinicalSignificance) {
            case "0":
            case "1":
            case "7":
            case "255":
                curationClassification = CurationClassification.uncertain_significance;
                break;
            case "2":
                curationClassification = CurationClassification.benign_variant;
                break;
            case "3":
                curationClassification = CurationClassification.likely_benign_variant;
                break;
            case "4":
                curationClassification = CurationClassification.likely_pathogenic_variant;
                break;
            case "5":
                curationClassification = CurationClassification.pathogenic_variant;
                break;
            case "6":
                curationClassification = CurationClassification.drug_response;
                break;
        }
        return curationClassification;
    }

    /**
     * ClinVar revision status in a Java enum
     */
    enum RevisionStatus {
        no_assertion,
        no_criteria,
        single,
        mult,
        conf,
        exp,
        guideline
    }


}
