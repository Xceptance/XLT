/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.localserver.LocalServerTestBase.ProtocolScheme;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.LocalTestServer;
import com.xceptance.common.lang.ReflectionUtils;
import com.xceptance.xlt.engine.httprequest.HttpRequest;
import com.xceptance.xlt.engine.httprequest.HttpRequestHeaders;
import com.xceptance.xlt.engine.httprequest.HttpResponse;
import com.xceptance.xlt.util.XltPropertiesImpl;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * Checks for our OkHttp-based web connection that request bodies, both binary data as well as text data with different
 * character encodings, are correctly transmitted over the wire. To this end, a test server is set up, which provides
 * the data received to let us verify the expectations.
 */
@Ignore("Can be run manually only for now")
@RunWith(JUnitParamsRunner.class)
public class OkHttpRequestBodyEncodingTest
{
    private static final byte[] BINARY_CONTENT;

    private static final String TEXT_CONTENT;

    static
    {
        // binary test data
        BINARY_CONTENT = new byte[256];
        for (int i = 0; i < BINARY_CONTENT.length; i++)
        {
            BINARY_CONTENT[i] = (byte) i;
        }

        // text test data
        final char[] chars = new char[256];
        for (int i = 0; i < chars.length; i++)
        {
            chars[i] = (char) i;
        }
        TEXT_CONTENT = new String(chars);
    }

    /**
     * The test server.
     */
    private static LocalTestServer localServer;

    /**
     * The base URL of the test server.
     */
    private static String baseUrl;

    /**
     * The bytes received at the test server.
     */
    private static byte[] receivedBytes;

    @BeforeClass
    public static final void setUp() throws Exception
    {
        // create the local test server
        localServer = new LocalTestServer(null);
        ReflectionUtils.writeInstanceField(localServer, "scheme", ProtocolScheme.http); // force server to use HTTP mode

        // register a handler that extracts the received data
        localServer.register("/test", new HttpRequestHandler()
        {
            @Override
            public void handle(final org.apache.http.HttpRequest request, final org.apache.http.HttpResponse response,
                               final HttpContext context)
                throws HttpException, IOException
            {
                final BasicHttpEntityEnclosingRequest putRequest = (BasicHttpEntityEnclosingRequest) request;

                final byte[] buffer = new byte[1024];
                final int bytesRead = putRequest.getEntity().getContent().read(buffer);

                receivedBytes = new byte[bytesRead];
                System.arraycopy(buffer, 0, receivedBytes, 0, bytesRead);
            }
        });

        // now start the server and build its URL
        localServer.start();

        baseUrl = localServer.getSchemeName() + "://" + localServer.getServer().getInetAddress().getHostName() + ":" +
                  localServer.getServer().getLocalPort();

        // enable okhttp-based web connection
        XltPropertiesImpl.getInstance().setProperty("com.xceptance.xlt.http.client", "okhttp3");
    }

    @AfterClass
    public static final void tearDown() throws Exception
    {
        XltPropertiesImpl.reset();
        SessionImpl.removeCurrent();
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Test
    @Parameters(value =
        {
            "application/octet-stream", "image/png"
    })
    public void binaryContent(final String contentType) throws IOException, URISyntaxException
    {
        final HttpRequest httpRequest = new HttpRequest().timerName("foo").method(HttpMethod.PUT).baseUrl(baseUrl).relativeUrl("/test")
                                                         .header(HttpRequestHeaders.CONTENT_TYPE, contentType).body(BINARY_CONTENT);
        final HttpResponse httpResponse = httpRequest.fire();

        Assert.assertEquals(HttpStatus.SC_OK, httpResponse.getStatusCode());
        validate(BINARY_CONTENT, receivedBytes);
    }

    @Test
    @Parameters(value =
        {
            "", "ISO-8859-1", "UTF-8"
    })
    public void textContent(final String charsetName) throws IOException, URISyntaxException
    {
        String contentType = "text/plain";
        if (!charsetName.isEmpty())
        {
            contentType = contentType + ";charset=" + charsetName;
        }

        final HttpRequest httpRequest = new HttpRequest().timerName("foo").method(HttpMethod.PUT).baseUrl(baseUrl).relativeUrl("/test")
                                                         .header(HttpRequestHeaders.CONTENT_TYPE, contentType).body(TEXT_CONTENT);
        final HttpResponse httpResponse = httpRequest.fire();

        Assert.assertEquals(HttpStatus.SC_OK, httpResponse.getStatusCode());
        validate(TEXT_CONTENT.getBytes(StringUtils.defaultIfBlank(charsetName, "ISO-8859-1")), receivedBytes);
    }

    private void validate(final byte[] expectedBytes, final byte[] actualBytes) throws UnsupportedOperationException, IOException
    {
        Assert.assertArrayEquals(expectedBytes, actualBytes);
    }
}
