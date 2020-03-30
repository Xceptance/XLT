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
package com.xceptance.xlt.agent;

import java.io.File;

/**
 * A simple value class to hold information about this agent and other agents that take part in a load test.
 */
public class AgentInfo
{
    /**
     * The name of the results directory.
     */
    public static final String NAME_RESULTS_DIR = "results";

    /**
     * The current agent's ID.
     */
    private final String agentID;

    /**
     * The current agent's number (or index).
     */
    private int agentNumber;

    /**
     * The total number of agents participating in the load test.
     */
    private int totalAgentCount;

    /**
     * The agent's main directory.
     */
    private final File agentDirectory;

    /**
     * The agent's results directory.
     */
    private final File resultsDirectory;

    /**
     * Constructor.
     * 
     * @param agentID
     *            the current agent's ID
     * @param agentDirectory
     *            the agent's main directory
     */
    public AgentInfo(final String agentID, final File agentDirectory)
    {
        this.agentID = agentID;
        this.agentDirectory = agentDirectory;
        resultsDirectory = new File(agentDirectory, NAME_RESULTS_DIR);
    }

    /**
     * Returns the value of the 'agentName' attribute.
     * 
     * @return the value of agentName
     */
    public String getAgentID()
    {
        return agentID;
    }

    /**
     * Returns the value of the 'agentNumber' attribute.
     * 
     * @return the value of agentNumber
     */
    public int getAgentNumber()
    {
        return agentNumber;
    }

    /**
     * Set the value of the 'agentNumber' attribute.
     */
    public void setAgentNumber(final int agentNumber)
    {
        this.agentNumber = agentNumber;
    }

    /**
     * Returns the value of the 'totalAgentCount' attribute.
     * 
     * @return the value of totalAgentCount
     */
    public int getTotalAgentCount()
    {
        return totalAgentCount;
    }

    /**
     * Sets the value of the 'totalAgentCount' attribute.
     */
    public void setTotalAgentCount(final int totalAgentCount)
    {
        this.totalAgentCount = totalAgentCount;
    }

    /**
     * Returns the agent's results directory.
     * 
     * @return the agent's results directory
     */
    public File getResultsDirectory()
    {
        return resultsDirectory;
    }

    /**
     * Returns the agent's main directory.
     * 
     * @return the agent's main directory
     */
    public File getAgentDirectory()
    {
        return agentDirectory;
    }
}
