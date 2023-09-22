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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.util.concurrent.DaemonThreadFactory;
import com.xceptance.xlt.agentcontroller.AgentController;
import com.xceptance.xlt.agentcontroller.AgentControllerStatus;
import com.xceptance.xlt.agentcontroller.AgentStatus;
import com.xceptance.xlt.util.StatusUtils;

/**
 * Retrieves the status from all agent controllers and stores that internally.
 */
public class AgentControllerStatusUpdater
{
    private static final Logger LOG = LoggerFactory.getLogger(AgentControllerStatusUpdater.class);

    /**
     * The agent controller status objects keyed by agent controller name.
     */
    private final Map<String, AgentControllerStatusInfo> statusMap;

    /**
     * The agent controller status objects keyed by agent controller name as an unmodifiable map.
     */
    private final Map<String, AgentControllerStatusInfo> unmodifiableStatusMap;

    /**
     * The executor service that actually runs the update tasks.
     */
    private final ExecutorService executor;

    /**
     * The executor service that schedules the update tasks.
     */
    private ScheduledExecutorService scheduledExecutor;

    /**
     * The update tasks by agent controller. Used to cancel any running or queued task when status update was stopped.
     */
    private final Map<AgentController, Future<?>> tasks;

    /**
     * Creates a new {@link AgentControllerStatusUpdater} and initializes it with the given executor service. The
     * settings of the executor service determine the degree of parallelity when querying the status from the agent
     * controllers.
     *
     * @param executor
     *            the executor service
     */
    public AgentControllerStatusUpdater(final ExecutorService executor)
    {
        this.executor = executor;

        statusMap = new ConcurrentHashMap<>();
        unmodifiableStatusMap = Collections.unmodifiableMap(statusMap);
        tasks = new ConcurrentHashMap<>();
    }

    /**
     * Clears the status stored for any agent controller.
     */
    public void clearAgentControllerStatusMap()
    {
        statusMap.clear();
    }

    /**
     * Returns the status of all the agent controllers as an unmodifiable map.
     *
     * @return the agent controller status map
     */
    public Map<String, AgentControllerStatusInfo> getAgentControllerStatusMap()
    {
        return unmodifiableStatusMap;
    }

    /**
     * Starts the periodic status update.
     *
     * @param agentControllers
     *            the agent controllers to query
     * @param delay
     *            the pause [ms] between status updates
     */
    public synchronized void start(final Collection<AgentController> agentControllers, final long delay)
    {
        // ensure to start fresh
        stop();

        // create a new scheduled executor that schedules the update tasks
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("AC-status-pool-"));

        // determine which agent controllers support the new status endpoint
        final Map<AgentController, Boolean> supported = checkNewStatusEndpointSupported(agentControllers);

