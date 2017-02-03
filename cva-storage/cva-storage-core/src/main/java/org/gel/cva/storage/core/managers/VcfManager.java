package org.gel.cva.storage.core.managers;

import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;

import java.io.*;
import java.net.URL;

/**
 * Created by priesgo on 03/02/17.
 */
public class VcfManager extends AbstractVcfManager implements IVcfManager {

    protected IKnownVariantManager knownVariantManager;
    protected CvaConfiguration cvaConfiguration;

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

        processVcf(localVcf);
        // TODO: create a QueryResult to be returned
        return null;
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
            this.knownVariantManager.createKnownVariant(
                    localVcf.getName(),     // TODO: add as a submitter the interface user???
                    variant.getChromosome(),
                    variant.getStart(),
                    variant.getReference(),
                    variant.getAlternate()
            );
        }
        catch (VariantAnnotatorException e) {
            throw new CvaException(e.getMessage());
        }
    }
}
