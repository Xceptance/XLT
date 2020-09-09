/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.mastercontroller;

import java.io.File;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.caucho.hessian.client.EasyHessianProxyFactory;
import com.caucho.hessian.client.HessianProxyFactory;
import com.xceptance.common.net.UrlConnectionFactory;
import com.xceptance.common.util.ProcessExitCodes;
import com.xceptance.xlt.agentcontroller.AgentController;
import com.xceptance.xlt.agentcontroller.AgentControllerImpl;
import com.xceptance.xlt.agentcontroller.AgentControllerProxy;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.mastercontroller.NonInteractiveUI.MasterControllerCommands;
import com.xceptance.xlt.util.FailedAgentControllerCollection;

/**
 * The Main class is the entry point to the XLT master controller.
 */
public class Main
{
    private static final String OPTION_TIMEZONE = "timezone";

    private static final String OPTION_AUTO = "auto";

    private static final String OPTION_EMBEDDED = "embedded";

    private static final String OPTION_REPORT = "report";

    private static final String OPTION_FAF = "faf";

    private static final String OPTION_SEQUENTIAL = "sequential";

    private static final String OPTION_TEST_PROPS_FILE = "testPropertiesFile";

    private static final String OPTION_PROPERTY_DEFINITION = "D";

    private static final String OPTION_RESULT_OVERRIDE = "o";

    private static final String OPTION_NO_DOWNLOAD = "noDownload";

    private static final String OPTION_COMMANDS = "c";

    private static final Log log = LogFactory.getLog(Main.class);

    private static BasicConsoleUI ui;

    private static void initialize(final CommandLine commandLine) throws Exception
    {
        // get commandline option with path to additional propertie file
        final File overridePropertyFile = getOverridePropertieFile(commandLine);

        // read master controller configuration
        final Properties commandLineProps = commandLine.getOptionProperties(OPTION_PROPERTY_DEFINITION);
        final MasterControllerConfiguration config = new MasterControllerConfiguration(overridePropertyFile, commandLineProps,
                                                                                       commandLine.hasOption(OPTION_EMBEDDED));

        // publish mastercontroller's https proxy settings
        setupHttpsProxy(config);

        final boolean sequentialMode = commandLine.hasOption(OPTION_SEQUENTIAL);
        final boolean fafMode = commandLine.hasOption(OPTION_FAF);
        final boolean autoMode = commandLine.hasOption(OPTION_AUTO) || fafMode || sequentialMode;
        final boolean commandsMode = commandLine.hasOption(OPTION_COMMANDS);

        // restrict relaxed connections to 'auto' mode
        final boolean isAgentControllerConnectionRelaxed = config.isAgentControllerConnectionRelaxed() &&
                                                           commandLine.hasOption(OPTION_AUTO);

        // restrict results override to non-sequential 'auto' mode or commands mode
        if ((autoMode && !sequentialMode || commandsMode) && commandLine.hasOption(OPTION_RESULT_OVERRIDE))
        {
            final String outputDir = commandLine.getOptionValue(OPTION_RESULT_OVERRIDE);
            if (StringUtils.isNotBlank(outputDir))
            {
                config.setResultOutputDirectory(outputDir);
            }
        }

        // unconnected agent controllers
        final FailedAgentControllerCollection unconnectedAgentControllers = new FailedAgentControllerCollection();

        // get the agent controllers
        final Map<String, AgentController> agentControllers = new TreeMap<String, AgentController>();
        final int agentBaseNumber = 0;
        if (commandLine.hasOption(OPTION_EMBEDDED))
        {
            startAgentControllerEmbedded(agentControllers, unconnectedAgentControllers, config, commandLineProps,
                                         isAgentControllerConnectionRelaxed, agentBaseNumber);
        }
        else
        {
            startAgentControllerRemote(agentControllers, unconnectedAgentControllers, config, commandLineProps,
                                       isAgentControllerConnectionRelaxed, agentBaseNumber);
        }

        // setup master controller
        final MasterController masterController = startMasterController(config, commandLine, isAgentControllerConnectionRelaxed,
                                                                        agentControllers);

        // setup user interface
        ui = setupUi(masterController, config, commandLine, sequentialMode, autoMode, fafMode);

        masterController.setUserInterface(ui);

        ui.printXltInfo();

        // inform user of unconnected agent controllers
        ui.skipAgentControllerConnections(unconnectedAgentControllers);

        // initialize agent statuses for all agent controllers
        masterController.init();

        // pre check agent controllers
        ui.printAgentControllerPreCheckInformation();
    }

    private static File getOverridePropertieFile(final CommandLine commandLine)
    {
        final String overridePropertyFileName = commandLine.getOptionValue(XltConstants.COMMANDLINE_OPTION_PROPERTY_FILENAME);
        if (StringUtils.isNoneBlank(overridePropertyFileName))
        {
            return new File(overridePropertyFileName);
        }
        return null;
    }

