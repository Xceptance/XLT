/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

        assertEquals(0.0, histogram.getPercentile(50), 0.0);
        assertEquals(0.0, histogram.getMedianValue(), 0.0);
        assertEquals(0, histogram.getValueCount());
        assertEquals(0, histogram.getNumberOfBuckets());
        assertEquals(1, histogram.getPrecision());
        assertTrue(histogram.isEmpty());

        histogram.addValue(5);
        assertEquals(5.0, histogram.getPercentile(0), 0.0);
        assertEquals(5.0, histogram.getPercentile(25), 0.0);
        assertEquals(5.0, histogram.getPercentile(50), 0.0);
        assertEquals(5.0, histogram.getPercentile(100), 0.0);
        assertEquals(5.0, histogram.getMedianValue(), 0.0);
        assertEquals(1, histogram.getValueCount());
        assertEquals(1, histogram.getNumberOfBuckets());
        assertEquals(1, histogram.getPrecision());
        assertFalse(histogram.isEmpty());
    }

    @Test
    public void testEven()
    {
        final RuntimeHistogram histogram = new RuntimeHistogram();
        int[] values = new int[] { 2, 3, 4, 5, 6, 7, 8, 9 };
        
        for (int v : values)
        {
            histogram.addValue(v);
        }

        for (int i = 0; i <= 100; i = i + 1)
        {
            assertEquals(getPercentile(i, values), histogram.getPercentile(i), 0.0);
        }

        assertEquals(5.5, histogram.getMedianValue(), 0.0);
        assertEquals(8, histogram.getValueCount());
        assertEquals(8, histogram.getNumberOfBuckets());
    }

    @Test
    public void testOdd()
    {
        final RuntimeHistogram histogram = new RuntimeHistogram();

        int[] values = new int[] { 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        
        for (int v : values)
        {
            histogram.addValue(v);
        }

        for (int i = 0; i <= 100; i = i + 1)
        {
            assertEquals(getPercentile(i, values), histogram.getPercentile(i), 0.0);
        }

        assertEquals(6.0, histogram.getMedianValue(), 0.0);
        assertEquals(9, histogram.getValueCount());
        assertEquals(9, histogram.getNumberOfBuckets());
    }

    @Test
    public void testPrecision_8()
    {
        final RuntimeHistogram histogram = new RuntimeHistogram(8);

        assertEquals(8, histogram.getPrecision());

        histogram.addValue(2); // becomes 0 extends to 0
        histogram.addValue(4); // 0 extends to 0
        histogram.addValue(10); // 1 extends to 8
        histogram.addValue(11); // 1 extends to 8
        histogram.addValue(111); // 13 extends to 104
        histogram.addValue(1111); // 139 extends to 1112

        assertEquals(8.0, histogram.getPercentile(50), 0.0);
        assertEquals(8.0, histogram.getMedianValue(), 0.0);
        assertEquals(6, histogram.getValueCount());
        assertEquals((1111 >> 3) - (2 >> 3) + 1, histogram.getNumberOfBuckets());
    }

    @Test
    public void testPrecision_10_becomes_16()
    {
        // 10 will be converted to 16 internally to allow bitshifting
        final RuntimeHistogram histogram = new RuntimeHistogram(10);

        histogram.addValue(200);
        histogram.addValue(300);
        histogram.addValue(400);
        histogram.addValue(500);
        histogram.addValue(600);
        histogram.addValue(700);

        final int r = ((500 >> 4 << 4) + (400 >> 4 << 4)) / 2; 
        assertEquals(r, histogram.getPercentile(50), 0.0);
        assertEquals(r, histogram.getMedianValue(), 0.0);
        assertEquals(6, histogram.getValueCount());
        assertEquals((700 >> 4) - (200 >> 4) + 1, 
                     histogram.getNumberOfBuckets());
    }

    @Test
    public void testPrecision_Even_16()
    {
        final RuntimeHistogram histogram = new RuntimeHistogram(16);

        histogram.addValue(15); // 0, 0 
        histogram.addValue(31); // 1, 16 
        histogram.addValue(47); // 2, 32
        histogram.addValue(63); // 3, 48
        histogram.addValue(79); // 4, 64
        histogram.addValue(95); // 5, 80
        histogram.addValue(111); // 6, 96
        histogram.addValue(127); // 7, 112

        assertEquals((48 + 64) / 2, histogram.getPercentile(50), 0.0);
        assertEquals((48 + 64) / 2, histogram.getMedianValue(), 0.0);
        assertEquals(8, histogram.getValueCount());
        assertEquals(8, histogram.getNumberOfBuckets());
    }

    @Test
    public void testPrecision_Odd_16()
    {
        // 10 will be converted to 16 internally to allow bitshifting
        final RuntimeHistogram histogram = new RuntimeHistogram(16);

        histogram.addValue(31); // 1, 16 
        histogram.addValue(47); // 2, 32
        histogram.addValue(63); // 3, 48
        histogram.addValue(79); // 4, 64
        histogram.addValue(95); // 5, 80
        histogram.addValue(111); // 6, 96
        histogram.addValue(127); // 7, 112

        final int r = (79 >> 4 << 4);
        assertEquals(r, histogram.getPercentile(50), 0.0);
        assertEquals(r, histogram.getMedianValue(), 0.0);
        assertEquals(7, histogram.getValueCount());
        assertEquals(((127 >> 4) - (31 >> 4) + 1), histogram.getNumberOfBuckets());
    }

    @Test
    public void testTypicalRuntimes()
    {
        final RuntimeHistogram histogram = new RuntimeHistogram();
        final List<Integer> runtimes = new ArrayList<Integer>();

        for (int i = 0; i <= 1000; i++)
        {
            runtimes.add(i);
        }

        final int[] runtimeArray = new int[runtimes.size()];
        int index = 0;
        for (var r : runtimes)
        {
            histogram.addValue(r);
            runtimeArray[index++] = r;
        }

        for (int i = 0; i <= 100; i += 10)
        {
            var p = i / 100.0;
//            System.out.printf("P%3d: %f vs %f%n", i, 
//                              getQuantile(runtimeArray, p), histogram.getQuantile(p));
            assertEquals(
                         String.format("P%d", i), 
                         getQuantile(runtimeArray, p), histogram.getQuantile(p), 0.0);
        }
    }

    @Test
    public void testRandom()
    {
        final Random rng = new Random();
        final int LENGTH = 10000;

        // random number of values [1..n]
        final int length = rng.nextInt(LENGTH) + 100;

        // fill histogram and our test array 
        final RuntimeHistogram histogram = new RuntimeHistogram();
        final int[] values = new int[length];

        for (int i = 0; i < length; i++)
        {
            // random values [0..999999]
            final int j = rng.nextInt(30000);
            values[i] = j;
            histogram.addValue(j);
        }

        // ensure the test array is sorted
        Arrays.sort(values);

        // now check the percentile for each permille
        for (int pm = 1; pm <= 1000; pm++)
        {
            final double p = pm / 10.0d;

            checkPercentile(p, values, histogram);
        }
    }

    private void checkPercentile(final double p, final int[] values, final RuntimeHistogram histogram)
    {
        final double expectedValue = getPercentile(p, values);
        final double actualValue = histogram.getPercentile(p);

        assertEquals("P" + Double.toString(p), expectedValue, actualValue, 0.0);
    }

    private double getPercentile(final double p, final int[] values)
    {
        final double percentile;

        if (values.length == 0)
        {
            percentile = 0;
        }
        else if (p == 0.0d)
        {
            percentile = values[0];
        }
        else if (p == 100.0d)
        {
            percentile = values[values.length - 1];
        }
        else
        {
            final double np = (double)values.length * (p / 100.0d);

            if ((np % 1) == 0)
            {
                final int i1 = (int) np;
                final int i2 = i1 + 1;

                percentile = (values[i1 - 1] + values[i2 - 1]) / 2.0d;
            }
            else
            {
                final int i = (int) Math.ceil(np);

                percentile = values[i - 1];
            }
        }

        return percentile;
    }

    /**
     * Calculates the quantile using linear interpolation.
     * 
     * @param sortedArray The input array (must be sorted!)
     * @param percentile The desired percentile (e.g., 25 for Q1, 50 for Median)
     * @return The calculated value
     */
    private static double getQuantile(int[] data, double percentile) 
    {
        if (data == null || data.length == 0) 
        {
            throw new IllegalArgumentException("Array cannot be empty");
        }

        // Sort reservoir to calculate quantile
        // we call that at the end
        final int[] sortedArray = Arrays.copyOf(data, data.length);
        Arrays.sort(sortedArray);

        // 1. Calculate the index in the sorted array
        // Formula: index = (N - 1) * p
        double index = (sortedArray.length - 1) * percentile;

        // 2. Split index into integer and decimal parts
        int lowerIndex = (int) index;
        double fraction = index - lowerIndex;

        // 3. Handle the edge case where the index is exactly the last element
        if (lowerIndex + 1 >= sortedArray.length) 
        {
            return sortedArray[lowerIndex];
        }

        // 4. Linear Interpolation
        // Value = LowerValue + (Difference * fraction)
        double lowerValue = sortedArray[lowerIndex];
        double upperValue = sortedArray[lowerIndex + 1];

        return (lowerValue + (upperValue - lowerValue) * fraction);
    }
    
    // ================================
    // Check for getCountForValues variations
    //
    @Test
    public void testGetCountForValue_EmptyHistogram()
    {
        var h1 = new RuntimeHistogram();
        assertEquals(0L, h1.getCountForValue(0, 100));
        assertEquals(0L, h1.getCountForValue(40, 100));

        var h2 = new RuntimeHistogram(8);
        assertEquals(0L, h2.getCountForValue(0, 100));
        assertEquals(0L, h2.getCountForValue(40, 100));
    }

    @Test
    public void testGetCountForValue_SingleValue()
    {
        RuntimeHistogram histogram = new RuntimeHistogram();
        histogram.addValue(50);
        
        assertEquals(1, histogram.getCountForValue(0, 100));
        assertEquals(1, histogram.getCountForValue(50, 50));
        assertEquals(0, histogram.getCountForValue(0, 49));
        assertEquals(0, histogram.getCountForValue(51, 100));
    }

    @Test
    public void testGetCountForValue_MultipleValuesInRange()
    {
        RuntimeHistogram histogram = new RuntimeHistogram();
        histogram.addValue(10);
        histogram.addValue(20);
        histogram.addValue(30);
        histogram.addValue(40);
        histogram.addValue(50);
        
        assertEquals(5, histogram.getCountForValue(0, 100));
        assertEquals(3, histogram.getCountForValue(20, 40));
        assertEquals(2, histogram.getCountForValue(10, 20));
    }

    @Test
    public void testGetCountForValue_RangeOutsideData()
    {
        RuntimeHistogram histogram = new RuntimeHistogram();
        histogram.addValue(50);
        histogram.addValue(60);
        
        assertEquals(0, histogram.getCountForValue(0, 40));
        assertEquals(0, histogram.getCountForValue(100, 200));
    }

    @Test
    public void testGetCountForValue_PartialOverlap()
    {
        RuntimeHistogram histogram = new RuntimeHistogram();
        histogram.addValue(20);
        histogram.addValue(30);
        histogram.addValue(40);
        histogram.addValue(50);
        
        assertEquals(2, histogram.getCountForValue(10, 35));
        assertEquals(2, histogram.getCountForValue(35, 60));
    }

    @Test
    public void testGetCountForValue_WithPrecision()
    {
        RuntimeHistogram histogram = new RuntimeHistogram(8);
        histogram.addValue(10);
        histogram.addValue(15);
        histogram.addValue(20);
        
        // Values 10 and 15 fall into bucket 8-15, value 20 into bucket 16-23
        assertEquals(2, histogram.getCountForValue(8, 15));
        assertEquals(1, histogram.getCountForValue(16, 23));
    }

    @Test
    public void testGetCountForValue_DuplicateValues()
    {
        RuntimeHistogram histogram = new RuntimeHistogram();
        for (int i = 0; i < 10; i++)
        {
            histogram.addValue(50);
        }
        
        assertEquals(10, histogram.getCountForValue(50, 50));
        assertEquals(10, histogram.getCountForValue(0, 100));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCountForValue_StartGreaterThanEnd()
    {
        RuntimeHistogram histogram = new RuntimeHistogram();
        histogram.addValue(50);
        histogram.getCountForValue(100, 50);    
    }

    @Test
    public void testGetCountForValue_StartEqualsEnd()
    {
        RuntimeHistogram histogram = new RuntimeHistogram();
        histogram.addValue(50);
        histogram.addValue(50);
        
        assertEquals(2, histogram.getCountForValue(50, 50));
    }

    @Test
    public void testGetCountForValue_NegativeValues()
    {
        RuntimeHistogram histogram = new RuntimeHistogram();
        histogram.addValue(-50);
        histogram.addValue(-30);
        histogram.addValue(10);
        
        assertEquals(2, histogram.getCountForValue(-60, -20));
        assertEquals(1, histogram.getCountForValue(0, 20));
        assertEquals(3, histogram.getCountForValue(-100, 100));
    }
}
