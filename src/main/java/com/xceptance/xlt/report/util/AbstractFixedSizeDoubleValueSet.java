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
package com.xceptance.xlt.report.util;

/**
 * A {@link AbstractFixedSizeDoubleValueSet} classifies all added values into N buckets. The total value range covered
 * is N times the current width of a bucket. Initially, the bucket width is very small. Once values outside the current
 * range are added, the value range is shifted or scaled up as necessary by scaling up the bucket width.
 */
public abstract class AbstractFixedSizeDoubleValueSet
{
    /**
     * The initial bucket width.
     */
    static double INITIAL_BUCKET_WIDTH = Math.pow(2, -13); // ~ 0.0001

    /**
     * The number of buckets, always a multiple of 2.
     */
    private final int numberOfBuckets;

    /**
     * The current width of a bucket.
     */
    private double bucketWidth = INITIAL_BUCKET_WIDTH;

    /**
     * The lower boundary of the current value range, inclusive.
     */
    private double min = Double.NaN;

    /**
     * The upper boundary of the last occupied bucket, exclusive.
     */
    private double max = Double.MIN_VALUE;

    /**
     * Creates a {@link AbstractFixedSizeDoubleValueSet} object with the given number of buckets, which must be a
     * multiple of 2.
     *
     * @param numberOfBuckets
     *            the number of buckets
     */
    public AbstractFixedSizeDoubleValueSet(final int numberOfBuckets)
    {
        if (numberOfBuckets <= 0 || numberOfBuckets % 2 != 0)
        {
            throw new IllegalArgumentException("Parameter 'numberOfBuckets' must be greater than 0 and be a multiple of 2");
        }

        this.numberOfBuckets = numberOfBuckets;
    }

    /**
     * Returns the configured number of buckets.
     *
     * @return the number of buckets
     */
    public int getNumberOfBuckets()
    {
        return numberOfBuckets;
    }

    /**
     * Returns the current bucket width.
     *
     * @return the bucket width
     */
    public double getBucketWidth()
    {
        return bucketWidth;
    }

    /**
     * Returns the lower boundary of the current value range.
     *
     * @return the lower boundary
     */
    public double getMinimum()
    {
        return min;
    }

    /**
     * Adds a value to this set.
     *
     * @param value
     *            the value
     */
    public void addValue(final double value)
    {
        // check if it is the first value added
        if (Double.isNaN(min))
        {
            // yes, initialize the range
            min = value;
        }
        else
        {
            // no, adjust the range if needed
            adjustRange(value);
        }

        // ensure the difference is never negative (should not happen, however, calculating with doubles is PITA)
        final double diff = Math.max(0.0, value - min);

        // determine the index of the bucket that corresponds to the value
        final int index = (int) Math.floor(diff / bucketWidth);

        // update the upper boundary of the last occupied bucket
        max = Math.max(max, min + bucketWidth * (index + 1));

        // now store/process the value
        storeValue(index, value);
    }

    /**
     * Extends or shifts the value range to include the new value.
     *
     * @param value
     *            the new value
     */
    private void adjustRange(final double value)
    {
        // first scale the value range if needed
        while (value >= min + bucketWidth * numberOfBuckets // value is larger than the current range
               || value < max - bucketWidth * numberOfBuckets) // cannot get value into range by shifting
        {
            // double the range
            bucketWidth = bucketWidth * 2;

            scale();
        }

        // now shift the value range if needed
        if (value < min)
        {
            // ensure the difference is never negative (should not happen, however, calculating with doubles is PITA)
            final double diff = Math.max(0.0, min - value);

            final int buckets = (int) Math.ceil(diff / bucketWidth);
            min = min - bucketWidth * buckets;

            shift(buckets);
        }
    }

    /**
     * Called after the value range has been doubled. Subclasses need to implement this method to update their internal
     * state as needed.
     */
    protected abstract void scale();

    /**
     * Called after the value range has been shifted. Subclasses need to implement this method to update their internal
     * state as needed.
     *
     * @param buckets
     *            the number of buckets
     */
    protected abstract void shift(final int buckets);

    /**
     * Called after the bucket index that corresponds to a value has been determined. Subclasses need to implement this
     * method to update their internal state as needed.
     *
     * @param index
     *            the bucket index
     * @param value
     *            the value
     */
    protected abstract void storeValue(final int index, final double value);

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(bucketWidth);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(max);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(min);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + numberOfBuckets;
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
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        AbstractFixedSizeDoubleValueSet other = (AbstractFixedSizeDoubleValueSet) obj;
        if (Double.doubleToLongBits(bucketWidth) != Double.doubleToLongBits(other.bucketWidth))
        {
            return false;
        }
        if (Double.doubleToLongBits(max) != Double.doubleToLongBits(other.max))
        {
            return false;
        }
        if (Double.doubleToLongBits(min) != Double.doubleToLongBits(other.min))
        {
            return false;
        }
        if (numberOfBuckets != other.numberOfBuckets)
        {
            return false;
        }
        return true;
    }
}
