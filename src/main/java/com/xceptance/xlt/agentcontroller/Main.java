package com.xceptance.xlt.agentcontroller;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.common.util.ProcessExitCodes;

/**
 * The Main class is the entry point to the XLT agent controller.
 */
public class Main
{
    private static final Log log = LogFactory.getLog(Main.class);

    private static final String OPTION_PROPERTY_DEFINITION = "D";

    public static void main(final String[] args)
    {
        final CommandLine commandLine = parseCommandLine(args);
        final Properties commandLineProps = commandLine.getOptionProperties(OPTION_PROPERTY_DEFINITION);

        try
        {
            new AgentControllerImpl(commandLineProps);
        }
        catch (final Exception ex)
        {
            log.fatal("Error while starting agent controller", ex);
            // agent controller failed to start -> terminate VM with proper exit code
            System.exit(ProcessExitCodes.GENERAL_ERROR);
        }
    }

    private static CommandLine parseCommandLine(final String[] args)
    {
        final Options options = createCommandLineOptions();
        final CommandLineParser parser = new DefaultParser();

        try
        {
            return parser.parse(options, args);
        }
        catch (final ParseException ex)
        {
            printUsageInfoAndExit(options);

            // will never get here
            return null;
        }
    }

    private static Options createCommandLineOptions()
    {
        final Options options = new Options();

        final Option propertyDefinition = new Option(OPTION_PROPERTY_DEFINITION, true,
                                                     "override a property in file 'agentcontroller.properties'");
        propertyDefinition.setValueSeparator('=');
        propertyDefinition.setArgName("property=value");
        propertyDefinition.setArgs(2);
        options.addOption(propertyDefinition);

        return options;
    }

    private static void printUsageInfoAndExit(final Options options)
    {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.setSyntaxPrefix("Usage: ");
        formatter.setWidth(79);

        System.out.println();
        formatter.printHelp("agentcontroller [<options>]", "\nOptions:", options, "");
        System.out.println();

        System.exit(ProcessExitCodes.PARAMETER_ERROR);
    }
}
