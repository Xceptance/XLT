package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("pageLoadTimings")
public class PageLoadTimingsReport
{
    @XStreamImplicit
    public List<TimerReport> pageLoadTimings = new ArrayList<TimerReport>();
}
