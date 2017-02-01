package org.gel.cva.storage.core.config;

/**
 * Created by priesgo on 19/01/17.
 */
public class ServerConfiguration {

    private RestServerConfiguration rest;


    /////////////////////////////////////////////////////////////
    //  Getters and setters                                   ///
    /////////////////////////////////////////////////////////////

    public RestServerConfiguration getRest() {
        return rest;
    }

    public void setRest(RestServerConfiguration rest) {
        this.rest = rest;
    }
}
