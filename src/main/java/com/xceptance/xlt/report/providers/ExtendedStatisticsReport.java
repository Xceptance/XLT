package com.xceptance.xlt.report.providers;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Represents some more statistics for a certain value.
 */
public class ExtendedStatisticsReport extends StatisticsReport
{
    /**
     * The total count.
     */
    public BigInteger totalCount;

    /**
     * The count per second.
     */
    public BigDecimal countPerSecond;

    /**
     * The count per minute.
     */
    public BigDecimal countPerMinute;

    /**
     * The count per hour.
     */
    public BigDecimal countPerHour;

    /**
     * The count per day.
     */
    public BigDecimal countPerDay;
}
