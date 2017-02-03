package org.gel.cva.storage.core.config;

import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.exceptions.IllegalCvaCredentialsException;
import org.junit.Before;
import org.junit.Test;
import org.opencb.opencga.storage.mongodb.auth.MongoCredentials;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by priesgo on 25/01/17.
 */
public class CvaConfigurationTest {

    @Before
    public void setUp() throws Exception {

    }

    private void notNullChecks(CvaConfiguration cvaConfiguration) {
        assertNotNull(cvaConfiguration);
        assertNotNull(cvaConfiguration.getDefaultStorageEngineId());
        assertNotNull(cvaConfiguration.getLogLevel());
        assertNotNull(cvaConfiguration.getLogFile());
        assertNotNull(cvaConfiguration.getTempFolder());
        assertNotNull(cvaConfiguration.getCellbase());
        assertNotNull(cvaConfiguration.getStorageEngines());
        assertNotNull(cvaConfiguration.getOrganism());
        assertNotNull(cvaConfiguration.getClinVar());
    }

    @Test
    public void testCvaConfiguration() {
        CvaConfiguration cvaConfiguration = null;
        try {
            cvaConfiguration = CvaConfiguration.getInstance();
        }
        catch (IllegalCvaConfigurationException ex) {
            assertTrue(false);
        }
        this.notNullChecks(cvaConfiguration);
    }

    @Test
    public void testCvaConfigurationOK() {
        InputStream configStream = CvaConfigurationTest.class.getResourceAsStream("/config/cva.ok.yml");
        CvaConfiguration cvaConfiguration = null;
        try {
            cvaConfiguration = CvaConfiguration.load(configStream, "yaml");
        }
        catch (IllegalCvaConfigurationException ex) {
            assertTrue(false);
        }
        this.notNullChecks(cvaConfiguration);
    }

    @Test
    public void testMongoCredentials() throws IllegalCvaCredentialsException, IllegalCvaConfigurationException {
        MongoCredentials mongoCredentials = CvaConfiguration.getMongoCredentials();
        assertNotNull(mongoCredentials);
    }

    @Test(expected = com.mongodb.MongoSecurityException.class)
    public void testWrongMongoCredentials() throws IllegalCvaCredentialsException, IllegalCvaConfigurationException {
        InputStream configStream = CvaConfigurationTest.class.getResourceAsStream("/config/cva.wrongmongocredentials.yml");
        CvaConfiguration cvaConfiguration = CvaConfiguration.load(configStream, "yaml");
        CvaConfiguration.getMongoCredentials();
    }

    @Test(expected = IllegalCvaConfigurationException.class)
    public void testCvaConfigurationUnexistingStorageId() throws IllegalCvaConfigurationException {
        InputStream configStream = CvaConfigurationTest.class.getResourceAsStream("/config/cva.unexistingstorageid.yml");
        CvaConfiguration cvaConfiguration = CvaConfiguration.load(configStream, "yaml");
    }

    @Test(expected = IllegalCvaConfigurationException.class)
    public void testCvaConfigurationUnexistingStorageId2() throws IllegalCvaConfigurationException {
        InputStream configStream = CvaConfigurationTest.class.getResourceAsStream("/config/cva.unexistingstorageid2.yml");
        CvaConfiguration cvaConfiguration = CvaConfiguration.load(configStream, "yaml");
    }

    @Test(expected = IllegalCvaConfigurationException.class)
    public void testCvaConfigurationWrongHost() throws IllegalCvaConfigurationException {
        InputStream configStream = CvaConfigurationTest.class.getResourceAsStream("/config/cva.wronghost.yml");
        CvaConfiguration cvaConfiguration = CvaConfiguration.load(configStream, "yaml");
    }
}
