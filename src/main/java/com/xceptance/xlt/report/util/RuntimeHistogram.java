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

/**
 * The {@link RuntimeHistogram} class calculates percentiles and range counts from {@code int} values
 * added over a test run without retaining every individual observation.
 *
 * <h3>Why Sparse 64-Bucket Pages Instead of a Flat {@code int[]} Array</h3>
 * <p>
 * During parallel report generation, each worker thread maintains thousands of {@link IntTimeSeries}
 * instances (e.g., 16 threads &times; 1,500 request types = 24,000 histograms). A naive flat array
 * spanning {@code [firstIndexValue .. lastIndexValue]} suffers from two severe memory pathologies in
 * real load tests:
 * </p>
 * <ol>
 *   <li><b>Outlier Sparse Blowup:</b> Most requests complete within {@code 0..500 ms}, but a single
 *       network timeout or long poll at {@code 180,000 ms} forces a flat histogram (at 8 ms precision)
 *       to allocate a contiguous {@code int[22500]} array (~90 KB per worker, or ~1.4 MB across 16
 *       threads for one timer) in which 99.9% of buckets are zero.</li>
 *   <li><b>Reallocation Churn on Monotonic Highs:</b> Growing a flat array to the exact new maximum
 *       every time a slightly slower request arrives causes dozens of {@code Arrays.copyOf} allocations
 *       per histogram (measured at 1.82 GB of temporary {@code int[]} churn in JFR).</li>
 * </ol>
 *
 * <h3>Memory Layout &amp; Global Coordinate Alignment</h3>
 * <ul>
 *   <li>Buckets are partitioned into fixed-size pages of {@link #PAGE_SIZE} (64) {@code int} counters
 *       (256 bytes per page). Empty 64-bucket regions remain {@code null} in the outer {@code int[][] pages}
 *       spine and consume zero page memory.</li>
 *   <li>Pages are aligned to global bucket coordinates via two's-complement bit math:
 *       {@code pageCoord = index >> 6} and {@code slot = index & 63}, satisfying the exact identity
 *       {@code (pageCoord << 6) + slot == index} for all signed 32-bit integers (negative, zero, and positive).</li>
 *   <li>Because {@code slot = index & 63} is invariant with respect to {@link #basePageCoord}, prepending
 *       earlier pages when a smaller value arrives only shifts {@code int[]} references in the outer spine
 *       and <b>never</b> shifts elements inside allocated 64-int pages. Likewise, {@link #merge(RuntimeHistogram)}
 *       combines pages at matching global coordinates directly without unaligned element shifting.</li>
 * </ul>
 */
public class RuntimeHistogram
{
    /**
     * The default precision, power of 2 for shifting aka this is 1&lt;&lt;0 = 1 (no precision loss).
     */
    private static final int DEFAULT_PRECISION = 1;

    /**
     * Bit shift for dividing a bucket index into a page coordinate ({@code 2^6 = 64} buckets per page).
     */
    static final int PAGE_SHIFT = 6;

    /**
     * Number of bucket counters per page (64 {@code int}s = 256 bytes per allocated page).
     */
    static final int PAGE_SIZE = 1 << PAGE_SHIFT;

    /**
     * Bitmask for extracting the intra-page slot index ({@code index & 63}).
     */
    static final int PAGE_MASK = PAGE_SIZE - 1;

    /**
     * Sparse spine of 64-bucket counter pages.
     * <p>
     * <b>Spine Invariant:</b> When {@code valueCount > 0}, {@code pages[0]} corresponds to global page
     * coordinate {@code basePageCoord == (firstIndexValue >> PAGE_SHIFT)}, and {@code pages[pages.length - 1]}
     * corresponds to {@code lastIndexValue >> PAGE_SHIFT}. Any intermediate 64-bucket range with no recorded
     * observations remains {@code null} in {@code pages}.
     * </p>
     */
    private int[][] pages;

    /**
     * Global page coordinate ({@code index >> PAGE_SHIFT}) corresponding to {@code pages[0]}.
     */
    private int basePageCoord;

    /**
     * Minimum bucket index ({@code value >> precision}) observed so far.
     */
    private int firstIndexValue = Integer.MAX_VALUE;

    /**
     * Maximum bucket index ({@code value >> precision}) observed so far.
     */
    private int lastIndexValue = Integer.MIN_VALUE;

    /**
     * Right-shift amount corresponding to the bucket width (power of 2).
     */
    private final int precision;

    /**
     * Total number of observations added so far.
     */
    private int valueCount;

    /**
     * Creates a new {@link RuntimeHistogram} with default precision (1 = exact bucket per integer).
     */
    public RuntimeHistogram()
    {
        this(DEFAULT_PRECISION);
    }

