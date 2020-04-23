/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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

/**
 * A histogram value set with a fixed number N of buckets. Initially, the range of the value set is very small. Once
 * values outside the current range are added, the value range is shifted or scaled up as necessary by scaling up the
 * bucket width.
 */
public class FixedSizeHistogramDoubleValueSet extends AbstractFixedSizeDoubleValueSet
{
    /**
     * The number of values added to each bucket.
     */
    private final int countPerBucket[];

    /**
     * The total number of values added to this value set.
     */
    private int totalCount;

    /**
     * Creates a {@link FixedSizeHistogramDoubleValueSet} object with the given number of buckets, which must be a
     * multiple of 2.
     *
     * @param numberOfBuckets
     *            the number of buckets
     */
    public FixedSizeHistogramDoubleValueSet(final int numberOfBuckets)
    {
        super(numberOfBuckets);

        countPerBucket = new int[numberOfBuckets];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void scale()
    {
        final int numberOfBuckets = countPerBucket.length;

        // merge two consecutive buckets into one
        for (int i = 0, j = i; i < numberOfBuckets; i += 2, j++)
        {
            countPerBucket[j] = countPerBucket[i] + countPerBucket[i + 1];
        }

        // clear the upper half of the buckets
        Arrays.fill(countPerBucket, numberOfBuckets / 2, numberOfBuckets, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void shift(final int buckets)
    {
        final int numberOfBuckets = countPerBucket.length;

        // shift the buckets to the right, clearing the lower buckets
        for (int i = numberOfBuckets - 1; i >= 0; i--)
        {
            if (i >= buckets)
            {
                countPerBucket[i] = countPerBucket[i - buckets];
            }
            else
            {
                countPerBucket[i] = 0;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void storeValue(final int index, final double value)
    {
        countPerBucket[index]++;
        totalCount++;
    }

    /**
     * Returns the number of values added to each bucket.
     *
     * @return the count per bucket
     */
    int[] getCountPerBucket()
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
     * Returns the total count of values added to this value set.
     *
     * @return the total count
     */
    public int getTotalCount()
    {
        return totalCount;
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

        final double bucketWidth = getBucketWidth();
        double bucketStartValue = getMinimum();
        double bucketEndValue = bucketStartValue + bucketWidth;

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
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(countPerBucket);
        result = prime * result + totalCount;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        FixedSizeHistogramDoubleValueSet other = (FixedSizeHistogramDoubleValueSet) obj;
        if (!Arrays.equals(countPerBucket, other.countPerBucket))
        {
            return false;
        }
        if (totalCount != other.totalCount)
        {
            return false;
        }
        return true;
    }
}
