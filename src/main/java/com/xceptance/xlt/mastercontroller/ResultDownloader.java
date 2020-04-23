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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.common.util.zip.ZipUtils;
import com.xceptance.xlt.agentcontroller.AgentController;
import com.xceptance.xlt.agentcontroller.AgentStatus;
import com.xceptance.xlt.agentcontroller.TestResultAmount;
import com.xceptance.xlt.agentcontroller.TestUserStatus;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.util.FailedAgentControllerCollection;
import com.xceptance.xlt.util.ProgressBar;

public class ResultDownloader
{
    private static final Log LOG = LogFactory.getLog(ResultDownloader.class);

    private final ThreadPoolExecutor downloadExecutor;

    private final File testResultsDir;

    private final File tempDirectory;

    private final FailedAgentControllerCollection failedAgentControllers;

    private final ProgressBar progress;

    private final List<AgentController> agentControllers;

    public ResultDownloader(final ThreadPoolExecutor downloadExecutor, final File testResultsDir, final File tempDirectory,
                            final ArrayList<AgentController> agentControllers, final ProgressBar progress)
    {
        this.downloadExecutor = downloadExecutor;
        this.testResultsDir = testResultsDir;
        this.tempDirectory = tempDirectory;
        this.progress = progress;

        this.agentControllers = agentControllers;
        failedAgentControllers = new FailedAgentControllerCollection();
    }

    /**
     * @progresscount 7 ac + 4
     */
    public boolean download(final TestResultAmount testResultAmount)
    {
        // download test configuration
        final boolean testConfigDownloaded = getRemoteTestConfig();

        // update time data
        boolean timeDataUpdated = false;
        if (testConfigDownloaded)
        {
            final File testPropFile = MasterController.getTestPropertyFile(testResultsDir);
            if (testPropFile != null)
            {
                timeDataUpdated = updateTimeData(testPropFile);
            }
        }

        // archive results
        archiveResults(testResultAmount);

        // download and unzip archives
        final boolean resultsDownloaded = downloadResults(testResultAmount);

        // We have downloaded results from at least 1 agent controller.
        // AND
        // We have successfully unzipped the test configuration and set up start/end time.
        return resultsDownloaded && testConfigDownloaded && timeDataUpdated;
    }

    /**
     * @progresscount 3
     */
    private boolean getRemoteTestConfig()
    {
        // download remote test configuration
        final Set<File> tempTestConfigs = downloadTestConfig();

        // unzip downloaded configuration and clean up downloaded files
        final boolean unziped = unzipTestConfig(tempTestConfigs);

        return unziped;
    }

