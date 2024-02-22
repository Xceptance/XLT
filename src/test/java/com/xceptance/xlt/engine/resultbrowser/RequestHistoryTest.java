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
package com.xceptance.xlt.engine.resultbrowser;

import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.htmlunit.BrowserVersion;
import org.htmlunit.MockWebConnection;
import org.htmlunit.StringWebResponse;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.html.HtmlPage;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.metrics.Metrics;
import com.xceptance.xlt.engine.resultbrowser.RequestHistory.DumpMode;
import com.xceptance.xlt.util.XltPropertiesImpl;

/**
 * Tests the implementation of {@link RequestHistory}.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class RequestHistoryTest
{
    /**
     * Mock for the metrics
     * @author rschwietzke
     *
     */
    class TestMetrics extends Metrics
    {
        public int callCount = 0;

        public void updateMetrics(final Data data)
        {
            callCount++;
        }
    }

    /**
     * Mock for the session and our verifier
     * @author rschwietzke
     *
     */
    class TestSession extends SessionImpl
    {
        private final String userName;

        public TestSession(final String userName)
        {
            super();
            this.userName = userName;
        }

        public String getUserName()
        {
            return userName;
        }
    }

    private RequestHistory getRequestHistory()
    {
        var props = new XltPropertiesImpl();
        var session = new TestSession("TUser");

        final RequestHistory requestHistory = new RequestHistory(session, props);
        requestHistory.setDumpManager(Mockito.mock(DumpMgr.class));
        return requestHistory;
    }

    private RequestHistory getRequestHistory(Properties p)
    {
        var props = new XltPropertiesImpl(p);
        var session = new TestSession("TUser");

        final RequestHistory requestHistory = new RequestHistory(session, props);
        requestHistory.setDumpManager(Mockito.mock(DumpMgr.class));
        return requestHistory;
    }

    /**
     * Tests the implementation of {@link RequestHistory#RequestHistory()} by undefining the result directory property.
     */
    @Test
    public void testInit_DumpModeReset()
    {
        final Properties props = new Properties();
        props.setProperty(XltConstants.XLT_PACKAGE_PATH + ".output2disk", "always");
        props.setProperty(XltConstants.XLT_PACKAGE_PATH + ".result-dir", XltConstants.EMPTYSTRING);

        final RequestHistory history = getRequestHistory(props);
        Assert.assertEquals(DumpMode.ALWAYS, history.getDumpMode());
    }

    /**
     * Tests the implementation of
     * {@link RequestHistory#add(String, WebRequest, WebResponse)} by passing an invalid
     * name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddResponse_NameNull() throws Throwable
    {
        getRequestHistory().add(null, new WebRequest(new URL("http://localhost")),
                                 new StringWebResponse(XltConstants.EMPTYSTRING, new URL("http://localhost")), null);
    }

    /**
     * Tests the implementation of
     * {@link RequestHistory#add(String, WebRequest, WebResponse)} by passing invalid
     * request settings.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddResponse_WebRequestSettingsNull() throws Throwable
    {
        getRequestHistory().add("AnyName", null, new StringWebResponse(XltConstants.EMPTYSTRING, new URL("http://localhost")), null);
    }

    /**
     * Tests the implementation of
     * {@link RequestHistory#add(String, WebRequest, WebResponse)} by setting the dump
     * mode to {@link DumpMode#ALWAYS}.
     */
    @Test
    public void testAddResponse_DumpModeAlways() throws Throwable
    {
        var history = getRequestHistory();

        history.setDumpMode(RequestHistory.DumpMode.ALWAYS);
        Mockito.doAnswer(new Answer<Void>()
        {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable
            {
                final Object[] args = invocation.getArguments();
                Assert.assertNotNull(args);
                Assert.assertEquals(2, args.length);

                final List<?> requests = (List<?>) args[1];
                final List<?> pages = (List<?>) args[0];

                Assert.assertNotNull(requests);
                Assert.assertNotNull(pages);

                Assert.assertTrue(requests.isEmpty());
                Assert.assertTrue(pages.isEmpty());

                return null;
            }
        }).when(history.getDumpManager()).dumpToDisk(ArgumentMatchers.anyList(), ArgumentMatchers.anyList());

        history.add("AnyName", new WebRequest(new URL("http://localhost")),
                    new StringWebResponse(XltConstants.EMPTYSTRING, new URL("http://localhost")), null);
        history.dumpToDisk();

        Mockito.verify(history.getDumpManager(), Mockito.times(1)).dump((Request) ArgumentMatchers.any());
    }

    /**
     * Tests the implementation of
     * {@link RequestHistory#add(String, WebRequest, WebResponse)} by setting the dump
     * mode to {@link DumpMode#NEVER}.
     */
    @Test
    public void testAddResponse_DumpModeNever() throws Throwable
    {
        var history = getRequestHistory();

        history.setDumpMode(RequestHistory.DumpMode.NEVER);
        Mockito.doAnswer(new Answer<Void>()
        {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable
            {
                final Object[] args = invocation.getArguments();
                Assert.assertNotNull(args);
                Assert.assertEquals(2, args.length);

                final List<?> requests = (List<?>) args[1];
                final List<?> pages = (List<?>) args[0];

                Assert.assertNotNull(requests);
                Assert.assertNotNull(pages);

                Assert.assertTrue(requests.isEmpty());
                Assert.assertTrue(pages.isEmpty());

                return null;
            }
        }).when(history.getDumpManager()).dumpToDisk(ArgumentMatchers.anyList(), ArgumentMatchers.anyList());

        history.add("AnyName", new WebRequest(new URL("http://localhost")),
                    new StringWebResponse(XltConstants.EMPTYSTRING, new URL("http://localhost")), null);
        history.dumpToDisk();

        Mockito.verify(history.getDumpManager(), Mockito.never()).dump((Request) ArgumentMatchers.any());
    }

    /**
     * Tests the implementation of
     * {@link RequestHistory#add(String, WebRequest, WebResponse)} by setting the dump
     * mode to {@link DumpMode#ON_ERROR}.
     */
    @Test
    public void testAddResponse_DumpModeOnError() throws Throwable
    {
        var history = getRequestHistory();

        history.setDumpMode(RequestHistory.DumpMode.ON_ERROR);
        Mockito.doAnswer(new Answer<Void>()
        {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable
            {
                final Object[] args = invocation.getArguments();
                Assert.assertNotNull(args);
                Assert.assertEquals(2, args.length);

                final List<?> requests = (List<?>) args[1];
                final List<?> pages = (List<?>) args[0];

                Assert.assertNotNull(requests);
                Assert.assertNotNull(pages);

                Assert.assertEquals(1, requests.size());
                Assert.assertTrue(pages.isEmpty());

                return null;
            }
        }).when(history.getDumpManager()).dumpToDisk(ArgumentMatchers.anyList(), ArgumentMatchers.anyList());

        history.add("AnyName", new WebRequest(new URL("http://localhost")),
                    new StringWebResponse(XltConstants.EMPTYSTRING, new URL("http://localhost")), null);
        history.dumpToDisk();

        Mockito.verify(history.getDumpManager(), Mockito.never()).dump((Request) ArgumentMatchers.any());
    }

    /**
     * Tests the implementation of {@link RequestHistory#add(String, HtmlPage)} by passing an invalid name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddHtmlPage_NameNull() throws Throwable
    {
        var history = getRequestHistory();

        final URL url = new URL("http://localhost/");

        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(url, XltConstants.EMPTYSTRING);

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.setWebConnection(conn);

            final HtmlPage page = wc.getPage(url);
            history.add(null, page);
        }
    }

    /**
     * Tests the implementation of {@link RequestHistory#add(String, HtmlPage)} by setting the dump mode to
     * {@link DumpMode#NEVER}.
     */
    @Test
    public void testAddHtmlPage_DumpModeNever() throws Throwable
    {
        var history = getRequestHistory();

        history.setDumpMode(RequestHistory.DumpMode.NEVER);
        Mockito.doAnswer(new Answer<Void>()
        {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable
            {
                final Object[] args = invocation.getArguments();
                Assert.assertNotNull(args);
                Assert.assertEquals(2, args.length);

                final List<?> requests = (List<?>) args[1];
                final List<?> pages = (List<?>) args[0];

                Assert.assertNotNull(requests);
                Assert.assertNotNull(pages);

                Assert.assertTrue(requests.isEmpty());
                Assert.assertTrue(pages.isEmpty());

                return null;
            }
        }).when(history.getDumpManager()).dumpToDisk(ArgumentMatchers.anyList(), ArgumentMatchers.anyList());

        final URL url = new URL("http://localhost/");

        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(url, XltConstants.EMPTYSTRING);

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.setWebConnection(conn);

            final HtmlPage page = wc.getPage(url);

            history.add("AnyName", page);
            history.dumpToDisk();

            Mockito.verify(history.getDumpManager(), Mockito.never()).dump((Page) ArgumentMatchers.any());
        }
    }

    /**
     * Tests the implementation of {@link RequestHistory#add(String, HtmlPage)} by setting the dump mode to
     * {@link DumpMode#ALWAYS}.
     */
    @Test
    public void testAddHtmlPage_DumpModeAlways() throws Throwable
    {
        var history = getRequestHistory();

        history.setDumpMode(RequestHistory.DumpMode.ALWAYS);
        Mockito.doAnswer(new Answer<Void>()
        {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable
            {
                final Object[] args = invocation.getArguments();
                Assert.assertNotNull(args);
                Assert.assertEquals(2, args.length);

                final List<?> requests = (List<?>) args[1];
                final List<?> pages = (List<?>) args[0];

                Assert.assertNotNull(requests);
                Assert.assertNotNull(pages);

                Assert.assertTrue(requests.isEmpty());
                Assert.assertTrue(pages.isEmpty());

                return null;
            }
        }).when(history.getDumpManager()).dumpToDisk(ArgumentMatchers.anyList(), ArgumentMatchers.anyList());

        final URL url = new URL("http://localhost/");

        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(url, XltConstants.EMPTYSTRING);

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.setWebConnection(conn);

            final HtmlPage page = wc.getPage(url);

            history.add("AnyName", page);
            history.dumpToDisk();

            Mockito.verify(history.getDumpManager(), Mockito.times(1)).dump((Page) ArgumentMatchers.any());
        }
    }

    /**
     * Tests the implementation of {@link RequestHistory#add(String, HtmlPage)} by setting the dump mode to
     * {@link DumpMode#ON_ERROR}.
     */
    @Test
    public void testAddHtmlPage_DumpModeOnError() throws Throwable
    {
        var history = getRequestHistory();

        history.setDumpMode(RequestHistory.DumpMode.ON_ERROR);
        Mockito.doAnswer(new Answer<Void>()
        {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable
            {
                final Object[] args = invocation.getArguments();
                Assert.assertNotNull(args);
                Assert.assertEquals(2, args.length);

                final List<?> requests = (List<?>) args[1];
                final List<?> pages = (List<?>) args[0];

                Assert.assertNotNull(requests);
                Assert.assertNotNull(pages);

                Assert.assertTrue(requests.isEmpty());
                Assert.assertEquals(1, pages.size());

                return null;
            }
        }).when(history.getDumpManager()).dumpToDisk(ArgumentMatchers.anyList(), ArgumentMatchers.anyList());

        final URL url = new URL("http://localhost/");

        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(url, XltConstants.EMPTYSTRING);

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.setWebConnection(conn);

            final HtmlPage page = wc.getPage(url);

            history.add("AnyName", page);
            history.dumpToDisk();

            Mockito.verify(history.getDumpManager(), Mockito.never()).dump((Page) ArgumentMatchers.any());
        }
    }

    /**
     * Tests the implementation of {@link RequestHistory#add(LightWeightPage)} by setting the dump mode to
     * {@link DumpMode#NEVER}.
     */
    @Test
    public void testAddLightweightPage_DumpModeNever() throws Throwable
    {
        var history = getRequestHistory();

        history.setDumpMode(RequestHistory.DumpMode.NEVER);
        Mockito.doAnswer(new Answer<Void>()
        {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable
            {
                final Object[] args = invocation.getArguments();
                Assert.assertNotNull(args);
                Assert.assertEquals(2, args.length);

                final List<?> requests = (List<?>) args[1];
                final List<?> pages = (List<?>) args[0];

                Assert.assertNotNull(requests);
                Assert.assertNotNull(pages);

                Assert.assertTrue(requests.isEmpty());
                Assert.assertTrue(pages.isEmpty());

                return null;
            }
        }).when(history.getDumpManager()).dumpToDisk(ArgumentMatchers.anyList(), ArgumentMatchers.anyList());

        history.add(new LightWeightPage(new StringWebResponse(XltConstants.EMPTYSTRING, new URL("http://localhost")), "LightweightPage"));
        history.dumpToDisk();

        Mockito.verify(history.getDumpManager(), Mockito.never()).dump((Page) ArgumentMatchers.any());
    }

    /**
     * Tests the implementation of {@link RequestHistory#add(LightWeightPage)} by setting the dump mode to
     * {@link DumpMode#ALWAYS}.
     */
    @Test
    public void testAddLightweightPage_DumpModeAlways() throws Throwable
    {
        var history = getRequestHistory();

        history.setDumpMode(RequestHistory.DumpMode.ALWAYS);
        Mockito.doAnswer(new Answer<Void>()
        {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable
            {
                final Object[] args = invocation.getArguments();
                Assert.assertNotNull(args);
                Assert.assertEquals(2, args.length);

                final List<?> requests = (List<?>) args[1];
                final List<?> pages = (List<?>) args[0];

                Assert.assertNotNull(requests);
                Assert.assertNotNull(pages);

                Assert.assertTrue(requests.isEmpty());
                Assert.assertTrue(pages.isEmpty());

                return null;
            }
        }).when(history.getDumpManager()).dumpToDisk(ArgumentMatchers.anyList(), ArgumentMatchers.anyList());

        history.add(new LightWeightPage(new StringWebResponse(XltConstants.EMPTYSTRING, new URL("http://localhost")), "LightweightPage"));
        history.dumpToDisk();

        Mockito.verify(history.getDumpManager(), Mockito.times(1)).dump((Page) ArgumentMatchers.any());
    }

    /**
     * Tests the implementation of {@link RequestHistory#add(LightWeightPage)} by setting the dump mode to
     * {@link DumpMode#ON_ERROR}.
     */
    @Test
    public void testAddLightweightPage_DumpModeOnError() throws Throwable
    {
        var history = getRequestHistory();

        history.setDumpMode(RequestHistory.DumpMode.ON_ERROR);
        Mockito.doAnswer(new Answer<Void>()
        {

            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable
            {
                final Object[] args = invocation.getArguments();
                Assert.assertNotNull(args);
                Assert.assertEquals(2, args.length);

                final List<?> requests = (List<?>) args[1];
                final List<?> pages = (List<?>) args[0];

                Assert.assertNotNull(requests);
                Assert.assertNotNull(pages);

                Assert.assertTrue(requests.isEmpty());
                Assert.assertEquals(1, pages.size());

                return null;
            }
        }).when(history.getDumpManager()).dumpToDisk(ArgumentMatchers.anyList(), ArgumentMatchers.anyList());

        history.add(new LightWeightPage(new StringWebResponse(XltConstants.EMPTYSTRING, new URL("http://localhost")), "LightweightPage"));
        history.dumpToDisk();

        Mockito.verify(history.getDumpManager(), Mockito.never()).dump((Page) ArgumentMatchers.any());
    }
}
