package com.xceptance.xlt.report.providers;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * The statistics for a certain event message.
 */
@XStreamAlias("message")
public class EventMessageInfo
{
    /**
     * The number how often a certain event message has occurred.
     */
    public int count;

    /**
     * The event message.
     */
    public String info;
}