    /**
     * Creates a new {@link RuntimeHistogram} with the specified bucket precision.
     * Precision defines the width of each bucket. For example, a precision of 8 means that values
     * {@code 0..7} map to bucket 0, {@code 8..15} map to bucket 1, etc. Non-power-of-two values are
     * rounded up to the next power of two so bucketing can use a single bit-shift.
     *
     * @param precision
     *            the desired bucket width
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
        this.basePageCoord = other.basePageCoord;
        this.pages = clonePages(other.pages);
    }

    /**
     * Deep-clones a sparse page spine, preserving {@code null} entries for unallocated pages.
     *
     * @param srcPages
     *            the source page spine (may be {@code null})
     * @return a deep copy of {@code srcPages}, or {@code null} if {@code srcPages} is {@code null}
     */
    private static int[][] clonePages(final int[][] srcPages)
    {
        if (srcPages == null)
        {
            return null;
        }
        final int len = srcPages.length;
        final int[][] copy = new int[len][];
        for (int i = 0; i < len; i++)
        {
            final int[] srcPage = srcPages[i];
            if (srcPage != null)
            {
                copy[i] = srcPage.clone();
            }
        }
        return copy;
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
        final int pageCoord = index >> PAGE_SHIFT;
        final int slot = index & PAGE_MASK;

        if (this.valueCount == 0)
        {
            this.firstIndexValue = index;
            this.lastIndexValue = index;
            this.basePageCoord = pageCoord;
            this.pages = new int[1][];
            final int[] page = new int[PAGE_SIZE];
            page[slot] = 1;
            this.pages[0] = page;
            this.valueCount = 1;
            return;
        }

        if (index < this.firstIndexValue)
        {
            this.firstIndexValue = index;
        }
        else if (index > this.lastIndexValue)
        {
            this.lastIndexValue = index;
        }

        int pageOffset = pageCoord - this.basePageCoord;
        if (pageOffset < 0 || pageOffset >= this.pages.length)
        {
            ensureSpineCovers(pageCoord);
            pageOffset = pageCoord - this.basePageCoord;
        }

        int[] page = this.pages[pageOffset];
        if (page == null)
        {
            page = new int[PAGE_SIZE];
            this.pages[pageOffset] = page;
        }
        page[slot]++;
        this.valueCount++;
    }

