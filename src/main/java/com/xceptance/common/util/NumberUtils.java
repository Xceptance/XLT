/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.common.util;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Utility class for operations on numbers.
 */
public class NumberUtils
{
    /**
     * Negates the given number.
     *
     * @param number
     *            the number to negate
     * @return the negated number
     * @param <T>
     *            subtypes of {@link Number}. Note: Support for custom implementations of {@link Number} will require
     *            adjustments to the code
     * @throws IllegalArgumentException
     *             if the given number can't be negated because it would exceed the max value of the respective type
     *             (e.g. {@link Integer#MIN_VALUE} can't be negated because the result would exceed
     *             {@link Integer#MAX_VALUE})
     * @throws UnsupportedOperationException
     *             if the given subtype of {@link Number} isn't supported by this method
     */
    public static <T extends Number> T negateNumber(final T number)
    {
        if (number instanceof Byte)
        {
            if (number.byteValue() == Byte.MIN_VALUE)
            {
                throw new IllegalArgumentException("Cannot negate 'Byte.MIN_VALUE'.");
            }

            return (T) Byte.valueOf((byte) -number.byteValue());
        }

        if (number instanceof Short)
        {
            if (number.shortValue() == Short.MIN_VALUE)
            {
                throw new IllegalArgumentException("Cannot negate 'Short.MIN_VALUE'.");
            }

            return (T) Short.valueOf((short) -number.shortValue());
        }

        if (number instanceof Integer)
        {
            if (number.intValue() == Integer.MIN_VALUE)
            {
                throw new IllegalArgumentException("Cannot negate 'Integer.MIN_VALUE'.");
            }

            return (T) Integer.valueOf(-number.intValue());
        }

        if (number instanceof Long)
        {
            if (number.longValue() == Long.MIN_VALUE)
            {
                throw new IllegalArgumentException("Cannot negate 'Long.MIN_VALUE'.");
            }

            return (T) Long.valueOf(-number.longValue());
        }

        if (number instanceof Float)
        {
            return (T) Float.valueOf(-number.floatValue());
        }

        if (number instanceof Double)
        {
            return (T) Double.valueOf(-number.doubleValue());
        }

        if (number instanceof BigInteger)
        {
            return (T) ((BigInteger) number).negate();
        }

        if (number instanceof BigDecimal)
        {
            return (T) ((BigDecimal) number).negate();
        }

        throw new UnsupportedOperationException("Cannot negate number of type '" + number.getClass().getSimpleName() + "'.");
    }
}
