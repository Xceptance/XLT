/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.htmlunit.MockWebConnection;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;
import org.htmlunit.util.NameValuePair;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.common.net.HttpHeaderConstants;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;

/**
 * Tests for cached requests logging in {@link XltHttpWebConnection} and {@link CachingHttpWebConnection}.
 * <p>
 * Created by AI (Gemini 2.5 Pro).
 * </p>
 */
public class CachedRequestsLoggingTest
{
    @AfterClass
    public static void afterClass()
    {
        XltEngine.reset();
        SessionImpl.removeCurrent();
    }

    @Before
    public void setUp()
    {
        SessionImpl.removeCurrent();
        // enable cache for testing
        XltProperties.getInstance().setProperty(XltConstants.XLT_PACKAGE_PATH + ".staticContentCache", "true");
        // default enabled logging
        XltProperties.getInstance().setProperty(XltConstants.XLT_PACKAGE_PATH + ".http.cachedRequests.logging", "true");
        // default error dump
        XltProperties.getInstance().setProperty(com.xceptance.xlt.engine.resultbrowser.RequestHistory.OUTPUT2DISK_PROPERTY, "onError");

        SessionImpl.removeCurrent();
    }
    
    @After
    public void tearDown()
    {
        SessionImpl.removeCurrent();
    }

    private RequestData getLatestRequestData(SessionImpl session) throws Exception
    {
        Object requestHistory = session.getRequestHistory();
        Field pendingRequestsField = requestHistory.getClass().getDeclaredField("pendingRequests");
        pendingRequestsField.setAccessible(true);
        List<?> pendingRequests = (List<?>) pendingRequestsField.get(requestHistory);
        
        if (pendingRequests.isEmpty())
        {
            return null;
        }
        
        Object request = pendingRequests.get(pendingRequests.size() - 1);
        Field requestDataField = request.getClass().getDeclaredField("requestData");
        requestDataField.setAccessible(true);
        return (RequestData) requestDataField.get(request);
    }

    private List<NameValuePair> getCacheHeaders()
    {
        return Arrays.asList(
            new NameValuePair(HttpHeaderConstants.CACHE_CONTROL, "max-age=31536000") // 1 year from now
        );
    }

    @Test
    public void testDumpMgrSkipsCachedPayloads() throws Exception
    {
        XltProperties.getInstance().setProperty(com.xceptance.xlt.engine.resultbrowser.RequestHistory.OUTPUT2DISK_PROPERTY, "always");

        final XltWebClient wc = new XltWebClient();
        wc.setTimerName("test");

        final MockWebConnection mockWebConn = new MockWebConnection();
        String html = "<html><head><title>Test</title></head><body></body></html>";
        mockWebConn.setResponse(new URL("http://example.org/"), html, 200, "OK", "text/html", getCacheHeaders());

        wc.setWebConnection(new XltHttpWebConnection(wc, mockWebConn));

        final URL u = new URL("http://example.org/");
        
        // First request - cache miss
        wc.loadWebResponse(new WebRequest(u));
        
        // Clear pageLocalCache so the second request goes to the WebConnection
        Field pageLocalCacheField = wc.getClass().getDeclaredField("pageLocalCache");
        pageLocalCacheField.setAccessible(true);
        ((java.util.Map<?, ?>) pageLocalCacheField.get(wc)).clear();

        // Second request - cache hit
        wc.loadWebResponse(new WebRequest(u));
        
        SessionImpl session = (SessionImpl) Session.getCurrent();
        File dumpDir = new File(new File(session.getResultsDirectory().toFile(), XltConstants.DUMP_OUTPUT_DIR), session.getID());
        File responsesDir = new File(dumpDir, XltConstants.DUMP_RESPONSES_DIR);
        
        String[] files = responsesDir.list();
        Assert.assertNotNull("Responses directory should exist", files);
        Assert.assertEquals("Only the cache miss payload should be dumped", 1, files.length);
    }

