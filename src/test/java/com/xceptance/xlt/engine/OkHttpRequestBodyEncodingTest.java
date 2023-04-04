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
package com.xceptance.xlt.engine;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.htmlunit.HttpMethod;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.xceptance.xlt.api.engine.Session;
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
    private static Server localServer;

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
        // create the local test server at any free port
        localServer = new Server(0);

        // register a handler that extracts the received data
        localServer.setHandler(new AbstractHandler()
        {
            @Override
            public void handle(final String target, final Request baseRequest, final HttpServletRequest request,
                               final HttpServletResponse response)
                throws IOException, ServletException
            {
                final byte[] buffer = new byte[1024];
                final int bytesRead = request.getInputStream().read(buffer);

                receivedBytes = new byte[bytesRead];
                System.arraycopy(buffer, 0, receivedBytes, 0, bytesRead);

                baseRequest.setHandled(true);
            }
        });

        // now start the server and build its URL
        localServer.start();
        baseUrl = localServer.getURI().toString();

        // enable okhttp-based web connection
        XltPropertiesImpl.getInstance().setProperty("com.xceptance.xlt.http.client", "okhttp3");
    }

    @AfterClass
    public static final void tearDown() throws Exception
    {
        XltEngine.reset();
        SessionImpl.removeCurrent();

        localServer.stop();
        localServer.destroy();
    }

    @After
    public final void cleanUp() throws Exception
    {
        // clear the current session which in turn will close the default WebClient used by HttpRequest
        Session.getCurrent().clear();
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

        Assert.assertEquals(HttpStatus.OK_200, httpResponse.getStatusCode());
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

        Assert.assertEquals(HttpStatus.OK_200, httpResponse.getStatusCode());
        validate(TEXT_CONTENT.getBytes(StringUtils.defaultIfBlank(charsetName, "ISO-8859-1")), receivedBytes);
    }

    private void validate(final byte[] expectedBytes, final byte[] actualBytes) throws UnsupportedOperationException, IOException
    {
        Assert.assertArrayEquals(expectedBytes, actualBytes);
    }
}
