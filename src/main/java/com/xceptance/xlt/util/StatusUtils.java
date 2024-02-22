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
package com.xceptance.xlt.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.agentcontroller.AgentControllerStatus;
import com.xceptance.xlt.agentcontroller.AgentStatus;
import com.xceptance.xlt.agentcontroller.AgentStatusInfo;
import com.xceptance.xlt.agentcontroller.ScenarioStatus;
import com.xceptance.xlt.agentcontroller.TestUserStatus;
import com.xceptance.xlt.mastercontroller.AgentControllerStatusInfo;

/**
 * Helper methods to aggregate status objects or to convert deprecated status objects into their new counterparts.
 */
public final class StatusUtils
{
    /**
     * Aggregates the agent-controller-local scenario status lists contained in the given agent controller status
     * objects into a single scenario status list with exactly one scenario status object per test scenario. The
     * scenario status objects of the different agent controllers are merged together sensibly.
     *
     * @param agentControllerStatusInfoList
     *            the list of agent controller status objects
     * @return the list of resulting scenario status objects
     */
    public static List<ScenarioStatus> aggregateScenarioStatusLists(final List<AgentControllerStatusInfo> agentControllerStatusInfoList)
    {
        final Map<String, ScenarioStatus> mergedScenarioStatusMap = new TreeMap<>();

        for (final AgentControllerStatusInfo agentControllerStatus : agentControllerStatusInfoList)
        {
            for (final ScenarioStatus scenarioStatus : agentControllerStatus.getScenarioStatusList())
            {
                // maintain one global scenario status for all input scenario statuses with the same name
                final String scenarioName = scenarioStatus.getScenarioName();
                final ScenarioStatus mergedScenarioStatus = mergedScenarioStatusMap.computeIfAbsent(scenarioName,
                                                                                                    StatusUtils::createInitialScenarioStatus);

                mergeScenarioStatusInto(scenarioStatus, mergedScenarioStatus);
            }
        }

        return new ArrayList<>(mergedScenarioStatusMap.values());
    }

    /**
     * Returns the total scenario status calculated from the individual scenario status objects.
     *
     * @param scenarioStatusList
     *            the list of scenario status objects
     * @return the total scenario status
     */
    public static ScenarioStatus getTotalScenarioStatus(final List<ScenarioStatus> scenarioStatusList)
    {
        // the initial total scenario status
        final ScenarioStatus totalScenarioStatus = createInitialScenarioStatus("Totals");

        // update the total status with the individual status values
        for (final ScenarioStatus scenarioStatus : scenarioStatusList)
        {
            mergeScenarioStatusInto(scenarioStatus, totalScenarioStatus);
        }

        return totalScenarioStatus;
    }

    /**
     * Merges a source scenario status into a target scenario status. The target status is updated with the aggregated
     * values.
     *
     * @param sourceScenarioStatus
     *            the scenario status to merge
     * @param targetScenarioStatus
     *            the scenario status to be updated
     */
    public static void mergeScenarioStatusInto(final ScenarioStatus sourceScenarioStatus, final ScenarioStatus targetScenarioStatus)
    {
        // determine the over-all execution state
        final ScenarioStatus.State targetState = targetScenarioStatus.getState();
        final ScenarioStatus.State sourceState = sourceScenarioStatus.getState();

        if (targetState != ScenarioStatus.State.Running && sourceState != ScenarioStatus.State.Waiting)
        {
            targetScenarioStatus.setState(sourceState);
        }

        // most values are trivial to merge
        targetScenarioStatus.setRunningUsers(targetScenarioStatus.getRunningUsers() + sourceScenarioStatus.getRunningUsers());
        targetScenarioStatus.setTotalUsers(targetScenarioStatus.getTotalUsers() + sourceScenarioStatus.getTotalUsers());
        targetScenarioStatus.setIterations(targetScenarioStatus.getIterations() + sourceScenarioStatus.getIterations());
        targetScenarioStatus.setTotalRuntime(targetScenarioStatus.getTotalRuntime() + sourceScenarioStatus.getTotalRuntime());
        targetScenarioStatus.setElapsedTime(Math.max(targetScenarioStatus.getElapsedTime(), sourceScenarioStatus.getElapsedTime()));
        targetScenarioStatus.setEvents(targetScenarioStatus.getEvents() + sourceScenarioStatus.getEvents());
        targetScenarioStatus.setErrors(targetScenarioStatus.getErrors() + sourceScenarioStatus.getErrors());
        targetScenarioStatus.setPercentageComplete(Math.max(targetScenarioStatus.getPercentageComplete(),
                                                            sourceScenarioStatus.getPercentageComplete()));

        targetScenarioStatus.setStartDate(Math.min(targetScenarioStatus.getStartDate(), sourceScenarioStatus.getStartDate()));
        targetScenarioStatus.setException(ObjectUtils.defaultIfNull(sourceScenarioStatus.getException(),
                                                                    targetScenarioStatus.getException()));

        // take the last run time and the last modified time from the most recently updated status
        if (targetScenarioStatus.getLastModifiedDate() < sourceScenarioStatus.getLastModifiedDate())
        {
            targetScenarioStatus.setLastModifiedDate(sourceScenarioStatus.getLastModifiedDate());
            targetScenarioStatus.setLastRuntime(sourceScenarioStatus.getLastRuntime());
        }
    }

