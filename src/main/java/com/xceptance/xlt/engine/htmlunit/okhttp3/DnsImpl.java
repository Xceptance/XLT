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
package com.xceptance.xlt.engine.htmlunit.okhttp3;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import com.xceptance.xlt.engine.dns.XltDnsResolver;

import okhttp3.Dns;

/**
 * An OkHttp {@link Dns} implementation that is backed by XLT's DNS resolver.
 */
class DnsImpl implements Dns
{
    private final XltDnsResolver xltDnsResolver;

    public DnsImpl(final XltDnsResolver xltDnsResolver)
    {
        this.xltDnsResolver = xltDnsResolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<InetAddress> lookup(final String hostname) throws UnknownHostException
    {
        final InetAddress[] addresses = xltDnsResolver.resolve(hostname);

        return Arrays.asList(addresses);
    }
}
