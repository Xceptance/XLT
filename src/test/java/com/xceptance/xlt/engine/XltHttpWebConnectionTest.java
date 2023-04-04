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

import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.htmlunit.HttpHeader;
import org.htmlunit.MockWebConnection;
import org.htmlunit.WebConnection;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.xceptance.xlt.api.util.XltProperties;

import util.lang.ClassFromByteArrayLoader;

/**
 * Tests the implementation of {@link XltHttpWebConnection}
 */
@RunWith(Parameterized.class)
public class XltHttpWebConnectionTest
{
    @AfterClass
    public static void afterClass()
    {
        // clean-up
        XltEngine.reset();
        SessionImpl.removeCurrent();
    }

    /**
     * Test setup. Primarily used for setting required properties and re-loading the web-connection class.
     *
     * @throws Throwable
     */
    @SuppressWarnings("unchecked")
    @Before
    public void setUp()
    {
        final XltProperties props = XltProperties.getInstance();
        final String propPrefix = "com.xceptance.xlt.http.requestId.";

        props.setProperty(propPrefix + "enabled", Boolean.toString(requestIdEnabled));

        props.setProperty(propPrefix + "appendToUserAgent", Boolean.toString(requestIdAtUA));
        props.setProperty(propPrefix + "headerName", requestIdHeaderName);

        props.setProperty(propPrefix + "length", Integer.toString(requestIdLength));

        webConnectionClazz = (Class<XltHttpWebConnection>) ClassFromByteArrayLoader.getFreshlyLoadedClass(XltHttpWebConnection.class);
    }

    private Class<XltHttpWebConnection> webConnectionClazz;

    @Parameter(0)
    public boolean requestIdEnabled;

    @Parameter(1)
    public boolean requestIdAtUA;

    @Parameter(2)
    public int requestIdLength;

    @Parameter(3)
    public String requestIdHeaderName;

    @Parameters
    public static Object[][] getData()
    {
        return new Object[][]
            {
                new Object[]
                {
                    true, true, 15, "Foo"
                }, new Object[]
                {
                    false, true, 12, "X-Rid"
                }, new Object[]
                {
                    true, false, 4, "X-Rid"
                }, new Object[]
                {
                    false, false, 0, "X-Rid"
                }
            };
    }

    /**
     * Tests request ID feature.
     *
     * @throws Throwable
     *             thrown on test failure
     */
    @Test
    public void testRequestId() throws Throwable
    {
        final XltWebClient wc = new XltWebClient();
        wc.setTimerName(getClass().getSimpleName());

        final MockWebConnection mockWebConn = new MockWebConnection();
        wc.setWebConnection(webConnectionClazz.getConstructor(XltWebClient.class, WebConnection.class).newInstance(wc, mockWebConn));

        // return this response whenever no explicitly mapped URL is requested
        mockWebConn.setDefaultResponse("<html><head><title>No Title</title></head><body>No Content</body></html>", 200, "OK", "text/html");

        final URL u = new URL("http://example.org");
        final WebRequest req = new WebRequest(u);
        final String userAgent = "My UserAgent";
        req.setAdditionalHeader(HttpHeader.USER_AGENT, userAgent);
        final WebResponse r = wc.loadWebResponse(req);
        Assert.assertEquals(200, r.getStatusCode());

        final String actualUA = r.getWebRequest().getAdditionalHeader(HttpHeader.USER_AGENT);
        final String requestId = r.getWebRequest().getAdditionalHeader(requestIdHeaderName);

        // this should always be true
        Assert.assertTrue("Where has my UA gone?", actualUA.startsWith(userAgent));

        if (Boolean.TRUE.equals(requestIdEnabled))
        {
            Assert.assertEquals("Unexpected length of request ID", requestIdLength, StringUtils.defaultString(requestId).length());
            if (Boolean.TRUE.equals(requestIdAtUA))
            {
                Assert.assertNotEquals("UA is still the same", userAgent, actualUA);

                Assert.assertEquals("Request IDs in User-Agent and appropriate request header do not match", requestId,
                                    StringUtils.substringAfter(actualUA, userAgent).trim());
            }
            else
            {
                Assert.assertEquals(userAgent, actualUA);
            }
        }
        else
        {
            Assert.assertNull("Request-Header '" + requestIdHeaderName + "' should not be set", requestId);
        }

    }
}
