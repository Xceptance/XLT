package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * All statistics data for all events. Represents the Events section in the test report XML.
 */
@XStreamAlias("events")
public class EventsReport
{
    /**
     * The data collected for each event type.
     */
    @XStreamImplicit
    public List<EventReport> events = new ArrayList<EventReport>();
}
