/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.util.rework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.xceptance.xlt.report.util.RuntimeHistogram;
import com.xceptance.xlt.report.util.lucene.BitUtil;

/**
 * A {@link IntTimeSeries} maintains different statistics, like minimum, maximum, and count, for values generated at a
 * certain time.
 * <p>
 * A {@link IntTimeSeries} is fixed-sized, but self-managing. If the distance between the smallest and greatest
 * time-stamp is greater than the value set size, two consecutive values are merged into one. This means the time period
 * for which values can be added to this set can be arbitrary long.
 */
public class IntTimeSeries
{
    /**
     * The default initial value set size.
     */
    public static final int DEFAULT_SIZE = 3600;

    /**
     * Where we start from if nothing is there yet.
     */
    private static final int DEFAULT = 2_147_385_000; // 2037-12-31 00:00:00

    /**
     * The quantile calculation.
     */
    private final RuntimeHistogram histogram;

    /**
     * The smallest time [s] for which a min/max value exists.
     * This is a raw value and never scales
     */
    private int firstSecond;

    /**
     * What position in the values array is the max used
     */
    private int lastPosUsed;

    /**
     * The number of seconds a single min/max value represents. Always a power of 2.
     */
    private int scale = 1;

    /**
     * The min/max values maintained by this value set.
     */
    private final IntTimeSeriesEntry[] values;

    /**
     * The minimum data size to store
     */
    private final int size;

    /**
     * The sum of the square of all values.
     */
    private double sumOfSquares;

    /**
     * Creates a {@link IntTimeSeries} instance with a size of {@link #DEFAULT_SIZE}.
     */
    public IntTimeSeries()
    {
        this(DEFAULT_SIZE);
    }

    /**
     * Creates a {@link IntTimeSeries} instance with the specified size. 
     * The minimum resolution is 1 second, so a size of 3600 would allow to store min/max values for one hour with one second resolution.
     * If the start size is too small, we are losing data when condensing happens,
     * hence we are ensuring our size is a power of two.
     *
     * @param size
     *            the size
     */
    public IntTimeSeries(final int size)
    {
        this.size = BitUtil.nextHighestPowerOfTwo(size);
        this.values = new IntTimeSeriesEntry[this.size];

        histogram = new RuntimeHistogram(10);
        
        // fill, so we don't have to check later for nulls
        Arrays.setAll(this.values, __ -> new IntTimeSeriesEntry());

        this.firstSecond = DEFAULT;
        this.lastPosUsed = -1;
    }

    /**
     * Adds a value for a certain time-stamp to this value set.
     *
     * @param startTime
     *            the time-stamp
     * @param endTime
     *           the end time-stamp
     * @param value
     *            the value
     * @param failed
     *           whether the value represents a failed measurement
     */
    public void addValue(final long startTime, final long endTime, final int value, final boolean failed)
    {
        // get the corresponding second, this gives us a year 2038 problem, but we don't care yet
        // mul is faster than div on x84 for that example
        final int startSecond = (int) (startTime * 0.001);
        final int endSecond = (int) (endTime * 0.001);

        if (startSecond - this.firstSecond < 0)
        {
            shiftRight(startSecond);
            this.firstSecond = startSecond;
        }
        else if (endSecond >= this.firstSecond + expandToSeconds(this.size))
        {
            // we would overrun to the right, so let's shrink until it fits
            condense(endSecond);
        }

        int pos = adjustToScale(startSecond - this.firstSecond);
        this.values[pos].updateValue(value, failed);

        // track the true concurrency of calls using start and end time
        // if the measurement is in that second, even only for a fraction of it,
        // we count it as one, we do that only for all extra seconds because
        // the first one is already counted above, if we scaled, the don't set
        // that accordingly for only the relevant seconds
        for (int i = startSecond + 1; i <= endSecond; i = i + this.scale)
        {
            pos = adjustToScale(i - this.firstSecond);
            this.values[pos].updateConcurrency();
        }
        this.lastPosUsed = Math.max(pos, this.lastPosUsed);

        // because none of our other value trackers produces that, we keep that directly here
        this.sumOfSquares += Math.pow(value, 2);

        // update the reservoir sample
        this.histogram.addValue(value);
    }

    /**
     * Adds a value for a certain time-stamp to this value set. This is likely
     * mostly to keep existing test cases working. 
     *
     * @param startTime
     *            the time-stamp
     * @param value
     *            the value
     * @param failed
     *           whether the value represents a failed measurement
     */
    public void addValue(final long startTime, final int value, final boolean failed)
    {
        addValue(startTime, startTime, value, failed);
    }

    /**
     * Just make sure we get the size of each slot right, we start with a second
     * and increase that by factor 2 every round
     * 
     * @param v the unscaled value
     * @return the adjusted scaled value
     */
    private int adjustToScale(final int v)
    {
        return v >> (this.scale - 1);
    }

    private int expandToSeconds(final int pos)
    {
        return pos << (this.scale - 1);
    }

    private void shiftRight(final int second)
    {
        if (this.lastPosUsed == -1)
        {
            // nothing important done yet, we are safe
            return;
        }

        // if our last second would be outside the new range, we
        // have to condense
        final int offset = adjustToScale(this.firstSecond) - adjustToScale(second);
        if (this.lastPosUsed + offset >= this.size)
        {
            // we have to condense it unfortunately, because when we shift right
            // we would move values out of the range
            // calculate the new max second in that set and set it as goal for condensing
            condense(second + expandToSeconds(this.lastPosUsed));
        }

        var newOffset = adjustToScale(this.firstSecond) - adjustToScale(second);
        System.arraycopy(this.values, 0, this.values, newOffset, this.size - newOffset);

        // overwrite the freed up space with new entries
        for (int i = 0; i < newOffset; i++)
        {
            this.values[i] = new IntTimeSeriesEntry();
        }

        this.lastPosUsed = this.lastPosUsed + newOffset;
    }