    /**
     * @progresscount 1
     */
    private Set<File> downloadTestConfig()
    {
        final Set<File> tempTestConfigs = Collections.synchronizedSet(new HashSet<File>());
        final CountDownLatch latch = new CountDownLatch(agentControllers.size());
        final AtomicBoolean callFailed = new AtomicBoolean();
        for (final AgentController agentController : agentControllers)
        {
            downloadExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    // if test config was not downloaded yet
                    LOG.info(agentController + ": Download test configuration");
                    try
                    {
                        if (tempTestConfigs.isEmpty())
                        {
                            tempTestConfigs.add(downloadConfiguration(agentController));
                            LOG.info(agentController + ": Download test configuration OK");
                        }
                    }
                    catch (final Exception e)
                    {
                        failedAgentControllers.add(agentController, e);
                        LOG.error("Failed downloading test configuration", e);
                        callFailed.set(true);
                    }
                    finally
                    {
                        latch.countDown();
                    }
                }
            });
        }

        try
        {
            latch.await();
        }
        catch (final InterruptedException e)
        {
            LOG.error("Waiting for download of test configuration has failed", e);
        }
        finally
        {
            if (callFailed.get())
            {
                removeFailedControllers();
            }
        }

        if (!tempTestConfigs.isEmpty())
        {
            progress.increaseCount();
        }

        return tempTestConfigs;
    }

    private void removeFailedControllers()
    {
        final Iterator<AgentController> it = agentControllers.iterator();
        final Set<AgentController> failed = failedAgentControllers.getAgentControllers();
        while (it.hasNext())
        {
            final AgentController ac = it.next();
            if (failed.contains(ac))
            {
                it.remove();
                LOG.debug(ac.getName() + ": Removed from list of used controllers.");
            }
        }
    }

    /**
     * @progresscount 2
     */
    private boolean unzipTestConfig(final Set<File> tempTestConfigs)
    {
        // unzip
        LOG.debug("Unzipping test configuration ...");
        boolean unzipped = false;
        for (final File tempTestConfigFile : tempTestConfigs)
        {
            try
            {
                ZipUtils.unzipFile(tempTestConfigFile, testResultsDir);
                LOG.debug("Finished unzipping of test configuration");

                unzipped = true;
                break;
            }
            catch (final IOException ioe)
            {
            }
        }
        progress.increaseCount();

        // clean up downloaded test config files
        LOG.debug("Clean up ... ");
        for (final File tempTestConfigFile : tempTestConfigs)
        {
            org.apache.commons.io.FileUtils.deleteQuietly(tempTestConfigFile);
        }
        progress.increaseCount();

        return unzipped;
    }

    /**
     * @progresscount ac
     */
    private boolean getTimeData(final AtomicLong startDate, final AtomicLong elapsedTime)
    {
        LOG.info("Query earliest start date and highest elapsed time");
        final CountDownLatch latch = new CountDownLatch(agentControllers.size());
        final AtomicBoolean callFailed = new AtomicBoolean();
        for (final AgentController agentController : agentControllers)
        {
            downloadExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        long earliestTestUserStartDate = Long.MAX_VALUE;
                        long highestTestUserElapsedTime = 0L;

                        final Set<AgentStatus> agentStatuses = agentController.getAgentStatus();
                        if (agentStatuses != null)
                        {
                            for (final AgentStatus agentStatus : agentStatuses)
                            {
                                for (final TestUserStatus testUserStatus : agentStatus.getTestUserStatusList())
                                {
                                    // start date
                                    {
                                        final long userStartDate = testUserStatus.getStartDate();
                                        if (userStartDate < earliestTestUserStartDate)
                                        {
                                            earliestTestUserStartDate = userStartDate;
                                        }
                                    }

                                    // elapsed time
                                    {
                                        final long userElapsedTime = testUserStatus.getElapsedTime();
                                        if (userElapsedTime > highestTestUserElapsedTime)
                                        {
                                            highestTestUserElapsedTime = userElapsedTime;
                                        }
                                    }
                                }
                            }
                        }

                        // update global earliest test user start date
                        synchronized (startDate)
                        {
                            if (earliestTestUserStartDate < startDate.get())
                            {
                                startDate.set(earliestTestUserStartDate);
                            }
                        }

                        // update highest test user elapsed time
                        synchronized (elapsedTime)
                        {
                            if (highestTestUserElapsedTime > elapsedTime.get())
                            {
                                elapsedTime.set(highestTestUserElapsedTime);
                            }
                        }
                    }
                    catch (final Exception e)
                    {
                        failedAgentControllers.add(agentController, e);
                        callFailed.set(true);
                    }
                    finally
                    {
                        progress.increaseCount();
                        latch.countDown();
                    }
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
            LOG.error("Waiting retrieving time data has failed", e);
            finished = false;
        }
        finally
        {
            if (callFailed.get())
            {
                removeFailedControllers();
            }
        }

        return finished;
    }

    /**
     * @progresscount ac
     */
    private void archiveResults(final TestResultAmount testResultAmount)
    {
        LOG.info("Archive results");
        final AtomicBoolean callFailed = new AtomicBoolean();
        final CountDownLatch latch = new CountDownLatch(agentControllers.size());
        for (final AgentController agentController : agentControllers)
        {
            downloadExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        LOG.debug("Archive results at " + agentController);
                        agentController.archiveAgentResults(testResultAmount);
                    }
                    catch (final Exception e)
                    {
                        failedAgentControllers.add(agentController, e);
                        LOG.debug("Archive results FAILED at " + agentController);
                        callFailed.set(true);
                    }
                    finally
                    {
                        latch.countDown();
                        progress.increaseCount();
                    }
                }
            });
        }

        try
        {
            latch.await();
        }
        catch (final InterruptedException e)
        {
            LOG.error("Failure");
        }
        finally
        {
            if (callFailed.get())
            {
                removeFailedControllers();
            }
        }

    }

    /**
     * @progresscount 5 * ac
     */
    private boolean downloadResults(final TestResultAmount testResultAmount)
    {
        LOG.debug("Download results");
        try
        {
            return Poll.poll(downloadExecutor, new Poll.AgentControllerPollingTask()
            {
                @Override
                public boolean call(final AgentController agentController) throws Exception
                {
                    if (agentController.isArchiveAvailable())
                    {
                        // download the archive
                        LOG.debug("Downloading results from " + agentController);
                        downloadTestResults(agentController, testResultAmount);
                        LOG.debug("Downloading results from " + agentController + " OK");
                        return true;
                    }
                    return false;
                }
            }, agentControllers, failedAgentControllers, progress);
        }
        finally
        {
            removeFailedControllers();
        }
    }

    /**
     * @progresscount ac + 1
     */
    private boolean updateTimeData(final File testPropFile)
    {
        // earliest start date and highest elapsed time of test users over all agent controllers
        final AtomicLong startDate = new AtomicLong(Long.MAX_VALUE);
        final AtomicLong elapsedTime = new AtomicLong(0);

        // get start date and elapsed time
        final boolean downloadedTimeData = getTimeData(startDate, elapsedTime);

        // update test config
        LOG.debug("Set start date and elapsed time to test configuration ...");

        boolean timeDataUpdated = false;
        if (downloadedTimeData && testPropFile.exists())
        {
            List<?> lines = null;
            try
            {
                lines = org.apache.commons.io.FileUtils.readLines(testPropFile, StandardCharsets.ISO_8859_1);
            }
            catch (final Exception ex)
            {
                LOG.error("Failed to read content of file '" + testPropFile.getAbsolutePath() + "'.", ex);
            }

            if (lines != null)
            {
                final ArrayList<String> outLines = new ArrayList<String>();
                for (final Object o : lines)
                {
                    final String s = (String) o;
                    outLines.add(s);
                }

                outLines.add(XltConstants.EMPTYSTRING);
                outLines.add("# start date / elapsed time (AUTOMATICALLY INSERTED)");
                outLines.add(XltConstants.LOAD_TEST_START_DATE + " = " + startDate.get());
                outLines.add(XltConstants.LOAD_TEST_ELAPSED_TIME + " = " + elapsedTime.get());

                try
                {
                    org.apache.commons.io.FileUtils.writeLines(testPropFile, outLines);
                    timeDataUpdated = true;
                }
                catch (final Exception ex)
                {
                    LOG.error("Failed to write content to file '" + testPropFile.getAbsolutePath() + "'.", ex);
                }
            }
        }
        progress.increaseCount();

        return timeDataUpdated;
    }

    /**
     * Downloads the test results from the specified agent controller to the given directory.
     * 
     * @param testResultsDir
     *            the target directory
     * @param agentController
     *            the target agent controller
     * @param testResultAmount
     *            the amount of test result data to download
     * @throws java.io.IOException
     *             if an I/O error occurs
     * @progresscount 4
     */
    private void downloadTestResults(final AgentController agentController, final TestResultAmount testResultAmount) throws IOException
    {
        /** agentID, downloadedZipFile */
        final Map<String, File> downloadedZipFiles = new HashMap<String, File>();

        /*
         * download
         */
        {
            LOG.info("Downloading test results files from " + agentController);
            /** agentID : remoteFileName */
            final Map<String, String> remoteZipFileNames = agentController.getAgentResultsArchives();
            progress.increaseCount();

            final String tempResultsPrefix = "testresults-";

            for (final Map.Entry<String, String> remoteAgentResultFile : remoteZipFileNames.entrySet())
            {
                final String agentID = remoteAgentResultFile.getKey();
                final String remoteZipFileName = remoteAgentResultFile.getValue();

                final File zipFile = File.createTempFile(tempResultsPrefix + agentID + "-", ".zip", tempDirectory);
                zipFile.deleteOnExit();

                agentController.getFileManager().downloadFile(zipFile, remoteZipFileName);

                downloadedZipFiles.put(agentID, zipFile);
            }
            progress.increaseCount();
        }

        /*
         * unzip
         */
        {
            LOG.debug("Unzipping test results files ...");
            for (final Map.Entry<String, File> downloadedZipFile : downloadedZipFiles.entrySet())
            {
                final String agentID = downloadedZipFile.getKey();
                final File zipFile = downloadedZipFile.getValue();

                final File agentResultsDir = new File(testResultsDir, agentID);
                LOG.debug("Unzipping '" + zipFile + "' to '" + agentResultsDir + "' ...");
                ZipUtils.unzipFile(zipFile, agentResultsDir);
            }
            progress.increaseCount();
        }

        /*
         * cleanup
         */
        {
            LOG.debug("cleanup agent controller test results archive files ...");
            agentController.archiveDownloadDone();

            LOG.debug("cleanup master controller test results archive files ...");
            for (final File zipFile : downloadedZipFiles.values())
            {
                org.apache.commons.io.FileUtils.deleteQuietly(zipFile);
            }

            LOG.info("Finished downloading test results files from " + agentController);
            progress.increaseCount();
        }
    }

    /**
     * Downloads the test configuration from the given agent controller and returns the downloaded file.
     * 
     * @param agentController
     *            the agent controller
     * @return downloaded test configuration archive
     * @throws java.io.IOException
     *             thrown if download/extraction of test configuration has failed
     * @progresscount 0
     */
    private File downloadConfiguration(final AgentController agentController) throws IOException
    {
        LOG.debug("Archiving test configuration ...");

        final String remoteZipFileName = agentController.archiveTestConfig();

        final File tempConfigZip = File.createTempFile("testconfig-", ".zip", tempDirectory);
        tempConfigZip.deleteOnExit();

        LOG.debug("Downloading test configuration archive ...");

        if (remoteZipFileName != null)
        {
            agentController.getFileManager().downloadFile(tempConfigZip, remoteZipFileName);
        }

        return tempConfigZip;
    }

    public FailedAgentControllerCollection getFailedAgentControllerCollection()
    {
        return failedAgentControllers;
    }
}