    /**
     * Returns the agent status objects for only those agents that exited with an error, grouped by host name.
     *
     * @param agentControllerStatusList
     *            the list of all agent controller status objects
     * @return the failed agent status objects keyed by host name
     */
    public static Map<String, List<AgentStatusInfo>> getFailedAgentsByHost(final List<AgentControllerStatusInfo> agentControllerStatusList)
    {
        final Map<String, List<AgentStatusInfo>> failedAgentsByHostMap = new HashMap<>();

        for (final AgentControllerStatusInfo agentControllerStatus : agentControllerStatusList)
        {
            for (final AgentStatusInfo agentStatus : agentControllerStatus.getAgentStatusList())
            {
                if (agentStatus.getExitCode() != null && agentStatus.getExitCode() != 0)
                {
                    final List<AgentStatusInfo> failedAgentStatusList = failedAgentsByHostMap.computeIfAbsent(agentStatus.getHostName(),
                                                                                                              k -> new ArrayList<>());
                    failedAgentStatusList.add(agentStatus);
                }
            }
        }

        return failedAgentsByHostMap;
    }

    /**
     * Converts a list of deprecated agent status objects into their new counterpart.
     *
     * @param agentStatusList
     *            the list of agent status objects containing the user-specific status objects
     * @return the list of resulting agent controller status objects
     * @deprecated To be removed in XLT 8.0.0.
     */
    @Deprecated
    public static AgentControllerStatus getAgentControllerStatus(final Set<AgentStatus> agentStatusList)
    {
        final List<AgentStatusInfo> agentStatusInfoList = new ArrayList<>();
        final List<TestUserStatus> userStatusList = new ArrayList<>();

        for (final AgentStatus agentStatus : agentStatusList)
        {
            // convert and remember the agent status
            final AgentStatusInfo agentStatusInfo = new AgentStatusInfo(agentStatus.getAgentID(), agentStatus.getHostName(), true,
                                                                        agentStatus.getErrorExitCode());

            agentStatusInfoList.add(agentStatusInfo);

            // collect the agent-specific user status objects
            userStatusList.addAll(agentStatus.getTestUserStatusList());
        }

        // aggregate the collected individual user status objects
        final List<ScenarioStatus> scenarioStatusList = aggregateUserStatusList(userStatusList);

        // return the data as a new agent controller status object
        return new AgentControllerStatus(agentStatusInfoList, scenarioStatusList);
    }

