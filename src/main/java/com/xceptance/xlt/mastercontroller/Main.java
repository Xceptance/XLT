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

        // restrict relaxed connections to 'auto' mode
        final boolean isAgentControllerConnectionRelaxed = config.isAgentControllerConnectionRelaxed() &&
                                                           commandLine.hasOption(OPTION_AUTO);

        // restrict results override to non-sequential 'auto' mode
        if (autoMode && !sequentialMode && commandLine.hasOption(OPTION_RESULT_OVERRIDE))
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
        else
        {
            ui = new CommandBasedUI(masterController, generateReport);
        }

        ui.setStatusListUpdateInterval(config.getStatusListUpdateInterval());
        ui.setShowDetailedStatusList(config.getShowDetailedStatusList());

        return ui;
    }

    private static Options createCommandLineOptions()
    {
        final Options options = new Options();

        final Option autoMode = new Option(OPTION_AUTO, false, "run in non-interactive mode");
        options.addOption(autoMode);

        final Option embeddedMode = new Option(OPTION_EMBEDDED, false, "use a single embedded agent controller");
        options.addOption(embeddedMode);

        final Option fafMode = new Option(OPTION_FAF, false, "(deprecated, use -" + OPTION_AUTO + " instead)");
        options.addOption(fafMode);

        final Option generateReport = new Option(OPTION_REPORT, false, "generate the test report right after downloading the test results");
        options.addOption(generateReport);

        final Option sequentialMode = new Option(OPTION_SEQUENTIAL, false,
                                                 "run test cases one after the other (implies -" + OPTION_AUTO + ")");
        options.addOption(sequentialMode);

        final Option additionalPropertiesFile = new Option(XltConstants.COMMANDLINE_OPTION_PROPERTY_FILENAME, true,
                                                           "the path to an additional properties file, which overrides the values in the mastercontroller.properties");
        additionalPropertiesFile.setArgName("file");
        options.addOption(additionalPropertiesFile);

        final Option testComment = new Option(XltConstants.COMMANDLINE_OPTION_COMMENT, true,
                                              "set a comment for the test run (ignored in interactive mode)");
        testComment.setArgName("string");
        options.addOption(testComment);

        final Option testPropertiesFileName = new Option(OPTION_TEST_PROPS_FILE, true,
                                                         "use the specified file as the test-run specific properties file");
        testPropertiesFileName.setArgName("fileName");
        options.addOption(testPropertiesFileName);

        final Option propertyDefinition = new Option(OPTION_PROPERTY_DEFINITION, true,
                                                     "override a property in file 'mastercontroller.properties'");
        propertyDefinition.setValueSeparator('=');
        propertyDefinition.setArgName("property=value");
        propertyDefinition.setArgs(2);
        options.addOption(propertyDefinition);

        final Option timeZone = new Option(OPTION_TIMEZONE, true, "override the user's default timezone when generating test report");
        timeZone.setArgName("timezoneId");
        options.addOption(timeZone);

        final Option output = new Option(OPTION_RESULT_OVERRIDE, true,
                                         "store downloaded test results in this directory (ignored in interactive or sequential mode)");
        output.setArgName("dir");
        options.addOption(output);

        final Option noResults = new Option(OPTION_NO_DOWNLOAD, false, "don't download test results (ignored in interactive mode)");
        options.addOption(noResults);

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
        formatter.setSyntaxPrefix("Usage: ");
        formatter.setWidth(79);

        System.out.println();
        formatter.printHelp(XltConstants.MASTERCONTROLLER_EXECUTABLE_NAME + " [<options>]", "\nOptions:", options, "");
        System.out.println();

        System.exit(ProcessExitCodes.PARAMETER_ERROR);
    }

    public static void main(final String[] args)
    {
        Locale.setDefault(Locale.US);

        final CommandLine commandLine = parseCommandLine(args);

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
