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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.engine.RequestExecutionContext;
import com.xceptance.xlt.engine.socket.SocketMonitor;

/**
 * A meta host name resolver that may use alternative {@link HostNameResolver} implementations for the actual work. This
 * class adds a layer on top of the underlying resolver that allows to cache address resolutions for the current virtual
 * user and to measure address resolution time. In case a host name is resolved to multiple addresses, the layer may
 * also shuffle the list of addresses for a better load distribution.
 *
 * @see PlatformHostNameResolver
 * @see DnsJavaHostNameResolver
 */
public class XltDnsResolver implements HostNameResolver
{
    static final String PROP_PREFIX_DNS = "xlt.dns.";

    static final String PROP_PREFIX_DNS_PROVIDERS = PROP_PREFIX_DNS + "providers.";

    private static final String PROP_PROVIDER = PROP_PREFIX_DNS + "provider";

    private static final String PROP_SHUFFLE_ADDRESSES = PROP_PREFIX_DNS + "shuffleAddresses";

    static final String PROP_PICK_ONE_ADDRESS_RANDOMLY = PROP_PREFIX_DNS + "pickOneAddressRandomly";

    static final String PROP_CACHE_ADDRESSES = PROP_PREFIX_DNS + "cacheAddresses";

    static final String PROP_RECORD_ADDRESSES = PROP_PREFIX_DNS + "recordAddresses";

    static final String PROP_IGNORE_IPV4_ADDRESSES = PROP_PREFIX_DNS + "ignoreIPv4Addresses";

    static final String PROP_IGNORE_IPV6_ADDRESSES = PROP_PREFIX_DNS + "ignoreIPv6Addresses";

    private static final Logger LOG = LoggerFactory.getLogger(XltDnsResolver.class);

    /**
     * Whether to record resolved addresses in timers.csv.
     */
    private final boolean recordAddresses;

    /**
     * Whether to shuffle the list of resolved addresses. This improves the distribution of traffic to multiple servers,
     * especially if the underlying resolver does not perform a round robin of addresses.
     */
    private final boolean shuffleAddresses;

    /**
     * Whether to pick a single address from the list of resolved addresses. This is useful only if you suspect issues
     * with one of the addresses. If the chosen address cannot be contacted, we would receive an exception right away.
     * Otherwise the HTTP client would silently try one address after the other until a connection is established,
     * masking issues with a certain server and causing longer over-all request runtimes.
     */
    private final boolean pickOneAddressRandomly;

    /**
     * Whether to cache address resolutions.
     */
    private final boolean cacheAddresses;

    /**
     * Whether to ignore IPv4 addresses received from DNS.
     */
    private final boolean ignoreIPv4Addresses;

    /**
     * Whether to ignore IPv6 addresses received from DNS.
     */
    private final boolean ignoreIPv6Addresses;

    /**
     * The address resolution cache. The cache does not expire. It lives as long as this {@link XltDnsResolver}
     * instance, which typically lives as long as the web client of a virtual user.
     */
    private final Map<String, InetAddress[]> addressesByHostName;

    /**
     * The underlying host name resolver.
     */
    private final DnsOverrideResolver resolver;

    /**
     * Creates a new instance and configures it according to the project configuration files.
     */
    public XltDnsResolver()
    {
        final XltProperties props = XltProperties.getInstance();

        recordAddresses = props.getProperty(PROP_RECORD_ADDRESSES, false);
        shuffleAddresses = props.getProperty(PROP_SHUFFLE_ADDRESSES, false);
        pickOneAddressRandomly = props.getProperty(PROP_PICK_ONE_ADDRESS_RANDOMLY, false);

        ignoreIPv4Addresses = props.getProperty(PROP_IGNORE_IPV4_ADDRESSES, false);
        ignoreIPv6Addresses = props.getProperty(PROP_IGNORE_IPV6_ADDRESSES, false);

        if (ignoreIPv4Addresses && ignoreIPv6Addresses)
        {
            LOG.warn(String.format("Both properties '%s' and '%s' are set to true at the same time. This effectively disables host name resolution!",
                                   PROP_IGNORE_IPV4_ADDRESSES, PROP_IGNORE_IPV6_ADDRESSES));
        }

        cacheAddresses = props.getProperty(PROP_CACHE_ADDRESSES, false);
        addressesByHostName = cacheAddresses ? new HashMap<>() : null;

        final String providerName = StringUtils.defaultIfBlank(props.getProperty(PROP_PROVIDER), PlatformHostNameResolver.PROVIDER_NAME);
        resolver = new DnsOverrideResolver(createResolver(providerName));
    }

