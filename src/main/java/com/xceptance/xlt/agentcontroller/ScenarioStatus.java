/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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

/**
 * The status of a load testing scenario, representing the results of all test users executing this scenario.
 */
public class ScenarioStatus extends TestUserStatus
{
    private static final long serialVersionUID = 1L;

    private int runningUsers;

    private int totalUsers;

    /**
     * Returns the value of the 'runningUsers' attribute.
     * 
     * @return the value of runningUsers
     */
    public int getRunningUsers()
    {
        return runningUsers;
    }

    /**
     * Returns the value of the 'totalUsers' attribute.
     * 
     * @return the value of totalUsers
     */
    public int getTotalUsers()
    {
        return totalUsers;
    }

    /**
     * Sets the new value of the 'runningUsers' attribute.
     * 
     * @param runningUsers
     *            the new runningUsers value
     */
    public void setRunningUsers(final int runningUsers)
    {
        this.runningUsers = runningUsers;
    }

    /**
     * Sets the new value of the 'totalUsers' attribute.
     * 
     * @param totalUsers
     *            the new totalUsers value
     */
    public void setTotalUsers(final int totalUsers)
    {
        this.totalUsers = totalUsers;
    }

    public String getScenarioName()
    {
        return getUserName();
    }

    public void setScenarioName(final String scenarioName)
    {
        setUserName(scenarioName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String.format("[scenarioName=%s, state=%s, runningUsers=%d, totalUsers=%d, iterations=%d, lastRuntime=%d ms, averageRuntime=%d ms, totalRuntime=%d ms, events=%d, errors=%d, progress=%d%%]",
                             getScenarioName(), getState(), getRunningUsers(), getTotalUsers(), getIterations(), getLastRuntime(),
                             getAverageRuntime(), getTotalRuntime(), getEvents(), getErrors(), getPercentageComplete());
    }
}
