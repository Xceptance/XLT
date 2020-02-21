package com.xceptance.xlt.report.providers;

import java.util.Date;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("general")
public class GeneralReport
{
    /**
     * The total number of bytes sent.
     */
    public long bytesSent;

    /**
     * The total number of bytes received.
     */
    public long bytesReceived;

    /**
     * The total number of hits.
     */
    public long hits;

    /**
     * The start time of the test.
     */
    public Date startTime;

    /**
     * The end time of the test.
     */
    public Date endTime;

    /**
     * The total run time of the test.
     */
    public int duration;

    /**
     * Infos about the slowest requests.
     */
    public List<SlowRequestReport> slowestRequests;
}