        // start update task for each agent controller
        for (final AgentController agentController : agentControllers)
        {
            final boolean supportsNewStatusEndpoint = supported.get(agentController);

            // execute task once, it will reschedule itself to become periodic
            final Future<?> task = executor.submit(() -> retrieveAgentControllerStatus(agentController, supportsNewStatusEndpoint, delay));
            tasks.put(agentController, task);
        }
    }

    /**
     * Stops the periodic status update.
     */
    public synchronized void stop()
    {
        // kill scheduled executor so no new update tasks can be created
        if (scheduledExecutor != null)
        {
            scheduledExecutor.shutdownNow();
            scheduledExecutor = null;
        }

        // cancel any already running/queued update task
        tasks.values().forEach(task -> task.cancel(true));
        tasks.clear();
    }

    /**
     * Determines for all agent controllers whether or not an agent controller already supports the new status endpoint
     * {@link AgentController#getStatus()}.
     *
     * @param agentControllers
     *            the agent controller to check
     * @return a map with the results keyed by agent controller
     * @deprecated To be removed in XLT 8.0.0.
     */
    @Deprecated
    private Map<AgentController, Boolean> checkNewStatusEndpointSupported(final Collection<AgentController> agentControllers)
    {
        final Map<AgentController, Future<Boolean>> futures = new HashMap<>();

        // start asynchronous tasks to determine the endpoint support in parallel
        for (final AgentController agentController : agentControllers)
        {
            final Future<Boolean> future = executor.submit(() -> isNewEndpointSupported(agentController));
            futures.put(agentController, future);
        }

        // now collect the results of all asynchronous tasks
        final Map<AgentController, Boolean> results = new HashMap<>();
        for (final Entry<AgentController, Future<Boolean>> futureEntry : futures.entrySet())
        {
            boolean result = false;

            try
            {
                result = futureEntry.getValue().get();
            }
            catch (InterruptedException | ExecutionException e)
            {
                // ignore, use the old status endpoint
            }

            results.put(futureEntry.getKey(), result);
        }

        return results;
    }

    /**
     * Determines if the agent controller already supports the new status endpoint {@link AgentController#getStatus()}.
     *
     * @param agentController
     *            the agent controller to check
     * @return <code>true</code> if the new status endpoint is available, <code>false</code> otherwise
     * @deprecated To be removed in XLT 8.0.0.
     */
    @Deprecated
    private boolean isNewEndpointSupported(final AgentController agentController)
    {
        boolean result = false;

        try
        {
            agentController.getStatus();
            result = true;
        }
        catch (final Exception e)
        {
            // result stays false
        }

        LOG.debug("{} supports the new status endpoint: {}", agentController, result);

        return result;
    }

    /**
     * Retrieves the current status from an agent controller and stores it internally. Furthermore, reschedules this
     * method to be run again after the specified delay.
     *
     * @param agentController
     *            the agent controller
     * @param supportsNewStatusEndpoint
     *            whether or not the agent controller supports the new status endpoint
     * @param delay
     *            the delay [ms] before running the method once again
     */
    private void retrieveAgentControllerStatus(final AgentController agentController, final boolean supportsNewStatusEndpoint,
                                               final long delay)
    {
        // perform the actual status update
        retrieveAgentControllerStatus(agentController, supportsNewStatusEndpoint);

        // schedule this method to run again at a later time
        final Runnable updateTask = () -> retrieveAgentControllerStatus(agentController, supportsNewStatusEndpoint, delay);

        final Runnable wrapperTask = () -> {
            final Future<?> task = executor.submit(updateTask);
            tasks.put(agentController, task);
        };

        synchronized (this)
        {
            if (scheduledExecutor != null && !scheduledExecutor.isShutdown())
            {
                scheduledExecutor.schedule(wrapperTask, delay, TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * Retrieves the current status from an agent controller and stores it internally.
     *
     * @param agentController
     *            the agent controller
     * @param supportsNewStatusEndpoint
     *            whether or not the agent controller supports the new status endpoint
     */
    private void retrieveAgentControllerStatus(final AgentController agentController, final boolean supportsNewStatusEndpoint)
    {
        // check whether this task was cancelled
        if (Thread.currentThread().isInterrupted())
        {
            return;
        }

        // query the agent controller status
        try
        {
            LOG.debug("Getting status from " + agentController);
            final AgentControllerStatus agentControllerStatus = getStatus(agentController, supportsNewStatusEndpoint);
            final AgentControllerStatusInfo agentControllerStatusInfo = new AgentControllerStatusInfo(agentControllerStatus);
            statusMap.put(agentController.getName(), agentControllerStatusInfo);
        }
        catch (final Exception e)
        {
            // check once more whether this task was cancelled
            if (Thread.currentThread().isInterrupted())
            {
                return;
            }

            LOG.error("Failed getting status from " + agentController, e);

            // keep any previous state if available but set the exception
            final AgentControllerStatusInfo agentControllerStatus = statusMap.get(agentController.getName());
            if (agentControllerStatus == null)
            {
                statusMap.put(agentController.getName(), new AgentControllerStatusInfo(e));
            }
            else
            {
                agentControllerStatus.setException(e);
            }
        }
    }

    /**
     * Queries the status of an agent controller, preferring the new status endpoint over the old one if the agent
     * controller supports it.
     *
     * @param agentController
     *            the agent controller to query
     * @param supportsNewStatusEndpoint
     *            whether the agent controller supports the new status endpoint
     * @return the agent controller status
     * @deprecated To be removed in XLT 8.0.0.
     */
    @Deprecated
    private AgentControllerStatus getStatus(final AgentController agentController, final boolean supportsNewStatusEndpoint)
    {
        AgentControllerStatus agentControllerStatus;

        if (supportsNewStatusEndpoint)
        {
            agentControllerStatus = agentController.getStatus();
        }
        else
        {
            // get the old detailed status, but immediately aggregate it to save heap space
            final Set<AgentStatus> agentStatuses = agentController.getAgentStatus();
            agentControllerStatus = StatusUtils.getAgentControllerStatus(agentStatuses);
        }

        return agentControllerStatus;
    }
}
