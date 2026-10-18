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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYIntervalSeries;

import com.xceptance.xlt.report.util.misc.BitCompression;

/**
 * An {@link IntTimeSeries} accumulates per-slot time series statistics (count, sum, min, max, error count,
 * peak 1-second burst rates, active concurrency, and an approximate distinct-value sketch) alongside exact
 * overall summary statistics (total count, sum, min, max, standard deviation, and histogram quantiles).
 * <p>
 * <b>Why Paged Primitive Storage?</b><br>
 * In large load tests (e.g., hundreds of request timers tracked across 16+ parallel worker threads over
 * multi-hour durations), storing an {@link IntTimeSeriesEntry} object per second creates tens of millions of
 * long-lived small heap objects (~64 bytes each). That object graph causes heavy Young-to-Old generation
 * promotion and GC evacuation pressure during report generation.
 * <p>
 * To eliminate per-second object overhead while keeping memory proportional to active time spans,
 * {@link IntTimeSeries} divides its timeline into fixed <b>64-slot primitive pages</b> ({@link Page}):
 * <ul>
 *   <li><b>Two-Level Indexing:</b> Any logical slot index {@code pos} maps to {@code pageIndex = pos >>> 6}
 *       ({@code pos / 64}) and {@code slotInPage = pos & 63} ({@code pos % 64}) using single-cycle bit shifts.</li>
 *   <li><b>Sparse Page Allocation:</b> The outer {@link #pages} array only allocates a {@link Page} when data
 *       actually lands in that 64-slot window. Timers that only fire briefly during a multi-hour test allocate
 *       only the pages they touch.</li>
 *   <li><b>Interleaved Core Primitives:</b> Each {@link Page} packs frequently accessed per-slot fields into
 *       contiguous primitive arrays ({@code ints} for count/max/min, {@code longs} for sum and the 128-bit
 *       distinct-value sketch, and {@code scales} for the sketch scale factor) for CPU cache locality.</li>
 *   <li><b>Lazy Auxiliary Arrays:</b> Secondary per-slot arrays ({@code errorCounts}, {@code concurrentCounts},
 *       {@code maxCounts}, {@code maxErrorCounts}) remain {@code null} on a page until a slot in that page
 *       actually diverges from its default invariant (e.g., an error occurs, a multi-second operation spans
 *       slots, or slots are condensed to {@code scale > 1}). In typical error-free 1-second series, these four
 *       arrays are never allocated at all.</li>
 * </ul>
 * <p>
 * <b>Dynamic Capacity &amp; Self-Managing Resolution:</b><br>
 * A series starts with a small page table ({@link #DEFAULT_INITIAL_CAPACITY} = 4,096 slots) and expands its
 * page directory without condensing up to {@link #DEFAULT_MAX_CAPACITY} (32,768 slots, ~9.1 hours at 1-second
 * resolution). If a test exceeds {@link #maxCapacity}, two consecutive slots are merged into one
 * ({@link #condenseOneStep()}), doubling the time width per slot ({@code slotWidth = 1 << (scale - 1)}) so
 * arbitrarily long tests remain bounded in memory.
 */
public class IntTimeSeries
{
    /**
     * The default initial capacity of a time series (rounded up to 4096 = ~1.1 hours at 1s resolution).
     * Pages of 64 slots are lazily instantiated as data arrives to keep memory minimal.
     */
    public static final int DEFAULT_INITIAL_CAPACITY = 4096;

    /**
     * The default maximum capacity of a time series before condensing kicks in (32768 = ~9.1 hours at 1s resolution).
     */
    public static final int DEFAULT_MAX_CAPACITY = 32768;

    /**
     * The legacy default initial value set size.
     */
    public static final int DEFAULT_SIZE = 3600;

    /**
     * Log2 of the number of time slots per page ({@code 2^6 = 64} slots per page).
     * Used to compute the page index from a slot index via {@code pos >>> PAGE_SHIFT}.
     */
    static final int PAGE_SHIFT = 6;

    /**
     * Number of time slots per primitive page (64 slots = 64 seconds at scale 1).
     */
    static final int PAGE_SIZE = 1 << PAGE_SHIFT;

    /**
     * Bitmask to extract the intra-page slot index ({@code 0..63}) from a slot index via {@code pos & PAGE_MASK}.
     */
    static final int PAGE_MASK = PAGE_SIZE - 1;

    /**
     * Sentinel value for {@link #firstSecond} indicating no data has been added to this time series yet.
     */
    private static final int DEFAULT = 2_147_385_000; // 2037-12-31 00:00:00

    /**
     * A 64-slot primitive page storing time series slot metrics without per-second object overhead.
     * <p>
     * <b>Memory Layout per Slot {@code s} ({@code 0 <= s < 64}):</b>
     * <ul>
     *   <li>{@code longs[s * 3 + 0]}: {@code totalValue} — sum of sample values in slot {@code s}</li>
     *   <li>{@code longs[s * 3 + 1]}: {@code distinctValuesLow} — lower 64 bits of the 128-bit distinct-value bitmap</li>
     *   <li>{@code longs[s * 3 + 2]}: {@code distinctValuesHigh} — upper 64 bits of the 128-bit distinct-value bitmap</li>
     *   <li>{@code ints[s * 3 + 0]}: {@code count} — number of completed operations in slot {@code s}</li>
     *   <li>{@code ints[s * 3 + 1]}: {@code maximum} — maximum value observed (initialized to {@link Integer#MIN_VALUE})</li>
     *   <li>{@code ints[s * 3 + 2]}: {@code minimum} — minimum value observed (initialized to {@link Integer#MAX_VALUE})</li>
     *   <li>{@code scales[s]}: {@code distinctValuesScale} — power-of-two shift factor ({@code 0..31}) for the distinct-value bitmap</li>
     * </ul>
     * <p>
     * <b>Lazy Auxiliary Array Invariants:</b><br>
     * To save up to 1 KB per page in the common case, the four secondary arrays are allocated on demand
     * only when at least one slot in the page deviates from the default relationship:
     * <ul>
     *   <li>{@code errorCounts == null} &rArr; all 64 slots in this page have {@code errorCount == 0}.</li>
     *   <li>{@code concurrentCounts == null} &rArr; all 64 slots in this page have {@code concurrentCount == count}.</li>
     *   <li>{@code maxCounts == null} &rArr; all 64 slots in this page have {@code maxCount == count} (always true at scale 1).</li>
     *   <li>{@code maxErrorCounts == null} &rArr; all 64 slots in this page have {@code maxErrorCount == errorCount} (always true at scale 1).</li>
     * </ul>
     */
    static final class Page
    {
        /**
         * Pre-initialized template for {@link #ints} where every slot has {@code count = 0},
         * {@code maximum = Integer.MIN_VALUE}, and {@code minimum = Integer.MAX_VALUE}.
         * Cloning this template is faster than running a initialization loop on every new page allocation.
         */
        private static final int[] EMPTY_INTS = new int[PAGE_SIZE * 3];

        static
        {
            for (int s = 0; s < PAGE_SIZE; s++)
            {
                final int base = s * 3;
                EMPTY_INTS[base + 1] = Integer.MIN_VALUE;
                EMPTY_INTS[base + 2] = Integer.MAX_VALUE;
            }
        }

        /**
         * Interleaved per-slot longs:
         * {@code [s * 3]} = totalValue, {@code [s * 3 + 1]} = distinctValuesLow, {@code [s * 3 + 2]} = distinctValuesHigh.
         */
        final long[] longs;

        /**
         * Interleaved per-slot ints:
         * {@code [s * 3]} = count, {@code [s * 3 + 1]} = maximum, {@code [s * 3 + 2]} = minimum.
         */
        final int[] ints;

        /**
         * Per-slot bit shift scale ({@code 0..31}) for the 128-bit distinct-value approximation bitmap.
         */
        final byte[] scales;

        /**
         * Lazily allocated per-slot error counts ({@code null} when all slots in this page have 0 errors).
         */
        int[] errorCounts;

        /**
         * Lazily allocated per-slot concurrent counts ({@code null} when {@code concurrentCount == count} for all slots in this page).
         */
        int[] concurrentCounts;

        /**
         * Lazily allocated per-slot peak 1s counts ({@code null} when {@code maxCount == count} for all slots in this page).
         */
        int[] maxCounts;

