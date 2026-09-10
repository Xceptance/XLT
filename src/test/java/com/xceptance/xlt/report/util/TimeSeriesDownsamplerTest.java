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

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link TimeSeriesDownsampler}.
 */
public class TimeSeriesDownsamplerTest
{
    private static final int MINUTE = 60;

    private static final int HOUR = 3600;

    @Test
    public void testTwoHourTestResolvesToOneMinute()
    {
        // this is the anchor the whole ladder is tuned around
        Assert.assertEquals(MINUTE, TimeSeriesDownsampler.selectInterval(2 * HOUR, 1));
    }

    @Test
    public void testIntervalGrowsWithDuration()
    {
        Assert.assertEquals(5, TimeSeriesDownsampler.selectInterval(5 * MINUTE, 1));
        Assert.assertEquals(10, TimeSeriesDownsampler.selectInterval(15 * MINUTE, 1));
        Assert.assertEquals(15, TimeSeriesDownsampler.selectInterval(30 * MINUTE, 1));
        Assert.assertEquals(30, TimeSeriesDownsampler.selectInterval(HOUR, 1));
        Assert.assertEquals(MINUTE, TimeSeriesDownsampler.selectInterval(75 * MINUTE, 1));
        Assert.assertEquals(2 * MINUTE, TimeSeriesDownsampler.selectInterval(4 * HOUR, 1));
        Assert.assertEquals(5 * MINUTE, TimeSeriesDownsampler.selectInterval(8 * HOUR, 1));
        Assert.assertEquals(15 * MINUTE, TimeSeriesDownsampler.selectInterval(24 * HOUR, 1));
    }

    @Test
    public void testRowCountStaysBounded()
    {
        // the point of deriving the interval is that the file size does not depend on how long the test ran
        final long[] durations =
            {
                5 * MINUTE, 15 * MINUTE, 30 * MINUTE, HOUR, 75 * MINUTE, 2 * HOUR, 4 * HOUR, 8 * HOUR, 24 * HOUR
            };

        for (final long duration : durations)
        {
            final int interval = TimeSeriesDownsampler.selectInterval(duration, 1);
            final long rows = (duration + interval - 1) / interval;

            Assert.assertTrue("Too many rows for a " + duration + "s test: " + rows, rows <= 120);
            Assert.assertTrue("Too few rows for a " + duration + "s test: " + rows, rows >= 50);
        }
    }

    @Test
    public void testIntervalNeverFinerThanTheSource()
    {
        // a 24 hour run collects at roughly 96s resolution, so asking for finer would invent precision
        Assert.assertEquals(120, TimeSeriesDownsampler.selectInterval(HOUR, 120));
        Assert.assertEquals(5 * MINUTE, TimeSeriesDownsampler.selectInterval(5 * MINUTE, 5 * MINUTE));
    }

    @Test
    public void testRatePerBucketDividesByTheInterval()
    {
        final ValueSet counts = new ValueSet();
        for (int second = 0; second < 60; second++)
        {
            counts.addOrUpdateValue(second * 1000L, 10);
        }

        final double[] rates = TimeSeriesDownsampler.ratePerBucket(counts, 0, 2, 30);

        Assert.assertEquals("10 per second for 30 seconds is 10 per second", 10.0, rates[0], 0.001);
        Assert.assertEquals(10.0, rates[1], 0.001);
    }

    @Test
    public void testEmptyBucketsAreZeroNotGaps()
    {
        final ValueSet counts = new ValueSet();
        counts.addOrUpdateValue(0, 5);

        final double[] rates = TimeSeriesDownsampler.ratePerBucket(counts, 0, 3, 10);

        Assert.assertEquals(0.5, rates[0], 0.001);
        Assert.assertEquals(0.0, rates[1], 0.001);
        Assert.assertEquals(0.0, rates[2], 0.001);
    }

    @Test
    public void testMeanPerBucketWeightsBySampleCount()
    {
        final IntMinMaxValueSet values = new IntMinMaxValueSet(1000);

        // one slow sample in the first second, nine fast ones in the second - the mean must lean fast
        values.addOrUpdateValue(0, 1000);
        for (int i = 0; i < 9; i++)
        {
            values.addOrUpdateValue(1000, 100);
        }

        final int[] means = TimeSeriesDownsampler.meanPerBucket(values, 0, 1, 10);

        Assert.assertEquals("A plain average of the two seconds would give 550", 190, means[0]);
    }

    @Test
    public void testMeanPerBucketAtARealisticEpoch()
    {
        // at epoch 0 a seconds/milliseconds mix-up is invisible, because 0 * 1000 is still 0.
        // IntMinMaxValueSet.getFirstSecond() returns milliseconds while ValueSet.getFirstSecond()
        // returns seconds, so this has to be pinned at a real timestamp.
        final long startSecond = 1763571456L;

        final IntMinMaxValueSet values = new IntMinMaxValueSet(1000);
        values.addOrUpdateValue(startSecond * 1000, 500);
        values.addOrUpdateValue((startSecond + 61) * 1000, 900);

        final int[] means = TimeSeriesDownsampler.meanPerBucket(values, startSecond, 2, 60);

        Assert.assertEquals("First bucket lost its samples", 500, means[0]);
        Assert.assertEquals("Second bucket lost its samples", 900, means[1]);
    }

    @Test
    public void testRatePerBucketAtARealisticEpoch()
    {
        final long startSecond = 1763571456L;

        final ValueSet counts = new ValueSet();
        for (int i = 0; i < 60; i++)
        {
            counts.addOrUpdateValue((startSecond + i) * 1000, 3);
        }

        final double[] rates = TimeSeriesDownsampler.ratePerBucket(counts, startSecond, 1, 60);

        Assert.assertEquals(3.0, rates[0], 0.001);
    }

    @Test
    public void testEmptyValueSetsAreTolerated()
    {
        // both value sets throw from getFirstSecond() when nothing was ever added
        Assert.assertArrayEquals(new int[]
            {
                0, 0
            }, TimeSeriesDownsampler.meanPerBucket(new IntMinMaxValueSet(100), 0, 2, 60));
        Assert.assertArrayEquals(new double[]
            {
                0.0, 0.0
            }, TimeSeriesDownsampler.ratePerBucket(new ValueSet(), 0, 2, 60), 0.001);
    }

    @Test
    public void testNullValueSetsAreTolerated()
    {
        Assert.assertArrayEquals(new int[]
            {
                0, 0
            }, TimeSeriesDownsampler.meanPerBucket(null, 0, 2, 60));
        Assert.assertArrayEquals(new double[]
            {
                0.0, 0.0
            }, TimeSeriesDownsampler.ratePerBucket(null, 0, 2, 60), 0.001);
    }
}
