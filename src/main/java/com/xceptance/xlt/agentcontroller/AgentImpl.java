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
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.util.StreamPump;

/**
 * 
 */

public class AgentImpl implements Agent
{
    class ProcessMonitor extends Thread
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            log.debug("Started agent process monitoring thread.");

            try
            {
                final int exitCode = process.waitFor();
                if (!isStopped.get() && exitCode != 0)
                {
                    agentExitedUnexpectedly(exitCode);
                }
            }
            catch (final Exception ex)
            {
                log.error("An error occurred while waiting for the agent process to die: ", ex);
            }

            agentStopped();
        }
    }

    private static final Logger log = LoggerFactory.getLogger(AgentImpl.class);

    private final AgentListener agentListener;

    private final ProcessMonitor monitor;

    private final Process process;

    private final OutputStream stdin;

    private final List<TestUserConfiguration> loadProfile;

    private final String agentID;

    private AgentStatus status;

    private final AtomicBoolean isStopped = new AtomicBoolean();

    /**
     * Create and initialize agent.
     * 
     * @param agentID
     *            the agent's ID
     * @param commandLine
     *            command line to start the agent
     * @param resultsDir
     *            directory to store the results in
     * @param loadProfile
     *            load profile
     * @param agentListener
     *            agent listener
     * @param workingDirectory
     *            the agent's working directory
     * @throws Exception
     *             if anything goes wrong
     */
    public AgentImpl(final String agentID, final String[] commandLine, final File resultsDir,
                     final List<TestUserConfiguration> loadProfile, final AgentListener agentListener, final File workingDirectory)
        throws Exception
    {
        this.agentID = agentID;
        this.loadProfile = loadProfile;
        this.agentListener = agentListener;

        // cleanup the results from a previous run if any
        FileUtils.forceMkdir(resultsDir);
        com.xceptance.common.io.FileUtils.cleanDirRelaxed(resultsDir);

        // create the agent process
        final ProcessBuilder procBuilder = new ProcessBuilder(commandLine);
        procBuilder.directory(workingDirectory);
        process = procBuilder.start();

        // setup the thread that monitors the agent process
        monitor = new ProcessMonitor();
        monitor.setDaemon(true);
        monitor.start();

        // remember stdin - we will use it to request the process to stop
        stdin = process.getOutputStream();

        // read away the sub process's output to stdout
        final StreamPump stdoutLogger = new StreamPump(process.getInputStream(), new File(resultsDir, "agent-stdout.log"));
        stdoutLogger.setDaemon(true);
        stdoutLogger.start();

        // read away the sub process's output to stderr
        final StreamPump stderrLogger = new StreamPump(process.getErrorStream(), new File(resultsDir, "agent-stderr.log"));
        stderrLogger.setDaemon(true);
        stderrLogger.start();
    }

    private void agentStopped()
    {
        if (agentListener != null)
        {
            agentListener.agentStopped(agentID);
        }
    }

    private void agentExitedUnexpectedly(final int exitCode)
    {
        if (agentListener != null)
        {
            agentListener.agentExitedUnexpectedly(agentID, exitCode);
        }
    }

    /**
     * @return the agentListener
     */

    public AgentListener getAgentListener()
    {
        return agentListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AgentStatus getStatus()
    {
        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRunning()
    {
        return monitor.isAlive();
    }

    /**
     * Sets the agent's runtime status.
     * 
     * @param status
     *            the new status
     */
    @Override
    public void setStatus(final AgentStatus status)
    {
        this.status = status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop()
    {
        // get stopped flag and set it to true in one operation
        final boolean wasStopped = isStopped.getAndSet(true);
        // if agent has not been stopped yet, stop it now by starting its stop thread
        if (!wasStopped)
        {
            new StopThread().start();
        }
    }

    /**
     * Returns the load profile.
     * 
     * @return load profile
     */
    public List<TestUserConfiguration> getLoadProfile()
    {
        return loadProfile;
    }

    /**
     * Requests the agent process to stop voluntarily, but kills it forcefully after a certain amount of time.
     */
    private class StopThread extends Thread
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            // closing stdin of the agent process causes it to quit
            IOUtils.closeQuietly(stdin);

            try
            {
                // wait until the agent process monitor is dead (or the timeout is reached)
                monitor.join(30000);
            }
            catch (final InterruptedException ex)
            {
                // ignore
            }

            // if the agent process monitor is not dead yet, kill the agent process
            if (monitor.isAlive())
            {
                // terminate all descendant processes first
                process.descendants().forEach(ProcessHandle::destroyForcibly);
                // now terminate the agent process itself
                process.destroyForcibly();
            }
        }
    }
}
