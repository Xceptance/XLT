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

import java.util.BitSet;

import org.apache.commons.lang3.ArrayUtils;

/**
 * A {@link LowPrecisionDoubleValueSet} stores any number of distinct double values in a memory-efficient way, however,
 * at the cost of loosing precision. This means that values added to this set may not necessarily be returned precisely
 * as they were, but rough approximations of them only. This data structure is especially useful for charts, where we
 * often have to deal with many different values. Since the chart resolution is rather low, we can live with the
 * approximated values and save a lot of memory at the same time.
 * <p>
 * The set maintains a fixed number N of buckets to store the values. If a value does not fit into the current value
 * range, the range is shifted or scaled until the value fits in. Scaling means the value range is extended causing two
 * adjacent buckets to be merged into one. Since the underlying storage is always fixed, scaling has the negative side
 * effect of loosing precision.
 */
public class LowPrecisionDoubleValueSet extends AbstractFixedSizeDoubleValueSet
{
    /**
     * The default number of buckets.
     */
    static int DEFAULT_BUCKET_COUNT = 128;

    /**
     * Sets the default number of buckets for new {@link LowPrecisionDoubleValueSet} objects.
     *
     * @param numberOfBuckets
     *            the number of buckets
     */
    public static void setDefaultBucketCount(final int numberOfBuckets)
    {
        DEFAULT_BUCKET_COUNT = numberOfBuckets;
    }

    /**
     * The bit set representing the buckets.
     */
    private final BitSet bitSet;

    /**
     * Creates a {@link LowPrecisionDoubleValueSet} object with {@value LowPrecisionDoubleValueSet#DEFAULT_BUCKET_COUNT}
     * buckets.
     */
    public LowPrecisionDoubleValueSet()
    {
        this(DEFAULT_BUCKET_COUNT);
    }

    /**
     * Creates a {@link LowPrecisionDoubleValueSet} object with the given number of buckets, which must be a multiple of
     * 2.
     *
     * @param numberOfBuckets
     *            the number of buckets
     */
    public LowPrecisionDoubleValueSet(final int numberOfBuckets)
    {
        super(numberOfBuckets);

        bitSet = new BitSet(numberOfBuckets);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void scale()
    {
        final int numberOfBuckets = getNumberOfBuckets();

        // merge two consecutive bits into one
        for (int i = 0, j = 0; i < numberOfBuckets; i += 2, j++)
        {
            final boolean bitValue = bitSet.get(i) || bitSet.get(i + 1);
            bitSet.set(j, bitValue);
        }

        // clear the upper half of the bit set
        bitSet.clear(numberOfBuckets / 2, numberOfBuckets);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void shift(final int buckets)
    {
        final int numberOfBuckets = getNumberOfBuckets();

        // shift the bits to the right, clearing the lower bits
        for (int i = numberOfBuckets - 1; i >= 0; i--)
        {
            if (i >= buckets)
            {
                bitSet.set(i, bitSet.get(i - buckets));
            }
            else
            {
                bitSet.clear(i);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void storeValue(final int index, final double value)
    {
        bitSet.set(index);
    }

    /**
     * Returns an approximation of the values added to this set.
     *
     * @return the values
     */
    public double[] getValues()
    {
        final double bucketWidth = getBucketWidth();
        final double min = getMinimum();

        final double[] values;

        if (Double.isNaN(min))
        {
            values = ArrayUtils.EMPTY_DOUBLE_ARRAY;
        }
        else
        {
            final int size = bitSet.cardinality();
            values = new double[size];

            for (int i = 0, j = bitSet.nextSetBit(0); i < size; i++, j = bitSet.nextSetBit(j + 1))
            {
                values[i] = min + bucketWidth * j;
            }
        }

        return values;
    }

    /**
     * Merges the data of the passed set into this set.
     *
     * @param other
     *            the other set
     */
    public void merge(final LowPrecisionDoubleValueSet other)
    {
        for (final double value : other.getValues())
        {
            addValue(value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((bitSet == null) ? 0 : bitSet.hashCode());
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
        LowPrecisionDoubleValueSet other = (LowPrecisionDoubleValueSet) obj;
        if (bitSet == null)
        {
            if (other.bitSet != null)
            {
                return false;
            }
        }
        else if (!bitSet.equals(other.bitSet))
        {
            return false;
        }
        return true;
    }
}