    /**
     * Creates a host name resolver for the given provider name.
     */
    private static HostNameResolver createResolver(final String providerName)
    {
        final HostNameResolver resolver;

        if (providerName.equals(PlatformHostNameResolver.PROVIDER_NAME))
        {
            resolver = new PlatformHostNameResolver();
        }
        else if (providerName.equals(DnsJavaHostNameResolver.PROVIDER_NAME))
        {
            resolver = new DnsJavaHostNameResolver();
        }
        else
        {
            throw new XltException("Unknown DNS provider configured: " + providerName);
        }

        return resolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InetAddress[] resolve(final String host) throws UnknownHostException
    {
        InetAddress[] addresses = null;

        if (cacheAddresses)
        {
            addresses = addressesByHostName.get(host);
        }

        if (addresses == null) // caching not enabled or host name not cached yet
        {
            // perform address resolution
            addresses = doResolve(host, resolver);

            // remove IPv4 or IPv6 addresses
            addresses = removeIgnoredAddresses(addresses);
            if (addresses.length == 0)
            {
                throw new UnknownHostException(host);
            }

            // post-process in case we have got multiple addresses
            if (addresses.length > 1)
            {
                if (pickOneAddressRandomly)
                {
                    addresses = pickRandomAddress(addresses);
                }
                else if (shuffleAddresses)
                {
                    shuffleAddresses(addresses);
                }
            }

            if (cacheAddresses)
            {
                // cache the post-processed addresses
                addressesByHostName.put(host, addresses);
            }
        }

        return addresses;
    }

    /**
     * Performs the actual address resolution using the passed resolver and measures the resolution time.
     */
    InetAddress[] doResolve(final String host, final HostNameResolver resolver) throws UnknownHostException
    {
        final RequestExecutionContext requestExecutionContext = RequestExecutionContext.getCurrent();
        final SocketMonitor socketMonitor = requestExecutionContext.getSocketMonitor();

        InetAddress[] inetAddresses = null;

        try
        {
            socketMonitor.dnsLookupStarted();
            inetAddresses = resolver.resolve(host);
        }
        finally
        {
            socketMonitor.dnsLookupDone();
        }

        if (recordAddresses)
        {
            final String[] ipAddresses = extractIpAddresses(inetAddresses);

            final DnsMonitor dnsMonitor = requestExecutionContext.getDnsMonitor();
            dnsMonitor.dnsLookupDone(ipAddresses);
        }

        return inetAddresses;
    }

    /**
     * Returns the raw IP address string for each of the given {@link InetAddress} instances.
     *
     * @param inetAddresses
     *            the list of {@link InetAddress} instances
     * @return the corresponding list of IP addresses
     */
    private static String[] extractIpAddresses(final InetAddress[] inetAddresses)
    {
        final String[] ipAddresses;

        if (inetAddresses == null || inetAddresses.length == 0)
        {
            ipAddresses = ArrayUtils.EMPTY_STRING_ARRAY;
        }
        else
        {
            ipAddresses = new String[inetAddresses.length];

            for (int i = 0; i < inetAddresses.length; i++)
            {
                ipAddresses[i] = inetAddresses[i].getHostAddress();
            }
        }

        return ipAddresses;
    }

    /**
     * Removes any IPv4/6 addresses from the given IP addresses, if so configured, and returns the remaining ones.
     * 
     * @param addresses
     *            all addresses received from DNS
     * @return the remaining addresses
     */
    private InetAddress[] removeIgnoredAddresses(final InetAddress[] addresses)
    {
        // check if there is something to do at all
        if (!ignoreIPv4Addresses && !ignoreIPv6Addresses)
        {
            return addresses;
        }

        // filter out part of the addresses
        final List<InetAddress> remainingAddresses = new ArrayList<>();

        for (final InetAddress address : addresses)
        {
            if ((!ignoreIPv4Addresses && address instanceof Inet4Address) || (!ignoreIPv6Addresses && address instanceof Inet6Address))
            {
                remainingAddresses.add(address);
            }
        }

        return remainingAddresses.toArray(new InetAddress[remainingAddresses.size()]);
    }

    /**
     * Returns only one (randomly chosen) address from the given list of addresses.
     */
    private static InetAddress[] pickRandomAddress(final InetAddress[] addresses)
    {
        return new InetAddress[]
            {
                addresses[XltRandom.nextInt(addresses.length)]
            };
    }

    /**
     * Shuffles the given list of addresses.
     */
    private static void shuffleAddresses(final InetAddress[] addresses)
    {
        ArrayUtils.shuffle(addresses);
    }
}
