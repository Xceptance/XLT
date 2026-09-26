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

import com.xceptance.xlt.report.util.misc.BitCompression;

/**
 * A {@link IntTimeSeriesEntry} stores the minimum/maximum/sum/count of all the sample values added, but can also reproduce a
 * rough approximation of the distinct values added.
 */
public class IntTimeSeriesEntry
{
    // The sum for later average calculation
    private long totalValue = 0;

    // how much data have we seen
    private int count = 0;

    // how much concurrent measurements do we truly have
    private int concurrentCount = 0;

    // how many errors have we seen
    private int errorCount = 0;

    // the min and max values seen
    private int maximum = Integer.MIN_VALUE;
    private int minimum = Integer.MAX_VALUE;

    /**
     * Holds an approximation of the distinct values added to this entry.
     * Stored in 128 bit-slots across two 64-bit primitives for minimal memory footprint.
     */
    private long distinctValuesLow = 0;
    private long distinctValuesHigh = 0;

    /**
     * The value scaling factor (power of 2).
     */
    private int distinctValuesScale = 0;

    /**
     * Constructor.
     */
    public IntTimeSeriesEntry()
    {
    }

    /**
     * Constructor.
     * 
     * @param firstValue
     *            the first value to add
     * @param failed
     *            whether the record failed
     */
    public IntTimeSeriesEntry(final int firstValue, final boolean failed)
    {
        updateValue(firstValue, failed);
    }

    /**
     * Copy constructor.
     * 
     * @param other
     *            the instance to copy
     */
    public IntTimeSeriesEntry(final IntTimeSeriesEntry other)
    {
        this.totalValue = other.totalValue;
        this.count = other.count;
        this.concurrentCount = other.concurrentCount;
        this.errorCount = other.errorCount;
        this.maximum = other.maximum;
        this.minimum = other.minimum;
        this.distinctValuesLow = other.distinctValuesLow;
        this.distinctValuesHigh = other.distinctValuesHigh;
        this.distinctValuesScale = other.distinctValuesScale;
    }

    /**
     * Adds the given sample value to this entry.
     * 
     * @param value
     *            the sample to add
     * @param failed
     *            whether the sample failed
     */
    public void updateValue(final int value, final boolean failed)
    {
        final int v = value < 0 ? 0 : value;

        this.count++;
        this.errorCount += failed ? 1 : 0;
        this.totalValue += v;
        this.concurrentCount++;

        if (v > this.maximum)
        {
            this.maximum = v;
        }
        if (v < this.minimum)
        {
            this.minimum = v;
        }

        final int scaled = scaleIfNeeded(v);

        if (scaled < 64)
        {
            distinctValuesLow |= (1L << scaled);
        }
        else
        {
            distinctValuesHigh |= (1L << (scaled - 64));
        }
    }

    /**
     * Increase the concurrency count by one for this slot.
     */
    public void updateConcurrency()
    {
        this.concurrentCount++;
    }

    /**
     * Resets this entry to its initial empty state for object reuse.
     */
    public void clear()
    {
        this.totalValue = 0;
        this.count = 0;
        this.concurrentCount = 0;
        this.errorCount = 0;
        this.maximum = Integer.MIN_VALUE;
        this.minimum = Integer.MAX_VALUE;
        this.distinctValuesLow = 0;
        this.distinctValuesHigh = 0;
        this.distinctValuesScale = 0;
    }

    /**
     * Scales up the distinct values bitmap to the target scale.
     */
    private void scaleUp(final int targetScale)
    {
        while (distinctValuesScale < targetScale)
        {
            long l = BitCompression.combineAdjacentBits(distinctValuesLow);
            l = BitCompression.compressAndShiftOddBits(l);

            long h = BitCompression.combineAdjacentBits(distinctValuesHigh);
            h = BitCompression.compressAndShiftOddBits(h);

            distinctValuesLow = l | (h << 32);
            distinctValuesHigh = 0;

            distinctValuesScale++;
        }
    }

    /**
     * Scales the bitmap if the value exceeds the 128-slot capacity.
     */
    private int scaleIfNeeded(final int value)
    {
        int v = value >> distinctValuesScale;

        while (v >= 128)
        {
            scaleUp(distinctValuesScale + 1);
            v = value >> distinctValuesScale;
        }

        return v;
    }

    /**
     * Returns the sum of the values added.
     */
    public long getTotalValue()
    {
        return this.totalValue;
    }

    /**
     * Returns the average of the values added.
     */
    public int getAverageValue()
    {
        return this.count == 0 ? 0 : (int) (this.totalValue / this.count);
    }

    /**
     * Returns the maximum of the values added.
     */
    public int getMaximumValue()
    {
        return maximum == Integer.MIN_VALUE ? 0 : maximum;
    }

