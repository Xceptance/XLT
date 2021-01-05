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

import java.io.File;
import java.net.URL;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.caucho.hessian.client.EasyHessianProxyFactory;
import com.caucho.hessian.client.HessianProxyFactory;
import com.xceptance.common.net.UrlConnectionFactory;
import com.xceptance.xlt.agentcontroller.AgentControllerConfiguration;
import com.xceptance.xlt.agentcontroller.AgentControllerProxy;
import com.xceptance.xlt.agentcontroller.AgentStatus;
import com.xceptance.xlt.agentcontroller.TestUserConfiguration;
import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.engine.GlobalClockImpl;

/**
 * Load test executor.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class Main
{
    /**
     * Class logger instance.
     */
    private static final Log log = LogFactory.getLog(Main.class);

    /**
     * The agent's ID.
     */
    private final String agentID;

    /**
     * The load test.
     */
    private final LoadTest loadTest;

    /**
     * Agent status pusher.
     */
    private final AgentStatusPusher statusPusher;

    /**
     * Watcher for agent controller.
     */
    private final AgentControllerWatcher watcher;

    /**
     * Generator for JVM resource usage statistics.
     */
    private final JvmResourceUsageDataGenerator jvmStatsGenerator;

    /**
     * Starting time of logging.
     */
    private long startOfLoggingPeriod;

    /**
     * Ending time of logging.
     */
    private long endOfLoggingPeriod;

    /**
     * Runner for custom samplers.
     */
    private final CustomSamplersRunner customSamplersRunner;

    /**
     * Creates a new load test executor.
     * 
     * @param args
     *            executor arguments
     *            <ul>
     *            <li>[0] - agent controller port</li>
     *            <li>[1] - agent ID</li>
     *            <li>[2] - host name</li>
     *            <li>[3] - agent number</li>
     *            <li>[4] - total agent count</li>
     *            <li>[5] - agent controller remote address</li>
     *            </ul>
     * @throws Exception
     *             thrown when setup of executor failed.
     */
    public Main(final String[] args) throws Exception
    {
        // parameter check
        if (args.length < 5)
        {
            System.err.println("The agent cannot be run stand-alone, but only from the agent controller.");
            System.exit(AgentExitCodes.PARAMETER_ERROR);
        }

        agentID = args[1];

        // get agent configuration
        final AgentConfiguration config = new AgentConfiguration();
        // final Appender a = (Appender) Logger.getRootLogger().getAppender("file");
        // if (a instanceof FileAppender)
        // {
        // final FileAppender fileAppender = (FileAppender) a;
        // fileAppender.setFile(new File(config.getResultsDirectory(), "agent.log").getAbsolutePath());
        // fileAppender.activateOptions();
        // }

        // get agent controller configuration
        final AgentControllerConfiguration agentControllerConfig = new AgentControllerConfiguration(null);

        // setup the agent manager
        final int port = Integer.parseInt(args[0]);
        final URL url = new URL("https://" + args[2] + ":" + port);

        // set up Hessian proxy factory
        final HessianProxyFactory proxyFactory = new EasyHessianProxyFactory();
        proxyFactory.setUser(agentControllerConfig.getUserName());
        proxyFactory.setPassword(agentControllerConfig.getPassword());

        final AgentControllerProxy agentController = new AgentControllerProxy(null, proxyFactory, new UrlConnectionFactory());
        agentController.startProxy(url);

        // setup the agent info
        final AgentInfo agentInfo = new AgentInfo(agentID, new File(config.getResultsDirectory(), agentID));

        final int agentNumber = Integer.parseInt(args[3]);
        agentInfo.setAgentNumber(agentNumber);

        final int totalAgentCount = Integer.parseInt(args[4]);
        agentInfo.setTotalAgentCount(totalAgentCount);

        // setup the agent status
        final AgentStatus status = new AgentStatus();
        final String hostName = args[2];
        status.setAgentID(agentID);
        status.setHostName(hostName);

        // setup the load test
        final List<TestUserConfiguration> loadProfile = agentController.getAgentLoadProfile(agentID);
        loadTest = new LoadTest(loadProfile, status, agentInfo);

        // setup the agent status pusher thread
        statusPusher = new AgentStatusPusher(agentController, status, config.getMaxErrors());
        statusPusher.setDaemon(true);

        // setup the agent controller monitor
        watcher = new AgentControllerWatcher(loadTest);
        watcher.setDaemon(true);

        // setup the global clock
        if (config.getUseMasterControllerTime())
        {
            final long referenceTimeDifference = agentController.getReferenceTimeDifference();
            ((GlobalClockImpl) GlobalClock.getInstance()).setReferenceTimeDifference(referenceTimeDifference);
        }

        String statsHost = hostName;
        int statsPort = port;
        if (args.length > 5)
        {
            final String acRemoteAddress = args[5];
            final int idx = acRemoteAddress.lastIndexOf(':');
            if (idx > -1)
            {
                statsHost = acRemoteAddress.substring(0, idx);
                statsPort = Integer.parseInt(acRemoteAddress.substring(idx + 1));
            }
        }
        // setup the JVM statistics generator
        determineOverallLoggingPeriod(loadProfile);
        jvmStatsGenerator = new JvmResourceUsageDataGenerator(agentID, statsHost, statsPort, startOfLoggingPeriod, endOfLoggingPeriod);
        jvmStatsGenerator.setDaemon(true);

        // register the shutdown hook
        final ShutdownHook shutdownHook = new ShutdownHook();
        shutdownHook.setPriority(Thread.MAX_PRIORITY);

        Runtime.getRuntime().addShutdownHook(shutdownHook);

        // setup custom sampler
        if (agentNumber == 0)
        {
            customSamplersRunner = new CustomSamplersRunner(agentInfo);
            customSamplersRunner.setDaemon(true);
        }
        else
        {
            customSamplersRunner = null;
        }
    }

    /**
     * Determines the starting and ending time of logging.
     * 
     * @param loadProfile
     *            list of load profiles to use for computation
     */
    private void determineOverallLoggingPeriod(final List<TestUserConfiguration> loadProfile)
    {
        long start = Long.MAX_VALUE;
        long end = 0;

        final long now = GlobalClock.getInstance().getTime();

        for (final TestUserConfiguration testUserConfiguration : loadProfile)
        {
            final long startUp = now + testUserConfiguration.getInitialDelay() + testUserConfiguration.getWarmUpPeriod();
            start = Math.min(start, startUp);
            end = Math.max(end, startUp + testUserConfiguration.getMeasurementPeriod());
        }

        startOfLoggingPeriod = start;
        endOfLoggingPeriod = end;
    }

    /**
     * Starts execution.
     * 
     * @throws Exception
     *             thrown when startup failed.
     */
    public void run() throws Exception
    {
        jvmStatsGenerator.start();
        watcher.start();
        statusPusher.start();
        if (customSamplersRunner != null)
        {
            customSamplersRunner.start();
        }
        loadTest.run();
    }

    /**
     * Entry point for command line execution.
     * 
     * @param args
     *            arguments to use
     */
    public static void main(final String[] args)
    {
        Main main = null;

        try
        {
            main = new Main(args);
        }
        catch (final Exception ex)
        {
            log.fatal("Failed to initialize load test agent.", ex);
            System.exit(AgentExitCodes.PARAMETER_ERROR);
        }

        try
        {
            main.run();
        }
        catch (final Exception ex)
        {
            log.fatal("Failed to run load test agent.", ex);
            System.exit(AgentExitCodes.GENERAL_ERROR);
        }

        log.info("Finished load test successfully.");

        System.exit(AgentExitCodes.SUCCESS);
    }

    /**
     * Shutdown hook that aborts a running load test.
     */
    class ShutdownHook extends Thread
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            // abort any running test
            loadTest.abort();

            // make sure the final state comes through
            statusPusher.pushStatus();
        }
    }
}
