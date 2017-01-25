package org.gel.cva.storage.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.exceptions.IllegalCvaCredentialsException;
import org.gel.cva.storage.core.helpers.CvaDateFormatter;
import org.opencb.opencga.core.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.config.CellBaseConfiguration;
import org.opencb.opencga.storage.core.config.DatabaseCredentials;
import org.opencb.opencga.storage.core.config.StorageConfiguration;
import org.opencb.opencga.storage.mongodb.auth.MongoCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by priesgo on 18/01/17.
 */
public class CvaConfiguration {

    private String defaultStorageEngineId;
    private String logLevel;
    private String logFile;
    private String tempFolder;
    private CellBaseConfiguration cellbase;
    private List<StorageEngineConfiguration> storageEngines;
    private OrganismConfiguration organism;
    private ClinVarConfiguration clinVar;

    private static CvaConfiguration instance = null;
    private static final String CONFIG_FILE = "/cva.yml";
    private static final String CONFIG_FORMAT = "yaml";

    // TODO: setup logging
    // TODO: print confguration overwriting toString()

    /**
     * Private default constructor
     */
    private CvaConfiguration() {

    }

    /**
     * Getter for the instance of this singleton
     * @return      the CvaConfiguration singleton
     * @throws IllegalCvaConfigurationException
     */
    public static CvaConfiguration getInstance() throws IllegalCvaConfigurationException {
        if (CvaConfiguration.instance == null) {
            InputStream configStream = CvaConfiguration.class.getResourceAsStream(CvaConfiguration.CONFIG_FILE);
            CvaConfiguration.load(configStream, CvaConfiguration.CONFIG_FORMAT);
        }
        return CvaConfiguration.instance;
    }

    /**
     * Loads a config file in memory and runs basic sanity checks
     * @param configurationInputStream  the config file
     * @param format                    the format (any of "json", "yml", "yaml")
     * @return                          the CvaConfiguration singleton
     * @throws IllegalCvaConfigurationException
     */
    public static CvaConfiguration load(InputStream configurationInputStream, String format)
            throws IllegalCvaConfigurationException {
        CvaConfiguration cvaConfiguration;
        ObjectMapper objectMapper;
        try {
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
        }
        catch (IOException ex) {
            throw new IllegalCvaConfigurationException("Something is very wrong in the CVA config file:" + ex.getMessage());
        }
        cvaConfiguration.sanityChecks();
        CvaConfiguration.instance = cvaConfiguration;
        return cvaConfiguration;
    }

    /**
     * Retrieves the default StorageEngineConfiguration as stated in "defaultStorageEngineId"
     * @return      Default StorageEngineConfiguration
     */
    public static StorageEngineConfiguration getDefaultStorageEngine() throws IllegalCvaConfigurationException {
        String defaultStorageEngineId = CvaConfiguration.getInstance().getDefaultStorageEngineId();
        StorageEngineConfiguration defaultStorageEngineConfiguration = CvaConfiguration.getInstance().getStorageEngines().stream()
                .filter((storageEngine) -> storageEngine.getId().equals(defaultStorageEngineId))
                .findFirst().get();
        return defaultStorageEngineConfiguration;
    }

    /**
     * Retrieves the Mongo credentials object for CVA storage
     * @return      Mongo credentials
     * @throws IllegalCvaConfigurationException
     * @throws IllegalCvaCredentialsException
     */
    // TODO: avoid using MongoCredentials in opencga.storage, use instead MongoDBConfiguration
    public static MongoCredentials getMongoCredentials() throws IllegalCvaConfigurationException, IllegalCvaCredentialsException {
        DatabaseCredentials databaseCredentials = CvaConfiguration.getDefaultStorageEngine().getDatabase();
        //TODO: make this somehow more secure to misconfiguration
        String host = databaseCredentials.getHosts().get(0).split(":")[0];
        String port = databaseCredentials.getHosts().get(0).split(":")[1];
        MongoCredentials mongoCredentials = null;
        // TODO: com.mongodb.MongoSocketOpenException is not controlled when server not reachable
        // TODO: com.mongodb.MongoSocketOpenException is not controlled when credentials are incorrect
        try {
            mongoCredentials = new MongoCredentials(
                    host,
                    Integer.parseInt(port),
                    CvaConfiguration.getDefaultStorageEngine().getOptions().get("database.name"),
                    databaseCredentials.getUser(),
                    databaseCredentials.getPassword(),
                    true
            );
        }
        catch (IllegalOpenCGACredentialsException ex) {
            throw new IllegalCvaCredentialsException(ex.getMessage());
        }
        return mongoCredentials;
    }

    /**
     * Returns the StorageConfiguration for CellBase
     * @return      Cellbase StorageConfiguration
     */
    public static StorageConfiguration getCellBaseStorageConfiguration() throws IllegalCvaConfigurationException {
        StorageConfiguration storageConfiguration = new StorageConfiguration();
        storageConfiguration.setCellbase(CvaConfiguration.getInstance().getCellbase());
        return storageConfiguration;
    }

    /**
     * Runs sanity checks on the CVA configuration
     * @throws IllegalCvaConfigurationException
     */
    private void sanityChecks () throws IllegalCvaConfigurationException{

        // Sanity checks on the CVA storage configuration
        StorageEngineConfiguration storageEngineConfiguration = null;
        try {
            String defaultStorageEngineId = this.getDefaultStorageEngineId();
            storageEngineConfiguration = this.getStorageEngines().stream()
                    .filter((storageEngine) -> storageEngine.getId().equals(defaultStorageEngineId))
                    .findFirst().get();
        }
        catch (NoSuchElementException ex) {
            throw new IllegalCvaConfigurationException("CVA requires the default storage to be configured: " + ex.getMessage());
        }
        if (storageEngineConfiguration == null ||
                storageEngineConfiguration.getDatabase() == null ||
                storageEngineConfiguration.getDatabase().getHosts().size() < 1) {
            throw new IllegalCvaConfigurationException("CVA requires the default storage to be configured");
        }
        for (String host: storageEngineConfiguration.getDatabase().getHosts()) {
            if (! host.matches("^\\S+:\\d+$")) {
                throw new IllegalCvaConfigurationException("CVA storage hosts require to be defined as \"host:port\"");
            }
        }

        //TODO: Sanity checks on the Cellbase configuration
        // Either webservices or database connection must be set
        // Should we allow not connecting Cellbase and not annotating mutations in that case??
    }

    /////////////////////////////////////////////////////////////
    //  Getters and setters                                   ///
    /////////////////////////////////////////////////////////////

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

    public ClinVarConfiguration getClinVar() {
        return clinVar;
    }

    public void setClinVar(ClinVarConfiguration clinVar) {
        this.clinVar = clinVar;
    }

    public String getTempFolder() {
        return tempFolder;
    }

    public void setTempFolder(String tempFolder) {
        this.tempFolder = tempFolder;
    }

}
