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
package com.xceptance.xlt.engine.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URLEncodedUtils;

import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.common.XltConstants;

/**
 * URL utility methods used in XLT engine.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public final class UrlUtils
{
    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private UrlUtils()
    {
    }

    /**
     * Rewrites the given URL string.
     * 
     * @param urlString
     *            the URL string to rewrite
     * @param urlOverride
     *            the URL override parameter
     * @return rewritten URL
     * @throws MalformedURLException
     */
    public static URL rewriteUrl(final String urlString, final URLInfo urlOverride) throws MalformedURLException
    {
        URLInfo urlInfo = null;
        try
        {
            final URL u = new URL(urlString);
            urlInfo = URLInfo.builder().proto(u.getProtocol()).userInfo(u.getUserInfo()).host(u.getHost()).port(u.getPort())
                             .path(u.getPath()).query(u.getQuery()).fragment(u.getRef()).build();
        }
        catch (final MalformedURLException mue)
        {
            urlInfo = parseUrlString(urlString);
        }

        final String proto = StringUtils.isEmpty(urlOverride.getProtocol()) ? urlInfo.getProtocol() : urlOverride.getProtocol();
        final String uInfo = StringUtils.isEmpty(urlOverride.getUserInfo()) ? urlInfo.getUserInfo() : urlOverride.getUserInfo();
        final String h = StringUtils.isEmpty(urlOverride.getHost()) ? urlInfo.getHost() : urlOverride.getHost();
        final int p = urlOverride.getPort() < 0 ? urlInfo.getPort() : urlOverride.getPort();
        final String pa = StringUtils.isEmpty(urlOverride.getPath()) ? urlInfo.getPath() : urlOverride.getPath();
        final String q = StringUtils.isEmpty(urlOverride.getQuery()) ? urlInfo.getQuery() : urlOverride.getQuery();

        final String frag = StringUtils.isEmpty(urlOverride.getFragment()) ? urlInfo.getFragment() : urlOverride.getFragment();

        // rebuild the URL
        final StringBuilder sb = new StringBuilder();
        sb.append(proto).append("://");
        if (uInfo != null)
        {
            sb.append(uInfo).append('@');
        }
        sb.append(h);
        if (p > 0)
        {
            sb.append(':').append(p);
        }
        /* The path can not be null due to implementation of url.getPath() */
        sb.append(pa);
        if (q != null)
        {
            sb.append('?').append(q);
        }
        if (frag != null)
        {
            sb.append('#').append(frag);
        }

        return new URL(sb.toString());
    }

    /**
     * Parses a URL string into pieces.
     * 
     * @param urlString
     *            the input URL
     * @return a {@link URLInfo} object with the details
     */
    public static URLInfo parseUrlString(final String urlString)
    {
        if (StringUtils.isEmpty(urlString))
        {
            return null;
        }

        final String protocol;
        final String userInfo;
        final String host;
        final String port;
        final String path;
        final String query;
        final String fragment;

        final String authorityPathQueryFragment;
        final String authorityPathQuery;
        final String authorityPath;
        final String authority;
        final String hostPort;

        String[] parts = split(urlString, "://", false);
        protocol = parts[0];
        authorityPathQueryFragment = parts[1];

        parts = split(authorityPathQueryFragment, "#", true);
        authorityPathQuery = parts[0];
        fragment = parts[1];

        parts = split(authorityPathQuery, "?", true);
        authorityPath = parts[0];
        query = parts[1];

        parts = split(authorityPath, "/", true);
        authority = parts[0];
        path = (parts[1] == null) ? StringUtils.EMPTY : "/" + parts[1];

        parts = split(authority, "@", false);
        userInfo = parts[0];
        hostPort = parts[1];

        parts = split(hostPort, ":", true);
        host = parts[0];
        port = parts[1];

        int p = -1;
        if (StringUtils.isNotBlank(port) && RegExUtils.isMatching(port, "\\d+"))
        {
            p = Integer.parseInt(port);
        }

        return URLInfo.builder().proto(protocol).userInfo(userInfo).host(host).port(p).path(path).query(query).fragment(fragment).build();
    }

    /**
     * Splits the given string into two parts at the first occurrence of the separator string. If the separator is not
     * found, the input string will be returned as either the first or the second string in the tuple, while the other
     * string will be <code>null</code>.
     * 
     * @param s
     *            the input string
     * @param sep
     *            the separator string
     * @param returnAsFirst
     *            whether the input string is to be returned as the first (<code>true</code>) or the second (
     *            <code>false</code>) string in the tuple in case the separator was not found
     * @return the tuple of strings
     */
    private static String[] split(final String s, final String sep, final boolean returnAsFirst)
    {
        final String[] parts = new String[2];

        final int i = s.indexOf(sep);
        if (i == -1)
        {
            if (returnAsFirst)
            {
                parts[0] = s;
                parts[1] = null;
            }
            else
            {
                parts[0] = null;
                parts[1] = s;
            }
        }
        else
        {
            parts[0] = s.substring(0, i);
            parts[1] = s.substring(i + sep.length());
        }

        return parts;
    }

    /**
     * Convert a list of name value pairs into an URL encoded parameter string. </br>
     * </br>
     * <b>For example:</b>
     * 
     * <pre>
     * List&ltNameValuePair&gt parameters = new ArrayList&lt&gt();
     * parameters.add(new NameValuePair("foo", "1"));
     * parameters.add(new NameValuePair("bar", "2"));
     * 
     * String result = getUrlEncodedParameters(parameters);
     * 
     * Where <b>result</b> will be <b>"foo=1&bar=2"</b>
     * </pre>
     * 
     * @param parameters
     *            the list of name value pairs to convert
     * @return the URL encoded parameter string
     */
    public static String getUrlEncodedParameters(final List<NameValuePair> parameters)
    {
        final List<org.apache.http.NameValuePair> httpClientPairs = NameValuePair.toHttpClient(parameters);
        return URLEncodedUtils.format(httpClientPairs, XltConstants.UTF8_ENCODING);
    }

    /**
     * Removes the user-info part from the given URL string.
     * 
     * @param url
     *            the URL string
     * @return given URL string without user-info part
     */
    public static String removeUserInfo(final String url)
    {
        final URLInfo info = StringUtils.isNotBlank(url) ? parseUrlString(url) : null;
        if (info != null)
        {
            final StringBuilder sb = new StringBuilder();
            sb.append(info.getProtocol()).append("://").append(info.getHost());
            if (info.getPort() > 0)
            {
                sb.append(':').append(info.getPort());
            }
            sb.append(info.getPath());
            if (info.getQuery() != null)
            {
                sb.append('?').append(info.getQuery());
            }
            if (info.getFragment() != null)
            {
                sb.append('#').append(info.getFragment());
            }
            return sb.toString();
        }

        return null;
    }

    /**
     * Removes the user-info part from the given URL.
     * 
     * @param url
     *            the URL
     * @return given URL as string without user-info part
     */
    public static String removeUserInfo(final URL url)
    {
        if (url != null)
        {
            final StringBuilder sb = new StringBuilder();
            sb.append(url.getProtocol()).append("://").append(url.getHost());
            if (url.getPort() > 0)
            {
                sb.append(':').append(url.getPort());
            }
            sb.append(url.getPath());
            if (url.getQuery() != null)
            {
                sb.append('?').append(url.getQuery());
            }
            if (url.getRef() != null)
            {
                sb.append('#').append(url.getRef());
            }
            return sb.toString();
        }

        return null;
    }

}
