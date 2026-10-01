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
package com.xceptance.xlt.report.util;

import java.util.Arrays;

/**
 * The {@link RuntimeHistogram} class calculates any percentile from the <code>int</code> values added. In contrast to
 * other implementations, this class does not store any value added, but counts the occurrences of each value. This
 * approach saves memory if the values added are in roughly the same range.
 */
public class RuntimeHistogram
{
    /**
     * The default precision, power of 2 for shifting aka this is 1<<0 = 1 (no precision loss).
     */
    private static final int DEFAULT_PRECISION = 1;

    /**
     * The buckets allocated so far.
     */
    private int[] countPerBucket;

    /**
     * Value at the first index.
     */
    private int firstIndexValue = Integer.MAX_VALUE;

    /**
     * Value at the last index.
     */
    private int lastIndexValue = Integer.MIN_VALUE;

    /**
     * What is the desired precision? Shift amount (power of 2).
     */
    private final int precision;

    /**
     * The number of values added so far.
     */
    private int valueCount;

    /**
     * Constructor.
     */
    public RuntimeHistogram()
    {
        this(DEFAULT_PRECISION);
    }

    /**
     * Constructor. Precision defines the size of each bucket. E.g. a precision of 8 means that values 0-7 go into
     * that bucket. To be fast, this must be a power of 2 (or rounded up to the next power of 2).
     *
     * @param precision
     *            the precision to use
     */
    public RuntimeHistogram(final int precision)
    {
        if (precision <= 1)
        {
            this.precision = 0;
        }
        else
        {
            final int nPoT = Integer.highestOneBit(precision - 1) << 1;
            this.precision = Integer.numberOfTrailingZeros(nPoT);
        }
    }

    /**
     * Copy constructor. Creates an independent deep copy of the given {@link RuntimeHistogram}.
     *
     * @param other
     *            the instance to copy
     */
    public RuntimeHistogram(final RuntimeHistogram other)
    {
        this.precision = other.precision;
        this.firstIndexValue = other.firstIndexValue;
        this.lastIndexValue = other.lastIndexValue;
        this.valueCount = other.valueCount;
        if (other.countPerBucket != null)
        {
            this.countPerBucket = new int[other.countPerBucket.length];
            System.arraycopy(other.countPerBucket, 0, this.countPerBucket, 0, other.countPerBucket.length);
        }
    }

    /**
     * Adds a value to this histogram.
     *
     * @param value
     *            the value to add
     */
    public void addValue(final int value)
    {
        final int index = value >> this.precision;
        this.valueCount++;

        // grow/shift values array if necessary
        if (index < this.firstIndexValue)
        {
            grow(index);
        }
        else if (index > this.lastIndexValue)
        {
            grow(index);
        }
        else
        {
            this.countPerBucket[index - this.firstIndexValue]++;
        }
    }

    /**
     * Grows the bucket array by the specified number of buckets.
     *
     * @param newIndexPositionToSupport the new index position to cover
     */
    private void grow(final int newIndexPositionToSupport)
    {
        if (firstIndexValue == Integer.MAX_VALUE)
        {
            this.countPerBucket = new int[1];
            this.firstIndexValue = this.lastIndexValue = newIndexPositionToSupport;
            this.countPerBucket[0] = 1;
            return;
        }

        if (newIndexPositionToSupport < this.firstIndexValue)
        {
            final int delta = this.firstIndexValue - newIndexPositionToSupport;
            final int[] oldCountPerBucket = this.countPerBucket;

            this.countPerBucket = new int[this.countPerBucket.length + delta];
            System.arraycopy(oldCountPerBucket, 0, this.countPerBucket, delta, oldCountPerBucket.length);

            this.firstIndexValue = newIndexPositionToSupport;
            this.countPerBucket[0] = 1;
        }
        else
        {
            final int delta = newIndexPositionToSupport - this.lastIndexValue;
            this.countPerBucket = Arrays.copyOf(this.countPerBucket, this.countPerBucket.length + delta);

            this.lastIndexValue = newIndexPositionToSupport;
            this.countPerBucket[this.countPerBucket.length - 1] = 1;
        }
    }

    /**
     * Returns the number of values added.
     * 
     * @return the number of values added
     */
    public int getValueCount()
    {
        return valueCount;
    }

    /**
     * Returns the median of the values added.
     *
     * @return the median value
     */
    public double getMedianValue()
    {
        return getPercentile(50.0);
    }

    /**
     * Returns the p-th quantile of the values added.
     *
     * @param p
     *            the p 0.0 &lt; p &lt;= 1.0
     * @return the p-th percentile
     */
    public double getQuantile(final double p)
    {
        return getPercentile(p * 100.0);
    }

