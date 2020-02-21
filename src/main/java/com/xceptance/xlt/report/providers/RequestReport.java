package com.xceptance.xlt.report.providers;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Represents the request timer statistics in a test report. The statistics is generated from a series of request data.
 */
@XStreamAlias("request")
public class RequestReport extends TimerReport
{
    /**
     * The statistics for the "bytesSent" values.
     */
    public ExtendedStatisticsReport bytesSent;

    /**
     * The statistics for the "bytesReceived" values.
     */
    public ExtendedStatisticsReport bytesReceived;

    /**
     * The statistics for the "dnsTime" values.
     */
    public StatisticsReport dnsTime;

    /**
     * The statistics for the "connectTime" values.
     */
    public StatisticsReport connectTime;

    /**
     * The statistics for the "sendTime" values.
     */
    public StatisticsReport sendTime;

    /**
     * The statistics for the "serverBusyTime" values.
     */
    public StatisticsReport serverBusyTime;

    /**
     * The statistics for the "receiveTime" values.
     */
    public StatisticsReport receiveTime;

    /**
     * The statistics for the "timeToFirstBytes" values.
     */
    public StatisticsReport timeToFirstBytes;

    /**
     * The statistics for the "timeToLastBytes" values.
     */
    public StatisticsReport timeToLastBytes;

    /**
     * The number of timer values per configured runtime interval.
     */
    public int[] countPerInterval;

    /**
     * A list of the top URLs.
     */
    public UrlData urls;
}
