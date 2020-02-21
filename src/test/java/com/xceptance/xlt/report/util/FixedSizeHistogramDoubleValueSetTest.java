package com.xceptance.xlt.report.util;

import org.jfree.data.xy.XYIntervalDataItem;
import org.jfree.data.xy.XYIntervalSeries;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FixedSizeHistogramDoubleValueSetTest
{
    private final int buckets = 100;

    private FixedSizeHistogramDoubleValueSet valueSet;

    @Before
    public void before()
    {
        valueSet = new FixedSizeHistogramDoubleValueSet(buckets);
    }

    // --- constructor ---

    @Test(expected = IllegalArgumentException.class)
    public void constructor_numberOfBuckets_negative()
    {
        new FixedSizeHistogramDoubleValueSet(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_numberOfBuckets_zero()
    {
        new FixedSizeHistogramDoubleValueSet(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_numberOfBuckets_odd()
    {
        new FixedSizeHistogramDoubleValueSet(1);
    }

    @Test
    public void constructor_numberOfBuckets_ok()
    {
        valueSet = new FixedSizeHistogramDoubleValueSet(10);

        // add twice as many values as the default bucket count
        for (int i = 0; i < 2 * 10; i++)
        {
            valueSet.addValue(i);
        }

        // check that we get exactly bucket count values back
        Assert.assertEquals(10, valueSet.getCountPerBucket().length);
    }

    // --- addValue / getCountPerBucket ---

    @Test
    public void getCountPerBucket_noValues()
    {
        // do not add any value

        checkCounts(0, valueSet);
    }

    @Test
    public void getCountPerBucket_oneValue()
    {
        valueSet.addValue(4);

        final int[] counts = valueSet.getCountPerBucket();

        for (int i = 0; i < counts.length; i++)
        {
            final int expectedCount = (i == 0) ? 1 : 0;
            Assert.assertEquals("Index " + i, expectedCount, counts[i]);
        }
    }

    @Test
    public void getCountPerBucket_noScale_noShift()
    {
        for (int i = 0; i < buckets; i++)
        {
            valueSet.addValue(i * AbstractFixedSizeDoubleValueSet.INITIAL_BUCKET_WIDTH);
        }

        checkCounts(1, valueSet);
    }

    @Test
    public void getCountPerBucket_noScale_shift()
    {
        // adding smaller values causes constant shifting
        for (int i = 0; i < buckets; i++)
        {
            valueSet.addValue(-i * AbstractFixedSizeDoubleValueSet.INITIAL_BUCKET_WIDTH);
        }

        checkCounts(1, valueSet);
    }

    @Test
    public void getCountPerBucket_scale_noShift()
    {
        // adding twice as many values causes scaling
        for (int i = 0; i < buckets * 2; i++)
        {
            valueSet.addValue(i * AbstractFixedSizeDoubleValueSet.INITIAL_BUCKET_WIDTH);
        }

        checkCounts(2, valueSet);
    }

    @Test
    public void getCountPerBucket_scale_shift()
    {
        // adding twice as many values that are getting smaller causes shifting and scaling
        for (int i = 0; i < buckets * 2; i++)
        {
            valueSet.addValue(-i * AbstractFixedSizeDoubleValueSet.INITIAL_BUCKET_WIDTH);
        }

        checkCounts(2, valueSet);
    }

    // --- getMaximumCount ---

    @Test
    public void getMaximumCountInt()
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

    // --- toSeries ---

    @Test
    public void toSeries()
    {
        // setup
        final double[] values =
            {
                3, 5, 7, 11, 13, 17
            };

        final String seriesName = "foo";

        for (final double value : values)
        {
            valueSet.addValue(value);
        }

        // test
        final XYIntervalSeries series = valueSet.toSeries(seriesName);

        Assert.assertEquals(seriesName, series.getKey());

        for (int i = 0; i < series.getItemCount(); i++)
        {
            final XYIntervalDataItem item = (XYIntervalDataItem) series.getDataItem(i);

            Assert.assertEquals(1, item.getX(), 0);
            Assert.assertEquals(0, item.getXLowValue(), 0);
            Assert.assertEquals(1, item.getXHighValue(), 0);
            Assert.assertEquals(values[i], item.getYValue(), 0.25);
            Assert.assertEquals(values[i], item.getYLowValue(), 0.25);
            Assert.assertEquals(values[i], item.getYHighValue(), 0.25);
        }
    }

    // --- helper methods ---

    private void checkCounts(final int expectedCountPerBucket, final FixedSizeHistogramDoubleValueSet valueSet)
    {
        final int[] counts = valueSet.getCountPerBucket();

        for (int i = 0; i < counts.length; i++)
        {
            Assert.assertEquals("Index " + i, expectedCountPerBucket, counts[i]);
        }
    }
}
