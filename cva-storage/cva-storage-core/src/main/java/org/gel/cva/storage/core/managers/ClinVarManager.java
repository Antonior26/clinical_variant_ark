package org.gel.cva.storage.core.managers;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.ClinVarManagerException;
import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.exceptions.VcfManagerException;
import org.gel.cva.storage.core.knownvariant.adaptors.KnownVariantDBAdaptor;
import org.gel.models.cva.avro.AlleleOrigin;
import org.gel.models.cva.avro.EvidencePathogenicity;
import org.gel.models.cva.avro.SourceType;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
            this.knownVariantManager.addEvidence(
                    variant.getChromosome(),
                    variant.getStart(),
                    variant.getReference(),
                    variant.getAlternate(),
                    localVcf.getName(),
                    "ClinVar",
                    SourceType.database,
                    localVcf.getName(),
                    "",
                    "",
                    AlleleOrigin.germline,
                    Collections.emptyList(),
                    null,
                    EvidencePathogenicity.moderate,
                    null,
                    "",
                    "",
                    0,
                    null,
                    ""
            );
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
}
