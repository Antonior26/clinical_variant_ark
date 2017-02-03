package org.gel.cva.storage.core.config;

/**
 * Created by priesgo on 19/01/17.
 */
public class OrganismConfiguration {

    private String taxonomyCode;
    private String scientificName;
    private String commonName;
    private String assembly;

    /////////////////////////////////////////////////////////////
    //  Getters and setters                                   ///
    /////////////////////////////////////////////////////////////

    public String getTaxonomyCode() {
        return taxonomyCode;
    }

    public void setTaxonomyCode(String taxonomyCode) {
        this.taxonomyCode = taxonomyCode;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getAssembly() {
        return assembly;
    }

    public void setAssembly(String assembly) {
        this.assembly = assembly;
    }
}
