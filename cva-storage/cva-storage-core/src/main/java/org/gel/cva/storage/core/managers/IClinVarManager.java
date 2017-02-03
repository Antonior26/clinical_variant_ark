package org.gel.cva.storage.core.managers;

import org.gel.cva.storage.core.exceptions.ClinVarManagerException;
import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.VcfManagerException;

import java.util.List;

/**
 * Created by priesgo on 03/02/17.
 */
public interface IClinVarManager {

    /**
     * Gets a list of available ClinVar versions.
     * @return
     */
    List<String> getVersions();

    /**
     * Gets latest ClinVar version
     * @return
     */
    String getLatestVersion() throws ClinVarManagerException;

    /**
     * Stores the evidences associated to variants in the provided ClinVar version
     */
    void addEvidencesFromLatest() throws ClinVarManagerException, VcfManagerException, CvaException;

    /**
     * Stores the evidences associated to variants in the provided ClinVar version
     * @param version   the ClinVar version
     */
    void addEvidencesFromVersion(String version);
}
