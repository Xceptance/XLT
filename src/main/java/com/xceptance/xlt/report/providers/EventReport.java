package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Represents the statistics for a certain event in a test report.
 */
@XStreamAlias("event")
public class EventReport
{
    /**
     * The number how often a certain event has occurred.
     */
    public int totalCount;

    /**
     * The name of the event.
     */
    public String name;

    /**
     * The name of the test case that generated the event.
     */
    public String testCaseName;

    /**
     * The list of associated messages and their respective count.
     */
    public List<EventMessageInfo> messages = new ArrayList<EventMessageInfo>();
}
