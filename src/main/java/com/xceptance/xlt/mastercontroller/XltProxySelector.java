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
package com.xceptance.xlt.mastercontroller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

/**
 * Use HTTPS proxy even if there is a localhost connection.
 * 
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 * @see http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html
 */
public class XltProxySelector extends ProxySelector
{
    // The proxy address
    private final SocketAddress sa;

    // The proxy
    private final Proxy proxy;

    // The host patterns for which the proxy should be used
    private final Set<Pattern> hostIncludePatterns;

    // The host patterns for which the proxy should be bypassed
    private final Set<Pattern> hostExcludePatterns;

    /**
     * Constructor.
     *
     * @param proxyHost
     *            the proxy host
     * @param proxyPort
     *            the proxy port
     * @param hostIncludes
     *            the hostname patterns for which the proxy should be used (format: a series of regular expressions
     *            delimited by space, comma or semicolon)
     * @param hostExcludes
     *            the hostname patterns for which the proxy should be bypassed (format: a series of regular expressions
     *            delimited by space, comma or semicolon)
     */
    public XltProxySelector(final String proxyHost, final String proxyPort, final String hostIncludes, final String hostExcludes)
    {
        // Assert proxy host is not blank.
        if (StringUtils.isBlank(proxyHost))
        {
            throw new IllegalArgumentException("Proxy host must not be NULL or empty.");
        }

        // Convert proxy port to int. If not set, fall back to port 80.
        final int port;
        try
        {
            port = StringUtils.isBlank(proxyPort) ? 80 : Integer.valueOf(proxyPort.trim());
        }
        catch (final NumberFormatException e)
        {
            throw new IllegalArgumentException("Proxy port must be a number, but was: " + proxyPort);
        }

        // Populate the set HTTPS proxy
        sa = new InetSocketAddress(proxyHost.trim(), port);
        proxy = new Proxy(Proxy.Type.HTTP, sa);

        // Read the include and exclude patterns for target hostnames
        hostIncludePatterns = readPatterns(hostIncludes, "Proxy hostname include string");
        hostExcludePatterns = readPatterns(hostExcludes, "Proxy hostname exclude string");
    }

    // This is the method that the handlers will call.
    /**
     * {@inheritDoc}
     */
    @Override
    public java.util.List<Proxy> select(final URI uri)
    {
        // Let's stick to the specs.
        if (uri == null)
        {
            throw new IllegalArgumentException("URI can't be null.");
        }

        // If it's an HTTPS URL, then we use our own list.
        final String protocol = uri.getScheme();
        if ("https".equalsIgnoreCase(protocol))
        {
            // Check if proxy should be used or bypassed for this host.
            if (useProxyForHost(uri.getHost()))
            {
                final ArrayList<Proxy> l = new ArrayList<>();
                l.add(proxy);
                return l;
            }
        }

        // Host is not HTTPS or proxy should be bypassed, so we don't return the proxy.
        final ArrayList<Proxy> l = new ArrayList<>();
        l.add(Proxy.NO_PROXY);
        return l;
    }

    // Method called by the handlers when it failed to connect to one of the proxies returned by select().
    /**
     * {@inheritDoc}
     */
    @Override
    public void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe)
    {
        // Let's stick to the specs again.
        if (uri == null || sa == null || ioe == null)
        {
            throw new IllegalArgumentException("Arguments can't be null.");
        }
    }

    /**
     * Convert a given string of delimited hostname regex patterns into a set of compiled patterns.
     *
     * @param patternString
     *            string containing no, one or multiple hostname regex patterns (delimited by space, comma or semicolon)
     * @param patternName
     *            name for the given pattern used in error messages
     * @return a set containing all provided patterns in compiled form; returns an empty set if patternString is blank
     */
    private Set<Pattern> readPatterns(final String patternString, final String patternName)
    {
        final Set<Pattern> patterns = new HashSet<>();

        if (StringUtils.isNotBlank(patternString))
        {
            try
            {
                for (final String regex : StringUtils.split(patternString, " ,;"))
                {
                    patterns.add(Pattern.compile(regex));
                }
            }
            catch (final PatternSyntaxException e)
            {
                throw new IllegalArgumentException(patternName + " contains invalid regex patterns: " + patternString, e);
            }
        }

        return patterns;
    }

    /**
     * Check if the proxy should be used or bypassed for the given host by evaluating the hostname include and exclude
     * patterns.
     *
     * @param host
     *            the hostname
     * @return "true" if the proxy should be used for the given host, "false" if the proxy should be bypassed
     */
    private boolean useProxyForHost(final String host)
    {
        // bypass host if it matches any exclude pattern
        for (final Pattern exclude : hostExcludePatterns)
        {
            if (exclude.matcher(host).find())
            {
                return false;
            }
        }

        // if no include patterns are defined, we include everything
        if (hostIncludePatterns.isEmpty())
        {
            return true;
        }

        // if include patterns are defined, the host must match at least one of them
        for (final Pattern include : hostIncludePatterns)
        {
            if (include.matcher(host).find())
            {
                return true;
            }
        }

        // if none of the include patterns matched, bypass the proxy
        return false;
    }
}
