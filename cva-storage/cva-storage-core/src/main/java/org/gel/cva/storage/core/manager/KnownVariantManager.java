package org.gel.cva.storage.core.manager;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.knownvariant.adaptors.KnownVariantDBAdaptor;
import org.opencb.opencga.core.auth.IllegalOpenCGACredentialsException;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by priesgo on 19/01/17.
 */
public class KnownVariantManager extends CvaManager {

    private FTPClient ftpClient;
    private KnownVariantDBAdaptor knownVariantDBAdaptor;

    public KnownVariantManager(CvaConfiguration cvaConfiguration)
            throws IOException, IllegalCvaConfigurationException, IllegalOpenCGACredentialsException,
            ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            InvocationTargetException {
        super(cvaConfiguration);
        this.ftpClient = this.connectToClinVarFTP();
        String adaptorImplClass = cvaConfiguration.getStorageEngines().get(0).getOptions().get("adaptor.knownvariants");
        Class<?> clazz = Class.forName(adaptorImplClass);
        Constructor<?> ctor = clazz.getConstructor(CvaConfiguration.class);
        this.knownVariantDBAdaptor = (KnownVariantDBAdaptor)ctor.newInstance(new Object[] { cvaConfiguration });
    }

    private FTPClient connectToClinVarFTP () throws IOException{
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(cvaConfiguration.getClinVar().getFtpServer(), FTPClient.DEFAULT_PORT);
        ftpClient.login(cvaConfiguration.getClinVar().getUser(), cvaConfiguration.getClinVar().getPassword());
        return ftpClient;
    }

    private Map getLatest() throws URISyntaxException, IOException {
        String clinVarFullPath =
                String.format(cvaConfiguration.getClinVar().getAssemblyFolder(),
                        cvaConfiguration.getOrganism().getAssembly());
        FTPFile[] ftpFiles = this.ftpClient.listFiles(clinVarFullPath);
        String fileName = null;
        for (FTPFile ftpFile: ftpFiles) {
             if (ftpFile.getName().matches("^clinvar_\\d{8}.vcf.gz$")) {
                 fileName = ftpFile.getName();
                 break;
             }
        }
        if (fileName == null) {
            throw new IOException("No ClinVar file matching expected pattern");
        }
        Map result = new HashMap<String, String>();
        result.put("path", clinVarFullPath);
        result.put("name", fileName);
        return result;
    }

    private Boolean downloadFTPFile(String remoteFile, String destination) throws IOException {
        OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(destination));
        boolean success = ftpClient.retrieveFile(remoteFile, outputStream1);
        outputStream1.close();
        return success;
    }

    public void importClinvar(String version) throws URISyntaxException, IOException {

        Map latest = this.getLatest();
        boolean success = this.downloadFTPFile(latest.get("path") + "/" + latest.get("name"),
                cvaConfiguration.getTempFolder() + "/" + latest.get("name"));
    }

    public List<String> listClinVarVersions() throws IOException {

        String folder = String.format("vcf_%s", cvaConfiguration.getOrganism().getAssembly());
        return Collections.emptyList();
    }

    public static void main (String[] args)
            throws IOException, IllegalCvaConfigurationException, IllegalOpenCGACredentialsException, URISyntaxException,
                ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        CvaConfiguration cvaConfiguration = CvaConfiguration.getInstance();
        KnownVariantManager knownVariantManager = new KnownVariantManager(cvaConfiguration);
        Map latest = knownVariantManager.getLatest();
        System.out.print(latest.toString());
        boolean success = knownVariantManager.downloadFTPFile(latest.get("path") + "/" + latest.get("name"),
                cvaConfiguration.getTempFolder() + "/" + latest.get("name"));
        System.out.print(success);
    }
}
