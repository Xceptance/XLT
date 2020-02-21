package com.xceptance.xlt.report.util;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeriesDataItem;

/**
 * A time series data item that wraps a {@link DoubleMinMaxValue}.
 * 
 * @see MinMaxTimeSeriesDataItem
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class DoubleMinMaxTimeSeriesDataItem extends TimeSeriesDataItem
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5133107791147593518L;

    /**
     * The wrapped min/max value.
     */
    private final DoubleMinMaxValue minMaxValue;

    /**
     * Constructor. Sets the average from the passed min/max value as the super class's value.
     * 
     * @param period
     *            the time period
     * @param minMaxValue
     *            the min/max value
     */
    public DoubleMinMaxTimeSeriesDataItem(final RegularTimePeriod period, final DoubleMinMaxValue minMaxValue)
    {
        super(period, minMaxValue.getAverageValue());

        this.minMaxValue = minMaxValue;
    }

    /**
     * Returns the wrapped min/max value.
     * 
     * @return the min/max value
     */
    public DoubleMinMaxValue getMinMaxValue()
    {
        return minMaxValue;
    }
}
