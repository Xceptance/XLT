package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * 
 */
@XStreamAlias("requests")
public class RequestsReport
{
    @XStreamImplicit
    public List<TimerReport> requests = new ArrayList<TimerReport>();
}
