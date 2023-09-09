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
package com.xceptance.xlt.report;

import java.lang.reflect.Constructor;
import java.util.Map;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.util.XltCharBuffer;
import com.xceptance.xlt.api.util.XltException;

/**
 * Data classes hold processor for certain data types, such as Request, Transaction, Action, and more. This is indicated
 * in the logs by the first column of the record (a line), such as A, T, R, C, and more. This can be later extended. The
 * column is not limited to a single character and can hold more, in case we run out of options sooner or later.
 */
public class DataRecordFactory
{
    /**
     * The registered default constructors per Data(Record) type.
     */
    private final Constructor<? extends Data> constructors[];

    /**
     * The offset of the characters in that array aka A-Z, needs offset A
     */
    private final int offset;

    /**
     * Setup this factory based on the config
     *
     * @param dataClasses
     *            the data classes to support
     */
    @SuppressWarnings("unchecked")
    public DataRecordFactory(final Map<String, Class<? extends Data>> dataClasses)
    {
        // parameter check
        if (dataClasses == null || dataClasses.size() == 0)
        {
            throw new XltException("No Data classes configured");
        }

        // determine the upper and lower limit for a nice efficient array
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (final Map.Entry<String, Class<? extends Data>> entry : dataClasses.entrySet())
        {
            final char c = entry.getKey().charAt(0);

            min = Math.min(min, c);
            max = Math.max(max, c);
        }

        offset = min;
        constructors = new Constructor[max - offset + 1];

        // partially fill the array with constructor references
        for (final Map.Entry<String, Class<? extends Data>> entry : dataClasses.entrySet())
        {
            final int typeCode = entry.getKey().charAt(0);
            final Class<? extends Data> clazz = entry.getValue();

            try
            {
                final Constructor<? extends Data> constructor = clazz.getConstructor();
                constructors[typeCode - offset] = constructor;
            }
            catch (final NoSuchMethodException | SecurityException e)
            {
                throw new XltException("Could not determine default constructor of class " + clazz.getName(), e);
            }
        }
    }

    /**
     * Creates a data record object for the given CSV line. Except for the type code character at the beginning, the CSV
     * line is not parsed yet.
     *
     * @param data
     *            the csv line
     * @return a data record object matching the type code
     * @throws Exception
     */
    public Data createStatistics(final XltCharBuffer src) throws Exception
    {
        // TODO: The following may throw NullPointerException or ArrayIndexOutOfBoundsException in case of unknown type
        // codes.
        final Constructor<? extends Data> c = constructors[src.charAt(0) - offset];
        final Data data = c.newInstance();

        return data;
    }
}
