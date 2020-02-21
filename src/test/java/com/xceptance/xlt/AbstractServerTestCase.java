/*
 * File: AbstractServerTestCase.java
 * Created on: Jun 20, 2014
 * 
 * Copyright 2014
 * Xceptance Software Technologies GmbH, Germany.
 */
package com.xceptance.xlt;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.After;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.MockWebConnection.RawResponseData;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.util.UrlUtils;

/**
 * Base class of all tests that require a server.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractServerTestCase extends AbstractWebTestCase
{
    /**
     * The instance-specific web-server.
     */
    private Server server;

    /**
     * Static web server shared by all test instances.
     */
    private static Server STATIC_SERVER;

    @Override
    protected HtmlPage loadPage(final URL url, final String html, final String contentType, final String charSet) throws Exception
    {
        getMockConnection().setResponse(url, html, contentType, Charset.forName(charSet));

        startServer(getMockConnection());
        return getWebClient().getPage(url);
    }

    protected void startServer(final int port, final String resourceBase) throws Exception
    {
        final WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setResourceBase(resourceBase);

        final ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(resourceBase);
        resourceHandler.getMimeTypes().addMimeMapping("js", "application/javascript");

        final HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]
            {
                resourceHandler, context
            });

        server = new Server(port);
        server.setHandler(handlers);
        server.setHandler(resourceHandler);
        server.start();
    }

    protected void startServer(final String resourceBase) throws Exception
    {
        startServer(PORT, resourceBase);
    }

    protected static synchronized void startServer(final MockWebConnection conn) throws Exception
    {
        if (STATIC_SERVER == null)
        {
            STATIC_SERVER = new Server(PORT);
            final WebAppContext ctx = new WebAppContext();
            ctx.setContextPath("/");
            ctx.setResourceBase("./");

            ctx.addServlet(MockWebConnServlet.class, "/*");
            STATIC_SERVER.setHandler(ctx);
            STATIC_SERVER.start();
        }
        MockWebConnServlet.setWebConnection(conn);
    }

    /**
     * Executes test clean-up routines.
     */
    @After
    public void tearDown() throws Exception
    {
        if (server != null)
        {
            server.stop();
        }
        server = null;
        stopWebServer();
    }

    protected static synchronized void stopWebServer() throws Exception
    {
        if (STATIC_SERVER != null)
        {
            STATIC_SERVER.stop();
        }
        STATIC_SERVER = null;
    }

    protected static String getModifiedContent(final String html)
    {
        return html;
    }

    public static class MockWebConnServlet extends HttpServlet
    {
        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 1L;

        /**
         * Globally shared web connection.
         */
        private static MockWebConnection SERVLET_WEBCONNECTION;

        public static void setWebConnection(final MockWebConnection conn)
        {
            SERVLET_WEBCONNECTION = conn;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
        {
            try
            {
                handle(req, resp);
            }
            catch (final ServletException se)
            {
                throw se;
            }
            catch (final IOException ioe)
            {
                throw ioe;
            }
            catch (final Exception e)
            {
                throw new ServletException(e);
            }
        }

        private void handle(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
        {
            // build the URL string
            final String url;
            {
                final StringBuffer sb = req.getRequestURL();
                final String queryString = req.getQueryString();
                if (queryString != null)
                {
                    sb.append("?").append(UrlUtils.decode(queryString));
                }
                url = sb.toString();
            }

            // copy parameters
            final List<NameValuePair> requestParameters = new ArrayList<NameValuePair>();
            for (final Enumeration<String> paramNames = req.getParameterNames(); paramNames.hasMoreElements();)
            {
                final String name = paramNames.nextElement();
                final String[] values = req.getParameterValues(name);
                for (final String value : values)
                {
                    requestParameters.add(new NameValuePair(name, value));
                }
            }

            final URL requestedUrl = new URL(url);
            final WebRequest webRequest = new WebRequest(requestedUrl);
            webRequest.setHttpMethod(HttpMethod.valueOf(req.getMethod()));

            // copy headers
            for (final Enumeration<String> en = req.getHeaderNames(); en.hasMoreElements();)
            {
                final String headerName = en.nextElement();
                final String headerValue = req.getHeader(headerName);
                webRequest.setAdditionalHeader(headerName, headerValue);
            }

            if (requestParameters.isEmpty() && req.getContentLength() > 0)
            {
                final byte[] buffer = new byte[req.getContentLength()];
                req.getInputStream().read(buffer, 0, buffer.length);
                webRequest.setRequestBody(new String(buffer, webRequest.getCharset()));
            }
            else
            {
                webRequest.setRequestParameters(requestParameters);
            }

            final RawResponseData data = SERVLET_WEBCONNECTION.getRawResponse(webRequest);

            // write WebResponse to HttpServletResponse
            resp.setStatus(data.getStatusCode());

            boolean charsetInContentType = false;
            for (final NameValuePair responseHeader : data.getHeaders())
            {
                final String headerName = responseHeader.getName();
                if ("Content-Type".equals(headerName) && responseHeader.getValue().contains("charset="))
                {
                    charsetInContentType = true;
                }
                resp.addHeader(headerName, responseHeader.getValue());
            }

            if (data.getByteContent() != null)
            {
                resp.getOutputStream().write(data.getByteContent());
            }
            else
            {
                final String newContent = getModifiedContent(data.getStringContent());
                if (!charsetInContentType)
                {
                    resp.setCharacterEncoding(data.getCharset().name());
                }
                resp.getWriter().print(newContent);
            }
            resp.flushBuffer();
        }
    }
}
