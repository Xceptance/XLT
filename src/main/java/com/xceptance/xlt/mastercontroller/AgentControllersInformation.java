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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.util.ProductInformation;
import com.xceptance.xlt.agentcontroller.AgentController;
import com.xceptance.xlt.util.AgentControllerInfo;

/**
 * Agent controller information. Queries some information from the agent controllers and runs some basic analyzing.
 */
public class AgentControllersInformation
{
    /** Logger. */
    private static final Logger log = LoggerFactory.getLogger(AgentControllersInformation.class);

    /** Unknown XLT version number dummy. */
    private static final String VERSION_UNKNOWN = "Version Unknown";

    /** Time difference threshold of agent controller and master controller in seconds. */
    private static final long DEFAULT_TIME_DIFF_THRESHOLD = 10;

    /** Map holding all the agent controller information objects mapped by the agent controller's name. */
    private final Map<String, AgentControllerInfo> agentControllerInfos = Collections.synchronizedMap(new TreeMap<String, AgentControllerInfo>());

    /** All XLT version numbers of master and agent controllers. */
    private final Set<String> xltInfos = new HashSet<String>();

    /** All Java information Strings of master and agent controllers. */
    private final Set<String> javaInfos = new HashSet<String>();

    /** Thread pool executor to run the information requests in. */
    private final ThreadPoolExecutor executor;

    /** Threshold to classify the time difference between master and agent controller as 'high'. */
    private final long timeDifferenceThreshold;

    /** Has at least 1 agent controller a timeout? */
    private boolean hasErrors = false;

    /** Has at least 1 agent controller a high time difference to the master controller? */
    private boolean hasHighDiff = false;

    /** Are there different Java versions running? */
    private boolean hasJavaConflict = false;

    /** Are there different XLT versions running? */
    private boolean hasXltVersionConflict = false;

    /**
     * Queries some information from the agent controllers and runs some basic analyzing with a default time difference
     * threshold of {@value #DEFAULT_TIME_DIFF_THRESHOLD} seconds..
     * 
     * @param agentControllers
     *            agent controllers
     * @param executor
     *            executor to run the queries in
     */
    public AgentControllersInformation(final Collection<AgentController> agentControllers, final ThreadPoolExecutor executor)
    {
        this(agentControllers, executor, DEFAULT_TIME_DIFF_THRESHOLD);
    }

    /**
     * Queries some information from the agent controllers and runs some basic analyzing.
     * 
     * @param agentControllers
     *            agent controllers
     * @param executor
     *            executor to run the queries in
     * @param timeDifferenceThreshold
     *            number of tolerable time difference between master and agent controller. Default is
     *            {@value #DEFAULT_TIME_DIFF_THRESHOLD} seconds.
     */
    public AgentControllersInformation(final Collection<AgentController> agentControllers, final ThreadPoolExecutor executor,
                                       final long timeDifferenceThreshold)
    {
        this.executor = executor;
        this.timeDifferenceThreshold = timeDifferenceThreshold;

        init(agentControllers);
    }

    /**
     * Queries some information from the agent controllers and runs some basic analyzing.
     * 
     * @param agentControllers
     *            agent controllers
     */
    private void init(final Collection<AgentController> agentControllers)
    {
        // get the agent controller information
        loadAgentControllersInformation(agentControllers);

        // take master controller's XLT version number into account
        xltInfos.add(ProductInformation.getProductInformation().getCondensedProductIdentifier());

        // analyze the responses
        analyze();
    }

    /**
     * Queries some information from the agent controllers.
     * 
     * @param agentControllers
     *            agent controllers
     */
    private void loadAgentControllersInformation(final Collection<AgentController> agentControllers)
    {
        final CountDownLatch latch = new CountDownLatch(agentControllers.size());

        // request information about all agent controllers
        for (final AgentController agentcontroller : agentControllers)
        {
            executor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    // initialize with the known static data
                    final AgentControllerInfo agentControllerInfo = new AgentControllerInfo();
                    agentControllerInfo.setName(agentcontroller.getName());
                    agentControllerInfo.setHostName(agentcontroller.getHostname());
                    try
                    {
                        // request the dynamic part
                        agentControllerInfo.setAgentControllerSystemInfo(agentcontroller.info());
                        // set time diff to Master Controller
                        agentControllerInfo.setTimeDifferenceTo(System.currentTimeMillis());
                    }
                    catch (final Exception e)
                    {
                        log.error("Failed to query agent controller information: " + agentcontroller, e);
                        agentControllerInfo.setException(e);
                    }

                    // remember the current agent controller information
                    agentControllerInfos.put(agentControllerInfo.getName(), agentControllerInfo);

                    latch.countDown();
                }
            });
        }

        // wait for all agent controller information requests to be finished
        try
        {
            latch.await();
        }
        catch (final InterruptedException e)
        {
            log.error("Waiting for agent controller system information results to complete has failed", e);
        }
    }

    /**
     * Traverse all gathered agent controller information, extract some basic data and detect conflicts.
     */
    private void analyze()
    {
        // traverse all gathered agent controller information
        for (final AgentControllerInfo agentControllerInfo : agentControllerInfos.values())
        {
            if (agentControllerInfo.getAgentControllerSystemInfo() != null)
            {
                // XLT Version
                final String xltVersion = agentControllerInfo.getAgentControllerSystemInfo().getXltVersion();
                xltInfos.add(xltVersion);

                // Java version
                final String javaVersion = agentControllerInfo.getAgentControllerSystemInfo().getJavaVersion();
                javaInfos.add(javaVersion);

                // get time difference to master controller
                final long timeDiff = agentControllerInfo.getTimeDifference();
                if (timeDiff >= (timeDifferenceThreshold * 1000))
                {
                    hasHighDiff = true;
                }
            }
            else
            {
                // interpret the absence of agent controller system information as timeout
                hasErrors = true;
                xltInfos.add(VERSION_UNKNOWN);
                javaInfos.add(VERSION_UNKNOWN);
            }
        }

        // detect XLT version conflict
        if (xltInfos.size() > 1 || xltInfos.contains(VERSION_UNKNOWN))
        {
            hasXltVersionConflict = true;
        }

        // detect Java conflict
        if (javaInfos.size() > 1 || javaInfos.contains(VERSION_UNKNOWN))
        {
            hasJavaConflict = true;
        }
    }

    /**
     * Get the agent controller information sorted by agent controller name.
     * 
     * @return the agent controller information
     */
    public Collection<AgentControllerInfo> getAgentControllerInformation()
    {
        return agentControllerInfos.values();
    }

    /**
     * Has at least one of the agent controllers a communication timeout?
     * 
     * @return
     */
    public boolean hasErrors()
    {
        return hasErrors;
    }

    public boolean hasXltVersionConflict()
    {
        return hasXltVersionConflict;
    }

    /**
     * @return <code>true</code> if
     */
    public boolean hasJavaConflict()
    {
        return hasJavaConflict;
    }

    /**
     * Has at least one of the agent controllers a high time difference to the master controller's time.
     * 
     * @return <code>true</code> if at least one of the agent controllers has a high time difference to the master
     *         controller's time
     */
    public boolean hasHighTimeDifference()
    {
        return hasHighDiff;
    }

    /**
     * Get the threshold in seconds to classify the time difference between master and agent controller as 'high'.
     * 
     * @return time difference threshold.
     */
    public long getTimeDiffThreshold()
    {
        return timeDifferenceThreshold;
    }
}
