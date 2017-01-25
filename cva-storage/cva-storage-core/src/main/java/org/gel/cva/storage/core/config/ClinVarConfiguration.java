package org.gel.cva.storage.core.config;

/**
 * Created by priesgo on 19/01/17.
 */
public class ClinVarConfiguration {

    private String ftpServer;
    private String user;
    private String password;
    private String assemblyFolder;


    /////////////////////////////////////////////////////////////
    //  Getters and setters                                   ///
    /////////////////////////////////////////////////////////////

    public String getFtpServer() {
        return ftpServer;
    }

    public void setFtpServer(String ftpServer) {
        this.ftpServer = ftpServer;
    }

    public String getAssemblyFolder() {
        return assemblyFolder;
    }

    public void setAssemblyFolder(String assemblyFolder) {
        this.assemblyFolder = assemblyFolder;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
