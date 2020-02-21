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
