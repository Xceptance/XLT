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
package com.xceptance.xlt.engine.htmlunit.apache;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLProtocolException;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpCoreContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Currently non-retriable I/O exceptions: {@link InterruptedIOException}, {@link UnknownHostException},
 * {@link ConnectException}, {@link SSLException}
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class XltHttpRequestRetryHandlerTest
{
    private static class SubClassOfNonRetriableException extends UnknownHostException
    {
    }

    private static class SubClassOfRetriableException extends SocketException
    {
    }

    private HttpRequestRetryHandler retryHandler;

    private HttpClientContext httpClientContext;

    @Before
    public void before()
    {
        retryHandler = new XltHttpRequestRetryHandler(1, true);

        // prepare a dummy HttpClientContext instance
        httpClientContext = new HttpClientContext();
        httpClientContext.setAttribute(HttpCoreContext.HTTP_REQUEST, new HttpGet("http://www.foobar.com/posters"));
    }

    @Test
    public void testRetry_retriableEx()
    {
        final IOException ioex = new SocketException("Connection reset");

        Assert.assertTrue(retryHandler.retryRequest(ioex, 1, httpClientContext));
    }

    @Test
    public void testRetry_nonRetriableExWithRetriableRootCause()
    {
        final IOException ioex = new SSLProtocolException("Whatever");
        ioex.initCause(new SocketException("Connection reset"));

        Assert.assertTrue(retryHandler.retryRequest(ioex, 1, httpClientContext));
    }

    @Test
    public void testRetry_nonRetriableExWithRetriableRootCauseDeep()
    {
        final IOException ioex = new SSLProtocolException("Whatever");
        ioex.initCause(new UnknownHostException().initCause(new SocketException("Connection reset")));

        Assert.assertTrue(retryHandler.retryRequest(ioex, 1, httpClientContext));
    }

    @Test
    public void testRetry_nonRetriableExWithRetriableRootCauseSubClass()
    {
        final IOException ioex = new SSLProtocolException("Whatever");
        ioex.initCause(new SubClassOfRetriableException());

        Assert.assertTrue(retryHandler.retryRequest(ioex, 1, httpClientContext));
    }

    @Test
    public void testRetry_nonRetriableEx()
    {
        final IOException ioex = new SSLProtocolException("Whatever");

        Assert.assertFalse(retryHandler.retryRequest(ioex, 1, httpClientContext));
    }

    @Test
    public void testRetry_nonRetriableExWithNonRetriableRootCause()
    {
        final IOException ioex = new SSLProtocolException("Whatever");
        ioex.initCause(new UnknownHostException());

        Assert.assertFalse(retryHandler.retryRequest(ioex, 1, httpClientContext));
    }

    @Test
    public void testRetry_nonRetriableExWithNonRetriableRootCauseDeep()
    {
        final IOException ioex = new SSLProtocolException("Whatever");
        ioex.initCause(new SocketException("Connection reset").initCause(new UnknownHostException()));

        Assert.assertFalse(retryHandler.retryRequest(ioex, 1, httpClientContext));
    }

    @Test
    public void testRetry_nonRetriableExWithNonRetriableRootCauseSubClass()
    {
        final IOException ioex = new SSLProtocolException("Whatever");
        ioex.initCause(new SubClassOfNonRetriableException());

        Assert.assertFalse(retryHandler.retryRequest(ioex, 1, httpClientContext));
    }
}
