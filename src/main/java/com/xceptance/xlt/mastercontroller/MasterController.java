/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration.JupIOFactory;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.agentcontroller.AgentController;
import com.xceptance.xlt.agentcontroller.TestResultAmount;
import com.xceptance.xlt.agentcontroller.TestUserConfiguration;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.util.TimerUtils;
import com.xceptance.xlt.report.ReportGenerator;
import com.xceptance.xlt.util.AgentControllerException;
import com.xceptance.xlt.util.ConcurrencyUtils;
import com.xceptance.xlt.util.FailedAgentControllerCollection;
import com.xceptance.xlt.util.FileReplicationIndex;
import com.xceptance.xlt.util.FileReplicationUtils;
import com.xceptance.xlt.util.ProgressBar;
import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 */
public class MasterController
{
    /**
     * The log facility of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MasterController.class);

    /**
     * A file filter that accepts all files/directories that are visible and are not named "results". Used when
     * copying/uploading a test suite.
     */
    private static final FileFilter FILE_FILTER = FileFilterUtils.and(HiddenFileFilter.VISIBLE,
                                                                      FileFilterUtils.notFileFilter(new NameFileFilter(XltConstants.RESULT_ROOT_DIR)));

    /**
     * All agent controllers known to this master controller. The key is the agent controller's name.
     */
    private final Map<String, AgentController> agentControllerMap;

    /**
     * The directory where the agent files are stored in.
     */
    private final File agentFilesDirectory;

    /**
     * The current load profile. Changes only after uploading agent files.
     */
    private TestLoadProfileConfiguration currentLoadProfile;

    /**
     * The name of the test that is currently running. Changes with every started test.
     */
    private String currentTestCaseName;

    /**
     * The current test result directory. Changes when downloading test results.
     */
    private File currentTestResultsDir;

    /**
     * The list with all agent controllers known to this master controller.
     */
    private final TestDeployer testDeployer;

    /**
     * The master controller's temp directory.
     */
    private final File tempDirectory;

    /**
     * The user interface this master controller will interact with.
     */
    private MasterControllerUI userInterface;

    /**
     * Test comment.
     */
    private String testComment;

    /**
     * The name of the file with test-run specific properties.
     */
    private final String propertiesFileName;

    /**
     * Is the agent controller connection relaxed? If yes, broken connections get skipped.
     */
    private final boolean isAgentControllerConnectionRelaxed;

    /**
     * ID of timezone to use for report generation.
     */
    private final String reportTimezoneId;

    private final boolean isEmbedded;

    private final ThreadPoolExecutor defaultExecutor;

    private final ThreadPoolExecutor uploadExecutor;

    private final ThreadPoolExecutor downloadExecutor;

    /**
     * The root directory where new directories with test results are to be stored in.
     */
    private final File testResultsRootDirectory;

    /**
     * Result output directory override as specified on command line.
     */
    private final File resultOutputDirectory;

    /**
     * Whether or not the test was explicitly stopped by the user.
     */
    private boolean stoppedByUser = false;

    /**
     * Keep timer files compressed after download
     */
    private boolean compressedTimerFiles = false;

    /**
     * The status update facility that periodically queries the status of all agent controllers while a load test is
     * running.
     */
    private final AgentControllerStatusUpdater agentControllerStatusUpdater;

    /**
     * Creates a new MasterController object.
     *
     * @param agentControllerMap
     *            the list of agent controllers
     * @param config
     *            the master controller configuration
     * @param testPropertyFileName
     *            the name of the file with test-run specific properties
     * @param isAgentControllerConnectionRelaxed
     *            is agent controller connection relaxed
     * @param reportTimezoneId
     *            the ID of the timezone to use for report generation (may be <code>null</code>)
     */
    public MasterController(final Map<String, AgentController> agentControllerMap, final MasterControllerConfiguration config,
                            final String testPropertyFileName, final boolean isAgentControllerConnectionRelaxed,
                            final String reportTimezoneId)
    {
        this.agentControllerMap = agentControllerMap;

        testDeployer = new TestDeployer(agentControllerMap);
        agentFilesDirectory = config.getAgentFilesDirectory();
        tempDirectory = config.getTempDirectory();
        propertiesFileName = testPropertyFileName;
        this.isAgentControllerConnectionRelaxed = isAgentControllerConnectionRelaxed;
        this.reportTimezoneId = reportTimezoneId;

        final int parallelCommunicationLimit = config.getParallelCommunicationLimit();
        final int parallelUploadLimit = config.getParallelUploadLimit();
        final int parallelDownloadLimit = config.getParallelDownloadLimit();

        defaultExecutor = ConcurrencyUtils.getNewThreadPoolExecutor("AC-default-pool-", parallelCommunicationLimit);
        uploadExecutor = ConcurrencyUtils.getNewThreadPoolExecutor("AC-upload-pool-", parallelUploadLimit);
        downloadExecutor = ConcurrencyUtils.getNewThreadPoolExecutor("AC-download-pool-", parallelDownloadLimit);

        testResultsRootDirectory = config.getTestResultsRootDirectory();
        resultOutputDirectory = config.getResultOutputDirectory();

        isEmbedded = config.isEmbedded();
        compressedTimerFiles = config.isCompressedTimerFiles();

        checkTestPropertiesFileName();

        agentControllerStatusUpdater = new AgentControllerStatusUpdater(defaultExecutor);
    }

