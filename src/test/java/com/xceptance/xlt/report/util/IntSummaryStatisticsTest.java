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
 * Tests for {@link IntSummaryStatistics}.
 */
public class IntSummaryStatisticsTest
{
    private static final double DELTA = 0.00001;

    @Test
    public void testEmpty()
    {
        final IntSummaryStatistics stats = new IntSummaryStatistics();
        Assert.assertEquals(0L, stats.getCount());
        Assert.assertEquals(0, stats.getMinimum());
        Assert.assertEquals(0, stats.getMaximum());
        Assert.assertEquals(0.0, stats.getSum(), DELTA);
        Assert.assertEquals(0.0, stats.getSumOfSquares(), DELTA);
        Assert.assertTrue(Double.isNaN(stats.getMean()));
        Assert.assertTrue(Double.isNaN(stats.getStandardDeviation()));
    }

    @Test
    public void testAddValues()
    {
        final IntSummaryStatistics stats = new IntSummaryStatistics();
        stats.addValue(10);
        stats.addValue(20);
        stats.addValue(30);

        Assert.assertEquals(3L, stats.getCount());
        Assert.assertEquals(10, stats.getMinimum());
        Assert.assertEquals(30, stats.getMaximum());
        Assert.assertEquals(60.0, stats.getSum(), DELTA);
        Assert.assertEquals(20.0, stats.getMean(), DELTA);
        Assert.assertEquals(1400.0, stats.getSumOfSquares(), DELTA);
        // Variance = 1400/3 - 400 = 66.66667 -> stdDev = sqrt(66.66667) = 8.1649658
        Assert.assertEquals(Math.sqrt(200.0 / 3.0), stats.getStandardDeviation(), DELTA);
    }

    @Test
    public void testMergeNullOrEmpty()
    {
        final IntSummaryStatistics stats1 = new IntSummaryStatistics();
        stats1.addValue(5);
        stats1.addValue(15);

        // Merge null
        stats1.merge(null);
        Assert.assertEquals(2L, stats1.getCount());
        Assert.assertEquals(5, stats1.getMinimum());
        Assert.assertEquals(15, stats1.getMaximum());

        // Merge empty
        stats1.merge(new IntSummaryStatistics());
        Assert.assertEquals(2L, stats1.getCount());
        Assert.assertEquals(5, stats1.getMinimum());
        Assert.assertEquals(15, stats1.getMaximum());

        // Merge into empty
        final IntSummaryStatistics emptyStats = new IntSummaryStatistics();
        emptyStats.merge(stats1);
        Assert.assertEquals(2L, emptyStats.getCount());
        Assert.assertEquals(5, emptyStats.getMinimum());
        Assert.assertEquals(15, emptyStats.getMaximum());
        Assert.assertEquals(20.0, emptyStats.getSum(), DELTA);
        Assert.assertEquals(250.0, emptyStats.getSumOfSquares(), DELTA);
    }

    @Test
    public void testMergeEquivalence()
    {
        final IntSummaryStatistics sequential = new IntSummaryStatistics();
        final IntSummaryStatistics statsA = new IntSummaryStatistics();
        final IntSummaryStatistics statsB = new IntSummaryStatistics();

        final int[] valuesA = { 12, 45, 99, 100, 2, 77, 85, 34 };
        final int[] valuesB = { 5, 200, 88, 14, 99, 3, 1000, 45 };

        for (final int v : valuesA)
        {
            sequential.addValue(v);
            statsA.addValue(v);
        }

        for (final int v : valuesB)
        {
            sequential.addValue(v);
            statsB.addValue(v);
        }

        statsA.merge(statsB);

        Assert.assertEquals(sequential.getCount(), statsA.getCount());
        Assert.assertEquals(sequential.getMinimum(), statsA.getMinimum());
        Assert.assertEquals(sequential.getMaximum(), statsA.getMaximum());
        Assert.assertEquals(sequential.getSum(), statsA.getSum(), DELTA);
        Assert.assertEquals(sequential.getSumOfSquares(), statsA.getSumOfSquares(), DELTA);
        Assert.assertEquals(sequential.getMean(), statsA.getMean(), DELTA);
        Assert.assertEquals(sequential.getStandardDeviation(), statsA.getStandardDeviation(), DELTA);
    }
}
