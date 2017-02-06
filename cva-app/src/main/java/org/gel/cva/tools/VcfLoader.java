package org.gel.cva.tools;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.knownvariant.wrappers.KnownVariantWrapper;
import org.gel.cva.storage.core.managers.VcfManager;
import org.opencb.biodata.models.variant.VariantNormalizer;
import org.opencb.opencga.storage.core.variant.annotation.VariantAnnotatorException;

import java.io.File;
import java.io.PrintWriter;

/**
 * Created by priesgo on 06/02/17.
 */
public class VcfLoader {

    private VcfManager vcfManager;
    private Options options;
    private CommandLineParser commandLineParser;
    private final String commandLineSyntax = "java -cp cva-app-0.1.jar";
    private final String helpHeader = "CVA - VCF loader tool help";
    private final String helpFooter = "--------------------------";
    private VariantNormalizer variantNormalizer;
    private Logger logger;

    public VcfLoader() throws IllegalCvaConfigurationException {

        // Creates options
        this.options = new Options();
        Option vcfOption = new Option("v", "vcf", true, "Input VCF to load into CVA");
        vcfOption.setRequired(true);
        this.options.addOption(vcfOption);
        this.options.addOption("h", "help", true, "Shows VCF loader help");
        // Initialize command line parser
        this.commandLineParser = new DefaultParser();
        // Initialize the VCF manager
        this.vcfManager = new VcfManager(CvaConfiguration.getInstance());
        this.variantNormalizer = new VariantNormalizer(
                true, true, true);
        this.logger = CvaConfiguration.getLogger();
    }

    /**
     * Prints help
     */
    private void printHelp()
    {

        final PrintWriter writer = new PrintWriter(System.out);
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(
                writer,
                80,
                this.commandLineSyntax,
                this.helpHeader,
                this.options,
                5,
                3,
                this.helpFooter,
                true);
        writer.close();
    }

    /**
     * Runs the application
     * @param arguments
     */
    public void run(String [] arguments) throws VariantAnnotatorException, CvaException {
        CommandLine commandLine;
        try
        {
            commandLine = this.commandLineParser.parse(this.options, arguments);
            if ( commandLine.hasOption("h") )
            {
                // Print help
                this.printHelp();
            }
            if ( commandLine.hasOption("v") )
            {

                File vcfFile = new File(commandLine.getOptionValue("v"));
                this.logger.info("Starting load of file " + vcfFile.getAbsolutePath());
                this.vcfManager.loadVcf(vcfFile);
                this.logger.info("Finished loading VCF file!");
            }
        }
        catch (ParseException parseException)  // checked exception
        {
            this.logger.error(
                    "Encountered exception while parsing using GnuParser:\n"
                            + parseException.getMessage() );
        }
    }

    /**
     * Main program
     * @param arguments
     */
    public static void main(String [] arguments) throws CvaException, VariantAnnotatorException {
        VcfLoader vcfLoader = new VcfLoader();
        vcfLoader.run(arguments);
    }
}
