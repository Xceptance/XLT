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
