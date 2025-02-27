/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.htmlunit.BrowserVersion;
import org.htmlunit.MockWebConnection;
import org.htmlunit.WebConnection;
import org.htmlunit.WebResponse;
import org.htmlunit.javascript.AbstractJavaScriptEngine;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.common.collection.ConcurrentLRUCache;
import com.xceptance.xlt.AbstractXLTTestCase;
import com.xceptance.xlt.XltMockWebConnection;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.util.ResponseProcessor;
import com.xceptance.xlt.api.util.XltProperties;

import util.lang.ClassFromByteArrayLoader;
import util.xlt.properties.ReversibleChangePipeline;

/**
 * Tests the implementation of the class {@link XltWebClientTest}. There are more tests in the testsuite-xlt project
 * cause they require a running web application that has the access controls been set.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class XltWebClientTest extends AbstractXLTTestCase
{
    @AfterClass
    public static void afterClass()
    {
        // clean-up
        XltEngine.reset();
        SessionImpl.removeCurrent();
    }

    /**
     * Test setup. Primarily used for setting required properties.
     *
     * @throws Throwable
     */
    @Before
    public void setUp()
    {
        final XltProperties props = XltProperties.getInstance();

        props.setProperty("com.xceptance.xlt.output2disk", "always");
        props.setProperty("com.xceptance.xlt.loadStaticContent", "true");
        props.setProperty("com.xceptance.xlt.cssEnabled", "true");
    }

    /**
     * Tests the handling of default ports.
     *
     * @throws Throwable
     *             thrown on test failure
     */
    @Test
    public void testHandlingOfDefaultPorts() throws Throwable
    {
        // dummy page content used for test URLs
        final String response = "<html><head><title>Test</title><link type=\"text/css\" rel=\"stylesheet\" href=\"dummy.css\" /></head><body></body></html>";
        // dummy CSS content
        final String dummyCSS = "";

        // test URLs
        final URL testUrl1 = new URL("http://localhost/test1?foo=bar&foo=baz&q=#anchor");
        final URL testUrl2 = new URL("https://localhost/test2foo=bar&foo=baz&q=#anchor2");

        final URLCollector collector = new URLCollector();

        //
        // === Test for http/80 and https/443 ===
        //
        AbstractHtmlPageAction action = new OpenPageAction("http://localhost:80/test1?foo=bar&foo=baz&q=#anchor");
        ((XltWebClient) action.getWebClient()).addResponseProcessor(collector);

        // create mocked web connection and set the response for the 1st test URL
        MockWebConnection conn = new XltMockWebConnection((XltWebClient) action.getWebClient());
        conn.setResponse(testUrl1, response);

        // set stylesheet response
        conn.setResponse(new URL("http://localhost/dummy.css"), dummyCSS);

        ((XltWebClient) action.getWebClient()).setWebConnection(conn);
        action.run();

        action = new OpenPageAction("https://localhost:443/test2foo=bar&foo=baz&q=#anchor2");
        ((XltWebClient) action.getWebClient()).addResponseProcessor(collector);

        // create mocked web connection and set the response for the 2nd test URL
        conn = new XltMockWebConnection((XltWebClient) action.getWebClient());
        conn.setResponse(testUrl2, response);

        // set stylesheet response
        conn.setResponse(new URL("https://localhost/dummy.css"), dummyCSS);

        ((XltWebClient) action.getWebClient()).setWebConnection(conn);
        action.run();

        // mocked web connection should fail for an unset URL/response pair, but we're paranoid and check for any
        // specified port
        for (final URL u : collector.getUrls())
        {
            if (u.getPort() > 0)
            {
                Assert.fail("Incorrect handling of default ports for URL '" + u.toExternalForm() + "'.");
            }
        }

        // URL for the case we want to load an URL using a non-default port
        final URL testUrl3 = new URL("http://localhost:8080/test3?someQuery=someValue&a=#someAnchor");

        collector.clear();

        //
        // === Test for non-default port ===
        //
        action = new OpenPageAction(testUrl3.toExternalForm());
        ((XltWebClient) action.getWebClient()).addResponseProcessor(collector);

        conn = new XltMockWebConnection((XltWebClient) action.getWebClient());
        conn.setResponse(testUrl3, response);
        conn.setResponse(new URL("http://localhost:8080/dummy.css"), dummyCSS);

        ((XltWebClient) action.getWebClient()).setWebConnection(conn);
        action.run();

        // again, our paranoia forces us to check the port of all loaded URLs
        for (final URL u : collector.getUrls())
        {
            Assert.assertEquals(8080, u.getPort());
        }
    }

    @Test
    public void testStaticInitializationBlock()
    {
        final ReversibleChangePipeline rcp = new ReversibleChangePipeline();

        rcp.addAndApply("com.xceptance.xlt.js.cache.size", ConcurrentLRUCache.MIN_SIZE - 1);
        rcp.addAndApply("com.xceptance.xlt.css.cache.size", ConcurrentLRUCache.MIN_SIZE - 1);
        ClassFromByteArrayLoader.getFreshlyLoadedClass(XltWebClient.class);

        rcp.reverseAll();
    }

    @Test
    public void testBrowserVersion()
    {
        checkBrowserVersion("CH", BrowserVersion.CHROME);
        checkBrowserVersion("EDGE", BrowserVersion.EDGE);
        checkBrowserVersion("FF", BrowserVersion.FIREFOX);
        checkBrowserVersion("FF_ESR", BrowserVersion.FIREFOX_ESR);
        checkBrowserVersion("IE", BrowserVersion.INTERNET_EXPLORER);
        checkBrowserVersion("", BrowserVersion.FIREFOX);
        checkBrowserVersion("XYZ", BrowserVersion.FIREFOX);
    }

    private void checkBrowserVersion(final String key, final BrowserVersion expected)
    {
        final XltProperties props = XltProperties.getInstance();

        final String propName = "com.xceptance.xlt.browser";
        final String origPropValue = props.getProperty(propName);

        props.setProperty(propName, key);

        try (final XltWebClient xltWebClient = new XltWebClient())
        {
            Assert.assertEquals("Wrong browser version, ", expected.getNickname(), xltWebClient.getBrowserVersion().getNickname());
        }
        finally
        {
            if (origPropValue == null)
            {
                props.removeProperty(propName);
            }
            else
            {
                props.setProperty(propName, origPropValue);
            }
        }
    }

    @Test
    public void testReset()
    {
        try (final XltWebClient webClient = new XltWebClient(BrowserVersion.BEST_SUPPORTED, true))
        {
            // prevalidation
            final WebConnection webConnection = webClient.getWebConnection();
            final AbstractJavaScriptEngine<?> javaScriptEngine = webClient.getJavaScriptEngine();

            Assert.assertEquals("Unexpected WebConnection class", XltHttpWebConnection.class, webConnection.getClass());
            Assert.assertEquals("Unexpected JavaScriptEngine class", XltJavaScriptEngine.class, javaScriptEngine.getClass());

            // reset
            webClient.reset();

            // validate that web connection and javascript engine are still XLT classes, but different objects
            final WebConnection newWebConnection = webClient.getWebConnection();
            final AbstractJavaScriptEngine<?> newJavaScriptEngine = webClient.getJavaScriptEngine();

            Assert.assertEquals("Unexpected WebConnection class", XltHttpWebConnection.class, newWebConnection.getClass());
            Assert.assertEquals("Unexpected JavaScriptEngine class", XltJavaScriptEngine.class, newJavaScriptEngine.getClass());

            Assert.assertNotEquals("WebConnection is the same as before", webConnection, newWebConnection);
            Assert.assertNotEquals("JavaScriptEngine is the same as before", javaScriptEngine, newJavaScriptEngine);
        }
    }

    /**
     * Response processor which simple stores the request URLs.
     */
    static class URLCollector implements ResponseProcessor
    {
        /**
         * Collected URLs. This has to be a synchronized set because some of the processing runs in another thread and
         * hence we might experiencene false sharing otherwise, mainly because a response processor is not designed to
         * be a data collector
         */
        private final Set<URL> urls = Collections.synchronizedSet(new HashSet<URL>());

        /**
         * {@inheritDoc}
         */
        @Override
        public WebResponse processResponse(final WebResponse webResponse)
        {
            urls.add(webResponse.getWebRequest().getUrl());

            return webResponse;
        }

        /**
         * Returns the collected URLs.
         *
         * @return collected URLs
         */
        public Set<URL> getUrls()
        {
            return Collections.unmodifiableSet(urls);
        }

        /**
         * Clears the set of collected URLs.
         */
        public void clear()
        {
            urls.clear();
        }
    }
}
