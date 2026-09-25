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

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link DoubleSummaryStatistics} class.
 */
public class DoubleSummaryStatisticsTest
{
    private static final double EPSILON = 1e-4;

    @Test
    public void testEmpty()
    {
        final DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        Assert.assertEquals(0, stats.getCount());
        Assert.assertEquals(0.0, stats.getSum(), EPSILON);
        Assert.assertEquals(0.0, stats.getSumOfSquares(), EPSILON);
        Assert.assertEquals(0.0, stats.getMinimum(), EPSILON);
        Assert.assertEquals(0.0, stats.getMaximum(), EPSILON);
        Assert.assertTrue(Double.isNaN(stats.getMean()));
    }

    @Test
    public void testAddValues()
    {
        final DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        stats.addValue(10.5);
        stats.addValue(20.5);
        stats.addValue(30.0);

        Assert.assertEquals(3, stats.getCount());
        Assert.assertEquals(61.0, stats.getSum(), EPSILON);
        Assert.assertEquals(10.5, stats.getMinimum(), EPSILON);
        Assert.assertEquals(30.0, stats.getMaximum(), EPSILON);
        Assert.assertEquals(61.0 / 3.0, stats.getMean(), EPSILON);
    }

    @Test
    public void testMergeNullOrEmpty()
    {
        final DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        stats.addValue(42.5);

        stats.merge(null);
        Assert.assertEquals(1, stats.getCount());
        Assert.assertEquals(42.5, stats.getSum(), EPSILON);

        stats.merge(new DoubleSummaryStatistics());
        Assert.assertEquals(1, stats.getCount());
        Assert.assertEquals(42.5, stats.getSum(), EPSILON);

        final DoubleSummaryStatistics empty = new DoubleSummaryStatistics();
        empty.merge(stats);
        Assert.assertEquals(1, empty.getCount());
        Assert.assertEquals(42.5, empty.getSum(), EPSILON);
        Assert.assertEquals(42.5, empty.getMinimum(), EPSILON);
        Assert.assertEquals(42.5, empty.getMaximum(), EPSILON);
    }

    @Test
    public void testMergeEquivalence()
    {
        final DoubleSummaryStatistics sequential = new DoubleSummaryStatistics();
        final DoubleSummaryStatistics part1 = new DoubleSummaryStatistics();
        final DoubleSummaryStatistics part2 = new DoubleSummaryStatistics();

        final Random rand = new Random(42);
        for (int i = 0; i < 5000; i++)
        {
            final double val = rand.nextDouble() * 1000.0;
            sequential.addValue(val);
            if (rand.nextBoolean())
            {
                part1.addValue(val);
            }
            else
            {
                part2.addValue(val);
            }
        }

        part1.merge(part2);

        Assert.assertEquals(sequential.getCount(), part1.getCount());
        Assert.assertEquals(sequential.getSum(), part1.getSum(), 1e-2);
        Assert.assertEquals(sequential.getSumOfSquares(), part1.getSumOfSquares(), 1e-2);
        Assert.assertEquals(sequential.getMinimum(), part1.getMinimum(), EPSILON);
        Assert.assertEquals(sequential.getMaximum(), part1.getMaximum(), EPSILON);
        Assert.assertEquals(sequential.getMean(), part1.getMean(), 1e-4);
        Assert.assertEquals(sequential.getStandardDeviation(), part1.getStandardDeviation(), 1e-4);
    }
}
