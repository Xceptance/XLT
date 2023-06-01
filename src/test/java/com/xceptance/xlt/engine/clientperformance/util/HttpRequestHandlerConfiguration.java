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
package com.xceptance.xlt.engine.clientperformance.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents the settings which influence the behavior of a {@link HttpRequestHandler}. Typically, you would first
 * configure the expected behavior and afterwards call {@link #buildUrl()} to get the URL to pass to the request
 * handler.
 */
public class HttpRequestHandlerConfiguration
{
    public static int HTTP_PORT = 4711;

    private static int requestId = 1;

    private int serverReadTime = -1;

    private int serverBusyTime = -1;

    private int serverWriteTime = -1;

    private int responseLength = -1;

    private boolean keepAlive = true;

    private boolean gzipEnabled = false;

    private char separatorChar;

    /**
     * Parses the settings from the given URL.
     */
    public void parseUrl(String url)
    {
        // reset all settings
        serverReadTime = -1;
        serverBusyTime = -1;
        serverWriteTime = -1;
        responseLength = -1;
        keepAlive = true;
        gzipEnabled = false;

        // get the query parameters from the URL
        String query;
        try
        {
            query = new URI(url).getQuery();
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException("Failed to parse URI: " + url, e);
        }

        if (query != null)
        {
            // get the parameters from the query
            String[] params = StringUtils.split(query, '&');

            for (String param : params)
            {
                String[] paramParts = StringUtils.split(param, '=');

                int value = Integer.parseInt(paramParts[1]);

                switch (paramParts[0])
                {
                    case "r":
                        serverReadTime = value;
                        break;
                    case "b":
                        serverBusyTime = value;
                        break;
                    case "w":
                        serverWriteTime = value;
                        break;
                    case "l":
                        responseLength = value;
                        break;
                    case "k":
                        keepAlive = (value != 0);
                        break;
                    case "c":
                        gzipEnabled = (value != 0);
                        break;
                }
            }
        }
    }

    /**
     * Builds a URL from the settings and a default base URL.
     */
    public String buildUrl()
    {
        return buildUrl("http://localhost:" + HTTP_PORT + "/");
    }

    /**
     * Builds a URL from the settings and the given base URL.
     */
    public String buildUrl(String baseUrl)
    {
        StringBuilder url = new StringBuilder();

        url.append(baseUrl);

        // append a request ID to avoid browser-side caching
        if (!baseUrl.endsWith("/"))
        {
            url.append('/');
        }
        url.append(requestId++);

        // append the parameters
        separatorChar = '?';

        if (serverReadTime >= 0)
        {
            appendParameter(url, "r", serverReadTime);
        }

        if (serverBusyTime >= 0)
        {
            appendParameter(url, "b", serverBusyTime);
        }

        if (serverWriteTime >= 0)
        {
            appendParameter(url, "w", serverWriteTime);
        }

        if (responseLength >= 0)
        {
            appendParameter(url, "l", responseLength);
        }

        if (keepAlive == false)
        {
            appendParameter(url, "k", 0);
        }

        if (gzipEnabled == true)
        {
            appendParameter(url, "c", 1);
        }

        return url.toString();
    }

    private void appendParameter(StringBuilder url, String name, int value)
    {
        url.append(separatorChar);
        url.append(name).append('=').append(value);

        separatorChar = '&';
    }

    public int getServerReadTime()
    {
        return serverReadTime < 0 ? 0 : serverReadTime;
    }

    public HttpRequestHandlerConfiguration setServerReadTime(int serverReadTime)
    {
        this.serverReadTime = serverReadTime;
        return this;
    }

    public int getServerBusyTime()
    {
        return serverBusyTime < 0 ? 0 : serverBusyTime;
    }

    public HttpRequestHandlerConfiguration setServerBusyTime(int serverBusyTime)
    {
        this.serverBusyTime = serverBusyTime;
        return this;
    }

    public int getServerWriteTime()
    {
        return serverWriteTime < 0 ? 0 : serverWriteTime;
    }

    public HttpRequestHandlerConfiguration setServerWriteTime(int serverWriteTime)
    {
        this.serverWriteTime = serverWriteTime;
        return this;
    }

    public int getResponseLength()
    {
        return responseLength < 0 ? 0 : responseLength;
    }

    public HttpRequestHandlerConfiguration setResponseLength(int responseLength)
    {
        this.responseLength = responseLength;
        return this;
    }

    public boolean isKeepAlive()
    {
        return keepAlive;
    }

    public HttpRequestHandlerConfiguration setKeepAlive(boolean keepAlive)
    {
        this.keepAlive = keepAlive;
        return this;
    }

    public boolean isGzipEnabled()
    {
        return gzipEnabled;
    }

    public HttpRequestHandlerConfiguration setGzipEnabled(boolean gzipEnabled)
    {
        this.gzipEnabled = gzipEnabled;
        return this;
    }
}
