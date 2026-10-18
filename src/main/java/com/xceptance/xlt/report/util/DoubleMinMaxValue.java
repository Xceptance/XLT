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
 * Stores the summary statistics (minimum, maximum, accumulated sum, and count) of {@code double} sample values
 * aggregated into a time bucket, along with a fixed-size 128-bucket low-precision approximation of the distinct
 * values seen.
 *
 * <h2>Why the 128-Bucket Bitset is Inlined</h2>
 * Previously, each {@link DoubleMinMaxValue} allocated a {@link DoubleLowPrecisionValueSet}, which in turn
 * allocated an {@code OpenBitSet(128)} and a {@code long[2]} array—4 separate heap objects (144 bytes) per
 * occupied time slot in a {@link DoubleMinMaxValueSet}. Moreover, merging two {@link DoubleLowPrecisionValueSet}
 * instances allocated a temporary {@code double[]} via {@code getValues()} and scaled/shifted bits using
 * 128-iteration bit-by-bit loops.
 * <p>
 * Because the default bucket count for {@code double} low-precision sets is fixed at 128 buckets, the entire
 * bitset fits in two 64-bit {@code long} fields ({@link #bits0} for buckets {@code 0..63} and {@link #bits1}
 * for buckets {@code 64..127}). Inlining these fields directly into {@link DoubleMinMaxValue}:
 * <ul>
 *   <li>Eliminates 3 out of 4 heap objects per slot (zero {@link DoubleLowPrecisionValueSet}, {@code OpenBitSet},
 *       or {@code long[]} allocations).</li>
 *   <li>Replaces the 128-step bit-by-bit {@code scale()} loop with two SWAR {@link BitCompression} operations.</li>
 *   <li>Replaces the 128-step bit-by-bit {@code shift()} loop with a direct 128-bit word shift.</li>
 *   <li>Eliminates temporary {@code double[]} allocations during {@link #merge(DoubleMinMaxValue)} by iterating
 *       set bits directly from {@code bits0} and {@code bits1} in registers.</li>
 * </ul>
 * 
 * @see IntMinMaxValue
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class DoubleMinMaxValue
{
    /**
     * Number of buckets in the inline low-precision value set (2 x 64-bit words = 128 bits).
     */
    private static final int BUCKET_COUNT = 128;

    private double accumulatedValue;

    private double maximum;

    private double minimum;

    private int valueCount;

    /**
     * Current width of a single bucket in the inline 128-bucket low-precision value set.
     * Starts at {@link AbstractFixedSizeDoubleValueSet#INITIAL_BUCKET_WIDTH} ({@code 2^-13}) and doubles
     * whenever the value range needs to be scaled up.
     */
    private double bucketWidth = AbstractFixedSizeDoubleValueSet.INITIAL_BUCKET_WIDTH;

    /**
     * Lower boundary (inclusive) of bucket 0 in the inline 128-bucket low-precision value set.
     */
    private double rangeMin;

    /**
     * Upper boundary (exclusive) of the highest occupied bucket in the inline low-precision value set.
     */
    private double rangeMax;

    /**
     * Inline 128-bit bitset storing occupied buckets {@code [0..127]}: {@code bits0} holds buckets {@code 0..63}
     * and {@code bits1} holds buckets {@code 64..127}.
     */
    private long bits0;
    private long bits1;

    /**
     * Creates a new {@link DoubleMinMaxValue} initialized with a single sample value.
     * 
     * @param value
     *            the first sample value to record
     */
    public DoubleMinMaxValue(final double value)
    {
        accumulatedValue = value;
        maximum = value;
        minimum = value;
        valueCount = 1;

        // Initialize the 128-bucket low-precision range directly with bucket 0 occupied by 'value'
        rangeMin = value;
        rangeMax = Math.max(Double.MIN_VALUE, value + bucketWidth);
        bits0 = 1L;
    }

    /**
     * Creates a copy of the given {@link DoubleMinMaxValue}.
     * 
     * @param other
     *            the other instance to copy from
     */
    public DoubleMinMaxValue(final DoubleMinMaxValue other)
    {
        accumulatedValue = other.accumulatedValue;
        maximum = other.maximum;
        minimum = other.minimum;
        valueCount = other.valueCount;
        bucketWidth = other.bucketWidth;
        rangeMin = other.rangeMin;
        rangeMax = other.rangeMax;
        bits0 = other.bits0;
        bits1 = other.bits1;
    }

    /**
     * Package-private constructor used by {@link DoubleMinMaxValueSet#getValues()} to materialize a
     * {@link DoubleMinMaxValue} view from a primitive page slot on demand.
     */
    DoubleMinMaxValue(final double accumulatedValue, final int valueCount, final double minimum, final double maximum,
                      final double bucketWidth, final double rangeMin, final double rangeMax,
                      final long bits0, final long bits1)
    {
        this.accumulatedValue = accumulatedValue;
        this.valueCount = valueCount;
        this.minimum = minimum;
        this.maximum = maximum;
        this.bucketWidth = bucketWidth;
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
        this.bits0 = bits0;
        this.bits1 = bits1;
    }

    /**
     * @return the accumulatedValue
     */
    public double getAccumulatedValue()
    {
        return accumulatedValue;
    }

    /**
     * @return the average value
     */
    public double getAverageValue()
    {
        return accumulatedValue / valueCount;
    }

    /**
     * @return the maximum
     */
    public double getMaximumValue()
    {
        return maximum;
    }

    /**
     * @return the minimum
     */
    public double getMinimumValue()
    {
        return minimum;
    }

    /**
     * @return the average value
     */
    public double getValue()
    {
        return getAverageValue();
    }

    /**
     * Returns an ascending array of approximated distinct values added to this min-max value.
     * 
     * @return the approximated distinct values
     */
    public double[] getValues()
    {
        final int size = Long.bitCount(bits0) + Long.bitCount(bits1);
        final double[] values = new double[size];

        int idx = 0;
        idx = extractWordValues(bits0, 0, rangeMin, bucketWidth, values, idx);
        extractWordValues(bits1, 64, rangeMin, bucketWidth, values, idx);

        return values;
    }

    /**
     * Extracts all set bits from a 64-bit word and writes their approximated bucket values
     * {@code min + width * (baseBit + bit)} into {@code dest} in ascending order.
     */
    private static int extractWordValues(long word, final int baseBit, final double min, final double width,
                                         final double[] dest, int idx)
    {
        while (word != 0L)
        {
            final int bit = baseBit + Long.numberOfTrailingZeros(word);
            dest[idx++] = min + width * bit;
            word &= (word - 1L);
        }
        return idx;
    }

    /**
     * @return the valueCount
     */
    public int getValueCount()
    {
        return valueCount;
    }

    /**
     * Merges the given {@link DoubleMinMaxValue} into this instance in-place.
     *
     * @param item
     *            the other min-max value to merge (may be {@code null})
     * @return this instance
     */
    public DoubleMinMaxValue merge(final DoubleMinMaxValue item)
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
     * Adds a sample value to this min-max value.
     *
     * @param sample
     *            the sample value to record
     */
    public void updateValue(final double sample)
    {
        maximum = Math.max(maximum, sample);
        minimum = Math.min(minimum, sample);

        accumulatedValue += sample;
        valueCount++;

        addBitValue(sample);
    }

    /**
     * Classifies {@code value} into one of the 128 buckets of the inline low-precision set, scaling or
     * shifting the bucket range first if {@code value} falls outside {@code [rangeMin, rangeMin + 128 * bucketWidth)}.
     */
    private void addBitValue(final double value)
    {
        adjustRange(value);

        final double diff = Math.max(0.0, value - rangeMin);
        final int index = (int) Math.floor(diff / bucketWidth);

        rangeMax = Math.max(rangeMax, rangeMin + bucketWidth * (index + 1));

        if (index < 64)
        {
            bits0 |= (1L << index);
        }
        else
        {
            // Note: Java's long shift operator (1L << index) automatically masks the shift distance by & 63
            bits1 |= (1L << index);
        }
    }

    /**
     * Scales (doubles {@code bucketWidth}) and/or shifts {@code rangeMin} downward so that {@code value}
     * fits within the 128-bucket window without losing existing occupied buckets.
     */
    private void adjustRange(final double value)
    {
        while (value >= rangeMin + bucketWidth * BUCKET_COUNT
               || value < rangeMax - bucketWidth * BUCKET_COUNT)
        {
            bucketWidth = bucketWidth * 2;
            scaleBitSet();
        }

        if (value < rangeMin)
        {
            final double diff = Math.max(0.0, rangeMin - value);
            final int buckets = (int) Math.ceil(diff / bucketWidth);
            rangeMin = rangeMin - bucketWidth * buckets;
            shiftBitSet(buckets);
        }
    }

    /**
     * Merges adjacent pairs of bits {@code (2k, 2k+1)} into bit {@code k} across the 128-bit bitset using
     * SWAR bit compression, compressing {@code bits0} into the lower 32 bits of {@code bits0}, {@code bits1}
     * into the upper 32 bits of {@code bits0}, and clearing {@code bits1}.
     */
    private void scaleBitSet()
    {
        bits0 = IntMinMaxValue.compressWordPair(bits0, bits1);
        bits1 = 0L;
    }

    /**
     * Shifts the 128-bit bitset {@code (bits1:bits0)} left by {@code buckets} positions ({@code 1 <= buckets < 128}),
     * clearing the lower {@code buckets} bits.
     */
    private void shiftBitSet(final int buckets)
    {
        if (buckets < 64)
        {
            bits1 = (bits1 << buckets) | (bits0 >>> (64 - buckets));
            bits0 = bits0 << buckets;
        }
        else
        {
            // Note: Java's long shift operator (bits0 << buckets) automatically masks the shift distance by & 63
            bits1 = bits0 << buckets;
            bits0 = 0L;
        }
    }

    /**
     * Merges all occupied buckets from {@code other} into this instance's 128-bucket bitset without allocating
     * an intermediate {@code double[]} array.
     */
    private void mergeBitSet(final DoubleMinMaxValue other)
    {
        long w = other.bits0;
        while (w != 0L)
        {
            final int bit = Long.numberOfTrailingZeros(w);
            addBitValue(other.rangeMin + other.bucketWidth * bit);
            w &= (w - 1L);
        }

        w = other.bits1;
        while (w != 0L)
        {
            final int bit = 64 + Long.numberOfTrailingZeros(w);
            addBitValue(other.rangeMin + other.bucketWidth * bit);
            w &= (w - 1L);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(Double.doubleToLongBits(accumulatedValue),
                            Double.doubleToLongBits(maximum),
                            Double.doubleToLongBits(minimum),
                            valueCount,
                            Double.doubleToLongBits(bucketWidth),
                            Double.doubleToLongBits(rangeMin),
                            Double.doubleToLongBits(rangeMax),
                            bits0,
                            bits1);
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
        final DoubleMinMaxValue other = (DoubleMinMaxValue) obj;
        return Double.doubleToLongBits(accumulatedValue) == Double.doubleToLongBits(other.accumulatedValue)
               && Double.doubleToLongBits(maximum) == Double.doubleToLongBits(other.maximum)
               && Double.doubleToLongBits(minimum) == Double.doubleToLongBits(other.minimum)
               && valueCount == other.valueCount
               && Double.doubleToLongBits(rangeMin) == Double.doubleToLongBits(other.rangeMin)
               && bits0 == other.bits0
               && bits1 == other.bits1;
    }
}
