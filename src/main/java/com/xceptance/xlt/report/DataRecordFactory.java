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
package com.xceptance.xlt.report;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map;

import com.xceptance.common.util.CsvByteColumns;
import com.xceptance.xlt.api.engine.AbstractData;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.util.XltException;

/**
 * Data classes hold processor for certain data types, such as Request, Transaction, Action, and more. This is indicated
 * in the logs by the first column of the record (a line), such as A, T, R, C, and more. This can be later extended. The
 * column is not limited to a single character and can hold more, in case we run out of options sooner or later.
 */
public class DataRecordFactory
{
    /**
     * Direct 256-slot lookup table indexed by {@code typeCode & 0xFF}.
     * <p>
     * Sizing the table to 256 eliminates the {@code - offset} subtraction on every record and allows the JIT
     * compiler to statically prove that {@code typeCode & 0xFF} is always within {@code [0, 255]}, eliding
     * the array bounds check completely. All unconfigured slots are pre-filled with {@link UnsupportedData}'s
     * constructor so unmapped bytes never trigger {@link NullPointerException} or {@link ArrayIndexOutOfBoundsException}
     * while adding zero conditional branches to the hot path.
     */
    @SuppressWarnings("unchecked")
    private final Constructor<? extends Data>[] constructors = new Constructor[256];

    /**
     * Sentinel data class whose constructor is installed in all unconfigured slots of {@link #constructors}.
     */
    public static final class UnsupportedData extends AbstractData
    {
        public UnsupportedData()
        {
            super('?');
            throw new IllegalArgumentException("Unknown data record type code");
        }

        @Override
        public void setRemainingValues(final CsvByteColumns values)
        {
        }
    }

    /**
     * Setup this factory based on the config
     *
     * @param dataClasses
     *            the data classes to support
     */
    public DataRecordFactory(final Map<String, Class<? extends Data>> dataClasses)
    {
        // parameter check
        if (dataClasses == null || dataClasses.size() == 0)
        {
            throw new XltException("No Data classes configured");
        }

        try
        {
            Arrays.fill(constructors, UnsupportedData.class.getConstructor());
        }
        catch (final NoSuchMethodException | SecurityException e)
        {
            throw new XltException("Could not initialize fallback constructor", e);
        }

        // fill configured slots with constructor references
        for (final Map.Entry<String, Class<? extends Data>> entry : dataClasses.entrySet())
        {
            final int typeCode = entry.getKey().charAt(0) & 0xFF;
            final Class<? extends Data> clazz = entry.getValue();

            try
            {
                final Constructor<? extends Data> constructor = clazz.getConstructor();
                constructors[typeCode] = constructor;
            }
            catch (final NoSuchMethodException | SecurityException e)
            {
                throw new XltException("Could not determine default constructor of class " + clazz.getName(), e);
            }
        }
    }

    /**
     * Creates a data record object for the given type code byte.
     *
     * @param typeCode
     *            the type code byte
     * @return a data record object matching the type code
     * @throws Exception
     */
    public Data createStatistics(final byte typeCode) throws Exception
    {
        return constructors[typeCode & 0xFF].newInstance();
    }

    /**
     * Creates a data record object for the given type code character.
     *
     * @param typeCode
     *            the type code character
     * @return a data record object matching the type code
     * @throws Exception
     */
    public Data createStatistics(final char typeCode) throws Exception
    {
        return constructors[Math.min(typeCode, 255)].newInstance();
    }
}
