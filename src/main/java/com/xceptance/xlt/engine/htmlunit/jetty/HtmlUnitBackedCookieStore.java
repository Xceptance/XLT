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
package com.xceptance.xlt.engine.htmlunit.jetty;

import java.io.Serializable;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.util.Cookie;

/**
 * An implementation of a Jetty-compatible {@link CookieStore} that uses HtmlUnit's {@link CookieManager} as back end.
 */
public final class HtmlUnitBackedCookieStore implements CookieStore, Serializable
{
    /**
     * HtmlUnit's cookie manager.
     */
    private final CookieManager cookieManager;

    /**
     * Constructor.
     *
     * @param cookieManager
     *            the underlying HtmlUnit cookie manager
     */
    public HtmlUnitBackedCookieStore(final CookieManager cookieManager)
    {
        this.cookieManager = cookieManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void add(final URI uri, final HttpCookie httpCookie)
    {
        cookieManager.addCookie(toHtmlUnitCookie(httpCookie));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized List<HttpCookie> get(final URI uri)
    {
        cookieManager.clearExpired(new Date());

        return cookieManager.getCookies().stream().filter(htmlUnitCookie -> matchesUri(htmlUnitCookie, uri))
                            .map(HtmlUnitBackedCookieStore::toHttpCookie).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized List<HttpCookie> getCookies()
    {
        cookieManager.clearExpired(new Date());

        return cookieManager.getCookies().stream().map(HtmlUnitBackedCookieStore::toHttpCookie).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized List<URI> getURIs()
    {
        // TODO: URI?
        // return Collections.emptyList();

        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean remove(final URI uri, final HttpCookie httpCookie)
    {
        final int cookieCountBefore = cookieManager.getCookies().size();
        cookieManager.removeCookie(toHtmlUnitCookie(httpCookie));
        final int cookieCountAfter = cookieManager.getCookies().size();

        return cookieCountBefore > cookieCountAfter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean removeAll()
    {
        final int cookieCountBefore = cookieManager.getCookies().size();
        cookieManager.clearCookies();
        final int cookieCountAfter = cookieManager.getCookies().size();

        return cookieCountBefore > cookieCountAfter;
    }

    /**
     * Converts the passed Jetty cookie instance to its HtmlUnit equivalent.
     *
     * @param httpCookie
     *            the source cookie
     * @return the converted cookie
     */
    private static Cookie toHtmlUnitCookie(final HttpCookie httpCookie)
    {
        // determine expiry date from max age
        final long maxAge = httpCookie.getMaxAge();

        final Date expiryDate;
        if (maxAge == -1)
        {
            // never expires
            expiryDate = null;
        }
        else
        {
            expiryDate = new Date(System.currentTimeMillis() + maxAge * 1000);
        }

        // now create the cookie
        return new Cookie(httpCookie.getDomain(), httpCookie.getName(), httpCookie.getValue(), httpCookie.getPath(), expiryDate,
                          httpCookie.getSecure(), httpCookie.isHttpOnly());
    }

    /**
     * Converts the passed HtmlUnit cookie instance to its Jetty equivalent.
     *
     * @param htmlUnitCookie
     *            the source cookie
     * @return the converted cookie
     */
    private static HttpCookie toHttpCookie(final Cookie htmlUnitCookie)
    {
        // determine max age from expiry date
        final long maxAge;

        final Date expiryDate = htmlUnitCookie.getExpires();
        if (expiryDate == null)
        {
            maxAge = -1;
        }
        else
        {
            maxAge = Math.max(0, (expiryDate.getTime() - System.currentTimeMillis()) / 1_000L);
        }

        // now create the cookie
        final HttpCookie httpCookie = new HttpCookie(htmlUnitCookie.getName(), htmlUnitCookie.getValue());

        httpCookie.setDomain(htmlUnitCookie.getDomain());
        httpCookie.setPath(htmlUnitCookie.getPath());
        httpCookie.setMaxAge(maxAge);
        httpCookie.setSecure(htmlUnitCookie.isSecure());
        httpCookie.setHttpOnly(htmlUnitCookie.isHttpOnly());

        return httpCookie;
    }

    /**
     * Checks if the cookie URI of passed cookie
     *
     * @param cookies
     * @return
     */
    private static boolean matchesUri(final Cookie htmlUnitCookie, final URI uri)
    {
        String cookieDomain = htmlUnitCookie.getDomain();
        final String uriDomain = uri.getHost();

        // System.err.printf("### %s %s\n", cookieDomain, uriDomain);

        // special cases
        if (cookieDomain.equals("localhost.local"))
        {
            cookieDomain = "localhost";
        }

        return uriDomain.endsWith(cookieDomain);
    }

    public static List<HttpCookie> matchPath(final URI uri, final List<HttpCookie> cookies)
    {
        if (cookies == null || cookies.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<HttpCookie> result = new ArrayList<>(4);
        String path = uri.getPath();

        if (path == null || path.trim().isEmpty())
        {
            path = "/";
        }

        for (final HttpCookie cookie : cookies)
        {
            final String cookiePath = cookie.getPath();

            if (cookiePath == null)
            {
                result.add(cookie);
            }
            else
            {
                // RFC 6265, section 5.1.4, path matching algorithm.
                if (path.equals(cookiePath))
                {
                    result.add(cookie);
                }
                else if (path.startsWith(cookiePath))
                {
                    if (cookiePath.endsWith("/") || path.charAt(cookiePath.length()) == '/')
                    {
                        result.add(cookie);
                    }
                }
            }
        }

        return result;
    }
}
