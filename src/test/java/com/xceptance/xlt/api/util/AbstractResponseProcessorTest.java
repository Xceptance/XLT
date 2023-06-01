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
package com.xceptance.xlt.api.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.htmlunit.StringWebResponse;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.util.NameValuePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.xceptance.common.net.HttpHeaderConstants;

/**
 * Test the implementation of {@link AbstractResponseProcessor}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class AbstractResponseProcessorTest
{
    /**
     * test instance of AbstractResponseProcessor
     */
    private TestResponseProcessor proc;

    /**
     * Mocked WebResponse.
     */
    private WebResponse response;

    /**
     * Test initialization.
     */
    @Before
    public void intro()
    {
        proc = new TestResponseProcessor();
        response = Mockito.mock(WebResponse.class);
    }

    /**
     * Tests the implementation of AbstractResponseProcessor.createWebResponse(WebResponse,byte[]).
     */
    @Test
    public void testCreateWebResponse_WebResponseByteArray() throws Throwable
    {
        // response URL
        final URL u = new URL("http://localhost/");
        // response headers
        final NameValuePair[] headers = new NameValuePair[]
            {
                new NameValuePair("content-type", "text/html")
            };

        // let mock object return some useful data
        final WebRequest webRequest = new WebRequest(u);
        webRequest.setCharset(StandardCharsets.UTF_8);
        Mockito.when(response.getWebRequest()).thenReturn(webRequest);
        Mockito.when(response.getStatusCode()).thenReturn(200);
        Mockito.when(response.getStatusMessage()).thenReturn("Frozen");
        Mockito.when(response.getLoadTime()).thenReturn(123L);
        Mockito.when(response.getResponseHeaders()).thenReturn(Arrays.asList(headers));

        // make the call
        final WebResponse r = proc.makeWebResponse(response, "Test".getBytes());

        // validation
        Assert.assertNotNull(r);
        Assert.assertEquals(StandardCharsets.ISO_8859_1, r.getContentCharset());
        Assert.assertEquals("Test", r.getContentAsString());
        Assert.assertEquals(200, r.getStatusCode());
        Assert.assertEquals("Frozen", r.getStatusMessage());
        Assert.assertEquals(123L, r.getLoadTime());
        Assert.assertEquals(u, r.getWebRequest().getUrl());

        final List<NameValuePair> nHeaders = r.getResponseHeaders();
        Assert.assertEquals(1, nHeaders.size());
        Assert.assertEquals(headers[0], nHeaders.get(0));
    }

    /**
     * Tests the implementation of AbstractResponseProcessor.createWebResponse(WebResponse,byte[]) by passing a null
     * reference as 'originalWebResponse' parameter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateWebResponse_WebResponseByteArray_OriginalResponseIsNull()
    {
        proc.makeWebResponse(null, "Any String".getBytes());
    }

    /**
     * Tests the implementation of AbstractResponseProcessor.createWebResponse(WebResponse,String).
     * 
     * @throws MalformedURLException
     */
    @Test
    public void testCreateWebResponse_WebResponseString() throws MalformedURLException
    {
        response = new StringWebResponse("xxx", StandardCharsets.UTF_8, new URL("http://localhost"));

        // make the call
        final String newContent = "Any String";
        final WebResponse r = proc.makeWebResponse(response, newContent);

        // just validate content character set since the call delegates to
        // createWebResponse(WebResponse,byte[])
        Assert.assertEquals(StandardCharsets.UTF_8, r.getContentCharset());
        Assert.assertEquals(newContent, r.getContentAsString());
    }

    /**
     * Tests the implementation of AbstractResponseProcessor.createWebResponse(WebResponse,String) by passing a null
     * reference as 'originalWebResponse' parameter.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateWebResponse_WebResponseString_OriginalResponseIsNull()
    {
        proc.makeWebResponse(null, "Any String");
    }

    @Test
    public void testCreateWebResponse_ContentLength()
    {
        final List<NameValuePair> headers = Arrays.asList(new NameValuePair(HttpHeaderConstants.CONTENT_LENGTH, "1024"));

        Mockito.when(response.getResponseHeaders()).thenReturn(headers);
        Mockito.when(response.getStatusCode()).thenReturn(200);
        Mockito.when(response.getStatusMessage()).thenReturn("OK");

        final byte[] testContent = "TEST".getBytes();
        final WebResponse r = proc.makeWebResponse(response, testContent);

        Assert.assertNotNull("No response returned", r);
        // validate un-effected properties
        Assert.assertEquals(200, r.getStatusCode());
        Assert.assertEquals("OK", r.getStatusMessage());

        // make sure that content length header is correct
        Assert.assertEquals(Integer.toString(testContent.length), r.getResponseHeaderValue(HttpHeaderConstants.CONTENT_LENGTH));
    }

    /**
     * Dummy implementation of {@link AbstractResponseProcessor} which provides delegates to protected
     * <tt>createWebResponse()</tt> methods.
     */
    private static class TestResponseProcessor extends AbstractResponseProcessor
    {
        @Override
        public WebResponse processResponse(final WebResponse response)
        {
            return response;
        }

        private WebResponse makeWebResponse(final WebResponse origResponse, final String content)
        {
            return createWebResponse(origResponse, content);
        }

        private WebResponse makeWebResponse(final WebResponse origResponse, final byte[] content)
        {
            return createWebResponse(origResponse, content);
        }
    }
}
