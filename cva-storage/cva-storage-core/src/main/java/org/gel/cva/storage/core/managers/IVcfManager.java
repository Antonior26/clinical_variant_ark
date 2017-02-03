package org.gel.cva.storage.core.managers;

import org.gel.cva.storage.core.exceptions.CvaException;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;

import java.io.File;
import java.net.URL;

/**
 * Created by priesgo on 03/02/17.
 */
public interface IVcfManager {

    /**
     * Downloads a VCF from a remote URL and loads every variant contained in it into CVA.
     * Sample information from the VCF is discarded.
     * @param remoteVcf     the VCF URL
     * @return              the result of the loading process
     */
    QueryResult loadVcf(URL remoteVcf) throws VariantAnnotatorException, CvaException;

    /**
     * Loads every variant contained in the VCF into CVA.
     * Sample information from the VCF is discarded.
     * @param localVcf      the VCF file
     * @return              the result of the loading process
     */
    QueryResult loadVcf(File localVcf) throws VariantAnnotatorException, CvaException;

}
