package com.xceptance.xlt.engine.dns;

import org.apache.commons.lang3.ArrayUtils;

import com.xceptance.xlt.engine.socket.SocketMonitor;

/**
 * Class to monitor DNS activities and record results. Make sure you call {@link #reset()} after querying the
 * information!
 * <p>
 * Note: Address resolution time is recorded by {@link SocketMonitor}.
 */
public class DnsMonitor
{
    /**
     * The resolved IP address(es).
     */
    private String[] ipAddresses = ArrayUtils.EMPTY_STRING_ARRAY;

    /**
     * Sets the resolved IP address(es).
     */
    public void dnsLookupDone(String[] addresses)
    {
        ipAddresses = addresses;
    }

    /**
     * Returns the current DNS information.
     */
    public DnsInfo getDnsInfo()
    {
        return new DnsInfo(ipAddresses);
    }

    /**
     * Resets the stored data.
     */
    public void reset()
    {
        ipAddresses = ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
