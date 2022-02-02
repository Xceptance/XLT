/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.gargoylesoftware.htmlunit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Tests the presence of 'Content-Length' and 'Content-Encoding' headers according to issue #2649.
 */
public class _2649_ResponseHeaderTest
{
    @Test
    public void testContentHeadersPresent() throws Exception
    {
        final Map<String, Class<? extends Servlet>> servlets = new HashMap<>();
        servlets.put("/", MyServlet.class);
        final Server server = createWebServer(5555, "/", (String[]) null, servlets, null);

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {

            final HtmlPage page = wc.getPage("http://localhost:5555/");
            Assert.assertNotNull("Failed to load page", page);

            assertResponseHeader("Content-Type", page);
            assertResponseHeader("Content-Encoding", page);
            assertResponseHeader("Content-Length", page);
        }
        finally
        {
            server.stop();
        }
    }

    /**
     * Makes sure that given page's response contains the given header.
     * 
     * @param header
     * @param page
     */
    private static void assertResponseHeader(final String header, final HtmlPage page)
    {
        Assert.assertNotNull("Response header '" + header + "' is missing", page.getWebResponse().getResponseHeaderValue(header));
    }

    /**
     * Creates and starts a new Jetty server. Code taken from HtmlUnit test base class
     * <code>com.gargoylesoftware.htmlunit.WebServerTestCase</code>.
     * 
     * @param port
     * @param resourceBase
     * @param classpath
     * @param servlets
     * @param handler
     * @return
     * @throws Exception
     */
    private static Server createWebServer(final int port, final String resourceBase, final String[] classpath,
                                          final Map<String, Class<? extends Servlet>> servlets, final HandlerWrapper handler)
        throws Exception
    {

        final Server server = new Server(port);

        final WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setResourceBase(resourceBase);

        if (servlets != null)
        {
            for (final Map.Entry<String, Class<? extends Servlet>> entry : servlets.entrySet())
            {
                final String pathSpec = entry.getKey();
                final Class<? extends Servlet> servlet = entry.getValue();
                context.addServlet(servlet, pathSpec);

                // disable defaults if someone likes to register his own root servlet
                if ("/".equals(pathSpec))
                {
                    context.setDefaultsDescriptor(null);
                    context.addServlet(DefaultServlet.class, "/favicon.ico");
                }
            }
        }

        final WebAppClassLoader loader = new WebAppClassLoader(context);
        if (classpath != null)
        {
            for (final String path : classpath)
            {
                loader.addClassPath(path);
            }
        }
        context.setClassLoader(loader);
        if (handler != null)
        {
            handler.setHandler(context);
            server.setHandler(handler);
        }
        else
        {
            server.setHandler(context);
        }
        server.start();
        return server;
    }

    /**
     * A dumb servlet for GET requests.
     */
    public static class MyServlet extends HttpServlet
    {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private static final String RESPONSE = "<html><head><title>Foo Bar</title></head><body>This is foo bar!!</body></html>";

        /**
         * {@inheritDoc}
         */
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            final byte[] bytes = RESPONSE.getBytes("UTF-8");
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final GZIPOutputStream gout = new GZIPOutputStream(bos);
            gout.write(bytes);
            gout.finish();

            final byte[] encoded = bos.toByteArray();

            resp.setContentType("text/html");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(200);
            resp.setContentLength(encoded.length);
            resp.setHeader("Content-Encoding", "gzip");

            final OutputStream rout = resp.getOutputStream();
            rout.write(encoded);
            rout.close();
        }
    }
}
