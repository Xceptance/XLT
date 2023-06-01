/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
    }

    /**
     * Returns the wrapped min/max value.
     * 
     * @return the min/max value
     */
    public IntMinMaxValue getMinMaxValue()
    {
        return minMaxValue;
    }
}
