package org.gel.cva.storage.core.managers;

import org.apache.commons.io.FileUtils;
import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.exceptions.VcfManagerException;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.biodata.models.variant.VariantStudy;
import org.opencb.biodata.tools.variant.VariantVcfHtsjdkReader;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

/**
 * Created by priesgo on 03/02/17.
 */
public class VcfManager extends CvaManager implements IVcfManager {

    private IKnownVariantManager knownVariantManager;
    private CvaConfiguration cvaConfiguration;
    private static final String SUBMITTER = "VCF-loader";

    public VcfManager(CvaConfiguration cvaConfiguration) throws IllegalCvaConfigurationException {
        super(cvaConfiguration);
        this.cvaConfiguration = cvaConfiguration;
        this.knownVariantManager = new KnownVariantManager(cvaConfiguration);
    }

    /**
     * Downloads a VCF from a remote URL and loads every variant contained in it into CVA.
     * Sample information from the VCF is discarded.
     * @param remoteVcf     the VCF URL
     * @return              the result of the loading process
     */
    @Override
    public QueryResult loadVcf(URL remoteVcf) throws VariantAnnotatorException, CvaException {
        File localVcf = this.downloadVcf(remoteVcf);
        return this.loadVcf(localVcf);
    }

    /**
     * Loads every variant contained in the VCF into CVA.
     * Sample information from the VCF is discarded.
     * @param localVcf      the VCF file
     * @return              the result of the loading process
     */
    @Override
    public QueryResult loadVcf(File localVcf)
            throws VariantAnnotatorException, CvaException {

        // Reads input VCF and registers each variant into CVA
        VariantVcfHtsjdkReader reader = this.getVcfReader(localVcf);
        reader.open();
        reader.pre();
        List<Variant> read;
        do {
            read = reader.read();
            for (Variant variant : read) {
                this.knownVariantManager.createKnownVariant(
                        localVcf.getName(),     // TODO: add as a submitter the interface user???
                        variant.getChromosome(),
                        variant.getStart(),
                        variant.getReference(),
                        variant.getAlternate()
                );
            }
        } while (!read.isEmpty());
        reader.close();
        return null;
    }

    /**
     * Downloads a remote file into the CVA's temp folder
     * @param remoteVcf     the remote VCF URL
     * @return              the downloaded file
     * @throws IOException
     */
    private File downloadVcf(URL remoteVcf) throws VcfManagerException {
        File localVcf = null;
        try {
            localVcf = new File(this.cvaConfiguration.getTempFolder(), remoteVcf.getFile());
            FileUtils.copyURLToFile(remoteVcf, localVcf);
        } catch (IOException ex) {
            throw new VcfManagerException("Error downloading VCF: " + ex.getMessage());
        }
        return localVcf;
    }

    /**
     * Creates a VCF reader
     * @param localVcf      the input VCF
     * @return              the VCF reader
     * @throws VcfManagerException
     */
    private VariantVcfHtsjdkReader getVcfReader(File localVcf) throws VcfManagerException {

        // Reads input VCF
        InputStream inputStream;
        try {
            try {
                inputStream = new GZIPInputStream(new FileInputStream(localVcf));
            } catch (ZipException ex) {
                inputStream = new FileInputStream(localVcf);
            } catch (IOException ex) {
                throw new VcfManagerException(ex.getMessage());
            }
        } catch (FileNotFoundException ex) {
            throw new VcfManagerException(ex.getMessage());
        }
        VariantSource source = new VariantSource(
                localVcf.getName(),
                "1",
                "1",
                "fake",
                VariantStudy.StudyType.FAMILY,
                VariantSource.Aggregation.NONE);
        VariantVcfHtsjdkReader reader = new VariantVcfHtsjdkReader(
                inputStream,
                source);
        return reader;
    }
}
