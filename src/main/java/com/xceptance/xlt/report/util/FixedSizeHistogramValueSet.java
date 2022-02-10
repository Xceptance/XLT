/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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

import org.jfree.data.xy.XYIntervalSeries;

import com.xceptance.common.util.ParameterCheckUtils;

/**
 * A histogram value set with a fixed number N of buckets. The value set has an initial value range of [0..N-1], i.e.
 * each bucket has the initial width of 1. Once values greater than N-1 are added to this set, the value range is scaled
 * up by scaling up the bucket width as necessary.
 */
public class FixedSizeHistogramValueSet
{
    /**
     * The number of buckets in this set. Always a multiple of 2.
     */
    private final int bucketCount;

    /**
     * The current width of a bucket. Always a multiple of 2.
     */
    private int bucketWidth;

    /**
     * The number of values added to each bucket.
     */
    private final int countPerBucket[];

    /**
     * Creates a new {@link FixedSizeHistogramValueSet} object with the given number of buckets. If the bucket count is
     * an odd number, it will be made even by adding a bucket.
     *
     * @param bucketCount
     *            the number of buckets
     */
    public FixedSizeHistogramValueSet(final int bucketCount)
    {
        ParameterCheckUtils.isGreaterThan(bucketCount, 0, "bucketCount");

        // make the number of buckets a multiple of 2 to make it easy for us
        this.bucketCount = (bucketCount % 2 == 1) ? bucketCount + 1 : bucketCount;

        countPerBucket = new int[this.bucketCount];
        bucketWidth = 1;
    }

    /**
     * Adds a positive value to this value set.
     *
     * @param value
     *            the value
     */
    public void addValue(int value)
    {
        // TODO: make it work for negative values as well
        if (value < 0)
        {
            // ignore for now
            return;
        }

        // adjust the value according to the current bucket width
        value = value / bucketWidth;

        // make the value fit into this set by scaling the bucket width as necessary
        while (value >= bucketCount)
        {
            scale();
            value = value / 2;
        }

        // finally increment the count in the respective bucket
        countPerBucket[value]++;
    }

    /**
     * Returns the configured number of buckets.
     *
     * @return the bucket count
     */
    public int getBucketCount()
    {
        return bucketCount;
    }

    /**
     * Returns the number of values added to each bucket.
     *
     * @return the count per bucket
     */
    public int[] getCountPerBucket()
    {
        return countPerBucket;
    }

    /**
     * Returns the highest count stored to any of the buckets.
     *
     * @return the maximum count
     */
    public int getMaximumCount()
    {
        int maxCount = 0;

        for (final int count : countPerBucket)
        {
            maxCount = Math.max(maxCount, count);
        }

        return maxCount;
    }

    /**
     * Converts this value set to an {@link XYIntervalSeries} object, where each non-empty bucket is represented as a
     * horizontal bar.
     *
     * @param seriesName
     *            the name of the series
     * @return the series
     */
    public XYIntervalSeries toSeries(final String seriesName)
    {
        final XYIntervalSeries series = new XYIntervalSeries(seriesName);

        int bucketStartValue = 0;
        int bucketEndValue = bucketWidth;

        for (int i = 0; i < countPerBucket.length; i++)
        {
            final int count = countPerBucket[i];
            if (count > 0)
            {
                series.add(count, 0, count, bucketEndValue, bucketStartValue, bucketEndValue);
            }

            bucketStartValue = bucketEndValue;
            bucketEndValue += bucketWidth;
        }

        return series;
    }

    /**
     * Converts this value set to an {@link XYIntervalSeries} object, where each non-empty bucket is represented as a
     * vertical bar.
     *
     * @param seriesName
     *            the name of the series
     * @return the series
     */
    public XYIntervalSeries toVerticalSeries(final String seriesName)
    {
        final XYIntervalSeries series = new XYIntervalSeries(seriesName);

        int bucketStartValue = 0;
        int bucketEndValue = bucketWidth;

        for (int i = 0; i < countPerBucket.length; i++)
        {
            final int count = countPerBucket[i];
            if (count > 0)
            {
                series.add(bucketEndValue, bucketStartValue, bucketEndValue, count, 0, count);
            }

            bucketStartValue = bucketEndValue;
            bucketEndValue += bucketWidth;
        }

        return series;
    }

    /**
     * Scales the width of the buckets to make room for larger values.
     */
    private void scale()
    {
        bucketWidth = bucketWidth * 2;

        // merge two consecutive buckets into one
        for (int i = 0; i < bucketCount; i += 2)
        {
            final int newBucketIndex = i / 2;

            countPerBucket[newBucketIndex] = countPerBucket[i] + countPerBucket[i + 1];
        }

        // clear the second half of the buckets
        Arrays.fill(countPerBucket, bucketCount / 2, bucketCount, 0);
    }
}
