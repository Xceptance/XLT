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
 * A fixed-capacity, self-scaling time series bucket array that maintains per-bucket summary statistics
 * ({@link IntMinMaxValue}: min, max, sum, count, and approximate distinct values) over an arbitrarily long test duration.
 *
 * <h2>How Self-Scaling Works</h2>
 * The backing array is allocated with {@code 2 * size} slots (where {@code size} typically matches the chart width in
 * pixels, e.g. 1024). Initially, each slot represents {@code scale = 1} second ({@code 2^scale2} where {@code scale2 = 0}).
 * <ul>
 *   <li><b>Appending forward in time ({@code second > firstSecond}):</b> As long as the target slot index
 *       {@code (second - firstSecond) >> scale2} exceeds the backing array capacity, {@link #shrink()} is called to
 *       merge adjacent slot pairs {@code (2k, 2k+1)} in-place, doubling {@link #scale} and halving the occupied slot
 *       count so the new timestamp fits without growing the array.</li>
 *   <li><b>Inserting earlier timestamps ({@code second < firstSecond}):</b> Out-of-order samples that precede
 *       {@link #firstSecond} first trigger {@link #shrink()} if the total span {@code (lastSecond - second) >> scale2}
 *       exceeds capacity, and then {@link #shift(int)} shifts existing entries rightward to make room at index 0.</li>
 *   <li><b>Grid alignment during {@link #shrink()}:</b> When {@code firstSecond} is not already aligned to the new,
 *       doubled {@code scale}, slot 0 sits in the upper half of its new coarse bucket and cannot be paired with slot 1
 *       (which belongs to the next coarse bucket). In that case, {@code offset = 1} leaves slot 0 untouched and pairs
 *       slots {@code (1, 2), (3, 4), ...} before snapping {@code firstSecond} down to the new scale boundary.</li>
 * </ul>
 */
public class IntMinMaxValueSet
{
    /**
     * The default logical value set size (backing array is allocated at {@code 2 * DEFAULT_SIZE}).
     */
    public static final int DEFAULT_SIZE = 1024;

    /**
     * The smallest scale-aligned time [s] for which a min/max value exists.
     */
    private int firstSecond;

    /**
     * The largest scale-aligned time [s] for which a min/max value exists.
     */
    private int lastSecond;

    /**
     * The maximum raw timestamp [ms] for which a value has been added to this set.
     */
    private long maximumTime;

    /**
     * The minimum raw timestamp [ms] for which a value has been added to this set.
     */
    private long minimumTime;

    /**
     * The number of seconds a single min/max slot represents. Always a power of 2 ({@code 1 << scale2}).
     */
    private int scale = 1;

    /**
     * Base-2 logarithm of {@link #scale} ({@code scale == 1 << scale2}), used to replace division and modulo
     * by {@code scale} with fast bit shifts.
     */
    private int scale2 = 0;

    /**
     * Internal backing array capacity ({@code 2 * logicalSize}), doubled so that after a {@link #shrink()} pass
     * at least {@code logicalSize} buckets remain populated.
     */
    private final int size;

    /**
     * The total number of sample values added to this value set.
     */
    private long valueCount;

    /**
     * The per-bucket min/max accumulators maintained by this value set.
     */
    private final IntMinMaxValue[] values;

    /**
     * Creates a {@link IntMinMaxValueSet} instance with a logical size of {@link #DEFAULT_SIZE}.
     */
    public IntMinMaxValueSet()
    {
        this(DEFAULT_SIZE);
    }

    /**
     * Creates a {@link IntMinMaxValueSet} instance with the specified logical size.
     *
     * @param size
     *            the logical size (number of buckets guaranteed after shrinking)
     */
    public IntMinMaxValueSet(final int size)
    {
        // Double the size so at least 'size' min/max values remain even immediately after shrinking
        this.size = size << 1;
        values = new IntMinMaxValue[this.size];
    }

    /**
     * Adds a value for a given millisecond timestamp to this value set.
     *
     * @param time
     *            the timestamp in milliseconds
     * @param value
     *            the sample value
     */
    public void addOrUpdateValue(final long time, final int value)
    {
        // Align the timestamp's second to the current power-of-two bucket scale
        final int second = ((int) (time / 1000)) & ~(scale - 1);

        // Fast-path for the very first value added to this set
        if (valueCount == 0)
        {
            firstSecond = lastSecond = second;
            values[0] = new IntMinMaxValue(value);

            minimumTime = maximumTime = time;
            valueCount = 1;

            return;
        }

        final int index = resolveSlotIndex(second);
        final IntMinMaxValue item = values[index];
        if (item != null)
        {
            item.updateValue(value);
        }
        else
        {
            values[index] = new IntMinMaxValue(value);
        }

        valueCount++;
        minimumTime = Math.min(minimumTime, time);
        maximumTime = Math.max(maximumTime, time);
    }

    /**
     * Adjusts the sliding window ({@link #firstSecond}, {@link #lastSecond}, {@link #scale}) by shrinking and/or
     * shifting the backing array as needed so that {@code second} maps to a valid slot index in {@code [0, size)}.
     *
     * @param second
     *            the scale-aligned second to accommodate
     * @return the resolved slot index in {@link #values}
     */
    private int resolveSlotIndex(int second)
    {
        if (second != firstSecond)
        {
            if (second > firstSecond)
            {
                // Shrink (double scale) until the new forward second fits within the array capacity
                while (((second - firstSecond) >> scale2) >= size)
                {
                    scale = scale << 1;
                    scale2++;

                    shrink();

                    final int mask = ~(scale - 1);
                    second = second & mask;
                    firstSecond = firstSecond & mask;
                    lastSecond = lastSecond & mask;
                }

                lastSecond = Math.max(lastSecond, second);
            }
            else
            {
                // Shrink (double scale) until the distance from the new earlier second to lastSecond fits in capacity
                while (((lastSecond - second) >> scale2) >= size)
                {
                    scale = scale << 1;
                    scale2++;

                    shrink();

                    final int mask = ~(scale - 1);
                    second = second & mask;
                    firstSecond = firstSecond & mask;
                    lastSecond = lastSecond & mask;
                }

                // Shift existing buckets right if the new second still precedes firstSecond after any shrinking
                if (second < firstSecond)
                {
                    final int indexDiff = (firstSecond - second) >> scale2;
                    shift(indexDiff);
                    firstSecond = second;
                }
            }
        }

        return (second - firstSecond) >> scale2;
    }

    /**
     * Returns the smallest second for which a min/max value exists in this set, as milliseconds since the epoch.
     * <p>
     * Note the unit: this returns milliseconds, unlike {@link ValueSet#getFirstSecond()} which returns seconds.
     *
     * @return the first second, in milliseconds
     */
    public long getFirstSecond()
    {
        if (valueCount == 0)
        {
            throw new IllegalStateException("No first second available as no values have been added so far.");
        }

        // 1000L, not 1000: firstSecond is an int, so int arithmetic overflows for any real epoch timestamp
        return firstSecond * 1000L;
    }

    /**
     * Returns the maximum time [ms] for which a value has been added to this set.
     *
     * @return the maximum time
     */
    public long getMaximumTime()
    {
        if (valueCount == 0)
        {
            throw new IllegalStateException("No maximum time available as no values have been added so far.");
        }

        return maximumTime;
    }

    /**
     * Returns the minimum time [ms] for which a value has been added to this set.
     *
     * @return the minimum time
     */
    public long getMinimumTime()
    {
        if (valueCount == 0)
        {
            throw new IllegalStateException("No minimum time available as no values have been added so far.");
        }

        return minimumTime;
    }

    /**
     * Returns the number of seconds a single min/max value represents. Always a power of 2.
     *
     * @return the scale
     */
    public int getScale()
    {
        return scale;
    }

    /**
     * Merges another {@link IntMinMaxValueSet} into this instance.
     *
     * @param other
     *            the other min-max value set to merge, may be {@code null}
     */
    public void merge(final IntMinMaxValueSet other)
    {
        if (other == null || other.valueCount == 0)
        {
            return;
        }

        if (this.valueCount == 0)
        {
            this.scale = other.scale;
            this.scale2 = other.scale2;
            this.minimumTime = other.minimumTime;
            this.maximumTime = other.maximumTime;
        }
        else
        {
            // If other is on a coarser scale, scale this instance up until scales match
            while (this.scale < other.scale)
            {
                this.scale = this.scale << 1;
                this.scale2++;

                shrink();

                final int mask = ~(this.scale - 1);
                this.firstSecond = this.firstSecond & mask;
                this.lastSecond = this.lastSecond & mask;
            }

            this.minimumTime = Math.min(this.minimumTime, other.minimumTime);
            this.maximumTime = Math.max(this.maximumTime, other.maximumTime);
        }

        final int otherLength = ((other.lastSecond - other.firstSecond) >> other.scale2) + 1;
        for (int i = 0; i < otherLength; i++)
        {
            final IntMinMaxValue otherItem = other.values[i];
            if (otherItem != null)
            {
                final int sec = other.firstSecond + (i << other.scale2);
                addOrUpdateItem(sec, otherItem);
            }
        }
    }

    /**
     * Adds or merges a non-null {@link IntMinMaxValue} item for a specific second into this value set.
     *
     * @param second
     *            the timestamp in seconds
     * @param newItem
     *            the non-null item to merge
     */
    private void addOrUpdateItem(int second, final IntMinMaxValue newItem)
    {
        second = second & ~(scale - 1);

        if (valueCount == 0)
        {
            firstSecond = lastSecond = second;
            values[0] = new IntMinMaxValue(newItem);
            valueCount = newItem.getValueCount();
            return;
        }

        final int index = resolveSlotIndex(second);
        final IntMinMaxValue item = values[index];
        if (item != null)
        {
            item.merge(newItem);
        }
        else
        {
            values[index] = new IntMinMaxValue(newItem);
        }

        valueCount += newItem.getValueCount();
    }

    /**
     * Returns the logical size of this value set.
     *
     * @return the logical size
     */
    public int getSize()
    {
        return size >> 1;
    }

    /**
     * Returns the total number of values added to this value set.
     *
     * @return the number of values
     */
    public long getValueCount()
    {
        return valueCount;
    }

    /**
     * Returns a copy of the occupied range of min/max values maintained by this set, from {@link #firstSecond}
     * to {@link #lastSecond} inclusive.
     *
     * @return the min/max values array
     */
    public IntMinMaxValue[] getValues()
    {
        if (valueCount == 0)
        {
            return new IntMinMaxValue[0];
        }

        final int length = ((lastSecond - firstSecond) >> scale2) + 1;
        final IntMinMaxValue[] copy = new IntMinMaxValue[length];
        System.arraycopy(values, 0, copy, 0, length);

        return copy;
    }

    /**
     * Shifts the content of the min/max value array to the right by {@code indexDiff} slots and clears the
     * vacated leading slots.
     *
     * @param indexDiff
     *            the number of slots to shift right
     */
    private void shift(final int indexDiff)
    {
        System.arraycopy(values, 0, values, indexDiff, size - indexDiff);
        Arrays.fill(values, 0, indexDiff, null);
    }

    /**
     * Compacts the backing array by merging adjacent pairs of min/max slots into single slots of doubled time scale.
     */
    private void shrink()
    {
        // Note: scale has already been doubled by the caller before shrink() is invoked.
        // If firstSecond is not aligned to the new scale, slot 0 belongs to the upper half of the first
        // new coarse bucket and stands alone, while pairing starts at slot 1 (offset = 1).
        final int offset = ((firstSecond & (scale - 1)) != 0) ? 1 : 0;

        int i;
        int j = offset;
        for (i = offset; i < size - 1; i += 2)
        {
            final IntMinMaxValue v1 = values[i];
            final IntMinMaxValue v2 = values[i + 1];
            final IntMinMaxValue rv = (v1 != null) ? v1.merge(v2) : v2;

            values[i] = null;
            values[i + 1] = null;
            values[j++] = rv;
        }

        // When offset == 1 and size is even, one trailing slot at index (size - 1) remains unpaired
        if (i < size)
        {
            values[j] = values[i];
            values[i] = null;
        }
    }
}
