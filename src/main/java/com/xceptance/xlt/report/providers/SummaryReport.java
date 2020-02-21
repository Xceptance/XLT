package com.xceptance.xlt.report.providers;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Represents the Summary section in testreport.xml.
 */
@XStreamAlias("summary")
public class SummaryReport
{
    public TransactionReport transactions;

    public ActionReport actions;

    public RequestReport requests;

    public PageLoadTimingReport pageLoadTimings;

    public CustomTimerReport customTimers;
}
