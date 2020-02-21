package com.xceptance.xlt.report.providers;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.xceptance.xlt.report.util.CustomMapConverter;

/**
 * Represents the timer statistics in a test report. The statistics is generated from a series of timer events.
 */
@XStreamAlias("timer")
public class TimerReport
{
    /**
     * The timer name.
     */
    public String name;

    /**
     * The number how often the timer has fired.
     */
    public int count;

    /**
     * The number how often the timer has fired.
     */
    public BigDecimal countPerSecond;

    /**
     * The number how often the timer has fired.
     */
    public BigDecimal countPerMinute;

    /**
     * The number how often the timer has fired.
     */
    public BigDecimal countPerHour;

    /**
     * The number how often the timer has fired.
     */
    public BigDecimal countPerDay;

    /**
     * The number how often the request has failed.
     */
    public int errors;

    /**
     * The minimum timer runtime.
     */
    public long min;

    /**
     * The maximum timer runtime.
     */
    public long max;

    /**
     * The median of the timer runtime.
     */
    public BigDecimal median;

    /**
     * The mean of the timer runtime.
     */
    public BigDecimal mean;

    /**
     * The deviation of the timer runtime.
     */
    public BigDecimal deviation;

    /**
     * Additional information is required to colorize the requests later.
     */
    public String colorizationGroupName;

    /**
     * The configured percentiles of the timer runtime.
     */
    @XStreamConverter(CustomMapConverter.class)
    public Map<String, BigDecimal> percentiles = new LinkedHashMap<>();
}
