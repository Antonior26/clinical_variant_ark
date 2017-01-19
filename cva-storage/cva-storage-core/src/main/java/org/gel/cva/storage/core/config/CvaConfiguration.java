package org.gel.cva.storage.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.opencb.opencga.core.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.config.CellBaseConfiguration;
import org.opencb.opencga.storage.core.config.DatabaseCredentials;
import org.opencb.opencga.storage.mongodb.auth.MongoCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by priesgo on 18/01/17.
 */
public class CvaConfiguration {

    private String defaultStorageEngineId;
    private String logLevel;
    private String logFile;
    private CellBaseConfiguration cellbase;
    private List<StorageEngineConfiguration> storageEngines;
    private OrganismConfiguration organism;


    public static CvaConfiguration load(InputStream configurationInputStream)
            throws IOException, IllegalCvaConfigurationException {
        return load(configurationInputStream, "yaml");
    }

    public static CvaConfiguration load(InputStream configurationInputStream, String format)
            throws IOException, IllegalCvaConfigurationException {
        CvaConfiguration cvaConfiguration;
        ObjectMapper objectMapper;
        switch (format) {
            case "json":
                objectMapper = new ObjectMapper();
                cvaConfiguration = objectMapper.readValue(configurationInputStream, CvaConfiguration.class);
                break;
            case "yml":
            case "yaml":
            default:
                objectMapper = new ObjectMapper(new YAMLFactory());
                cvaConfiguration = objectMapper.readValue(configurationInputStream, CvaConfiguration.class);
                break;
        }
        cvaConfiguration.sanityChecks();
        return cvaConfiguration;
    }

    private void sanityChecks () throws IllegalCvaConfigurationException{
        // Checks that MongoDB storage is configured
        if (! this.getDefaultStorageEngineId().equals("mongodb")) {
            throw new IllegalCvaConfigurationException("CVA ClinVar loader only supports mongoDB as storage");
        }
        DatabaseCredentials databaseCredentials = null;
        for (StorageEngineConfiguration storageEngineConfiguration : this.getStorageEngines()) {
            if (storageEngineConfiguration.getId().equals(this.getDefaultStorageEngineId())) {
                databaseCredentials = storageEngineConfiguration.getDatabase();
                break;
            }
        }
        if (databaseCredentials == null || databaseCredentials.getHosts().size() < 1) {
            throw new IllegalCvaConfigurationException("CVA ClinVar loader requires mongoDB as storage");
        }
        //TODO: check that host follows pattern host:port
        //TODO: check that cellbase is configured properly
    }

    public String getDefaultStorageEngineId() {
        return defaultStorageEngineId;
    }

    public void setDefaultStorageEngineId(String defaultStorageEngineId) {
        this.defaultStorageEngineId = defaultStorageEngineId;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public CellBaseConfiguration getCellbase() {
        return cellbase;
    }

    public void setCellbase(CellBaseConfiguration cellbase) {
        this.cellbase = cellbase;
    }

    public List<StorageEngineConfiguration> getStorageEngines() {
        return storageEngines;
    }

    public void setStorageEngines(List<StorageEngineConfiguration> storageEngines) {
        this.storageEngines = storageEngines;
    }

    public OrganismConfiguration getOrganism() {
        return organism;
    }

    public void setOrganism(OrganismConfiguration organism) {
        this.organism = organism;
    }

    public static void main(String [] args) throws IOException, IllegalCvaConfigurationException {
        InputStream configStream = CvaConfiguration.class.getResourceAsStream("/cva.yml");
        CvaConfiguration cvaConfiguration = CvaConfiguration.load(configStream);
        int i = 0;
    }

    public DatabaseCredentials getDefaultDatabaseCredentials() {
        DatabaseCredentials databaseCredentials = null;
        for (StorageEngineConfiguration storageEngineConfiguration : this.getStorageEngines()) {
            if (storageEngineConfiguration.getId().equals(this.getDefaultStorageEngineId())) {
                databaseCredentials = storageEngineConfiguration.getDatabase();
                break;
            }
        }
        // this will never be null as we have ran before the sanity checks
        return databaseCredentials;
    }

    public MongoCredentials getMongoCredentials() throws IllegalOpenCGACredentialsException {
        DatabaseCredentials databaseCredentials = this.getDefaultDatabaseCredentials();
        //TODO: make this somehow more secure to misconfiguration
        String host = databaseCredentials.getHosts().get(0).split(":")[0];
        String port = databaseCredentials.getHosts().get(0).split(":")[1];
        MongoCredentials mongoCredentials = new MongoCredentials(
            host,
            Integer.parseInt(port),
            databaseCredentials.getOptions().get("database.name"),
            databaseCredentials.getUser(),
            databaseCredentials.getPassword(),
            false
        );
        return mongoCredentials;
    }
}