    private void condense(final int second)
    {
        var l = this.size;

        do
        {
            // join every second value into one, fill the rest up with 
            // new entries
            int newPos = 0;
            for (int i = 0; i < l - 1; i = i + 2)
            {
                final IntTimeSeriesEntry v1 = this.values[i];
                final IntTimeSeriesEntry v2 = this.values[i + 1];
                this.values[newPos++] = v1.merge(v2);
            }
            for (int i = newPos; i < l; i++)
            {
                this.values[i] = new IntTimeSeriesEntry();
            }

            scale++;

            // next round, only half the size must be processed
            // to avoid that we lose data, we must ensure we keep it even
            // when it became odd and increase to the next even number again
            l = l >> 1;
            this.lastPosUsed = this.lastPosUsed >> 1;
        }
        while (second >= firstSecond + expandToSeconds(this.size));
    }

    /**
     * Returns the smallest second for which a min/max value exists in this set.
     *
     * @return the first second
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
     * Returns the smallest second for which a min/max value exists in this set.
     *
     * @return the first second
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
     * Returns the raw scale number
     *
     * @return the scale
     */
    public int getScale()
    {
        return scale;
    }

    /**
     * Returns the number of seconds a single min/max value represents. Always a power of 2.
     *
     * @return the scale
     */
    public int getSlotWidth()
    {
        return 1 << (scale - 1);
    }

    /**
     * Returns the size of this value set.
     * 
     * @return the size
     */
    public int getSize()
    {
        return size;
    }

    /**
     * Returns the basic information about this value set.
     * 
     * @return the statistics
     */
    public Statistics getStatistics()
    {
        return calculateOverviewData();
    }
    
    /**
     * Returns the total number of values added to this value set.
     *
     * @return the number of values
     */
    public long getCount()
    {
        final Statistics stat = calculateOverviewData();
        return stat.count;
    }

    /**
     * Returns the total sum of all values added to this value set.
     *
     * @return the sum of all values
     */
    public long getTotalValue()
    {
        final Statistics stat = calculateOverviewData();
        return stat.sum;
    }
    
    /**
     * Returns the total number of values marked with error added to this value set.
     *
     * @return the number of values with errors
     */
    public long getErrorCount()
    {
        final Statistics stat = calculateOverviewData();
        return stat.errorCount;
    }

    /**
     * Returns the min/max values maintained by this set.
     *
     * @return the min/max values
     */
    public IntTimeSeriesEntry[] getValues()
    {
        return values;
    }

    /**
     * Returns the percentile value from the histogram 
     * 
     * @param percentile the desired percentile (0..100)
     * @return the percentile value
     */
    public int getPercentile(final double percentile)
    {
        return (int) Math.round(this.histogram.getPercentile(percentile));
    }

    /**
     * Returns the standard deviation of the values added.
     * 
     * @return the standard deviation
     */
    public double getStandardDeviation()
    {
        final Statistics stat = calculateOverviewData();
        
        // in case we don't have any values, avoid division by zero
        if (stat.count == 0)
        {
            return 0.0;
        }
        final double mean = (double)stat.sum / (double)stat.count;

        return Math.sqrt(sumOfSquares / (double)stat.count - mean * mean);
    }

    /**
     * Returns an overview data object containing aggregated statistics.
     * 
     * @return the overview data
     */
    private Statistics calculateOverviewData()
    {
        final Statistics stat = new Statistics();
        Arrays.stream(this.values).forEach(e -> 
        {
            stat.count += e.getCount();
            stat.sum += e.getTotalValue();
            stat.errorCount += e.getErrorCount();
            stat.maxValue = Math.max(stat.maxValue, e.getMaximumValue());
            stat.minValue = Math.max(stat.minValue, e.getMinimumValue());
        });
        return stat;
    }
    
    /**
     * Returns the mean of the values added.
     * 
     * @return the mean
     */
    public double getMean()
    {
        final Statistics stat = calculateOverviewData();
            
        // avoid division by zero
        if (stat.count == 0)
        {
            return 0.0;
        }
        
        return (double)stat.sum / (double)stat.count;
    }

    /**
     * Returns an histogram representation of the values added.
     * 
     * @param bucketCount the number of desired buckets
     * @return a list of histogram buckets according to the specified bucket count
     */
    public List<HistogramBucket> toHistogram(final int bucketCount)
    {
        final List<HistogramBucket> histogramBuckets = new ArrayList<>(bucketCount);

        if (this.histogram.getValueCount() == 0)
        {
            return histogramBuckets;
        }

        final Statistics stat = calculateOverviewData();

        final double min = stat.minValue;
        final double max = stat.maxValue;
        final double bucketWidth = (max - min) / bucketCount;

        
        for (int i = 0; i < bucketCount; i++)
        {
            var start = i == 0 ? 0 : min + i * bucketWidth;
            var end = min + (i + 1) * bucketWidth;
            
            var count = this.histogram.getCountForValue(start, end);
            
            histogram.add(new HistogramBucket(
                                              (int) Math.round(i == 0 ? 0 : sp[i - 1]), 
                                              (int) Math.round(sp[i]), 
                                              (int) Math.round(buckets[i] * this.getCount())));
        }

        return histogram;
    }

    public static record HistogramBucket(int startValue, int endValue, int count)
    {
    }

    public static class Statistics
    {
        public long count = 0;
        public long errorCount = 0;
        public long sum = 0;
        public int maxValue = Integer.MIN_VALUE;
        public int minValue = Integer.MAX_VALUE;
    }
}
