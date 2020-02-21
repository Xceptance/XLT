package com.xceptance.xlt.report.external.util.dataItem;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeriesDataItem;

/**
 * @author matthias.ullrich
 */
public class DataItem extends TimeSeriesDataItem
{
    private static final long serialVersionUID = 1L;

    private double sum = 0;

    private int count = 0;

    /**
     * @param period
     * @param value
     */
    public DataItem(final RegularTimePeriod period, final double value)
    {
        super(period, value);
        addOrUpdate(value);
    }

    /**
     * get the average value (sum of all values divided by their count)
     * 
     * @return the average value
     */
    @Override
    public Number getValue()
    {
        return count > 0 ? (sum / count) : 0;
    }

    /**
     * add or update a value to this item
     * 
     * @param value
     */
    public void addOrUpdate(final double value)
    {
        sum += value;
        count++;
    }
}