    /**
     * Checks if the passed test properties file name is valid.
     *
     * @throws IllegalArgumentException
     *             thrown if file path is absolute, does not exist, cannot be read or does not reside in test suite's
     *             configuration directory.
     */
    private void checkTestPropertiesFileName()
    {
        // check if there is something to do
        if (StringUtils.isBlank(propertiesFileName))
        {
            return;
        }

        // test properties files must not be absolute
        if (new File(propertiesFileName).isAbsolute())
        {
            final String msg = "Parameter '%s' is invalid, because its value is not a relative path -> [%s]";
            throw new IllegalArgumentException(String.format(msg, "testPropertiesFile", propertiesFileName));
        }

        final File agentConfDir = new File(agentFilesDirectory, "config");
        final File testPropFile = new File(agentConfDir, propertiesFileName);

        // check if file exists and can be read
        ParameterCheckUtils.isReadableFile(testPropFile, "testPropertiesFile");

        // no check if test suite's configuration directory contains specified properties file
        boolean valid = false;
        try
        {
            valid = FileUtils.directoryContains(agentConfDir, testPropFile);
        }
        catch (final IOException ioe)
        {
        }

        if (!valid)
        {
            final String msg = "Parameter '%s' is invalid, because its value does not point to a file inside directory '%s' -> [%s]";
            throw new IllegalArgumentException(String.format(msg, "testPropertiesFile", agentConfDir.getAbsolutePath(),
                                                             propertiesFileName));
        }
    }

    /**
     * Downloads the test results from all configured agent controllers at once.
     *
     * @param testResultAmount
     *            the amount of test result data to download
     * @return true if the operation was successful for ALL known agent controllers; false otherwise
     */
    public boolean downloadTestResults(final TestResultAmount testResultAmount)
    {
        currentTestResultsDir = resultOutputDirectory;
        if (currentTestResultsDir == null)
        {
            currentTestResultsDir = getTestResultsDirectory(testResultsRootDirectory, currentTestCaseName);
        }

        // If the test is still running we will tag the directory as "intermediate results"
        if (!stoppedByUser && isAnyAgentRunning_SAFE())
        {
            final String intermediateResultsPath = currentTestResultsDir.getPath() + "-intermediate";
            currentTestResultsDir = new File(intermediateResultsPath);
        }

        final ArrayList<AgentController> agentControllers = new ArrayList<>(agentControllerMap.values());
        final int agentControllerSize = agentControllers.size();

        // Progress count
        //
        // test config : 3
        // time data : agentControllers.size() + 1
        // archiving : agentControllers.size()
        // archive download : 5 * agentControllers.size()
        // --
        // sum : 7 * agentControllers.size() + 4
        final ProgressBar progress = startNewProgressBar(agentControllerSize > 0 ? 7 * agentControllerSize + 4 : 0);

        // download results
        final ResultDownloader resultDownloader = new ResultDownloader(downloadExecutor, currentTestResultsDir, tempDirectory,
                                                                       agentControllers, progress);
        final boolean downloadSuccess = resultDownloader.download(testResultAmount, compressedTimerFiles);

        // inform user
        final FailedAgentControllerCollection failedAgentControllers = resultDownloader.getFailedAgentControllerCollection();
        userInterface.testResultsDownloaded(failedAgentControllers);

        // We have downloaded results successfully
        // AND
        // We have either no failed agentcontroller OR at least 1 agent controller succeeded in case of relaxed
        // connection
        return downloadSuccess && (failedAgentControllers.isEmpty() ||
                                   (isAgentControllerConnectionRelaxed && failedAgentControllers.getMap().size() < agentControllerSize));
    }

