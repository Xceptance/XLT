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
package com.xceptance.xlt.mastercontroller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xceptance.xlt.agentcontroller.AgentController;
import com.xceptance.xlt.agentcontroller.AgentStatus;
import com.xceptance.xlt.agentcontroller.FileManager;
import com.xceptance.xlt.agentcontroller.TestResultAmount;
import com.xceptance.xlt.agentcontroller.TestUserConfiguration;
import com.xceptance.xlt.util.AgentControllerSystemInfo;
import com.xceptance.xlt.util.FileReplicationIndex;

/**
 * Dummy {@link AgentController} implementation for testing purposes.
 */
public class TestAgentController implements AgentController
{
    private int agentCount;

    private Set<String> agentIds;

    private String name;

    private URL url;

    private int weight;

    private boolean runsCPTests;

    /**
     * {@inheritDoc}
     */
    @Override
    public void archiveAgentResults(final TestResultAmount testResultAmount)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAgentCount()
    {
        return agentCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileReplicationIndex getAgentFilesIndex() throws IOException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAgentIDs()
    {
        return agentIds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TestUserConfiguration> getAgentLoadProfile(final String agentID)
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<AgentStatus> getAgentStatus()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileManager getFileManager()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHostname()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getReferenceTimeDifference()
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getUrl()
    {
        return url;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWeight()
    {
        return weight;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasRunningAgent()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final String name, final URL url, final int weight, final int agentCount, final int agentBaseNumber,
                     final boolean runsClientPerformanceTests) throws IOException
    {
        this.name = name;
        this.url = url;
        this.weight = weight;
        this.agentCount = agentCount;

        agentIds = new HashSet<String>();
        for (int i = 0; i < agentCount; i++)
        {
            agentIds.add(name + "-" + (agentBaseNumber + i));
        }

        runsCPTests = runsClientPerformanceTests;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAgentStatus(final AgentStatus agentStatus)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReferenceTime(final long time)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTotalAgentCount(final int totalAgentCount)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startAgents(final Map<String, List<TestUserConfiguration>> loadProfiles)
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopAgents()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAgentFiles(final String agentFilesZipFileName, final List<File> filesToBeDeleted) throws IOException
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetAgentsStatus()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String archiveTestConfig()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ping()
    {
        return;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AgentControllerSystemInfo info()
    {
        final AgentControllerSystemInfo agentControllerSystemInfo = new AgentControllerSystemInfo();
        agentControllerSystemInfo.setTime(System.currentTimeMillis());
        return agentControllerSystemInfo;
    }

    @Override
    public boolean isArchiveAvailable()
    {
        return false;
    }

    @Override
    public void archiveDownloadDone()
    {
    }

    @Override
    public Map<String, String> getAgentResultsArchives()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean runsClientPerformanceTests()
    {
        return runsCPTests;
    }

    @Override
    public boolean isUpdateDone()
    {
        return false;
    }

    @Override
    public void setUpdateAcknowledged()
    {
    }

}
