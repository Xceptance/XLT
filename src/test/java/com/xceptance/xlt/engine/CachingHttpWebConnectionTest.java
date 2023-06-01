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
package com.xceptance.xlt.engine;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.htmlunit.HttpMethod;
import org.htmlunit.WebResponse;
import org.htmlunit.WebResponseData;
import org.htmlunit.util.NameValuePair;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test some details of the caching ({@link CachingHttpWebConnection}).
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class CachingHttpWebConnectionTest
{
    // Status=OK - 200
    // Date=Fri, 14 Sep 2007 16:23:12 GMT
    // Server=Apache
    // Content-Length=25026
    // Cache-Control=no-cache,no-store
    // Pragma=no-cache
    // Expires=Thu, 01 Dec 1994 16:00:00 GMT
    // Accept-Ranges=bytes
    // Keep-Alive=timeout=5, max=100
    // Connection=Keep-Alive
    // Content-Type=text/html;charset=UTF-8

    private WebResponse buildWebResponse(final NameValuePair... headers) throws Exception
    {
        final String body = "<html><head><title>Test</title></head><body></body></html>";
        final WebResponseData responseData = new WebResponseData(body.getBytes(), 200, "No message", Arrays.asList(headers));

        return new WebResponse(responseData, new URL("http://www.test.com/"), HttpMethod.GET, 2000);
    }

    @Test
    public void testPragma() throws Exception
    {
        final WebResponse response = buildWebResponse(new NameValuePair("Pragma", "no-cache"),
                                                      new NameValuePair("Date", "Thu, 01 Dec 1993 16:00:00 GMT"),
                                                      new NameValuePair("Expires", "Thu, 01 Dec 1994 16:00:00 GMT"));

        final long time = CachingHttpWebConnection.determineExpirationTime(response);
        Assert.assertEquals(0, time);
    }

    @Test
    public void testExpires() throws Exception
    {
        final WebResponse response = buildWebResponse(new NameValuePair("Date", "Thu, 01 Dec 1993 16:00:00 GMT"),
                                                      new NameValuePair("Expires", "Thu, 01 Dec 1994 16:00:00 GMT"));

        final long time = CachingHttpWebConnection.determineExpirationTime(response);
        Assert.assertEquals(786297600000L, time);
    }

    @Test
    public void testExpires_ExpiresIs0() throws Exception
    {
        // expiration string doesn't contain leading or trailing whitespace
        {
            final WebResponse response = buildWebResponse(new NameValuePair("Expires", "0"));
            final long time = CachingHttpWebConnection.determineExpirationTime(response);
            Assert.assertEquals(0, time);
        }

        // expiration string contains leading and/or trailing whitespace
        {
            final WebResponse response = buildWebResponse(new NameValuePair("Expires", " 0  "));
            final long time = CachingHttpWebConnection.determineExpirationTime(response);
            Assert.assertEquals(0, time);
        }
    }

    @Test
    public void testExpires_ExpiresIsInvalid() throws Exception
    {
        // expiration string doesn't contain leading or trailing whitespace
        {
            final WebResponse response = buildWebResponse(new NameValuePair("Expires", "010101010101010101"));
            final long time = CachingHttpWebConnection.determineExpirationTime(response);
            Assert.assertEquals(0, time);
        }

        // expiration string contains leading and/or trailing whitespace
        {
            final WebResponse response = buildWebResponse(new NameValuePair("Expires", "  010101010101010101  "));
            final long time = CachingHttpWebConnection.determineExpirationTime(response);
            Assert.assertEquals(0, time);
        }
    }

    @Test
    public void testExpires_CacheControlNoCache() throws Exception
    {
        // max-age overwrites expires
        final WebResponse response = buildWebResponse(new NameValuePair("Date", "Thu, 01 Dec 1993 16:00:00 GMT"), // 754758000000
                                                      new NameValuePair("Expires", "Thu, 01 Dec 1994 16:00:00 GMT"),
                                                      new NameValuePair("Cache-Control", "no-cache")); // 786297600000

        final long time = CachingHttpWebConnection.determineExpirationTime(response);
        Assert.assertEquals(0, time);
    }

    @Test
    public void testExpires_CacheControlMaxAge() throws Exception
    {
        final WebResponse response = buildWebResponse(new NameValuePair("Date", "Thu, 01 Dec 1993 16:00:00 GMT"), // 754758000000
                                                      new NameValuePair("Expires", "Thu, 01 Dec 1994 16:00:00 GMT"),
                                                      new NameValuePair("Cache-Control", "private, max-age=100; bar")); // 786297600000

        final long time = CachingHttpWebConnection.determineExpirationTime(response);
        // 2 millisecond delta to avoid clock problems
        Assert.assertEquals(System.currentTimeMillis() + 100 * 1000, time, 2);
    }

    @Test
    public void testExpires_CacheControlMaxAge0() throws Exception
    {
        final WebResponse response = buildWebResponse(new NameValuePair("Date", "Thu, 01 Dec 1993 16:00:00 GMT"), // 754758000000
                                                      new NameValuePair("Expires", "Thu, 01 Dec 1994 16:00:00 GMT"),
                                                      new NameValuePair("Cache-Control", "foo; max-age=0; public")); // 786297600000

        final long time = CachingHttpWebConnection.determineExpirationTime(response);
        // 2 millisecond delta to avoid clock problems
        Assert.assertEquals(System.currentTimeMillis(), time, 2);
    }

    @Test
    public void testExpires_CacheControlMustRevalidate() throws Exception
    {
        // max-age overwrites expires
        final WebResponse response = buildWebResponse(new NameValuePair("Date", "Thu, 01 Dec 1993 16:00:00 GMT"), // 754758000000
                                                      new NameValuePair("Expires", "Thu, 01 Dec 1994 16:00:00 GMT"),
                                                      new NameValuePair("Cache-Control", "must-revalidate")); // 786297600000

        final long time = CachingHttpWebConnection.determineExpirationTime(response);
        Assert.assertEquals(0, time);
    }

    @Test
    public void testExpires_CacheControlPublic_MaxAge() throws Exception
    {
        // max-age overwrites expires
        final WebResponse response = buildWebResponse(new NameValuePair("Date", "Thu, 01 Dec 1993 16:00:00 GMT"), // 754758000000
                                                      new NameValuePair("Expires", "Thu, 01 Dec 1994 16:00:00 GMT"),
                                                      new NameValuePair("Cache-Control", "public, max-age=15500000"));

        final long current = System.currentTimeMillis();
        final long expected = current + (15500000L * 1000);
        final long time = CachingHttpWebConnection.determineExpirationTime(response);
        final long diff = expected - time;

        Assert.assertTrue(diff < 10);
    }

    @Test
    public void testExpires_LastModifiedInThePast() throws Exception
    {
        final long now = System.currentTimeMillis();

        final WebResponse response = buildWebResponse(new NameValuePair("Last-Modified", formatDate(now - 1000)));
        final long time = CachingHttpWebConnection.determineExpirationTime(response);
        Assert.assertTrue(time >= now + 100);
    }

    @Test
    public void testExpires_LastModifiedInTheFuture() throws Exception
    {
        final long now = System.currentTimeMillis();

        final WebResponse response = buildWebResponse(new NameValuePair("Last-Modified", formatDate(now + 5000)));
        final long time = CachingHttpWebConnection.determineExpirationTime(response);
        Assert.assertEquals(0, time);
    }

    @Test
    public void testExpires_NoCachingHints() throws Exception
    {
        final long time = CachingHttpWebConnection.determineExpirationTime(buildWebResponse());

        Assert.assertEquals(0, time);
    }

    private String formatDate(final long time)
    {
        return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH).format(new Date(time));
    }
}