        /**
         * Lazily allocated per-slot peak 1s error counts ({@code null} when {@code maxErrorCount == errorCount} for all slots in this page).
         */
        int[] maxErrorCounts;

        /**
         * Allocates a fresh 64-slot page with empty core arrays and unallocated ({@code null}) auxiliary arrays.
         */
        Page()
        {
            this.longs = new long[PAGE_SIZE * 3];
            this.ints = EMPTY_INTS.clone();
            this.scales = new byte[PAGE_SIZE];
        }

        /**
         * Deep-copy constructor used when cloning an {@link IntTimeSeries} (e.g., in {@link #toResolution(int)}).
         */
        Page(final Page other)
        {
            this.longs = other.longs.clone();
            this.ints = other.ints.clone();
            this.scales = other.scales.clone();
            this.errorCounts = other.errorCounts != null ? other.errorCounts.clone() : null;
            this.concurrentCounts = other.concurrentCounts != null ? other.concurrentCounts.clone() : null;
            this.maxCounts = other.maxCounts != null ? other.maxCounts.clone() : null;
            this.maxErrorCounts = other.maxErrorCounts != null ? other.maxErrorCounts.clone() : null;
        }

        int getCount(final int s)
        {
            return ints[s * 3];
        }

        int getMaximum(final int s)
        {
            return ints[s * 3 + 1];
        }

        int getMinimum(final int s)
        {
            return ints[s * 3 + 2];
        }

        long getTotalValue(final int s)
        {
            return longs[s * 3];
        }

        long getDistinctLow(final int s)
        {
            return longs[s * 3 + 1];
        }

        long getDistinctHigh(final int s)
        {
            return longs[s * 3 + 2];
        }

        int getDistinctScale(final int s)
        {
            return scales[s] & 0xFF;
        }

        int getErrorCount(final int s)
        {
            return errorCounts != null ? errorCounts[s] : 0;
        }

        /**
         * Returns the active concurrency for slot {@code s}, falling back to {@code count} when
         * {@link #concurrentCounts} has not needed to diverge from {@code count}.
         */
        int getConcurrentCount(final int s)
        {
            return concurrentCounts != null ? concurrentCounts[s] : ints[s * 3];
        }

        /**
         * Returns the peak 1-second completion count for slot {@code s}, falling back to {@code count}
         * when {@link #maxCounts} has not needed to diverge from {@code count}.
         */
        int getMaxCount(final int s)
        {
            return maxCounts != null ? maxCounts[s] : ints[s * 3];
        }

        /**
         * Returns the peak 1-second error count for slot {@code s}, falling back to {@code errorCount}
         * (or {@code 0}) when {@link #maxErrorCounts} has not needed to diverge from {@code errorCount}.
         */
        int getMaxErrorCount(final int s)
        {
            return maxErrorCounts != null ? maxErrorCounts[s] : (errorCounts != null ? errorCounts[s] : 0);
        }

        /**
         * A slot is considered active if any operation completed in it ({@code count > 0}) OR if an
         * in-flight operation passed through it without completing in it ({@code concurrentCount > 0}).
         */
        boolean isActive(final int s)
        {
            return ints[s * 3] > 0 || (concurrentCounts != null && concurrentCounts[s] > 0);
        }

        int[] ensureErrorCounts()
        {
            if (errorCounts == null)
            {
                errorCounts = new int[PAGE_SIZE];
            }
            return errorCounts;
        }

        /**
         * Lazily allocates {@link #concurrentCounts} and seeds every slot with its current {@code count}.
         * <p>
         * Seeding is essential: while {@code concurrentCounts == null}, all 64 slots implicitly had
         * {@code concurrentCount == count}. When one slot diverges (e.g. via {@link #updateConcurrency(int)}),
         * the other 63 slots on this page must retain their accumulated {@code count} as their baseline concurrency.
         */
        int[] ensureConcurrentCounts()
        {
            if (concurrentCounts == null)
            {
                concurrentCounts = new int[PAGE_SIZE];
                for (int i = 0; i < PAGE_SIZE; i++)
                {
                    concurrentCounts[i] = ints[i * 3];
                }
            }
            return concurrentCounts;
        }

        /**
         * Lazily allocates {@link #maxCounts} and seeds every slot with its current {@code count}
         * so existing slots on this page preserve their implicit {@code maxCount == count} baseline.
         */
        int[] ensureMaxCounts()
        {
            if (maxCounts == null)
            {
                maxCounts = new int[PAGE_SIZE];
                for (int i = 0; i < PAGE_SIZE; i++)
                {
                    maxCounts[i] = ints[i * 3];
                }
            }
            return maxCounts;
        }

        /**
         * Lazily allocates {@link #maxErrorCounts} and seeds every slot with its current {@code errorCount}
         * so existing slots on this page preserve their implicit {@code maxErrorCount == errorCount} baseline.
         */
        int[] ensureMaxErrorCounts()
        {
            if (maxErrorCounts == null)
            {
                maxErrorCounts = new int[PAGE_SIZE];
                System.arraycopy(errorCounts, 0, maxErrorCounts, 0, PAGE_SIZE);
            }
            return maxErrorCounts;
        }

        void setErrorCount(final int s, final int err)
        {
            if (err > 0)
            {
                ensureErrorCounts()[s] = err;
            }
            else if (errorCounts != null)
            {
                errorCounts[s] = 0;
            }
        }

        /**
         * Sets the concurrent count for slot {@code s}.
         * Note: {@code ints[s * 3]} ({@code count}) MUST already be updated for slot {@code s} before calling this method,
         * because it compares {@code conc} against {@code ints[s * 3]} to avoid allocating {@link #concurrentCounts}
         * when {@code conc == count}.
         */
        void setConcurrentCount(final int s, final int conc)
        {
            if (conc != ints[s * 3])
            {
                ensureConcurrentCounts()[s] = conc;
            }
            else if (concurrentCounts != null)
            {
                concurrentCounts[s] = conc;
            }
        }

        /**
         * Sets the peak 1-second count for slot {@code s}.
         * Note: {@code ints[s * 3]} ({@code count}) MUST already be updated for slot {@code s} before calling this method.
         */
        void setMaxCount(final int s, final int maxCnt)
        {
            if (maxCnt != ints[s * 3])
            {
                ensureMaxCounts()[s] = maxCnt;
            }
            else if (maxCounts != null)
            {
                maxCounts[s] = maxCnt;
            }
        }

        /**
         * Sets the peak 1-second error count for slot {@code s}.
         * Note: {@link #setErrorCount(int, int)} MUST be called for slot {@code s} before calling this method
         * so that {@code errorCounts[s]} reflects the new error count when checking if {@code maxErr != err}.
         */
        void setMaxErrorCount(final int s, final int maxErr)
        {
            final int err = errorCounts != null ? errorCounts[s] : 0;
            if (maxErr != err)
            {
                ensureMaxErrorCounts()[s] = maxErr;
            }
            else if (maxErrorCounts != null)
            {
                maxErrorCounts[s] = maxErr;
            }
        }