    /**
     * Generates the test report from the test results downloaded last.
     *
     * @param reportCreationType
     *            report creation type
     * @return true if the operation was successful; false otherwise
     */
    public boolean generateReport(final ReportCreationType reportCreationType)
    {
        boolean result = false;

        if (currentTestResultsDir != null)
        {
            final TimeZone systemTZ = TimeZone.getDefault();
            final TimeZone tz = reportTimezoneId != null ? TimeZone.getTimeZone(reportTimezoneId) : systemTZ;
            final boolean overrideTZ = !systemTZ.equals(tz);

            try
            {
                if (overrideTZ)
                {
                    TimeZone.setDefault(tz);
                }

                final FileObject testResultDir = VFS.getManager().resolveFile(currentTestResultsDir.toURI().toString());
                final ReportGenerator reportGenerator = new ReportGenerator(testResultDir, null, false);

                // get limit time range if necessary
                if (reportCreationType.equals(ReportCreationType.ALL))
                {
                    reportGenerator.generateReport(false);
                    result = true;
                }
                else if (reportCreationType.equals(ReportCreationType.NO_RAMPUP))
                {
                    reportGenerator.generateReport(true);
                    result = true;
                }
            }
            catch (final Exception ex)
            {
                LOG.error("Failed to generate report from the results in " + currentTestResultsDir, ex);
            }
            finally
            {
                if (overrideTZ)
                {
                    TimeZone.setDefault(systemTZ);
                }
            }
        }
        else
        {
            LOG.error("There are no downloaded results to generate a report from.");
        }

        return result;
    }

