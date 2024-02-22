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
package com.xceptance.xlt.report.external.util;

import java.util.TreeMap;

import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;

import com.xceptance.xlt.report.external.util.dataItem.DataItem;
import com.xceptance.xlt.report.util.JFreeChartUtils;

/**
 * @author matthias.ullrich
 */
public class ValueSet
{
    protected TreeMap<Second, DataItem> timerData = new TreeMap<Second, DataItem>();

    /**
     * add or update current value set with time value pair
     * 
     * @param time
     *            time to update
     * @param value
     *            value
     */
    public void addOrUpdate(final long time, final double value)
    {
        final Second s = JFreeChartUtils.getSecond(time);

        DataItem current = timerData.get(s);
        if (current == null)
        {
            current = new DataItem(s, value);
            timerData.put(s, current);
        }
        current.addOrUpdate(value);
    }

    /**
     * convert current value set to time series
     * 
     * @param timeSeriesName
     *            time series name
     * @return resulting time series
     */
    public TimeSeries toTimeSeries(final String timeSeriesName)
    {
        final TimeSeries timeSeries = new TimeSeries(timeSeriesName);

        for (final DataItem item : timerData.values())
        {
            timeSeries.add(item);
        }

        return timeSeries;
    }
}
