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
package com.xceptance.xlt.api.engine;

import java.net.URL;
import java.util.regex.Pattern;

import com.gargoylesoftware.htmlunit.WebRequest;

/**
 * Request filter.<br>
 * Used to get filtered data from {@link NetworkDataManager}. Currently, filtering is restricted to the request's URL.
 * 
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class RequestFilter
{
    private String protocol = null;

    private String hostPattern = null;

    private int port = -1;

    private String pathPattern = null;

    private String queryPattern = null;

    private String urlPattern;

    /**
     * Request protocol of the wanted request
     * 
     * @return request protocol of the wanted request
     */
    public String getProtocol()
    {
        return protocol;
    }

    /**
     * Request protocol (<code>http</code>, <code>https</code>, ...)
     * 
     * @param protocol
     *            the wanted request's protocol or <code>null</code> to disable protocol filtering
     */
    public void setProtocol(final String protocol)
    {
        this.protocol = protocol;
    }

    /**
     * Regular expression to match the host of the wanted request
     * 
     * @return regular expression to match the host or <code>null</code> if no host filter is set
     */
    public String getHostPattern()
    {
        return hostPattern;
    }

    /**
     * Regular expression the matches the wanted request's host
     * 
     * @param regex
     *            regular expression the matches the wanted request's host or <code>null</code> to disable host
     *            filtering
     */
    public void setHostPattern(final String regex)
    {
        hostPattern = regex;
    }

    /**
     * Port number
     * 
     * @return port number of the wanted request or <code>-1</code> if no port filter is set
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Port number of the wanted request
     * 
     * @param port
     *            port number of the wanted request or <code>-1</code> to disable port filtering
     */
    public void setPort(final int port)
    {
        this.port = port;
    }

    /**
     * Path pattern
     * 
     * @return regular expression that matches the wanted request's path or <code>null</code> if path filtering is
     *         disabled
     */
    public String getPathPattern()
    {
        return pathPattern;
    }

    /**
     * Path pattern
     * 
     * @param regex
     *            regular expression that matches the wanted request's path or <code>null</code> to disable path
     *            filtering
     */
    public void setPathPattern(final String regex)
    {
        pathPattern = regex;
    }

    /**
     * Query Pattern
     * 
     * @return regular expression that matches the wanted request's query string or <code>null</code> if query filtering
     *         is disabled
     */
    public String getQueryPattern()
    {
        return queryPattern;
    }

    /**
     * Query pattern
     * 
     * @param regex
     *            regular expression that matches the wanted request's query string or <code>null</code> if query
     *            filtering is disabled
     */
    public void setQueryPattern(final String regex)
    {
        queryPattern = regex;
    }

    /**
     * URL Pattern If the URL pattern is set this overrides all other filters.
     * 
     * @return regular expression that matches the wanted request's URL string or <code>null</code> if URL filtering is
     *         disabled
     */
    public String getUrlPattern()
    {
        return urlPattern;
    }

    /**
     * URL Pattern If the URL pattern is set this overrides all other filters.
     * 
     * @param regex
     *            regular expression that matches the wanted request's URL string or <code>null</code> to disable URL
     *            filtering
     */
    public void setUrlPattern(final String regex)
    {
        urlPattern = regex;
    }

    /**
     * Returns whether or not the this filter accepts the given request.
     * 
     * @param request
     *            the request to be checked
     * @return <code>true</code> if this filter accepts the given request, <code>false</code> otherwise
     */
    public boolean accepts(final WebRequest request)
    {
        if (request == null)
        {
            return false;
        }

        /*
         * Check if request URL matches.
         */

        final URL url = request.getUrl();
        if (urlPattern != null)
        {
            return Pattern.matches(urlPattern, url.toString());
        }

        // check protocol, host, port, path and query consecutively
        if (protocol != null && !url.getProtocol().equalsIgnoreCase(protocol))
        {
            return false;
        }
        if (hostPattern != null && !Pattern.matches(hostPattern, url.getHost()))
        {
            return false;
        }
        if (port > 0 && port != url.getPort())
        {
            return false;
        }
        if (pathPattern != null && !Pattern.matches(pathPattern, url.getPath()))
        {
            return false;
        }
        if (queryPattern != null && !Pattern.matches(queryPattern, url.getQuery()))
        {
            return false;
        }
        return true;
    }
}
