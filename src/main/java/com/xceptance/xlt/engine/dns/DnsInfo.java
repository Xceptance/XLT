package com.xceptance.xlt.engine.dns;

import java.util.Arrays;

/**
 * Class to hold all DNS information gathered during the execution of a single request.
 */
public class DnsInfo
{
    /**
     * The resolved IP address(es).
     */
    private final String[] ipAddresses;

    /**
     * Constructor.
     * 
     * @param ipAddresses
     *            the resolved IP addresses
     */
    public DnsInfo(final String[] ipAddresses)
    {
        this.ipAddresses = ipAddresses;
    }

    /**
     * Returns the resolved IP addresses.
     * 
     * @return the IP addresses
     */
    public String[] getIpAddresses()
    {
        return ipAddresses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return String.format("%s{ipAddresses=%s}", getClass().getSimpleName(), Arrays.toString(ipAddresses));
    }
}
