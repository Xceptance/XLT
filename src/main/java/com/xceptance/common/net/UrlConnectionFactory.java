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
package com.xceptance.common.net;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;

import com.xceptance.common.util.ssl.EasySSLUtils;

/**
 * A factory to create URL connections with specific pre-configured properties.
 */
public class UrlConnectionFactory
{
    /**
     * The socket connect timeout [ms].
     */
    private int connectTimeout = -1;

    /**
     * Whether or not invalid/self-signed certificates are accepted in case of an SSL connection.
     */
    private boolean easySsl;

    /**
     * The password (for Basic Authentication).
     */
    private String password;

    /**
     * The socket read timeout [ms].
     */
    private int readTimeout = -1;

    /**
     * The user name (for Basic Authentication).
     */
    private String userName;

    /**
     * Returns the currently configured socket connect timeout.
     * 
     * @return the timeout [ms]
     */
    public int getConnectTimeout()
    {
        return connectTimeout;
    }

    /**
     * Returns the currently configured password used for Basic Authentication.
     * 
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Returns the currently configured socket read timeout.
     * 
     * @return the timeout [ms]
     */
    public int getReadTimeout()
    {
        return readTimeout;
    }

    /**
     * Returns the currently configured user name used for Basic Authentication.
     * 
     * @return the user name
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * Returns whether or not invalid/self-signed certificates are accepted in case of an SSL connection.
     * 
     * @return whether easy mode is enabled
     */
    public boolean isEasySsl()
    {
        return easySsl;
    }

    /**
     * Opens a connection to the given URL with all the current factory settings applied.
     * 
     * @param url
     *            the target URL
     * @return an open connection
     * @throws IOException
     *             if an error occurred when opening the connection
     */
    public URLConnection open(final URL url) throws IOException
    {
        final URLConnection conn = easySsl ? EasySSLUtils.openEasyConnection(url) : url.openConnection();

        if (connectTimeout >= 0)
        {
            conn.setConnectTimeout(connectTimeout);
        }

        if (readTimeout >= 0)
        {
            conn.setReadTimeout(readTimeout);
        }

        if (userName != null && password != null)
        {
            conn.setRequestProperty(HttpHeaderConstants.AUTHORIZATION,
                                    "Basic " + Base64.encodeBase64String((userName + ":" +
                                                                          password).getBytes(StandardCharsets.ISO_8859_1)));
        }

        return conn;
    }

    /**
     * Sets the new socket connect timeout.
     * 
     * @param connectTimeout
     *            the connect timeout [ms]
     * @see URLConnection#setConnectTimeout(int)
     */
    public void setConnectTimeout(final int connectTimeout)
    {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Sets whether or not invalid/self-signed certificates are accepted in case of an SSL connection.
     * 
     * @param easySsl
     *            whether easy mode is enabled
     */
    public void setEasySsl(final boolean easySsl)
    {
        this.easySsl = easySsl;
    }

    /**
     * Sets the new password used for Basic Authentication. If the password is <code>null</code>, Basic Authentication
     * support will be turned off.
     * 
     * @param password
     *            the new password
     */
    public void setPassword(final String password)
    {
        this.password = password;
    }

    /**
     * Sets the new socket read timeout.
     * 
     * @param readTimeout
     *            the read timeout [ms]
     * @see URLConnection#setReadTimeout(int)
     */
    public void setReadTimeout(final int readTimeout)
    {
        this.readTimeout = readTimeout;
    }

    /**
     * Sets the new user name used for Basic Authentication. If the user name is <code>null</code>, Basic Authentication
     * support will be turned off.
     * 
     * @param userName
     *            the new user name
     */
    public void setUserName(final String userName)
    {
        this.userName = userName;
    }
}
