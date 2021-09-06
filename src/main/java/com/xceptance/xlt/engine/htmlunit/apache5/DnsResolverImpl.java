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
package com.xceptance.xlt.engine.htmlunit.apache5;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.hc.client5.http.DnsResolver;

import com.xceptance.xlt.engine.dns.XltDnsResolver;

/**
 * 
 */
class DnsResolverImpl implements DnsResolver
{
    private final XltDnsResolver xltDnsResolver;

    public DnsResolverImpl(final XltDnsResolver xltDnsResolver)
    {
        this.xltDnsResolver = xltDnsResolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InetAddress[] resolve(final String host) throws UnknownHostException
    {
        return xltDnsResolver.resolve(host);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resolveCanonicalHostname(String host) throws UnknownHostException
    {
        final InetAddress[] resolvedAddresses = resolve(host);
        if (resolvedAddresses.length > 0)
        {
            return resolvedAddresses[0].getCanonicalHostName();
        }
        return host;
    }
}
