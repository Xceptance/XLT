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

import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link Median} class.
 */
@SuppressWarnings("deprecation")
public class MedianTest
{
    @Test
    public void testBasics()
    {
        final Median median = new Median();

        Assert.assertEquals(0.0, median.getMedianValue(), 0.0);

        median.addValue(5);
        Assert.assertEquals(5.0, median.getMedianValue(), 0.0);
    }

    @Test
    public void testEven()
    {
        final Median median = new Median();

        median.addValue(2);
        median.addValue(3);
        median.addValue(4);
        median.addValue(4);
        median.addValue(5);
        median.addValue(5);
        median.addValue(6);
        median.addValue(8);
        Assert.assertEquals(4.5, median.getMedianValue(), 0.0);
    }

    @Test
    public void testOdd()
    {
        final Median median = new Median();

        median.addValue(2);
        median.addValue(4);
        median.addValue(4);
        median.addValue(5);
        median.addValue(5);
        median.addValue(6);
        median.addValue(8);
        Assert.assertEquals(5.0, median.getMedianValue(), 0.0);
    }

    @Test
    public void testPrecision()
    {
        final Median median = new Median(10);

        median.addValue(2);
        median.addValue(4);
        median.addValue(10);
        median.addValue(11);
        median.addValue(111);
        median.addValue(1111);
        Assert.assertEquals(10.0, median.getMedianValue(), 0.0);
    }

    @Test
    public void testRandom()
    {
        final Random rng = new Random();

        // random number of values [1..n]
        final int length = rng.nextInt(1000000) + 1;

        // random precision [1..100]
        final int precision = rng.nextInt(100) + 1;

        final Median median = new Median(precision);
        final int[] values = new int[length];

        for (int i = 0; i < length; i++)
        {
            // random values [0..999999]
            final int j = rng.nextInt(1000000);

            median.addValue(j);
            values[i] = j / precision * precision;
        }

        final double expectedMedian = getMedian(values);
        final double actualMedian = median.getMedianValue();

        Assert.assertEquals(expectedMedian, actualMedian, 0.0);
    }

    private double getMedian(final int[] values)
    {
        Arrays.sort(values);

        final int length = values.length;
        double median;

        if (length % 2 == 0)
        {
            median = (values[length / 2 - 1] + values[length / 2]) / 2.0;
        }
        else
        {
            median = values[length / 2];
        }

        return median;
    }
}