    /**
     * Expands the outer {@code int[][] pages} spine so that {@code targetPageCoord} is addressable.
     * <p>
     * Because each page spans 64 buckets ({@code 64 * 8 ms = 512 ms} at default time-series precision),
     * spine expansions happen $64\times$ less frequently than bucket expansions. Furthermore, because
     * pages are aligned to global coordinates ({@code index >> 6}), shifting the spine when
     * {@code targetPageCoord < basePageCoord} only copies page object references and never touches
     * the interior of any allocated {@code int[64]} page.
     * </p>
     *
     * @param targetPageCoord
     *            the global page coordinate that must be covered
     */
    private void ensureSpineCovers(final int targetPageCoord)
    {
        final int currentLen = this.pages.length;
        if (targetPageCoord < this.basePageCoord)
        {
            final int shift = this.basePageCoord - targetPageCoord;
            final int[][] newPages = new int[currentLen + shift][];
            System.arraycopy(this.pages, 0, newPages, shift, currentLen);
            this.pages = newPages;
            this.basePageCoord = targetPageCoord;
        }
        else
        {
            final int newLen = (targetPageCoord - this.basePageCoord) + 1;
            final int[][] newPages = new int[newLen][];
            System.arraycopy(this.pages, 0, newPages, 0, currentLen);
            this.pages = newPages;
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
     *            the quantile in {@code [0.0 .. 1.0]}
     * @return the p-th quantile
     */
    public double getQuantile(final double p)
    {
        return getPercentile(p * 100.0);
    }

    /**
     * Returns the p-th percentile of the values added.
     *
     * @param p
     *            the percentile ({@code 0 <= p <= 100})
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
     * @param start
     *            the start value (inclusive)
     * @param end
     *            the end value (inclusive)
     * @return the number of values in the range
     */
    public long getCountForValue(final int start, final int end)
    {
        if (start > end)
        {
            throw new IllegalArgumentException("Start value must be less than or equal to end value");
        }

        // Note: When empty, firstIndexValue == Integer.MAX_VALUE and lastIndexValue == Integer.MIN_VALUE,
        // which clamps startPageOffset to +33,554,431 and endPageOffset to -33,554,432 so the page loop
        // naturally executes 0 iterations without needing a separate empty or out-of-bounds branch.
        final int clampedStart = Math.max(this.firstIndexValue, start >> this.precision);
        final int clampedEnd = Math.min(this.lastIndexValue, end >> this.precision);

        final int startPageOffset = (clampedStart >> PAGE_SHIFT) - this.basePageCoord;
        final int endPageOffset = (clampedEnd >> PAGE_SHIFT) - this.basePageCoord;
        final int startSlot = clampedStart & PAGE_MASK;
        final int endSlot = clampedEnd & PAGE_MASK;

        long count = 0;
        for (int p = startPageOffset; p <= endPageOffset; p++)
        {
            final int[] page = this.pages[p];
            if (page != null)
            {
                final int sStart = (p == startPageOffset) ? startSlot : 0;
                final int sEnd = (p == endPageOffset) ? endSlot : PAGE_MASK;
                for (int s = sStart; s <= sEnd; s++)
                {
                    count += page[s];
                }
            }
        }

        return count;
    }

    /**
     * Returns the value that belongs to the first bucket whose cumulative observation count
     * reaches or exceeds {@code totalCount}.
     * <p>
     * Unallocated ({@code null}) pages contain 64 zero buckets and are skipped in O(1) without
     * scanning individual slots.
     * </p>
     *
     * @param totalCount
     *            the cumulative count threshold to reach ({@code 1 <= totalCount <= valueCount})
     * @return the bucket value shifted back by {@link #precision}
     */
    private int getValueByCount(final double totalCount)
    {
        int p = 0;
        double count = 0;

        while (true)
        {
            final int[] page = this.pages[p];
            if (page != null)
            {
                for (int s = 0; s < PAGE_SIZE; s++)
                {
                    count += page[s];
                    if (count >= totalCount)
                    {
                        final int bucketIndex = ((this.basePageCoord + p) << PAGE_SHIFT) + s;
                        return bucketIndex << this.precision;
                    }
                }
            }
            p++;
        }
    }

    /**
     * Returns the logical bucket span covered by this histogram ({@code lastIndexValue - firstIndexValue + 1}),
     * or {@code 0} if empty.
     *
     * @return the logical bucket range size
     */
    public int getNumberOfBuckets()
    {
        return valueCount != 0 ? (lastIndexValue - firstIndexValue + 1) : 0;
    }

    /**
     * Returns the number of non-null 64-bucket pages physically allocated in this histogram.
     * Package-private for memory-sparsity verification in unit tests.
     *
     * @return the number of allocated 64-int pages
     */
    int getAllocatedPageCount()
    {
        if (pages == null)
        {
            return 0;
        }
        int allocated = 0;
        for (final int[] page : pages)
        {
            if (page != null)
            {
                allocated++;
            }
        }
        return allocated;
    }

    /**
     * Returns the precision used. 1 means we keep all values as is.
     * 2 means we reduce the result precision by a factor of 2, 4 means by a factor of 4, etc.
     *
     * @return the precision
     */
    public int getPrecision()
    {
        return 1 << precision;
    }

    /**
     * Merges another {@link RuntimeHistogram} into this instance.
     * <p>
     * Because both histograms align their 64-bucket pages to global coordinates ({@code index >> 6}),
     * corresponding pages share identical intra-page slot offsets ({@code index & 63}) and can be
     * cloned (when the destination page is {@code null}) or summed element-wise without any bit shifting.
     * </p>
     *
     * @param other
     *            the other histogram to merge, may be {@code null}
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
            this.basePageCoord = other.basePageCoord;
            this.pages = clonePages(other.pages);
            return;
        }

        final int newFirstIndex = Math.min(this.firstIndexValue, other.firstIndexValue);
        final int newLastIndex = Math.max(this.lastIndexValue, other.lastIndexValue);
        final int newMinPageCoord = newFirstIndex >> PAGE_SHIFT;
        final int newMaxPageCoord = newLastIndex >> PAGE_SHIFT;

        if (newMinPageCoord < this.basePageCoord)
        {
            ensureSpineCovers(newMinPageCoord);
        }
        if (newMaxPageCoord - this.basePageCoord >= this.pages.length)
        {
            ensureSpineCovers(newMaxPageCoord);
        }

        this.firstIndexValue = newFirstIndex;
        this.lastIndexValue = newLastIndex;

        for (int p = 0; p < other.pages.length; p++)
        {
            final int[] srcPage = other.pages[p];
            if (srcPage == null)
            {
                continue;
            }
            final int dstOffset = (other.basePageCoord + p) - this.basePageCoord;
            final int[] dstPage = this.pages[dstOffset];
            if (dstPage == null)
            {
                this.pages[dstOffset] = srcPage.clone();
            }
            else
            {
                for (int s = 0; s < PAGE_SIZE; s++)
                {
                    dstPage[s] += srcPage[s];
                }
            }
        }

        this.valueCount += other.valueCount;
    }
}
