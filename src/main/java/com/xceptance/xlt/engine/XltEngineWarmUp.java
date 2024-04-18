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
package com.xceptance.xlt.engine;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.resultbrowser.RequestHistory.DumpMode;

/**
 * Utility to warm the XLT framework up. Warming the framework (including the JVM) up is only necessary if you need
 * reliable measurements right from the beginning, for example for monitoring test cases which are being run only once
 * per JVM. For load tests, an explicit warm-up is typically not needed as there is a ramp-up period anyway.
 * <p>
 * To warm the framework up explicitly, add a static initializer block to all your test cases (or a super class) as
 * shown below:
 * 
 * <pre>
 * public class TVisit extends AbstractTestCase
 * {
 *     static
 *     {
 *         XltEngineWarmUp.execute();
 *     }
 * 
 *     &#64;Test
 *     public void test()
 *     {
 *         // ...
 *     }
 * }
 * </pre>
 * 
 * This way, the warm-up is part of all your test cases, but the actual procedure will be performed for the first
 * invocation only.
 */
public class XltEngineWarmUp
{
    /**
     * The log facility of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(XltEngineWarmUp.class);

    /**
     * Whether the warm-up was already performed.
     */
    private static boolean alreadyRun;

    /**
     * The port used by the internal HTTP server.
     */
    private static int serverPort;

    /**
     * Performs the warm-up of the XLT framework. The warm-up will be run only once per JVM. This method returns
     * immediately when being called a second time.
     */
    public static synchronized void execute()
    {
        if (!alreadyRun)
        {
            alreadyRun = true;

            // get the warm-up settings
            final XltProperties props = XltProperties.getInstance();
            final String prefix = XltConstants.XLT_PACKAGE_PATH + "warmUp.";
            final boolean enabled = true; // TODO: props.getProperty(prefix + "enabled", false);
            final int iterations = props.getProperty(prefix + "iterations", 10);

            // perform warm-up if enabled
            if (enabled)
            {
                LOG.debug("Performing warm-up...");

                // create an embedded HTTP server
                final Server server = createServer();

                // start the HTTP server
                try
                {
                    server.start();
                }
                catch (final Exception e)
                {
                    LOG.error("Warm-up: Failed to start HTTP server", e);
                }

                // retrieve the port of the HTTP server once it was started
                serverPort = ((ServerConnector) server.getConnectors()[0]).getLocalPort();

                // warm the framework up
                performWarmUp(iterations);

                // stop the HTTP server
                try
                {
                    server.stop();
                }
                catch (final Exception e)
                {
                    LOG.error("Warm-up: Failed to stop HTTP server", e);
                }
            }
        }
    }

    /**
     * Creates a simple embedded HTTP server that serves content from the class path.
     *
     * @return the server
     */
    private static Server createServer()
    {
        final Resource resource = Resource.newClassPathResource("/com/xceptance/xlt/engine/warmup/");

        final ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setBaseResource(resource);

        final HandlerList handlers = new HandlerList(resourceHandler, new DefaultHandler());

        final Server server = new Server(0); // choose a free port
        server.setHandler(handlers);

        return server;
    }

    /**
     * Runs the warm-up scenario repeatedly.
     *
     * @param iterations
     *            the number of warm-up iterations
     */
    private static void performWarmUp(final int iterations)
    {
        // disable the logging of measurements and the creation of a result browser
        final SessionImpl session = SessionImpl.getCurrent();
        session.getDataManager().setLoggingEnabled(false);
        session.getRequestHistory().setDumpMode(DumpMode.NEVER);

        // run the warm-up scenario repeatedly
        for (int i = 0; i < iterations; i++)
        {
            LOG.debug("Warm-up iteration #" + i);

            final Result result = JUnitCore.runClasses(WarmUp.class);
            if (result.getFailureCount() > 0)
            {
                LOG.error("Warm-up: Scenario failed", result.getFailures().get(0).getException());
            }
        }

        // clean up the current session
        SessionImpl.removeCurrent();
    }

    /**
     * Test scenario used for warming up the XLT framework. Reads an HTML page from an embedded HTTP server.
     */
    public static class WarmUp extends AbstractWebDriverScriptTestCase
    {
        public WarmUp()
        {
            super(new XltDriver(true), "http://localhost:" + serverPort);
        }

        @Test
        public void test()
        {
            startAction("WarmUp");
            open("index.html");
            assertTitle("Warm-Up");
        }

        @After
        public void cleanUp()
        {
            getWebDriver().quit();
        }
    }
}
