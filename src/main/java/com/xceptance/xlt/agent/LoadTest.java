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
package com.xceptance.xlt.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import com.xceptance.common.util.TimerUtils;
import com.xceptance.xlt.agentcontroller.AgentStatus;
import com.xceptance.xlt.agentcontroller.TestUserConfiguration;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;

/**
 * Class responsible for running a load test.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class LoadTest
{
    /**
     * The default time period [ms] to wait for threads to finish voluntarily before quitting the JVM (currently 30 s).
     */
    private static final long DEFAULT_GRACE_PERIOD = 30 * 1000;

    /**
     * The configured time period [ms] to wait for threads to finish voluntarily before quitting the JVM.
     */
    private static final long gracePeriod;

    static
    {
        final long v = XltProperties.getInstance().getProperty(XltConstants.XLT_PACKAGE_PATH + ".hangingUsersGracePeriod",
                                                               DEFAULT_GRACE_PERIOD);

        gracePeriod = (v < 0) ? DEFAULT_GRACE_PERIOD : v;
    }

    /**
     * List of test runners.
     */
    private final List<LoadTestRunner> testRunners = new ArrayList<LoadTestRunner>();

    /**
     * Create a new load test using the given configurations and agent status.
     * 
     * @param configs
     *            test configurations to use
     * @param agentStatus
     *            agent status to use
     * @param agentInfo
     *            load test agent information
     */
    public LoadTest(final List<TestUserConfiguration> configs, final AgentStatus agentStatus, final AgentInfo agentInfo)
    {
        // add a safety break to quit the agent process after the load test even in case of hanging test users (#1632)
        final long agentTimeout = determineLoadTestDuration(configs) + gracePeriod;
        TimerUtils.getTimer().schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                System.exit(AgentExitCodes.SUCCESS);
            }
        }, agentTimeout);

        // process configurations
        for (final TestUserConfiguration config : configs)
        {
            // get/create the responsible execution timer
            final AbstractExecutionTimer timer = ExecutionTimerFactory.createTimer(config);

            // create runner for configuration
            final LoadTestRunner runner = new LoadTestRunner(config, agentInfo, timer);
            runner.setDaemon(true);

            // add runner to list of known runners
            testRunners.add(runner);

            // add agent status
            agentStatus.addTestUserStatus(runner.getTestUserStatus());
        }
    }

    /**
     * Aborts a running load test.
     */
    public void abort()
    {
        // first mark all test runners as aborted
        for (final LoadTestRunner testRunner : testRunners)
        {
            testRunner.setAborted();
        }

        // now stop all execution timers which will in turn stop their test runners
        for (final AbstractExecutionTimer timer : ExecutionTimerFactory.getTimers())
        {
            timer.stop();
        }
    }

    /**
     * Runs the load test.
     */
    public void run()
    {
        // process list of runners
        for (final LoadTestRunner testRunner : testRunners)
        {
            testRunner.start();
        }

        // wait for their completion
        waitForCompletion(testRunners);
    }

    /**
     * Waits for the given runners to complete.
     * 
     * @param runners
     *            list of runners to wait for their completion
     */
    private void waitForCompletion(final List<LoadTestRunner> runners)
    {
        // process given list of runners
        for (final LoadTestRunner runner : runners)
        {
            try
            {
                // wait for completion
                runner.join();
            }
            catch (final InterruptedException ex)
            {
                // ignore
            }
        }
    }

    /**
     * Returns the total duration of the load test - the time when the shutdown period of the longest running user is
     * over.
     * 
     * @param testUserConfigurations
     *            the list of test user configurations to check
     * @return the load test duration
     */
    private long determineLoadTestDuration(final List<TestUserConfiguration> testUserConfigurations)
    {
        long totalDuration = 0;

        for (final TestUserConfiguration userConfig : testUserConfigurations)
        {
            final long durationOfTestUser = userConfig.getInitialDelay() + userConfig.getWarmUpPeriod() +
                                            userConfig.getMeasurementPeriod() + userConfig.getShutdownPeriod();

            totalDuration = Math.max(totalDuration, durationOfTestUser);
        }

        return totalDuration;
    }
}
