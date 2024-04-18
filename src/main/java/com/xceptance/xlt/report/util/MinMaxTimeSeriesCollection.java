/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.report.util;

import java.util.TimeZone;

import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;

/**
 * Collection of Min-Max time series objects.
 */
public class MinMaxTimeSeriesCollection extends TimeSeriesCollection
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -7636814302926500703L;

    /**
     * Constructor.
     */
    public MinMaxTimeSeriesCollection()
    {
        super();
    }

    /**
     * Constructor.
     * 
     * @param series
     * @param zone
     */
    public MinMaxTimeSeriesCollection(final TimeSeries series, final TimeZone zone)
    {
        super(series, zone);
    }

    /**
     * Constructor.
     * 
     * @param series
     */
    public MinMaxTimeSeriesCollection(final TimeSeries series)
    {
        super(series);
    }

    /**
     * Constructor.
     * 
     * @param zone
     */
    public MinMaxTimeSeriesCollection(final TimeZone zone)
    {
        super(zone);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number getEndY(final int series, final int item)
    {
        final TimeSeries ts = getSeries(series);

        final TimeSeriesDataItem dataItem = ts.getDataItem(item);
        if (dataItem instanceof IntMinMaxTimeSeriesDataItem)
        {
            return ((IntMinMaxTimeSeriesDataItem) dataItem).getMinMaxValue().getMaximumValue();
        }
        else if (dataItem instanceof DoubleMinMaxTimeSeriesDataItem)
        {
            return ((DoubleMinMaxTimeSeriesDataItem) dataItem).getMinMaxValue().getMaximumValue();
        }
        else
        {
            return dataItem.getValue();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number getStartY(final int series, final int item)
    {
        final TimeSeries ts = getSeries(series);

        final TimeSeriesDataItem dataItem = ts.getDataItem(item);
        if (dataItem instanceof IntMinMaxTimeSeriesDataItem)
        {
            return ((IntMinMaxTimeSeriesDataItem) dataItem).getMinMaxValue().getMinimumValue();
        }
        else if (dataItem instanceof DoubleMinMaxTimeSeriesDataItem)
        {
            return ((DoubleMinMaxTimeSeriesDataItem) dataItem).getMinMaxValue().getMinimumValue();
        }
        else
        {
            return dataItem.getValue();
        }
    }

    /**
     * Returns the min/max value attached to a data item in the given series.
     * 
     * @param series
     *            the series index
     * @param item
     *            the item index
     * @return the min/max value
     */
    public double[] getValues(final int series, final int item)
    {
        final TimeSeries timeSeries = getSeries(series);

        final TimeSeriesDataItem dataItem = timeSeries.getDataItem(item);
        if (dataItem instanceof IntMinMaxTimeSeriesDataItem)
        {
            return ((IntMinMaxTimeSeriesDataItem) dataItem).getMinMaxValue().getValues();
        }
        else if (dataItem instanceof DoubleMinMaxTimeSeriesDataItem)
        {
            return ((DoubleMinMaxTimeSeriesDataItem) dataItem).getMinMaxValue().getValues();
        }
        else
        {
            return new double[]
                {
                    dataItem.getValue().doubleValue()
                };
        }
    }
}