    /**
     * Publish master controller's https proxy settings if HTTPS proxy is enabled in configuration
     * 
     * @param config
     *            master controller configuration
     */
    private static void setupHttpsProxy(final MasterControllerConfiguration config)
    {
        if (config.isHttpsProxyEnabled())
        {
            System.setProperty("https.proxyHost", config.getHttpsProxyHost());
            System.setProperty("https.proxyPort", config.getHttpsProxyPort());
            System.setProperty("https.nonProxyHosts", config.getHttpsProxyBypassHosts());

            final XltProxySelector proxySel = new XltProxySelector(ProxySelector.getDefault());
            ProxySelector.setDefault(proxySel);
        }
    }

    /**
     * Get the HessianProxyFactory.
     * 
     * @param config
     *            master controller configuration
     * @return the HessianProxyFactory
     */
    private static HessianProxyFactory getHessianProxyFactory(final MasterControllerConfiguration config)
    {
        final HessianProxyFactory proxyFactory = new EasyHessianProxyFactory();
        proxyFactory.setConnectTimeout(config.getAgentControllerConnectTimeout());
        proxyFactory.setReadTimeout(config.getAgentControllerReadTimeout());
        proxyFactory.setUser(config.getUserName());
        proxyFactory.setPassword(config.getPassword());

        return proxyFactory;
    }

    /**
     * Get the UrlConnectionFactory.
     * 
     * @param config
     *            master controller configuration
     * @return the UrlConnectionFactory
     */
    private static UrlConnectionFactory getUrlConnectionFactory(final MasterControllerConfiguration config)
    {
        final UrlConnectionFactory urlConnectionFactory = new UrlConnectionFactory();
        urlConnectionFactory.setEasySsl(true);
        urlConnectionFactory.setConnectTimeout(config.getAgentControllerConnectTimeout());
        urlConnectionFactory.setReadTimeout(config.getAgentControllerReadTimeout());
        urlConnectionFactory.setUserName(config.getUserName());
        urlConnectionFactory.setPassword(config.getPassword());

        return urlConnectionFactory;
    }

    private static void startAgentControllerEmbedded(final Map<String, AgentController> agentControllers,
                                                     final FailedAgentControllerCollection unconnectedAgentControllers,
                                                     final MasterControllerConfiguration config, final Properties commandLineProps,
                                                     final boolean isAgentControllerConnectionRelaxed, final int agentBaseNumber)
        throws IOException
    {
        log.info("create embedded agent controller");
        AgentControllerImpl agentController;
        try
        {
            // local mode -> create a local agent controller
            agentController = new AgentControllerImpl(commandLineProps);
            agentController.init("embedded", null, config.getDefaultWeight(), config.getDefaultAgentCount(), agentBaseNumber, false);
            agentControllers.put(agentController.getName(), agentController);
        }
        catch (final Exception e)
        {
            final String message = "Unable to open proxy for embedded@localhost:8500";
            if (isAgentControllerConnectionRelaxed)
            {
                log.warn(message, e);
                System.out.println(message);
            }
            else
            {
                throw new IOException(message, e);
            }
        }
    }

    private static void startAgentControllerRemote(final Map<String, AgentController> agentControllers,
                                                   final FailedAgentControllerCollection unconnectedAgentControllers,
                                                   final MasterControllerConfiguration config, final Properties commandLineProps,
                                                   final boolean isAgentControllerConnectionRelaxed, int agentBaseNumber)
        throws Exception
    {
        log.info("create proxy controllers for remote agent controllers");

        // set up Hessian proxy factory
        final HessianProxyFactory proxyFactory = getHessianProxyFactory(config);

        // set up URL connection factory (for non-Hessian requests)
        final UrlConnectionFactory urlConnectionFactory = getUrlConnectionFactory(config);

        // distributed mode -> create a proxy for each configured agent controller
        final List<AgentControllerConnectionInfo> connectionInfos = config.getAgentControllerConnectionInfos();

        // now create/setup the agent controllers
        for (final AgentControllerConnectionInfo info : connectionInfos)
        {
            final int agentCount = info.getNumberOfAgents();
            final AgentControllerProxy agentController = new AgentControllerProxy(commandLineProps, proxyFactory, urlConnectionFactory);

            try
            {
                agentController.init(info.getName(), info.getUrl(), info.getWeight(), agentCount, agentBaseNumber,
                                     info.runsClientPerformanceTests());

                agentControllers.put(agentController.getName(), agentController);// proxy

                if (log.isDebugEnabled())
                {
                    log.debug("proxy created for " + info.getName() + " @ " + info.getUrl().getHost() + ":" + info.getUrl().getPort());
                }

                agentBaseNumber += agentCount;
            }
            catch (final Exception e)
            {
                // connection is broken
                final String message = "Unable to open proxy for " + info.getName() + " @ " + info.getUrl().getHost() + ":" +
                                       info.getUrl().getPort() + ": " + e.getMessage();
                if (isAgentControllerConnectionRelaxed)
                {
                    log.warn(message, e);
                    unconnectedAgentControllers.add(agentController, e);
                }
                else
                {
                    throw new IOException(message, e);
                }
            }
        }
    }

