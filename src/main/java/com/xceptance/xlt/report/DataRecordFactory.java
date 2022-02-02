/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
