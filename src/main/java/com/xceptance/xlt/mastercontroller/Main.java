/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
import java.util.ArrayList;
import java.util.EnumSet;
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
import org.apache.commons.lang3.EnumUtils;
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
import com.xceptance.xlt.agentcontroller.TestResultAmount;
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

    private static final String OPTION_DOWNLOAD = "only-download";

    private static final String OPTION_COMMANDS = "c";

    private static final Log log = LogFactory.getLog(Main.class);

    protected BasicConsoleUI ui;

    protected void initialize(final CommandLine commandLine) throws Exception
    {
        // get commandline option with path to additional propertie file
        final File overridePropertyFile = getOverridePropertiesFile(commandLine);

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

    File getOverridePropertiesFile(final CommandLine commandLine)
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
    protected void setupHttpsProxy(final MasterControllerConfiguration config)
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
    protected HessianProxyFactory getHessianProxyFactory(final MasterControllerConfiguration config)
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
    protected UrlConnectionFactory getUrlConnectionFactory(final MasterControllerConfiguration config)
    {
        final UrlConnectionFactory urlConnectionFactory = new UrlConnectionFactory();
        urlConnectionFactory.setEasySsl(true);
        urlConnectionFactory.setConnectTimeout(config.getAgentControllerConnectTimeout());
        urlConnectionFactory.setReadTimeout(config.getAgentControllerReadTimeout());
        urlConnectionFactory.setUserName(config.getUserName());
        urlConnectionFactory.setPassword(config.getPassword());

        return urlConnectionFactory;
    }

    private void startAgentControllerEmbedded(final Map<String, AgentController> agentControllers,
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

    private void startAgentControllerRemote(final Map<String, AgentController> agentControllers,
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

    private MasterController startMasterController(final MasterControllerConfiguration config, final CommandLine commandLine,
                                                   final boolean isAgentControllerConnectionRelaxed,
                                                   final Map<String, AgentController> agentControllers)
    {
        final String propertiesFileName = commandLine.getOptionValue(OPTION_TEST_PROPS_FILE);
        final String timezone = commandLine.getOptionValue(OPTION_TIMEZONE);
        final MasterController masterController = new MasterController(agentControllers, config, propertiesFileName,
                                                                       isAgentControllerConnectionRelaxed, timezone);

        return masterController;
    }

    protected BasicConsoleUI setupUi(final MasterController masterController, final MasterControllerConfiguration config,
                                     final CommandLine commandLine, final boolean sequentialMode, final boolean autoMode,
                                     final boolean fafMode)
    {
        final BasicConsoleUI ui;

        final boolean commandsMode = commandLine.hasOption(OPTION_COMMANDS);
        final boolean generateReport = commandLine.hasOption(OPTION_REPORT);
        final boolean noResults = autoMode && commandLine.hasOption(OPTION_NO_DOWNLOAD);
        final boolean download = commandLine.hasOption(OPTION_DOWNLOAD);

        // determine amount of test results to download
        TestResultAmount resultAmount = TestResultAmount.ALL;
        if (download)
        {
            final String downloadArg = commandLine.getOptionValue(OPTION_DOWNLOAD);
            resultAmount = ResultDataTypes.asTestResultAmount(downloadArg);
        }

        /*
         * Inform user about mutually exclusive command line options
         */
        if (fafMode)
        {
            System.out.println("\n*** Command-line option -" + OPTION_FAF + " is deprecated. Please use -" + OPTION_AUTO +
                               " instead. ***\n");
        }
        if (generateReport && noResults)
        {
            System.out.println("\n*** Cannot generate report as download of test results will be skipped. ***\n");
        }
        if (download && noResults)
        {
            System.out.println("\n*** Cannot apply given download filter as download of test results will be skipped.");
        }

        if (autoMode)
        {
            ui = new FireAndForgetUI(masterController, sequentialMode, generateReport, noResults,
                                     config.getAgentControllerInitialResponseTimeout(), resultAmount);
            masterController.setTestComment(commandLine.getOptionValue(XltConstants.COMMANDLINE_OPTION_COMMENT, null));
        }
        else if (commandsMode)
        {
            final String commandList = commandLine.getOptionValue(OPTION_COMMANDS);

            ui = new NonInteractiveUI(masterController, commandList, config.getAgentControllerInitialResponseTimeout(), resultAmount);
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

    protected Options createCommandLineOptions()
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
                                           "Execute the commands, given as a comma-separated list, in the specified order and quit. Supported commands are: " +
                                                                              StringUtils.join(MasterControllerCommands.values(), ", ") +
                                                                              ".");
        commands.setArgName("commandList");
        options.addOption(commands);

        final Option download = new Option(null, OPTION_DOWNLOAD, true,
                                           "Restrict download to the given (comma-separated list of) result data types (non-interactive mode only). Supported values are: " +
                                                                        StringUtils.join(ResultDataTypes.values(), ", ") + ".");
        download.setArgName("dataTypeList");
        options.addOption(download);

        return options;
    }

    protected CommandLine parseCommandLine(final String[] args, Options options) throws ParseException
    {
        final CommandLineParser parser = new DefaultParser();

        return parser.parse(options, args);
    }

    protected void printUsageInfo(final Options options)
    {
        final HelpFormatter formatter = new HelpFormatter();

        formatter.setSyntaxPrefix("Usage:\n");
        formatter.setWidth(79);

        final StringBuilder usage = new StringBuilder();
        usage.append("   mastercontroller [<other options>]\n");
        usage.append("     -> Runs in interactive mode. Choose the command to be executed next.\n\n");
        usage.append("   mastercontroller -c <commandList> [<other options>]\n");
        usage.append("     -> Runs in non-interactive mode. Pass the commands to be executed.\n\n");
        usage.append("   mastercontroller -auto [<other options>]\n");
        usage.append("     -> Runs a load test in non-interactive mode by executing all needed commands automatically.\n\n");

        formatter.printHelp(usage.toString(), "Options:", options, null);
    }

    /**
     * Validates the given command line. Although the command line has been parsed okay previously regarding to syntax,
     * there might be contradicting options, invalid option values, etc. If any of those is detected, the program will
     * quit immediately with exit code {@link ProcessExitCodes#PARAMETER_ERROR}.
     * 
     * @param commandLine
     *            the command line object to check
     */
    protected boolean validateCommandLine(final CommandLine commandLine)
    {
        final boolean fafMode = commandLine.hasOption(OPTION_FAF);
        final boolean autoMode = commandLine.hasOption(OPTION_AUTO);
        final boolean commandMode = commandLine.hasOption(OPTION_COMMANDS);
        final boolean sequentialMode = commandLine.hasOption(OPTION_SEQUENTIAL);

        boolean invalid = false;

        // -auto|-faf|-sequential and -c are mutually exclusive
        if ((autoMode || fafMode || sequentialMode) && commandMode)
        {
            final String message = String.format("Option '-%s' cannot be used together with '-%s', '-%s', or '-%s'.", OPTION_COMMANDS,
                                                 OPTION_AUTO, OPTION_FAF, OPTION_SEQUENTIAL);

            System.out.println(message);
            log.error(message);

            invalid = true;
        }

        // check -c option value
        final String commandList = commandLine.getOptionValue(OPTION_COMMANDS);
        if (commandList != null)
        {
            final String[] unknownCommands = MasterControllerCommands.validate(commandList);
            if (unknownCommands.length > 0)
            {
                final String message = String.format("Unrecognized commands passed to '-%s' option: %s\nSupported commands: %s",
                                                     OPTION_COMMANDS, StringUtils.join(unknownCommands, ", "),
                                                     StringUtils.join(MasterControllerCommands.values(), ", "));

                System.out.println(message);
                log.error(message);

                invalid = true;
            }
        }

        // check --only-download option
        if (commandLine.hasOption(OPTION_DOWNLOAD))
        {
            // --only-download and -noDownload are mutually exclusive
            if (commandLine.hasOption(OPTION_NO_DOWNLOAD))
            {
                final String message = String.format("Option '--%s' cannot be used together with option '-%s'.", OPTION_DOWNLOAD,
                                                     OPTION_NO_DOWNLOAD);
                System.out.println(message);
                log.error(message);

                invalid = true;
            }
            else if (!(commandMode || autoMode || sequentialMode || fafMode))
            {
                final String message = String.format("Option '--%s' can only be used in non-interactive mode.", OPTION_DOWNLOAD);
                System.out.println(message);
                log.error(message);

                invalid = true;
            }
            else
            {
                // validate option values
                final String[] unknownTypes = ResultDataTypes.validate(commandLine.getOptionValue(OPTION_DOWNLOAD));
                if (unknownTypes.length > 0)
                {
                    final String message = String.format("Unrecognized values passed as argument to '--%s' option: %s\nSupported values: %s",
                                                         OPTION_DOWNLOAD, StringUtils.join(unknownTypes, ", "),
                                                         StringUtils.join(ResultDataTypes.values(), ", "));
                    System.out.println(message);
                    log.error(message);

                    invalid = true;
                }
            }
        }

        return invalid;
    }

    protected void run(final String[] args)
    {
        Locale.setDefault(Locale.US);

        // parse command line
        Options options = null;
        CommandLine commandLine = null;
        try
        {
            options = createCommandLineOptions();
            commandLine = parseCommandLine(args, options);
        }
        catch (final ParseException ex)
        {
            printUsageInfo(options);
            System.exit(ProcessExitCodes.PARAMETER_ERROR);
        }

        // validate command line
        if (validateCommandLine(commandLine))
        {
            System.exit(ProcessExitCodes.PARAMETER_ERROR);
        }

        // initialize master controller
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

        // run the master controller UI
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

    public static void main(final String[] args)
    {
        final Main main = new Main();

        main.run(args);
    }

    private static enum ResultDataTypes
    {
     logs,
     resultbrowsers,
     measurements;

        public static String[] validate(final String valueString)
        {
            final ArrayList<String> unknown = new ArrayList<>();
            for (final String arg : parse(valueString))
            {
                try
                {
                    ResultDataTypes.valueOf(arg);
                }
                catch (IllegalArgumentException e)
                {
                    unknown.add(arg);
                }

            }

            return unknown.toArray(new String[unknown.size()]);
        }

        public static TestResultAmount asTestResultAmount(final String valueString)
        {
            final EnumSet<ResultDataTypes> selection = EnumSet.noneOf(ResultDataTypes.class);
            for (final String arg : parse(valueString))
            {
                final ResultDataTypes dataType = EnumUtils.getEnum(ResultDataTypes.class, arg);
                if (dataType != null)
                {
                    selection.add(dataType);
                }
            }

            final boolean withLogs = selection.contains(logs);
            final boolean withResultBrowsers = selection.contains(resultbrowsers);
            final boolean withMeasurements = selection.contains(measurements);

            TestResultAmount resultAmount = TestResultAmount.ALL;
            if (withLogs)
            {
                if (!withResultBrowsers)
                {
                    resultAmount = withMeasurements ? TestResultAmount.MEASUREMENTS_AND_LOGS : TestResultAmount.LOGS_ONLY;
                }
                else if (!withMeasurements)
                {
                    resultAmount = TestResultAmount.RESULTBROWSER_AND_LOGS;
                }
            }
            else
            {
                if (withResultBrowsers)
                {
                    resultAmount = withMeasurements ? TestResultAmount.MEASUREMENTS_AND_RESULTBROWSER : TestResultAmount.RESULTBROWSER_ONLY;
                }
                else if (withMeasurements)
                {
                    resultAmount = TestResultAmount.MEASUREMENTS_ONLY;
                }
                else
                {
                    resultAmount = TestResultAmount.CANCEL;
                }
            }

            return resultAmount;
        }

        private static String[] parse(final String valueString)
        {
            return StringUtils.split(valueString, ',');
        }
    }
}