    private static MasterController startMasterController(final MasterControllerConfiguration config, final CommandLine commandLine,
                                                          final boolean isAgentControllerConnectionRelaxed,
                                                          final Map<String, AgentController> agentControllers)
    {
        final String propertiesFileName = commandLine.getOptionValue(OPTION_TEST_PROPS_FILE);
        final String timezone = commandLine.getOptionValue(OPTION_TIMEZONE);
        final MasterController masterController = new MasterController(agentControllers, config, propertiesFileName,
                                                                       isAgentControllerConnectionRelaxed, timezone);

        return masterController;
    }

    private static BasicConsoleUI setupUi(final MasterController masterController, final MasterControllerConfiguration config,
                                          final CommandLine commandLine, final boolean sequentialMode, final boolean autoMode,
                                          final boolean fafMode)
    {
        final BasicConsoleUI ui;

        final boolean commandsMode = commandLine.hasOption(OPTION_COMMANDS);

        // inform user about mutually exclusive command line options
        if (fafMode)
        {
            System.out.println("\n*** Command-line option -" + OPTION_FAF + " is deprecated. Please use -" + OPTION_AUTO +
                               " instead. ***\n");
        }

        // inform user about mutually exclusive command line options
        final boolean generateReport = commandLine.hasOption(OPTION_REPORT);
        final boolean noResults = autoMode && commandLine.hasOption(OPTION_NO_DOWNLOAD);
        if (generateReport && noResults)
        {
            System.out.println("\n*** Cannot generate report as download of test results will be skipped. ***\n");
        }

        if (autoMode)
        {
            ui = new FireAndForgetUI(masterController, sequentialMode, generateReport, noResults,
                                     config.getAgentControllerInitialResponseTimeout());
            masterController.setTestComment(commandLine.getOptionValue(XltConstants.COMMANDLINE_OPTION_COMMENT, null));
        }
        else if (commandsMode)
        {
            final String commandList = commandLine.getOptionValue(OPTION_COMMANDS);

            ui = new NonInteractiveUI(masterController, commandList, config.getAgentControllerInitialResponseTimeout());
            masterController.setTestComment(commandLine.getOptionValue(XltConstants.COMMANDLINE_OPTION_COMMENT, null));
        }
        else
        {
            ui = new InteractiveUI(masterController, generateReport);
        }

        ui.setStatusListUpdateInterval(config.getStatusListUpdateInterval());
        ui.setShowDetailedStatusList(config.getShowDetailedStatusList());

        return ui;
    }

