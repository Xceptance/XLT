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

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LowPrecisionDoubleValueSetTest
{
    private static final int buckets = 100;

    private LowPrecisionDoubleValueSet valueSet;

    @Before
    public void before()
    {
        valueSet = new LowPrecisionDoubleValueSet(buckets);
    }

    // --- setDefaultBucketCount ---

    @Test(expected = IllegalArgumentException.class)
    public void setDefaultBucketCount_negative()
    {
        LowPrecisionDoubleValueSet.setDefaultBucketCount(-1);

        new LowPrecisionDoubleValueSet();
    }

    @Test(expected = IllegalArgumentException.class)
    public void setDefaultBucketCount_zero()
    {
        LowPrecisionDoubleValueSet.setDefaultBucketCount(0);

        new LowPrecisionDoubleValueSet();
    }

    @Test(expected = IllegalArgumentException.class)
    public void setDefaultBucketCount_odd()
    {
        LowPrecisionDoubleValueSet.setDefaultBucketCount(1);

        new LowPrecisionDoubleValueSet();
    }

    @Test
    public void setDefaultBucketCount_ok()
    {
        try
        {
            LowPrecisionDoubleValueSet.setDefaultBucketCount(10);

            valueSet = new LowPrecisionDoubleValueSet();

            // add twice as many values as the default bucket count
            for (int i = 0; i < 2 * LowPrecisionDoubleValueSet.DEFAULT_BUCKET_COUNT; i++)
            {
                valueSet.addValue(i);
            }

            // check that we get exactly bucket count values back
            Assert.assertEquals(LowPrecisionDoubleValueSet.DEFAULT_BUCKET_COUNT, valueSet.getValues().length);
        }
        finally
        {
            // in any case, reset to defaults
            LowPrecisionDoubleValueSet.setDefaultBucketCount(LowPrecisionDoubleValueSet.DEFAULT_BUCKET_COUNT);
        }
    }

    // --- constructor ---

    @Test(expected = IllegalArgumentException.class)
    public void constructor_numberOfBuckets_negative()
    {
        new LowPrecisionDoubleValueSet(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_numberOfBuckets_zero()
    {
        new LowPrecisionDoubleValueSet(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_numberOfBuckets_odd()
    {
        new LowPrecisionDoubleValueSet(1);
    }

    @Test
    public void constructor_numberOfBuckets_none()
    {
        valueSet = new LowPrecisionDoubleValueSet();

        // add twice as many values as the default bucket count
        for (int i = 0; i < 2 * LowPrecisionDoubleValueSet.DEFAULT_BUCKET_COUNT; i++)
        {
            valueSet.addValue(i);
        }

        // check that we get exactly bucket count values back
        Assert.assertEquals(LowPrecisionDoubleValueSet.DEFAULT_BUCKET_COUNT, valueSet.getValues().length);
    }

    @Test
    public void constructor_numberOfBuckets_ok()
    {
        valueSet = new LowPrecisionDoubleValueSet(10);

        // add twice as many values as the default bucket count
        for (int i = 0; i < 2 * 10; i++)
        {
            valueSet.addValue(i);
        }

        // check that we get exactly bucket count values back
        Assert.assertEquals(10, valueSet.getValues().length);
    }

    // --- addValue / getValues ---

    @Test
    public void getValues_noValues()
    {
        // do not add any value

        // check that we get an empty array back
        final double[] values = valueSet.getValues();
        Assert.assertEquals(0, values.length);
    }

    @Test
    public void getValues_oneValue()
    {
        final double[] values =
            {
                4
            };

        addValues(values, valueSet);
        checkValues(values, valueSet);
    }

    @Test
    public void getValues_twoValues()
    {
        final double[] values =
            {
                10, -10
            };

        addValues(values, valueSet);
        checkValuesReverse(values, valueSet);
    }

    @Test
    public void getValues_specificValues()
    {
        final double[] values =
            {
                32, 64, 128, 256, 512, 1024
            };

        addValues(values, valueSet);
        checkValues(values, valueSet);
    }

    @Test
    public void getValues_otherSpecificValues()
    {
        final double[] values =
            {
                3, 11, 37, 53, 73, 97
            };

        addValues(values, valueSet);
        checkValues(values, valueSet);
    }

    @Test
    public void getValues_noScale_noShift()
    {
        final double[] values = new double[buckets];
        for (int i = 0; i < buckets; i++)
        {
            values[i] = i * AbstractFixedSizeDoubleValueSet.INITIAL_BUCKET_WIDTH;
        }

        addValues(values, valueSet);
        checkValues(values, valueSet);
    }

    @Test
    public void getValues_noScale_shift()
    {
        // adding smaller values causes constant shifting
        final double[] values = new double[buckets];
        for (int i = 0; i < buckets; i++)
        {
            values[i] = -i * AbstractFixedSizeDoubleValueSet.INITIAL_BUCKET_WIDTH;
        }

        addValues(values, valueSet);
        checkValuesReverse(values, valueSet);
    }

    @Test
    public void getValues_scale_noShift()
    {
        // adding twice as many values causes scaling
        final double[] values = new double[buckets * 2];
        for (int i = 0; i < values.length; i++)
        {
            values[i] = i * AbstractFixedSizeDoubleValueSet.INITIAL_BUCKET_WIDTH;
        }

        addValues(values, valueSet);

        final double[] expectedValues = new double[buckets];
        for (int i = 0; i < expectedValues.length; i++)
        {
            expectedValues[i] = i * 2 * AbstractFixedSizeDoubleValueSet.INITIAL_BUCKET_WIDTH;
        }

        checkValues(expectedValues, valueSet);
    }

    @Test
    public void getValues_scale_shift()
    {
        // adding twice as many values that are getting smaller causes shifting and scaling
        final double[] values = new double[buckets * 2];
        for (int i = 0; i < values.length; i++)
        {
            values[i] = -i * AbstractFixedSizeDoubleValueSet.INITIAL_BUCKET_WIDTH;
        }

        addValues(values, valueSet);

        final double[] expectedValues = new double[buckets];
        for (int i = 0; i < expectedValues.length; i++)
        {
            expectedValues[i] = (-i * 2 - 1) * AbstractFixedSizeDoubleValueSet.INITIAL_BUCKET_WIDTH;
        }

        checkValuesReverse(expectedValues, valueSet);
    }

    // --- merge ---

    @Test
    public void merge_sameRange()
    {
        final LowPrecisionDoubleValueSet anotherValueSet = new LowPrecisionDoubleValueSet(buckets);

        // [-10..10]
        anotherValueSet.addValue(-10);
        anotherValueSet.addValue(10);
        anotherValueSet.addValue(5);
        anotherValueSet.addValue(-5);

        // [-10..10]
        valueSet.addValue(-10);
        valueSet.addValue(0);
        valueSet.addValue(10);

        valueSet.merge(anotherValueSet);

        // check that valueSet contains also the values of the other set
        final double[] values = valueSet.getValues();
        Assert.assertEquals(5, values.length);
        Assert.assertEquals(-10, values[0], 0);
        Assert.assertEquals(-5, values[1], 0);
        Assert.assertEquals(0, values[2], 0);
        Assert.assertEquals(5, values[3], 0);
        Assert.assertEquals(10, values[4], 0);
    }

    @Test
    public void merge_overlappingRanges()
    {
        final LowPrecisionDoubleValueSet anotherValueSet = new LowPrecisionDoubleValueSet(buckets);

        // [-5..5]
        anotherValueSet.addValue(5);
        anotherValueSet.addValue(-5);

        // [0..15]
        valueSet.addValue(0);
        valueSet.addValue(15);

        valueSet.merge(anotherValueSet);

        // check that valueSet contains also the values of the other set
        final double[] values = valueSet.getValues();
        Assert.assertEquals(4, values.length);
        Assert.assertEquals(-5, values[0], 0);
        Assert.assertEquals(0, values[1], 0);
        Assert.assertEquals(5, values[2], 0);
        Assert.assertEquals(15, values[3], 0);
    }

    @Test
    public void merge_disjunctRanges()
    {
        final LowPrecisionDoubleValueSet anotherValueSet = new LowPrecisionDoubleValueSet(buckets);

        // [-15..-5]
        anotherValueSet.addValue(-5);
        anotherValueSet.addValue(-15);

        // [15..25]
        valueSet.addValue(15);
        valueSet.addValue(25);

        valueSet.merge(anotherValueSet);

        // check that valueSet contains also the values of the other set
        final double[] values = valueSet.getValues();
        Assert.assertEquals(4, values.length);
        Assert.assertEquals(-15, values[0], 0);
        Assert.assertEquals(-5, values[1], 0);
        Assert.assertEquals(15, values[2], 0);
        Assert.assertEquals(25, values[3], 0);
    }

    // --- helper methods ---

    private void addValues(final double[] values, final LowPrecisionDoubleValueSet valueSet)
    {
        for (final double v : values)
        {
            valueSet.addValue(v);
        }
    }

    private void checkValues(final double[] expectedValues, final LowPrecisionDoubleValueSet valueSet)
    {
        Assert.assertArrayEquals(expectedValues, valueSet.getValues(), 0);
    }

    private void checkValuesReverse(final double[] expectedValues, final LowPrecisionDoubleValueSet valueSet)
    {
        ArrayUtils.reverse(expectedValues);
        checkValues(expectedValues, valueSet);
    }
}
