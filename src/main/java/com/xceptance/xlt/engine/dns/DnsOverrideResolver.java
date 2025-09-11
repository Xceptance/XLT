/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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

import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.common.util.ParseUtils;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.api.util.XltProperties;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link HostNameResolver} implementation that checks if IP overrides for the host name have been configured in the
 * properties, or (if no overrides were found) revolves the host name using a given fallback resolver.
 */
public class DnsOverrideResolver implements HostNameResolver
{
    /**
     * Prefix for host name overrides in the properties.
     */
    static final String PROP_DNS_OVERRIDE_PREFIX = XltDnsResolver.PROP_PREFIX_DNS + "override";

    /**
     * Host name resolver to use if no matching overrides were configured.
     */
    private final HostNameResolver fallbackResolver;

    /**
     * Stores the host name overrides read from the properties. The key is the host name to override, and the value is
     * an array of InetAddress objects representing the IP addresses to resolve to.
     */
    private final Map<String, InetAddress[]> addressOverrides;

    /**
     * Creates a new instance with the given fallback resolver and reads the host name overrides from the properties.
     *
     * @param fallbackResolver
     *            host name resolver to fall back to if no overrides were specified for a host name
     */
    public DnsOverrideResolver(final HostNameResolver fallbackResolver)
    {
        ParameterCheckUtils.isNotNull(fallbackResolver, "fallbackResolver");

        this.fallbackResolver = fallbackResolver;
        this.addressOverrides = readHostNameOverrides();
    }

    /**
     * Determine all IP addresses for the given host name. This will first look for an override from the properties; if
     * no override configured the fallback resolver will be used to get the IPs instead.
     *
     * @param host
     *            the host name to look up
     * @return the IPs for the given host, taken either from the overrides defined in the properties or from the
     *         fallback resolver
     * @throws UnknownHostException
     *             if no override was configured for the host name and the fallback resolver couldn't resolve the host
     *             name
     */
    @Override
    public InetAddress[] resolve(final String host) throws UnknownHostException
    {
        final InetAddress[] overrideIps = addressOverrides.get(host);
        return overrideIps != null ? overrideIps : fallbackResolver.resolve(host);
    }

    /**
     * Get the host name overrides from the test suite properties.
     *
     * @return a map containing the host name overrides; each entry maps a host name to an array of IP addresses
     */
    static Map<String, InetAddress[]> readHostNameOverrides()
    {
        final Map<String, InetAddress[]> overrides = new HashMap<>();

        XltProperties.getInstance().getPropertiesForKey(PROP_DNS_OVERRIDE_PREFIX).forEach((hostName, ipString) -> {
            // we ignore override properties if the host name is blank
            if (StringUtils.isNotBlank(hostName))
            {
                try
                {
                    overrides.put(hostName, ParseUtils.parseIpAddresses(ipString));
                }
                catch (final ParseException e)
                {
                    throw new XltException("Failed to parse IP override '" + ipString + "' for host '" + hostName + "'.", e);
                }
            }
        });

        return overrides;
    }
}
