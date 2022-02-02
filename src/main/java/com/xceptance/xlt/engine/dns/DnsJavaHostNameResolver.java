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
package com.xceptance.xlt.engine.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Address;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.api.util.XltProperties;

/**
 * A {@link HostNameResolver} implementation that uses the DnsJava library to resolve host names.
 */
class DnsJavaHostNameResolver implements HostNameResolver
{
    static final String PROVIDER_NAME = "dnsjava";

    private static final String PROP_PREFIX = XltDnsResolver.PROP_PREFIX_DNS_PROVIDERS + PROVIDER_NAME + ".";

    private static final String PROP_PREFIX_RESOLVER = PROP_PREFIX + "resolver.";

    private static final String PROP_DNS_SERVERS = PROP_PREFIX_RESOLVER + "servers";

    private static final String PROP_TIMEOUT = PROP_PREFIX_RESOLVER + "timeout";

    private static final String PROP_EDNS_VERSION = PROP_PREFIX + "edns.version";

    private final Resolver resolver;

    /**
     * Creates a new instance and configures it according to the project configuration files.
     */
    DnsJavaHostNameResolver()
    {
        final XltProperties props = XltProperties.getInstance();

        // DNS resolver
        try
        {
            final String dnsServers = props.getProperty(PROP_DNS_SERVERS, "");
            final int timeoutSeconds = props.getProperty(PROP_TIMEOUT, 5);
            final int ednsVersion = props.getProperty(PROP_EDNS_VERSION, 0);

            final String[] serverAddresses = StringUtils.split(dnsServers, "\t ,;");
            resolver = (serverAddresses.length == 0) ? new ExtendedResolver() : new ExtendedResolver(serverAddresses);

            resolver.setTimeout(timeoutSeconds);

            if (ednsVersion >= 0)
            {
                resolver.setEDNS(ednsVersion);
            }
        }
        catch (final UnknownHostException e)
        {
            throw new XltException("Failed to set up resolver", e);
        }

        // diagnostic output
        // Options.set("verbose", "true");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InetAddress[] resolve(final String name) throws UnknownHostException
    {
        try
        {
            final InetAddress addr = Address.getByAddress(name);

            return new InetAddress[]
                {
                    addr
                };
        }
        catch (final UnknownHostException e)
        {
            return lookupAddressesByHostName(name);
        }
    }

    /**
     * Looks up all addresses for the given host name.
     */
    private InetAddress[] lookupAddressesByHostName(final String name) throws UnknownHostException
    {
        final Record[] records = lookupRecordsByHostName(name);

        final InetAddress[] addrs = new InetAddress[records.length];
        for (int i = 0; i < records.length; i++)
        {
            addrs[i] = addressFromRecord(name, records[i]);
        }

        return addrs;
    }

    /**
     * Looks up all DNS records for the given host name.
     */
    private Record[] lookupRecordsByHostName(final String name) throws UnknownHostException
    {
        try
        {
            final Record[] a = createNewLookup(name, Type.A).run();
            final Record[] aaaa = createNewLookup(name, Type.AAAA).run();

            final Record[] merged = ArrayUtils.addAll(a, aaaa);
            if (merged == null)
            {
                throw new UnknownHostException("Unknown host: " + name);
            }

            return merged;
        }
        catch (final TextParseException e)
        {
            throw new UnknownHostException("Invalid host name: " + name);
        }
    }

    /**
     * Creates a new {@link Lookup} instance for the given host name and query type and customizes it a bit.
     */
    private Lookup createNewLookup(final String name, final int type) throws TextParseException
    {
        final Lookup lookup = new Lookup(name, type);

        lookup.setResolver(resolver);

        return lookup;
    }

    /**
     * Creates an {@link InetAddress} instance from the host name and the data in the given record.
     */
    private static InetAddress addressFromRecord(final String name, final Record r) throws UnknownHostException
    {
        InetAddress addr;

        if (r instanceof ARecord)
        {
            addr = ((ARecord) r).getAddress();
        }
        else
        {
            addr = ((AAAARecord) r).getAddress();
        }

        return InetAddress.getByAddress(name, addr.getAddress());
    }
}
