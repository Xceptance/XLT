package com.xceptance.xlt.mastercontroller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.xceptance.common.util.AbstractConfiguration;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.XltExecutionContext;

/**
 * The MasterControllerConfiguration is the central place where all configuration information of the master controller
 * can be retrieved from.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class MasterControllerConfiguration extends AbstractConfiguration
{
    private static final String PROP_PREFIX = XltConstants.XLT_PACKAGE_PATH + ".mastercontroller.";

    private static final String PROP_AGENT_CONTROLLER_URL_SUFFIX = ".url";

    private static final String PROP_AGENT_CONTROLLER_WEIGHT_SUFFIX = ".weight";

    private static final String PROP_AGENT_CONTROLLER_AGENTS_SUFFIX = ".agents";

    private static final String PROP_AGENT_CONTROLLER_CLIENTPERF_SUFFIX = ".clientPerformance";

    private static final String PROP_AGENT_CONTROLLERS_PREFIX = PROP_PREFIX + "agentcontrollers.";

    private static final String PROP_AGENT_CONTROLLER_DEFAULT_PREFIX = PROP_AGENT_CONTROLLERS_PREFIX + "default.";

    private static final String PROP_AGENT_CONTROLLER_DEFAULT_AGENTS = PROP_AGENT_CONTROLLER_DEFAULT_PREFIX + "agents";

    private static final String PROP_AGENT_CONTROLLER_DEFAULT_WEIGHT = PROP_AGENT_CONTROLLER_DEFAULT_PREFIX + "weight";

    private static final String PROP_AGENT_CONTROLLER_DEFAULT_CP = PROP_AGENT_CONTROLLER_DEFAULT_PREFIX + "clientPerformance";

    private static final String PROP_AGENT_FILES_DIR = PROP_PREFIX + "agentfiles";

    private static final String PROP_TEST_SUITE_PATH = PROP_PREFIX + "testSuitePath";

    private static final String PROP_REPORTS_ROOT_DIR = PROP_PREFIX + "reports";

    private static final String PROP_RESULTS_ROOT_DIR = PROP_PREFIX + "results";

    private static final String PROP_UI_STATUS_PREFIX = PROP_PREFIX + "ui.status.";

    private static final String PROP_UI_STATUS_SHOW_DETAILED = PROP_UI_STATUS_PREFIX + "detailedList";

    private static final String PROP_UI_STATUS_UPDATE_INTERVAL = PROP_UI_STATUS_PREFIX + "updateInterval";

    private static final String PROP_IGNORE_UNREACHABLE_AGENT_CONTROLLERS = PROP_PREFIX + "ignoreUnreachableAgentControllers";

    private static final String PROP_HTTPS_PROXY = PROP_PREFIX + "https.proxy";

    private static final String PROP_HTTPS_PROXY_ENABLED = PROP_HTTPS_PROXY + ".enabled";

    private static final String PROP_HTTPS_PROXY_HOST = PROP_HTTPS_PROXY + ".host";

    private static final String PROP_HTTPS_PROXY_PORT = PROP_HTTPS_PROXY + ".port";

    private static final String PROP_HTTPS_PROXY_BYPASS_HOSTS = PROP_HTTPS_PROXY + ".bypassForHosts";

    private static final String PROP_TEMP_DIR = PROP_PREFIX + "tempdir";

    private static final String PROP_MAX_PARALLEL_COMMINUCATIONS = PROP_PREFIX + "maxParallelCommunications";

    private static final String PROP_MAX_PARALLEL_UPLOADS = PROP_PREFIX + "maxParallelUploads";

    private static final String PROP_MAX_PARALLEL_DOWNLOADS = PROP_PREFIX + "maxParallelDownloads";

    private static final String PROP_CONNECT_TIMEOUT = PROP_PREFIX + "connectTimeout";

    private static final String PROP_READ_TIMEOUT = PROP_PREFIX + "readTimeout";

    private static final String PROP_INITIAL_RESPONSE_TIMEOUT = PROP_PREFIX + "initialResponseTimeout";

    private static final String PROP_PASSWORD = PROP_PREFIX + "password";

    private final List<AgentControllerConnectionInfo> agentControllerConnectionInfos;

    private File agentFilesDirectory;

    private final File configDirectory;

    private final File homeDirectory;

    private final boolean showDetailedStatusList;

    private final int statusListUpdateInterval;

    private File testReportsRootDirectory;

    private File testResultsRootDirectory;

    private File tempDirectory;

    private final boolean isAgentControllerConnectionRelaxed;

    private final boolean isHttpsProxyEnabled;

    private final String httpsProxyHost;

    private final String httpsProxyPort;

    private final String httpsProxyBypassHosts;

    private int defaultWeight = 1;

    private int defaultAgentCount = 1;

    private final int parallelCommunicationLimit;

    private final int parallelDownloadLimit;

    private final int parallelUploadLimit;

    private final int acConnectTimeout;

    private final int acReadTimeout;

    private final int acInitialResponseTimeout;

    private File resultOutputDirectory;

    private final String userName;

    private final String password;

    private final boolean isEmbedded;

    /**
     * Creates a new MasterControllerConfiguration object.
     * 
     * @param commandLineProperties
     *            the properties specified on the command line
     * @param isEmbeddedMode
     *            is the master controller started in embedded mode?
     * @throws IOException
     *             if an I/O error occurs
     */
    public MasterControllerConfiguration(final File overridePropertyFile, final Properties commandLineProperties,
                                         final boolean isEmbeddedMode) throws IOException
    {
        isEmbedded = isEmbeddedMode;
        homeDirectory = XltExecutionContext.getCurrent().getXltHomeDir();
        configDirectory = XltExecutionContext.getCurrent().getXltConfigDir();

        final File propFile = new File(configDirectory, XltConstants.MASTERCONTROLLER_PROPERTY_FILENAME);

        loadProperties(propFile);

        if (overridePropertyFile != null)
        {
            if (!overridePropertyFile.isFile() || !overridePropertyFile.canRead())
            {
                throw new FileNotFoundException(overridePropertyFile.getAbsolutePath());
            }
            loadProperties(overridePropertyFile);
        }

        addProperties(commandLineProperties);

        // test suite directory
        String agentFilesPropertyName = PROP_TEST_SUITE_PATH;
        agentFilesDirectory = getFileProperty(PROP_TEST_SUITE_PATH, null);
        if (agentFilesDirectory == null)
        {
            // fall back to the old property name
            agentFilesPropertyName = PROP_AGENT_FILES_DIR;
            agentFilesDirectory = getFileProperty(PROP_AGENT_FILES_DIR);
        }

        if (!agentFilesDirectory.isAbsolute())
        {
            agentFilesDirectory = new File(homeDirectory, agentFilesDirectory.getPath());
        }

        if (!agentFilesDirectory.isDirectory())
        {
            throw new RuntimeException("The property '" + agentFilesPropertyName + "' does not specify an existing directory.");
        }

        // test reports directory
        testReportsRootDirectory = getFileProperty(PROP_REPORTS_ROOT_DIR, new File(XltConstants.REPORT_ROOT_DIR));
        if (!testReportsRootDirectory.isAbsolute())
        {
            testReportsRootDirectory = new File(homeDirectory, testReportsRootDirectory.getPath());
        }

        // test results directory
        testResultsRootDirectory = getFileProperty(PROP_RESULTS_ROOT_DIR, new File(XltConstants.RESULT_ROOT_DIR));
        if (!testResultsRootDirectory.isAbsolute())
        {
            testResultsRootDirectory = new File(homeDirectory, testResultsRootDirectory.getPath());
        }

        // temp directory
        tempDirectory = getFileProperty(PROP_TEMP_DIR, new File(System.getProperty("java.io.tmpdir")));
        if (!tempDirectory.isAbsolute())
        {
            tempDirectory = new File(homeDirectory, tempDirectory.getPath());
        }
        if (!tempDirectory.exists())
        {
            FileUtils.forceMkdir(tempDirectory);
        }
        else if (!(tempDirectory.isDirectory() && tempDirectory.canWrite()))
        {
            throw new IOException("Temp directory is not a directory or is not writable: " + tempDirectory.getCanonicalPath());
        }

        // limit parallel communications/uploads/downloads
        parallelCommunicationLimit = getIntProperty(PROP_MAX_PARALLEL_COMMINUCATIONS, -1);
        parallelUploadLimit = getIntProperty(PROP_MAX_PARALLEL_UPLOADS, parallelCommunicationLimit);
        parallelDownloadLimit = getIntProperty(PROP_MAX_PARALLEL_DOWNLOADS, parallelCommunicationLimit);

        // agent controllers
        agentControllerConnectionInfos = readAgentControllerConnectionInfos();
        if (!isEmbeddedMode && agentControllerConnectionInfos.isEmpty())
        {
            throw new RuntimeException("No agent controllers are configured.");
        }

        // proxy settings
        isHttpsProxyEnabled = getBooleanProperty(PROP_HTTPS_PROXY_ENABLED, false);
        httpsProxyHost = getStringProperty(PROP_HTTPS_PROXY_HOST, "");
        httpsProxyPort = getStringProperty(PROP_HTTPS_PROXY_PORT, "");
        httpsProxyBypassHosts = getStringProperty(PROP_HTTPS_PROXY_BYPASS_HOSTS, "");

        // other settings
        showDetailedStatusList = getBooleanProperty(PROP_UI_STATUS_SHOW_DETAILED, false);
        statusListUpdateInterval = getIntProperty(PROP_UI_STATUS_UPDATE_INTERVAL, 5);

        isAgentControllerConnectionRelaxed = getBooleanProperty(PROP_IGNORE_UNREACHABLE_AGENT_CONTROLLERS, false);

        // set read/connect timeouts
        acConnectTimeout = getIntProperty(PROP_CONNECT_TIMEOUT, -1);
        acReadTimeout = getIntProperty(PROP_READ_TIMEOUT, -1);

        // set initial response timeout
        acInitialResponseTimeout = getIntProperty(PROP_INITIAL_RESPONSE_TIMEOUT, -1);

        // user name/password
        userName = XltConstants.USER_NAME;
        password = getStringProperty(PROP_PASSWORD, null);
    }

    /**
     * Returns the list of all configured agent controllers.
     * 
     * @return the agent controllers
     */
    public List<AgentControllerConnectionInfo> getAgentControllerConnectionInfos()
    {
        return agentControllerConnectionInfos;
    }

    /**
     * Returns the directory where the agent files are located.
     * 
     * @return the agent files directory
     */
    public File getAgentFilesDirectory()
    {
        return agentFilesDirectory;
    }

    /**
     * Returns the directory where the master controller's configuration is located.
     * 
     * @return the config directory
     */
    public File getConfigDirectory()
    {
        return configDirectory;
    }

    /**
     * Returns the master controller's home directory.
     * 
     * @return the home directory
     */
    public File getHomeDirectory()
    {
        return homeDirectory;
    }

    /**
     * Returns the master controller's temp directory.
     * 
     * @return the temp directory
     */
    public File getTempDirectory()
    {
        return tempDirectory;
    }

    /**
     * Returns the root directory of all test reports.
     * 
     * @return the test reports directory
     */
    public File getTestReportsRootDirectory()
    {
        return testReportsRootDirectory;
    }

    /**
     * Returns the root directory of all test result files.
     * 
     * @return the test results directory
     */
    public File getTestResultsRootDirectory()
    {
        return testResultsRootDirectory;
    }

    /**
     * Reads and returns the list of all configured agent controllers.
     * 
     * @return the list of agent controllers
     */
    private List<AgentControllerConnectionInfo> readAgentControllerConnectionInfos()
    {
        final List<AgentControllerConnectionInfo> infos = new ArrayList<AgentControllerConnectionInfo>();

        defaultAgentCount = getIntProperty(PROP_AGENT_CONTROLLER_DEFAULT_AGENTS, defaultAgentCount);
        defaultWeight = getIntProperty(PROP_AGENT_CONTROLLER_DEFAULT_WEIGHT, defaultWeight);

        final boolean defaultCP = getBooleanProperty(PROP_AGENT_CONTROLLER_DEFAULT_CP, false);

        final Set<String> agentControllerNames = getPropertyKeyFragment(PROP_AGENT_CONTROLLERS_PREFIX);
        final HashMap<String, String> urlToNameMap = new HashMap<String, String>(agentControllerNames.size());
        for (final String name : agentControllerNames)
        {
            // skip "default" agent controller settings
            if (name.equals("default"))
            {
                continue;
            }

            final URL url = getUrlProperty(PROP_AGENT_CONTROLLERS_PREFIX + name + PROP_AGENT_CONTROLLER_URL_SUFFIX, null);

            // silently skip agent controllers for which no URL is defined (#1193)
            if (url != null)
            {
                final String urlString = url.toString();
                final String acName = urlToNameMap.get(urlString);
                if (acName != null)
                {
                    final String errMsg = String.format("Agent controllers '%s' and '%s' share the same URL (%s). Each agent controller needs to have a unique URL.",
                                                        name, acName, urlString);
                    throw new RuntimeException(errMsg);
                }

                final int agentCount = getIntProperty(PROP_AGENT_CONTROLLERS_PREFIX + name + PROP_AGENT_CONTROLLER_AGENTS_SUFFIX,
                                                      defaultAgentCount);

                // skip agent controllers for which no agent is defined
                if (agentCount > 0)
                {
                    final int weight = getIntProperty(PROP_AGENT_CONTROLLERS_PREFIX + name + PROP_AGENT_CONTROLLER_WEIGHT_SUFFIX,
                                                      defaultWeight);

                    if (weight <= 0)
                    {
                        throw new RuntimeException("The value of property '" + PROP_AGENT_CONTROLLERS_PREFIX + name +
                                                   PROP_AGENT_CONTROLLER_WEIGHT_SUFFIX + "' must be greater than 0.");
                    }

                    final boolean runsCPTests = getBooleanProperty(PROP_AGENT_CONTROLLERS_PREFIX + name +
                                                                   PROP_AGENT_CONTROLLER_CLIENTPERF_SUFFIX, defaultCP);

                    final AgentControllerConnectionInfo info = new AgentControllerConnectionInfo();
                    info.setUrl(url);
                    info.setWeight(weight);
                    info.setName(name);
                    info.setNumberOfAgents(agentCount);
                    info.setRunsClientPerformanceTests(runsCPTests);

                    infos.add(info);

                    urlToNameMap.put(urlString, name);
                }
            }
        }

        return infos;
    }

    /**
     * Returns whether to display detailed status information for each simulated test user, or whether status
     * information will be aggregated into one line per user type.
     * 
     * @return whether to show detailed information
     */
    public boolean getShowDetailedStatusList()
    {
        return showDetailedStatusList;
    }

    /**
     * Returns the number of seconds to wait before the status list is updated again.
     * 
     * @return the update interval
     */
    public int getStatusListUpdateInterval()
    {
        return statusListUpdateInterval;
    }

    /**
     * In case of initial connection problems with a agent controller the load of the test is distributed to the
     * remaining agent controllers if the connection is relaxed.
     * 
     * @return <code>true</code> if the agent controller connection is relaxed; <code>false</code> otherwise
     */
    public boolean isAgentControllerConnectionRelaxed()
    {
        return isAgentControllerConnectionRelaxed;
    }

    /**
     * Tells to use a proxy or not.
     * 
     * @return <code>true</code> if using a proxy is enabled explicitly; <code>false</code> otherwise
     */
    public boolean isHttpsProxyEnabled()
    {
        return isHttpsProxyEnabled;
    }

    /**
     * Returns the https proxy host.
     * 
     * @return https proxy host
     */
    public String getHttpsProxyHost()
    {
        return httpsProxyHost;
    }

    /**
     * Returns the https proxy port.
     * 
     * @return https proxy port
     */
    public String getHttpsProxyPort()
    {
        return httpsProxyPort;
    }

    /**
     * Returns the hosts to bypass the proxy connection.
     * 
     * @return hosts to bypass the proxy connection
     */
    public String getHttpsProxyBypassHosts()
    {
        return httpsProxyBypassHosts;
    }

    /**
     * Returns the default agent count.
     * 
     * @return default agent count
     */
    public int getDefaultAgentCount()
    {
        return defaultAgentCount;
    }

    /**
     * Returns the default agent controller weight.
     * 
     * @return default agent controller weight
     */
    public int getDefaultWeight()
    {
        return defaultWeight;
    }

    /**
     * Returns the number of maximum parallel agent controller communication limit
     * 
     * @return the number of maximum parallel agent controller communication limit
     */
    public int getParallelCommunicationLimit()
    {
        return parallelCommunicationLimit;
    }

    /**
     * Returns the number of maximum parallel uploads
     * 
     * @return the number of maximum parallel uploads
     */
    public int getParallelUploadLimit()
    {
        return parallelUploadLimit;
    }

    /**
     * Returns the number of maximum parallel downloads
     * 
     * @return the number of maximum parallel downloads
     */
    public int getParallelDownloadLimit()
    {
        return parallelDownloadLimit;
    }

    /**
     * Returns the configured agent-controller connection timeout.
     * 
     * @return agent-controller connection timeout
     */
    public int getAgentControllerConnectTimeout()
    {
        return acConnectTimeout;
    }

    /**
     * Returns the configured agent-controller read timeout.
     * 
     * @return agent-controller read timeout
     */
    public int getAgentControllerReadTimeout()
    {
        return acReadTimeout;
    }

    /**
     * Returns the configured agent controller initial response timeout.
     * 
     * @return agent controller initial response timeout
     */
    public int getAgentControllerInitialResponseTimeout()
    {
        return acInitialResponseTimeout;
    }

    /**
     * Returns the configured user name.
     * 
     * @return the user name
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * Returns the configured password.
     * 
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Returns the result output directory override as specified on command line.
     * 
     * @return result output directory override
     */
    public File getResultOutputDirectory()
    {
        return resultOutputDirectory;
    }

    /**
     * Sets the result output directory override. If the given directory name denotes a relative file then it will be
     * rooted at the test results root directory.
     * 
     * @param outputDirectory
     *            the result output directory name to use as override
     */
    public void setResultOutputDirectory(final String outputDirectory)
    {
        File outDir = new File(outputDirectory);
        if (!outDir.isAbsolute())
        {
            outDir = new File(testResultsRootDirectory, outputDirectory);
        }

        try
        {
            FileUtils.forceMkdir(outDir);
            com.xceptance.common.io.FileUtils.cleanDirRelaxed(outDir);
        }
        catch (final IOException ioe)
        {
            throw new RuntimeException("Failed to create or clean result output directory '" + outputDirectory + "'", ioe);
        }

        resultOutputDirectory = outDir;

    }

    /**
     * @return the isEmbedded
     */
    public boolean isEmbedded()
    {
        return isEmbedded;
    }

}
