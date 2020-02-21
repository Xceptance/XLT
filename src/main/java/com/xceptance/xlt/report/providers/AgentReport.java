package com.xceptance.xlt.report.providers;

import java.math.BigDecimal;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 */
@XStreamAlias("agent")
public class AgentReport
{
    public DoubleStatisticsReport totalCpuUsage;

    public DoubleStatisticsReport cpuUsage;

    public long fullGcCount;

    public BigDecimal fullGcCpuUsage;

    public long fullGcTime;

    public long minorGcCount;

    public BigDecimal minorGcCpuUsage;

    public long minorGcTime;

    public String name;
}
