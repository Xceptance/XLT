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
package com.xceptance.xlt.engine.htmlunit.jetty;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.Promise;
import org.eclipse.jetty.util.SocketAddressResolver;

import com.xceptance.xlt.engine.dns.XltDnsResolver;

/**
 * An API adapter that makes XLT's DNS resolver fit for use with Jetty's {@link HttpClient}.
 */
public class XltDnsResolverAdapterForJetty implements SocketAddressResolver
{
    private final XltDnsResolver xltDnsResolver;

    public XltDnsResolverAdapterForJetty(final XltDnsResolver xltDnsResolver)
    {
        this.xltDnsResolver = xltDnsResolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolve(final String host, final int port, final Promise<List<InetSocketAddress>> promise)
    {
        try
        {
            final InetAddress[] addresses = xltDnsResolver.resolve(host);

            final List<InetSocketAddress> result = new ArrayList<>(addresses.length);
            for (final InetAddress address : addresses)
            {
                result.add(new InetSocketAddress(address, port));
            }

            promise.succeeded(result);
        }
        catch (final UnknownHostException e)
        {
            promise.failed(e);
        }
    }
}
