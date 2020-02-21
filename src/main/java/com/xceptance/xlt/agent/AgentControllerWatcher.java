package com.xceptance.xlt.agent;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Watcher thread that monitors the connection to the agent controller. If the connection is closed (either actively by
 * the agent controller, or forcefully by killing the agent controller), a currently running load test will be aborted.
 */
public class AgentControllerWatcher extends Thread
{
    private static final Log log = LogFactory.getLog(AgentControllerWatcher.class);

    private final LoadTest loadTest;

    public AgentControllerWatcher(final LoadTest loadTest)
    {
        super("AgentControllerConnectionMonitor");

        this.loadTest = loadTest;
    }

    @Override
    public void run()
    {
        log.debug("Agent controller watcher thread started.");

        try
        {
            System.in.read();

            log.info("Connection to agent controller was closed.");

            loadTest.abort();
        }
        catch (final IOException ex)
        {
            log.error("Error while watching the connection to the agent controller:", ex);
        }
    }
}
