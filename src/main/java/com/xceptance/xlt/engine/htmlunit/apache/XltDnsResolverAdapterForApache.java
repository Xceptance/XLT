package com.xceptance.xlt.engine.htmlunit.apache;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.DnsResolver;

import com.xceptance.xlt.engine.dns.XltDnsResolver;

/**
 * An API adapter that makes XLT's DNS resolver fit for use with Apache's {@link HttpClient}.
 */
public class XltDnsResolverAdapterForApache implements DnsResolver
{
    private final XltDnsResolver xltDnsResolver;

    public XltDnsResolverAdapterForApache(final XltDnsResolver xltDnsResolver)
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
}
