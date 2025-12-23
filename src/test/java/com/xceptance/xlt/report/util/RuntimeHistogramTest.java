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

        assertEquals("P25", 
                     getPercentile(25, values), 
                     histogram.getPercentile(25), 0.0);
        for (int i = 0; i <= 100; i = i + 1)
        {
            assertEquals("P" + i, getPercentile(i, values), histogram.getPercentile(i), 0.0);
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
    public void testPrecision_0()
    {
        final RuntimeHistogram histogram = new RuntimeHistogram();

        int[] values = new int[] { 10, 20, 30 };

        for (int v : values)
        {
            histogram.addValue(v);
        }

        for (int i = 0; i <= 100; i = i + 1)
        {
            assertEquals(getPercentile(i, values), histogram.getPercentile(i), 0.0);
        }

        assertEquals(20.0, histogram.getMedianValue(), 0.0);
        assertEquals(3, histogram.getValueCount());

        assertEquals((30 >> 0) - (10 >> 0) + 1, histogram.getNumberOfBuckets());
    }

    @Test
    public void testPrecision_0_2()
    {
        final RuntimeHistogram histogram = new RuntimeHistogram();

        int[] values = new int[] { 10, 20, 30, 40 };

        for (int v : values)
        {
            histogram.addValue(v);
        }

        for (int i = 0; i <= 100; i = i + 1)
        {
            assertEquals(getPercentile(i, values), histogram.getPercentile(i), 0.0);
        }

        assertEquals(25.0, histogram.getMedianValue(), 0.0);
        assertEquals(4, histogram.getValueCount());

        assertEquals((40 >> 0) - (10 >> 0) + 1, histogram.getNumberOfBuckets());
    }

    @Test
    public void testPrecision_2_1()
    {
        int precision = 1;
        final RuntimeHistogram histogram = new RuntimeHistogram(1 << precision);

        int[] values = new int[] { 1000, 2000, 3000, 4000 };

        for (int v : values)
        {
            histogram.addValue(v);
        }

        for (int i = 0; i <= 100; i = i + 1)
        {
            assertEquals(
                         getPercentile(i, values, precision), 
                         histogram.getPercentile(i), 0.0);
        }

        assertEquals(2500.0, histogram.getMedianValue(), 0.0);
        assertEquals(4, histogram.getValueCount());
        assertEquals((4000 >> precision) - (1000 >> precision) + 1, histogram.getNumberOfBuckets());
    }

    @Test
    public void testPrecision_2_2()
    {
        int precision = 1;
        final RuntimeHistogram histogram = new RuntimeHistogram(1 << precision);

        int[] values = new int[] { 123, 918, 300, 8171 };

        for (int v : values)
        {
            histogram.addValue(v);
        }

        for (int i = 0; i <= 100; i = i + 1)
        {
            assertEquals(
                         "P" + i,
                         getPercentile(i, values, precision), 
                         histogram.getPercentile(i), 0.0);
        }

        assertEquals(609, histogram.getMedianValue(), 0.0);
        assertEquals(4, histogram.getValueCount());
        assertEquals((8171 >> precision) - (123 >> precision) + 1, histogram.getNumberOfBuckets());
    }

    @Test
    public void testPrecision_2_3()
    {
        int precision = 1;
        final RuntimeHistogram histogram = new RuntimeHistogram(1 << precision);

        int[] values = new int[] { 123, 918, 300, 8171, 86113 };

        for (int v : values)
        {
            histogram.addValue(v);
        }

        for (int i = 0; i <= 100; i = i + 1)
        {
            assertEquals(
                         "P" + i,
                         getPercentile(i, values, precision), 
                         histogram.getPercentile(i), 0.0);
        }

        assertEquals(918, histogram.getMedianValue(), 0.0);
        assertEquals(5, histogram.getValueCount());
        assertEquals((86113 >> precision) - (123 >> precision) + 1, histogram.getNumberOfBuckets());
    }

    @Test
    public void testPrecision_8_1()
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
    public void testPrecision_8_2()
    {
        final RuntimeHistogram histogram = new RuntimeHistogram(8);

        int[] values = new int[] { 10, 20, 30 };

        for (int v : values)
        {
            histogram.addValue(v);
        }

        for (int i = 0; i <= 100; i = i + 1)
        {
            System.out.format("P%s, %s, %s%n", i, getPercentile(i, values, 3), histogram.getPercentile(i));
            assertEquals(getPercentile(i, values, 3), histogram.getPercentile(i), 0.0);
        }

        assertEquals(16, histogram.getMedianValue(), 0.0);
        assertEquals(3, histogram.getValueCount());

        assertEquals((30 >> 3) - (10 >> 3) + 1, histogram.getNumberOfBuckets());
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
        return getPercentile(p, values, 0);
    }

    private double getPercentile(final double p, final int[] v, int precision)
    {
        // adjust the precision 
        final int[] values = Arrays.copyOf(v, v.length);
        for (int i = 0; i < values.length; i++)
        {
            values[i] = values[i] >> precision;
        }
        Arrays.sort(values);

        final double percentile;

        if (values.length == 0)
        {
            return 0;
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
            final double np = ((double)values.length) * (p / 100.0d);

            if ((np % 1) == 0)
            {
                final int i1 = (int) np;
                final double v1 = values[i1 - 1];
                final double v2 = values[i1];

                percentile = (v1 + v2) / 2.0d;
            }
            else
            {
                final int i = (int) Math.ceil(np);

                percentile = values[i - 1];
            }
        }

        return (percentile * (1 << precision));
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
        assertEquals(1, histogram.getCountForValue(50, 100));
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
    public void testGetCountForValue_With0Precision_LargerValues()
    {
        final var histogram = new RuntimeHistogram(); // precision = 0, resolution = 1
        int[] values = new int[]{ 145, 981, 81782, 998, 771, 1500, 2048, 4096 };
        Arrays.stream(values).forEach(histogram::addValue);

        // Test various ranges
        assertEquals(0, histogram.getCountForValue(0, 144));
        assertEquals(1, histogram.getCountForValue(0, 145));
        assertEquals("Range covering 771", 1, histogram.getCountForValue(768, 775));
        assertEquals("Range covering 981 and 998", 2, histogram.getCountForValue(976, 999));
        assertEquals("Range covering 2048 and 4096", 2, histogram.getCountForValue(2048, 4103));
        assertEquals("Full range", 8, histogram.getCountForValue(0, 100000));
        assertEquals("Range with no values", 0, histogram.getCountForValue(200, 500));
        assertEquals("Range covering largest value", 1, histogram.getCountForValue(81776, 81783));

        // Test edge cases around bucket boundaries
        assertEquals("Multiple mid-range values", 3, histogram.getCountForValue(700, 1000));
    }

    @Test
    public void testGetCountForValue_WithPrecision_LargerValues()
    {
        final var histogram = new RuntimeHistogram(8); // precision = 8, resolution = 8

        int[] values = new int[]{ 
            // 100, // 100 >> 3 = 12
            145, // 145 >> 3 = 18  
            771, // 771 >> 3 = 96
            // 871, // 871 >> 3 = 108
            981, // 981 >> 3 = 122
            998, // 998 >> 3 = 124
            1500, // 1500 >> 3 = 187
            // 1601, // 1601 >> 3 = 200
            2181, // 2181 >> 3 = 272
            7611, // 7611 >> 3 = 951
            81781 }; // 81781 >> 3 = 10222
        Arrays.stream(values).forEach(histogram::addValue);

        // all and the last
        assertEquals("Last", 1, histogram.getCountForValue(81782, 81782));
        assertEquals("Full range", 8, histogram.getCountForValue(0, 81782));

        // this is 1, because the 145 >> 3 = 18, so the bucket covers 144-151
        // 18 << 3 = 144 
        // 19 << 3 = 152
        assertEquals(1, histogram.getCountForValue(0, 144));
        // that is one bucket down
        assertEquals(0, histogram.getCountForValue(0, 143));

        assertEquals(1, histogram.getCountForValue(0, 145));
        assertEquals(2, histogram.getCountForValue(0, 771));
        assertEquals(3, histogram.getCountForValue(0, 981));
        assertEquals(4, histogram.getCountForValue(0, 998));
        assertEquals(5, histogram.getCountForValue(0, 1500));
        assertEquals(6, histogram.getCountForValue(0, 2181));
        assertEquals(7, histogram.getCountForValue(0, 7611));
        assertEquals(8, histogram.getCountForValue(0, 81781));
        
        histogram.addValue(1601);
        histogram.addValue(871);
        histogram.addValue(100);
        
        assertEquals(1, histogram.getCountForValue(0, 100));
        assertEquals(2, histogram.getCountForValue(0, 145));
        assertEquals(3, histogram.getCountForValue(0, 771));
        assertEquals(4, histogram.getCountForValue(0, 871));
        assertEquals(5, histogram.getCountForValue(0, 981));
        assertEquals(6, histogram.getCountForValue(0, 998));
        assertEquals(7, histogram.getCountForValue(0, 1500));
        assertEquals(8, histogram.getCountForValue(0, 1601));
        assertEquals(9, histogram.getCountForValue(0, 2181));
        assertEquals(10, histogram.getCountForValue(0, 7611));
        assertEquals(11, histogram.getCountForValue(0, 81781));
        
        assertEquals(6, histogram.getCountForValue(0, 999));
        assertEquals(2, histogram.getCountForValue(1000, 1999));
        assertEquals(1, histogram.getCountForValue(2000, 2999));
        assertEquals(0, histogram.getCountForValue(3000, 3999));
        assertEquals(0, histogram.getCountForValue(4000, 4999));
        assertEquals(0, histogram.getCountForValue(5000, 5999));
        assertEquals(0, histogram.getCountForValue(6000, 6999));
        assertEquals(1, histogram.getCountForValue(7000, 7999));
        assertEquals(0, histogram.getCountForValue(8000, 8999));
        assertEquals(0, histogram.getCountForValue(9000, 9999));
        assertEquals(0, histogram.getCountForValue(80000, 80999));
        assertEquals(1, histogram.getCountForValue(81000, 81999));
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
