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
 * Picks the bucket interval for the AI data time series and reduces the collected value sets down to it.
 * <p>
 * A fixed interval does not work at both ends of the range: at one minute a five minute test yields five rows and hides
 * its ramp-up entirely, while a twenty-four hour test yields 1,440 rows, more than the rest of the AI data file put
 * together. So the row count is the target and the interval follows from it, rounded up to a value a reader recognises.
 * <p>
 * The divisor is chosen so that a two hour test lands on one minute buckets. Everything shorter or longer follows from
 * that anchor, and the row count stays between roughly 60 and 120 across a 300-fold range of test durations.
 * <p>
 * The ladder and the divisor are deliberately not configurable. A property here would let someone ask for a resolution
 * the collected data cannot deliver, or for a series that outweighs everything else in the file, and neither would
 * report an error - it would just quietly get expensive.
 */
public final class TimeSeriesDownsampler
{
    /**
     * Intervals a reader recognises, in seconds. The chosen interval is always one of these.
     */
    private static final int[] INTERVAL_LADDER =
        {
            1, 5, 10, 15, 30, 60, 120, 180, 300, 600, 900, 1800, 3600
        };

    /**
     * The row count the interval selection aims at. 120 rather than 90 so that a two hour test comes out at one minute.
     */
    private static final int TARGET_ROWS = 120;

    private TimeSeriesDownsampler()
    {
    }

    /**
     * Returns the bucket interval to use for a test of the given duration.
     *
     * @param durationSeconds
     *            the test duration in seconds
     * @param sourceResolutionSeconds
     *            the resolution of the collected data; the result is never finer than this, because a finer interval
     *            would claim a precision the data does not have
     * @return the interval in seconds
     */
    public static int selectInterval(final long durationSeconds, final int sourceResolutionSeconds)
    {
        final long wanted = Math.max(1, (durationSeconds + TARGET_ROWS - 1) / TARGET_ROWS);

        int interval = INTERVAL_LADDER[INTERVAL_LADDER.length - 1];
        for (final int candidate : INTERVAL_LADDER)
        {
            if (candidate >= wanted)
            {
                interval = candidate;
                break;
            }
        }

        return Math.max(interval, Math.max(1, sourceResolutionSeconds));
    }

    /**
     * Reduces a min/max value set to buckets of the given interval, returning the mean per bucket.
     *
     * @param values
     *            the collected values, already bucketed at their own scale
     * @param firstSecond
     *            the second the series starts at
     * @param buckets
     *            the number of output buckets
     * @param interval
     *            the output bucket size in seconds
     * @return one mean per bucket, or 0 where a bucket holds no data
     */
    public static int[] meanPerBucket(final IntMinMaxValueSet values, final long firstSecond, final int buckets,
                                      final int interval)
    {
        final long[] weightedSum = new long[buckets];
        final long[] weight = new long[buckets];

        if (values != null)
        {
            final IntMinMaxValue[] data = values.getValues();
            final int scale = Math.max(1, values.getScale());
            final long start = values.getFirstSecond();

            for (int i = 0; i < data.length; i++)
            {
                final IntMinMaxValue value = data[i];
                if (value == null)
                {
                    continue;
                }

                final int bucket = bucketOf(start + (long) i * scale, firstSecond, buckets, interval);
                if (bucket >= 0)
                {
                    // weight by sample count so that a busy source bucket counts for more than a quiet one
                    weightedSum[bucket] += (long) value.getAverageValue() * value.getValueCount();
                    weight[bucket] += value.getValueCount();
                }
            }
        }

        final int[] means = new int[buckets];
        for (int i = 0; i < buckets; i++)
        {
            means[i] = weight[i] == 0 ? 0 : (int) Math.round((double) weightedSum[i] / weight[i]);
        }

        return means;
    }

    /**
     * Reduces a per-second value set to buckets of the given interval, returning the per-second rate in each bucket.
     *
     * @param values
     *            the collected per-second counts
     * @param firstSecond
     *            the second the series starts at
     * @param buckets
     *            the number of output buckets
     * @param interval
     *            the output bucket size in seconds
     * @return one rate per bucket
     */
    public static double[] ratePerBucket(final ValueSet values, final long firstSecond, final int buckets,
                                         final int interval)
    {
        final long[] sums = new long[buckets];

        if (values != null)
        {
            final int[] data = values.getValues();
            final long start = values.getFirstSecond();

            for (int i = 0; i < data.length; i++)
            {
                final int bucket = bucketOf(start + i, firstSecond, buckets, interval);
                if (bucket >= 0)
                {
                    sums[bucket] += data[i];
                }
            }
        }

        final double[] rates = new double[buckets];
        for (int i = 0; i < buckets; i++)
        {
            rates[i] = (double) sums[i] / interval;
        }

        return rates;
    }

    /**
     * Returns the output bucket the given second falls into, or -1 when it falls outside the series.
     */
    private static int bucketOf(final long second, final long firstSecond, final int buckets, final int interval)
    {
        final long bucket = (second - firstSecond) / interval;

        return (bucket < 0 || bucket >= buckets) ? -1 : (int) bucket;
    }
}
