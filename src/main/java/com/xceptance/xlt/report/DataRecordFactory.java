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
import java.util.function.Supplier;

import com.xceptance.common.util.CsvByteColumns;
import com.xceptance.xlt.agent.JvmResourceUsageData;
import com.xceptance.xlt.api.engine.AbstractData;
import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.engine.CustomValue;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.PageLoadTimingData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.engine.WebVitalData;
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
     * the array bounds check completely. Standard built-in data record classes use direct constructor method
     * references (zero reflection overhead), while unconfigured slots use {@link UnsupportedData#new} so unmapped
     * bytes never trigger {@link NullPointerException} or {@link ArrayIndexOutOfBoundsException} while adding zero
     * conditional branches to the hot path.
     */
    @SuppressWarnings("unchecked")
    private final Supplier<? extends Data>[] suppliers = new Supplier[256];

    private final boolean standardRequestData;
    private final boolean standardActionData;
    private final boolean standardTransactionData;
    private final boolean standardCustomData;

    /**
     * Sentinel data class whose supplier is installed in all unconfigured slots of {@link #suppliers}.
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

        Arrays.fill(suppliers, (Supplier<Data>) UnsupportedData::new);

        // fill configured slots with direct method references or constructor suppliers
        for (final Map.Entry<String, Class<? extends Data>> entry : dataClasses.entrySet())
        {
            final int typeCode = entry.getKey().charAt(0) & 0xFF;
            final Class<? extends Data> clazz = entry.getValue();

            suppliers[typeCode] = createSupplier(clazz);
        }

        this.standardRequestData = dataClasses.get("R") == RequestData.class;
        this.standardActionData = dataClasses.get("A") == ActionData.class;
        this.standardTransactionData = dataClasses.get("T") == TransactionData.class;
        this.standardCustomData = dataClasses.get("C") == CustomData.class;
    }

    private static Supplier<? extends Data> createSupplier(final Class<? extends Data> clazz)
    {
        if (clazz == RequestData.class) return RequestData::new;
        if (clazz == ActionData.class) return ActionData::new;
        if (clazz == TransactionData.class) return TransactionData::new;
        if (clazz == CustomData.class) return CustomData::new;
        if (clazz == EventData.class) return EventData::new;
        if (clazz == CustomValue.class) return CustomValue::new;
        if (clazz == PageLoadTimingData.class) return PageLoadTimingData::new;
        if (clazz == WebVitalData.class) return WebVitalData::new;
        if (clazz == JvmResourceUsageData.class) return JvmResourceUsageData::new;

        try
        {
            final Constructor<? extends Data> constructor = clazz.getConstructor();
            return () -> {
                try
                {
                    return constructor.newInstance();
                }
                catch (final RuntimeException e)
                {
                    throw e;
                }
                catch (final Exception e)
                {
                    throw new RuntimeException(e);
                }
            };
        }
        catch (final NoSuchMethodException | SecurityException e)
        {
            throw new XltException("Could not determine default constructor of class " + clazz.getName(), e);
        }
    }

    public boolean isStandardRequestData()
    {
        return standardRequestData;
    }

    public boolean isStandardActionData()
    {
        return standardActionData;
    }

    public boolean isStandardTransactionData()
    {
        return standardTransactionData;
    }

    public boolean isStandardCustomData()
    {
        return standardCustomData;
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
        return suppliers[typeCode & 0xFF].get();
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
        return suppliers[Math.min(typeCode, 255)].get();
    }
}
