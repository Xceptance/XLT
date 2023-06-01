/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import org.htmlunit.MockWebConnection;
import org.htmlunit.html.HtmlPage;
import org.junit.After;

import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.XltWebClient;

/**
 * Base class of all tests that require at least a web client.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public abstract class AbstractWebTestCase extends AbstractXLTTestCase
{
    /**
     * The test server port. Defaults to <code>4444</code>.
     */
    protected final static int PORT = XltProperties.getInstance().getProperty("test.server.port", 4444);

    /**
     * The default URL used in tests.
     */
    private static final URL DEFAULT_URL;

    /**
     * MockWebConnection used by web client or server.
     */
    private MockWebConnection connection;

    /**
     * The web client used by this test.
     */
    private XltWebClient webClient;

    static
    {
        try
        {
            DEFAULT_URL = new URL("http://localhost:" + PORT + "/");
        }
        catch (final MalformedURLException mue)
        {
            throw new RuntimeException("Failed to create default URL", mue);
        }
    }

    /**
     * Returns the default test URL.
     * 
     * @return default URL
     */
    protected URL getDefaultUrl()
    {
        return DEFAULT_URL;
    }

    /**
     * Returns the MockWebConnection instance used by this test. Creates a new one if necessary.
     * 
     * @return MockWebConnection instance
     */
    protected MockWebConnection getMockConnection()
    {
        if (connection == null)
        {
            connection = new XltMockWebConnection(getWebClient());
        }
        return connection;
    }

    /**
     * Returns the web client to use by this test. Creates a new one if necessary.
     * 
     * @return the web client
     */
    protected XltWebClient getWebClient()
    {
        if (webClient == null)
        {
            webClient = createWebClient();
            configureNewWebClient(webClient);
        }
        return webClient;
    }

    /**
     * Called when creating a new web client. Override if necessary.
     * 
     * @return new web client
     */
    protected XltWebClient createWebClient()
    {
        return new XltWebClient();
    }

    /**
     * Configures the web client created by {@link #createWebClient()}. Override if necessary.
     * 
     * @param webClient
     *            the new web client
     */
    protected void configureNewWebClient(final XltWebClient webClient)
    {
        webClient.setTimerName("TimerName");
        webClient.setLoadStaticContent(true);
        webClient.getOptions().setJavaScriptEnabled(true);
    }

    protected HtmlPage loadPage(final URL url, final String html, final String contentType, final String charSet) throws Exception
    {
        getMockConnection().setResponse(url, html, contentType, Charset.forName(charSet));
        getWebClient().setWebConnection(getMockConnection());

        return getWebClient().getPage(url);
    }

    protected HtmlPage loadPage(final URL url, final String html) throws Exception
    {
        return loadPage(url, html, "text/html", "UTF-8");
    }

    protected HtmlPage loadPage(final String html) throws Exception
    {
        return loadPage(getDefaultUrl(), html);
    }

    /**
     * Clean-up routine. Releases all resources held by this instance.
     */
    @After
    public void releaseResources()
    {
        if (webClient != null)
        {
            webClient.shutdown();
        }

        webClient = null;
        connection = null;
    }
}
