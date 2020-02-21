package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * 
 */
@XStreamAlias("customTimers")
public class CustomTimersReport
{
    @XStreamImplicit
    public List<TimerReport> customTimers = new ArrayList<TimerReport>();
}
