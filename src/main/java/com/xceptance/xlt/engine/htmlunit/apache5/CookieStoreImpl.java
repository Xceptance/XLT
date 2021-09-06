/*
 * Copyright (c) 2002-2021 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.engine.htmlunit.apache5;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;

import com.gargoylesoftware.htmlunit.CookieManager;

/**
 */
class CookieStoreImpl implements CookieStore
{
    private static final String HTTP_ONLY_ATTR = "httponly";

    private CookieManager cookieManager;

    /**
     * Constructor.
     *
     * @param manager
     *            the CookieManager
     */
    public CookieStoreImpl(final CookieManager manager)
    {
        cookieManager = manager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addCookie(final Cookie httpClientCookie)
    {
        cookieManager.addCookie(toHtmlUnitCookie(httpClientCookie));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized List<Cookie> getCookies()
    {
        List<Cookie> httpClientCookies = new ArrayList<>();

        for (com.gargoylesoftware.htmlunit.util.Cookie htmlUnitCookie : cookieManager.getCookies())
        {
            httpClientCookies.add(toHttpClientCookie(htmlUnitCookie));
        }

        return httpClientCookies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean clearExpired(final Date date)
    {
        return cookieManager.clearExpired(date);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void clear()
    {
        cookieManager.clearCookies();
    }

    private static com.gargoylesoftware.htmlunit.util.Cookie toHtmlUnitCookie(Cookie httpClientCookie)
    {
        boolean httpOnly = httpClientCookie.getAttribute(HTTP_ONLY_ATTR) != null;

        return new com.gargoylesoftware.htmlunit.util.Cookie(httpClientCookie.getDomain(), httpClientCookie.getName(),
                                                             httpClientCookie.getValue(), httpClientCookie.getPath(),
                                                             httpClientCookie.getExpiryDate(), httpClientCookie.isSecure(), httpOnly);
    }

    private static Cookie toHttpClientCookie(com.gargoylesoftware.htmlunit.util.Cookie htmlUnitCookie)
    {
        BasicClientCookie httpClientCookie = new BasicClientCookie(htmlUnitCookie.getName(), htmlUnitCookie.getValue());

        httpClientCookie.setDomain(htmlUnitCookie.getDomain());
        // domain has to be set as attribute also
        httpClientCookie.setAttribute(Cookie.DOMAIN_ATTR, htmlUnitCookie.getDomain());

        httpClientCookie.setPath(htmlUnitCookie.getPath());
        httpClientCookie.setExpiryDate(htmlUnitCookie.getExpires());
        httpClientCookie.setSecure(htmlUnitCookie.isSecure());

        if (htmlUnitCookie.isHttpOnly())
        {
            httpClientCookie.setAttribute(HTTP_ONLY_ATTR, "true");
        }

        return httpClientCookie;
    }
}
