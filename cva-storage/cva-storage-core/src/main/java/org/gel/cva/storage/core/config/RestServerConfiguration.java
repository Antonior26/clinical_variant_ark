package org.gel.cva.storage.core.config;

/**
 * Created by priesgo on 19/01/17.
 */
public class RestServerConfiguration {

    private Integer port;
    private String logFile;
    private Integer defaultLimit;
    private Integer maxLimit;

    /////////////////////////////////////////////////////////////
    //  Getters and setters                                   ///
    /////////////////////////////////////////////////////////////
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public Integer getDefaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(Integer defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    public Integer getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(Integer maxLimit) {
        this.maxLimit = maxLimit;
    }
}
