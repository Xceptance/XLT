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

/**
 * A {@link IntTimeSeries} maintains different statistics, like minimum, maximum, count, and error count,
 * for values generated over time.
 * <p>
 * A {@link IntTimeSeries} is fixed-sized, but self-managing. If the distance between the smallest and greatest
 * timestamp is greater than the value set size, two consecutive values are merged into one. This means the time period
 * for which values can be added to this set can be arbitrarily long.
 */
public class IntTimeSeries
{
    /**
     * The default initial capacity of a time series (rounded up to 4096 = ~1.1 hours at 1s resolution).
     */
    /**
     * The default initial capacity of a time series (rounded up to 4096 = ~1.1 hours at 1s resolution).
     * Slots are lazily instantiated to keep memory minimal.
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
     * Sentinel value indicating no data has been added yet.
     */
    private static final int DEFAULT = 2_147_385_000; // 2037-12-31 00:00:00

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
     * The scale level. A single slot represents (1 << (scale - 1)) seconds. Always >= 1.
     */
    private int scale = 1;

    /**
     * The min/max/count/error entries maintained by this time series.
     * Slots are lazily instantiated as records arrive to avoid creating millions of empty
     * objects across multi-threaded worker report providers.
     */
    private IntTimeSeriesEntry[] values;

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
     * Creates a {@link IntTimeSeries} instance with {@link #DEFAULT_INITIAL_CAPACITY} (1024)
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
        this.values = new IntTimeSeriesEntry[this.size];

        this.histogram = new RuntimeHistogram(8);

        // Slots are allocated lazily upon first use to keep memory footprint minimal
        this.firstSecond = DEFAULT;
        this.lastPosUsed = -1;
    }

    /**
     * Copy constructor. Creates an independent deep copy of the given {@link IntTimeSeries}.
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
        this.values = new IntTimeSeriesEntry[this.size];
        for (int i = 0; i <= other.lastPosUsed && i < this.size; i++)
        {
            if (other.values[i] != null)
            {
                this.values[i] = new IntTimeSeriesEntry(other.values[i]);
            }
        }
    }

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

        if (this.firstSecond == DEFAULT)
        {
            this.firstSecond = startSecond;
        }
        else if (startSecond < this.firstSecond)
        {
            shiftRight(startSecond);
            this.firstSecond = startSecond;
        }

        final long requiredSeconds = (long) endSecond - this.firstSecond + 1;
        if (this.scale == 1 && this.size < this.maxCapacity && requiredSeconds > this.size)
        {
            growToCapacity((int) Math.min(this.maxCapacity, requiredSeconds));
        }

        if (endSecond >= this.firstSecond + expandToSeconds(this.size))
        {
            condense(endSecond);
        }

        final int startPos = adjustToScale(startSecond - this.firstSecond);
        final int endPos = Math.max(startPos, Math.min(this.size - 1, adjustToScale(endSecond - this.firstSecond)));

        // Record the completion value (runtime), completion count, and failure status
        // at the time the event completed (endPos). This matches the historical XLT completion
        // semantics and ensures throughput (completed operations/sec), response time, and
        // error rates align with the actual completion/failure moment (preventing premature
        // smearing of outage error spikes backwards in time).
        IntTimeSeriesEntry endEntry = this.values[endPos];
        if (endEntry == null)
        {
            endEntry = new IntTimeSeriesEntry();
            this.values[endPos] = endEntry;
        }
        endEntry.updateValue(value, failed);

        // Track active concurrency across the duration of the operation prior to completion [startPos .. endPos - 1].
        // Concurrency for endPos was already incremented by updateValue() above.
        for (int p = startPos; p < endPos; p++)
        {
            IntTimeSeriesEntry pEntry = this.values[p];
            if (pEntry == null)
            {
                pEntry = new IntTimeSeriesEntry();
                this.values[p] = pEntry;
            }
            pEntry.updateConcurrency();
        }

        this.lastPosUsed = Math.max(endPos, this.lastPosUsed);

        final int v = value < 0 ? 0 : value;
        this.totalCount++;
        this.totalSum += v;
        if (failed)
        {
            this.totalErrors++;
        }
        if (v > this.maxValue)
        {
            this.maxValue = v;
        }
        if (v < this.minValue)
        {
            this.minValue = v;
        }

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
     * up to {@link #maxCapacity}. Existing entries are retained in their current slots, and newly allocated
     * slots are left null for lazy instantiation upon first write.
     *
     * @param targetCapacity
     *            the target buffer capacity to achieve
     */
    private void growToCapacity(final int targetCapacity)
    {
        int newSize = this.size;
        while (newSize < targetCapacity && newSize < this.maxCapacity)
        {
            newSize <<= 1;
        }
        if (newSize > this.maxCapacity)
        {
            newSize = this.maxCapacity;
        }

        if (newSize > this.size)
        {
            final IntTimeSeriesEntry[] newValues = new IntTimeSeriesEntry[newSize];
            System.arraycopy(this.values, 0, newValues, 0, this.size);
            this.values = newValues;
            this.size = newSize;
        }
    }

