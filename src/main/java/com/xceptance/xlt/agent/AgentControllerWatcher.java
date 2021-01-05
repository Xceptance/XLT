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
