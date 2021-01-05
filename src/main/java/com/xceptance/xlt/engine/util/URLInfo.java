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

/**
 * Immutable URL information object.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class URLInfo
{
    private final String protocol;

    private final String userInfo;

    private final String host;

    private final String path;

    private final int port;

    private final String query;

    private final String fragment;

    private URLInfo(final String protocol, final String userInfo, final String host, final int port, final String path, final String query,
                    final String fragment)
    {
        this.protocol = protocol;
        this.userInfo = userInfo;
        this.host = host;
        this.port = port;
        this.path = path;
        this.query = query;
        this.fragment = fragment;
    }

    /**
     * @return the protocol
     */
    public String getProtocol()
    {
        return protocol;
    }

    /**
     * @return the userInfo
     */
    public String getUserInfo()
    {
        return userInfo;
    }

    /**
     * @return the host
     */
    public String getHost()
    {
        return host;
    }

    /**
     * @return the path
     */
    public String getPath()
    {
        return path;
    }

    /**
     * @return the port
     */
    public int getPort()
    {
        return port;
    }

    /**
     * @return the query
     */
    public String getQuery()
    {
        return query;
    }

    /**
     * @return the fragment
     */
    public String getFragment()
    {
        return fragment;
    }

    /**
     * Creates and returns a new URLInfo builder.
     * 
     * @return URLInfo builder
     */
    public static URLInfoBuilder builder()
    {
        return new URLInfoBuilder();
    }

    /**
     * URLInfo builder.
     */
    public static class URLInfoBuilder
    {
        private String proto;

        private String host;

        private String userInfo;

        private String path;

        private String query;

        private String fragment;

        private int port = -1;

        /**
         * Sets the URL protocol.
         * 
         * @param protocol
         *            the URL protocol.
         */
        public URLInfoBuilder proto(final String protocol)
        {
            proto = protocol;
            return this;
        }

        /**
         * Sets the URL host.
         * 
         * @param host
         *            the URL host
         */
        public URLInfoBuilder host(final String host)
        {
            this.host = host;
            return this;
        }

        /**
         * Sets the URL user information using the given user and password.
         * 
         * @param user
         *            the username
         * @param password
         *            the password
         */
        public URLInfoBuilder userInfo(final String user, final String password)
        {
            userInfo = user + ":" + password;
            return this;
        }

        /**
         * Sets the URL user information-
         * 
         * @param userInfo
         *            the URL user information
         */
        public URLInfoBuilder userInfo(final String userInfo)
        {
            this.userInfo = userInfo;
            return this;
        }

        /**
         * Sets the URL port.
         * 
         * @param port
         *            the URL port
         */
        public URLInfoBuilder port(final int port)
        {
            this.port = port;
            return this;
        }

        /**
         * Sets the URL path.
         * 
         * @param path
         *            the URL path
         */
        public URLInfoBuilder path(final String path)
        {
            this.path = path;
            return this;
        }

        /**
         * Sets the URL query.
         * 
         * @param query
         *            the URL query
         */
        public URLInfoBuilder query(final String query)
        {
            this.query = query;
            return this;
        }

        /**
         * Sets the URL fragment.
         * 
         * @param fragment
         *            the URL fragment
         */
        public URLInfoBuilder fragment(final String fragment)
        {
            this.fragment = fragment;
            return this;
        }

        /**
         * Builds the final URLInfo object.
         * 
         * @return constructed URLInfo
         */
        public URLInfo build()
        {
            return new URLInfo(proto, userInfo, host, port, path, query, fragment);
        }
    }
}
