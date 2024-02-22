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
package com.xceptance.xlt.agentcontroller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.xceptance.xlt.agent.AgentInfo;
import com.xceptance.xlt.util.FileReplicationIndex;

/**
 * The AgentManager interface represents the (remote) agent manager and defines its API.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public interface AgentManager
{
    /**
     * Sets up environment for agent (e.g. creating necessary directories).
     * 
     * @throws IOException
     */
    public void setupEnvironment() throws IOException;

    /**
     * Archives the agent's results files to the given file.
     * 
     * @param testResultAmount
     *            the amount of test result data to archive
     * @param zipFile
     *            the file to zip the files to
     * @return <code>true</code> if process was successful, <code>false</code> otherwise
     */
    public boolean archiveAgentResults(TestResultAmount testResultAmount, File zipFile);

    /**
     * Returns an index data structure which holds information about all local agent files. This index will be used at
     * the master controller to determine which files need to be update before the next test run.
     * 
     * @return the file index or <code>null</code> if agent directory doesn't exist
     */
    public FileReplicationIndex getAgentFilesIndex();

    /**
     * Removes the agent's results directory
     */
    public void removeResultsDirectory();

    /**
     * Returns the agent's load profile.
     * 
     * @return the load profile
     */
    public List<TestUserConfiguration> getAgentLoadProfile();

    /**
     * Returns the agent's runtime status.
     * 
     * @return the status or <code>null</code> if agent or status is not available
     */
    public AgentStatus getAgentStatus();

    /**
     * Resets the agent's runtime status.
     */
    public void resetAgentStatus();

    /**
     * Checks whether agent is running.
     * 
     * @return the status
     */
    public boolean isAgentRunning();

    /**
     * Sets the agent's runtime status.
     * 
     * @param status
     *            the new status
     */
    public void setAgentStatus(AgentStatus status);

    /**
     * Stops the agent.
     */
    public void stopAgent();

    /**
     * Update the agent directory based on the source directory.
     * 
     * @param sourceDir
     *            source directory
     * @throws IOException
     *             if updating files fails
     */
    public void updateAgentFiles(File sourceDir);

    /**
     * Returns the agent.
     * 
     * @return agent
     */
    public AgentImpl getAgent();

    /**
     * Returns the agent information object.
     * 
     * @return agent information object
     */
    public AgentInfo getAgentInfo();

    /**
     * Returns the command line.
     * 
     * @return command line
     */
    public String[] getCommandLine();

    /**
     * Start the agent with given load profile.
     * 
     * @param loadProfile
     *            load profile
     * @throws Exception
     *             if anything goes wrong
     */
    public void startAgent(final List<TestUserConfiguration> loadProfile) throws Exception;

    /**
     * Stop the agent and remove the corresponding agent directory.
     * 
     * @throws IOException
     */
    public void close() throws IOException;

    /**
     * Set the total agent count.
     * 
     * @param totalAgentCount
     *            total agent count
     */
    public void setTotalAgentCount(final int totalAgentCount);

    /**
     * Set the agent's number.
     * 
     * @param agentNumber
     *            agent number
     */
    public void setAgentNumber(final int agentNumber);
}
