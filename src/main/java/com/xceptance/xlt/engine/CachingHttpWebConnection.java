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
import java.text.ParseException;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.htmlunit.HttpMethod;
import org.htmlunit.WebConnection;
import org.htmlunit.WebRequest;
import org.htmlunit.WebResponse;

import com.xceptance.common.collection.ConcurrentLRUCache;
import com.xceptance.common.net.HttpHeaderConstants;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;

/**
 * The CachingHttpWebConnection class adds caching capabilities to the standard HttpWebConnection. Like a browser, it
 * tries to minimize the network load when querying resources. Resources loaded the first time are put into a cache.
 * Once a previously loaded resource is requested again, it is served from the cache. If the cache time of the content
 * is expired, the server is asked to revalidate the content using a conditional GET request with the
 * "If-Modified-Since" header set. Only if there is new data, the cache is updated.
 * 
 * @see https://developer.mozilla.org/en-US/docs/Web/HTTP/Caching
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class CachingHttpWebConnection extends WebConnectionWrapper
{
    /**
     * The date format used in HTTP header values, for example: "Tue, 11 Sep 2007 08:01:38 GMT".
     */
    static final FastDateFormat HEADER_DATE_FORMAT = FastDateFormat.getInstance("EEE, d MMM yyyy HH:mm:ss z", TimeZone.getTimeZone("GMT"),
                                                                                Locale.ENGLISH);

    private static final Pattern MAX_AGE_PATTERN = Pattern.compile(Pattern.quote(HttpHeaderConstants.MAX_AGE) + "=(\\d+)");

    /**
     * An entry in the response cache.
     */
    private static class CacheEntry
    {
        /**
         * The web response.
         */
        public long expires;

        /**
         * The last modified date of the resource (as returned from the server).
         */
        public String lastModified;

        /**
         * The etag value of the resource (as returned from the server).
         */
        public String etag;

        /**
         * The web response.
         */
        public WebResponse webResponse;
    }

    /**
     * Tries to determine the time when the given web response expires. This is done by examining the response headers.
     * A return value which is less than the current time denotes a web response that has expired (is stale) and needs
     * to be revalidated. A value of -1 indicates that the response is not cacheable at all.
     * 
     * @param webResponse
     *            the web response to check
     * @return the expiration time
     */
    public static long determineExpirationTime(final WebResponse webResponse)
    {
        /*
         * Quick checks first.
         */

        // check the "Cache-Control" header (part 1)
        final String cacheControl = webResponse.getResponseHeaderValue(HttpHeaderConstants.CACHE_CONTROL);
        if (cacheControl != null && cacheControl.length() > 0)
        {
            // is there a "no-store" value?
            if (cacheControl.contains(HttpHeaderConstants.NO_STORE))
            {
                return -1;
            }

            // is there a "no-cache" value?
            if (cacheControl.contains(HttpHeaderConstants.NO_CACHE))
            {
                return 0;
            }

            // is there a "must-revalidate" value?
            if (cacheControl.contains(HttpHeaderConstants.MUST_REVALIDATE))
            {
                return 0;
            }
        }

        // check the deprecated "Pragma" header
        final String pragma = webResponse.getResponseHeaderValue(HttpHeaderConstants.PRAGMA);
        if (pragma != null && pragma.length() > 0)
        {
            // is there a "no-cache" value?
            if (pragma.contains(HttpHeaderConstants.NO_CACHE))
            {
                return 0;
            }
        }

        /*
         * Determine the date when the resource was generated at the server.
         */

        long date = System.currentTimeMillis();

        // check the "Date" header
        final String dateValue = webResponse.getResponseHeaderValue(HttpHeaderConstants.DATE);
        if (dateValue != null && dateValue.length() > 0)
        {
            try
            {
                date = HEADER_DATE_FORMAT.parse(dateValue).getTime();
            }
            catch (final ParseException ex)
            {
                if (XltLogger.runTimeLogger.isWarnEnabled())
                {
                    XltLogger.runTimeLogger.warn("Header " + HttpHeaderConstants.DATE +
                                                 " does not match a valid date format. Check RFC 2616. Should be " +
                                                 "a valid RFC 1123 format, such as 'Thu, 01 Dec 1994 16:00:00 GMT', but was '" + dateValue +
                                                 "'.");
                }
            }
        }

        /*
         * Determine the expiration time.
         */

        // check the "Cache-Control" header (part 2)
        if (cacheControl != null && cacheControl.length() > 0)
        {
            // is there a "max-age=" value? (Format: "max-age=65289")
            final Matcher m = MAX_AGE_PATTERN.matcher(cacheControl);
            if (m.find() && m.groupCount() > 0)
            {
                try
                {
                    final long maxAge = Long.parseLong(m.group(1)) * 1000;

                    return date + maxAge;
                }
                catch (final NumberFormatException ex)
                {
                    // ignore, try next header
                }
            }
        }

        // check the "Expires" header
        final String expires = webResponse.getResponseHeaderValue(HttpHeaderConstants.EXPIRES);
        if (expires != null && expires.length() > 0)
        {
            if (expires.trim().equals("0"))
            {
                // '0' is commonly used to indicate that a response expires immediately
                return 0;
            }
            else
            {
                try
                {
                    return HEADER_DATE_FORMAT.parse(expires).getTime();
                }
                catch (final ParseException ex)
                {
                    if (XltLogger.runTimeLogger.isWarnEnabled())
                    {
                        XltLogger.runTimeLogger.warn("Header " + HttpHeaderConstants.EXPIRES +
                                                     " does not match a valid date format. Check RFC 2616. Should be " +
                                                     "a valid RFC 1123 format, such as 'Thu, 01 Dec 1994 16:00:00 GMT', but was '" +
                                                     expires + "'.");
                    }

                    // invalid date -> expires immediately
                    return 0;
                }
            }
        }

        // check the "Last-Modified" header
        final String lastModified = webResponse.getResponseHeaderValue(HttpHeaderConstants.LAST_MODIFIED);
        if (lastModified != null && lastModified.length() > 0)
        {
            try
            {
                final long lastModifiedTime = HEADER_DATE_FORMAT.parse(lastModified).getTime();
                final long age = Math.max(date - lastModifiedTime, 0);

                // use 10% of the current age as a heuristic expiration value
                return date + age / 10;
            }
            catch (final ParseException ex)
            {
                if (XltLogger.runTimeLogger.isWarnEnabled())
                {
                    XltLogger.runTimeLogger.warn("Header " + HttpHeaderConstants.LAST_MODIFIED +
                                                 " does not match a valid date format. Check RFC 2616. Should be " +
                                                 "a valid RFC 1123 format, such as 'Thu, 01 Dec 1994 16:00:00 GMT', but was '" + expires +
                                                 "'.");
                }

                // invalid date -> expires immediately
                return 0;
            }
        }

        // expires immediately
        return 0;
    }

    /**
     * A cache that maps URL strings to their corresponding web responses.
     */
    private final ConcurrentLRUCache<String, CacheEntry> cache;

    /**
     * Is true when cache usage is enabled.
     */
    private final boolean useCache;

    /**
     * Creates a new CachingHttpWebConnection.
     * 
     * @param webConnection
     *            the underlying web connection to use
     */
    public CachingHttpWebConnection(final WebConnection webConnection)
    {
        super(webConnection);

        useCache = XltProperties.getInstance().getProperty(XltConstants.XLT_PACKAGE_PATH + ".staticContentCache", false);
        if (useCache)
        {
            final int cacheSize = XltProperties.getInstance().getProperty(XltConstants.XLT_PACKAGE_PATH + ".staticContentCache.size", 100);
            if (cacheSize < ConcurrentLRUCache.MIN_SIZE)
            {
                XltLogger.runTimeLogger.warn("Size of static content cache is lower than minimum size of " + ConcurrentLRUCache.MIN_SIZE +
                                             ". Will use the minimum size.");
            }
            cache = new ConcurrentLRUCache<String, CacheEntry>(Math.max(cacheSize, ConcurrentLRUCache.MIN_SIZE));
        }
        else
        {
            cache = null;
        }
    }

    /**
     * Loads the web response for a given set of request parameters. Tries to find the resource in the cache, when
     * request is a GET and caching is active. If found the resource is loaded using an if-modified-since.
     * 
     * @param webRequest
     *            the request parameters
     * @return the web response loaded
     * @throws IOException
     *             if something went wrong
     */
    @Override
    public WebResponse getResponse(final WebRequest webRequest) throws IOException
    {
        // using cache only when active and cache only for a GET
        if (!useCache || !webRequest.getHttpMethod().equals(HttpMethod.GET))
        {
            return getResponse(webRequest, null, null);
        }

        final String url = webRequest.getUrl().toExternalForm();

        // check whether we have an entry for this URL in the cache
        CacheEntry cacheEntry = cache.get(url);
        if (cacheEntry == null)
        {
            // no -> load the response normally
            final WebResponse webResponse = getResponse(webRequest, null, null);

            // check whether the response is cacheable
            final long expires = determineExpirationTime(webResponse);
            if (webResponse.getStatusCode() == 200 && expires > -1)
            {
                // yes, put it in the cache, even if the response is already expired
                cacheEntry = new CacheEntry();

                cacheEntry.webResponse = webResponse;
                cacheEntry.expires = expires;
                cacheEntry.lastModified = webResponse.getResponseHeaderValue(HttpHeaderConstants.LAST_MODIFIED);
                cacheEntry.etag = webResponse.getResponseHeaderValue(HttpHeaderConstants.ETAG);

                cache.put(url, cacheEntry);
            }

            // return the response just read
            return webResponse;
        }

        // yes, it's in the cache

        // check whether the entry is expired
        if (cacheEntry.expires < System.currentTimeMillis())
        {
            // expired -> revalidate the cached response (using a conditional GET)
            final WebResponse webResponse = getResponse(webRequest, cacheEntry.lastModified, cacheEntry.etag);

            // check whether the response is cacheable
            final long expires = determineExpirationTime(webResponse);
            if (expires > -1)
            {
                cacheEntry.expires = expires;

                // check the HTTP response code
                if (webResponse.getStatusCode() == 200)
                {
                    // there is new content -> save the response
                    cacheEntry.webResponse = webResponse;
                    cacheEntry.lastModified = webResponse.getResponseHeaderValue(HttpHeaderConstants.LAST_MODIFIED);
                    cacheEntry.etag = webResponse.getResponseHeaderValue(HttpHeaderConstants.ETAG);

                    return webResponse;
                }
            }
            if (webResponse.getStatusCode() == 304)
            {
                // already expired or no expiration info at the response
                // however, we got a 304 which means that the content did not change, so use the cache!
                return cacheEntry.webResponse;
            }

            // either non-cacheable or wrong response code
            cache.remove(url);

            // return the response just read
            return webResponse;
        }

        // not expired -> return the cached response
        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            XltLogger.runTimeLogger.info("Return cached response for " + webRequest.getUrl());
        }

        return cacheEntry.webResponse;
    }

    /**
     * Loads the web response for the given set of request parameters. The resource is loaded using a conditional GET
     * with the "If-Modified-Since" request header set to the specified last-modified date and etag (if known). If a
     * additional header flags is <code>null</code> or empty, it won't get attached. If no additional header is
     * attached, a "normal" GET request is executed.
     * 
     * @param webRequest
     *            the request parameters
     * @param lastModifiedHeader
     *            the last-modified date
     * @param etag
     *            the etag value
     * @return the web response loaded
     * @throws IOException
     *             if something went wrong
     */
    protected WebResponse getResponse(final WebRequest webRequest, final String lastModifiedHeader, final String etag) throws IOException
    {
        // add the If-Modified-Since header only if there is a last-modified date
        if (lastModifiedHeader != null && lastModifiedHeader.length() > 0)
        {
            webRequest.setAdditionalHeader(HttpHeaderConstants.IF_MODIFIED_SINCE, lastModifiedHeader);
        }

        // add the If-None-Match header only if there is an etag value
        if (StringUtils.isNotBlank(etag))
        {
            webRequest.setAdditionalHeader(HttpHeaderConstants.IF_NONE_MATCH, etag);
        }

        return getWrappedWebConnection().getResponse(webRequest);
    }
}
