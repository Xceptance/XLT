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

import java.util.Objects;

import com.xceptance.xlt.report.util.misc.BitCompression;

/**
 * Stores the summary statistics (minimum, maximum, accumulated sum, and count) of integer sample values
 * aggregated into a time bucket, along with a fixed-size low-precision approximation of the distinct values seen.
 *
 * <h2>Why the Low-Precision Bitset is Inlined</h2>
 * Previously, each {@link IntMinMaxValue} held a reference to an {@link IntLowPrecisionValueSet}, which in turn
 * allocated a Lucene {@code OpenBitSet} and a 4-element {@code long[]} array—resulting in 4 separate heap objects
 * (128 bytes total) for every occupied second in an {@link IntMinMaxValueSet}. Across hundreds of request timers
 * and agent metrics in large load tests, millions of these 4-object chains were allocated and retained.
 * <p>
 * Because the default low-precision bucket count is fixed at 256 buckets, the entire bitset fits in exactly four
 * 64-bit {@code long} fields ({@link #w0}, {@link #w1}, {@link #w2}, {@link #w3}) plus a power-of-two shift
 * {@link #scale}. Inlining these primitives directly into {@link IntMinMaxValue} eliminates 3 out of 4 heap objects
 * per slot (100% of {@link IntLowPrecisionValueSet}, {@code OpenBitSet}, and {@code long[]} allocations) and allows
 * scaling, merging, and copying to execute entirely in CPU registers via SWAR bit compression.
 */
public class IntMinMaxValue
{
    /**
     * Number of buckets in the inline low-precision value set (4 x 64-bit words = 256 bits).
     */
    private static final int BUCKET_COUNT = 256;

    private long accumulatedValue;

    private int maximum = Integer.MIN_VALUE;

    private int minimum = Integer.MAX_VALUE;

    private int valueCount;

    /**
     * Power-of-two shift exponent ({@code 0..24}) of the inline 256-bucket low-precision value set.
     * Bucket {@code b} in {@code [0..255]} represents the approximated sample value {@code b << scale}.
     */
    private int scale;

    /**
     * Inline 256-bit (4 x 64-bit) bitset storing the occupied buckets {@code [0..255]} of the low-precision
     * value set: {@code w0} holds buckets {@code 0..63}, {@code w1} holds {@code 64..127}, {@code w2} holds
     * {@code 128..191}, and {@code w3} holds {@code 192..255}.
     */
    private long w0;
    private long w1;
    private long w2;
    private long w3;

    /**
     * Creates a new {@link IntMinMaxValue} initialized with a single sample value.
     * 
     * @param value
     *            the first sample value to record
     */
    public IntMinMaxValue(final int value)
    {
        addBitValue(value);

        accumulatedValue = value;
        maximum = value;
        minimum = value;
        valueCount = 1;
    }

    /**
     * Creates a copy of the given {@link IntMinMaxValue}.
     * 
     * @param other
     *            the instance to copy from (may be {@code null})
     */
    public IntMinMaxValue(final IntMinMaxValue other)
    {
        if (other != null)
        {
            accumulatedValue = other.accumulatedValue;
            maximum = other.maximum;
            minimum = other.minimum;
            valueCount = other.valueCount;
            scale = other.scale;
            w0 = other.w0;
            w1 = other.w1;
            w2 = other.w2;
            w3 = other.w3;
        }
    }

    /**
     * Creates a new {@link IntMinMaxValue} from an {@link IntTimeSeriesEntry}.
     *
     * @param entry
     *            the time series entry to convert (may be {@code null})
     */
    public IntMinMaxValue(final IntTimeSeriesEntry entry)
    {
        if (entry != null && entry.getCount() > 0)
        {
            accumulatedValue = entry.getTotalValue();
            maximum = entry.getMaximumValue();
            minimum = entry.getMinimumValue();
            valueCount = (int) entry.getCount();
            for (final double v : entry.getValues())
            {
                addBitValue((int) v);
            }
        }
    }

    /**
     * Returns the sum of the values added to this min-max value.
     * 
     * @return the accumulated value
     */
    public long getAccumulatedValue()
    {
        return accumulatedValue;
    }

    /**
     * Returns the integer-truncated average of the values added to this min-max value.
     * 
     * @return the average value
     */
    public int getAverageValue()
    {
        return (int) (accumulatedValue / valueCount);
    }

    /**
     * Returns the maximum of the values added to this min-max value, or {@code 0} if no values have been added.
     * 
     * @return the maximum value
     */
    public int getMaximumValue()
    {
        return maximum == Integer.MIN_VALUE ? 0 : maximum;
    }

    /**
     * Returns the minimum of the values added to this min-max value, or {@code 0} if no values have been added.
     * 
     * @return the minimum value
     */
    public int getMinimumValue()
    {
        return minimum == Integer.MAX_VALUE ? 0 : minimum;
    }

    /**
     * Returns the average of the values added to this min-max value.
     * 
     * @return the average value
     */
    public int getValue()
    {
        return getAverageValue();
    }

    /**
     * Returns an ascending array of approximated distinct non-negative values added to this min-max value.
     * 
     * @return the approximated distinct values
     */
    public double[] getValues()
    {
        final int count = Long.bitCount(w0) + Long.bitCount(w1) + Long.bitCount(w2) + Long.bitCount(w3);
        final double[] values = new double[count];

        int idx = 0;
        idx = extractWordBits(w0, 0, scale, values, idx);
        idx = extractWordBits(w1, 64, scale, values, idx);
        idx = extractWordBits(w2, 128, scale, values, idx);
        extractWordBits(w3, 192, scale, values, idx);

        return values;
    }

