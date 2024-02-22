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
/*
 * File: _2810_DownloadContentTest.java
 * Created on: Nov 3, 2016
 */
package org.htmlunit;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.xceptance.xlt.AbstractWebTestCase;
import com.xceptance.xlt.engine.XltHttpWebConnection;
import com.xceptance.xlt.engine.XltWebClient;

import util.xlt.properties.ReversibleChange;

/**
 * Tests the socket timeout behavior of {@link HttpWebConnection}.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class _2810_DownloadResponseContentTest extends AbstractWebTestCase
{
    private static Server Server;

    private final ReversibleChange rc = new ReversibleChange("com.xceptance.xlt.timeout", "5000");

    @BeforeClass
    public static void startServer() throws Throwable
    {

        final WebAppContext ctx = new WebAppContext();
        ctx.setResourceBase("./");
        ctx.setContextPath("/");

        ctx.addServlet(WaitingServlet.class, "/*");

        Server = new Server(PORT);
        Server.setHandler(ctx);
        Server.start();
    }

    @AfterClass
    public static void stopServer() throws Throwable
    {
        try
        {
            if (Server != null && !Server.isStopping())
            {
                Server.stop();
            }
        }
        finally
        {
            Server = null;
        }
    }

    @Before
    public void setTimeout()
    {
        rc.apply();
    }

    @After
    public void resetTimeout()
    {
        rc.reverse();
    }

    @Test(expected = IOException.class)
    public void readingStreamTimesOut() throws Throwable
    {
        try (final XltWebClient wc = new XltWebClient())
        {
            final XltHttpWebConnection xltConn = (XltHttpWebConnection) wc.getWebConnection();
            final HttpWebConnection conn = (HttpWebConnection) xltConn.getWrappedWebConnection();
            final HttpWebConnection spy = Mockito.spy(conn);
            wc.setWebConnection(spy);
            try
            {
                wc.setTimerName("LoadResponse");
                wc.getPage(getDefaultUrl());
            }
            finally
            {
                Mockito.verify(spy, Mockito.atLeastOnce()).downloadResponseBody(Mockito.<HttpResponse>any());
            }
        }
    }

    /**
     * Servlet used to simulate a busy server that needs a meaningful amount of time to serve the requested resource.
     */
    public static class WaitingServlet extends HttpServlet
    {

        /**
         * Default serialVersionUID.
         */
        private static final long serialVersionUID = 1L;

        /**
         * {@inheritDoc}
         */
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            resp.setContentType("text/html; charset=UTF-8");

            try
            {
                final PrintWriter out = resp.getWriter();
                out.write("<html><head><title>Test me</title></head><body><p>Some text that is</p>");
                out.write("\r\n");
                out.write(RandomStringUtils.randomAlphanumeric(24254643));
                Thread.sleep(10000);
                out.write("\r\nto continue </body></html>");
                out.close();
            }
            catch (final InterruptedException ie)
            {
                throw new ServletException(ie);
            }

        }

    }
}