    /**
     * Returns the p-th percentile of the values added.
     *
     * @param p
     *            the p (0 &lt;= p &le; 100)
     * @return the p-th percentile
     */
    public double getPercentile(final double p)
    {
        if (p < 0.0 || p > 100.0)
        {
            throw new IllegalArgumentException("Value of parameter 'p' must be in range (0, 100], but was " + p);
        }

        double value;

        if (valueCount == 0)
        {
            value = 0.0;
        }
        else if (p == 0.0)
        {
            value = (double) (firstIndexValue << precision);
        }
        else if (p == 100.0)
        {
            value = (double) (lastIndexValue << precision);
        }
        else
        {
            final double np = ((double) valueCount) * (p / 100.0d);

            // even number of values -> mean of two adjacent values
            if ((np % 1.0) == 0.0)
            {
                final int value1 = getValueByCount(np);
                final int value2 = getValueByCount(np + 1);

                value = (value1 + value2) / 2.0d;
            }
            else
            {
                value = getValueByCount(Math.ceil(np));
            }
        }

        return value;
    }

    /**
     * Returns if the histogram is empty.
     * 
     * @return true if empty, false otherwise
     */
    public boolean isEmpty()
    {
        return valueCount == 0;
    }

    /**
     * Returns the number of values that fall into the specified value range.
     *
     * @param start the start value (inclusive)
     * @param end the end value (inclusive)
     * 
     * @return the number of values in the range
     */
    public long getCountForValue(final int start, final int end)
    {
        if (start > end)
        {
            throw new IllegalArgumentException("Start value must be less than or equal to end value");
        }

        if (valueCount == 0)
        {
            return 0;
        }

        final int startIndex = start >> this.precision;
        final int endIndex = end >> this.precision;

        if (endIndex < this.firstIndexValue || startIndex > this.lastIndexValue)
        {
            return 0;
        }

        final int firstBucket = Math.max(0, startIndex - this.firstIndexValue);
        final int lastBucket = Math.min(this.countPerBucket.length - 1, endIndex - this.firstIndexValue);

        long count = 0;
        for (int i = firstBucket; i <= lastBucket; i++)
        {
            count += this.countPerBucket[i];
        }

        return count;
    }

    /**
     * Returns the value that belongs to the last index entry which
     * fulfilled the condition of having at least the given number of values.
     *
     * @param totalCount the total count to reach
     * @return the value corrected to precision
     */
    private int getValueByCount(final double totalCount)
    {
        int bucketIndex = -1;
        double count = 0;

        while (count < totalCount)
        {
            count += countPerBucket[++bucketIndex];
        }

        return (firstIndexValue + bucketIndex) << precision;
    }

    /**
     * Returns the number of allocated buckets.
     *
     * @return the number of buckets used
     */
    public int getNumberOfBuckets()
    {
        return countPerBucket != null ? countPerBucket.length : 0;
    }

    /**
     * Returns the precision used. 1 means, we keep all values as is.
     * 2 means, we reduce the result precision by a factor of 2, 4 means by a factor of 4, etc.
     * 
     * @return the precision
     */
    public int getPrecision()
    {
        return 1 << precision;
    }

    /**
     * Merges another {@link RuntimeHistogram} into this instance.
     *
     * @param other
     *            the other histogram to merge, may be <code>null</code>
     */
    public void merge(final RuntimeHistogram other)
    {
        if (other == null || other.valueCount == 0)
        {
            return;
        }

        if (this.precision != other.precision)
        {
            throw new IllegalArgumentException("Cannot merge RuntimeHistogram with precision " + getPrecision() + " and " + other.getPrecision());
        }

        if (this.valueCount == 0)
        {
            this.firstIndexValue = other.firstIndexValue;
            this.lastIndexValue = other.lastIndexValue;
            this.valueCount = other.valueCount;
            this.countPerBucket = new int[other.countPerBucket.length];
            System.arraycopy(other.countPerBucket, 0, this.countPerBucket, 0, other.countPerBucket.length);
            return;
        }

        final int newFirstIndex = Math.min(this.firstIndexValue, other.firstIndexValue);
        final int newLastIndex = Math.max(this.lastIndexValue, other.lastIndexValue);
        final int newLength = newLastIndex - newFirstIndex + 1;

        if (newLength > this.countPerBucket.length || newFirstIndex < this.firstIndexValue)
        {
            final int[] newCountPerBucket = new int[newLength];
            System.arraycopy(this.countPerBucket, 0, newCountPerBucket, this.firstIndexValue - newFirstIndex, this.countPerBucket.length);
            this.countPerBucket = newCountPerBucket;
            this.firstIndexValue = newFirstIndex;
            this.lastIndexValue = newLastIndex;
        }

        final int otherOffset = other.firstIndexValue - this.firstIndexValue;
        for (int i = 0; i < other.countPerBucket.length; i++)
        {
            this.countPerBucket[otherOffset + i] += other.countPerBucket[i];
        }

        this.valueCount += other.valueCount;
    }
}
