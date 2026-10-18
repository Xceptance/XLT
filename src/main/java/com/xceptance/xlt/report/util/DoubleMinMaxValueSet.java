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

import com.xceptance.common.util.ParameterCheckUtils;

/**
 * A fixed-capacity, self-scaling time series bucket array for {@link DoubleMinMaxValue} instances that maintains
 * per-bucket summary statistics (min, max, sum, count, and approximate distinct values) over an arbitrarily long test
 * duration.
 *
 * <h2>Why Sparse 64-Slot Primitive Pages Instead of {@code DoubleMinMaxValue[]}</h2>
 * <p>
 * Storing a {@code DoubleMinMaxValue[2 * size]} reference array and allocating a separate {@link DoubleMinMaxValue}
 * heap object for every occupied second creates significant wrapper object overhead and pointer chasing across worker
 * threads. Conversely, allocating flat primitive arrays of length {@code 2 * size} eagerly for every set would waste
 * memory on sparse series that only record a few samples over a multi-hour test.
 * </p>
 * <p>
 * Partitioning the {@code 2 * size} logical slots into lazily allocated 64-slot primitive {@link Page}s combines both
 * advantages:
 * </p>
 * <ul>
 *   <li><b>Zero Wrapper Object Churn:</b> {@link #addOrUpdateValue(long, double)}, {@link #shrink()},
 *       {@link #shift(int)}, and {@link #merge(DoubleMinMaxValueSet)} operate exclusively on primitive arrays inside
 *       {@link Page}, allocating zero {@link DoubleMinMaxValue} instances during ingestion and merging.</li>
 *   <li><b>Sparse Memory Footprint:</b> Empty sets allocate no pages at all, and sparse series only allocate the
 *       specific 64-slot pages that receive data.</li>
 *   <li><b>In-Place Page Reuse:</b> Clearing a slot during {@link #shift(int)} or {@link #shrink()} simply sets
 *       {@code page.counts[slot] = 0}, keeping already-allocated {@link Page} arrays in place for subsequent reuse.</li>
 * </ul>
 *
 * <h2>How Self-Scaling Works</h2>
 * The logical capacity is {@code 2 * size} slots (where {@code size} typically matches the chart width in
 * pixels, e.g. 1024). Initially, each slot represents {@code scale = 1} second ({@code 2^scale2} where {@code scale2 = 0}).
 * <ul>
 *   <li><b>Appending forward in time ({@code second > firstSecond}):</b> As long as the target slot index
 *       {@code (second - firstSecond) >> scale2} exceeds the backing capacity, {@link #shrink()} merges adjacent
 *       slot pairs {@code (2k, 2k+1)} in-place, doubling {@link #scale} and halving the occupied slot count.</li>
 *   <li><b>Inserting earlier timestamps ({@code second < firstSecond}):</b> Out-of-order samples that precede
 *       {@link #firstSecond} trigger {@link #shrink()} if the total span {@code (lastSecond - second) >> scale2}
 *       exceeds capacity, and then {@link #shift(int)} shifts existing entries rightward to make room at index 0.</li>
 *   <li><b>Grid alignment during {@link #shrink()}:</b> When {@code firstSecond} is not aligned to the new, doubled
 *       {@code scale}, slot 0 sits in the upper half of its new coarse bucket and stands alone while pairing starts
 *       at slot 1 ({@code offset = 1}).</li>
 * </ul>
 * 
 * @see IntMinMaxValueSet
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class DoubleMinMaxValueSet
{
    /**
     * The default logical value set size (backing capacity is {@code 2 * DEFAULT_SIZE}).
     */
    public static final int DEFAULT_SIZE = 1024;

    /**
     * Bit shift for dividing a slot index into a page index ({@code 2^6 = 64} slots per page).
     */
    private static final int PAGE_SHIFT = 6;

    /**
     * Number of slots per {@link Page}.
     */
    private static final int PAGE_SIZE = 1 << PAGE_SHIFT;

    /**
     * Bitmask for extracting the intra-page slot index ({@code index & 63}).
     */
    private static final int PAGE_MASK = PAGE_SIZE - 1;

    /**
     * Number of buckets in each slot's inline low-precision value set (2 &times; 64-bit words = 128 bits).
     */
    private static final int BITSET_BUCKET_COUNT = 128;

    /**
     * A 64-slot primitive page storing per-slot min, max, sum, count, and the 128-bucket low-precision value set.
     * Slot {@code s} is occupied if and only if {@code counts[s] > 0}.
     */
    private static final class Page
    {
        final int[] counts = new int[PAGE_SIZE];
        final double[] sums = new double[PAGE_SIZE];
        final double[] mins = new double[PAGE_SIZE];
        final double[] maxs = new double[PAGE_SIZE];
        final double[] bucketWidths = new double[PAGE_SIZE];
        final double[] rangeMins = new double[PAGE_SIZE];
        final double[] rangeMaxs = new double[PAGE_SIZE];
        final long[] words = new long[PAGE_SIZE << 1];

        void addOrUpdate(final int s, final double value)
        {
            if (counts[s] == 0)
            {
                counts[s] = 1;
                sums[s] = value;
                mins[s] = value;
                maxs[s] = value;

                final double width = AbstractFixedSizeDoubleValueSet.INITIAL_BUCKET_WIDTH;
                bucketWidths[s] = width;
                rangeMins[s] = value;
                rangeMaxs[s] = Math.max(Double.MIN_VALUE, value + width);

                final int wIdx = s << 1;
                words[wIdx] = 1L;
                words[wIdx + 1] = 0L;
            }
            else
            {
                counts[s]++;
                sums[s] += value;
                mins[s] = Math.min(mins[s], value);
                maxs[s] = Math.max(maxs[s], value);
                addBitValue(s, value);
            }
        }

        private void addBitValue(final int s, final double value)
        {
            double width = bucketWidths[s];
            double rMin = rangeMins[s];
            final double rMax = rangeMaxs[s];
            final int wIdx = s << 1;
            long b0 = words[wIdx];
            long b1 = words[wIdx + 1];

            while (value >= rMin + width * BITSET_BUCKET_COUNT
                   || value < rMax - width * BITSET_BUCKET_COUNT)
            {
                width = width * 2;
                b0 = IntMinMaxValue.compressWordPair(b0, b1);
                b1 = 0L;
            }

            if (value < rMin)
            {
                final double diff = Math.max(0.0, rMin - value);
                final int buckets = (int) Math.ceil(diff / width);
                rMin = rMin - width * buckets;
                if (buckets < 64)
                {
                    b1 = (b1 << buckets) | (b0 >>> (64 - buckets));
                    b0 = b0 << buckets;
                }
                else
                {
                    // Note: Java's long shift operator (b0 << buckets) automatically masks the shift distance by & 63
                    b1 = b0 << buckets;
                    b0 = 0L;
                }
            }

            final double diff = Math.max(0.0, value - rMin);
            final int index = (int) Math.floor(diff / width);

            bucketWidths[s] = width;
            rangeMins[s] = rMin;
            rangeMaxs[s] = Math.max(rMax, rMin + width * (index + 1));

            if (index < 64)
            {
                b0 |= (1L << index);
            }
            else
            {
                // Note: Java's long shift operator (1L << index) automatically masks the shift distance by & 63
                b1 |= (1L << index);
            }
            words[wIdx] = b0;
            words[wIdx + 1] = b1;
        }

        void copyFrom(final int dstSlot, final Page srcPage, final int srcSlot)
        {
            counts[dstSlot] = srcPage.counts[srcSlot];
            sums[dstSlot] = srcPage.sums[srcSlot];
            mins[dstSlot] = srcPage.mins[srcSlot];
            maxs[dstSlot] = srcPage.maxs[srcSlot];
            bucketWidths[dstSlot] = srcPage.bucketWidths[srcSlot];
            rangeMins[dstSlot] = srcPage.rangeMins[srcSlot];
            rangeMaxs[dstSlot] = srcPage.rangeMaxs[srcSlot];

            final int dstW = dstSlot << 1;
            final int srcW = srcSlot << 1;
            words[dstW] = srcPage.words[srcW];
            words[dstW + 1] = srcPage.words[srcW + 1];
        }

        void mergeFrom(final int dstSlot, final Page srcPage, final int srcSlot)
        {
            if (counts[dstSlot] == 0)
            {
                copyFrom(dstSlot, srcPage, srcSlot);
                return;
            }

            counts[dstSlot] += srcPage.counts[srcSlot];
            sums[dstSlot] += srcPage.sums[srcSlot];
            mins[dstSlot] = Math.min(mins[dstSlot], srcPage.mins[srcSlot]);
            maxs[dstSlot] = Math.max(maxs[dstSlot], srcPage.maxs[srcSlot]);

            final int srcW = srcSlot << 1;
            final double srcMin = srcPage.rangeMins[srcSlot];
            final double srcWidth = srcPage.bucketWidths[srcSlot];

            long w = srcPage.words[srcW];
            while (w != 0L)
            {
                final int bit = Long.numberOfTrailingZeros(w);
                addBitValue(dstSlot, srcMin + srcWidth * bit);
                w &= (w - 1L);
            }

            w = srcPage.words[srcW + 1];
            while (w != 0L)
            {
                final int bit = 64 + Long.numberOfTrailingZeros(w);
                addBitValue(dstSlot, srcMin + srcWidth * bit);
                w &= (w - 1L);
            }
        }

        DoubleMinMaxValue toMinMaxValue(final int s)
        {
            final int wIdx = s << 1;
            return new DoubleMinMaxValue(sums[s], counts[s], mins[s], maxs[s],
                                         bucketWidths[s], rangeMins[s], rangeMaxs[s],
                                         words[wIdx], words[wIdx + 1]);
        }
    }

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
     * Internal backing slot capacity ({@code 2 * logicalSize}), doubled so that after a {@link #shrink()} pass
     * at least {@code logicalSize} buckets remain populated.
     */
    private final int size;

    /**
     * The total number of sample values added to this value set.
     */
    private long valueCount;

    /**
     * Lazily allocated spine of 64-slot primitive {@link Page}s covering {@code [0, size)}.
     */
    private Page[] pages;

    /**
     * Creates a {@link DoubleMinMaxValueSet} instance with a logical size of {@link #DEFAULT_SIZE}.
     */
    public DoubleMinMaxValueSet()
    {
        this(DEFAULT_SIZE);
    }

    /**
     * Creates a {@link DoubleMinMaxValueSet} instance with the specified logical size.
     * 
     * @param size
     *            the logical size (must be greater than 0)
     */
    public DoubleMinMaxValueSet(final int size)
    {
        ParameterCheckUtils.isGreaterThan(size, 0, "size");

        // Double the size so at least 'size' min/max values remain even immediately after shrinking
        this.size = size << 1;
    }

    /**
     * Returns the {@link Page} at {@code pageIndex}, allocating both the outer page spine and the {@link Page}
     * lazily if not yet present.
     */
    private Page getOrCreatePage(final int pageIndex)
    {
        if (pages == null)
        {
            pages = new Page[(size + PAGE_MASK) >>> PAGE_SHIFT];
        }
        Page page = pages[pageIndex];
        if (page == null)
        {
            page = new Page();
            pages[pageIndex] = page;
        }
        return page;
    }

    /**
     * Returns {@code true} if the slot at {@code index} contains at least one sample.
     */
    private boolean isOccupied(final int index)
    {
        final Page page = pages[index >>> PAGE_SHIFT];
        return page != null && page.counts[index & PAGE_MASK] > 0;
    }

    /**
     * Moves the occupied slot at {@code srcIndex} to {@code dstIndex} and marks {@code srcIndex} as empty.
     */
    private void moveSlot(final int srcIndex, final int dstIndex)
    {
        final Page srcPage = pages[srcIndex >>> PAGE_SHIFT];
        final int srcSlot = srcIndex & PAGE_MASK;
        getOrCreatePage(dstIndex >>> PAGE_SHIFT).copyFrom(dstIndex & PAGE_MASK, srcPage, srcSlot);
        srcPage.counts[srcSlot] = 0;
    }

    /**
     * Adds a value for a given millisecond timestamp to this value set.
     * 
     * @param time
     *            the timestamp in milliseconds
     * @param value
     *            the sample value
     */
    public void addOrUpdateValue(final long time, final double value)
    {
        // Align the timestamp's second to the current power-of-two bucket scale
        final int second = ((int) (time / 1000)) & ~(scale - 1);

        // Fast-path for the very first value added to this set
        if (valueCount == 0)
        {
            firstSecond = lastSecond = second;
            getOrCreatePage(0).addOrUpdate(0, value);

            minimumTime = maximumTime = time;
            valueCount = 1;
            return;
        }

        final int index = resolveSlotIndex(second);
        getOrCreatePage(index >>> PAGE_SHIFT).addOrUpdate(index & PAGE_MASK, value);

        valueCount++;
        minimumTime = Math.min(minimumTime, time);
        maximumTime = Math.max(maximumTime, time);
    }

    /**
     * Adjusts the sliding window ({@link #firstSecond}, {@link #lastSecond}, {@link #scale}) by shrinking and/or
     * shifting the backing pages as needed so that {@code second} maps to a valid slot index in {@code [0, size)}.
     *
     * @param second
     *            the scale-aligned second to accommodate
     * @return the resolved slot index in {@code [0, size)}
     */
    private int resolveSlotIndex(int second)
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
            }

            lastSecond = Math.max(lastSecond, second);
        }
        else if (second < firstSecond)
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

        return (second - firstSecond) >> scale2;
    }

    /**
     * Returns the smallest second for which a min/max value exists in this set, as milliseconds since the epoch.
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
     * Merges another {@link DoubleMinMaxValueSet} into this instance without allocating intermediate
     * {@link DoubleMinMaxValue} objects.
     *
     * @param other
     *            the other min-max value set to merge, may be {@code null}
     */
    public void merge(final DoubleMinMaxValueSet other)
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
            if (other.isOccupied(i))
            {
                final int sec = other.firstSecond + (i << other.scale2);
                addOrUpdateSlot(sec, other.pages[i >>> PAGE_SHIFT], i & PAGE_MASK);
            }
        }
    }

    /**
     * Adds or merges an occupied slot {@code (srcPage, srcSlot)} at {@code second} into this value set.
     */
    private void addOrUpdateSlot(int second, final Page srcPage, final int srcSlot)
    {
        second = second & ~(scale - 1);

        if (valueCount == 0)
        {
            firstSecond = lastSecond = second;
            getOrCreatePage(0).copyFrom(0, srcPage, srcSlot);
            valueCount = srcPage.counts[srcSlot];
            return;
        }

        final int index = resolveSlotIndex(second);
        getOrCreatePage(index >>> PAGE_SHIFT).mergeFrom(index & PAGE_MASK, srcPage, srcSlot);
        valueCount += srcPage.counts[srcSlot];
    }

    /**
     * Materializes and returns a copy of the occupied range of min/max values maintained by this set,
     * from {@link #firstSecond} to {@link #lastSecond} inclusive. Unoccupied buckets are {@code null}.
     * 
     * @return the min/max values array
     */
    public DoubleMinMaxValue[] getValues()
    {
        if (valueCount == 0)
        {
            return new DoubleMinMaxValue[0];
        }

        final int length = ((lastSecond - firstSecond) >> scale2) + 1;
        final DoubleMinMaxValue[] copy = new DoubleMinMaxValue[length];
        for (int i = 0; i < length; i++)
        {
            if (isOccupied(i))
            {
                copy[i] = pages[i >>> PAGE_SHIFT].toMinMaxValue(i & PAGE_MASK);
            }
        }

        return copy;
    }

    /**
     * Shifts the content of the min/max value slots to the right by {@code indexDiff} positions and clears the
     * vacated leading slots.
     * 
     * @param indexDiff
     *            the number of slots to shift right
     */
    private void shift(final int indexDiff)
    {
        // Note: resolveSlotIndex() shrinks before calling shift(indexDiff), guaranteeing that
        // ((lastSecond - firstSecond) >> scale2) + indexDiff < size. Iterating downward from the highest
        // occupied slot moves every occupied entry right by indexDiff and clears its vacated source slot in-place.
        for (int src = (lastSecond - firstSecond) >> scale2; src >= 0; src--)
        {
            if (isOccupied(src))
            {
                moveSlot(src, src + indexDiff);
            }
        }
    }

    /**
     * Compacts the backing slots by merging adjacent pairs of min/max slots into single slots of doubled time scale.
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
            final boolean occ1 = isOccupied(i);
            final boolean occ2 = isOccupied(i + 1);
            if (occ1)
            {
                if (occ2)
                {
                    final Page p2 = pages[(i + 1) >>> PAGE_SHIFT];
                    pages[i >>> PAGE_SHIFT].mergeFrom(i & PAGE_MASK, p2, (i + 1) & PAGE_MASK);
                    p2.counts[(i + 1) & PAGE_MASK] = 0;
                }
                if (j != i)
                {
                    moveSlot(i, j);
                }
            }
            else if (occ2)
            {
                moveSlot(i + 1, j);
            }
            j++;
        }

        // When offset == 1 and size is even, one trailing slot at index (size - 1) remains unpaired
        if (i < size && isOccupied(i))
        {
            moveSlot(i, j);
        }
    }
}
