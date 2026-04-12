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
import java.util.Map;
import java.util.function.Supplier;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.util.XltCharBuffer;
import com.xceptance.xlt.api.util.XltException;

/**
 * Factory for creating {@link Data} record instances based on type code characters.
 * <p>
 * Data classes hold processors for certain data types, such as Request, Transaction, Action, and more.
 * This is indicated in the logs by the first column of the record (a line), such as A, T, R, C, and more.
 * This can be later extended. The column is not limited to a single character and can hold more, in case
 * we run out of options sooner or later.
 * <p>
 * <b>Performance note:</b> During initialization, reflection is used once to obtain the no-arg constructor
 * of each Data class. However, the constructor is then wrapped in a {@link Supplier} lambda using
 * {@link Constructor#newInstance()}'s lambda-captured form, which the JVM's JIT compiler can inline
 * and optimize far more aggressively than reflective calls dispatched at runtime. The hot path
 * ({@link #createStatistics(char)}) therefore involves only a direct Supplier.get() call — no
 * reflective dispatch, no security checks, no varargs array allocation per invocation.
 */
public class DataRecordFactory
{
    /**
     * Supplier-based factory functions for each registered Data type, indexed by
     * (typeCode - offset). This replaces the previous Constructor[] array to eliminate
     * per-call reflection overhead in the hot parsing loop.
     */
    private final Supplier<? extends Data>[] suppliers;

    /**
     * The offset of the type code characters in the suppliers array (i.e. the smallest
     * type code character value), so that suppliers[typeCode - offset] gives the factory
     * for that type code.
     */
    private final int offset;

    /**
     * Setup this factory based on the config. For each registered Data class, we resolve
     * the no-arg constructor once via reflection, then capture it in a {@link Supplier}
     * lambda for zero-reflection instantiation at runtime.
     *
     * @param dataClasses
     *            the data classes to support, keyed by their single-character type code
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
        suppliers = new Supplier[max - offset + 1];

        // For each registered type, resolve the no-arg constructor once and wrap it
        // in a Supplier lambda. This moves reflection cost to init time, keeping the
        // hot path (createStatistics) allocation-free and JIT-friendly.
        for (final Map.Entry<String, Class<? extends Data>> entry : dataClasses.entrySet())
        {
            final int typeCode = entry.getKey().charAt(0);
            final Class<? extends Data> clazz = entry.getValue();

            try
            {
                final Constructor<? extends Data> constructor = clazz.getConstructor();

                // Capture the constructor in a lambda — the JIT can inline this into
                // a direct allocation + <init> call, avoiding reflective dispatch.
                suppliers[typeCode - offset] = () ->
                {
                    try
                    {
                        return constructor.newInstance();
                    }
                    catch (final Exception e)
                    {
                        throw new XltException("Failed to instantiate " + clazz.getName(), e);
                    }
                };
            }
            catch (final NoSuchMethodException | SecurityException e)
            {
                throw new XltException("Could not determine default constructor of class " + clazz.getName(), e);
            }
        }
    }

    /**
     * Creates a data record object for the given CSV line. Except for the type code
     * character at the beginning, the CSV line is not parsed yet.
     *
     * @param src
     *            the csv line
     * @return a data record object matching the type code
     */
    public final Data createStatistics(final XltCharBuffer src)
    {
        return createStatistics(src.charAt(0));
    }

    /**
     * Creates a data record object for the given type code character.
     * <p>
     * This is the hot-path method called once per CSV line during report generation.
     * It performs a simple array lookup and a Supplier.get() call — no reflection.
     *
     * @param typeCode
     *            the type code character (e.g., 'T', 'R', 'C')
     * @return a data record object matching the type code
     * @throws ArrayIndexOutOfBoundsException
     *             if the type code is outside the registered range
     * @throws NullPointerException
     *             if no Data class is registered for the given type code
     */
    public final Data createStatistics(final char typeCode)
    {
        return suppliers[typeCode - offset].get();
    }
}