        /**
         * Records a completed measurement in slot {@code s}, updating count, errors, concurrency, min/max,
         * total value, and the 128-bit distinct-value bitmap in place.
         */
        void updateValue(final int s, final int value, final boolean failed)
        {
            // Clamp negative measurements to 0 so bit-shifting into the distinct-value bitmap is always safe
            final int v = Math.max(0, value);
            final int intBase = s * 3;

            // 1. Update completion count (and peak count if this page has already materialized maxCounts)
            ints[intBase]++;
            if (maxCounts != null)
            {
                maxCounts[s]++;
            }

            // 2. Update error count (allocating errorCounts array only if an error actually occurred)
            if (failed)
            {
                ensureErrorCounts()[s]++;
                if (maxErrorCounts != null)
                {
                    maxErrorCounts[s]++;
                }
            }

            // 3. If concurrentCounts was materialized on this page, keep it in sync with the completion
            if (concurrentCounts != null)
            {
                concurrentCounts[s]++;
            }

            // 4. Update slot minimum and maximum
            ints[intBase + 1] = Math.max(ints[intBase + 1], v);
            ints[intBase + 2] = Math.min(ints[intBase + 2], v);

            // 5. Accumulate sum of values
            final int longBase = s * 3;
            longs[longBase] += v;

            // 6. Record value in the 128-bit distinct-value sketch (longs[longBase + 1] = low 64 bits, +2 = high 64 bits).
            // Fast-path: if (v >> dScale) < 128, set the bit directly without rescaling the bitmap.
            // Note: per JLS §15.19, 64-bit long shifts (1L << s) implicitly mask the shift distance by (s & 63),
            // so for s in [64 .. 127], (1L << s) directly sets bit (s - 64) in the high word.
            int dScale = scales[s] & 0xFF;
            final int scaled = v >> dScale;
            if (scaled < 128)
            {
                if (scaled < 64)
                {
                    longs[longBase + 1] |= (1L << scaled);
                }
                else
                {
                    longs[longBase + 2] |= (1L << scaled);
                }
            }
            else
            {
                // Value exceeds the current 128-bucket range [0 .. 128 * 2^dScale - 1].
                // Compute the smallest scale where (v >> neededScale) fits in [64 .. 127] using leading zeros,
                // compress existing bits in the 128-bit sketch down to neededScale, and set the bit in the high word.
                final int neededScale = (32 - Integer.numberOfLeadingZeros(v)) - 7;
                scaleUpSlot(s, neededScale);
                final int sc = v >> neededScale;
                longs[longBase + 2] |= (1L << sc);
            }
        }

        /**
         * Increments active concurrency for a slot where an operation was in flight but did not complete.
         * Forces allocation of {@link #concurrentCounts} because {@code concurrentCount} now exceeds {@code count}.
         */
        void updateConcurrency(final int s)
        {
            ensureConcurrentCounts()[s]++;
        }

        /**
         * Rescales the 128-bit distinct-value bitmap of slot {@code s} up to {@code targetScale}
         * (where {@code targetScale > currentScale}).
         * <p>
         * Each increment of scale doubles the bucket width by OR-ing adjacent bit pairs and compacting
         * the resulting 64 bits via {@link BitCompression}. After the first step folds {@code high} into
         * the upper 32 bits of {@code low}, {@code high} becomes {@code 0L} and any remaining steps only
         * need to compress {@code low}.
         */
        void scaleUpSlot(final int s, final int targetScale)
        {
            final int longBase = s * 3;
            long low = longs[longBase + 1];
            long high = longs[longBase + 2];
            int currentScale = scales[s] & 0xFF;

            // First step when high word has bits: compress both words and fold high into the upper 32 bits of low
            if (high != 0L)
            {
                long l = BitCompression.combineAdjacentBits(low);
                l = BitCompression.compressAndShiftOddBits(l);

                long h = BitCompression.combineAdjacentBits(high);
                h = BitCompression.compressAndShiftOddBits(h);

                low = l | (h << 32);
                high = 0L;
                currentScale++;
            }

            // Subsequent steps only need to compress the low 64-bit word
            while (currentScale < targetScale)
            {
                long l = BitCompression.combineAdjacentBits(low);
                low = BitCompression.compressAndShiftOddBits(l);
                currentScale++;
            }

            longs[longBase + 1] = low;
            longs[longBase + 2] = high;
            scales[s] = (byte) currentScale;
        }

        /**
         * Resets slot {@code s} back to its initial empty state (used when shifting slots or clearing upper pages after condensing).
         */
        void clearSlot(final int s)
        {
            final int intBase = s * 3;
            ints[intBase] = 0;
            ints[intBase + 1] = Integer.MIN_VALUE;
            ints[intBase + 2] = Integer.MAX_VALUE;

            final int longBase = s * 3;
            longs[longBase] = 0L;
            longs[longBase + 1] = 0L;
            longs[longBase + 2] = 0L;

            scales[s] = 0;

            if (errorCounts != null)
            {
                errorCounts[s] = 0;
            }
            if (concurrentCounts != null)
            {
                concurrentCounts[s] = 0;
            }
            if (maxCounts != null)
            {
                maxCounts[s] = 0;
            }
            if (maxErrorCounts != null)
            {
                maxErrorCounts[s] = 0;
            }
        }

        /**
         * Materializes an {@link IntTimeSeriesEntry} value object from slot {@code s} on demand
         * (e.g., when building JFreeChart data items or inspecting entries in unit tests).
         */
        IntTimeSeriesEntry toEntry(final int s)
        {
            return new IntTimeSeriesEntry(
                getTotalValue(s),
                getCount(s),
                getConcurrentCount(s),
                getErrorCount(s),
                getMaxErrorCount(s),
                getMaxCount(s),
                getMaximum(s),
                getMinimum(s),
                getDistinctLow(s),
                getDistinctHigh(s),
                getDistinctScale(s));
        }
    }

    /**
     * The quantile and distribution calculation.
     */
    private final RuntimeHistogram histogram;

    /**
     * The smallest time [s] for which a min/max value exists.
     * This is a raw value in seconds and never scales.
     */
    private int firstSecond;

    /**
     * The highest position in the values array that has been populated.
     */
    private int lastPosUsed;

    /**
     * The scale level. A single slot represents {@code (1 << (scale - 1))} seconds. Always &ge; 1.
     */
    private int scale = 1;

    /**
     * Paged primitive storage of 64-slot blocks.
     * Pages are lazily instantiated as records arrive to avoid per-second object overhead.
     */
    private Page[] pages;

    /**
     * The capacity of this time series buffer (always a power of 2).
     */
    private int size;

    /**
     * The maximum capacity of this time series before condensing kicks in.
     */
    private final int maxCapacity;

    /**
     * The sum of squares of all values added (used for standard deviation calculation).
     */
    private double sumOfSquares;

    /**
     * Running total count of all values added.
     */
    private long totalCount;

    /**
     * Running total errors of all values added.
     */
    private long totalErrors;

    /**
     * Running sum of all values added.
     */
    private long totalSum;

    /**
     * Minimum value observed.
     */
    private int minValue = Integer.MAX_VALUE;

    /**
     * Maximum value observed.
     */
    private int maxValue = Integer.MIN_VALUE;

