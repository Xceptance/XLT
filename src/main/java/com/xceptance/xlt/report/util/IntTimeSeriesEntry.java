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

    /**
     * The maximum number of errors observed in any single 1-second interval aggregated into this entry.
     * When scale is 1, this equals errorCount. When scale > 1 (condensed entry), this preserves the
     * peak instantaneous 1-second error burst observed within the entire aggregated window.
     */
    private int maxErrorCount = 0;

    /**
     * The maximum number of completions (throughput) observed in any single 1-second interval aggregated into this entry.
     * When scale is 1, this equals count. When scale > 1 (condensed entry), this preserves the
     * peak instantaneous 1-second throughput observed within the entire aggregated window.
     */
    private int maxCount = 0;

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
        this.maxErrorCount = other.maxErrorCount;
        this.maxCount = other.maxCount;
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
        if (this.count > this.maxCount)
        {
            this.maxCount = this.count;
        }

        if (failed)
        {
            this.errorCount++;
            if (this.errorCount > this.maxErrorCount)
            {
                this.maxErrorCount = this.errorCount;
            }
        }

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

        // Fast-path: inline bitmap update without method call overhead when value fits in current scale
        final int scaled = v >> distinctValuesScale;
        if (scaled < 128)
        {
            if (scaled < 64)
            {
                distinctValuesLow |= (1L << scaled);
            }
            else
            {
                distinctValuesHigh |= (1L << (scaled - 64));
            }
        }
        else
        {
            final int neededScale = (32 - Integer.numberOfLeadingZeros(v)) - 7;
            scaleUp(neededScale);
            final int s = v >> distinctValuesScale;
            if (s < 64)
            {
                distinctValuesLow |= (1L << s);
            }
            else
            {
                distinctValuesHigh |= (1L << (s - 64));
            }
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
     * <p>
     * Every scale step halves the resolution by merging adjacent bits across the 128-bit
     * register (low 64-bit and high 64-bit words) using parallel bit compression.
     * 
     * @param targetScale
     *            the target scale factor to reach
     */
    private void scaleUp(final int targetScale)
    {
        // Fast-path: if no distinct values have been recorded yet, simply jump to targetScale
        // without executing bitwise compression loops over all-zero bitmasks.
        if (distinctValuesLow == 0L && distinctValuesHigh == 0L)
        {
            distinctValuesScale = targetScale;
            return;
        }

        // If high word is present, compress both words for the first step, folding high into low
        if (distinctValuesHigh != 0L && distinctValuesScale < targetScale)
        {
            long l = BitCompression.combineAdjacentBits(distinctValuesLow);
            l = BitCompression.compressAndShiftOddBits(l);

            long h = BitCompression.combineAdjacentBits(distinctValuesHigh);
            h = BitCompression.compressAndShiftOddBits(h);

            distinctValuesLow = l | (h << 32);
            distinctValuesHigh = 0L;

            distinctValuesScale++;
        }

        // High word is now 0, subsequent compressions only need to operate on low word
        while (distinctValuesScale < targetScale)
        {
            long l = BitCompression.combineAdjacentBits(distinctValuesLow);
            distinctValuesLow = BitCompression.compressAndShiftOddBits(l);

            distinctValuesScale++;
        }
    }

    /**
     * Scales the bitmap if the value exceeds the 128-slot capacity.
     * <p>
     * Calculates the required scale factor using a single leading zeros instruction
     * instead of invoking iterative compression passes.
     * 
     * @param value
     *            the sample value to scale
     * @return the scaled slot index fitting into [0..127]
     */
    private int scaleIfNeeded(final int value)
    {
        // Fast-path: if the distinct value bitmap is empty, compute target scale directly
        // without running iterative scaling loops.
        if (distinctValuesLow == 0L && distinctValuesHigh == 0L)
        {
            final int neededScale = (32 - Integer.numberOfLeadingZeros(value)) - 7;
            distinctValuesScale = neededScale;
            return value >> neededScale;
        }

        int v = value >> distinctValuesScale;
        if (v >= 128)
        {
            final int neededScale = (32 - Integer.numberOfLeadingZeros(value)) - 7;
            scaleUp(neededScale);
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
     * Returns the maximum error count observed in any single 1-second slot aggregated into this entry.
     * For unconsolidated entries (scale == 1), this is identical to {@link #getErrorCount()}.
     * For consolidated entries (scale > 1), this preserves the peak instantaneous 1-second burst rate.
     * 
     * @return the maximum 1-second error count
     */
    public int getMaxErrorCount()
    {
        return this.maxErrorCount;
    }

    /**
     * Returns the maximum sample count (throughput) observed in any single 1-second slot aggregated into this entry.
     * For unconsolidated entries (scale == 1), this is identical to {@link #getCount()}.
     * For consolidated entries (scale > 1), this preserves the peak instantaneous 1-second throughput.
     * 
     * @return the maximum 1-second sample count
     */
    public int getMaxCount()
    {
        return this.maxCount;
    }

    /**
    /**
     * Merges another entry representing the SAME time interval into this instance (parallel aggregation across threads).
     * <p>
     * <b>Thread-Merge Semantics:</b>
     * When merging two time series representing the identical timeline (such as parser threads merging into a global
     * processor), both entries represent activity that occurred simultaneously during the same time window.
     * Therefore, total samples, total execution time, total errors, and instantaneous 1-second burst counts are
     * additive across the threads.
     * 
     * @param other
     *            the other entry representing the same time interval to merge
     * @return this instance for method chaining
     */
    public IntTimeSeriesEntry merge(final IntTimeSeriesEntry other)
    {
        if (other == null)
        {
            return this;
        }
        if (other.count == 0 && other.concurrentCount == 0)
        {
            return this;
        }
        if (this.count == 0 && this.concurrentCount == 0)
        {
            this.totalValue = other.totalValue;
            this.count = other.count;
            this.concurrentCount = other.concurrentCount;
            this.errorCount = other.errorCount;
            this.maxErrorCount = other.maxErrorCount;
            this.maxCount = other.maxCount;
            this.maximum = other.maximum;
            this.minimum = other.minimum;
            this.distinctValuesLow = other.distinctValuesLow;
            this.distinctValuesHigh = other.distinctValuesHigh;
            this.distinctValuesScale = other.distinctValuesScale;
            return this;
        }
        if (other.count == 0)
        {
            this.concurrentCount = Math.max(this.concurrentCount, other.concurrentCount);
            return this;
        }
        if (this.count == 0)
        {
            this.totalValue = other.totalValue;
            this.count = other.count;
            this.concurrentCount = Math.max(this.concurrentCount, other.concurrentCount);
            this.errorCount = other.errorCount;
            this.maxErrorCount = other.maxErrorCount;
            this.maxCount = other.maxCount;
            this.maximum = other.maximum;
            this.minimum = other.minimum;
            this.distinctValuesLow = other.distinctValuesLow;
            this.distinctValuesHigh = other.distinctValuesHigh;
            this.distinctValuesScale = other.distinctValuesScale;
            return this;
        }

        if (other.distinctValuesScale > this.distinctValuesScale)
        {
            scaleUp(other.distinctValuesScale);
        }
        else if (other.distinctValuesScale < this.distinctValuesScale)
        {
            other.scaleUp(this.distinctValuesScale);
        }

        maximum = Math.max(maximum, other.maximum);
        minimum = Math.min(minimum, other.minimum);

        totalValue += other.totalValue;
        count += other.count;
        concurrentCount = Math.max(concurrentCount, other.concurrentCount);
        errorCount += other.errorCount;
        maxErrorCount = Math.min(errorCount, maxErrorCount + other.maxErrorCount);
        maxCount = Math.min(count, maxCount + other.maxCount);

        distinctValuesLow |= other.distinctValuesLow;
        distinctValuesHigh |= other.distinctValuesHigh;

        return this;
    }

    /**
     * Condenses an adjacent chronological entry into this instance (temporal aggregation downsampling).
     * <p>
     * <b>Resolution Condensing Semantics:</b>
     * Unlike {@link #merge(IntTimeSeriesEntry)} which merges concurrent data for the same time window,
     * condensing combines consecutive adjacent time intervals into a wider interval (e.g. two 1-second intervals
     * into a single 2-second interval during chart resolution downsampling).
     * <p>
     * While total sample count and total errors are summed over the wider window, peak instantaneous 1-second burst
     * rates (peak 1-second error bursts and peak 1-second throughput) and peak active concurrency are preserved
     * by taking the {@link Math#max} over the adjacent chronological intervals.
     * 
     * @param adjacent
     *            the adjacent chronological entry to consolidate into this entry
     * @return this instance for method chaining
     */
    public IntTimeSeriesEntry condenseWith(final IntTimeSeriesEntry adjacent)
    {
        if (adjacent == null)
        {
            return this;
        }
        if (adjacent.count == 0 && adjacent.concurrentCount == 0)
        {
            return this;
        }
        if (this.count == 0 && this.concurrentCount == 0)
        {
            this.totalValue = adjacent.totalValue;
            this.count = adjacent.count;
            this.concurrentCount = adjacent.concurrentCount;
            this.errorCount = adjacent.errorCount;
            this.maxErrorCount = adjacent.maxErrorCount;
            this.maxCount = adjacent.maxCount;
            this.maximum = adjacent.maximum;
            this.minimum = adjacent.minimum;
            this.distinctValuesLow = adjacent.distinctValuesLow;
            this.distinctValuesHigh = adjacent.distinctValuesHigh;
            this.distinctValuesScale = adjacent.distinctValuesScale;
            return this;
        }
        if (adjacent.count == 0)
        {
            this.concurrentCount = Math.max(this.concurrentCount, adjacent.concurrentCount);
            return this;
        }
        if (this.count == 0)
        {
            this.totalValue = adjacent.totalValue;
            this.count = adjacent.count;
            this.concurrentCount = Math.max(this.concurrentCount, adjacent.concurrentCount);
            this.errorCount = adjacent.errorCount;
            this.maxErrorCount = adjacent.maxErrorCount;
            this.maxCount = adjacent.maxCount;
            this.maximum = adjacent.maximum;
            this.minimum = adjacent.minimum;
            this.distinctValuesLow = adjacent.distinctValuesLow;
            this.distinctValuesHigh = adjacent.distinctValuesHigh;
            this.distinctValuesScale = adjacent.distinctValuesScale;
            return this;
        }

        if (adjacent.distinctValuesScale > this.distinctValuesScale)
        {
            scaleUp(adjacent.distinctValuesScale);
        }
        else if (adjacent.distinctValuesScale < this.distinctValuesScale)
        {
            adjacent.scaleUp(this.distinctValuesScale);
        }

        maximum = Math.max(maximum, adjacent.maximum);
        minimum = Math.min(minimum, adjacent.minimum);

        totalValue += adjacent.totalValue;
        count += adjacent.count;
        concurrentCount = Math.max(concurrentCount, adjacent.concurrentCount);
        errorCount += adjacent.errorCount;
        maxErrorCount = Math.max(maxErrorCount, adjacent.maxErrorCount);
        maxCount = Math.max(maxCount, adjacent.maxCount);

        distinctValuesLow |= adjacent.distinctValuesLow;
        distinctValuesHigh |= adjacent.distinctValuesHigh;

        return this;
    }

    @Override
    public String toString()
    {
        return getCount() + " / " +
               getConcurrentCount() + " / " +
               getErrorCount() + " / " +
               getMaxErrorCount() + " / " +
               getMaxCount() + " / " +
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
               maxErrorCount == other.maxErrorCount &&
               maxCount == other.maxCount &&
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
        result = 31 * result + maxErrorCount;
        result = 31 * result + maxCount;
        result = 31 * result + maximum;
        result = 31 * result + minimum;
        result = 31 * result + Long.hashCode(distinctValuesLow);
        result = 31 * result + Long.hashCode(distinctValuesHigh);
        result = 31 * result + distinctValuesScale;
        return result;
    }
}
