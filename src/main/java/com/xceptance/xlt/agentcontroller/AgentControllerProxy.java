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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.client.HessianProxyFactory;
import com.xceptance.common.net.UrlConnectionFactory;
import com.xceptance.xlt.util.AgentControllerSystemInfo;
import com.xceptance.xlt.util.FileReplicationIndex;

/**
 * The AgentControllerProxy is a client-side implementation of the AgentController interface.
 */
public class AgentControllerProxy extends AgentControllerImpl
{
    private static final Logger log = LoggerFactory.getLogger(AgentControllerProxy.class);

    /**
     * The default size of a file chunk when downloading a result archive from an agent controller.
     */
    public static final long DEFAULT_DOWNLOAD_CHUNK_SIZE = 100_000_000L;

    /**
     * The default maximum number of retries in case downloading a result file (chunk) failed because of an I/O error.
     */
    public static final int DEFAULT_DOWNLOAD_MAX_RETRIES = 1;

    /**
     * The client-side agent controller implementation.
     */
    private AgentController agentController;

    /**
     * The file manager used by this controller.
     */
    private FileManager fileManager;

    /**
     * The Hessian proxy factory to use.
     */
    private final HessianProxyFactory proxyFactory;

    /**
     * The URL connection factory to use (for non-Hessian requests).
     */
    private final UrlConnectionFactory urlConnectionFactory;

    /**
     * The size of a file chunk when downloading a result archive from an agent controller.
     */
    private final long downloadChunkSize;

    /**
     * The maximum number of retries in case downloading a result file (chunk) failed because of an I/O error.
     */
    private final int downloadMaxRetries;

    /**
     * Creates a new AgentControllerProxy object.
     *
     * @param commandLineProperties
     * @param proxyFactory
     * @param urlConnectionFactory
     */
    public AgentControllerProxy(final Properties commandLineProperties, final HessianProxyFactory proxyFactory,
                                final UrlConnectionFactory urlConnectionFactory)
        throws Exception
    {
        this(commandLineProperties, proxyFactory, urlConnectionFactory, DEFAULT_DOWNLOAD_CHUNK_SIZE, DEFAULT_DOWNLOAD_MAX_RETRIES);
    }

    /**
     * Creates a new AgentControllerProxy object.
     *
     * @param commandLineProperties
     * @param proxyFactory
     * @param urlConnectionFactory
     * @param downloadChunkSize
     *            the size of a file chunk
     * @param downloadMaxRetries
     *            the maximum number of download retries
     */
    public AgentControllerProxy(final Properties commandLineProperties, final HessianProxyFactory proxyFactory,
                                final UrlConnectionFactory urlConnectionFactory, final long downloadChunkSize, final int downloadMaxRetries)
        throws Exception
    {
        super(commandLineProperties);

        this.proxyFactory = proxyFactory;
        this.urlConnectionFactory = urlConnectionFactory;
        this.downloadChunkSize = downloadChunkSize;
        this.downloadMaxRetries = downloadMaxRetries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepare() throws IOException
    {
        // ignore
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupEnvironment(final AgentManager agentManager) throws IOException
    {
        // ignore
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final String name, final URL url, final int weight, final int agentCount, final int agentBaseNumber,
                     final boolean runsClientPerformanceTests)
        throws IOException
    {
        super.init(name, url, weight, agentCount, agentBaseNumber, runsClientPerformanceTests);

        setupAgentManagers();

        // start proxy
        startProxy(getUrl());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTotalAgentCount(final int totalAgentCount)
    {
        super.setTotalAgentCount(totalAgentCount);
        getAgentController().setTotalAgentCount(totalAgentCount);
    }

    /**
     * Is already done by init() method. Call this method to NOT initialize the agent controller. Just open the proxy
     * connections.
     *
     * @param url
     * @throws MalformedURLException
     */
    public void startProxy(final URL url) throws MalformedURLException
    {
        log.info("start proxy for " + getName());

        // start file manager proxy
        fileManager = new FileManagerProxy(url, urlConnectionFactory, downloadChunkSize, downloadMaxRetries);

        // start agent controller proxy
        agentController = (AgentController) proxyFactory.create(AgentController.class,
                                                                new URL(url + "/" + AgentController.class.getName()).toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startServlet()
    {
        // ignore
    }

    /**
     * Returns the client-side AgentController implementation.
     *
     * @return the agent controller
     */
    protected AgentController getAgentController()
    {
        return agentController;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileManager getFileManager()
    {
        return fileManager;
    }

    /**
     */
    @Override
    public String toString()
    {
        return "Agent Controller " + getName() + " <" + getUrl() + ">";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getReferenceTimeDifference()
    {
        return getAgentController().getReferenceTimeDifference();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReferenceTime(final long time)
    {
        getAgentController().setReferenceTime(time);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasRunningAgent()
    {
        return getAgentController().hasRunningAgent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopAgents()
    {
        getAgentController().stopAgents();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TestUserConfiguration> getAgentLoadProfile(final String agentID)
    {
        return getAgentController().getAgentLoadProfile(agentID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startAgents(final Map<String, List<TestUserConfiguration>> loadProfiles)
    {
        getAgentController().startAgents(loadProfiles);
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @Override
    public Set<AgentStatus> getAgentStatus()
    {
        return getAgentController().getAgentStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AgentControllerStatus getStatus()
    {
        return getAgentController().getStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void archiveAgentResults(final TestResultAmount testResultAmount)
    {
        getAgentController().archiveAgentResults(testResultAmount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getAgentResultsArchives()
    {
        return getAgentController().getAgentResultsArchives();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isArchiveAvailable()
    {
        return getAgentController().isArchiveAvailable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void archiveDownloadDone()
    {
        getAgentController().archiveDownloadDone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileReplicationIndex getAgentFilesIndex() throws IOException
    {
        // initialize remote agent controller
        // initialize _here_ because it is the first contact point with the remote AC
        getAgentController().init(getName(), getUrl(), getWeight(), getAgentCount(), getAgentBaseNumber(), runsClientPerformanceTests());

        return getAgentController().getAgentFilesIndex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAgentFiles(final String agentFilesZipFileName, final List<File> filesToBeDeleted) throws IOException
    {
        getAgentController().updateAgentFiles(agentFilesZipFileName, filesToBeDeleted);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAgentStatus(final AgentStatus agentStatus)
    {
        getAgentController().setAgentStatus(agentStatus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetAgentsStatus()
    {
        getAgentController().resetAgentsStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String archiveTestConfig()
    {
        return getAgentController().archiveTestConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ping()
    {
        getAgentController().ping();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AgentControllerSystemInfo info()
    {
        return getAgentController().info();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUpdateDone() throws Exception
    {
        return getAgentController().isUpdateDone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUpdateAcknowledged()
    {
        getAgentController().setUpdateAcknowledged();
    }
}
