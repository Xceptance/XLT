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
