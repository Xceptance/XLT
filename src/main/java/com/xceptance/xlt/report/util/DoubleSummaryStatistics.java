/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
 * Computes summary statistics for a stream of data values added using the {@link #addValue(double))} method. The data
 * values are not stored in memory, so this class can be used to compute statistics for very large data streams. This
 * class is similar to {@link org.apache.commons.math.stat.descriptive.SummaryStatistics}, but reduced to the needed
 * functionality.
 * <p>
 * Note: This class is not thread-safe.
 */
public class DoubleSummaryStatistics
{
    /**
     * The number of values added.
     */
    private long count;

    /**
     * The maximum value.
     */
    private double maximum = Double.MIN_VALUE;

    /**
     * The minimum value.
     */
    private double minimum = Double.MAX_VALUE;

    /**
     * The sum of all values.
     */
    private double sum;

    /**
     * The sum of the square of all values.
     */
    private double sumOfSquares;

    /**
     * Adds a value.
     * 
     * @param value
     *            the value to add
     */
    public void addValue(final double value)
    {
        count++;

        if (value > maximum)
        {
            maximum = value;
        }

        if (value < minimum)
        {
            minimum = value;
        }

        sum += value;
        sumOfSquares += value * value;
    }

    /**
     * Returns the number of values added.
     * 
     * @return the number of values
     */
    public long getCount()
    {
        return count;
    }

    /**
     * Returns the maximum of the values added.
     * 
     * @return the maximum
     */
    public double getMaximum()
    {
        return count == 0 ? 0.0 : maximum;
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

    /**
     * Returns the minimum of the values added.
     * 
     * @return the minimum
     */
    public double getMinimum()
    {
        return count == 0 ? 0.0 : minimum;
    }

    /**
     * Returns the standard deviation of the values added.
     * 
     * @return the standard deviation
     */
    public double getStandardDeviation()
    {
        final double mean = getMean();

        return Math.sqrt(sumOfSquares / count - mean * mean);
    }

    /**
     * Returns the sum of all values added.
     * 
     * @return the sum
     */
    public double getSum()
    {
        return sum;
    }
}
