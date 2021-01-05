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
package com.xceptance.xlt.agentcontroller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xceptance.xlt.util.AgentControllerSystemInfo;
import com.xceptance.xlt.util.FileReplicationIndex;

/**
 * The AgentController interface represents the (remote) agent controller and defines its API. Currently, it only
 * provides access to its two important components: the agent manager and the file manager.
 */

public interface AgentController
{
    /**
     * @param name
     *            the agent controller's name
     * @param url
     *            the agent controller's URL
     * @param weight
     *            the agent controller's weight
     * @param agentCount
     *            amount of agents at this agent controller
     * @param agentBaseNumber
     *            number of first agent at this agent controller
     * @throws IOException
     *             if proxy can not get started
     */
    public void init(final String name, final URL url, final int weight, final int agentCount, final int agentBaseNumber,
                     final boolean runsClientPerformanceTests) throws IOException;

    /**
     * Set the current total agent count.
     *
     * @param totalAgentCount
     *            total agent count
     */
    public void setTotalAgentCount(final int totalAgentCount);

    /**
     * Returns the number of agents managed by this agent controller.
     *
     * @return the agent count
     */
    public int getAgentCount();

    /**
     * Returns the {@link FileManager} implementation of this agent controller.
     *
     * @return the file manager
     */
    public FileManager getFileManager();

    /**
     * Returns the name of this agent controller.
     *
     * @return the name
     */
    public String getName();

    /**
     * Returns the weight of this agent controller.
     *
     * @return the weight
     */
    public int getWeight();

    /**
     * Returns the time difference between the global reference time and the local system time.
     *
     * @return the time difference
     */
    public long getReferenceTimeDifference();

    /**
     * Sets the global reference time. Used to calculate the time difference between the caller's and the local system.
     *
     * @param time
     *            the caller's time
     */
    public void setReferenceTime(long time);

    /**
     * Checks whether at least one agent is still running.
     *
     * @return <code>true</code> if at least 1 agent is running, <code>false</code> otherwise
     */
    public boolean hasRunningAgent();

    /**
     * Stops all agents.
     */
    public void stopAgents();

    /**
     * Returns the agent's load profile.
     *
     * @param agentID
     *            the agent's ID
     * @return the agent's load profile
     */
    public List<TestUserConfiguration> getAgentLoadProfile(String agentID);

    /**
     * Starts all agents with a load profile.
     *
     * @param loadProfiles
     *            the mapped load profiles, while the <code>key</code> is the agentID and the <code>value</code> the
     *            according load profile
     */
    public void startAgents(final Map<String, List<TestUserConfiguration>> loadProfiles);

    /**
     * Returns IDs of managed agents.<br>
     *
     * @return IDs of managed agents
     */
    public Set<String> getAgentIDs();

    /**
     * Returns all agents status.
     *
     * @return all agents status
     */
    public Set<AgentStatus> getAgentStatus();

    /**
     * Returns the agent controller's URL.
     *
     * @return the agent controller's URL
     */
    public URL getUrl();

    /**
     * Returns the agent controller's host name.
     *
     * @return the agent controller's host name
     */
    public String getHostname();

    /**
     * Archive all managed agent's results.
     *
     * @param testResultAmount
     */
    public void archiveAgentResults(final TestResultAmount testResultAmount);

    /**
     * Request agent result archive creating state
     *
     * @return <code>true</code> if the archive is ready for download or <code>false</code> otherwise
     */
    public boolean isArchiveAvailable();

    /**
     * Inform agent controller that downloading the agent results archives is finished.
     */
    public void archiveDownloadDone();

    /**
     * Get the previously archived agent results for all managed agents. The returned map uses the <code>agent ID</code>
     * as <code>key</code> and the <code>zip file name</code> as <code>value</code>.
     *
     * @return the previously archived agent results
     * @see #archiveAgentResults(TestResultAmount)
     */
    public Map<String, String> getAgentResultsArchives();

    /**
     * Returns the representative file index for the managed agents. The file index is same for all managed agents.
     *
     * @return the representative file index for the managed agents
     * @throws IOException
     *             if unable to initialize remote agent controller
     */
    public FileReplicationIndex getAgentFilesIndex() throws IOException;

    /**
     * Update the agents files. This method return immediately. Please query the success by {@link #isUpdateDone()}.
     * Furthermore it's recommended to acknowledge the state by {@link #setUpdateAcknowledged()}.
     *
     * @param agentFilesZipFileName
     *            zipped files to update
     * @param filesToBeDeleted
     *            files to delete
     * @throws IOException
     *             if a file update fails
     */
    public void updateAgentFiles(String agentFilesZipFileName, List<File> filesToBeDeleted) throws IOException;

    /**
     * Set the agent's status. The agent ID is part of the status object.
     *
     * @param agentStatus
     *            the agent's status
     */
    public void setAgentStatus(AgentStatus agentStatus);

    /**
     * Reset agents status
     */
    public void resetAgentsStatus();

    /**
     * Archives the test configuration.
     *
     * @return name of test configuration archive
     */
    public String archiveTestConfig();

    /**
     * Just an echo to a ping request.
     */
    public void ping();

    /**
     * Get some basic agent controller specific information.
     *
     * @return a agent controller system information object
     */
    public AgentControllerSystemInfo info();

    public boolean runsClientPerformanceTests();

    /**
     * Is the update based on the previously updated files done?
     * 
     * @return <code>true</code> if update is done, <code>false</code> otherwise
     * @throws Exception
     *             if the asynchronous file update failed
     */
    public boolean isUpdateDone() throws Exception;

    /**
     * Acknowledge update state
     */
    public void setUpdateAcknowledged();
}
