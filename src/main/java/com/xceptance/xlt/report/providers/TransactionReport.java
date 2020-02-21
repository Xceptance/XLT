package com.xceptance.xlt.report.providers;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Represents the transaction timer statistics in a test report. The statistics is generated from a series of
 * transaction data.
 */
@XStreamAlias("transaction")
public class TransactionReport extends TimerReport
{
    /**
     * The number of events.
     */
    public int events;
}
