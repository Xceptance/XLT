package com.xceptance.xlt.report.providers;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 */

@XStreamAlias("interval")
public class RuntimeInterval
{
    /**
     */

    @XStreamAsAttribute
    public String from;

    /**
     */

    @XStreamAsAttribute
    public String to;

    public RuntimeInterval(final String from, final String to)
    {
        this.from = from;
        this.to = to;
    }
}
