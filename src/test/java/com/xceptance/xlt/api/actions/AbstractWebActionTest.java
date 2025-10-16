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
package com.xceptance.xlt.api.actions;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.htmlunit.HttpMethod;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.util.NameValuePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.xceptance.xlt.AbstractXLTTestCase;
import com.xceptance.xlt.api.util.ResponseContentProcessor;
import com.xceptance.xlt.api.util.ResponseProcessor;
import com.xceptance.xlt.engine.XltWebClient;

/**
 * Test the implementation of {@link AbstractWebAction}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class AbstractWebActionTest extends AbstractXLTTestCase
{

    private AbstractWebAction action;

    @Before
    public void setUp()
    {
        action = new TestWebAction();
    }

    /**
     * Test {@link AbstractWebAction#closeWebClient()}.
     */
    @Test
    public void testCloseWebClient()
    {
        action.closeWebClient();

        try
        {
            action.getWebClient();
            Assert.fail("getWebClient() should throw an exception!");
        }
        catch (final IllegalStateException e)
        {
            // That is exactly what should happen if calling the web client after it has been explicitly closed.
        }
    }

    /**
     * Tests {@link AbstractWebAction#closeWebClient()} may safely be called more than once.
     */
    @Test
    public void testCloseWebClientMoreThanOnce()
    {
        action.closeWebClient();
        action.closeWebClient();
    }

    /**
     * Test {@link AbstractWebAction#run()}.
     */
    @Test
    public void testRun()
    {
        try
        {
            action.run();
            Assert.assertEquals(action.getClass().getName(), ((XltWebClient) action.getWebClient()).getTimerName());
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }

    }

    /**
     * Test {@link AbstractWebAction#addResponseProcessor(ResponseProcessor)}.
     */
    @Test
    public void testAddResponseProcessor()
    {
        try
        {
            final ResponseProcessor proc = new ResponseContentProcessor("\\d", "!");
            action.addResponseProcessor(proc);
            final WebResponse r = mock(WebResponse.class);

            final String content = "Test 123";
            Mockito.when(r.getContentAsString()).thenReturn(content);
            Mockito.when(r.getWebRequest()).thenReturn(new WebRequest(new URL("http://localhost")));
            Mockito.when(r.getContentCharset()).thenReturn(StandardCharsets.UTF_8);

            Assert.assertEquals(content.replaceAll("\\d", "!"),
                                ((XltWebClient) action.getWebClient()).processResponse(r).getContentAsString());
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }
    }

    @Test
    public void testCreateWebRequestSettings() throws MalformedURLException
    {
        final TestWebAction twa = new TestWebAction();
        String baseUrl = "http://www.heise.de/";
        URL url = new URL(baseUrl);

        final List<NameValuePair> requestParameters = new ArrayList<NameValuePair>(3);
        requestParameters.add(new NameValuePair("user", "XceptanceSoftwareTechnologiesGmbH"));
        requestParameters.add(new NameValuePair("password", "thereIsNoPasswordHere"));
        final String timeStamp = String.valueOf(System.currentTimeMillis());
        requestParameters.add(new NameValuePair("timeStamp", timeStamp));
        /* Hard coded, has to match the NameValuePairs. */
        final String query = "user=XceptanceSoftwareTechnologiesGmbH&password=thereIsNoPasswordHere&timeStamp=" + timeStamp;

        /* Request 1, using POST. */
        WebRequest request = twa.createWebRequestSettings(url, HttpMethod.POST, requestParameters);
        Assert.assertEquals("Wrong URL constructed", baseUrl, request.getUrl().toString());

        /* Request 2, not using POST but having no query which is already coded into the URL */
        request = twa.createWebRequestSettings(url, HttpMethod.GET, requestParameters);
        Assert.assertEquals("Wrong URL constructed", baseUrl + "?" + query, request.getUrl().toString());

        /* Request 2, not using POST but having a query which is already coded into the URL */
        baseUrl += "?milk=no";
        url = new URL(baseUrl);
        request = twa.createWebRequestSettings(url, HttpMethod.GET, requestParameters);
        Assert.assertEquals("Wrong URL constructed", baseUrl + "&" + query, request.getUrl().toString());
    }

    /**
     * Dummy implementation of {@link AbstractWebAction}.
     * <p>
     * Used to be able to instantiate AbstractWebAction.
     */
    private class TestWebAction extends AbstractWebAction
    {
        public TestWebAction()
        {
            this(TestWebAction.class.getName(), null);
        }

        public TestWebAction(final String name, final AbstractWebAction action)
        {
            super(action, name);
        }

        @Override
        protected void execute() throws Exception
        {
        }

        @Override
        protected void postValidate() throws Exception
        {
        }

        @Override
        public void preValidate() throws Exception
        {
        }

        @Override
        protected WebRequest createWebRequestSettings(final URL url, final HttpMethod method, final List<NameValuePair> requestParameters)
            throws MalformedURLException
        {
            return super.createWebRequestSettings(url, method, requestParameters);
        }
    }

}
