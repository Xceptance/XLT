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
package com.xceptance.xlt.engine.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Security;

import com.xceptance.xlt.api.util.XltProperties;

/**
 * A {@link HostNameResolver} implementation that uses the Java platform to resolve host names. The only value-added
 * feature is that the caching time of the platform resolver can easily be adjusted by configuration.
 */
class PlatformHostNameResolver implements HostNameResolver
{
    static final String PROVIDER_NAME = "platform";

    private static final String PROP_PREFIX = XltDnsResolver.PROP_PREFIX_DNS_PROVIDERS + PROVIDER_NAME + ".";

    static
    {
        /*
         * Adjust the lifetime of entries in Java's global address resolution cache.
         */
        final XltProperties props = XltProperties.getInstance();

        final int duration = props.getProperty(PROP_PREFIX + "cache.duration", 30);

        Security.setProperty("networkaddress.cache.ttl", String.valueOf(duration));
        // Security.setProperty("networkaddress.cache.negative.ttl", "10");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InetAddress[] resolve(final String host) throws UnknownHostException
    {
        return InetAddress.getAllByName(host);
    }
}
