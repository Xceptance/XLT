package com.xceptance.xlt.report;

import java.util.HashMap;
import java.util.Map;

import com.xceptance.xlt.api.engine.Data;

/**
 * 
 */
public class DataRecordFactory
{
    private final Map<String, Class<? extends Data>> classes = new HashMap<String, Class<? extends Data>>(11);

    public void registerStatisticsClass(final Class<? extends Data> c, final String typeCode)
    {
        classes.put(typeCode, c);
    }

    public void unregisterStatisticsClass(final String typeCode)
    {
        classes.remove(typeCode);
    }

    public Data createStatistics(final String s) throws Exception
    {
        // get the type code
        int i = s.indexOf(Data.DELIMITER);
        if (i == -1)
        {
            i = s.length();
        }

        final String typeCode = s.substring(0, i);

        // get the respective data record class
        final Class<? extends Data> c = classes.get(typeCode);
        if (c == null)
        {
            throw new RuntimeException("No class found for type code: " + typeCode);
        }

        // create the statistics object
        final Data stats = c.newInstance();
        stats.fromCSV(s);

        return stats;
    }
}
