package com.xceptance.xlt.report.providers;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Represents the total number of requests to a certain host.
 */
@XStreamAlias("host")
public class HostReport
{
    /**
     * The host name.
     */
    public String name;

    /**
     * The total number of requests to that host.
     */
    public int count;
}
