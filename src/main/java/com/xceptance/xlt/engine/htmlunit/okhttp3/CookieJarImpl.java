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
package com.xceptance.xlt.engine.htmlunit.okhttp3;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.util.Cookie;

import okhttp3.Cookie.Builder;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * An OkHttp {@link CookieJar} that is backed by HtmlUnit's {@link CookieManager}.
 */
class CookieJarImpl implements CookieJar
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
    public CookieJarImpl(final CookieManager cookieManager)
    {
        this.cookieManager = cookieManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveFromResponse(final HttpUrl url, final List<okhttp3.Cookie> okHttpCookies)
    {
        for (final okhttp3.Cookie okHttpCookie : okHttpCookies)
        {
            cookieManager.addCookie(toHtmlUnitCookie(okHttpCookie));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<okhttp3.Cookie> loadForRequest(final HttpUrl url)
    {
        cookieManager.clearExpired(new Date());

        return cookieManager.getCookies().stream().filter(htmlUnitCookie -> matchesUri(htmlUnitCookie, url))
                            .map(CookieJarImpl::toOkHttpCookie).collect(Collectors.toList());
    }

    /**
     * Checks if the cookie is applicable for the URL.
     *
     * @param htmlUnitCookie
     *            the cookie
     * @param url
     *            the URL
     * @return whether or not the cookie is to be sent back to the URL
     */
    private static boolean matchesUri(final Cookie htmlUnitCookie, final HttpUrl url)
    {
        final String host = url.host();
        String cookieDomain = htmlUnitCookie.getDomain();

        // special cases
        if (cookieDomain.equals("localhost.local"))
        {
            cookieDomain = "localhost";
        }

        return host.endsWith(cookieDomain);
    }

    /**
     * Converts the passed OkHttp cookie instance to its HtmlUnit equivalent.
     *
     * @param okHttpCookie
     *            the source cookie
     * @return the converted cookie
     */
    private static Cookie toHtmlUnitCookie(final okhttp3.Cookie okHttpCookie)
    {
        return new Cookie(okHttpCookie.domain(), okHttpCookie.name(), okHttpCookie.value(), okHttpCookie.path(),
                          new Date(okHttpCookie.expiresAt()), okHttpCookie.secure(), okHttpCookie.httpOnly());
    }

    /**
     * Converts the passed HtmlUnit cookie instance to its OkHttp equivalent.
     *
     * @param htmlUnitCookie
     *            the source cookie
     * @return the converted cookie
     */
    private static okhttp3.Cookie toOkHttpCookie(final Cookie htmlUnitCookie)
    {
        final Builder builder = new okhttp3.Cookie.Builder();

        // OkHttp does not like cookie domains starting with "."
        final String domain = StringUtils.stripStart(htmlUnitCookie.getDomain(), ".");

        builder.name(htmlUnitCookie.getName()).value(htmlUnitCookie.getValue()).domain(domain).path(htmlUnitCookie.getPath())
               .expiresAt(htmlUnitCookie.getExpires().getTime());

        if (htmlUnitCookie.isSecure())
        {
            builder.secure();
        }

        if (htmlUnitCookie.isHttpOnly())
        {
            builder.httpOnly();
        }

        return builder.build();
    }
}
