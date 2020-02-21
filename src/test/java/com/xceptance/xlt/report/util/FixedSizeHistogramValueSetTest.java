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
}
