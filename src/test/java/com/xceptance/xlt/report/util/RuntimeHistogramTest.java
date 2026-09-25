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

import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link RuntimeHistogram} class.
 */
public class RuntimeHistogramTest
{
    @Test
    public void testBasics()
    {
        final RuntimeHistogram histogram = new RuntimeHistogram();

        Assert.assertEquals(0.0, histogram.getPercentile(50), 0.0);

        histogram.addValue(5);
        Assert.assertEquals(5.0, histogram.getPercentile(50), 0.0);
    }

    @Test
    public void testEven()
    {
        final RuntimeHistogram histogram = new RuntimeHistogram();

        histogram.addValue(2);
        histogram.addValue(3);
        histogram.addValue(4);
        histogram.addValue(4);
        histogram.addValue(5);
        histogram.addValue(5);
        histogram.addValue(6);
        histogram.addValue(8);
        Assert.assertEquals(4.5, histogram.getPercentile(50), 0.0);
    }

    @Test
    public void testOdd()
    {
        final RuntimeHistogram histogram = new RuntimeHistogram();

        histogram.addValue(2);
        histogram.addValue(4);
        histogram.addValue(4);
        histogram.addValue(5);
        histogram.addValue(5);
        histogram.addValue(6);
        histogram.addValue(8);
        Assert.assertEquals(5.0, histogram.getPercentile(50), 0.0);
    }

    @Test
    public void testPrecision()
    {
        final RuntimeHistogram histogram = new RuntimeHistogram(10);

        histogram.addValue(2);
        histogram.addValue(4);
        histogram.addValue(10);
        histogram.addValue(11);
        histogram.addValue(111);
        histogram.addValue(1111);
        Assert.assertEquals(10.0, histogram.getPercentile(50), 0.0);
    }

    @Test
    public void testRandom()
    {
        final Random rng = new Random();

        // random number of values [1..n]
        final int length = rng.nextInt(1000000) + 1;

        // random precision [1..100]
        final int precision = rng.nextInt(100) + 1;

        // fill histogram and our test array 
        final RuntimeHistogram histogram = new RuntimeHistogram(precision);
        final int[] values = new int[length];

        for (int i = 0; i < length; i++)
        {
            // random values [0..999999]
            final int j = rng.nextInt(1000000);

            histogram.addValue(j);
            values[i] = j / precision * precision;
        }

        // ensure the test array is sorted
        Arrays.sort(values);

        // now check the percentile for each permille
        for (int pm = 1; pm <= 1000; pm++)
        {
            final double p = pm / 10.0;
            
            checkPercentile(p, values, histogram);
        }
    }

    private void checkPercentile(final double p, final int[] values, final RuntimeHistogram histogram)
    {
        final double expectedValue = getPercentile(p, values);
        final double actualValue = histogram.getPercentile(p);

        Assert.assertEquals(expectedValue, actualValue, 0.0);
    }

    private double getPercentile(final double p, final int[] values)
    {
        final double percentile;

        if (values.length == 0)
        {
            percentile = 0;
        }
        else if (p == 100)
        {
            percentile = values[values.length - 1];
        }
        else
        {
            final double np = values.length * p / 100;

            if (np % 1 == 0)
            {
                final int i1 = (int) np;
                final int i2 = i1 + 1;

                percentile = (values[i1 - 1] + values[i2 - 1]) / 2.0;
            }
            else
            {
                final int i = (int) Math.ceil(np);

                percentile = values[i - 1];
            }
        }

        return percentile;
    }

    @Test
    public void testMergeNullOrEmpty()
    {
        final RuntimeHistogram h1 = new RuntimeHistogram();
        h1.addValue(10);
        h1.addValue(20);

        // merge null
        h1.merge(null);
        Assert.assertEquals(2, h1.getValueCount());
        Assert.assertEquals(15.0, h1.getMedianValue(), 0.0);

        // merge empty
        h1.merge(new RuntimeHistogram());
        Assert.assertEquals(2, h1.getValueCount());
        Assert.assertEquals(15.0, h1.getMedianValue(), 0.0);

        // merge into empty
        final RuntimeHistogram empty = new RuntimeHistogram();
        empty.merge(h1);
        Assert.assertEquals(2, empty.getValueCount());
        Assert.assertEquals(15.0, empty.getMedianValue(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeDifferentPrecisionThrows()
    {
        final RuntimeHistogram h1 = new RuntimeHistogram(1);
        final RuntimeHistogram h2 = new RuntimeHistogram(10);
        h1.addValue(10);
        h2.addValue(20);
        h1.merge(h2);
    }

    @Test
    public void testMergeOverlappingAndDisjoint()
    {
        final RuntimeHistogram sequential = new RuntimeHistogram();
        final RuntimeHistogram h1 = new RuntimeHistogram();
        final RuntimeHistogram h2 = new RuntimeHistogram();

        final int[] set1 = { 10, 20, 30, 40, 50 };
        final int[] set2 = { 35, 45, 55, 100, 200 };

        for (final int v : set1)
        {
            sequential.addValue(v);
            h1.addValue(v);
        }
        for (final int v : set2)
        {
            sequential.addValue(v);
            h2.addValue(v);
        }

        h1.merge(h2);

        Assert.assertEquals(sequential.getValueCount(), h1.getValueCount());
        for (double p = 1.0; p <= 100.0; p += 1.0)
        {
            Assert.assertEquals("Percentile " + p + " mismatch", sequential.getPercentile(p), h1.getPercentile(p), 0.0);
        }
    }

    @Test
    public void testMergeRandomMultiway()
    {
        final Random rng = new Random(42);
        final int precision = 5;

        final RuntimeHistogram sequential = new RuntimeHistogram(precision);
        final RuntimeHistogram h1 = new RuntimeHistogram(precision);
        final RuntimeHistogram h2 = new RuntimeHistogram(precision);
        final RuntimeHistogram h3 = new RuntimeHistogram(precision);

        for (int i = 0; i < 5000; i++)
        {
            final int val = rng.nextInt(50000);
            sequential.addValue(val);

            final int branch = rng.nextInt(3);
            if (branch == 0)
            {
                h1.addValue(val);
            }
            else if (branch == 1)
            {
                h2.addValue(val);
            }
            else
            {
                h3.addValue(val);
            }
        }

        h1.merge(h2);
        h1.merge(h3);

        Assert.assertEquals(sequential.getValueCount(), h1.getValueCount());
        for (double p = 1.0; p <= 100.0; p += 0.5)
        {
            Assert.assertEquals("Percentile " + p + " mismatch", sequential.getPercentile(p), h1.getPercentile(p), 0.0);
        }
    }
}
