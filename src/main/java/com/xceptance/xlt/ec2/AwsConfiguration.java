/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.ec2;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.Protocol;
import com.xceptance.common.util.AbstractConfiguration;
import com.xceptance.xlt.engine.XltExecutionContext;

/**
 * The {@link AwsConfiguration} is the central place for AWS configuration values.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class AwsConfiguration extends AbstractConfiguration
{
    /**
     * AWS property prefix.
     */
    private static final String PROP_PREFIX = "aws.";

    /**
     * AWS property prefix for proxy settings.
     */
    private static final String PROP_PROXY_PREFIX = PROP_PREFIX + "proxy.";

    /**
     * AWS property name for the access key.
     */
    private static final String PROP_ACCESS_KEY = PROP_PREFIX + "accessKey";

    /**
     * AWS property name for the secret key.
     */
    private static final String PROP_SECRET_KEY = PROP_PREFIX + "secretKey";

    private static final String PROP_SSH_KEY = PROP_PREFIX + "keypair.";

    /**
     * AWS property name for the protocol.
     */
    private static final String PROP_PROTOCOL = PROP_PREFIX + "protocol";

    /**
     * AWS property name for the HTTP proxy host.
     */
    private static final String PROP_PROXY_HOST = PROP_PROXY_PREFIX + "host";

    /**
     * AWS property name for the HTTP proxy port.
     */
    private static final String PROP_PROXY_PORT = PROP_PROXY_PREFIX + "port";

    /**
     * AWS property name for the HTTP proxy user name.
     */
    private static final String PROP_PROXY_USER_NAME = PROP_PROXY_PREFIX + "userName";

    /**
     * AWS property name for the HTTP proxy password.
     */
    private static final String PROP_PROXY_PASSWORD = PROP_PROXY_PREFIX + "password";

    /**
     * AWS property name for the instance connect timeout.
     */
    private static final String PROP_INSTANCE_CONNECT_TIMEOUT = PROP_PREFIX + "instanceConnectTimeout";

    /**
     * AWS property name for the instance pricing URL.
     */
    private static final String PROP_INSTANCE_PRICING_URL = PROP_PREFIX + "instancePricingUrl";

    /**
     * The AWS access key.
     */
    private final String accessKey;

    /**
     * The protocol.
     */
    private final Protocol protocol;

    /**
     * The HTTP proxy host.
     */
    private final String proxyHost;

    /**
     * The HTTP proxy password.
     */
    private final String proxyPassword;

    /**
     * The HTTP proxy port.
     */
    private final int proxyPort;

    /**
     * The HTTP proxy user name.
     */
    private final String proxyUserName;

    /**
     * The AWS secret key.
     */
    private final String secretKey;

    /**
     * The timeout to wait for a specified instance state.
     */
    private final int instanceConnectTimeout;

    /**
     * The URL used to download the instance pricing from AWS.
     */
    private String instancePricingUrl;

    /**
     * Creates a new {@link AwsConfiguration} object.
     *
     * @throws RuntimeException
     *             if an error occurs
     */
    public AwsConfiguration()
    {
        this(null, null);
    }

    /**
     * Creates a new {@link AwsConfiguration} object. The given credential s override the configured credentials.
     *
     * @throws RuntimeException
     *             if an error occurs
     */
    public AwsConfiguration(final String accessKey, final String secretKey)
    {
        try
        {
            final File configDirectory = XltExecutionContext.getCurrent().getXltConfigDir();
            final File propFile = new File(configDirectory, "ec2_admin.properties");

            loadProperties(propFile);

            this.accessKey = StringUtils.isNotBlank(accessKey) ? accessKey : getStringProperty(PROP_ACCESS_KEY);
            this.secretKey = StringUtils.isNotBlank(secretKey) ? secretKey : getStringProperty(PROP_SECRET_KEY);

            protocol = Protocol.valueOf(getStringProperty(PROP_PROTOCOL, "https").toUpperCase());
            proxyHost = getStringProperty(PROP_PROXY_HOST, null);
            proxyPort = getIntProperty(PROP_PROXY_PORT, 8888);
            proxyUserName = getStringProperty(PROP_PROXY_USER_NAME, null);
            proxyPassword = getStringProperty(PROP_PROXY_PASSWORD, null);
            instanceConnectTimeout = getIntProperty(PROP_INSTANCE_CONNECT_TIMEOUT, -1);

            instancePricingUrl = getStringProperty(PROP_INSTANCE_PRICING_URL, "https://a0.awsstatic.com/pricing/1/ec2/linux-od.min.js");
        }
        catch (final IOException e)
        {
            throw new RuntimeException("Failed to read AWS configuration", e);
        }
    }

    /**
     * Returns the value of the 'accessKey' attribute.
     *
     * @return the value of accessKey
     */
    public String getAccessKey()
    {
        return accessKey;
    }

    /**
     * Returns the value of the 'protocol' attribute.
     *
     * @return the value of protocolxyHost
     */
    public Protocol getProtocol()
    {
        return protocol;
    }

    /**
     * Returns the value of the 'proxyHost' attribute.
     *
     * @return the value of proxyHost
     */
    public String getProxyHost()
    {
        return proxyHost;
    }

    /**
     * Returns the value of the 'proxyPassword' attribute.
     *
     * @return the value of proxyPassword
     */
    public String getProxyPassword()
    {
        return proxyPassword;
    }

    /**
     * Returns the value of the 'proxyPort' attribute.
     *
     * @return the value of proxyPort
     */
    public int getProxyPort()
    {
        return proxyPort;
    }

    /**
     * Returns the value of the 'proxyUserName' attribute.
     *
     * @return the value of proxyUserName
     */
    public String getProxyUserName()
    {
        return proxyUserName;
    }

    /**
     * Returns the value of the 'secretKey' attribute.
     *
     * @return the value of secretKey
     */
    public String getSecretKey()
    {
        return secretKey;
    }

    /**
     * Returns the specified instance connect timeout in milliseconds.
     *
     * @return the value of 'instanceConnectTimeout'
     */
    public int getInstanceConnectTimeout()
    {
        return instanceConnectTimeout;
    }

    /**
     * Returns the URL used to download the instance pricing from AWS.
     *
     * @return the instance pricing URL as string
     */
    public String getInstancePricingUrl()
    {
        return instancePricingUrl;
    }

    /**
     * Get the configured key pair name for given region
     *
     * @param regionName
     *            region name
     * @return key par name or <code>null</code> if no such key is present
     */
    public String getSshKey(final String regionName)
    {
        return getStringProperty(PROP_SSH_KEY + regionName, null);
    }
}
