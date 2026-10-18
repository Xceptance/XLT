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
        Assert.assertArrayEquals(new int[]
            {
                0, 0
            }, TimeSeriesDownsampler.meanPerBucket(new IntTimeSeries(), 0, 2, 60));
        Assert.assertArrayEquals(new double[]
            {
                0.0, 0.0
            }, TimeSeriesDownsampler.ratePerBucket(new ValueSet(), 0, 2, 60), 0.001);
        Assert.assertArrayEquals(new double[]
            {
                0.0, 0.0
            }, TimeSeriesDownsampler.countRatePerBucket(new IntTimeSeries(), 0, 2, 60), 0.001);
        Assert.assertArrayEquals(new double[]
            {
                0.0, 0.0
            }, TimeSeriesDownsampler.errorRatePerBucket(new IntTimeSeries(), 0, 2, 60), 0.001);
    }

    @Test
    public void testNullValueSetsAreTolerated()
    {
        Assert.assertArrayEquals(new int[]
            {
                0, 0
            }, TimeSeriesDownsampler.meanPerBucket((IntMinMaxValueSet) null, 0, 2, 60));
        Assert.assertArrayEquals(new int[]
            {
                0, 0
            }, TimeSeriesDownsampler.meanPerBucket((IntTimeSeries) null, 0, 2, 60));
        Assert.assertArrayEquals(new double[]
            {
                0.0, 0.0
            }, TimeSeriesDownsampler.ratePerBucket(null, 0, 2, 60), 0.001);
        Assert.assertArrayEquals(new double[]
            {
                0.0, 0.0
            }, TimeSeriesDownsampler.countRatePerBucket((IntTimeSeries) null, 0, 2, 60), 0.001);
        Assert.assertArrayEquals(new double[]
            {
                0.0, 0.0
            }, TimeSeriesDownsampler.errorRatePerBucket((IntTimeSeries) null, 0, 2, 60), 0.001);
    }

    @Test
    public void testSelectIntervalCeilingAndZeroDuration()
    {
        // Zero or negative duration falls back to minimum 1s interval
        Assert.assertEquals(1, TimeSeriesDownsampler.selectInterval(0, 0));
        Assert.assertEquals(1, TimeSeriesDownsampler.selectInterval(120, 1));
        Assert.assertEquals(5, TimeSeriesDownsampler.selectInterval(121, 1));

        // Duration exceeding 120 * 3600s exhausts the ladder and clamps to 3600s (unless sourceResolution is larger)
        Assert.assertEquals(3600, TimeSeriesDownsampler.selectInterval(200L * HOUR, 1));
        Assert.assertEquals(7200, TimeSeriesDownsampler.selectInterval(200L * HOUR, 7200));
    }

    @Test
    public void testScaledSeriesAndOutOfBoundsBucketsIgnored()
    {
        final long firstSec = 100L;
        final int buckets = 2;
        final int interval = 10; // Bucket 0: [100..109], Bucket 1: [110..119]

        // 1. IntMinMaxValueSet at scale > 1 (capacity 4, span > 4s forces scale = 2) with samples before, inside, and after range
        final IntMinMaxValueSet mm = new IntMinMaxValueSet(4);
        mm.addOrUpdateValue(99_000L, 999);  // second 99 (< firstSec, ignored)
        mm.addOrUpdateValue(102_000L, 100); // second 102 (bucket 0)
        mm.addOrUpdateValue(112_000L, 300); // second 112 (bucket 1, forces scale > 1 on size 4)
        mm.addOrUpdateValue(120_000L, 888); // second 120 (bucket == 2 >= buckets, ignored)
        Assert.assertTrue(mm.getScale() > 1);
        Assert.assertArrayEquals(new int[] { 100, 300 }, TimeSeriesDownsampler.meanPerBucket(mm, firstSec, buckets, interval));

        // 2. ValueSet with samples before (second 99), inside (100, 119), and after (120)
        final ValueSet vs = new ValueSet();
        vs.addOrUpdateValue(99_000L, 50);
        vs.addOrUpdateValue(100_000L, 20);
        vs.addOrUpdateValue(119_000L, 40);
        vs.addOrUpdateValue(120_000L, 70);
        Assert.assertArrayEquals(new double[] { 2.0, 4.0 }, TimeSeriesDownsampler.ratePerBucket(vs, firstSec, buckets, interval), 0.0001);

        // 3. IntTimeSeries at scale > 1 (condensed so slotWidth = 2) with weighted samples, errors, empty slots, and out-of-range slots
        final IntTimeSeries ts = new IntTimeSeries(16, 16);
        ts.addValue(98_000L, 999, true);   // second 98 (< firstSec, ignored)
        ts.addValue(100_000L, 100, false); // second 100 (bucket 0: 1 sample of 100, 0 errors)
        ts.addValue(104_000L, 400, true);  // second 104 (bucket 0: 3 samples of 400, 2 errors -> weighted mean (100 + 3*400)/4 = 325)
        ts.addValue(104_000L, 400, true);
        ts.addValue(104_000L, 400, false);
        ts.addValue(112_000L, 250, true);  // second 112 (bucket 1: 1 sample of 250, 1 error)
        ts.addValue(120_000L, 777, true);  // second 120 (bucket == 2 >= buckets, ignored; span 23s > 16s auto-condenses to slotWidth=2)

        Assert.assertEquals(2, ts.getSlotWidth());
        Assert.assertArrayEquals(new int[] { 325, 250 }, TimeSeriesDownsampler.meanPerBucket(ts, firstSec, buckets, interval));
        Assert.assertArrayEquals(new double[] { 0.4, 0.1 }, TimeSeriesDownsampler.countRatePerBucket(ts, firstSec, buckets, interval), 0.0001);
        Assert.assertArrayEquals(new double[] { 0.2, 0.1 }, TimeSeriesDownsampler.errorRatePerBucket(ts, firstSec, buckets, interval), 0.0001);

        // Verify empty bucket produces 0 mean when other buckets have data (bucket 2 has 777 at 120s, bucket 3 at [130..139] is empty)
        Assert.assertArrayEquals(new int[] { 325, 250, 777, 0 }, TimeSeriesDownsampler.meanPerBucket(ts, firstSec, 4, interval));
    }
}
