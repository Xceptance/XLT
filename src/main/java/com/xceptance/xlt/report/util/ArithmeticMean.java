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
package com.xceptance.xlt.report.util;

/**
 * Computes the arithmetic mean for a stream of data values added using the {@link #addValue(double)} method. The data
 * values are not stored in memory, so this class can be used to compute statistics for very large data streams.
 * <p>
 * Note: This class is not thread-safe.
 */
public class ArithmeticMean
{
    /**
     * The number of values added.
     */
    private int count;

    /**
     * The sum of all values.
     */
    private double sum;

    /**
     * Adds a value.
     * 
     * @param value
     *            the value to add
     */
    public void addValue(final double value)
    {
        count++;
        sum += value;
    }

    /**
     * Returns the number of values added.
     * 
     * @return the number of values
     */
    public int getCount()
    {
        return count;
    }

    /**
     * Returns the mean of the values added.
     * 
     * @return the mean
     */
    public double getMean()
    {
        return sum / count;
    }
}
