/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
import com.xceptance.xlt.report.util.lucene.OpenBitSet;

/**
 * A {@link IntLowPrecisionValueSet} stores any number of distinct integer values out of [0..{@link Integer#MAX_VALUE}]
 * in a memory-efficient way, however, at the cost of loosing precision. This means that values added to this set may
 * not necessarily be returned precisely as they were, but rough approximations of them only. This data structure is
 * especially useful for charts, where we often have to deal with many different values. Since the chart resolution is
 * rather low, we can live with the approximated values and save a lot of memory at the same time.
 * <p>
 * The set maintains a fixed number N of buckets to store the values, and the initial value range is [0..N-1]. If a
 * value does not fit into the current value range, the range is scaled until the value fits in. Scaling means the value
 * range is doubled causing two adjacent buckets to be merged into one. Since the underlying storage is always fixed,
 * scaling has the negative side effect of loosing precision.
 */
public class IntLowPrecisionValueSet
{
    /**
     * The default number of buckets.
     */
    private static int DEFAULT_BUCKET_COUNT = 256;

    /**
     * Sets the default number of buckets for new {@link IntLowPrecisionValueSet} objects.
     * 
     * @param buckets
     *            the number of buckets
     */
    public static void setDefaultBucketCount(final int buckets)
    {
        ParameterCheckUtils.isNotNegative(buckets, "buckets");

        DEFAULT_BUCKET_COUNT = buckets;
    }

    /**
     * The bit set representing the buckets.
     */
    private final OpenBitSet bitSet;

    /**
     * The number of buckets.
     */
    private final int buckets;

    /**
     * The value scaling factor. 2 pow scale.
     */
    private int scale;

    /**
     * Creates a {@link IntLowPrecisionValueSet} object with {@value IntLowPrecisionValueSet#DEFAULT_BUCKET_COUNT}
     * buckets.
     */
    public IntLowPrecisionValueSet()
    {
        this(DEFAULT_BUCKET_COUNT);
    }

    /**
     * Creates a {@link IntLowPrecisionValueSet} object with the given number of buckets.
     * 
     * @param buckets
     *            the number of buckets
     */
    public IntLowPrecisionValueSet(final int buckets)
    {
        this.buckets = buckets;

        scale = 0;
        bitSet = new OpenBitSet(this.buckets);
    }

    /**
     * Adds a positive value to this set.
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

        // adjust the value according to the current scale
        value = value >> scale; // div

        // make the value fit into the bit set by scaling the bit set as necessary
        while (value >= buckets)
        {
            scale();
            value = value >> 1; // 2
        }

        // finally set the corresponding bit
        bitSet.set(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj)
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
        final IntLowPrecisionValueSet other = (IntLowPrecisionValueSet) obj;
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
        if (scale != other.scale)
        {
            return false;
        }
        if (buckets != other.buckets)
        {
            return false;
        }
        return true;
    }

    /**
     * Returns an approximation of the values added to this set.
     *
     * @return the values
     */
    public double[] getValues()
    {
        final double[] values = new double[(int) bitSet.cardinality()];

        //        for (int i = 0, j = bitSet.nextSetBit(0); i < values.length; i++, j = bitSet.nextSetBit(j + 1))
        //        {
        //            values[i] = j << scale; // * scale
        //        }

        int x = 0;
        for (int i = 0; i < values.length; i++) 
        {
            final int v = bitSet.nextSetBit(x);
            values[i] = v << scale;
            x = v + 1;
        }

        return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bitSet == null) ? 0 : bitSet.hashCode());
        result = prime * result + scale;
        result = prime * result + buckets;
        return result;
    }

    /**
     * Merges the data of the passed set into this set.
     *
     * @param other
     *            the other set
     */
    public void merge(final IntLowPrecisionValueSet other)
    {
        // TODO: check for same bucket count

        // first make both bit sets the same scale
        if (scale != other.scale)
        {
            final IntLowPrecisionValueSet toBeScaled;
            final int targetScale;

            if (scale < other.scale)
            {
                toBeScaled = this;
                targetScale = other.scale;
            }
            else
            {
                toBeScaled = other;
                targetScale = scale;
            }

            while (toBeScaled.scale < targetScale)
            {
                toBeScaled.scale();
            }
        }

        // now merge the bit sets
        bitSet.or(other.bitSet);
    }

    /**
     * Scales this set.
     */
    private void scale()
    {
        scale++;

        // merge two consecutive bits into one
        for (int i = 0; i < buckets; i += 2)
        {
            final int bitIndex = i >> 1; // / 2
            final boolean bitValue = bitSet.get(i) || bitSet.get(i + 1);

            if (bitValue)
            {
                bitSet.set(bitIndex);
            }
            else
            {
                bitSet.clear(bitIndex);
            }
        }

        // clear the second half of the bit set
        bitSet.clear(buckets >> 1, buckets); // / 2
    }
}
