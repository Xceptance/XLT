package com.xceptance.xlt.report.providers;

import java.math.BigDecimal;

/**
 * Represents some of the statistics for a certain value.
 */
public class DoubleStatisticsReport
{
    /**
     * The minimum value.
     */
    public BigDecimal min;

    /**
     * The maximum value.
     */
    public BigDecimal max;

    /**
     * The mean value.
     */
    public BigDecimal mean;

    /**
     * The deviation of the value.
     */
    public BigDecimal deviation;
}
