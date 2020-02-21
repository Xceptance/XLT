package com.xceptance.xlt.gce;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.xceptance.common.util.ProcessExitCodes;

/**
 * Entry point to gce_admin console application, the simple front-end for managing Google Compute Engine (GCE) machine
 * instances.
 */
public class GceAdminMain
{
    /**
     * The main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(final String[] args)
    {
        final Options options = createCommandLineOptions();
        final CommandLine commandLine = parseCommandLine(options, args);

        if (commandLine.hasOption("help"))
        {
            printUsageInfo(options);
        }
        else
        {
            try
            {
                final GceAdmin gceAdmin = new GceAdmin();

                if (commandLine.getArgs().length > 0)
                {
                    // start in non-interactive mode
                    gceAdmin.startNonInteractiveMode(commandLine);
                }
                else
                {
                    // Enter the command loop
                    gceAdmin.startInteractiveMode();
                }
            }
            catch (final Exception e)
            {
                // In case any exception slips through
                GceAdminUtils.dieWithMessage("An unexpected error occurred: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Creates the command-line options.
     *
     * @return the options
     */
    private static Options createCommandLineOptions()
    {
        final Options options = new Options();

        options.addOption(null, "help", false, "Show help.");

        options.addOption("o", "outputFile", true,
                          "The file that will contain the agent controller connection properties corresponding to the started instances. If no such file is specified, the properties will be printed to the console.");

        return options;
    }

    /**
     * Parses the command-line arguments and returns a command-line object.
     *
     * @param options
     *            the command-line options
     * @param args
     *            the command-line arguments
     * @return the parsed command line
     */
    private static CommandLine parseCommandLine(final Options options, final String[] args)
    {
        final CommandLineParser parser = new DefaultParser();

        try
        {
            return parser.parse(options, args);
        }
        catch (final ParseException ex)
        {
            printUsageInfo(options);
            System.exit(ProcessExitCodes.PARAMETER_ERROR);

            // will never get here
            return null;
        }
    }

    /**
     * Prints usage information according to the given command-line options.
     *
     * @param options
     *            the command-line options
     */
    private static void printUsageInfo(final Options options)
    {
        final HelpFormatter formatter = new HelpFormatter();

        System.out.println("Simple front-end application to manage Google Compute Engine instances.");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("    gce_admin");
        System.out.println("      -> Run in interactive mode.\n");
        System.out.println();
        System.out.println("    gce_admin list-by-label <region-name> <name-label> [<options>]");
        System.out.println("      -> List instances non-interactively by a name label.\n");
        System.out.println();
        System.out.println("    gce_admin list-by-group <region-name> <group-name> [<options>]");
        System.out.println("      -> List instances non-interactively by instance group name.\n");
        System.out.println();
        System.out.println("    gce_admin create-group <region-name> <group-name> <instance-template-name> <instance-count> [<options>]");
        System.out.println("      -> Create managed instance group non-interactively.\n");
        System.out.println();
        System.out.println("    gce_admin delete-group <region-name> <group-name>");
        System.out.println("      -> Delete managed instance group non-interactively.");

        formatter.setSyntaxPrefix("");
        formatter.setWidth(79);

        if (!options.equals(null))
        {
            formatter.printHelp(" ", "Options:", options, "");
        }

        System.out.println();
    }
}
