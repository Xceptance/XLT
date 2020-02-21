package com.xceptance.xlt.engine.resultbrowser;

import java.net.URL;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.common.collection.ConcurrentLRUCache;
import com.xceptance.common.lang.ReflectionUtils;
import com.xceptance.common.lang.ThrowableUtils;
import com.xceptance.xlt.AbstractXLTTestCase;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.htmlunit.LightWeightPage;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.resultbrowser.RequestHistory.DumpMode;
import com.xceptance.xlt.engine.util.TimedCounter;

/**
 * Tests the implementation of {@link RequestHistory}.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
@SuppressWarnings("unchecked")
public class RequestHistoryTest extends AbstractXLTTestCase
{
    /**
     * Test instance.
     */
    private RequestHistory history;

    /**
     * Test fixture setup.
     */
    @Before
    public void intro()
    {
        history = getNewRequestHistory();
    }

    private RequestHistory getNewRequestHistory()
    {
        final RequestHistory requestHistory = new RequestHistory();
        requestHistory.setDumpManager(Mockito.mock(DumpMgr.class));
        return requestHistory;
    }

    /**
     * Tests the implementation of {@link RequestHistory#RequestHistory()} by undefining the result directory property.
     */
    @Test
    public void testInit_DumpModeReset()
    {
        final XltProperties props = XltProperties.getInstance();
        final String outputOrig = props.getProperty(XltConstants.XLT_PACKAGE_PATH + ".output2disk", XltConstants.EMPTYSTRING);
        final String resultOrig = props.getProperty(XltConstants.XLT_PACKAGE_PATH + ".result-dir", XltConstants.EMPTYSTRING);

        try
        {
            props.setProperty(XltConstants.XLT_PACKAGE_PATH + ".output2disk", "always");
            props.setProperty(XltConstants.XLT_PACKAGE_PATH + ".result-dir", XltConstants.EMPTYSTRING);

            final RequestHistory history = new RequestHistory();
            Assert.assertEquals(DumpMode.ALWAYS, history.getDumpMode());

        }
        finally
        {
            props.setProperty(XltConstants.XLT_PACKAGE_PATH + ".output2disk", outputOrig);
            props.setProperty(XltConstants.XLT_PACKAGE_PATH + ".result-dir", resultOrig);
        }
    }

    /**
     * Tests the implementation of
     * {@link RequestHistory#add(String, WebRequest, com.gargoylesoftware.htmlunit.WebResponse)} by passing an invalid
     * name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddResponse_NameNull() throws Throwable
    {
        new RequestHistory().add(null, new WebRequest(new URL("http://localhost")),
                                 new StringWebResponse(XltConstants.EMPTYSTRING, new URL("http://localhost")), null);
    }

    /**
     * Tests the implementation of
     * {@link RequestHistory#add(String, WebRequest, com.gargoylesoftware.htmlunit.WebResponse)} by passing invalid
     * request settings.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddResponse_WebRequestSettingsNull() throws Throwable
    {
        new RequestHistory().add("AnyName", null, new StringWebResponse(XltConstants.EMPTYSTRING, new URL("http://localhost")), null);
    }

    /**
     * Tests the implementation of
     * {@link RequestHistory#add(String, WebRequest, com.gargoylesoftware.htmlunit.WebResponse)} by setting the dump
     * mode to {@link DumpMode#ALWAYS}.
     */
    @Test
    public void testAddResponse_DumpModeAlways() throws Throwable
    {
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
        }).when(history.getDumpManager()).dumpToDisk(Matchers.anyList(), Matchers.anyList());

        history.add("AnyName", new WebRequest(new URL("http://localhost")),
                    new StringWebResponse(XltConstants.EMPTYSTRING, new URL("http://localhost")), null);
        history.dumpToDisk();

        Mockito.verify(history.getDumpManager(), Mockito.times(1)).dump((Request) Matchers.anyObject());
    }

    /**
     * Tests the implementation of
     * {@link RequestHistory#add(String, WebRequest, com.gargoylesoftware.htmlunit.WebResponse)} by setting the dump
     * mode to {@link DumpMode#NEVER}.
     */
    @Test
    public void testAddResponse_DumpModeNever() throws Throwable
    {
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
        }).when(history.getDumpManager()).dumpToDisk(Matchers.anyList(), Matchers.anyList());

        history.add("AnyName", new WebRequest(new URL("http://localhost")),
                    new StringWebResponse(XltConstants.EMPTYSTRING, new URL("http://localhost")), null);
        history.dumpToDisk();

        Mockito.verify(history.getDumpManager(), Mockito.never()).dump((Request) Matchers.anyObject());
    }

    /**
     * Tests the implementation of
     * {@link RequestHistory#add(String, WebRequest, com.gargoylesoftware.htmlunit.WebResponse)} by setting the dump
     * mode to {@link DumpMode#ON_ERROR}.
     */
    @Test
    public void testAddResponse_DumpModeOnError() throws Throwable
    {
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
        }).when(history.getDumpManager()).dumpToDisk(Matchers.anyList(), Matchers.anyList());

        history.add("AnyName", new WebRequest(new URL("http://localhost")),
                    new StringWebResponse(XltConstants.EMPTYSTRING, new URL("http://localhost")), null);
        history.dumpToDisk();

        Mockito.verify(history.getDumpManager(), Mockito.never()).dump((Request) Matchers.anyObject());
    }

    /**
     * Tests the implementation of {@link RequestHistory#add(String, HtmlPage)} by passing an invalid name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAddHtmlPage_NameNull() throws Throwable
    {
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
        }).when(history.getDumpManager()).dumpToDisk(Matchers.anyList(), Matchers.anyList());

        final URL url = new URL("http://localhost/");

        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(url, XltConstants.EMPTYSTRING);

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.setWebConnection(conn);

            final HtmlPage page = wc.getPage(url);

            history.add("AnyName", page);
            history.dumpToDisk();

            Mockito.verify(history.getDumpManager(), Mockito.never()).dump((Page) Matchers.anyObject());
        }
    }

    /**
     * Tests the implementation of {@link RequestHistory#add(String, HtmlPage)} by setting the dump mode to
     * {@link DumpMode#ALWAYS}.
     */
    @Test
    public void testAddHtmlPage_DumpModeAlways() throws Throwable
    {
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
        }).when(history.getDumpManager()).dumpToDisk(Matchers.anyList(), Matchers.anyList());

        final URL url = new URL("http://localhost/");

        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(url, XltConstants.EMPTYSTRING);

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.setWebConnection(conn);

            final HtmlPage page = wc.getPage(url);

            history.add("AnyName", page);
            history.dumpToDisk();

            Mockito.verify(history.getDumpManager(), Mockito.times(1)).dump((Page) Matchers.anyObject());
        }
    }

    /**
     * Tests the implementation of {@link RequestHistory#add(String, HtmlPage)} by setting the dump mode to
     * {@link DumpMode#ON_ERROR}.
     */
    @Test
    public void testAddHtmlPage_DumpModeOnError() throws Throwable
    {
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
        }).when(history.getDumpManager()).dumpToDisk(Matchers.anyList(), Matchers.anyList());

        final URL url = new URL("http://localhost/");

        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(url, XltConstants.EMPTYSTRING);

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.setWebConnection(conn);

            final HtmlPage page = wc.getPage(url);

            history.add("AnyName", page);
            history.dumpToDisk();

            Mockito.verify(history.getDumpManager(), Mockito.never()).dump((Page) Matchers.anyObject());
        }
    }

    /**
     * Tests the implementation of {@link RequestHistory#add(LightWeightPage)} by setting the dump mode to
     * {@link DumpMode#NEVER}.
     */
    @Test
    public void testAddLightweightPage_DumpModeNever() throws Throwable
    {
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
        }).when(history.getDumpManager()).dumpToDisk(Matchers.anyList(), Matchers.anyList());

        history.add(new LightWeightPage(new StringWebResponse(XltConstants.EMPTYSTRING, new URL("http://localhost")), "LightweightPage"));
        history.dumpToDisk();

        Mockito.verify(history.getDumpManager(), Mockito.never()).dump((Page) Matchers.anyObject());
    }

    /**
     * Tests the implementation of {@link RequestHistory#add(LightWeightPage)} by setting the dump mode to
     * {@link DumpMode#ALWAYS}.
     */
    @Test
    public void testAddLightweightPage_DumpModeAlways() throws Throwable
    {
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
        }).when(history.getDumpManager()).dumpToDisk(Matchers.anyList(), Matchers.anyList());

        history.add(new LightWeightPage(new StringWebResponse(XltConstants.EMPTYSTRING, new URL("http://localhost")), "LightweightPage"));
        history.dumpToDisk();

        Mockito.verify(history.getDumpManager(), Mockito.times(1)).dump((Page) Matchers.anyObject());
    }

    /**
     * Tests the implementation of {@link RequestHistory#add(LightWeightPage)} by setting the dump mode to
     * {@link DumpMode#ON_ERROR}.
     */
    @Test
    public void testAddLightweightPage_DumpModeOnError() throws Throwable
    {
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
        }).when(history.getDumpManager()).dumpToDisk(Matchers.anyList(), Matchers.anyList());

        history.add(new LightWeightPage(new StringWebResponse(XltConstants.EMPTYSTRING, new URL("http://localhost")), "LightweightPage"));
        history.dumpToDisk();

        Mockito.verify(history.getDumpManager(), Mockito.never()).dump((Page) Matchers.anyObject());
    }

    private String getTypicalErrorMessage()
    {
        return "My Error Message (user: 'TMyTestcase', output: '" + System.nanoTime() + "')";
    }

    @Test
    public void testGetErrorKey() throws Throwable
    {
        final SessionImpl session = (SessionImpl) Session.getCurrent();
        session.setUserName("foo");
        session.setFailed(true);
        session.setFailReason(new Throwable(getTypicalErrorMessage()));

        final Object o = ReflectionUtils.callStaticMethod(history.getClass(), "getErrorKey");
        final String key = (String) o;

        Assert.assertTrue(StringUtils.isNotBlank(key));
        Assert.assertEquals(32, key.length());
    }

    @Test
    public void testGetErrorKey_repeatable() throws Throwable
    {
        final SessionImpl session = (SessionImpl) Session.getCurrent();
        session.setUserName("foo");
        session.setFailed(true);
        session.setFailReason(new Throwable(getTypicalErrorMessage()));

        final String key1 = (String) ReflectionUtils.callStaticMethod(history.getClass(), "getErrorKey");
        final String key2 = (String) ReflectionUtils.callStaticMethod(history.getClass(), "getErrorKey");

        Assert.assertEquals(key1, key2);
    }

    @Test
    public void testGetErrorKey_differentUserNames() throws Throwable
    {
        final SessionImpl session = (SessionImpl) Session.getCurrent();
        session.setFailed(true);
        session.setFailReason(new Throwable(getTypicalErrorMessage()));

        session.setUserName("foo");
        final String key1 = (String) ReflectionUtils.callStaticMethod(history.getClass(), "getErrorKey");

        session.setUserName("bar");
        final String key2 = (String) ReflectionUtils.callStaticMethod(history.getClass(), "getErrorKey");

        Assert.assertNotEquals(key1, key2);
    }

    @Test
    public void testGetErrorKey_handleHints() throws Throwable
    {
        final SessionImpl session = (SessionImpl) Session.getCurrent();
        session.setUserName("foo");
        final Throwable t = new Throwable();
        session.setFailed(true);
        session.setFailReason(t);

        ThrowableUtils.setMessage(t, getTypicalErrorMessage());
        final String m1 = t.getMessage();
        final String key1 = (String) ReflectionUtils.callStaticMethod(history.getClass(), "getErrorKey");

        ThrowableUtils.setMessage(t, getTypicalErrorMessage());
        final String m2 = t.getMessage();
        final String key2 = (String) ReflectionUtils.callStaticMethod(history.getClass(), "getErrorKey");

        Assert.assertFalse(m1.equals(m2));
        Assert.assertEquals(key1, key2);
    }

    @Test
    public void testGetErrorCount_differentKeys()
    {
        final String baseKey = "foo";
        for (int i = 0; i < 10; i++)
        {
            final TimedCounter c = (TimedCounter) ReflectionUtils.callStaticMethod(history.getClass(), "getErrorCount", baseKey + i);
            Assert.assertEquals(0, c.get());
        }
    }

    @Test
    public void testGetErrorCount_sameCounter()
    {
        final String key = "foo";
        final TimedCounter c1 = (TimedCounter) ReflectionUtils.callStaticMethod(history.getClass(), "getErrorCount", key);
        final TimedCounter c2 = (TimedCounter) ReflectionUtils.callStaticMethod(history.getClass(), "getErrorCount", key);
        Assert.assertSame(c1, c2);
    }

    @Test
    public void testGetErrorCount_concurrency()
    {
        final String key = "foo";
        final TimedCounter c1 = (TimedCounter) ReflectionUtils.callStaticMethod(history.getClass(), "getErrorCount", key);
        final TimedCounter c2 = (TimedCounter) ReflectionUtils.callStaticMethod(history.getClass(), "getErrorCount", key);
        Assert.assertSame(c1, c2);
    }

    /**
     * Dump mode: ALLWAYS<br>
     * Expected: grant permission
     */
    @Test
    public void testRequestDumpPermission_dumpModeAllways()
    {
        history.setDumpMode(DumpMode.ALWAYS);

        final boolean permissionGranted = (Boolean) ReflectionUtils.callMethod(history.getClass(), history, "requestDumpPermission");

        Assert.assertTrue(permissionGranted);
    }

    /**
     * Dump mode: ONERROR<br>
     * Session hasFailed: false<br>
     * Expected: deny permission (no session error, no dump necessary)
     */
    @Test
    public void testRequestDumpPermission_onError_noError()
    {
        history.setDumpMode(DumpMode.ON_ERROR);
        final SessionImpl session = (SessionImpl) Session.getCurrent();
        session.setFailed(false);
        session.setFailReason(null);

        final boolean permissionGranted = (Boolean) ReflectionUtils.callMethod(history.getClass(), history, "requestDumpPermission");

        Assert.assertFalse(permissionGranted);
    }

    /**
     * Dump mode: ONERROR<br>
     * Session hasFailed: true<br>
     * maxDumpCount: &lt; 0 <br>
     * Expected: TRUE (negative value is same as missing property -> no limit)
     */
    @Test
    public void testRequestDumpPermission_onError_negativeAllowedNrOfDumps()
    {
        history.setDumpMode(DumpMode.ON_ERROR);
        final SessionImpl session = (SessionImpl) Session.getCurrent();
        session.setFailed(true);
        session.setFailReason(new Throwable());

        ReflectionUtils.writeInstanceField(history, "maxDumpCount", -1);

        final boolean permissionGranted = (Boolean) ReflectionUtils.callMethod(history.getClass(), history, "requestDumpPermission");

        Assert.assertTrue(permissionGranted);
    }

    /**
     * Dump mode: ONERROR<br>
     * Session hasFailed: true<br>
     * maxDumpCount: == 0<br>
     * Expected: FALSE (because of maxDumpCount)
     */
    @Test
    public void testRequestDumpPermission_onError_zeroDumpsAllowed()
    {
        history.setDumpMode(DumpMode.ON_ERROR);
        ReflectionUtils.writeInstanceField(history, "maxDumpCount", 0);
        final SessionImpl session = (SessionImpl) Session.getCurrent();
        session.setFailed(true);
        session.setFailReason(new Throwable());

        final boolean permissionGranted = (Boolean) ReflectionUtils.callMethod(history.getClass(), history, "requestDumpPermission");

        Assert.assertFalse(permissionGranted);
    }

    /**
     * Dump mode: ONERROR<br>
     * Session hasFailed: true<br>
     * maxDumpCount: 2<br>
     * different errors: 1<br>
     * test error count: 4<br>
     * <br>
     * Request 4 dump permissions.<br>
     * Expected: grant permission to 1st and 2nd request, other permissions are denied
     */
    @Test
    public void testRequestDumpPermission_onError_dumpLimit_singleError()
    {
        for (int i = 0; i < 4; i++)
        {
            final RequestHistory requestHistory = getNewRequestHistory();
            requestHistory.setDumpMode(DumpMode.ON_ERROR);
            ReflectionUtils.writeInstanceField(requestHistory, "maxDumpCount", 2);
            final SessionImpl session = (SessionImpl) Session.getCurrent();
            session.setFailed(true);
            session.setFailReason(new Throwable());

            final boolean permissionGranted = (Boolean) ReflectionUtils.callMethod(requestHistory.getClass(), requestHistory,
                                                                                   "requestDumpPermission");

            if (i < 2)
            {
                Assert.assertTrue(permissionGranted);
            }
            else
            {
                Assert.assertFalse(permissionGranted);
            }
        }
    }

    /**
     * Dump mode: ONERROR<br>
     * Session hasFailed: true<br>
     * maxDumpCount: 2<br>
     * different errors: 2<br>
     * test error count: 8<br>
     * <br>
     * Request 4 dump permissions per error (we have 2 different errors).<br>
     * Expected: grant permission to the 1st and 2nd request of each error, other permissions are denied
     */
    @Test
    public void testRequestDumpPermission_onError_dumpLimit_differentErrors()
    {
        for (int i = 0; i < 8; i++)
        {
            final RequestHistory requestHistory = getNewRequestHistory();
            requestHistory.setDumpMode(DumpMode.ON_ERROR);
            ReflectionUtils.writeInstanceField(requestHistory, "maxDumpCount", 2);
            final SessionImpl session = (SessionImpl) Session.getCurrent();
            session.setFailed(true);
            session.setFailReason(new Throwable("#" + i % 2));

            final boolean permissionGranted = (Boolean) ReflectionUtils.callMethod(requestHistory.getClass(), requestHistory,
                                                                                   "requestDumpPermission");

            if (i < 4)
            {
                Assert.assertTrue(permissionGranted);
            }
            else
            {
                Assert.assertFalse(permissionGranted);
            }
        }
    }

    @Test
    public void test_maxDumpCount_notAvailable()
    {
        final String maxDumpcountPropertyName = ReflectionUtils.readStaticField(RequestHistory.class, "MAX_DUMP_COUNT_PROPERTY");
        XltProperties.getInstance().removeProperty(maxDumpcountPropertyName);

        final RequestHistory rh = getNewRequestHistory();
        final int maxDumpCount = ReflectionUtils.<Integer>readField(RequestHistory.class, rh, "maxDumpCount");

        Assert.assertTrue(maxDumpCount <= 0);
    }

    @Test
    public void test_maxDumpCount_negative()
    {
        final String maxDumpcountPropertyName = ReflectionUtils.readStaticField(RequestHistory.class, "MAX_DUMP_COUNT_PROPERTY");
        XltProperties.getInstance().setProperty(maxDumpcountPropertyName, "-1");

        final RequestHistory rh = getNewRequestHistory();
        final int maxDumpCount = ReflectionUtils.<Integer>readField(RequestHistory.class, rh, "maxDumpCount");

        Assert.assertTrue(maxDumpCount <= 0);
    }

    @Test
    public void test_maxDumpCount_zero()
    {
        final String maxDumpcountPropertyName = ReflectionUtils.readStaticField(RequestHistory.class, "MAX_DUMP_COUNT_PROPERTY");
        XltProperties.getInstance().setProperty(maxDumpcountPropertyName, "0");

        final RequestHistory rh = getNewRequestHistory();
        final int maxDumpCount = ReflectionUtils.<Integer>readField(RequestHistory.class, rh, "maxDumpCount");

        Assert.assertTrue(maxDumpCount <= 0);
    }

    @Test
    public void test_maxDumpCount_positive()
    {
        final String maxDumpcountPropertyName = ReflectionUtils.readStaticField(RequestHistory.class, "MAX_DUMP_COUNT_PROPERTY");
        XltProperties.getInstance().setProperty(maxDumpcountPropertyName, "3");

        final RequestHistory rh = getNewRequestHistory();
        final int maxDumpCount = ReflectionUtils.<Integer>readField(RequestHistory.class, rh, "maxDumpCount");

        Assert.assertEquals(3, maxDumpCount);
    }

    @Test
    public void test_resetInterval_notAvailable()
    {
        final String resetIntervalPropertyName = ReflectionUtils.readStaticField(RequestHistory.class, "COUNTER_RESET_INTERVAL_PROPERTY");
        XltProperties.getInstance().removeProperty(resetIntervalPropertyName);

        final long resetInterval = ReflectionUtils.<Long>callStaticMethod(RequestHistory.class, "getConfiguredCounterResetInterval");

        Assert.assertEquals(0, resetInterval);
    }

    @Test(expected = RuntimeException.class)
    public void test_resetInterval_negative()
    {
        final String resetIntervalPropertyName = ReflectionUtils.readStaticField(RequestHistory.class, "COUNTER_RESET_INTERVAL_PROPERTY");
        XltProperties.getInstance().setProperty(resetIntervalPropertyName, "-1");

        ReflectionUtils.callStaticMethod(RequestHistory.class, "getConfiguredCounterResetInterval");
    }

    @Test
    public void test_resetInterval_zero()
    {
        final String resetIntervalPropertyName = ReflectionUtils.readStaticField(RequestHistory.class, "COUNTER_RESET_INTERVAL_PROPERTY");
        XltProperties.getInstance().setProperty(resetIntervalPropertyName, "0");

        final long resetInterval = ReflectionUtils.<Long>callStaticMethod(RequestHistory.class, "getConfiguredCounterResetInterval");

        Assert.assertTrue(resetInterval <= 0);
    }

    @Test
    public void test_resetInterval_positive()
    {
        final String resetIntervalPropertyName = ReflectionUtils.readStaticField(RequestHistory.class, "COUNTER_RESET_INTERVAL_PROPERTY");
        XltProperties.getInstance().setProperty(resetIntervalPropertyName, "30");

        final long resetInterval = ReflectionUtils.<Long>callStaticMethod(RequestHistory.class, "getConfiguredCounterResetInterval");

        Assert.assertEquals(30 * 1000, resetInterval);
    }

    @Test
    public void test_configuredDumpCount_notAvailable()
    {
        final String maxDiffErrorsPropertyName = ReflectionUtils.readStaticField(RequestHistory.class, "MAX_DIFFERENT_ERRORS_PROPERTY");
        XltProperties.getInstance().removeProperty(maxDiffErrorsPropertyName);

        final int maxDiffErrorsDefault = ReflectionUtils.<Integer>readStaticField(RequestHistory.class, "MAX_DIFFERENT_ERRORS_DEFAULT");
        final ConcurrentLRUCache<String, TimedCounter> lruCache = ReflectionUtils.callStaticMethod(RequestHistory.class, "getDumpCounter");
        final int lruMaxSize = ReflectionUtils.<Integer>readField(ConcurrentLRUCache.class, lruCache, "maxSize");

        // if not configured the size must be 3 times as big as the default value
        Assert.assertEquals(maxDiffErrorsDefault * 3, lruMaxSize);
    }

    @Test
    public void test_configuredDumpCount_negative()
    {
        final String maxDiffErrorsPropertyName = ReflectionUtils.readStaticField(RequestHistory.class, "MAX_DIFFERENT_ERRORS_PROPERTY");
        XltProperties.getInstance().setProperty(maxDiffErrorsPropertyName, "-1");

        final ConcurrentLRUCache<String, TimedCounter> lruCache = ReflectionUtils.callStaticMethod(RequestHistory.class, "getDumpCounter");
        final int lruMaxSize = ReflectionUtils.<Integer>readField(lruCache.getClass(), lruCache, "maxSize");

        // size must be 3 times as big as configured value but minimal value is 30
        Assert.assertEquals(30, lruMaxSize);
    }

    @Test
    public void test_configuredDumpCount_zero()
    {
        final String maxDiffErrorsPropertyName = ReflectionUtils.readStaticField(RequestHistory.class, "MAX_DIFFERENT_ERRORS_PROPERTY");
        XltProperties.getInstance().setProperty(maxDiffErrorsPropertyName, "0");

        final int maxDiffErrorsDefault = ReflectionUtils.<Integer>readStaticField(RequestHistory.class, "MAX_DIFFERENT_ERRORS_DEFAULT");
        final ConcurrentLRUCache<String, TimedCounter> lruCache = ReflectionUtils.readStaticField(RequestHistory.class, "DUMP_COUNT");
        final int lruMaxSize = ReflectionUtils.<Integer>readField(lruCache.getClass(), lruCache, "maxSize");

        // if configured value is 0 the size is determined by the default value. So size must be 3 times as big as the
        // default value.
        Assert.assertEquals(maxDiffErrorsDefault * 3, lruMaxSize);
    }

    @Test
    public void test_configuredDumpCount_lowerThenTen()
    {
        final String maxDiffErrorsPropertyName = ReflectionUtils.readStaticField(RequestHistory.class, "MAX_DIFFERENT_ERRORS_PROPERTY");
        XltProperties.getInstance().setProperty(maxDiffErrorsPropertyName, "9");

        final ConcurrentLRUCache<String, TimedCounter> lruCache = ReflectionUtils.callStaticMethod(RequestHistory.class, "getDumpCounter");
        final int lruMaxSize = ReflectionUtils.<Integer>readField(lruCache.getClass(), lruCache, "maxSize");

        // size must be 3 times as big as configured value but minimal value is 30
        Assert.assertEquals(30, lruMaxSize);
    }

    @Test
    public void test_configuredDumpCount_Ten()
    {
        final String maxDiffErrorsPropertyName = ReflectionUtils.readStaticField(RequestHistory.class, "MAX_DIFFERENT_ERRORS_PROPERTY");
        XltProperties.getInstance().setProperty(maxDiffErrorsPropertyName, "10");

        final ConcurrentLRUCache<String, TimedCounter> lruCache = ReflectionUtils.callStaticMethod(RequestHistory.class, "getDumpCounter");
        final int lruMaxSize = ReflectionUtils.<Integer>readField(lruCache.getClass(), lruCache, "maxSize");

        // size must be 3 times as big as configured value but minimal value is 30
        Assert.assertEquals(30, lruMaxSize);
    }

    @Test
    public void test_configuredDumpCount_greaterThen10()
    {
        final String maxDiffErrorsPropertyName = ReflectionUtils.readStaticField(RequestHistory.class, "MAX_DIFFERENT_ERRORS_PROPERTY");
        XltProperties.getInstance().setProperty(maxDiffErrorsPropertyName, "30");

        final ConcurrentLRUCache<String, TimedCounter> lruCache = ReflectionUtils.callStaticMethod(RequestHistory.class, "getDumpCounter");
        final int lruMaxSize = ReflectionUtils.<Integer>readField(lruCache.getClass(), lruCache, "maxSize");

        // size must be 3 times as big as configured value
        Assert.assertEquals(90, lruMaxSize);
    }
}
