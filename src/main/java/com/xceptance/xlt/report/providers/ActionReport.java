package com.xceptance.xlt.report.providers;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 */
@XStreamAlias("action")
public class ActionReport extends TimerReport
{
    /**
     * The Apdex value.
     */
    public final ApdexReport apdex = new ApdexReport();
}
