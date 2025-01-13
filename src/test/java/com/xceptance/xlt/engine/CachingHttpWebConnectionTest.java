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

import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;

import org.htmlunit.HttpMethod;
import org.htmlunit.WebResponse;
import org.htmlunit.WebResponseData;
import org.htmlunit.util.NameValuePair;
import org.junit.Assert;
import org.junit.Test;

import com.xceptance.common.net.HttpHeaderConstants;

/**
 * Test some details of the caching ({@link CachingHttpWebConnection}).
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class CachingHttpWebConnectionTest
{
    /** Thu, 01 Dec 1993 16:00:00 GMT */
    private static final long DATE = 754761600000L;

    private static final long EXPIRES = Instant.ofEpochMilli(DATE).plus(365, ChronoUnit.DAYS).toEpochMilli();

    private static final String DATE_STRING = formatDate(DATE);

    private static final String EXPIRES_STRING = formatDate(EXPIRES);

    private WebResponse buildWebResponse(final NameValuePair... headers) throws Exception
    {
        final String body = "<html><head><title>Test</title></head><body></body></html>";
        final WebResponseData responseData = new WebResponseData(body.getBytes(), 200, "No message", Arrays.asList(headers));

        return new WebResponse(responseData, new URL("http://www.test.com/"), HttpMethod.GET, 2000);
    }

    @Test
    public void testPragma() throws Exception
    {
        final WebResponse response = buildWebResponse(new NameValuePair(HttpHeaderConstants.PRAGMA, HttpHeaderConstants.NO_CACHE),
                                                      new NameValuePair(HttpHeaderConstants.DATE, DATE_STRING),
                                                      new NameValuePair(HttpHeaderConstants.EXPIRES, EXPIRES_STRING));

        final long time = CachingHttpWebConnection.determineExpirationTime(response);
        Assert.assertEquals(0, time);
    }

    @Test
    public void testExpires() throws Exception
    {
        final WebResponse response = buildWebResponse(new NameValuePair(HttpHeaderConstants.DATE, DATE_STRING),
                                                      new NameValuePair(HttpHeaderConstants.EXPIRES, EXPIRES_STRING));

        final long time = CachingHttpWebConnection.determineExpirationTime(response);
        Assert.assertEquals(EXPIRES, time);
    }

    @Test
    public void testExpires_ExpiresIs0() throws Exception
    {
        // expiration string doesn't contain leading or trailing whitespace
        {
            final WebResponse response = buildWebResponse(new NameValuePair(HttpHeaderConstants.EXPIRES, "0"));
            final long time = CachingHttpWebConnection.determineExpirationTime(response);
            Assert.assertEquals(0, time);
        }

        // expiration string contains leading and/or trailing whitespace
        {
            final WebResponse response = buildWebResponse(new NameValuePair(HttpHeaderConstants.EXPIRES, " 0  "));
            final long time = CachingHttpWebConnection.determineExpirationTime(response);
            Assert.assertEquals(0, time);
        }
    }

    @Test
    public void testExpires_ExpiresIsInvalid() throws Exception
    {
        // expiration string doesn't contain leading or trailing whitespace
        {
            final WebResponse response = buildWebResponse(new NameValuePair(HttpHeaderConstants.EXPIRES, "010101010101010101"));
            final long time = CachingHttpWebConnection.determineExpirationTime(response);
            Assert.assertEquals(0, time);
        }

        // expiration string contains leading and/or trailing whitespace
        {
            final WebResponse response = buildWebResponse(new NameValuePair(HttpHeaderConstants.EXPIRES, "  010101010101010101  "));
            final long time = CachingHttpWebConnection.determineExpirationTime(response);
            Assert.assertEquals(0, time);
        }
    }

    @Test
    public void testExpires_CacheControlNoCache() throws Exception
    {
        // no-cache overwrites expires
        final WebResponse response = buildWebResponse(new NameValuePair(HttpHeaderConstants.DATE, DATE_STRING),
                                                      new NameValuePair(HttpHeaderConstants.EXPIRES, EXPIRES_STRING),
                                                      new NameValuePair(HttpHeaderConstants.CACHE_CONTROL, "no-cache"));

        final long time = CachingHttpWebConnection.determineExpirationTime(response);
        Assert.assertEquals(0, time);
    }

    @Test
    public void testExpires_CacheControlMaxAge() throws Exception
    {
        // max-age overwrites expires
        final WebResponse response = buildWebResponse(new NameValuePair(HttpHeaderConstants.DATE, DATE_STRING),
                                                      new NameValuePair(HttpHeaderConstants.EXPIRES, EXPIRES_STRING),
                                                      new NameValuePair(HttpHeaderConstants.CACHE_CONTROL, "private, max-age=100; bar"));

        final long time = CachingHttpWebConnection.determineExpirationTime(response);
        Assert.assertEquals(DATE + 100 * 1000, time);
    }

    @Test
    public void testExpires_CacheControlMaxAge0() throws Exception
    {
        // max-age overwrites expires
        final WebResponse response = buildWebResponse(new NameValuePair(HttpHeaderConstants.DATE, DATE_STRING),
                                                      new NameValuePair(HttpHeaderConstants.EXPIRES, EXPIRES_STRING),
                                                      new NameValuePair(HttpHeaderConstants.CACHE_CONTROL, "foo; max-age=0; public"));

        final long time = CachingHttpWebConnection.determineExpirationTime(response);
        Assert.assertEquals(DATE, time);
    }

    @Test
    public void testExpires_CacheControlMustRevalidate() throws Exception
    {
        final WebResponse response = buildWebResponse(new NameValuePair(HttpHeaderConstants.DATE, DATE_STRING),
                                                      new NameValuePair(HttpHeaderConstants.EXPIRES, EXPIRES_STRING),
                                                      new NameValuePair(HttpHeaderConstants.CACHE_CONTROL, "must-revalidate"));

        final long time = CachingHttpWebConnection.determineExpirationTime(response);
        Assert.assertEquals(0, time);
    }

    @Test
    public void testExpires_LastModifiedInThePast() throws Exception
    {
        final long lastModified = DATE - 1_000_000L;

        final WebResponse response = buildWebResponse(new NameValuePair(HttpHeaderConstants.DATE, DATE_STRING),
                                                      new NameValuePair(HttpHeaderConstants.LAST_MODIFIED, formatDate(lastModified)));

        final long time = CachingHttpWebConnection.determineExpirationTime(response);
        Assert.assertEquals(DATE + 1_000_000L / 10, time);
    }

    @Test
    public void testExpires_LastModifiedInTheFuture() throws Exception
    {
        final long lastModified = DATE + 1_000_000L;

        final WebResponse response = buildWebResponse(new NameValuePair(HttpHeaderConstants.DATE, DATE_STRING),
                                                      new NameValuePair(HttpHeaderConstants.LAST_MODIFIED, formatDate(lastModified)));

        final long time = CachingHttpWebConnection.determineExpirationTime(response);
        Assert.assertEquals(DATE, time);
    }

    @Test
    public void testExpires_NoCachingHints() throws Exception
    {
        final long time = CachingHttpWebConnection.determineExpirationTime(buildWebResponse());

        Assert.assertEquals(0, time);
    }

    private static String formatDate(final long time)
    {
        return CachingHttpWebConnection.HEADER_DATE_FORMAT.format(new Date(time));
    }
}
