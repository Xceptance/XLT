/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeriesDataItem;

/**
 * A time series data item that wraps a {@link IntMinMaxValue}.
 */
public class IntMinMaxTimeSeriesDataItem extends TimeSeriesDataItem
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 6836336147826544850L;

    /**
     * The wrapped min/max value.
     */
    private final IntMinMaxValue minMaxValue;

    /**
     * The wrapped time series entry.
     */
    private final IntTimeSeriesEntry entry;

    /**
     * Constructor. Sets the average from the passed min/max value as the super class's value.
     * 
     * @param period
     *            the time period
     * @param minMaxValue
     *            the min/max value
     */
    public IntMinMaxTimeSeriesDataItem(final RegularTimePeriod period, final IntMinMaxValue minMaxValue)
    {
        super(period, (double) minMaxValue.getAccumulatedValue() / (double) minMaxValue.getValueCount());

        this.minMaxValue = minMaxValue;
        this.entry = null;
    }

    /**
     * Constructor. Sets the average from the passed time series entry as the super class's value.
     *
     * @param period
     *            the time period
     * @param entry
     *            the time series entry
     */
    public IntMinMaxTimeSeriesDataItem(final RegularTimePeriod period, final IntTimeSeriesEntry entry)
    {
        super(period, entry != null ? entry.getAverageValue() : 0.0);

        this.entry = entry;
        this.minMaxValue = null;
    }

    /**
     * Returns the wrapped min/max value, creating an adapter if backed by an IntTimeSeriesEntry.
     * 
     * @return the min/max value
     */
    public IntMinMaxValue getMinMaxValue()
    {
        if (minMaxValue != null)
        {
            return minMaxValue;
        }
        if (entry != null)
        {
            return new IntMinMaxValue(entry);
        }
        return null;
    }

    public IntTimeSeriesEntry getEntry()
    {
        return entry;
    }

    public int getMinimumValue()
    {
        if (entry != null)
        {
            return entry.getMinimumValue();
        }
        return minMaxValue != null ? minMaxValue.getMinimumValue() : 0;
    }

    public int getMaximumValue()
    {
        if (entry != null)
        {
            return entry.getMaximumValue();
        }
        return minMaxValue != null ? minMaxValue.getMaximumValue() : 0;
    }

    public double getAverageValue()
    {
        if (entry != null)
        {
            return entry.getAverageValue();
        }
        return minMaxValue != null ? minMaxValue.getAverageValue() : 0.0;
    }

    public double[] getValues()
    {
        if (entry != null)
        {
            return entry.getValues();
        }
        return minMaxValue != null ? minMaxValue.getValues() : new double[0];
    }
}