    private static Options createCommandLineOptions()
    {
        final Options options = new Options();

        final Option autoMode = new Option(OPTION_AUTO, false, "Run a load test in non-interactive mode.");
        options.addOption(autoMode);

        final Option embeddedMode = new Option(OPTION_EMBEDDED, false, "Use a single embedded agent controller.");
        options.addOption(embeddedMode);

        final Option fafMode = new Option(OPTION_FAF, false, "(deprecated, use -" + OPTION_AUTO + " instead)");
        options.addOption(fafMode);

        final Option generateReport = new Option(OPTION_REPORT, false,
                                                 "Generate the test report right after downloading the test results (ignored in commands mode).");
        options.addOption(generateReport);

        final Option sequentialMode = new Option(OPTION_SEQUENTIAL, false,
                                                 "Run test cases one after the other (implies -" + OPTION_AUTO + ").");
        options.addOption(sequentialMode);

        final Option additionalPropertiesFile = new Option(XltConstants.COMMANDLINE_OPTION_PROPERTY_FILENAME, true,
                                                           "Set the path to an additional properties file, which overrides the values in file 'mastercontroller.properties'.");
        additionalPropertiesFile.setArgName("file");
        options.addOption(additionalPropertiesFile);

        final Option testComment = new Option(XltConstants.COMMANDLINE_OPTION_COMMENT, true,
                                              "Set a comment for the test run (ignored in interactive mode).");
        testComment.setArgName("string");
        options.addOption(testComment);

        final Option testPropertiesFileName = new Option(OPTION_TEST_PROPS_FILE, true,
                                                         "Use the specified file as the test-run specific properties file.");
        testPropertiesFileName.setArgName("fileName");
        options.addOption(testPropertiesFileName);

        final Option propertyDefinition = new Option(OPTION_PROPERTY_DEFINITION, true,
                                                     "Override a property in file 'mastercontroller.properties'.");
        propertyDefinition.setValueSeparator('=');
        propertyDefinition.setArgName("property=value");
        propertyDefinition.setArgs(2);
        options.addOption(propertyDefinition);

        final Option timeZone = new Option(OPTION_TIMEZONE, true, "Override the user's default timezone when generating the test report.");
        timeZone.setArgName("timezoneId");
        options.addOption(timeZone);

        final Option output = new Option(OPTION_RESULT_OVERRIDE, true,
                                         "Store downloaded test results to this directory (ignored in interactive or sequential mode).");
        output.setArgName("dir");
        options.addOption(output);

        final Option noResults = new Option(OPTION_NO_DOWNLOAD, false, "Don't download test results (auto mode only).");
        options.addOption(noResults);

        final Option commands = new Option(OPTION_COMMANDS, "commands", true,
                                           "Execute commands given as a comma-separated list and quit. Supported commands are: " +
                                                                              StringUtils.join(MasterControllerCommands.values(), ", ") +
                                                                              ".");
        commands.setArgName("commandList");
        options.addOption(commands);

        return options;
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

    private static void printUsageInfoAndExit(final Options options)
    {
        final HelpFormatter formatter = new HelpFormatter();

        System.out.println("Usage:");
        System.out.println("    mastercontroller [<other options>]");
        System.out.println("      -> Runs in interactive mode. Choose the command to be executed next.");
        System.out.println();
        System.out.println("    mastercontroller -c <commandList> [<other options>]");
        System.out.println("      -> Runs in non-interactive mode. Pass the commands to be executed.");
        System.out.println();
        System.out.println("    mastercontroller -auto [<other options>]");
        System.out.println("      -> Runs a load test in non-interactive mode by executing all needed commands automatically.");
        System.out.println();

        formatter.setSyntaxPrefix("");
        formatter.setWidth(79);

        formatter.printHelp(" ", "Options:", options, "");
        System.out.println();

        System.exit(ProcessExitCodes.PARAMETER_ERROR);
    }

    /**
     * Validates the given command line. Although the command line has been parsed okay previously regarding to syntax,
     * there might be contradicting options, invalid option values, etc. If any of those is detected, the program will
     * quit immediately with exit code {@link ProcessExitCodes#PARAMETER_ERROR}.
     * 
     * @param commandLine
     *            the command line object to check
     */
    private static void validateCommandLine(final CommandLine commandLine)
    {
        boolean invalid = false;

        // -auto|-faf|-sequential and -c are mutually exclusive
        if ((commandLine.hasOption(OPTION_AUTO) || commandLine.hasOption(OPTION_FAF) || commandLine.hasOption(OPTION_SEQUENTIAL)) &&
            commandLine.hasOption(OPTION_COMMANDS))
        {
            System.out.printf("Option '-%s' cannot be used together with '-%s', '-%s', or '-%s'.\n", OPTION_COMMANDS, OPTION_AUTO,
                              OPTION_FAF, OPTION_SEQUENTIAL);
            invalid = true;
        }

        // check -c option value
        final String commandList = commandLine.getOptionValue(OPTION_COMMANDS);
        if (commandList != null)
        {
            final String[] unknownCommands = MasterControllerCommands.validate(commandList);
            if (unknownCommands.length > 0)
            {
                System.out.printf("Unrecognized commands passed to '-%s' option: %s\n", OPTION_COMMANDS,
                                  StringUtils.join(unknownCommands, ", "));
                System.out.printf("Supported commands: %s\n", StringUtils.join(MasterControllerCommands.values(), ", "));
                invalid = true;
            }
        }

        // if invalid quit with the correct exit code
        if (invalid)
        {
            System.exit(ProcessExitCodes.PARAMETER_ERROR);
        }
    }

    public static void main(final String[] args)
    {
        Locale.setDefault(Locale.US);

        final CommandLine commandLine = parseCommandLine(args);
        validateCommandLine(commandLine);

        try
        {
            initialize(commandLine);
        }
        catch (final Exception ex)
        {
            System.out.println("\nFailed to initialize master controller: " + ex.getMessage());
            log.fatal("Failed to initialize master controller:", ex);
            System.exit(ProcessExitCodes.GENERAL_ERROR);
        }

        try
        {
            ui.run();
            System.exit(ProcessExitCodes.SUCCESS);
        }
        catch (final Exception ex)
        {
            System.out.println("\nFailed to run master controller: " + ex.getMessage());
            log.fatal("Failed to run master controller:", ex);
            System.exit(ProcessExitCodes.GENERAL_ERROR);
        }
    }
}
