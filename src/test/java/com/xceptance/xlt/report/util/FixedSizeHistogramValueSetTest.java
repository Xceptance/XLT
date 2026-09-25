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

import org.jfree.data.xy.XYIntervalDataItem;
import org.jfree.data.xy.XYIntervalSeries;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FixedSizeHistogramValueSetTest
{
    private final int bucketCount = 100;

    private FixedSizeHistogramValueSet valueSet;

    @Before
    public void before()
    {
        valueSet = new FixedSizeHistogramValueSet(bucketCount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_bucketCountNegative()
    {
        new FixedSizeHistogramValueSet(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_bucketCountzero()
    {
        new FixedSizeHistogramValueSet(0);
    }

    @Test
    public void constructor_bucketCountOdd()
    {
        // make sure the effective bucket count is always even
        Assert.assertEquals(2, new FixedSizeHistogramValueSet(1).getBucketCount());
        Assert.assertEquals(8, new FixedSizeHistogramValueSet(7).getBucketCount());
    }

    @Test
    public void getValues_noValueAdded()
    {
        // test
        final int[] counts = valueSet.getCountPerBucket();

        Assert.assertEquals(bucketCount, counts.length);

        for (final int count : counts)
        {
            Assert.assertEquals(0, count);
        }
    }

    @Test
    public void getValues_noPositiveValueAdded()
    {
        // setup
        valueSet.addValue(-1);

        // test
        final int[] counts = valueSet.getCountPerBucket();

        Assert.assertEquals(bucketCount, counts.length);

        for (final int count : counts)
        {
            Assert.assertEquals(0, count);
        }
    }

    @Test
    public void getValues_someValuesAdded_noScaling()
    {
        // setup
        for (int i = 0; i < bucketCount; i++)
        {
            valueSet.addValue(i);
        }

        // test
        final int[] counts = valueSet.getCountPerBucket();

        Assert.assertEquals(bucketCount, counts.length);

        for (final int count : counts)
        {
            Assert.assertEquals(1, count);
        }
    }

    @Test
    public void getValues_moreValuesAdded_scaling()
    {
        // setup
        for (int i = 0; i < 2 * bucketCount; i++)
        {
            valueSet.addValue(i);
        }

        // test
        final int[] counts = valueSet.getCountPerBucket();

        Assert.assertEquals(bucketCount, counts.length);

        for (final int count : counts)
        {
            Assert.assertEquals(2, count);
        }
    }

    @Test
    public void getMaximumCount()
    {
        // setup
        valueSet.addValue(0);
        valueSet.addValue(1);
        valueSet.addValue(1);
        valueSet.addValue(2);
        valueSet.addValue(2);
        valueSet.addValue(2);

        // test
        Assert.assertEquals(3, valueSet.getMaximumCount());
    }

    @Test
    public void toSeries()
    {
        // setup
        final int[] values =
            {
                3, 5, 7, 11, 13, 17
            };

        final String seriesName = "foo";

        for (final int value : values)
        {
            valueSet.addValue(value);
        }

        // test
        final XYIntervalSeries series = valueSet.toSeries(seriesName);

        Assert.assertEquals(seriesName, series.getKey());

        for (int i = 0; i < series.getItemCount(); i++)
        {
            final XYIntervalDataItem item = (XYIntervalDataItem) series.getDataItem(i);

            Assert.assertEquals(1, item.getX().intValue());
            Assert.assertEquals(0, (int) item.getXLowValue());
            Assert.assertEquals(1, (int) item.getXHighValue());
            Assert.assertEquals(values[i] + 1, (int) item.getYValue());
            Assert.assertEquals(values[i], (int) item.getYLowValue());
            Assert.assertEquals(values[i] + 1, (int) item.getYHighValue());
        }
    }

    @Test
    public void testMergeNullOrEmpty()
    {
        final FixedSizeHistogramValueSet set1 = new FixedSizeHistogramValueSet(10);
        set1.addValue(5);

        // merge null
        set1.merge(null);
        Assert.assertEquals(1, set1.getMaximumCount());

        // merge empty
        set1.merge(new FixedSizeHistogramValueSet(10));
        Assert.assertEquals(1, set1.getMaximumCount());

        // merge into empty
        final FixedSizeHistogramValueSet empty = new FixedSizeHistogramValueSet(10);
        empty.merge(set1);
        Assert.assertEquals(1, empty.getMaximumCount());
        Assert.assertEquals(set1.getBucketWidth(), empty.getBucketWidth());
        Assert.assertArrayEquals(set1.getCountPerBucket(), empty.getCountPerBucket());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeDifferentBucketCountThrows()
    {
        final FixedSizeHistogramValueSet set1 = new FixedSizeHistogramValueSet(10);
        final FixedSizeHistogramValueSet set2 = new FixedSizeHistogramValueSet(20);
        set1.addValue(1);
        set2.addValue(1);
        set1.merge(set2);
    }

    @Test
    public void testMergeSameBucketWidth()
    {
        final FixedSizeHistogramValueSet seq = new FixedSizeHistogramValueSet(20);
        final FixedSizeHistogramValueSet set1 = new FixedSizeHistogramValueSet(20);
        final FixedSizeHistogramValueSet set2 = new FixedSizeHistogramValueSet(20);

        for (int i = 0; i < 15; i++)
        {
            seq.addValue(i);
            set1.addValue(i);
        }
        for (int i = 5; i < 20; i++)
        {
            seq.addValue(i);
            set2.addValue(i);
        }

        set1.merge(set2);
        Assert.assertEquals(seq.getBucketWidth(), set1.getBucketWidth());
        Assert.assertArrayEquals(seq.getCountPerBucket(), set1.getCountPerBucket());
    }

    @Test
    public void testMergeDifferentBucketWidth()
    {
        // setLarge forces bucketWidth 4
        final FixedSizeHistogramValueSet setLarge = new FixedSizeHistogramValueSet(10);
        setLarge.addValue(35); // 35 / 10 -> scales bucketWidth to 4
        Assert.assertEquals(4, setLarge.getBucketWidth());

        // setSmall has bucketWidth 1
        final FixedSizeHistogramValueSet setSmall = new FixedSizeHistogramValueSet(10);
        setSmall.addValue(0);
        setSmall.addValue(1);
        setSmall.addValue(2);
        setSmall.addValue(3);
        Assert.assertEquals(1, setSmall.getBucketWidth());

        // merge small into large
        setLarge.merge(setSmall);
        Assert.assertEquals(4, setLarge.getBucketWidth());
        // bucket 0 in setLarge (width 4, spanning 0..3) should now have 4 items
        Assert.assertEquals(4, setLarge.getCountPerBucket()[0]);

        // merge large into small (small should scale up to width 4)
        final FixedSizeHistogramValueSet setSmall2 = new FixedSizeHistogramValueSet(10);
        setSmall2.addValue(0);
        setSmall2.addValue(1);
        setSmall2.addValue(2);
        setSmall2.addValue(3);

        final FixedSizeHistogramValueSet setLarge2 = new FixedSizeHistogramValueSet(10);
        setLarge2.addValue(35);

        setSmall2.merge(setLarge2);
        Assert.assertEquals(4, setSmall2.getBucketWidth());
        Assert.assertEquals(4, setSmall2.getCountPerBucket()[0]);
        Assert.assertArrayEquals(setLarge.getCountPerBucket(), setSmall2.getCountPerBucket());
    }
}
