package com.xceptance.xlt.engine.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The interface of a host name resolver.
 */
interface HostNameResolver
{
    /**
     * Determines all IP addresses for the given host name.
     *
     * @param host
     *            the host name to look up
     * @return all known IP addresses
     * @exception UnknownHostException
     *                if the host name does not have any addresses
     */
    public InetAddress[] resolve(final String host) throws UnknownHostException;
}