    /**
     * Aggregates the list of individual user status objects (one per test user) into a list of scenario status objects
     * (one per test scenario). All user status objects of the same user type are merged sensibly into a single scenario
     * status.
     *
     * @param userStatusList
     *            the list of user-specific status objects
     * @return the list of resulting scenario status objects
     * @deprecated To be removed in XLT 8.0.0.
     */
    @Deprecated
    public static List<ScenarioStatus> aggregateUserStatusList(final List<TestUserStatus> userStatusList)
    {
        final Map<String, ScenarioStatus> scenarioStatusMap = new TreeMap<>();

        for (final TestUserStatus userStatus : userStatusList)
        {
            // maintain one global scenario status object for all user status objects of the same user type
            final String scenarioName = StringUtils.substringBeforeLast(userStatus.getUserName(), "-");
            final ScenarioStatus scenarioStatus = scenarioStatusMap.computeIfAbsent(scenarioName, StatusUtils::createInitialScenarioStatus);

            // maintain count of total and running users
            scenarioStatus.setTotalUsers(scenarioStatus.getTotalUsers() + 1);

            if (userStatus.getState() == TestUserStatus.State.Running)
            {
                scenarioStatus.setRunningUsers(scenarioStatus.getRunningUsers() + 1);
            }

            // determine the over-all running state
            final TestUserStatus.State state = userStatus.getState();
            if (scenarioStatus.getState() != TestUserStatus.State.Running && state != TestUserStatus.State.Waiting)
            {
                scenarioStatus.setState(state);
            }

            // take the last run time from the most recently updated status
            if (scenarioStatus.getLastModifiedDate() < userStatus.getLastModifiedDate())
            {
                scenarioStatus.setLastModifiedDate(userStatus.getLastModifiedDate());
                scenarioStatus.setLastRuntime(userStatus.getLastRuntime());
            }

            // calculate the overall percentage
            if (userStatus.getMode() == TestUserStatus.Mode.TIME_PERIOD)
            {
                // for duration-based tests take the maximum
                scenarioStatus.setPercentageComplete(Math.max(scenarioStatus.getPercentageComplete(), userStatus.getPercentageComplete()));
            }
            else
            {
                // for iteration-based tests take the mean
                final int totalUsers = scenarioStatus.getTotalUsers();
                if (totalUsers == 1)
                {
                    // this is the initial value
                    scenarioStatus.setPercentageComplete(userStatus.getPercentageComplete());
                }
                else
                {
                    // incrementally update the mean value
                    double mean = scenarioStatus.getPercentageComplete();

                    mean = mean + (userStatus.getPercentageComplete() - mean) / totalUsers;

                    scenarioStatus.setPercentageComplete((int) mean);
                }
            }

            // update the remaining values
            scenarioStatus.setIterations(scenarioStatus.getIterations() + userStatus.getIterations());
            scenarioStatus.setTotalRuntime(scenarioStatus.getTotalRuntime() + userStatus.getTotalRuntime());
            scenarioStatus.setElapsedTime(Math.max(scenarioStatus.getElapsedTime(), userStatus.getElapsedTime()));
            scenarioStatus.setEvents(scenarioStatus.getEvents() + userStatus.getEvents());
            scenarioStatus.setErrors(scenarioStatus.getErrors() + userStatus.getErrors());

            scenarioStatus.setException(ObjectUtils.defaultIfNull(userStatus.getException(), scenarioStatus.getException()));
            scenarioStatus.setStartDate(Math.min(scenarioStatus.getStartDate(), userStatus.getStartDate()));
        }

        return new ArrayList<>(scenarioStatusMap.values());
    }

    /**
     * Creates an initial scenario status object that can be safely used as a target scenario when merging two scenario
     * status objects via {@link #mergeScenarioStatusInto(ScenarioStatus, ScenarioStatus)}.
     * 
     * @param scenarioName
     *            the scenario name to set
     * @return the scenario status object
     */
    private static ScenarioStatus createInitialScenarioStatus(final String scenarioName)
    {
        final ScenarioStatus scenarioStatus = new ScenarioStatus();
        scenarioStatus.setScenarioName(scenarioName);
        scenarioStatus.setStartDate(Long.MAX_VALUE);
        scenarioStatus.setElapsedTime(Long.MIN_VALUE);

        return scenarioStatus;
    }
}
