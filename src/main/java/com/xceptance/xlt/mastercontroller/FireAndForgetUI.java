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
package com.xceptance.xlt.mastercontroller;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.lang.ThreadUtils;
import com.xceptance.common.util.ConsoleUiUtils;
import com.xceptance.common.util.ConsoleUiUtils.EofHandler;
import com.xceptance.xlt.agentcontroller.TestResultAmount;

/**
 * The FireAndForgetUI is a user interface for the master controller which allows for only a limited user interaction,
 * but is best suited for the typical test run cycle where it is desirable to not require any user intervention. This
 * means, the test run starts immediately and proceeds automatically.
 * <p>
 * Internally, the master controller is triggered to run the agent at all configured agent controllers at once. This
 * includes:
 * <ul>
 * <li>uploading the agent files to all agent controllers</li>
 * <li>executing the agent at all agent controllers</li>
 * <li>downloading the test results from all agent controllers</li>
 * </ul>
 * The current agent status is reported regularly to the user. The only thing a user may do is to abort the test run
 * prematurely by hitting CTRL-C. This will stop any agent immediately, but all test results will be downloaded
 * nevertheless.
 */
public class FireAndForgetUI extends BasicConsoleUI
{
    /**
     * The ShutdownHook is responsible to gracefully abort the test run on all agent controllers if this process is
     * terminated (e.g. by CTRL-C or SIGTERM). This includes not only aborting all agents, but also keeping the JVM from
     * exiting before the test results are downloaded completely.
     */
    class ShutdownHook extends Thread
    {
        @Override
        public void run()
        {
            if (isShutdownHookActive.get())
            {
                isTestAborted.set(true);

                // stop the agents which will cause the test run to finish
                stopAgents();

                // wait until the test run is complete
                while (isShutdownHookActive.get())
                {
                    ThreadUtils.sleep(500);
                }
            }
        }
    }

    /**
     * Class logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FireAndForgetUI.class);

    /**
     * Whether a test report will be automatically created right after downloading the test results.
     */
    private final boolean generateReport;

    /**
     * Whether the test cases should be run sequentially.
     */
    private final boolean isSequential;

    /**
     * A flag used to control the shutdown hook.
     */
    private final AtomicBoolean isShutdownHookActive = new AtomicBoolean();

    /**
     * A flag indicating whether the test has been aborted via CTRL-C.
     */
    private final AtomicBoolean isTestAborted = new AtomicBoolean();

    /**
     * Whether download of test results should be skipped.
     */
    private final boolean noResults;

    /**
     * The agent controller initial response timeout.
     */
    private final long initialResponseTimeout;

    /**
     * The amount of test results to be downloaded.
     */
    private final TestResultAmount testResultAmount;

    /**
     * Creates a new FireAndForgetUI object.
     * 
     * @param masterController
     *            the master controller to use
     * @param isSequential
     *            whether the test cases should be run sequentially
     * @param generateReport
     *            whether a test report will be automatically created right after downloading the test results
     * @param noResults
     *            whether test results should not be downloaded (this also prevents report generation)
     * @param initialResponseTimeout
     *            the maximum time to wait for configured agent controllers to be up and running
     * @param resultAmount
     *            the amount of test results to download
     */
    public FireAndForgetUI(final MasterController masterController, final boolean isSequential, final boolean generateReport,
                           final boolean noResults, final long initialResponseTimeout, final TestResultAmount resultAmount)
    {
        super(masterController);

        this.isSequential = isSequential;
        this.generateReport = generateReport && !noResults;
        this.noResults = noResults;
        this.initialResponseTimeout = initialResponseTimeout;
        this.testResultAmount = resultAmount;
    }

    /**
     * Runs the user interface.
     */

    @Override
    public void run()
    {
        // install the shutdown hook for CTRL-C handling
        final ShutdownHook hook = new ShutdownHook();
        Runtime.getRuntime().addShutdownHook(hook);

        // register an EOF handler with ConsoleUiUtils
        ConsoleUiUtils.addEofHandler(new EofHandler()
        {
            @Override
            public void onEof()
            {
                // release the shutdown hook if blocked
                isShutdownHookActive.set(false);
            }
        });

        String cancelMessage = null;

        // check if instances are alive within the specified timeout
        if (checkAlive(initialResponseTimeout))
        {
            // check whether there is already a running load test
            if (!isLoadTestRunning())
            {
                // upload the agent files
                if (uploadAgentFiles())
                {
                    // run the test in normal or sequential mode
                    if (isSequential)
                    {
                        // run the tests one after the other
                        for (final String activeTestCaseName : masterController.getActiveTestCaseNames())
                        {
                            executeTestCase(activeTestCaseName);

                            if (isTestAborted.get())
                            {
                                break;
                            }
                        }
                    }
                    else
                    {
                        // run all tests at once
                        executeTestCase(null);
                    }
                }
                else
                {
                    cancelMessage = "Uploading test suite failed.";
                }
            }
            else
            {
                cancelMessage = "There is another load test running.";
            }
        }
        else
        {
            cancelMessage = "There are unreachable agent controllers.";
        }

        masterController.shutdown();

        if (cancelMessage != null)
        {
            throw new RuntimeException(cancelMessage);
        }
    }

    protected boolean checkAlive(final long timeout)
    {
        System.out.println("Waiting for agent controllers up to " + timeout / 1000 + "s.");
        final long deadline = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < deadline)
        {
            // check alive
            try
            {
                masterController.checkAlive();
                return true;
            }
            catch (final Exception e)
            {
                // ignore
            }
            // wait some time
            try
            {
                Thread.sleep(1000);
            }
            catch (final Exception e)
            {
                // ignore
            }
        }
        // finally check one last time
        return super.checkAlive();
    }

    /**
     * Runs the user interface.
     */

    private void executeTestCase(final String activeTestCaseName)
    {
        // start all agents
        if (!startAgents(activeTestCaseName))
        {
            // do not commence with the test
            stopAgents();
            throw new RuntimeException("Starting agents failed.");
        }

        isShutdownHookActive.set(true);

        printLoadTestSettings();
        System.out.println();
        System.out.println();

        // show the agent status
        try
        {
            ThreadUtils.sleep(3000);
            masterController.startAgentStatusList();

            while (masterController.isAnyAgentRunning_SAFE())
            {
                printAgentStatusList();
                ThreadUtils.sleep(getStatusListUpdateInterval() * 1000);
            }

            printAgentStatusList();

            masterController.stopAgentStatusList();
        }
        catch (final Exception ex)
        {
            log.error("Failed to update agent status:", ex);
        }

        // download the results
        if (isTestAborted.get())
        {
            if (ConsoleUiUtils.confirm("Do you want to download the results of the aborted test run?"))
            {
                System.out.println();
                downloadTestResults(null);

                // cannot generate report as we are shutting down
            }

            isShutdownHookActive.set(false);
        }
        else
        {
            isShutdownHookActive.set(false);

            if (noResults)
            {
                log.info("Download of test results has been skipped.");
            }
            else
            {
                if (!downloadTestResults(testResultAmount))
                {
                    throw new RuntimeException("Downloading test results failed.");
                }

                if (generateReport)
                {
                    generateReport(ReportCreationType.ALL);
                }
            }
        }
    }

    /**
     * @return the isSequential
     */
    public boolean isSequential()
    {
        return isSequential;
    }
}
