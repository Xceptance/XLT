package com.xceptance.xlt.report.providers;

import java.math.BigDecimal;

/**
 * Represents some of the statistics for a certain value.
 */
public class StatisticsReport
{
    /**
     * The minimum value.
     */
    public int min;

    /**
     * The maximum value.
     */
    public int max;

    /**
     * The mean value.
     */
    public BigDecimal mean;

    /**
     * The deviation of the value.
     */
    public BigDecimal deviation;
}
