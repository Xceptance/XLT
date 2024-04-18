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
package com.xceptance.xlt.engine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.engine.httprequest.HttpRequest;
import com.xceptance.xlt.engine.httprequest.HttpRequestHeaders;
import com.xceptance.xlt.util.XltPropertiesImpl;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * Checks that DELETE requests may or may not have a body. To this end, a test server is set up, which provides the data
 * received to let us verify the expectations.
 */
@RunWith(JUnitParamsRunner.class)
public class DeleteRequestWithBodyTest
{
    private static final Charset CONTENT_CHARSET = StandardCharsets.UTF_8;

    private static final String CONTENT_TYPE = "application/json;charset=" + CONTENT_CHARSET;

    private static final String CONTENT = "{ \"dummy\": \"äüö\" }";

    private static final byte[] CONTENT_BYTES = CONTENT.getBytes(CONTENT_CHARSET);

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

                if (bytesRead == -1)
                {
                    receivedBytes = null;
                }
                else
                {
                    receivedBytes = new byte[bytesRead];
                    System.arraycopy(buffer, 0, receivedBytes, 0, bytesRead);
                }

                baseRequest.setHandled(true);
            }
        });

        // now start the server and build its URL
        localServer.start();
        baseUrl = localServer.getURI().toString();
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
            "apache4|false|false", //
            "apache4|false|true",  //
            "apache4|true|false",  //
            "apache4|true|true",   //
            "okhttp3|false|false", //
            "okhttp3|false|true",  //
            "okhttp3|true|false",  //
            "okhttp3|true|true",   //
    })
    public void delete(final String httpClientName, final boolean shouldHaveBody, final boolean useHttpRequest)
        throws IOException, URISyntaxException
    {
        // choose the underlying HTTP client
        XltPropertiesImpl.getInstance().setProperty("com.xceptance.xlt.http.client", httpClientName);

        final HttpMethod method = HttpMethod.DELETE;

        final WebResponse webResponse;
        if (useHttpRequest)
        {
            // set up and execute request via HttpRequest
            final HttpRequest httpRequest = new HttpRequest().timerName("foo").method(method).baseUrl(baseUrl).relativeUrl("/test");

            if (shouldHaveBody)
            {
                httpRequest.header(HttpRequestHeaders.CONTENT_TYPE, CONTENT_TYPE).charset(StandardCharsets.UTF_8).body(CONTENT);
            }

            webResponse = httpRequest.fire().getWebResponse();
        }
        else
        {
            // set up and execute request via WebRequest/XltWebClient
            final WebRequest webRequest = new WebRequest(new URL(baseUrl + "/test"), method);

            if (shouldHaveBody)
            {
                webRequest.setAdditionalHeader(HttpRequestHeaders.CONTENT_TYPE, CONTENT_TYPE);
                webRequest.setCharset(StandardCharsets.UTF_8);
                webRequest.setRequestBody(CONTENT);
            }

            try (XltWebClient webClient = new XltWebClient())
            {
                webClient.setTimerName("foo");

                webResponse = webClient.loadWebResponse(webRequest);
            }
        }

        // validate response / request body content as received on the server
        Assert.assertEquals(HttpStatus.OK_200, webResponse.getStatusCode());
        Assert.assertArrayEquals(shouldHaveBody ? CONTENT_BYTES : null, receivedBytes);
    }
}