    /**
     * Shifts entries to the right to make room for an earlier second.
     */
    private void shiftRight(final int second)
    {
        if (this.lastPosUsed == -1)
        {
            return;
        }

        int offset = adjustToScale(this.firstSecond) - adjustToScale(second);
        while (this.lastPosUsed + offset >= this.size)
        {
            if (this.scale == 1 && this.size < this.maxCapacity)
            {
                final int requiredSize = this.lastPosUsed + offset + 1;
                growToCapacity(Math.min(this.maxCapacity, requiredSize));
                offset = adjustToScale(this.firstSecond) - adjustToScale(second);
                if (this.lastPosUsed + offset < this.size)
                {
                    break;
                }
            }
            condenseOneStep();
            offset = adjustToScale(this.firstSecond) - adjustToScale(second);
        }

        if (offset > 0)
        {
            System.arraycopy(this.values, 0, this.values, offset, this.size - offset);
            Arrays.fill(this.values, 0, offset, null);
            this.lastPosUsed = Math.min(this.size - 1, this.lastPosUsed + offset);
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
     * Performs a single step of condensing: merges adjacent pairs of slots, doubles slot width,
     * and clears the upper half of the buffer.
     */
    public void condenseOneStep()
    {
        final int l = this.size;
        int newPos = 0;
        for (int i = 0; i < l - 1; i += 2)
        {
            final IntTimeSeriesEntry v1 = this.values[i];
            final IntTimeSeriesEntry v2 = this.values[i + 1];
            if (v1 == null && v2 == null)
            {
                this.values[newPos++] = null;
            }
            else if (v1 == null)
            {
                this.values[newPos++] = v2;
            }
            else if (v2 == null)
            {
                this.values[newPos++] = v1;
            }
            else
            {
                // Condense adjacent chronological slots into one wider time interval,
                // preserving peak 1-second burst rates via Math.max
                this.values[newPos++] = v1.condenseWith(v2);
            }
        }
        Arrays.fill(this.values, newPos, l, null);

        this.scale++;
        this.lastPosUsed = this.lastPosUsed >> 1;
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
        if (targetWidth > 0 && copy.lastPosUsed >= targetWidth)
        {
            while (copy.lastPosUsed >= targetWidth)
            {
                copy.condenseOneStep();
            }
        }
        return copy;
    }

    /**
     * Merges another {@link IntTimeSeries} into this instance.
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

        if (this.lastPosUsed == -1)
        {
            if (other.size > this.size)
            {
                this.growToCapacity(other.size);
            }
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
            for (int i = 0; i <= other.lastPosUsed; i++)
            {
                if (other.values[i] != null)
                {
                    this.values[i] = new IntTimeSeriesEntry(other.values[i]);
                }
            }
            return;
        }

        final long minSecond = Math.min(this.firstSecond, other.firstSecond);
        final long maxSecond = Math.max(this.getLastSecond(), other.getLastSecond());

        if (this.scale == 1 && other.scale == 1)
        {
            final long span = maxSecond - minSecond + 1;
            if (span > this.size && this.size < this.maxCapacity)
            {
                growToCapacity((int) Math.min(this.maxCapacity, span));
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

        final int otherSlotWidth = other.getSlotWidth();
        final int limit = Math.min(other.lastPosUsed, other.size - 1);
        for (int i = 0; i <= limit; i++)
        {
            final IntTimeSeriesEntry otherEntry = other.values[i];
            if (otherEntry != null && (otherEntry.getCount() > 0 || otherEntry.getConcurrentCount() > 0))
            {
                final long entrySecond = other.firstSecond + ((long) i * otherSlotWidth);
                int targetPos = this.adjustToScale((int) (entrySecond - this.firstSecond));
                if (targetPos < 0)
                {
                    targetPos = 0;
                }
                else if (targetPos >= this.size)
                {
                    targetPos = this.size - 1;
                }
                if (this.values[targetPos] == null)
                {
                    this.values[targetPos] = new IntTimeSeriesEntry(otherEntry);
                }
                else
                {
                    this.values[targetPos].merge(otherEntry);
                }
                this.lastPosUsed = Math.max(this.lastPosUsed, targetPos);
            }
        }

        this.totalCount += other.totalCount;
        this.totalErrors += other.totalErrors;
        this.totalSum += other.totalSum;
        if (this.minValue == Integer.MAX_VALUE)
        {
            this.minValue = other.minValue;
            this.maxValue = other.maxValue;
        }
        else if (other.minValue != Integer.MAX_VALUE)
        {
            this.minValue = Math.min(this.minValue, other.minValue);
            this.maxValue = Math.max(this.maxValue, other.maxValue);
        }
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

    public int getLastPosUsed()
    {
        return lastPosUsed;
    }

    public int getScale()
    {
        return scale;
    }

    public int getSlotWidth()
    {
        return 1 << (scale - 1);
    }

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
            stat.mean = (double) this.totalSum / (double) this.totalCount;
            final double variance = ((double) this.sumOfSquares / (double) this.totalCount) - (stat.mean * stat.mean);
            stat.standardDeviation = variance > 0.0 ? Math.sqrt(variance) : 0.0;
            stat.median = this.histogram.getMedianValue();
        }
        else
        {
            stat.minValue = 0;
            stat.maxValue = 0;
            stat.mean = 0.0;
            stat.standardDeviation = 0.0;
            stat.median = 0.0;
        }
        return stat;
    }

    public long getCount()
    {
        return this.totalCount;
    }

    public long getTotalValue()
    {
        return this.totalSum;
    }

    public long getErrorCount()
    {
        return this.totalErrors;
    }

    public IntTimeSeriesEntry[] getValues()
    {
        for (int i = 0; i < this.size; i++)
        {
            if (this.values[i] == null)
            {
                this.values[i] = new IntTimeSeriesEntry();
            }
        }
        return values;
    }

    public int getPercentile(final double percentile)
    {
        return (int) this.histogram.getPercentile(percentile);
    }

    public int getMedianValue()
    {
        return (int) this.histogram.getMedianValue();
    }

    public RuntimeHistogram getHistogram()
    {
        return histogram;
    }

    public double getStandardDeviation()
    {
        if (this.totalCount == 0)
        {
            return 0.0;
        }
        final double mean = (double) this.totalSum / (double) this.totalCount;
        final double variance = ((double) this.sumOfSquares / (double) this.totalCount) - (mean * mean);
        return variance > 0.0 ? Math.sqrt(variance) : 0.0;
    }

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
                final IntTimeSeriesEntry entry = this.values[i];
                if (entry != null && entry.getCount() > 0)
                {
                    final long timeInSeconds = this.firstSecond + ((long) i * slotWidth);
                    final Second second = JFreeChartUtils.getSecond(timeInSeconds * 1000L);
                    timeSeries.add(new IntMinMaxTimeSeriesDataItem(second, entry));
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
                final IntTimeSeriesEntry entry = this.values[i];
                final long timeInSeconds = this.firstSecond + ((long) i * slotWidth);
                final Second second = JFreeChartUtils.getSecond(timeInSeconds * 1000L);
                final double countPerSec = entry != null ? (double) entry.getCount() / slotWidth : 0.0;
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
                final IntTimeSeriesEntry entry = this.values[i];
                final long timeInSeconds = this.firstSecond + ((long) i * slotWidth);
                final Second second = JFreeChartUtils.getSecond(timeInSeconds * 1000L);
                final double errorRate = entry != null ? entry.getMaxErrorCount() : 0.0;
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
                final IntTimeSeriesEntry entry = this.values[i];
                if (entry != null && entry.getCount() > 0)
                {
                    final long timeInSeconds = this.firstSecond + ((long) i * slotWidth);
                    final Second second = JFreeChartUtils.getSecond(timeInSeconds * 1000L);
                    final double rate = 100.0 * entry.getErrorCount() / entry.getCount();
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