    /**
     * Creates a {@link IntTimeSeries} instance with {@link #DEFAULT_INITIAL_CAPACITY} (4096)
     * and dynamic capacity expansion up to {@link #DEFAULT_MAX_CAPACITY} (32768).
     */
    public IntTimeSeries()
    {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_MAX_CAPACITY);
    }

    /**
     * Creates a fixed-capacity {@link IntTimeSeries} instance with the specified size rounded up to the next power of 2.
     * When capacity is reached, it will condense immediately (legacy compatibility).
     *
     * @param size
     *            the target fixed size
     */
    public IntTimeSeries(final int size)
    {
        this(size, size);
    }

    /**
     * Creates a {@link IntTimeSeries} instance with an initial capacity and a maximum capacity
     * before condensing occurs.
     *
     * @param initialCapacity
     *            the initial buffer capacity
     * @param maxCapacity
     *            the maximum capacity before condensing
     */
    public IntTimeSeries(final int initialCapacity, final int maxCapacity)
    {
        this.size = Math.max(2, nextHighestPowerOfTwo(initialCapacity));
        this.maxCapacity = Math.max(this.size, nextHighestPowerOfTwo(maxCapacity));
        this.pages = new Page[pageCount(this.size)];

        this.histogram = new RuntimeHistogram(8);

        // Pages are allocated lazily upon first use to keep memory footprint minimal
        this.firstSecond = DEFAULT;
        this.lastPosUsed = -1;
    }

    /**
     * Copy constructor. Creates an independent deep copy of the given {@link IntTimeSeries}.
     * Only pages up to {@link #lastPosUsed} are cloned; unpopulated pages remain {@code null}.
     *
     * @param other
     *            the instance to copy
     */
    public IntTimeSeries(final IntTimeSeries other)
    {
        this.size = other.size;
        this.maxCapacity = other.maxCapacity;
        this.scale = other.scale;
        this.firstSecond = other.firstSecond;
        this.lastPosUsed = other.lastPosUsed;
        this.sumOfSquares = other.sumOfSquares;
        this.histogram = new RuntimeHistogram(other.histogram);
        this.totalCount = other.totalCount;
        this.totalErrors = other.totalErrors;
        this.totalSum = other.totalSum;
        this.minValue = other.minValue;
        this.maxValue = other.maxValue;
        this.pages = new Page[pageCount(this.size)];
        if (other.lastPosUsed >= 0)
        {
            final int maxPage = other.lastPosUsed >>> PAGE_SHIFT;
            for (int p = 0; p <= maxPage; p++)
            {
                if (other.pages[p] != null)
                {
                    this.pages[p] = new Page(other.pages[p]);
                }
            }
        }
    }

    /**
     * Computes how many 64-slot pages are required to hold {@code slotCapacity} slots.
     */
    private static int pageCount(final int slotCapacity)
    {
        return Math.max(1, (slotCapacity + PAGE_MASK) >>> PAGE_SHIFT);
    }

    /**
     * Returns the {@link Page} at {@code pageIndex}, allocating it on demand if currently {@code null}.
     */
    private Page ensurePage(final int pageIndex)
    {
        Page page = this.pages[pageIndex];
        if (page == null)
        {
            page = new Page();
            this.pages[pageIndex] = page;
        }
        return page;
    }

    /**
     * Copies all metrics from active {@code (srcPage, srcSlot)} into {@code (dstPage, dstSlot)}.
     * <p>
     * Supports in-place page operations (where {@code srcPage == dstPage}), so all source values
     * are read into locals before writing to the destination slot. Also preserves lazy array invariants
     * on {@code dstPage} by writing {@code ints[dstIntBase]} ({@code count}) and {@code setErrorCount}
     * before calling {@code setConcurrentCount}, {@code setMaxCount}, and {@code setMaxErrorCount}.
     */
    private static void copySlot(final Page srcPage, final int srcSlot, final Page dstPage, final int dstSlot)
    {
        final int cnt = srcPage.getCount(srcSlot);
        final int max = srcPage.getMaximum(srcSlot);
        final int min = srcPage.getMinimum(srcSlot);
        final long total = srcPage.getTotalValue(srcSlot);
        final long low = srcPage.getDistinctLow(srcSlot);
        final long high = srcPage.getDistinctHigh(srcSlot);
        final byte dScale = srcPage.scales[srcSlot];
        final int err = srcPage.getErrorCount(srcSlot);
        final int conc = srcPage.getConcurrentCount(srcSlot);
        final int maxCnt = srcPage.getMaxCount(srcSlot);
        final int maxErr = srcPage.getMaxErrorCount(srcSlot);

        final int dstIntBase = dstSlot * 3;
        dstPage.ints[dstIntBase] = cnt;
        dstPage.ints[dstIntBase + 1] = max;
        dstPage.ints[dstIntBase + 2] = min;

        final int dstLongBase = dstSlot * 3;
        dstPage.longs[dstLongBase] = total;
        dstPage.longs[dstLongBase + 1] = low;
        dstPage.longs[dstLongBase + 2] = high;

        dstPage.scales[dstSlot] = dScale;

        // Order matters: setErrorCount must run before setMaxErrorCount so the lazy check sees the updated errorCount
        dstPage.setErrorCount(dstSlot, err);
        dstPage.setConcurrentCount(dstSlot, conc);
        dstPage.setMaxCount(dstSlot, maxCnt);
        dstPage.setMaxErrorCount(dstSlot, maxErr);
    }

    /**
     * Combines the metrics of active {@code (srcPage, srcSlot)} into {@code (dstPage, dstSlot)}.
     * <p>
     * This single primitive helper handles both aggregation modes via {@code isCondense}:
     * <ul>
     *   <li><b>{@code isCondense == true} (Temporal Condensing):</b> Combines two adjacent chronological time slots
     *       into a wider time slot (e.g., two 1s slots into one 2s slot). Total counts and errors are summed,
     *       while peak 1-second burst metrics ({@code maxCount}, {@code maxErrorCount}) take {@link Math#max}
     *       because the two slots occurred at different moments in time.</li>
     *   <li><b>{@code isCondense == false} (Parallel Thread Merge):</b> Combines two slots representing the
     *       <i>exact same time window</i> recorded on different worker threads. Because the events occurred
     *       simultaneously across threads, peak 1-second burst metrics are summed.</li>
     * </ul>
     */
    private static void combineSlot(final Page dstPage, final int dstSlot, final Page srcPage, final int srcSlot, final boolean isCondense)
    {
        final int srcCount = srcPage.getCount(srcSlot);
        final int srcConc = srcPage.getConcurrentCount(srcSlot);
        final int dstCount = dstPage.getCount(dstSlot);
        final int dstConc = dstPage.getConcurrentCount(dstSlot);

        // Handle concurrency-only slots (where an operation was in flight during the slot, so concurrentCount > 0, but count == 0)
        if (srcCount == 0)
        {
            dstPage.setConcurrentCount(dstSlot, Math.max(dstConc, srcConc));
            return;
        }
        if (dstCount == 0)
        {
            final int maxConc = Math.max(dstConc, srcConc);
            copySlot(srcPage, srcSlot, dstPage, dstSlot);
            dstPage.setConcurrentCount(dstSlot, maxConc);
            return;
        }

        // Align the 128-bit distinct-value sketches to the larger scale before bitwise OR-ing them together.
        // Note: we rescale srcLow/srcHigh in local variables so we never mutate srcPage during a merge!
        final int dstScale = dstPage.getDistinctScale(dstSlot);
        final int srcScale = srcPage.getDistinctScale(srcSlot);
        final int targetScale = Math.max(dstScale, srcScale);

        if (dstScale < targetScale)
        {
            dstPage.scaleUpSlot(dstSlot, targetScale);
        }

        long srcLow = srcPage.getDistinctLow(srcSlot);
        long srcHigh = srcPage.getDistinctHigh(srcSlot);
        int curSrcScale = srcScale;
        if (curSrcScale < targetScale)
        {
            if (srcHigh != 0L)
            {
                long l = BitCompression.combineAdjacentBits(srcLow);
                l = BitCompression.compressAndShiftOddBits(l);

                long h = BitCompression.combineAdjacentBits(srcHigh);
                h = BitCompression.compressAndShiftOddBits(h);

                srcLow = l | (h << 32);
                srcHigh = 0L;
                curSrcScale++;
            }
            while (curSrcScale < targetScale)
            {
                long l = BitCompression.combineAdjacentBits(srcLow);
                srcLow = BitCompression.compressAndShiftOddBits(l);
                curSrcScale++;
            }
        }

        final int newCount = dstCount + srcCount;
        final int newMax = Math.max(dstPage.getMaximum(dstSlot), srcPage.getMaximum(srcSlot));
        final int newMin = Math.min(dstPage.getMinimum(dstSlot), srcPage.getMinimum(srcSlot));
        final long newTotal = dstPage.getTotalValue(dstSlot) + srcPage.getTotalValue(srcSlot);
        final int newConc = Math.max(dstConc, srcConc);
        final int dstErr = dstPage.getErrorCount(dstSlot);
        final int srcErr = srcPage.getErrorCount(srcSlot);
        final int newErr = dstErr + srcErr;
        final int dstMaxErr = dstPage.getMaxErrorCount(dstSlot);
        final int srcMaxErr = srcPage.getMaxErrorCount(srcSlot);
        final int dstMaxCnt = dstPage.getMaxCount(dstSlot);
        final int srcMaxCnt = srcPage.getMaxCount(srcSlot);

        final int newMaxErr = isCondense ? Math.max(dstMaxErr, srcMaxErr) : dstMaxErr + srcMaxErr;
        final int newMaxCnt = isCondense ? Math.max(dstMaxCnt, srcMaxCnt) : dstMaxCnt + srcMaxCnt;

        final int dstIntBase = dstSlot * 3;
        dstPage.ints[dstIntBase] = newCount;
        dstPage.ints[dstIntBase + 1] = newMax;
        dstPage.ints[dstIntBase + 2] = newMin;

        final int dstLongBase = dstSlot * 3;
        dstPage.longs[dstLongBase] = newTotal;
        dstPage.longs[dstLongBase + 1] |= srcLow;
        dstPage.longs[dstLongBase + 2] |= srcHigh;

        dstPage.scales[dstSlot] = (byte) targetScale;

        // Order matters: setErrorCount must run before setMaxErrorCount
        dstPage.setErrorCount(dstSlot, newErr);
        dstPage.setConcurrentCount(dstSlot, newConc);
        dstPage.setMaxCount(dstSlot, newMaxCnt);
        dstPage.setMaxErrorCount(dstSlot, newMaxErr);
    }

    /**
     * Returns the smallest power of two greater than or equal to {@code n} (minimum 1).
     */
    public static int nextHighestPowerOfTwo(final int n)
    {
        if (n <= 1)
        {
            return 1;
        }
        return Integer.highestOneBit(n - 1) << 1;
    }

    /**
     * Adds a measurement value spanning from a start time to an end time to this time series.
     * <p>
     * <b>Recording Semantics:</b>
     * <ul>
     * <li><b>Completion Event:</b> The sample measurement {@code value} (e.g. response time in ms),
     * completion {@code count}, and failure status {@code failed} are recorded at {@code endTime} (the
     * moment the operation completed). This conforms to standard performance testing semantics where
     * throughput (TPS) represents completed operations per unit of time, and outage error spikes reflect
     * the actual moment the failures occurred rather than being diluted backwards to the start time.</li>
     * <li><b>Concurrency:</b> Concurrency is tracked across the entire active interval from {@code startTime}
     * through {@code endTime}. Every time slot during which the operation was active has its concurrent
     * count updated.</li>
     * </ul>
     *
     * @param startTime
     *            the start time-stamp in milliseconds
     * @param endTime
     *            the end time-stamp in milliseconds
     * @param value
     *            the value (e.g. response time in ms)
     * @param failed
     *            whether the measurement represents a failure
     */
    public void addValue(final long startTime, final long endTime, final int value, final boolean failed)
    {
        final int startSecond = (int) (startTime * 0.001);
        final int endSecond = Math.max(startSecond, (int) (endTime * 0.001));

        // 1. Anchor or shift the timeline start if an out-of-order earlier timestamp arrives
        if (this.firstSecond == DEFAULT)
        {
            this.firstSecond = startSecond;
        }
        else if (startSecond < this.firstSecond)
        {
            shiftRight(startSecond);
            this.firstSecond = startSecond;
        }

        // 2. Dynamically expand page directory up to maxCapacity before resorting to condensing
        final long requiredSeconds = (long) endSecond - this.firstSecond + 1;
        if (this.scale == 1 && this.size < this.maxCapacity && requiredSeconds > this.size)
        {
            growToCapacity((int) requiredSeconds);
        }

        // 3. If the time span still exceeds buffer capacity, condense adjacent slots (doubling slot width)
        if (endSecond >= this.firstSecond + expandToSeconds(this.size))
        {
            condense(endSecond);
        }

        final int startPos = adjustToScale(startSecond - this.firstSecond);
        final int endPos = adjustToScale(endSecond - this.firstSecond);

        // 4. Record the completion value (runtime), completion count, and failure status
        // at the time the event completed (endPos). This matches the historical XLT completion
        // semantics and ensures throughput (completed operations/sec), response time, and
        // error rates align with the actual completion/failure moment (preventing premature
        // smearing of outage error spikes backwards in time).
        ensurePage(endPos >>> PAGE_SHIFT).updateValue(endPos & PAGE_MASK, value, failed);

        // 5. Track active concurrency across the duration of the operation prior to completion [startPos .. endPos - 1].
        // Concurrency for endPos was already incremented by updateValue() above.
        for (int p = startPos; p < endPos; p++)
        {
            ensurePage(p >>> PAGE_SHIFT).updateConcurrency(p & PAGE_MASK);
        }

        this.lastPosUsed = Math.max(endPos, this.lastPosUsed);

        // 6. Update exact global summary statistics and quantile histogram
        final int v = Math.max(0, value);
        this.totalCount++;
        this.totalSum += v;
        if (failed)
        {
            this.totalErrors++;
        }
        this.maxValue = Math.max(this.maxValue, v);
        this.minValue = Math.min(this.minValue, v);

        // Direct floating point multiplication replaces transcendental Math.pow(v, 2)
        this.sumOfSquares += (double) v * v;
        this.histogram.addValue(v);
    }

    /**
     * Adds a single-point measurement.
     *
     * @param startTime
     *            the time-stamp in milliseconds
     * @param value
     *            the value
     * @param failed
     *            whether the measurement represents a failure
     */
    public void addValue(final long startTime, final int value, final boolean failed)
    {
        addValue(startTime, startTime, value, failed);
    }

    /**
     * Adjusts the unscaled seconds offset to the current slot index.
     *
     * @param v
     *            the seconds offset
     * @return slot index
     */
    private int adjustToScale(final int v)
    {
        return v >> (this.scale - 1);
    }

    /**
     * Converts a slot offset into seconds according to the current scale.
     */
    private int expandToSeconds(final int pos)
    {
        return pos << (this.scale - 1);
    }

    /**
     * Expands the capacity of this time series buffer to at least targetCapacity without condensing,
     * up to {@link #maxCapacity}. Existing pages are retained, and newly added page slots are left null
     * for lazy instantiation upon first write.
     *
     * @param targetCapacity
     *            the target buffer capacity to achieve
     */
    private void growToCapacity(final int targetCapacity)
    {
        final int limit = Math.min(targetCapacity, this.maxCapacity);
        while (this.size < limit)
        {
            this.size <<= 1;
        }
        this.pages = Arrays.copyOf(this.pages, pageCount(this.size));
    }

    /**
     * Shifts populated slots to the right when an out-of-order timestamp earlier than {@link #firstSecond} arrives.
     * <p>
     * If shifting by {@code offset} would push existing data past the end of the buffer, this method first
     * tries to expand the buffer capacity (up to {@link #maxCapacity} while at scale 1) and otherwise
     * condenses the series until the shifted range fits. Slots are then copied backward from right to left
     * so source slots are never overwritten before being read, and vacated leading slots {@code [0 .. offset - 1]}
     * are cleared.
     */
    private void shiftRight(final int second)
    {
        int offset = adjustToScale(this.firstSecond) - adjustToScale(second);
        if (this.scale == 1 && this.size < this.maxCapacity && this.lastPosUsed + offset >= this.size)
        {
            growToCapacity(this.lastPosUsed + offset + 1);
        }
        while (this.lastPosUsed + offset >= this.size)
        {
            condenseOneStep();
            offset = adjustToScale(this.firstSecond) - adjustToScale(second);
        }

        if (offset > 0)
        {
            // Copy active slots backward (high index -> low index) to avoid clobbering uncopied slots
            for (int src = this.lastPosUsed; src >= 0; src--)
            {
                final int dst = src + offset;
                final Page srcPage = this.pages[src >>> PAGE_SHIFT];
                final int srcSlot = src & PAGE_MASK;
                if (srcPage != null && srcPage.isActive(srcSlot))
                {
                    copySlot(srcPage, srcSlot, ensurePage(dst >>> PAGE_SHIFT), dst & PAGE_MASK);
                }
                else
                {
                    final Page dstPage = this.pages[dst >>> PAGE_SHIFT];
                    if (dstPage != null)
                    {
                        dstPage.clearSlot(dst & PAGE_MASK);
                    }
                }
            }
            // Clear the newly vacated prefix slots [0 .. offset - 1]
            final int clearLimit = Math.min(this.lastPosUsed, offset - 1);
            for (int i = clearLimit; i >= 0; i--)
            {
                final Page p = this.pages[i >>> PAGE_SHIFT];
                if (p != null)
                {
                    p.clearSlot(i & PAGE_MASK);
                }
            }
            this.lastPosUsed += offset;
        }
    }

    /**
     * Condenses the time series by half until targetEndSecond fits within capacity.
     */
    private void condense(final int targetEndSecond)
    {
        while (targetEndSecond >= this.firstSecond + expandToSeconds(this.size))
        {
            condenseOneStep();
        }
    }

    /**
     * Performs a single step of temporal condensing: merges adjacent pairs of slots {@code (2k, 2k+1)}
     * into slot {@code k}, doubles the time width represented by each slot ({@code scale++}),
     * and releases any pages in the upper half of the buffer that are no longer used.
     */
    public void condenseOneStep()
    {
        if (this.lastPosUsed >= 0)
        {
            // Because PAGE_SIZE (64) is even, pair (i, i + 1) for even i always resides on the exact same source Page!
            final int maxPairStart = this.lastPosUsed & ~1;
            for (int i = 0; i <= maxPairStart; i += 2)
            {
                final int newPos = i >>> 1;
                final int dstPageIdx = newPos >>> PAGE_SHIFT;
                final int dstSlot = newPos & PAGE_MASK;

                final Page srcPage = this.pages[i >>> PAGE_SHIFT];
                final int s1 = i & PAGE_MASK;
                final int s2 = s1 + 1;

                final boolean a1 = srcPage != null && srcPage.isActive(s1);
                final boolean a2 = srcPage != null && srcPage.isActive(s2);

                if (!a1 && !a2)
                {
                    final Page dstPage = this.pages[dstPageIdx];
                    if (dstPage != null)
                    {
                        dstPage.clearSlot(dstSlot);
                    }
                }
                else if (!a1)
                {
                    copySlot(srcPage, s2, ensurePage(dstPageIdx), dstSlot);
                }
                else
                {
                    final Page dstPage = ensurePage(dstPageIdx);
                    copySlot(srcPage, s1, dstPage, dstSlot);
                    if (a2)
                    {
                        combineSlot(dstPage, dstSlot, srcPage, s2, true);
                    }
                }
            }

            // Clear trailing slots in the last active destination page and free all pages above it
            final int newLastPosUsed = this.lastPosUsed >> 1;
            final int lastDstPageIdx = newLastPosUsed >>> PAGE_SHIFT;
            final Page lastDstPage = this.pages[lastDstPageIdx];
            if (lastDstPage != null)
            {
                for (int s = (newLastPosUsed & PAGE_MASK) + 1; s < PAGE_SIZE; s++)
                {
                    lastDstPage.clearSlot(s);
                }
            }
            for (int p = lastDstPageIdx + 1; p < this.pages.length; p++)
            {
                this.pages[p] = null;
            }
            this.lastPosUsed = newLastPosUsed;
        }

        this.scale++;
    }

    /**
     * Condenses this series until it reaches targetScale.
     */
    public void condenseToScale(final int targetScale)
    {
        while (this.scale < targetScale)
        {
            condenseOneStep();
        }
    }

    /**
     * Returns an independent copy of this time series condensed to fit within the specified target resolution
     * (e.g. chart width in pixels). If this series already fits within targetWidth (i.e. {@code lastPosUsed < targetWidth}),
     * returns a copy at current scale. During condensing, peak metrics per second (such as peak error burst rate
     * and peak throughput) are preserved.
     *
     * @param targetWidth
     *            the target maximum number of slots (e.g. chart width in pixels)
     * @return a condensed copy suitable for report charts
     */
    public IntTimeSeries toResolution(final int targetWidth)
    {
        final IntTimeSeries copy = new IntTimeSeries(this);
        if (targetWidth > 0)
        {
            while (copy.lastPosUsed >= targetWidth)
            {
                copy.condenseOneStep();
            }
        }
        return copy;
    }

    /**
     * Merges another {@link IntTimeSeries} (typically from a worker thread) into this master instance.
     * <p>
     * <b>How Merging Works:</b>
     * <ol>
     *   <li><b>Fast-Path Adoption:</b> If this series is currently empty ({@code lastPosUsed == -1}) and
     *       {@code other} fits within capacity, {@code other}'s populated pages and summary statistics are
     *       deep-copied directly without slot-by-slot iteration.</li>
     *   <li><b>Timeline Alignment:</b> Otherwise, computes the combined time span {@code [minSecond .. maxSecond]},
     *       expands capacity up to {@link #maxCapacity} if both series are at scale 1, condenses this series if
     *       needed so the union span fits, and shifts slots right if {@code other} starts earlier than {@code this}.</li>
     *   <li><b>Page-by-Page Slot Combination:</b> Iterates only over non-null pages in {@code other}, mapping
     *       each active slot's timestamp into {@code this} series' slot coordinate space and combining via
     *       {@link #combineSlot(Page, int, Page, int, boolean)} with {@code isCondense = false}.</li>
     * </ol>
     *
     * @param other
     *            the other series to merge
     */
    public void merge(final IntTimeSeries other)
    {
        if (other == null || other.lastPosUsed == -1)
        {
            return;
        }

        // Fast-path: when merging into an empty series, clone other's populated pages directly
        if (this.lastPosUsed == -1)
        {
            if (other.size > this.size && this.size < this.maxCapacity)
            {
                this.growToCapacity(other.size);
            }
            if (other.lastPosUsed < this.size)
            {
                this.firstSecond = other.firstSecond;
                this.scale = other.scale;
                this.lastPosUsed = other.lastPosUsed;
                this.sumOfSquares = other.sumOfSquares;
                this.histogram.merge(other.histogram);
                this.totalCount = other.totalCount;
                this.totalErrors = other.totalErrors;
                this.totalSum = other.totalSum;
                this.minValue = other.minValue;
                this.maxValue = other.maxValue;
                final int maxPage = other.lastPosUsed >>> PAGE_SHIFT;
                for (int p = 0; p <= maxPage; p++)
                {
                    if (other.pages[p] != null)
                    {
                        this.pages[p] = new Page(other.pages[p]);
                    }
                }
                return;
            }
            this.firstSecond = other.firstSecond;
            this.scale = other.scale;
        }

        // Compute the union time span across both series
        final long minSecond = Math.min(this.firstSecond, other.firstSecond);
        final long maxSecond = Math.max(
            this.lastPosUsed >= 0 ? this.getLastSecond() : other.getLastSecond(),
            other.getLastSecond());

        if (this.scale == 1 && other.scale == 1)
        {
            final long span = maxSecond - minSecond + 1;
            if (span > this.size && this.size < this.maxCapacity)
            {
                growToCapacity((int) span);
            }
        }

        int targetScale = Math.max(this.scale, other.scale);
        while (maxSecond >= minSecond + ((long) this.size << (targetScale - 1)))
        {
            targetScale++;
        }

        this.condenseToScale(targetScale);

        if (minSecond < this.firstSecond)
        {
            this.shiftRight((int) minSecond);
            this.firstSecond = (int) minSecond;
        }

        // Walk only the allocated pages of 'other' and combine active slots into 'this'
        final int otherSlotWidth = other.getSlotWidth();
        final int limit = other.lastPosUsed;
        final int maxPage = limit >>> PAGE_SHIFT;
        for (int p = 0; p <= maxPage; p++)
        {
            final Page otherPage = other.pages[p];
            if (otherPage == null)
            {
                continue;
            }
            final int baseIdx = p << PAGE_SHIFT;
            final int slotLimit = Math.min(PAGE_SIZE - 1, limit - baseIdx);
            for (int s = 0; s <= slotLimit; s++)
            {
                if (otherPage.isActive(s))
                {
                    final int i = baseIdx + s;
                    final long entrySecond = other.firstSecond + ((long) i * otherSlotWidth);
                    final int targetPos = this.adjustToScale((int) (entrySecond - this.firstSecond));
                    final Page dstPage = ensurePage(targetPos >>> PAGE_SHIFT);
                    combineSlot(dstPage, targetPos & PAGE_MASK, otherPage, s, false);
                    this.lastPosUsed = Math.max(this.lastPosUsed, targetPos);
                }
            }
        }

        this.totalCount += other.totalCount;
        this.totalErrors += other.totalErrors;
        this.totalSum += other.totalSum;
        this.minValue = Math.min(this.minValue, other.minValue);
        this.maxValue = Math.max(this.maxValue, other.maxValue);
        this.sumOfSquares += other.sumOfSquares;
        this.histogram.merge(other.histogram);
    }

    /**
     * Returns the smallest second for which a value exists.
     */
    public long getFirstSecond()
    {
        if (firstSecond == DEFAULT)
        {
            throw new IllegalStateException("No first second available as no values have been added so far.");
        }
        return firstSecond;
    }

    /**
     * Returns the highest second for which a value exists.
     */
    public long getLastSecond()
    {
        if (firstSecond == DEFAULT)
        {
            throw new IllegalStateException("No first second available as no values have been added so far.");
        }
        return firstSecond + expandToSeconds(this.lastPosUsed);
    }

    /**
     * Returns the highest slot index that has been populated (or {@code -1} if no values have been added yet).
     */
    public int getLastPosUsed()
    {
        return lastPosUsed;
    }

    /**
     * Returns the current resolution scale level ({@code 1} = 1s per slot, {@code 2} = 2s per slot, {@code 3} = 4s per slot, etc.).
     */
    public int getScale()
    {
        return scale;
    }

    /**
     * Returns the time span in seconds represented by a single slot at the current scale ({@code 1 << (scale - 1)}).
     */
    public int getSlotWidth()
    {
        return 1 << (scale - 1);
    }

    /**
     * Returns the current slot capacity of this series (always a power of two between initial capacity and {@link #maxCapacity}).
     */
    public int getSize()
    {
        return size;
    }

    /**
     * Returns the maximum buffer capacity before condensing kicks in.
     *
     * @return the maximum capacity
     */
    public int getMaxCapacity()
    {
        return maxCapacity;
    }

    /**
     * Computes and returns overall summary statistics (count, errorCount, sum, min, max, mean, standard deviation, median)
     * across all samples ever added to this series. Note that these totals are accumulated directly as samples arrive
     * and are completely unaffected by slot condensing.
     */
    public Statistics getStatistics()
    {
        final Statistics stat = new Statistics();
        stat.count = this.totalCount;
        stat.errorCount = this.totalErrors;
        stat.sum = this.totalSum;
        if (this.totalCount > 0)
        {
            stat.minValue = this.minValue;
            stat.maxValue = this.maxValue;
            stat.mean = getMean();
            stat.standardDeviation = getStandardDeviation();
            stat.median = this.histogram.getMedianValue();
        }
        return stat;
    }

    /**
     * Returns the total number of samples added to this time series.
     */
    public long getCount()
    {
        return this.totalCount;
    }

    /**
     * Returns the sum of all sample values added to this time series.
     */
    public long getTotalValue()
    {
        return this.totalSum;
    }

    /**
     * Returns the total number of failed samples added to this time series.
     */
    public long getErrorCount()
    {
        return this.totalErrors;
    }

    /**
     * Returns the completion count for the given slot index without allocating an {@link IntTimeSeriesEntry}.
     */
    public int getSlotCount(final int pos)
    {
        if (pos < 0 || pos >= this.size)
        {
            return 0;
        }
        final Page page = this.pages[pos >>> PAGE_SHIFT];
        return page != null ? page.getCount(pos & PAGE_MASK) : 0;
    }

    /**
     * Returns the error count for the given slot index without allocating an {@link IntTimeSeriesEntry}.
     */
    public int getSlotErrorCount(final int pos)
    {
        if (pos < 0 || pos >= this.size)
        {
            return 0;
        }
        final Page page = this.pages[pos >>> PAGE_SHIFT];
        return page != null ? page.getErrorCount(pos & PAGE_MASK) : 0;
    }

    /**
     * Returns the peak 1-second error count for the given slot index without allocating an {@link IntTimeSeriesEntry}.
     */
    public int getSlotMaxErrorCount(final int pos)
    {
        if (pos < 0 || pos >= this.size)
        {
            return 0;
        }
        final Page page = this.pages[pos >>> PAGE_SHIFT];
        return page != null ? page.getMaxErrorCount(pos & PAGE_MASK) : 0;
    }

    /**
     * Returns the peak 1-second completion count for the given slot index without allocating an {@link IntTimeSeriesEntry}.
     */
    public int getSlotMaxCount(final int pos)
    {
        if (pos < 0 || pos >= this.size)
        {
            return 0;
        }
        final Page page = this.pages[pos >>> PAGE_SHIFT];
        return page != null ? page.getMaxCount(pos & PAGE_MASK) : 0;
    }

    /**
     * Returns the active concurrency count for the given slot index without allocating an {@link IntTimeSeriesEntry}.
     */
    public int getSlotConcurrentCount(final int pos)
    {
        if (pos < 0 || pos >= this.size)
        {
            return 0;
        }
        final Page page = this.pages[pos >>> PAGE_SHIFT];
        return page != null ? page.getConcurrentCount(pos & PAGE_MASK) : 0;
    }

    /**
     * Returns the total sum of values for the given slot index without allocating an {@link IntTimeSeriesEntry}.
     */
    public long getSlotTotalValue(final int pos)
    {
        if (pos < 0 || pos >= this.size)
        {
            return 0L;
        }
        final Page page = this.pages[pos >>> PAGE_SHIFT];
        return page != null ? page.getTotalValue(pos & PAGE_MASK) : 0L;
    }

    /**
     * Returns the integer average value for the given slot index without allocating an {@link IntTimeSeriesEntry}.
     */
    public int getSlotAverageValue(final int pos)
    {
        if (pos < 0 || pos >= this.size)
        {
            return 0;
        }
        final Page page = this.pages[pos >>> PAGE_SHIFT];
        if (page == null)
        {
            return 0;
        }
        final int s = pos & PAGE_MASK;
        final int count = page.getCount(s);
        return count == 0 ? 0 : (int) (page.getTotalValue(s) / count);
    }

    /**
     * Returns the minimum value for the given slot index (or 0 if empty) without allocating an {@link IntTimeSeriesEntry}.
     */
    public int getSlotMinimumValue(final int pos)
    {
        if (pos < 0 || pos >= this.size)
        {
            return 0;
        }
        final Page page = this.pages[pos >>> PAGE_SHIFT];
        if (page == null)
        {
            return 0;
        }
        final int min = page.getMinimum(pos & PAGE_MASK);
        return min == Integer.MAX_VALUE ? 0 : min;
    }

    /**
     * Returns the maximum value for the given slot index (or 0 if empty) without allocating an {@link IntTimeSeriesEntry}.
     */
    public int getSlotMaximumValue(final int pos)
    {
        if (pos < 0 || pos >= this.size)
        {
            return 0;
        }
        final Page page = this.pages[pos >>> PAGE_SHIFT];
        if (page == null)
        {
            return 0;
        }
        final int max = page.getMaximum(pos & PAGE_MASK);
        return max == Integer.MIN_VALUE ? 0 : max;
    }

    /**
     * Materializes an {@link IntTimeSeriesEntry} snapshot for the specified slot index.
     */
    public IntTimeSeriesEntry getEntry(final int pos)
    {
        if (pos < 0 || pos >= this.size)
        {
            return new IntTimeSeriesEntry();
        }
        final Page page = this.pages[pos >>> PAGE_SHIFT];
        return page != null ? page.toEntry(pos & PAGE_MASK) : new IntTimeSeriesEntry();
    }

    /**
     * Materializes and returns an array of {@link IntTimeSeriesEntry} snapshots for all slots in this series.
     * Prefer the primitive {@code getSlot*()} accessors in hot loops to avoid allocating {@code size} entry objects.
     */
    public IntTimeSeriesEntry[] getValues()
    {
        final IntTimeSeriesEntry[] values = new IntTimeSeriesEntry[this.size];
        for (int i = 0; i < this.size; i++)
        {
            values[i] = getEntry(i);
        }
        return values;
    }

    /**
     * Package-private inspection helper for unit tests: returns the number of currently allocated 64-slot pages.
     */
    int getAllocatedPageCount()
    {
        int count = 0;
        for (final Page p : this.pages)
        {
            if (p != null)
            {
                count++;
            }
        }
        return count;
    }

    /**
     * Package-private inspection helper for unit tests: returns the length of the page directory array.
     */
    int getPageDirectoryLength()
    {
        return this.pages.length;
    }

    /**
     * Package-private inspection helper for unit tests: returns true if the given page has allocated its errorCounts array.
     */
    boolean hasPageErrorCounts(final int pageIndex)
    {
        return this.pages[pageIndex] != null && this.pages[pageIndex].errorCounts != null;
    }

    /**
     * Package-private inspection helper for unit tests: returns true if the given page has allocated its concurrentCounts array.
     */
    boolean hasPageConcurrentCounts(final int pageIndex)
    {
        return this.pages[pageIndex] != null && this.pages[pageIndex].concurrentCounts != null;
    }

    /**
     * Package-private inspection helper for unit tests: returns true if the given page has allocated its maxCounts array.
     */
    boolean hasPageMaxCounts(final int pageIndex)
    {
        return this.pages[pageIndex] != null && this.pages[pageIndex].maxCounts != null;
    }

    /**
     * Package-private inspection helper for unit tests: returns true if the given page has allocated its maxErrorCounts array.
     */
    boolean hasPageMaxErrorCounts(final int pageIndex)
    {
        return this.pages[pageIndex] != null && this.pages[pageIndex].maxErrorCounts != null;
    }

    /**
     * Returns the estimated value at the given percentile ({@code 0.0 .. 100.0}) from the underlying {@link RuntimeHistogram}.
     */
    public int getPercentile(final double percentile)
    {
        return (int) this.histogram.getPercentile(percentile);
    }

    /**
     * Returns the estimated median (P50) value from the underlying {@link RuntimeHistogram}.
     */
    public int getMedianValue()
    {
        return (int) this.histogram.getMedianValue();
    }

    /**
     * Returns the underlying {@link RuntimeHistogram} tracking the value distribution.
     */
    public RuntimeHistogram getHistogram()
    {
        return histogram;
    }

    /**
     * Returns the population standard deviation of all values added to this series.
     */
    public double getStandardDeviation()
    {
        if (this.totalCount == 0)
        {
            return 0.0;
        }
        final double mean = getMean();
        final double variance = ((double) this.sumOfSquares / (double) this.totalCount) - (mean * mean);
        return Math.sqrt(Math.max(0.0, variance));
    }

    /**
     * Returns the arithmetic mean of all values added to this series (or {@code 0.0} if empty).
     */
    public double getMean()
    {
        return this.totalCount > 0 ? (double) this.totalSum / (double) this.totalCount : 0.0;
    }

    /**
     * Returns a histogram representation of the values added.
     */
    public List<HistogramBucket> toHistogram(final int bucketCount)
    {
        final int numBuckets = Math.max(1, bucketCount);
        final List<HistogramBucket> histogramBuckets = new ArrayList<>(numBuckets);

        if (this.histogram.getValueCount() == 0)
        {
            return histogramBuckets;
        }

        final Statistics stat = getStatistics();
        final double min = stat.minValue;
        final double max = stat.maxValue;
        final double bucketWidth = (max > min) ? (max - min) / numBuckets : 1.0;

        for (int i = 0; i < numBuckets; i++)
        {
            final double start = (i == 0) ? 0 : min + i * bucketWidth;
            final double rawEnd = (i == numBuckets - 1) ? min + (i + 1) * bucketWidth : (min + (i + 1) * bucketWidth) - 1;
            final double end = Math.max(start, rawEnd);

            final long count = this.histogram.getCountForValue((int) start, (int) end);
            histogramBuckets.add(new HistogramBucket((int) start, (int) end, count));
        }

        return histogramBuckets;
    }

    /**
     * Converts to an {@link XYIntervalSeries} suitable for response time distribution plots.
     */
    public XYIntervalSeries toHistogramSeries(final String seriesName, final int bucketCount)
    {
        final XYIntervalSeries series = new XYIntervalSeries(seriesName);
        final List<HistogramBucket> buckets = toHistogram(bucketCount);
        for (final HistogramBucket bucket : buckets)
        {
            if (bucket.count() > 0)
            {
                series.add(bucket.count(), 0, bucket.count(), bucket.endValue(), bucket.startValue(), bucket.endValue());
            }
        }
        return series;
    }

    /**
     * Converts to a JFreeChart {@link TimeSeries} where each item is an {@link IntMinMaxTimeSeriesDataItem}.
     */
    public TimeSeries toRunTimeTimeSeries(final String seriesName)
    {
        final TimeSeries timeSeries = new TimeSeries(seriesName);

        if (this.lastPosUsed >= 0)
        {
            final int slotWidth = getSlotWidth();
            for (int i = 0; i <= this.lastPosUsed; i++)
            {
                final Page page = this.pages[i >>> PAGE_SHIFT];
                if (page != null)
                {
                    final int s = i & PAGE_MASK;
                    if (page.getCount(s) > 0)
                    {
                        final long timeInSeconds = this.firstSecond + ((long) i * slotWidth);
                        final Second second = JFreeChartUtils.getSecond(timeInSeconds * 1000L);
                        timeSeries.add(new IntMinMaxTimeSeriesDataItem(second, page.toEntry(s)));
                    }
                }
            }
        }

        return timeSeries;
    }

    /**
     * Converts to a JFreeChart {@link TimeSeries} where each item is an {@link IntMinMaxTimeSeriesDataItem}.
     * Generic alias for {@link #toRunTimeTimeSeries(String)} suitable for min/max metric series (e.g. response size).
     *
     * @param seriesName
     *            the name of the time series
     * @return the time series
     */
    public TimeSeries toMinMaxTimeSeries(final String seriesName)
    {
        return toRunTimeTimeSeries(seriesName);
    }

    /**
     * Converts to a JFreeChart {@link TimeSeries} representing count per second.
     */
    public TimeSeries toCountPerSecondTimeSeries(final String seriesName)
    {
        final TimeSeries timeSeries = new TimeSeries(seriesName);

        if (this.lastPosUsed >= 0)
        {
            final int slotWidth = getSlotWidth();
            for (int i = 0; i <= this.lastPosUsed; i++)
            {
                final long timeInSeconds = this.firstSecond + ((long) i * slotWidth);
                final Second second = JFreeChartUtils.getSecond(timeInSeconds * 1000L);
                final double countPerSec = (double) getSlotCount(i) / slotWidth;
                timeSeries.add(new TimeSeriesDataItem(second, countPerSec));
            }
        }

        return timeSeries;
    }

    /**
     * Converts to a JFreeChart {@link TimeSeries} representing errors per second.
     * <p>
     * For bar charts representing peak outage burst rates, this outputs {@link IntTimeSeriesEntry#getMaxErrorCount()},
     * preserving the maximum 1-second failure spike observed even when the series has been condensed
     * for report chart resolution.
     */
    public TimeSeries toErrorsPerSecondTimeSeries(final String seriesName)
    {
        final TimeSeries timeSeries = new TimeSeries(seriesName);

        if (this.lastPosUsed >= 0)
        {
            final int slotWidth = getSlotWidth();
            for (int i = 0; i <= this.lastPosUsed; i++)
            {
                final long timeInSeconds = this.firstSecond + ((long) i * slotWidth);
                final Second second = JFreeChartUtils.getSecond(timeInSeconds * 1000L);
                final double errorRate = getSlotMaxErrorCount(i);
                timeSeries.add(new TimeSeriesDataItem(second, errorRate));
            }
        }

        return timeSeries;
    }

    /**
     * Converts to a JFreeChart {@link TimeSeries} representing error rate in percent (errors * 100.0 / total count) for each slot.
     */
    public TimeSeries toErrorRateTimeSeries(final String seriesName)
    {
        final TimeSeries timeSeries = new TimeSeries(seriesName);

        if (this.lastPosUsed >= 0)
        {
            final int slotWidth = getSlotWidth();
            for (int i = 0; i <= this.lastPosUsed; i++)
            {
                final int count = getSlotCount(i);
                if (count > 0)
                {
                    final long timeInSeconds = this.firstSecond + ((long) i * slotWidth);
                    final Second second = JFreeChartUtils.getSecond(timeInSeconds * 1000L);
                    final double rate = 100.0 * getSlotErrorCount(i) / count;
                    timeSeries.add(second, rate);
                }
            }
        }

        return timeSeries;
    }

    /**
     * Converts to a JFreeChart {@link XYIntervalSeries} representing a vertical histogram (for request runtime charts).
     */
    public XYIntervalSeries toVerticalHistogramSeries(final String seriesName, final int bucketCount)
    {
        final XYIntervalSeries series = new XYIntervalSeries(seriesName);
        final List<HistogramBucket> buckets = toHistogram(bucketCount);
        for (final HistogramBucket bucket : buckets)
        {
            if (bucket.count() > 0)
            {
                series.add(bucket.endValue(), bucket.startValue(), bucket.endValue(), bucket.count(), 0, bucket.count());
            }
        }
        return series;
    }

    public static record HistogramBucket(int startValue, int endValue, long count)
    {
        @Override
        public String toString()
        {
            return startValue + ", " + endValue + ", " + count;
        }
    }

    public static class Statistics
    {
        public long count = 0;
        public long errorCount = 0;
        public long sum = 0;
        public int maxValue = 0;
        public int minValue = 0;
        public double mean = 0.0;
        public double standardDeviation = 0.0;
        public double median = 0.0;
    }
}
