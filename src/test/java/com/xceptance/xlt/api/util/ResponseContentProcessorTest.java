/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * Test the implementation of {@link ResponseContentProcessor}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class ResponseContentProcessorTest
{
    /**
     * Tests the implementation of {@link ResponseProcessor#processResponse(WebResponse)} by passing a response whose
     * request URL does not match the processor's URL pattern.
     * 
     * @throws Throwable
     */
    @Test
    public void testProcessResponse_UrlDoesNotMatch_Ctr1() throws Throwable
    {
        final ResponseContentProcessor proc = new ResponseContentProcessor("foo", "bar", "example");
        final WebResponse response = Mockito.mock(WebResponse.class);

        Mockito.doReturn(new WebRequest(new URL("http://localhost"))).when(response).getWebRequest();
        Assert.assertEquals(response, proc.processResponse(response));
    }

    @Test
    public void testProcessResponse_UrlDoesNotMatch_Ctr2() throws Throwable
    {
        final ResponseContentProcessor proc = new ResponseContentProcessor(Pattern.compile("foo"), "bar", Pattern.compile("example"));
        final WebResponse response = Mockito.mock(WebResponse.class);

        Mockito.doReturn(new WebRequest(new URL("http://localhost"))).when(response).getWebRequest();
        Assert.assertEquals(response, proc.processResponse(response));
    }

    /**
     * Tests the implementation of {@link ResponseProcessor#processResponse(WebResponse)} by passing a response whose
     * content does not match the processor's content pattern.
     * 
     * @throws Throwable
     */
    @Test
    public void testProcessResponse_ContentDoesNotMatch_Ctr1() throws Throwable
    {
        final ResponseContentProcessor proc = new ResponseContentProcessor("foo", "bar", "example");
        final WebResponse response = Mockito.mock(WebResponse.class);

        Mockito.doReturn(new WebRequest(new URL("http://localhost/example.html"))).when(response).getWebRequest();
        Mockito.doReturn("my test text!").when(response).getContentAsString();

        Assert.assertEquals(response, proc.processResponse(response));
        Assert.assertEquals("my test text!", response.getContentAsString());
    }

    @Test
    public void testProcessResponse_ContentDoesNotMatch_Ctr2() throws Throwable
    {
        final ResponseContentProcessor proc = new ResponseContentProcessor(Pattern.compile("foo"), "bar", Pattern.compile("example"));
        final WebResponse response = Mockito.mock(WebResponse.class);

        Mockito.doReturn(new WebRequest(new URL("http://localhost/example.html"))).when(response).getWebRequest();
        Mockito.doReturn("my test text!").when(response).getContentAsString();

        Assert.assertEquals(response, proc.processResponse(response));
        Assert.assertEquals("my test text!", response.getContentAsString());
    }

    @Test
    public void testProcessResponse_ContentDoesNotMatch_Ctr3() throws Throwable
    {
        final ResponseContentProcessor proc = new ResponseContentProcessor(Pattern.compile("foo"), "bar");
        final WebResponse response = Mockito.mock(WebResponse.class);

        Mockito.doReturn(new WebRequest(new URL("http://localhost/example.html"))).when(response).getWebRequest();
        Mockito.doReturn("my test text!").when(response).getContentAsString();

        Assert.assertEquals(response, proc.processResponse(response));
        Assert.assertEquals("my test text!", response.getContentAsString());
    }

    /**
     * Tests the implementation of {@link ResponseProcessor#processResponse(WebResponse)} by passing a response whose
     * content and request URL match the processor's content pattern and URL pattern respectively.
     * 
     * @throws Throwable
     */
    @Test
    public void testProcessResponse_Ctr1() throws Throwable
    {
        final ResponseContentProcessor proc = new ResponseContentProcessor("foo", "bar", "example");
        final WebResponse response = Mockito.mock(WebResponse.class);

        Mockito.doReturn(new WebRequest(new URL("http://localhost/example.html"))).when(response).getWebRequest();
        Mockito.doReturn("This is my test text: foo bar foobar.").when(response).getContentAsString();
        Mockito.doReturn(StandardCharsets.UTF_8).when(response).getContentCharset();

        final WebResponse r = proc.processResponse(response);
        Assert.assertNotSame(response, r);
        Assert.assertEquals("This is my test text: bar bar barbar.", r.getContentAsString());
    }

    @Test
    public void testProcessResponse_Ctr2() throws Throwable
    {
        final ResponseContentProcessor proc = new ResponseContentProcessor(Pattern.compile("foo"), "bar", Pattern.compile("example"));
        final WebResponse response = Mockito.mock(WebResponse.class);

        Mockito.doReturn(new WebRequest(new URL("http://localhost/example.html"))).when(response).getWebRequest();
        Mockito.doReturn("This is my test text: foo bar foobar.").when(response).getContentAsString();
        Mockito.doReturn(StandardCharsets.UTF_8).when(response).getContentCharset();

        final WebResponse r = proc.processResponse(response);
        Assert.assertNotSame(response, r);
        Assert.assertEquals("This is my test text: bar bar barbar.", r.getContentAsString());
    }

    @Test
    public void testProcessResponse_Ctr3() throws Throwable
    {
        final ResponseContentProcessor proc = new ResponseContentProcessor(Pattern.compile("foo"), "bar");
        final WebResponse response = Mockito.mock(WebResponse.class);

        Mockito.doReturn(new WebRequest(new URL("http://localhost/example.html"))).when(response).getWebRequest();
        Mockito.doReturn("This is my test text: foo bar foobar.").when(response).getContentAsString();
        Mockito.doReturn(StandardCharsets.UTF_8).when(response).getContentCharset();

        final WebResponse r = proc.processResponse(response);
        Assert.assertNotSame(response, r);
        Assert.assertEquals("This is my test text: bar bar barbar.", r.getContentAsString());
    }

    @Test
    public void testProcessResponse_Regexp_Ctr3() throws Throwable
    {
        final ResponseContentProcessor proc = new ResponseContentProcessor(Pattern.compile("\\sis.*text"), " is water");
        final WebResponse response = Mockito.mock(WebResponse.class);

        Mockito.doReturn(new WebRequest(new URL("http://localhost/example.html"))).when(response).getWebRequest();
        Mockito.doReturn("This is my test text: foobar.").when(response).getContentAsString();
        Mockito.doReturn(StandardCharsets.UTF_8).when(response).getContentCharset();

        final WebResponse r = proc.processResponse(response);
        Assert.assertNotSame(response, r);
        Assert.assertEquals("This is water: foobar.", r.getContentAsString());
    }

    /**
     * Tests the implementation of {@link ResponseProcessor#processResponse(WebResponse)} by passing a response whose
     * content and request URL match the processor's content pattern and URL pattern respectively.
     * 
     * @throws Throwable
     */
    @Test
    public void testProcessResponse_NullResponse() throws Throwable
    {
        final ResponseContentProcessor proc = new ResponseContentProcessor("foo", "bar", "example");
        final WebResponse response = Mockito.mock(WebResponse.class);

        Mockito.doReturn(new WebRequest(new URL("http://localhost/example.html"))).when(response).getWebRequest();
        Mockito.doReturn(null).when(response).getContentAsString();
        Mockito.doReturn(StandardCharsets.UTF_8).when(response).getContentCharset();

        final WebResponse r = proc.processResponse(response);
        Assert.assertSame(response, r);
        Assert.assertNull(r.getContentAsString());
    }
}