    /**
     * Extracts all set bits from a 64-bit word and writes their scaled values {@code (baseBit + bit) << scale}
     * into the destination array in ascending order using Kernighan's bit-clearing trick {@code word &= (word - 1)}.
     */
    private static int extractWordBits(long word, final int baseBit, final int scale, final double[] dest, int idx)
    {
        while (word != 0L)
        {
            final int bit = baseBit + Long.numberOfTrailingZeros(word);
            dest[idx++] = bit << scale;
            word &= (word - 1L);
        }
        return idx;
    }

    /**
     * Returns the number of values added to this min-max value.
     * 
     * @return the value count
     */
    public int getValueCount()
    {
        return valueCount;
    }

    /**
     * Merges the data of the given min-max value into this min-max value in-place.
     * 
     * @param item
     *            the other value to merge (may be {@code null})
     * @return this instance
     */
    IntMinMaxValue merge(final IntMinMaxValue item)
    {
        if (item != null)
        {
            maximum = Math.max(maximum, item.maximum);
            minimum = Math.min(minimum, item.minimum);

            accumulatedValue += item.accumulatedValue;
            valueCount += item.valueCount;

            mergeBitSet(item);
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "" + getValue() + "/" + getAccumulatedValue() + "/" + getMinimumValue() + "/" + getMaximumValue() + "/" + getValueCount();
    }

    /**
     * Adds the given sample value to this min-max value.
     * 
     * @param sample
     *            the sample to add
     */
    public void updateValue(final int sample)
    {
        addBitValue(sample);

        maximum = Math.max(maximum, sample);
        minimum = Math.min(minimum, sample);

        accumulatedValue += sample;
        valueCount++;
    }

    /**
     * Records a non-negative sample in the inline 256-bucket low-precision bitset, scaling the bitset range up
     * as needed when the value exceeds the current capacity {@code 256 << scale}.
     */
    private void addBitValue(int value)
    {
        // Negative values are not represented in the low-precision bitset
        if (value < 0)
        {
            return;
        }

        value >>= scale;
        while (value >= BUCKET_COUNT)
        {
            scaleBitSet();
            value >>= 1;
        }

        setBit(value);
    }

    /**
     * Sets a single bucket bit {@code bitIndex} in {@code [0..255]} across the four 64-bit words.
     */
    private void setBit(final int bitIndex)
    {
        // Note: Java's long shift operator (1L << bitIndex) automatically masks the shift distance by & 63
        final long mask = 1L << bitIndex;
        switch (bitIndex >> 6)
        {
            case 0:
                w0 |= mask;
                break;
            case 1:
                w1 |= mask;
                break;
            case 2:
                w2 |= mask;
                break;
            default:
                w3 |= mask;
                break;
        }
    }

    /**
     * Doubles the value range of the inline 256-bucket bitset by merging adjacent bit pairs {@code (2k, 2k+1)}
     * into bit {@code k} using SWAR parallel bit compression and clearing the upper 128 bits.
     */
    private void scaleBitSet()
    {
        scale++;
        w0 = (BitCompression.compressAndShiftOddBits(BitCompression.combineAdjacentBits(w0)) & 0xFFFFFFFFL)
             | (BitCompression.compressAndShiftOddBits(BitCompression.combineAdjacentBits(w1)) << 32);
        w1 = (BitCompression.compressAndShiftOddBits(BitCompression.combineAdjacentBits(w2)) & 0xFFFFFFFFL)
             | (BitCompression.compressAndShiftOddBits(BitCompression.combineAdjacentBits(w3)) << 32);
        w2 = 0L;
        w3 = 0L;
    }

    /**
     * Aligns the scales of {@code this} and {@code other} and ORs their 256-bit bucket masks together without
     * mutating {@code other}.
     */
    private void mergeBitSet(final IntMinMaxValue other)
    {
        while (this.scale < other.scale)
        {
            this.scaleBitSet();
        }

        long ow0 = other.w0;
        long ow1 = other.w1;
        long ow2 = other.w2;
        long ow3 = other.w3;
        for (int s = other.scale; s < this.scale; s++)
        {
            ow0 = (BitCompression.compressAndShiftOddBits(BitCompression.combineAdjacentBits(ow0)) & 0xFFFFFFFFL)
                  | (BitCompression.compressAndShiftOddBits(BitCompression.combineAdjacentBits(ow1)) << 32);
            ow1 = (BitCompression.compressAndShiftOddBits(BitCompression.combineAdjacentBits(ow2)) & 0xFFFFFFFFL)
                  | (BitCompression.compressAndShiftOddBits(BitCompression.combineAdjacentBits(ow3)) << 32);
            ow2 = 0L;
            ow3 = 0L;
        }

        this.w0 |= ow0;
        this.w1 |= ow1;
        this.w2 |= ow2;
        this.w3 |= ow3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(accumulatedValue, maximum, minimum, valueCount, scale, w0, w1, w2, w3);
    }

    /**
     * {@inheritDoc}
     */
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
        final IntMinMaxValue other = (IntMinMaxValue) obj;
        // Note: scale is a deterministic function of maximum and therefore implicitly verified by maximum == other.maximum
        return accumulatedValue == other.accumulatedValue
               && maximum == other.maximum
               && minimum == other.minimum
               && valueCount == other.valueCount
               && w0 == other.w0
               && w1 == other.w1
               && w2 == other.w2
               && w3 == other.w3;
    }
}