    /**
     * Returns the minimum of the values added.
     */
    public int getMinimumValue()
    {
        return minimum == Integer.MAX_VALUE ? 0 : minimum;
    }

    /**
     * Returns the number of errors added.
     */
    public int getErrorCount()
    {
        return this.errorCount;
    }

    /**
     * Returns an approximation of the distinct values added to this entry without object allocation overhead.
     */
    public double[] getValues()
    {
        final int bitCount = Long.bitCount(distinctValuesLow) + Long.bitCount(distinctValuesHigh);
        if (bitCount == 0)
        {
            return new double[0];
        }

        final double[] result = new double[bitCount];
        final long multiplier = 1L << distinctValuesScale;
        int idx = 0;

        long bits = distinctValuesLow;
        while (bits != 0)
        {
            final int bit = Long.numberOfTrailingZeros(bits);
            result[idx++] = (double) (multiplier * bit);
            bits &= bits - 1;
        }

        bits = distinctValuesHigh;
        while (bits != 0)
        {
            final int bit = Long.numberOfTrailingZeros(bits);
            result[idx++] = (double) (multiplier * (64 + bit));
            bits &= bits - 1;
        }

        return result;
    }

    /**
     * Returns the number of values added.
     */
    public int getCount()
    {
        return this.count;
    }

    /**
     * Returns the number of concurrent measurements added.
     */
    public int getConcurrentCount()
    {
        return this.concurrentCount;
    }

    /**
     * Merges another entry into this instance.
     */
    public IntTimeSeriesEntry merge(final IntTimeSeriesEntry item)
    {
        if (item == null)
        {
            return this;
        }
        if (item.count == 0 && item.concurrentCount == 0)
        {
            return this;
        }
        if (this.count == 0 && this.concurrentCount == 0)
        {
            this.totalValue = item.totalValue;
            this.count = item.count;
            this.concurrentCount = item.concurrentCount;
            this.errorCount = item.errorCount;
            this.maximum = item.maximum;
            this.minimum = item.minimum;
            this.distinctValuesLow = item.distinctValuesLow;
            this.distinctValuesHigh = item.distinctValuesHigh;
            this.distinctValuesScale = item.distinctValuesScale;
            return this;
        }
        if (item.count == 0)
        {
            this.concurrentCount = Math.max(this.concurrentCount, item.concurrentCount);
            return this;
        }
        if (this.count == 0)
        {
            this.totalValue = item.totalValue;
            this.count = item.count;
            this.concurrentCount = Math.max(this.concurrentCount, item.concurrentCount);
            this.errorCount = item.errorCount;
            this.maximum = item.maximum;
            this.minimum = item.minimum;
            this.distinctValuesLow = item.distinctValuesLow;
            this.distinctValuesHigh = item.distinctValuesHigh;
            this.distinctValuesScale = item.distinctValuesScale;
            return this;
        }

        if (item.distinctValuesScale > this.distinctValuesScale)
        {
            scaleUp(item.distinctValuesScale);
        }
        else if (item.distinctValuesScale < this.distinctValuesScale)
        {
            item.scaleUp(this.distinctValuesScale);
        }

        maximum = Math.max(maximum, item.maximum);
        minimum = Math.min(minimum, item.minimum);

        totalValue += item.totalValue;
        count += item.count;
        concurrentCount = Math.max(concurrentCount, item.concurrentCount);
        errorCount += item.errorCount;

        distinctValuesLow |= item.distinctValuesLow;
        distinctValuesHigh |= item.distinctValuesHigh;

        return this;
    }

    @Override
    public String toString()
    {
        return getCount() + " / " +
               getConcurrentCount() + " / " +
               getErrorCount() + " / " +
               getTotalValue() + " / " +
               getAverageValue() + " / " +
               getMinimumValue() + " / " + 
               getMaximumValue() + " / " + 
               Arrays.toString(getValues()) + "\n";
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        final IntTimeSeriesEntry other = (IntTimeSeriesEntry) obj;
        return count == other.count &&
               maximum == other.maximum &&
               minimum == other.minimum &&
               totalValue == other.totalValue &&
               errorCount == other.errorCount &&
               distinctValuesLow == other.distinctValuesLow &&
               distinctValuesHigh == other.distinctValuesHigh &&
               concurrentCount == other.concurrentCount &&
               distinctValuesScale == other.distinctValuesScale;
    }

    @Override
    public int hashCode()
    {
        int result = Long.hashCode(totalValue);
        result = 31 * result + count;
        result = 31 * result + concurrentCount;
        result = 31 * result + errorCount;
        result = 31 * result + maximum;
        result = 31 * result + minimum;
        result = 31 * result + Long.hashCode(distinctValuesLow);
        result = 31 * result + Long.hashCode(distinctValuesHigh);
        result = 31 * result + distinctValuesScale;
        return result;
    }
}