    @Test
    public void testCacheHitLoggingEnabled() throws Exception
    {
        final XltWebClient wc = new XltWebClient();
        wc.setTimerName("test");

        final MockWebConnection mockWebConn = new MockWebConnection();
        String html = "<html><head><title>Test</title></head><body></body></html>";
        mockWebConn.setResponse(new URL("http://example.org/"), html, 200, "OK", "text/html", getCacheHeaders());

        wc.setWebConnection(new XltHttpWebConnection(wc, mockWebConn));

        final URL u = new URL("http://example.org/");
        
        // First request - should be a cache miss
        WebRequest req1 = new WebRequest(u);
        WebResponse r1 = wc.loadWebResponse(req1);
        Assert.assertEquals(200, r1.getStatusCode());
        
        // Verify it was cached!
        java.lang.reflect.Field cacheField = CachingHttpWebConnection.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        com.xceptance.common.collection.ConcurrentLRUCache<String, Object> cacheObj = 
            (com.xceptance.common.collection.ConcurrentLRUCache<String, Object>) cacheField.get(wc.getWebConnection());
        Object cacheEntry = cacheObj.get(u.toExternalForm());
        Assert.assertNotNull("Request should be in cache", cacheEntry);
        
        java.lang.reflect.Field expiresField = cacheEntry.getClass().getDeclaredField("expires");
        expiresField.setAccessible(true);
        long expires = expiresField.getLong(cacheEntry);
        Assert.assertTrue("Cache entry should not be expired. Expires: " + expires + ", Current: " + System.currentTimeMillis(), expires > System.currentTimeMillis());

        // Clear pageLocalCache so the second request goes to the WebConnection
        Field pageLocalCacheField = wc.getClass().getDeclaredField("pageLocalCache");
        pageLocalCacheField.setAccessible(true);
        ((java.util.Map<?, ?>) pageLocalCacheField.get(wc)).clear();
        
        RequestData data1 = getLatestRequestData((SessionImpl) Session.getCurrent());
        Assert.assertNotNull(data1);
        Assert.assertFalse(data1.isCached());
        Assert.assertTrue(data1.getRunTime() >= 0);
        
        // Second request - should be a cache hit
        WebRequest req2 = new WebRequest(u);
        WebResponse r2 = wc.loadWebResponse(req2);
        Assert.assertEquals(200, r2.getStatusCode());

        RequestData data2 = getLatestRequestData((SessionImpl) Session.getCurrent());
        Assert.assertNotNull(data2);
        Assert.assertNotSame(data1, data2);
        Assert.assertTrue("Request should be marked as cached", data2.isCached());
        Assert.assertEquals(0, data2.getRunTime());
        Assert.assertEquals(0, data2.getConnectTime());
        Assert.assertEquals(0, data2.getSendTime());
        Assert.assertEquals(0, data2.getServerBusyTime());
        Assert.assertEquals(0, data2.getReceiveTime());
        Assert.assertEquals(0, data2.getTimeToFirstBytes());
        Assert.assertEquals(0, data2.getTimeToLastBytes());
        Assert.assertEquals(0, data2.getDnsTime());
        Assert.assertEquals(0, data2.getBytesSent());
        Assert.assertEquals(data1.getBytesReceived(), data2.getBytesReceived());
    }
    
    @Test
    public void testCacheHitLoggingDisabled() throws Exception
    {
        // The logCachedRequests flag is a static final field read once at class load time.
        // We must use reflection to toggle it for this test.
        final Field logCachedRequestsField = XltHttpWebConnection.class.getDeclaredField("logCachedRequests");
        logCachedRequestsField.setAccessible(true);

        try
        {
            // disable cached request logging via reflection
            logCachedRequestsField.setBoolean(null, false);

            final XltWebClient wc = new XltWebClient();
            wc.setTimerName("test");

            final MockWebConnection mockWebConn = new MockWebConnection();
            String html = "<html><head><title>Test</title></head><body></body></html>";
            mockWebConn.setResponse(new URL("http://example.org/"), html, 200, "OK", "text/html", getCacheHeaders());

            wc.setWebConnection(new XltHttpWebConnection(wc, mockWebConn));

            final URL u = new URL("http://example.org/");

            // First request - should be a cache miss and logged
            WebRequest req1 = new WebRequest(u);
            WebResponse r1 = wc.loadWebResponse(req1);
            Assert.assertEquals(200, r1.getStatusCode());

            RequestData data1 = getLatestRequestData((SessionImpl) Session.getCurrent());
            Assert.assertNotNull(data1);
            Assert.assertFalse(data1.isCached());

            // Clear pageLocalCache so the second request goes to the WebConnection
            Field pageLocalCacheField = wc.getClass().getDeclaredField("pageLocalCache");
            pageLocalCacheField.setAccessible(true);
            ((java.util.Map<?, ?>) pageLocalCacheField.get(wc)).clear();

            // Second request - should be a cache hit, but NOT logged
            WebRequest req2 = new WebRequest(u);
            WebResponse r2 = wc.loadWebResponse(req2);
            Assert.assertEquals(200, r2.getStatusCode());

            RequestData data2 = getLatestRequestData((SessionImpl) Session.getCurrent());
            Assert.assertNotNull(data2);
            Assert.assertSame("No new RequestData should have been logged", data1, data2);
        }
        finally
        {
            // restore original value to avoid contaminating other tests
            logCachedRequestsField.setBoolean(null, true);
        }
    }
}