    /**
     * Ping the agent controllers.
     *
     * @return the ping results keyed by agent controller name
     */
    public Map<String, PingResult> pingAgentControllers()
    {
        final Map<String, PingResult> pingResults = Collections.synchronizedMap(new TreeMap<>());

        // ping agent controllers
        final CountDownLatch latch = new CountDownLatch(agentControllerMap.size());
        for (final AgentController agentcontroller : agentControllerMap.values())
        {
            defaultExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    PingResult pingResult;

                    try
                    {
                        final long pingStartTime = TimerUtils.getHighPrecisionTimer().getStartTime();
                        agentcontroller.ping();
                        final long pingTime = TimerUtils.getHighPrecisionTimer().getElapsedTime(pingStartTime);

                        pingResult = new PingResult(pingTime);
                    }
                    catch (final Exception e)
                    {
                        LOG.error("Failed to ping agent controller: " + agentcontroller, e);
                        pingResult = new PingResult(e);
                    }

                    pingResults.put(agentcontroller.toString(), pingResult);

                    latch.countDown();
                }
            });
        }

        try
        {
            latch.await();
        }
        catch (final InterruptedException e)
        {
            LOG.error("Waiting for ping results to complete has failed", e);
        }

        return pingResults;
    }

    /**
     * Print the current agent controller information.
     *
     * @return <code>true</code> if there are agent controller information, <code>false</code> otherwise
     */
    public AgentControllersInformation getAgentControllerInformation()
    {
        return new AgentControllersInformation(agentControllerMap.values(), defaultExecutor);
    }

    /**
     * Returns the names of the test cases, which are active in the current load profile.
     *
     * @return the test case names
     */
    public Set<String> getActiveTestCaseNames()
    {
        if (currentLoadProfile == null)
        {
            return Collections.emptySet();
        }

        return currentLoadProfile.getActiveTestCaseNames();
    }

    public TestLoadProfileConfiguration getCurrentLoadProfile()
    {
        return currentLoadProfile;
    }

    /**
     * Creates a test deployment for the given load profile. If parameter testCaseName is non-null, the load profile is
     * modified such that only the given test is included in the load profile.
     *
     * @param loadProfile
     *            the load profile
     * @param testCaseName
     *            the test case, or <code>null</code> to include all tests
     * @return the test deployment
     */
    private TestDeployment getTestDeployment(TestLoadProfileConfiguration loadProfile, final String testCaseName)
    {
        if (testCaseName != null)
        {
            loadProfile = loadProfile.getTestLoadProfileConfiguration(testCaseName);
        }

        final TestDeployment testDeployment = testDeployer.createTestDeployment(loadProfile);
        // log.debug("Test Deployment:\n" + testDeployment);

        return testDeployment;
    }

    /**
     * Creates a sub directory, named after the current date and time as well as the current test case, in the given
     * directory.
     *
     * @param testResultsRootDir
     *            the root directory
     * @param testCaseName
     *            the name of the active test case
     * @return the new sub directory
     */
    private File getTestResultsDirectory(final File testResultsRootDir, final String testCaseName)
    {
        String dirName = new SimpleDateFormat(XltConstants.DIRECTORY_DATE_FORMAT).format(new Date());

        // append the test case name if we have one
        if (testCaseName != null)
        {
            dirName = dirName + "-" + testCaseName;
        }

        return new File(testResultsRootDir, dirName);
    }

    /**
     * Returns the current user interface.
     *
     * @return the user interface
     */
    public MasterControllerUI getUserInterface()
    {
        return userInterface;
    }

    /**
     * Checks if the agent controllers do respond.
     *
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent site</li>
     *             </ul>
     * @throws IllegalStateException
     *             if unreachable instances are tolerated but no agent controller is connectable
     */
    public void checkAlive() throws AgentControllerException
    {
        LOG.debug("Check if agents are alive");

        final Map<AgentController, Future<Boolean>> agentFutures = getAgentRunningState();

        final FailedAgentControllerCollection failed = new FailedAgentControllerCollection();
        for (final Map.Entry<AgentController, Future<Boolean>> agentFuture : agentFutures.entrySet())
        {
            try
            {
                agentFuture.getValue().get();
            }
            catch (final InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (final Exception e)
            {
                failed.add(agentFuture.getKey(), e);
                LOG.error("Agentcontroller is not alive: " + agentFuture.getKey());
            }
        }

        checkSuccess(failed, true);
    }

    /**
     * Call {@link AgentController#hasRunningAgent()} concurrently for every agent controller. Nevertheless this method
     * is blocking until all agent controllers have sent a response or timed out.
     *
     * @return query futures; call {@link Future#get()} to receive the running state
     */
    public Map<AgentController, Future<Boolean>> getAgentRunningState()
    {
        final CountDownLatch latch = new CountDownLatch(agentControllerMap.size());
        final Map<AgentController, Future<Boolean>> agentFutures = new HashMap<>();
        for (final AgentController agentcontroller : agentControllerMap.values())
        {
            agentFutures.put(agentcontroller, defaultExecutor.submit(new Callable<Boolean>()
            {
                @Override
                public Boolean call() throws Exception
                {
                    boolean hasRunningAgent;
                    try
                    {
                        hasRunningAgent = agentcontroller.hasRunningAgent();
                    }
                    finally
                    {
                        latch.countDown();
                    }
                    return hasRunningAgent;
                }
            }));
        }

        try
        {
            latch.await();
        }
        catch (final InterruptedException e)
        {
            LOG.error("Waiting for agents running check to complete has failed", e);
        }

        return agentFutures;
    }

    /**
     * Checks whether there is at least one responding agent controller with a running agent.
     *
     * @return <code>true</code> if all agent controllers are responsive and there is at least 1 running agent;
     *         <code>false</code> otherwise
     * @throws AgentControllerException
     *             if not {@link #isAgentControllerConnectionRelaxed} and there is a connection problem with an agent
     *             controller
     */
    public boolean isAnyAgentRunning() throws AgentControllerException
    {
        final FailedAgentControllerCollection failedAgentControllers = new FailedAgentControllerCollection();

        final Map<AgentController, Future<Boolean>> agentFutures = getAgentRunningState();
        final AtomicBoolean result = new AtomicBoolean();
        for (final Map.Entry<AgentController, Future<Boolean>> agentFuture : agentFutures.entrySet())
        {
            try
            {
                if (agentFuture.getValue().get())
                {
                    result.set(true);
                }
            }
            catch (final InterruptedException e)
            {
                // ignore
            }
            catch (final ExecutionException e)
            {
                failedAgentControllers.add(agentFuture.getKey(), e);
            }
        }

        checkSuccess(failedAgentControllers, true);

        return result.get();
    }

    /**
     * Checks whether there is at least one responding agent controller with a running agent.
     *
     * @return <code>true</code> if there is a running agent; <code>false</code> otherwise
     */
    public boolean isAnyAgentRunning_SAFE()
    {
        final Map<AgentController, Future<Boolean>> agentFutures = getAgentRunningState();

        for (final Map.Entry<AgentController, Future<Boolean>> agentFuture : agentFutures.entrySet())
        {
            try
            {
                if (agentFuture.getValue().get())
                {
                    return true;
                }
            }
            catch (final Exception e)
            {
                // ignore, in this case we don't know the agent state and assume it's not running
            }
        }

        return false;
    }

    /**
     * Checks whether the test suite has been uploaded to all agents.
     *
     * @return true if all agents are in sync; false otherwise
     */
    public boolean areAgentsInSync()
    {
        return currentLoadProfile != null && currentLoadProfile.getActiveTestCaseNames().size() > 0;
    }

    /**
     * Sets the new user interface.
     *
     * @param userInterface
     *            the user interface
     */
    public void setUserInterface(final MasterControllerUI userInterface)
    {
        this.userInterface = userInterface;
    }

    /**
     * Starts the agents on all agent controllers at once.
     *
     * @param testCaseName
     *            the name of the test case to start the agents for, or <code>null</code> if all active test cases
     *            should be started
     * @return true if the operation was successful for ALL known agent controllers; false otherwise
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent site</li>
     *             </ul>
     */
    public boolean startAgents(final String testCaseName) throws AgentControllerException, IOException
    {
        if (currentLoadProfile == null)
        {
            // read load test profile if not already done so before during upload
            final File workDir = setUpWorkDir(FILE_FILTER);
            currentLoadProfile = getTestProfile(workDir);
        }

        resetAgentStatuses();

        currentTestCaseName = testCaseName;

        final TestDeployment testDeployment = getTestDeployment(currentLoadProfile, currentTestCaseName);

        // start the agents
        final FailedAgentControllerCollection failedAgentcontrollers = new FailedAgentControllerCollection();
        final int agentControllerSize = agentControllerMap.size();
        final CountDownLatch latch = new CountDownLatch(agentControllerSize);
        final ProgressBar progress = startNewProgressBar(agentControllerSize);
        for (final AgentController agentController : agentControllerMap.values())
        {
            defaultExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        final Map<String, List<TestUserConfiguration>> agentControllerLoadProfile = testDeployment.getAgentsUserList(agentController);

                        if (agentControllerLoadProfile == null || agentControllerLoadProfile.isEmpty())
                        {
                            LOG.info("No need to start agents at " + agentController);
                        }
                        else
                        {
                            // tell the agent manager about the reference time
                            agentController.setReferenceTime(System.currentTimeMillis());

                            LOG.info("Starting agents at " + agentController);
                            agentController.startAgents(agentControllerLoadProfile);
                        }
                    }
                    catch (final Exception ex)
                    {
                        failedAgentcontrollers.add(agentController, ex);
                        LOG.error("Failed starting agents at " + agentController, ex);
                    }
                    progress.increaseCount();
                    latch.countDown();
                }
            });
        }

        boolean finished = true;
        try
        {
            latch.await();
        }
        catch (final InterruptedException e)
        {
            LOG.error("Waiting for agent to start failed", e);
            finished = false;
        }

        checkSuccess(failedAgentcontrollers, true);
        userInterface.agentsStarted();

        final boolean operationCompleted = finished && (failedAgentcontrollers.isEmpty() ||
                                                        (isAgentControllerConnectionRelaxed &&
                                                         failedAgentcontrollers.getMap().size() < agentControllerSize));
        if (operationCompleted)
        {
            stoppedByUser = false;
        }
        return operationCompleted;
    }

    /**
     * Stops the agents on all agent controllers at once.
     *
     * @return true if the operation was successful for ALL known agent controllers; false otherwise
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent site</li>
     *             </ul>
     */
    public boolean stopAgents() throws AgentControllerException
    {
        final FailedAgentControllerCollection failedAgentcontrollers = new FailedAgentControllerCollection();
        final int agentControllerSize = agentControllerMap.size();
        final CountDownLatch latch = new CountDownLatch(agentControllerSize);
        final ProgressBar progress = startNewProgressBar(agentControllerSize);
        for (final AgentController agentController : agentControllerMap.values())
        {
            defaultExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    LOG.info("Stopping agents at " + agentController);
                    try
                    {
                        agentController.stopAgents();
                    }
                    catch (final Exception ex)
                    {
                        failedAgentcontrollers.add(agentController, ex);
                        LOG.error("Failed stopping agent at " + agentController, ex);
                    }
                    progress.increaseCount();
                    latch.countDown();
                }
            });
        }

        boolean finished = true;

        try
        {
            latch.await();
        }
        catch (final InterruptedException e)
        {
            finished = false;
        }

        checkSuccess(failedAgentcontrollers, false);

        userInterface.agentsStopped();

        final boolean operationCompleted = finished && (failedAgentcontrollers.isEmpty() ||
                                                        (isAgentControllerConnectionRelaxed &&
                                                         failedAgentcontrollers.getMap().size() < agentControllerSize));
        if (operationCompleted)
        {
            stoppedByUser = true;
        }
        return operationCompleted;
    }

    /**
     * Updates the agent files on all configured agent controllers at once.
     *
     * @return true if the operation was successful for ALL known agent controllers; false otherwise
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent controller site</li>
     *             </ul>
     * @throws IOException
     *             if an I/O error ocurres on archiving agent files for update
     * @throws IllegalStateException
     *             if there is no active load test configured
     */
    public void updateAgentFiles() throws AgentControllerException, IOException, IllegalStateException
    {
        System.out.print("    Preparing:");
        final ProgressBar progressPrepare = startNewProgressBar(agentControllerMap.size() + 5);

        /*
         * Cleanup the data left from last upload.
         */
        LOG.info("Cleanup");

        resetAgentStatuses();
        progressPrepare.increaseCount();

        /*
         * Optionally copy and manipulate the test suite.
         */
        LOG.info("Read target test suite");
        final File workDir = setUpWorkDir(FILE_FILTER);
        progressPrepare.increaseCount();

        /*
         * Read the configuration files and build load profile.
         */
        currentLoadProfile = getTestProfile(workDir);
        progressPrepare.increaseCount();

        if (currentLoadProfile.getActiveTestCaseNames().size() <= 0)
        {
            final String msg = "No test case configured.";
            XltLogger.runTimeLogger.warn(msg);
            throw new IllegalStateException(msg);
        }

        /*
         * Create the file replication index for the local agent files
         */
        LOG.info("Considering files in '" + workDir + "' for upload ...");
        final FileReplicationIndex localIndex = FileReplicationUtils.getIndex(workDir, FILE_FILTER);
        progressPrepare.increaseCount();

        final AgentControllerUpdate updater = new AgentControllerUpdate(agentControllerMap.values(), uploadExecutor, downloadExecutor,
                                                                        tempDirectory);
        updater.prepare(progressPrepare);

        System.out.println("- OK");

        /*
         * Upload test suite.
         */
        System.out.print("    Uploading:");
        final ProgressBar progressUpload = startNewProgressBar(4 * agentControllerMap.size() + 1);
        updater.update(workDir, localIndex, progressUpload);

        /*
         * Clean up.
         */
        LOG.info("Clean up");
        // delete the copy if we have made one
        if (workDir != agentFilesDirectory)
        {
            org.apache.commons.io.FileUtils.deleteQuietly(workDir);
        }
        progressUpload.increaseCount();

        checkSuccess(updater.getFailedAgentControllers(), true);

        /*
         * User information
         */
        userInterface.agentFilesUploaded();
    }

    /**
     * Set up the working directory.
     *
     * @param fileFilter
     *            file filter for testsuite
     */
    private File setUpWorkDir(final FileFilter fileFilter)
    {
        // only make a copy if we really have to change project.properties
        final File workDir;
        if (StringUtils.isBlank(propertiesFileName))
        {
            workDir = agentFilesDirectory;
        }
        else
        {
            try
            {
                // create a new sub directory in the temp directory
                workDir = File.createTempFile("xlt-", "", tempDirectory);
                org.apache.commons.io.FileUtils.forceDelete(workDir);
                org.apache.commons.io.FileUtils.forceMkdir(workDir);

                // copy the test suite
                org.apache.commons.io.FileUtils.copyDirectory(agentFilesDirectory, workDir, fileFilter);

                // enter the test properties file into project.properties
                final File projectPropertiesFile = new File(new File(workDir, "config"), "project.properties");
                final PropertiesConfiguration config = new PropertiesConfiguration();
                config.setIOFactory(new JupIOFactory()); // for better compatibility with java.util.Properties (GH#144)
                final FileHandler fileHandler = new FileHandler(config);

                fileHandler.load(projectPropertiesFile);
                config.setProperty(XltConstants.TEST_PROPERTIES_FILE_PATH_PROPERTY, propertiesFileName);
                fileHandler.save(projectPropertiesFile);
            }
            catch (final Exception e)
            {
                throw new RuntimeException("Failed to make a copy of the agent files", e);
            }
        }
        return workDir;
    }

    /**
     * Get the test profile.
     *
     * @param agentTemplateDir
     *            agent template directory
     */
    private TestLoadProfileConfiguration getTestProfile(final File agentTemplateDir) throws IOException
    {
        final File agentTemplateConfigDir = new File(agentTemplateDir, XltConstants.CONFIG_DIR_NAME);

        TestLoadProfileConfiguration testConfig;
        try
        {
            // read the load profile from the configuration
            final XltPropertiesImpl properties = TestLoadProfileConfiguration.readProperties(agentTemplateDir, agentTemplateConfigDir);
            testConfig = new TestLoadProfileConfiguration(properties);

            postProcessLoadProfile(testConfig);
        }
        catch (final Throwable ex)
        {
            throw new RuntimeException("Load profile configuration failed using directory: '" + agentTemplateConfigDir + "'. " +
                                       getDetailedMessage(ex), ex);
        }

        // check if test properties file is loaded if configured
        final String testPropertiesFileName = testConfig.getProperties().getProperty(XltConstants.TEST_PROPERTIES_FILE_PATH_PROPERTY);

        if (StringUtils.isNotBlank(testPropertiesFileName))
        {
            final FileObject testPropertiesFile = VFS.getManager().resolveFile(agentTemplateConfigDir, testPropertiesFileName);
            if (!testPropertiesFile.exists() || !testPropertiesFile.isFile() || !testPropertiesFile.isReadable())
            {
                throw new IOException("Unable to load test properties file.");
            }
        }

        return testConfig;
    }

    /**
     * Returns a detailed message for the given throwable object.
     *
     * @param throwable
     *            the throwable object
     * @return detailed message of given throwable object
     */
    protected static String getDetailedMessage(final Throwable throwable)
    {
        final List<String> messages = new LinkedList<>();

        Throwable t = throwable;
        while (t != null)
        {
            final String msg = t.getMessage();
            if (msg != null)
            {
                messages.add(msg);
            }

            t = t.getCause();
        }

        return StringUtils.join(messages, " :: ");
    }

    /**
     * Sets the test comment.
     *
     * @param comment
     *            the test comment
     */
    void setTestComment(final String comment)
    {
        if (comment != null)
        {
            testComment = comment.trim();
        }
        else
        {
            testComment = null;
        }
        LOG.debug("Test comment set to: " + StringUtils.defaultString(comment, "<NULL>"));
    }

    File getCurrentTestResultsDirectory()
    {
        return currentTestResultsDir;
    }

    /**
     * Returns the test comment.
     *
     * @return test comment
     */
    public String getTestComment()
    {
        return testComment;
    }

    /**
     * Sets the comment for the downloaded test results.
     */
    void setTestComment4DownloadedResults()
    {
        if (StringUtils.isBlank(getTestComment()))
        {
            return;
        }

        final FileObject testPropFile = getTestPropertyFile(currentTestResultsDir);
        try
        {
            if (testPropFile != null && testPropFile.exists())
            {
                try (final BufferedWriter w = new BufferedWriter(new OutputStreamWriter(testPropFile.getContent().getOutputStream(true))))
                {
                    w.newLine();
                    w.write("# Command line comment (AUTOMATICALLY INSERTED)\n");
                    w.write("com.xceptance.xlt.loadtests.comment.commandLine = " + getTestComment());
                }
            }
        }
        catch (final Exception e)
        {
            if (testPropFile != null)
            {
                LOG.error("Unable to write comment from CLI to '" + testPropFile.getPublicURIString() + "'.", e);
            }
            else
            {
                LOG.error("Unable to write comment from CLI. File information missing", e);
            }
        }
    }

    /**
     * Returns the value of the test comment property <tt>com.xceptance.xlt.loadtests.comment</tt>.
     *
     * @return value of test comment property
     */
    public String getTestCommentPropertyValue()
    {
        try
        {
            final FileSystemManager fsMgr = VFS.getManager();
            final XltProperties props = new XltPropertiesImpl(fsMgr.resolveFile(currentTestResultsDir.getAbsolutePath()),
                                                              fsMgr.resolveFile(getConfigDir(currentTestResultsDir).getAbsolutePath()),
                                                              false, true);

            final String testCommentPropValue = props.getProperties().getProperty("com.xceptance.xlt.loadtests.comment");

            return testCommentPropValue;
        }
        catch (final Exception e)
        {
            LOG.error("Failed to read/parse test configuration from '" + currentTestResultsDir + "'", e);
            return null;
        }
    }

    /**
     * Returns the file containing the test-specific properties.
     *
     * @param testResultsDir
     *            test result directory
     * @return test-specific properties file
     */
    public static FileObject getTestPropertyFile(final File testResultsDir)
    {
        try
        {
            final FileSystemManager fsMgr = VFS.getManager();

            final File confDir = getConfigDir(testResultsDir);

            final XltPropertiesImpl props = new XltPropertiesImpl(fsMgr.resolveFile(testResultsDir.getAbsolutePath()),
                                                                  fsMgr.resolveFile(confDir.getAbsolutePath()), false, true);
            return props.getTestPropertyFile();
        }
        catch (final Exception e)
        {
            LOG.error("Failed to read/parse test configuration from '" + testResultsDir + "'");
            return null;
        }
    }

    public void shutdown()
    {
        defaultExecutor.shutdownNow();
        uploadExecutor.shutdownNow();
        downloadExecutor.shutdownNow();
    }

    public void init()
    {
        agentControllerStatusUpdater.clearAgentControllerStatusMap();
    }

    /**
     * Creates a new progress bar and sets the default indentation.
     *
     * @param total
     *            expected total progress count
     */
    private ProgressBar startNewProgressBar(final int total)
    {
        final ProgressBar progress = new ProgressBar(total);
        System.out.print("    ");
        progress.start();

        return progress;
    }

    /**
     * Reset status of agent controllers (agents)
     *
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent site</li>
     *             </ul>
     */
    private void resetAgentStatuses() throws AgentControllerException
    {
        final FailedAgentControllerCollection failedAgentControllers = new FailedAgentControllerCollection();
        final CountDownLatch latch = new CountDownLatch(agentControllerMap.size());
        for (final AgentController agentController : agentControllerMap.values())
        {
            defaultExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        agentController.resetAgentsStatus();
                    }
                    catch (final Exception e)
                    {
                        failedAgentControllers.add(agentController, e);
                    }

                    latch.countDown();
                }
            });
        }

        try
        {
            latch.await();
        }
        catch (final InterruptedException e)
        {
            LOG.error("Waiting for resetting agent status to complete has failed");
        }

        agentControllerStatusUpdater.clearAgentControllerStatusMap();

        checkSuccess(failedAgentControllers, true);
    }

    /**
     * Check if communication with an agent controller failed and react.
     *
     * @param failedAgentControllers
     * @param keepLivingAgentControllersOnly
     *            if <code>true</code> all unreachable agent controllers get removed from the list of (available) agent
     *            controllers
     * @throws AgentControllerException
     *             if one of the following reasons
     *             <ul>
     *             <li>mastercontroller is not in relaxed mode and at least one agent controller did not respond</li>
     *             <li>an exception was thrown at agent site</li>
     *             </ul>
     */
    private void checkSuccess(final FailedAgentControllerCollection failedAgentControllers, final boolean keepLivingAgentControllersOnly)
        throws AgentControllerException
    {
        if (failedAgentControllers != null && !failedAgentControllers.isEmpty())
        {
            // tolerate unconnected agent controllers
            if (isAgentControllerConnectionRelaxed)
            {
                // inform user
                userInterface.skipAgentControllerConnections(failedAgentControllers);

                // remove unavailable agent controllers
                if (keepLivingAgentControllersOnly)
                {
                    for (final AgentController ac : failedAgentControllers.getAgentControllers())
                    {
                        agentControllerMap.remove(ac.getName());
                    }
                }

                // check that there is at least 1 agent controller left
                if (agentControllerMap.isEmpty())
                {
                    throw new IllegalStateException("No living AgentController left.");
                }
            }
            else
            {
                throw new AgentControllerException(failedAgentControllers);
            }
        }
    }

    private static File getConfigDir(final File inputDir)
    {
        /*
         * Starting with XLT 4.3.0 the load test configuration resides in a separate sub-directory named "config". We
         * have to fall back to given input directory in case such sub-directory does not exist, is not a directory or
         * is not readable.
         */
        final File configDir = new File(inputDir, XltConstants.RESULT_CONFIG_DIR);
        if (configDir != null && configDir.exists() && configDir.canRead() && configDir.isDirectory())
        {
            // set configuration context
            return configDir;
        }
        else
        {
            return inputDir;
        }
    }

    /**
     * Post-processes the given load profile.
     *
     * @param loadProfile
     *            the load profile
     */
    private void postProcessLoadProfile(final TestLoadProfileConfiguration loadProfile)
    {
        boolean haveCPTests = false;
        boolean haveLoadTests = false;
        for (final TestCaseLoadProfileConfiguration testConfig : loadProfile.getLoadTestConfiguration())
        {
            haveCPTests = haveCPTests || testConfig.isCPTest();
            haveLoadTests = haveLoadTests || !testConfig.isCPTest();
            if (isEmbedded)
            {
                testConfig.setCPTest(false);
            }
        }

        if (!isEmbedded)
        {
            final int acCount = agentControllerMap.size();
            int nbCPACs = 0;
            for (final AgentController ac : agentControllerMap.values())
            {
                if (ac.runsClientPerformanceTests())
                {
                    ++nbCPACs;
                }
            }

            final int diff = acCount - nbCPACs;
            final boolean haveCPAC = diff != acCount;
            final boolean haveLoadAC = diff > 0;

            if (haveCPTests && !haveCPAC)
            {
                throw new IllegalArgumentException("There is at least one client-performance test configured but no agent controller capable to run client-performance tests could be found.");
            }

            if (haveLoadTests && !haveLoadAC)
            {
                throw new IllegalArgumentException("There is at least one load/performance test configured but no agent controller capable to run load/performance tests could be found.");
            }
        }
    }

    /**
     * Starts querying the status from all agent controllers.
     */
    public void startAgentControllerStatusUpdates()
    {
        final long interval = userInterface.getStatusListUpdateInterval() * 1000L;

        agentControllerStatusUpdater.start(agentControllerMap.values(), interval);
    }

    /**
     * Stops querying the status from all agent controllers.
     */
    public void stopAgentControllerStatusUpdates()
    {
        agentControllerStatusUpdater.stop();
    }

    /**
     * Returns the status of each known agent controller.
     *
     * @return the status list
     */
    public List<AgentControllerStatusInfo> getAgentControllerStatusList()
    {
        final FailedAgentControllerCollection failedAgentcontrollers = new FailedAgentControllerCollection();

        userInterface.receivingAgentStatus();

        final Map<String, AgentControllerStatusInfo> agentControllerStatusMap = agentControllerStatusUpdater.getAgentControllerStatusMap();

        for (final AgentController agentController : agentControllerMap.values())
        {
            final AgentControllerStatusInfo agentControllerStatus = agentControllerStatusMap.get(agentController.getName());
            if (agentControllerStatus != null)
            {
                final Exception e = agentControllerStatus.getException();
                if (e != null)
                {
                    failedAgentcontrollers.add(agentController, e);
                }
            }
        }

        userInterface.agentStatusReceived(failedAgentcontrollers);

        return new ArrayList<>(agentControllerStatusMap.values());
    }
}
