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
 * The {@link Median} class calculates the median value from the <code>int</code> values added. In contrast to other
 * implementations, this class does not store any value added, but counts the occurrences of each value. This approach
 * saves memory if the values added are in roughly the same range.
 * 
 * @deprecated As of XLT 4.6.0, use {@link RuntimeHistogram} instead.
 */
@Deprecated
public class Median
{
    /**
     * The default precision.
     */
    private static final int DEFAULT_PRECISION = 1;

    /**
     * The buckets allocated so far.
     */
    private int[] countPerBucket;

    /**
     * 
     */
    private int firstIndex;

    /**
     * 
     */
    private int lastIndex;

    /**
     * 
     */
    private final int precision;

    /**
     * The number of values added so far to this median calculator.
     */
    private int valueCount;

    /**
     * Constructor.
     */
    public Median()
    {
        this(DEFAULT_PRECISION);
    }

    /**
     * Constructor.
     * 
     * @param precision
     *            the precision to use
     */
    public Median(final int precision)
    {
        this.precision = precision;
    }

    /**
     * Adds a value to this median calculator.
     * 
     * @param value
     *            the value to add
     */
    public void addValue(final int value)
    {
        final int index = value / precision;

        if (valueCount == 0)
        {
            countPerBucket = new int[1];

            countPerBucket[0] = 1;

            firstIndex = lastIndex = index;
            valueCount = 1;
        }
        else
        {
            // grow/shift values array if necessary
            if (index < firstIndex)
            {
                final int delta = firstIndex - index;

                grow(delta, true);

                firstIndex = index;
            }
            else if (index > lastIndex)
            {
                final int delta = index - lastIndex;

                grow(delta, false);

                lastIndex = index;
            }

            countPerBucket[index - firstIndex]++;
            valueCount++;
        }
    }

    /**
     * Returns the median of the values added.
     * 
     * @return the median value
     */
    public double getMedianValue()
    {
        double median;

        if (valueCount == 0)
        {
            median = 0.0;
        }
        else
        {
            // get the index of the "middle" bucket
            int i = -1;
            int count = 0;

            while (count * 2 < valueCount)
            {
                count += countPerBucket[++i];
            }

            // calculate the median
            if (count * 2 == valueCount)
            {
                // we have exactly hit a bucket boundary so take the mean
                final int i1 = i;

                while (countPerBucket[++i] == 0)
                {
                }

                final int i2 = i;

                median = (i1 + firstIndex + i2 + firstIndex) * precision / 2.0;
            }
            else
            {
                median = (firstIndex + i) * precision;
            }
        }

        return median;
    }

    /**
     * Returns the number of allocated buckets.
     * 
     * @return the number of buckets used
     */
    public int getNumberOfBuckets()
    {
        return countPerBucket.length;
    }

    /**
     * Grows the bucket array by the specified number of buckets.
     * 
     * @param delta
     *            the number of buckets to add
     */
    private void grow(final int delta, final boolean shiftToRight)
    {
        final int[] newCountPerBucket = new int[countPerBucket.length + delta];

        System.arraycopy(countPerBucket, 0, newCountPerBucket, shiftToRight ? delta : 0, countPerBucket.length);

        countPerBucket = newCountPerBucket;
    }
}
