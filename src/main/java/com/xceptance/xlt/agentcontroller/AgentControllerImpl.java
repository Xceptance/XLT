/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.agentcontroller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipOutputStream;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration.JupIOFactory;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.server.HessianServlet;
import com.xceptance.common.util.zip.ZipUtils;
import com.xceptance.xlt.agent.AgentInfo;
import com.xceptance.xlt.agentcontroller.ResultArchives.ArchiveToken;
import com.xceptance.xlt.agentcontroller.TestUserStatus.State;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.util.AgentControllerSystemInfo;
import com.xceptance.xlt.util.FileReplicationIndex;
import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 * The {@link AgentController} implementation.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class AgentControllerImpl implements AgentController
{
    private enum Status
    {
        NEW("Initialized"), UPLOADED("Uploaded"), RUNNING("Running"), FINISHED("Finished"), ABORTED("Aborted");

        private String s;

        private Status(final String s)
        {
            this.s = s;
        }

        @Override
        public String toString()
        {
            return s;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(AgentControllerImpl.class);

    private static final String LOCALHOST_NAME = "localhost";

    private static final String AGENT_RESULTS_FILE_PREFIX = "agentresults_";

    private static final String AGENT_RESULTS_FILE_EXTENSION = ".zip";

    /**
     * Agent-stopping thread used for VM shutdown.
     */
    private class ShutdownHook extends Thread
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            // stop all agents
            for (final AgentManager agentManager : getAgentManagers().values())
            {
                agentManager.stopAgent();
            }

            // remove agent file managers update directory
            if (agentFileManager != null)
            {
                try
                {
                    agentFileManager.clear();
                }
                catch (final IOException e)
                {
                    log.error("Unable to delete agent file manager update directory", e);
                }
            }
        }
    }

    /**
     * agent ID : agent manager
     */
    private final Map<String, AgentManager> agentManagers = new ConcurrentHashMap<String, AgentManager>();

    /**
     * The file manager used by this controller.
     */
    private FileManager fileManager;

    /**
     * The directory containing all the separate agent directories.
     */
    private File agentsDirectory;

    /**
     * Directory to store received files or files for upload.
     */
    private File transferDirectory;

    /**
     * Time difference between master controller and agent controller.
     */
    private long referenceTimeDifference = 0;

    private String[] agentBaseCommandLine;

    /**
     * Agent controller name.
     */
    private volatile String name;

    /**
     * Agent controller weight.
     */
    private volatile int weight = 0;

    /**
     * Agent count at this agent controller.
     */
    private volatile int agentCount = 0;

    /**
     * Number of first agent at this agent controller.
     */
    private volatile int agentBaseNumber = 0;

    private AgentFileManager agentFileManager;

    /**
     * The URL of the agent controller.
     */
    private volatile URL url;

    /**
     * The agent controller's temp directory.
     */
    private File tempDir;

    /**
     * Temporary test configuration archive file.
     */
    private File tempConfigArchiveFile;

    /**
     * The agent controller's status.<br>
     */
    private volatile Status status = Status.NEW;

    /**
     * Whether this agent controller runs client-performance tests.
     */
    private volatile boolean runsClientPerformanceTests;

    /**
     * Agent results archives
     */
    private final ResultArchives archives = new ResultArchives();

    /**
     * The agent controller configuration object.
     */
    private final AgentControllerConfiguration agentControllerConfig;

    /**
     * Is the file update done?
     */
    private volatile boolean isUpdateDone = false;

    /**
     * The exception that caused the update to fail (if any).
     */
    private volatile Exception updateException;

    /**
     * Creates an agent controller. The controller must get initialized.
     */
    public AgentControllerImpl(final Properties commandLineProperties) throws Exception
    {
        agentControllerConfig = new AgentControllerConfiguration(commandLineProperties);

        prepare();
        startServlet();
    }

    /**
     * @throws IOException
     *             if either there is a problem with the configuration file or there is a problem creating the agents
     *             directory
     */
    protected void prepare() throws IOException
    {
        agentsDirectory = agentControllerConfig.getAgentsDirectory();
        tempDir = agentControllerConfig.getTempDir();
        transferDirectory = tempDir;

        FileUtils.forceMkdir(agentsDirectory);

        final String[] agentCmdLine = agentControllerConfig.getAgentCommandLine();
        final int argSize = agentCmdLine.length;
        agentBaseCommandLine = new String[argSize];
        System.arraycopy(agentCmdLine, 0, agentBaseCommandLine, 0, argSize);

        // register shutdown-hook
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        // create file manager (for embedded use only)
        fileManager = new FileManagerImpl(transferDirectory);

        final File updateManagerDirectory = new File(getTransferDirectory(), "xltUpdate_" + UUID.randomUUID().toString());
        agentFileManager = new AgentFileManager(updateManagerDirectory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final String name, final URL url, final int weight, final int agentCount, final int agentBaseNumber,
                     final boolean runsClientPerformanceTests) throws IOException
    {
        this.weight = weight;
        this.agentCount = agentCount;
        this.agentBaseNumber = agentBaseNumber;
        this.name = name;
        this.url = url;
        this.runsClientPerformanceTests = runsClientPerformanceTests;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTotalAgentCount(final int totalAgentCount)
    {
        for (final AgentManager agentManager : getAgentManagers().values())
        {
            agentManager.setTotalAgentCount(totalAgentCount);
        }
    }

    /**
     * Start agent controller servlet.
     *
     * @throws Exception
     *             if anything goes wrong
     */
    protected void startServlet() throws Exception
    {
        log.info("start servlet");

        // create servlet engine
        final Server server = new Server();

        // create HTTPS connector
        final SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(agentControllerConfig.getKeyStoreFile().getPath());
        sslContextFactory.setKeyStorePassword(agentControllerConfig.getKeyStorePassword());
        sslContextFactory.setKeyManagerPassword(agentControllerConfig.getKeyPassword());

        final SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString());

        final HttpConfiguration httpsConfiguration = new HttpConfiguration();
        httpsConfiguration.addCustomizer(new SecureRequestCustomizer());

        final HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpsConfiguration);

        final ServerConnector sslConnector = new ServerConnector(server, sslConnectionFactory, httpConnectionFactory);
        sslConnector.setHost(agentControllerConfig.getHostName());
        sslConnector.setPort(agentControllerConfig.getPort());
        server.addConnector(sslConnector);

        // create root context
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);

        // create and configure servlets
        final FileManagerServlet fileManagerServlet = new FileManagerServlet(transferDirectory);
        context.addServlet(new ServletHolder(fileManagerServlet), FileManagerServlet.SERVLET_MAPPING);

        final HessianServlet hessianServlet = new HessianServlet();
        hessianServlet.setHome(this);
        context.addServlet(new ServletHolder(hessianServlet), "/" + AgentController.class.getName());

        server.setHandler(context);

        // set up basic authentication
        final String userName = agentControllerConfig.getUserName();
        final String password = agentControllerConfig.getPassword();
        if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password))
        {
            final String roleName = "user";
            final String realmName = "XLT Agent Controller";

            // add authentication
            final Constraint constraint = new Constraint(Constraint.__BASIC_AUTH, roleName);
            constraint.setAuthenticate(true);

            // map the security constraint to the root path
            final ConstraintMapping constraintMapping = new ConstraintMapping();
            constraintMapping.setConstraint(constraint);
            constraintMapping.setPathSpec("/*");

            // create the login service and add the only XLT user
            final SimpleLoginService loginService = new SimpleLoginService();
            loginService.setName(realmName);
            loginService.putUser(userName, Credential.getCredential(password), new String[]
                {
                    roleName
                });

            // create the security handler
            final ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
            securityHandler.setAuthenticator(new BasicAuthenticator());
            securityHandler.setRealmName(realmName);
            securityHandler.addConstraintMapping(constraintMapping);
            securityHandler.setLoginService(loginService);

            // finally enable security
            context.setSecurityHandler(securityHandler);
        }

        // Try to start the servlet engine
        try
        {
            server.start();
        }
        // servlet failed to start -> stop it immediately and propagate the error cause
        catch (final Exception e)
        {
            try
            {
                // NOTICE:
                // Stopping the servlet engine is necessary in order to stop all running threads which have been
                // started as part of its startup procedure.
                server.stop();
            }
            catch (final Exception ignored)
            {
                // Ignore it. We did our best.
            }

            throw e;
        }

        // modify the agent command line to use the actually chosen port (for the case the configured port was 0 -> ie.
        // choose a free port)
        agentBaseCommandLine[1] = Integer.toString(sslConnector.getLocalPort());
    }

    /**
     * {@inheritDoc}
     */
    protected void setupAgentManagers()
    {
        final int realAgentCountLength = String.valueOf(getAgentCount() - 1).length();
        final int usedAgentCountLength = realAgentCountLength > 2 ? realAgentCountLength : 2;

        final int maxAgentNumber = getAgentBaseNumber() + getAgentCount() - 1;

        final String[] baseCommandLine = getAgentBaseCommandLine();
        final int commandSize = baseCommandLine != null ? baseCommandLine.length : 0;
        final String agentHostname = getHostname();
        final String acRemoteAddress;
        {
            final URL acUrl = getUrl();
            if (acUrl != null)
            {
                acRemoteAddress = acUrl.getHost() + ":" + acUrl.getPort();
            }
            else if (baseCommandLine != null)
            {
                acRemoteAddress = agentHostname + ":" + baseCommandLine[1];
            }
            else
            {
                acRemoteAddress = null;
            }
        }
        final Set<String> agentManagers2close = new HashSet<String>(getAgentManagers().keySet());

        for (int agentNumber = getAgentBaseNumber(); agentNumber <= maxAgentNumber; agentNumber++)
        {
            // build agentIDs
            // an agent ID is build of the agent controller name and a zero based running number of agents
            final String agentID = getName() + "_" +
                                   StringUtils.leftPad(String.valueOf(agentNumber - getAgentBaseNumber()), usedAgentCountLength, '0');

            // if agent manager for ID is not known (number of agents might increase for further runs), create it
            if (!getAgentManagers().containsKey(agentID))
            {
                final File agentDirectory = new File(getAgentsDirectory(), agentID); // keep in mind that {@link
                                                                                     // #archiveAgentResults(TestResultAmount)}
                                                                                     // depends on that behavior
                final AgentInfo agentInfo = new AgentInfo(agentID, agentDirectory);

                // build command line
                final String[] currentCommandLine = new String[commandSize];
                if (commandSize >= 5)
                {
                    System.arraycopy(baseCommandLine, 0, currentCommandLine, 0, commandSize);
                    currentCommandLine[2] = agentID;
                    currentCommandLine[3] = agentHostname;
                    if (commandSize > 6)
                    {
                        currentCommandLine[6] = acRemoteAddress;
                    }
                }

                // create and register agent manager
                try
                {
                    final AgentListener listener = new AgentListener()
                    {
                        @Override
                        public void agentExitedUnexpectedly(final String agentID, final int exitCode)
                        {
                            if (!Status.ABORTED.equals(status))
                            {
                                log.error("Agent " + agentID + "@" + getHostname() + " exited unexpectedly (exit code: " + exitCode + ").");
                            }
                        }

                        @Override
                        public void agentStopped(final String agentID)
                        {
                            // ignore
                        }
                    };

                    final AgentManager agentManager = new AgentManagerImpl(agentInfo, currentCommandLine, listener);
                    setupEnvironment(agentManager);

                    getAgentManagers().put(agentID, agentManager);
                }
                catch (final Exception e)
                {
                    log.error("unable to create agent manager for agent ID " + agentID);
                }
            }
            else
            {
                // agent manager is needed so do not close it
                agentManagers2close.remove(agentID);
            }

            // update agent number
            getAgentManagers().get(agentID).setAgentNumber(agentNumber);
        }

        // close agent managers that are not needed anymore
        for (final String agentID : agentManagers2close)
        {
            final AgentManager agentManager = getAgentManagers().remove(agentID);
            try
            {
                agentManager.close();
            }
            catch (final IOException e)
            {
                log.warn("unable to close agent manager for ID " + agentID);
            }
        }
    }

    /**
     * Environment setup for the given agent manager.
     *
     * @param agentManager
     *            the agent manager whose environment should be set up
     * @throws IOException
     *             thrown on setup failure
     */
    protected void setupEnvironment(final AgentManager agentManager) throws IOException
    {
        agentManager.setupEnvironment();
    }

    /**
     * Returns the base number (or start index) of all the agents managed by this agent controller.
     *
     * @return the base number
     */
    protected int getAgentBaseNumber()
    {
        return agentBaseNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAgentCount()
    {
        return agentCount;
    }

    /**
     * Returns the agent manager for appropriate agent ID.
     */
    protected AgentManager getAgentManager(final String agentID)
    {
        return getAgentManagers().get(agentID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileManager getFileManager()
    {
        return fileManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWeight()
    {
        return weight;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReferenceTime(final long time)
    {
        referenceTimeDifference = time - System.currentTimeMillis();

        log.debug("Difference between master controller's time and local time: " + referenceTimeDifference);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getReferenceTimeDifference()
    {
        return referenceTimeDifference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return getName();
    }

    /**
     * @return the agents directory
     */
    protected File getAgentsDirectory()
    {
        return agentsDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TestUserConfiguration> getAgentLoadProfile(final String agentID)
    {
        final AgentManager agentManager = getAgentManager(agentID);
        if (agentManager != null)
        {
            return agentManager.getAgentLoadProfile();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<AgentStatus> getAgentStatus()
    {
        final Set<AgentStatus> result = new HashSet<AgentStatus>();

        for (final AgentManager agentManager : getAgentManagers().values())
        {
            final AgentStatus status = agentManager.getAgentStatus();
            if (status != null)
            {
                result.add(status);
            }
        }

        return result;
    }

    /**
     * @return the transferDirectory
     */
    protected File getTransferDirectory()
    {
        return transferDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasRunningAgent()
    {
        boolean result = false;
        for (final AgentManager agentManager : getAgentManagers().values())
        {
            if (agentManager.isAgentRunning())
            {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startAgents(final Map<String, List<TestUserConfiguration>> loadProfiles)
    {
        log.info("Start agents ...");

        for (final Entry<String, AgentManager> itemEntry : getAgentManagers().entrySet())
        {
            final String agentID = itemEntry.getKey();
            final AgentManager agentManager = itemEntry.getValue();

            log.debug("Starting process: " + StringUtils.join(agentManager.getCommandLine(), ' '));
            final List<TestUserConfiguration> loadProfile = loadProfiles.get(agentID);
            if (loadProfile != null && !loadProfile.isEmpty())
            {
                try
                {
                    agentManager.startAgent(loadProfile);
                }
                catch (final Exception ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        }
        status = Status.RUNNING;
        log.info("Agents started");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopAgents()
    {
        log.info("Stopping agents ...");
        if (hasRunningAgent())
        {
            status = Status.ABORTED;
        }
        for (final AgentManager agentManager : getAgentManagers().values())
        {
            agentManager.stopAgent();
        }
        log.info("Agents stopped");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void archiveAgentResults(final TestResultAmount testResultAmount)
    {
        // archive when this is job not in progress yet
        final ArchiveToken archiveToken = archives.requestCreating();
        if (archiveToken != null)
        {
            // start archiving nonblocking
            new Thread()
            {
                @Override
                public void run()
                {
                    log.info("Archive agent results ...");
                    try
                    {
                        // zip results of all known agents
                        if (!getAgentIDs().isEmpty())
                        {
                            for (final String agentID : getAgentIDs())
                            {
                                try
                                {
                                    // prepare archive file
                                    final File zipFile = getTempFile(agentID);
                                    zipFile.deleteOnExit();

                                    // archive results
                                    final boolean success = getAgentManager(agentID).archiveAgentResults(testResultAmount, zipFile);
                                    if (success)
                                    {
                                        // publish archive
                                        if (!archives.update(agentID, zipFile, archiveToken))
                                        {
                                            // update fails if token is not valid
                                            break;
                                        }
                                    }
                                }
                                catch (final Exception e)
                                {
                                    log.warn("Unable to provide results file for agent ID " + agentID, e);
                                }
                            }
                        }
                        else
                        {
                            // FALLBACK
                            // In case of restarted agent controller, the index of used agents is lost.
                            final File[] agentDirectories = agentsDirectory.listFiles((FileFilter) FileFilterUtils.directoryFileFilter());
                            if (agentDirectories != null)
                            {
                                for (final File directory : agentDirectories)
                                {
                                    final File resultsDirectory = new File(directory, AgentInfo.NAME_RESULTS_DIR);
                                    if (resultsDirectory.isDirectory())
                                    {
                                        // We have to parse them back from directory naming.
                                        // This approach assumes that the directory is named as the agent ID {@see
                                        // #setupAgentManagers()}.
                                        final String agentID = directory.getName();

                                        try
                                        {
                                            final File zipFile = getTempFile(agentID);
                                            final boolean success = AgentManagerImpl.archiveAgentResults(resultsDirectory, testResultAmount,
                                                                                                         zipFile, agentID);
                                            if (success)
                                            {
                                                // publish archive
                                                if (!archives.update(agentID, zipFile, archiveToken))
                                                {
                                                    // update fails if token is not valid
                                                    break;
                                                }
                                            }
                                        }
                                        catch (final Exception e)
                                        {
                                            log.warn("Unable to provide results file for agent ID " + agentID, e);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    finally
                    {
                        // the job is done
                        log.debug("Remove old agent results archives");
                        archives.setReadyForDownload(archiveToken);
                    }
                }
            }.start();
        }
    }

    private File getTempFile(final String coreName) throws IOException
    {
        return File.createTempFile(AGENT_RESULTS_FILE_PREFIX + coreName + "_", AGENT_RESULTS_FILE_EXTENSION, tempDir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAgentIDs()
    {
        return getAgentManagers().keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHostname()
    {
        return StringUtils.defaultString(agentControllerConfig.getHostName(), LOCALHOST_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getUrl()
    {
        return url;
    }

    protected String[] getAgentBaseCommandLine()
    {
        return agentBaseCommandLine;
    }

    protected Map<String, AgentManager> getAgentManagers()
    {
        return agentManagers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileReplicationIndex getAgentFilesIndex() throws IOException
    {
        setupAgentManagers();

        // remove results directories of all agents
        for (final String agentID : getAgentIDs())
        {
            getAgentManager(agentID).removeResultsDirectory();
        }

        // return the files index of the first agent
        final FileReplicationIndex fri = agentFileManager.getAgentFilesIndex();

        // return the file index
        // in case of no agent returned a valid file index, return an empty index
        return fri != null ? fri : new FileReplicationIndex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAgentFiles(final String agentFilesZipFileName, final List<File> filesToBeDeleted) throws IOException
    {
        isUpdateDone = false;
        updateException = null;

        // start update non-blocking
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    log.debug("Remove old agent results archives");
                    archives.clear();

                    if (tempConfigArchiveFile != null)
                    {
                        FileUtils.deleteQuietly(tempConfigArchiveFile);
                    }
                    tempConfigArchiveFile = null;

                    final File zipFile = new File(getTransferDirectory(), agentFilesZipFileName);
                    log.info("Update agent files ...");

                    // update files and synch all agents
                    agentFileManager.updateAgentFiles(zipFile, filesToBeDeleted, getAgentManagers().values());

                    log.info("Remove orphaned agent directories");
                    // remove unused agent directories (might be left from further runs)
                    final String[] filenames = getAgentsDirectory().list(FileFilterUtils.makeSVNAware(null));
                    if (filenames != null)
                    {
                        for (final String filename : filenames)
                        {
                            // agent directories are named like their agent ID
                            if (!getAgentIDs().contains(filename))
                            {
                                FileUtils.deleteQuietly(new File(getAgentsDirectory(), filename));
                            }
                        }
                    }

                    log.info("Update of agent files finished");

                    log.debug("Clean up agent files");
                    FileUtils.deleteQuietly(zipFile);

                    status = Status.UPLOADED;
                }
                catch (final Exception e)
                {
                    log.error("Failed to update agent files", e);

                    // remember this exception, it will be reported to the master controller later on
                    updateException = e;
                }
                finally
                {
                    isUpdateDone = true;
                }
            }
        }.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUpdateDone() throws Exception
    {
        // if we had an exception while updating report it now
        if (updateException != null)
        {
            throw updateException;
        }

        return isUpdateDone;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUpdateAcknowledged()
    {
        isUpdateDone = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAgentStatus(final AgentStatus agentStatus)
    {
        getAgentManager(agentStatus.getAgentID()).setAgentStatus(agentStatus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetAgentsStatus()
    {
        log.debug("Reset agent statuses");
        for (final AgentManager agentManager : getAgentManagers().values())
        {
            agentManager.resetAgentStatus();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String archiveTestConfig()
    {
        if (tempConfigArchiveFile == null || !tempConfigArchiveFile.exists())
        {
            final String confDirPath = "./" + XltConstants.RESULT_CONFIG_DIR;
            final File[] agentDirectories = getAgentsDirectory().listFiles((FileFilter) FileFilterUtils.makeSVNAware(FileFilterUtils.directoryFileFilter()));
            if (agentDirectories != null)
            {
                for (final File directory : agentDirectories)
                {
                    final File configDirectory = new File(directory, XltConstants.CONFIG_DIR_NAME);
                    if (configDirectory.isDirectory() && configDirectory.canRead())
                    {
                        final IOFileFilter propertiesFilesFilter = FileFilterUtils.suffixFileFilter(XltConstants.PROPERTY_FILE_EXTENSION);
                        final IOFileFilter cfgFilesFilter = FileFilterUtils.suffixFileFilter(XltConstants.CFG_FILE_EXTENSION);
                        final IOFileFilter xmlFilesFilter = FileFilterUtils.suffixFileFilter(XltConstants.XML_FILE_EXTENSION);
                        final IOFileFilter extensionFilter = FileFilterUtils.or(propertiesFilesFilter, cfgFilesFilter, xmlFilesFilter);
                        final IOFileFilter filter = FileFilterUtils.and(FileFileFilter.FILE, extensionFilter);

                        ZipOutputStream out = null;
                        try
                        {
                            final File tempfile = File.createTempFile("testconfig-", ".zip", getTransferDirectory());
                            tempfile.deleteOnExit();

                            /*
                             * The property files are stored under config as we have to clearly distinguish them from
                             * result data, however one might put a sub folder in the config directory and name it for
                             * example embedded_00 ... . Furthermore one might name a property files with embedded_00.
                             */
                            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tempfile)));
                            ZipUtils.addDirectoryEntry(out, confDirPath);
                            addMaskedDirectory(out, configDirectory, filter, new File(confDirPath));
                            addIncludedPropertyFiles(out, configDirectory, confDirPath);

                            tempConfigArchiveFile = tempfile;
                            break;
                        }
                        catch (final Exception e)
                        {
                            log.warn("Unable to archive test configuration", e);
                        }
                        finally
                        {
                            IOUtils.closeQuietly(out);
                        }
                    }
                }
            }
        }
        return tempConfigArchiveFile != null ? tempConfigArchiveFile.getName() : null;
    }

    /**
     * Adds the properties files contained contained in the given config directory to the given ZIP output
     * stream with their secret properties masked.
     *
     * @param out The ZipOutputStream to write the data to
     * @param configDirectory The input directory containing the configuration files
     * @param filter The FileFilter determining which files to include in the output
     * @param configPath The relative path inside the ZIP file
     * @throws IOException
     * @throws ConfigurationException
     */
    private void addMaskedDirectory(ZipOutputStream out, File configDirectory, IOFileFilter filter, File configPath) throws IOException, ConfigurationException
    {
        final File tempDir = Files.createTempDirectory("masked").toFile();
        try
        {
            FileUtils.copyDirectory(configDirectory, tempDir, filter);
            for (final File fileToMask : FileUtils.listFiles(tempDir, null, true))
            {
                if (fileToMask.getAbsolutePath().endsWith(XltConstants.PROPERTY_FILE_EXTENSION))
                {
                    maskFile(fileToMask, fileToMask);
                }
            }
            ZipUtils.zipDirectory(out, tempDir, filter, configPath);
        }
        finally
        {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    /**
     * Mask all properties in the given file and write the output to the given output file.
     * The input file is guaranteed to be closed before starting to write the output file, so that
     * the input file can be overwritten with a masked version, if desired.
     *
     * @param inputFile The input file to mask.
     * @param outputFile The output file to write the masked data to.
     * @throws IOException
     * @throws ConfigurationException
     */
    private static void maskFile(File inputFile, File outputFile) throws ConfigurationException, IOException
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.setIOFactory(new JupIOFactory()); // for better compatibility with java.util.Properties (GH#144)
        try (final FileReader reader = new FileReader(inputFile))
        {
            config.read(reader);
        }
        config = mask(config, inputFile.getName().equals(XltConstants.SECRET_PROPERTIES_FILENAME));
        final StringWriter writer = new StringWriter();
        config.write(writer);
        FileUtils.writeStringToFile(outputFile, writer.toString(), StandardCharsets.ISO_8859_1);
    }

    /**
     * Mask secret properties in the given configuration
     *
     * @param config The configuration to mask the secret props in
     * @return A copy of the new config with the secret values replaced
     */
    private static PropertiesConfiguration mask(final PropertiesConfiguration config, boolean maskAll)
    {
        Iterator<String> keys = config.getKeys();
        final PropertiesConfiguration output = (PropertiesConfiguration) config.clone();
        while (keys.hasNext())
        {
            final String key = keys.next();
            if (maskAll || key.startsWith(XltConstants.SECRET_PREFIX))
            {
                output.setProperty(key, XltConstants.MASK_PROPERTIES_HIDETEXT);
            }
        }
        return output;
    }

    /**
     * Adds the property files which are included by &quot;include&quot; properties to the argument stream. However all
     * regular files which are directly in the config directory are skipped as they are already contained in the stream.
     * Another attempt to add them will result in an exception.
     *
     * @param out
     *            the stream to which to add the content
     * @param configDirectory
     *            the config directory of the test suite (more formerly on the corresponding agent)
     * @param confDirPath
     *            the config directory path
     * @throws IOException
     */
    private void addIncludedPropertyFiles(final ZipOutputStream out, final File configDirectory, final String confDirPath)
        throws IOException
    {
        final List<String> resolvedPropertyFiles;
        try
        {
            final FileObject configDir = VFS.getManager().resolveFile(configDirectory.getAbsolutePath());
            final XltPropertiesImpl props = new XltPropertiesImpl(configDir.getParent(), configDir, true);
            resolvedPropertyFiles = props.getResolvedPropertyFiles();
        }
        catch (final Throwable ex)
        {
            log.error("Failed to determine resolved property includes", ex);
            return;
        }

        final int confDirAncestors = com.xceptance.common.io.FileUtils.getNumberOfAncestors(configDirectory);
        /*
         * Here we collect files that we have already created. This is to avoid duplicate attempts to create the same
         * sub directory for example if we have ...include.1=foo/bar/smallLoad.properties and
         * ...include.2=foo/development.credentials we would otherwise try to create the directory "foo" twice and get
         * an exception.
         */
        final Set<String> added = new HashSet<String>();
        /*
         * Avoid attempts to create the config folder again if someone uses something like
         * ...include.1=../../smallLoad.properties. We use the canonical path as the same file may have different
         * absolute or relative paths. For example a file fi may have config/smallLoad/foo.properties and
         * config/smallLoad/sub/../foo.properties as absolute path. Different instances of this file with the different
         * absolute path won't be considered equal by Java. Thus we use the canonical path.
         */
        added.add(configDirectory.getCanonicalPath());
        for (int i = 0; i < resolvedPropertyFiles.size(); i++)
        {
            final String path = resolvedPropertyFiles.get(i);
            final File current = new File(configDirectory, path);
            final String currentCanonicalPath = current.getCanonicalPath();
            final int currentAncestors = com.xceptance.common.io.FileUtils.getNumberOfAncestors(current);
            if (!current.exists() || added.contains(currentCanonicalPath) || current.getParentFile().equals(configDirectory) ||
                confDirAncestors > currentAncestors)
            {
                continue;
            }

            addParentDirectories(current, confDirAncestors, added, out);

            added.add(currentCanonicalPath);
            // add current regular file to zip
            ZipUtils.addRegularFile(out, current, confDirPath.concat("/").concat(path).replace('\\', '/'));
        }
    }

    /**
     * Adds all parent directories of the argument file to the argument stream except the top most ones to skip. Adds
     * the canonical path of each added parent directory to the argument set. Does not add any contents within these
     * directories to the argument stream or set.
     */
    private void addParentDirectories(final File current, final int topMostOnesToSkip, final Set<String> added, final ZipOutputStream out)
        throws IOException
    {
        final List<File> parents = com.xceptance.common.io.FileUtils.getParents(current, topMostOnesToSkip);
        String relPath = ".";
        for (int j = 0; j < parents.size(); j++)
        {
            final File fi = parents.get(j);
            relPath += "/" + fi.getName();

            final String fiCanonicalPath = fi.getCanonicalPath();
            if (added.contains(fiCanonicalPath))
            {
                continue;
            }
            added.add(fiCanonicalPath);
            // add directory to zip;
            ZipUtils.addDirectoryEntry(out, relPath.replace('\\', '/'));
        }
    }

    @Override
    public void ping()
    {
        return;
    }

    @Override
    public AgentControllerSystemInfo info()
    {
        // if test cases were started but not stopped and not currently running the must be finished
        if (status.equals(Status.RUNNING) && !hasRunningAgent())
        {
            boolean hasAborted = false;
            final Set<AgentStatus> agentStatuses = getAgentStatus();
            for (final AgentStatus agentStatus : agentStatuses)
            {
                final List<TestUserStatus> testUserStatuses = agentStatus.getTestUserStatusList();
                for (final TestUserStatus testUserStatus : testUserStatuses)
                {
                    if (testUserStatus.getState().equals(State.Aborted))
                    {
                        hasAborted = true;
                        break;
                    }
                }
                if (hasAborted)
                {
                    break;
                }
            }

            status = hasAborted ? Status.ABORTED : Status.FINISHED;
        }

        final AgentControllerSystemInfo acSysInfo = new AgentControllerSystemInfo();
        acSysInfo.setStatus(status.toString());
        acSysInfo.setTime(System.currentTimeMillis());

        return acSysInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getAgentResultsArchives()
    {
        return archives.getArchives();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isArchiveAvailable()
    {
        return archives.getState().equals(ResultArchives.State.READY_FOR_DOWNLOAD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void archiveDownloadDone()
    {
        log.debug("Clear agent results archives");
        archives.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean runsClientPerformanceTests()
    {
        return runsClientPerformanceTests;
    }
}
