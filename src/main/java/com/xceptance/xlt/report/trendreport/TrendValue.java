package com.xceptance.xlt.report.trendreport;

import java.util.Date;

/**
 *
 */
public class TrendValue implements Comparable<TrendValue>
{
    public Double countPerSecond;

    public Integer errors;

    public Integer maximum;

    public Double mean;

    public Double median;

    public Integer minimum;

    public String reportName;

    public Date reportDate;

    public String reportComment;

    /**
     * Constructor.
     * 
     * @param median
     * @param mean
     * @param minimum
     * @param maximum
     */
    public TrendValue(final Double median, final Double mean, final Integer minimum, final Integer maximum, final String reportName,
                      final Date reportDate, final String reportComment, final Integer errors, final Double countPerSecond)
    {
        this.median = median;
        this.mean = mean;
        this.minimum = minimum;
        this.maximum = maximum;
        this.reportName = reportName;
        this.reportDate = reportDate;
        this.reportComment = reportComment;
        this.errors = errors;
        this.countPerSecond = countPerSecond;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final TrendValue value)
    {
        int result = reportDate.compareTo(value.reportDate);

        if (result == 0)
        {
            result = reportName.compareTo(value.reportName);
        }

        return result;
    }
}
