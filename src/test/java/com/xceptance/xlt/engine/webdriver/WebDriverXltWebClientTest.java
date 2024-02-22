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
package com.xceptance.xlt.engine.webdriver;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.HttpMethod;
import org.htmlunit.MockWebConnection;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.WebWindow;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.protocol.about.Handler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.xceptance.xlt.AbstractXLTTestCase;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.HtmlPageUtils;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.engine.xltdriver.WebDriverXltWebClient;

/**
 * Tests the implementation of internal class {@link WebDriverXltWebClient}.
 * 
 * @author hardy (Xceptance Software Technologies GmbH)
 */
public class WebDriverXltWebClientTest extends AbstractXLTTestCase
{
    /**
     * Test instance.
     */
    protected WebDriverXltWebClient client;

    /**
     * XLT driver implementation used by test instance.
     */
    protected XltDriver driver;

    /**
     * Test fixture setup.
     */
    @Before
    public void intro()
    {
        Session.getCurrent().clear();

        driver = new XltDriver();
        client = (WebDriverXltWebClient) driver.getWebClient();
    }

    /**
     * Tests the implementation of {@link WebDriverXltWebClient#getTimerName()}.
     */
    @Test
    public void testGetTimerName()
    {
        // action name in driver currently not set -> timer name should be a default value
        Assert.assertEquals("UnsetTimerName", client.getTimerName());

        // set the name of the action
        final String actionName = "AnyAction";
        Session.getCurrent().startAction(actionName);

        // timer name should be equal to the action name set above
        Assert.assertEquals(actionName, client.getTimerName());
    }

    /**
     * Tests the implementation of
     * {@link WebDriverXltWebClient#getPage(WebWindow, WebRequest)}
     * using 'about:blank' as request URL.
     */
    @Test
    public void testGetPage_AboutBlank() throws FailingHttpStatusCodeException, IOException
    {
        final WebRequest settings = new WebRequest(new URL(null, "about:blank", new Handler()));

        final WebWindow window = Mockito.mock(WebWindow.class);
        Mockito.doReturn(client).when(window).getWebClient();

        final WebResponse webResponse = Mockito.mock(WebResponse.class);
        Mockito.doReturn(settings).when(webResponse).getWebRequest();

        final HtmlPage page = new HtmlPage(webResponse, window);
        Mockito.doReturn(page).when(window).getEnclosedPage();

        Assert.assertEquals(page, client.getPage(window, settings));
    }

    /**
     * Tests the implementation of {@link WebDriverXltWebClient#getPage(WebWindow, WebRequest)} using a
     * MockWebConnection.
     */
    @Test
    public void testGetPage() throws FailingHttpStatusCodeException, IOException
    {
        final MockWebConnection webConnection = new MockWebConnection();
        final URL url = new URL("http://www.example.com");
        webConnection.setResponse(url, "<html><body><p>Dummy Text</p></body></html>", "text/html");
        client.setWebConnection(webConnection);

        final WebRequest settings = Mockito.mock(WebRequest.class);
        Mockito.doReturn(url).when(settings).getUrl();
        Mockito.doReturn(StandardCharsets.UTF_8).when(settings).getCharset();
        Mockito.doReturn(HttpMethod.GET).when(settings).getHttpMethod();

        final HtmlPage page = client.getPage(client.getCurrentWindow(), settings);

        Assert.assertNotNull("Page is null", page);
        Assert.assertFalse("Last request must not be failed", Session.getCurrent().hasFailed());
        final HtmlElement e = HtmlPageUtils.findSingleHtmlElementByXPath(page, "//p");
        Assert.assertEquals("Dummy Text", e.asNormalizedText());
    }

}
