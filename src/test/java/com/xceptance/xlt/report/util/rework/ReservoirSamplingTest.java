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
package com.xceptance.xlt.report.util.rework;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.function.BiFunction;

import org.apache.datasketches.tdigest.TDigestDouble;
import org.junit.Test;

import com.datadoghq.sketch.ddsketch.DDSketch;
import com.datadoghq.sketch.ddsketch.DDSketches;
import com.xceptance.xlt.report.util.RuntimeHistogram;

import it.unimi.dsi.util.FastRandom;

/**
 * Test class for {@link ReservoirSampling}.
 * Verifies reservoir r behavior, quantile calculations, and range estimates.
 * 
 * Initial version created by CoPilot Claude Sonnet 4.5, reviewed fully manually
 */
public class ReservoirSamplingTest
{
    /**
     * Verifies that a newly created instance has zero count and empty reservoir.
     */
    @Test
    public void testInitialState()
    {
        var r = new ReservoirSampling(100);
        assertEquals(0, r.getTotalCount());
        assertEquals(0, r.getReservoirSize());
    }

    /**
     * Tests that adding values increases both total count and reservoir size
     * when reservoir is not yet full.
     */
    @Test
    public void testAddValues()
    {
        var r = new ReservoirSampling(100);

        for (int i = 0; i < 50; i++)
        {
            r.add(i);
        }

        assertEquals(50, r.getTotalCount());
        assertEquals(50, r.getReservoirSize());
    }

    /**
     * Verifies that reservoir size never exceeds the configured maximum,
     * even when more values are added.
     */
    @Test
    public void testReservoirSizeLimit()
    {
        var r = new ReservoirSampling(100);

        for (int i = 0; i < 200; i++)
        {
            r.add(i);
        }

        assertEquals(200, r.getTotalCount());
        assertEquals(100, r.getReservoirSize());
    }

    /**
     * Tests that querying quantile on empty reservoir throws IllegalStateException.
     */
    @Test(expected = IllegalStateException.class)
    public void testGetQuantileWithEmptyReservoir()
    {
        var r = new ReservoirSampling(100);
        r.getQuantile(0.5);
    }

    /**
     * Tests that negative quantile values are rejected.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetQuantileWithNegativeValue()
    {
        var r = new ReservoirSampling(100);

        r.add(1);
        r.getQuantile(-0.1);
    }

    /**
     * Tests that quantile values greater than 1.0 are rejected.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetQuantileWithValueGreaterThanOne()
    {
        var r = new ReservoirSampling(100);

        r.add(1);
        r.getQuantile(1.1);
    }

    /**
     * Verifies that all quantiles return the same value when only one value exists.
     */
    @Test
    public void testGetQuantileWithSingleValue()
    {
        var r = new ReservoirSampling(100);

        r.add(42);

        assertEquals(42.0, r.getQuantile(0.0), 0.0001);
        assertEquals(42.0, r.getQuantile(0.5), 0.0001);
        assertEquals(42.0, r.getQuantile(1.0), 0.0001);
    }

    /**
     * Tests quantile calculations with a sorted sequence of values.
     * Allows for some tolerance due to r approximation.
     */
    @Test
    public void testGetQuantileWithSortedValues()
    {
        var r = new ReservoirSampling(100);

        for (int i = 1; i <= 100; i++)
        {
            r.add(i);
        }

        assertEquals(1.0, r.getQuantile(0.0), 0.001);
        assertEquals(50.0, r.getQuantile(0.5), 5.0);
        assertEquals(100.0, r.getQuantile(1.0), 0.001);
        assertEquals(25.0, r.getQuantile(0.25), 5.0);
        assertEquals(75.0, r.getQuantile(0.75), 5.0);
    }

    /**
     * Tests quantile calculations with a sorted sequence of values.
     * Allows for some tolerance due to r approximation.
     */
    @Test
    public void testGetQuantileWithReversedValues()
    {
        var r = new ReservoirSampling(100);

        for (int i = 100; i > 0; i--)
        {
            r.add(i);
        }

        assertEquals(1.0, r.getQuantile(0.0), 0.001);
        assertEquals(50.0, r.getQuantile(0.5), 5.0);
        assertEquals(100.0, r.getQuantile(1.0), 0.001);
        assertEquals(25.0, r.getQuantile(0.25), 5.0);
        assertEquals(75.0, r.getQuantile(0.75), 5.0);
    }

    /**
     * Tests that range estimates return expected counts based on quantile fractions.
     */
    @Test
    public void testEstimateCountInRange()
    {
        var r = new ReservoirSampling(100);

        for (int i = 0; i < 1000; i++)
        {
            r.add(i);
        }

        assertEquals(500, r.estimateCountInRange(0.0, 0.5));
        assertEquals(250, r.estimateCountInRange(0.25, 0.5));
        assertEquals(1000, r.estimateCountInRange(0.0, 1.0));
        assertEquals(0, r.estimateCountInRange(0.5, 0.5));
    }

    /**
     * Tests that negative min quantile is rejected.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testEstimateCountInRangeWithInvalidMinQuantile()
    {
        var r = new ReservoirSampling(100);

        r.add(1);
        r.estimateCountInRange(-0.1, 0.5);
    }

    /**
     * Tests that max quantile greater than 1.0 is rejected.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testEstimateCountInRangeWithInvalidMaxQuantile()
    {
        var r = new ReservoirSampling(100);

        r.add(1);
        r.estimateCountInRange(0.0, 1.1);
    }

    /**
     * Tests that min quantile greater than max quantile is rejected.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testEstimateCountInRangeWithReversedQuantiles()
    {
        var r = new ReservoirSampling(100);

        r.add(1);
        r.estimateCountInRange(0.7, 0.3);
    }

    /**
     * Tests behavior with a large dataset to verify r maintains
     * reasonable approximations of the data distribution.
     */
    @Test
    public void testLargeDataset_increasing()
    {
        // the seed makes the test repeatable
        ReservoirSampling larger = new ReservoirSampling(1_000, 12345L);

        for (int i = 0; i < 100_000; i++)
        {
            larger.add(i);
        }

        assertEquals(100_000, larger.getTotalCount());
        assertEquals(1_000, larger.getReservoirSize());

        final double median = larger.getQuantile(0.5);
        assertEquals(49_999.5, median, 70.0); // allow some tolerance
    }

    /**
     * Tests that minimum (0.0) and maximum (1.0) quantiles return
     * the actual min and max values but only when the reservoir is
     * larger than the data set.
     */
    @Test
    public void testQuantileEdgeCases()
    {
        var r = new ReservoirSampling(1000);

        for (int i = 1; i <= 100; i++)
        {
            r.add(i * 10);
        }

        double min = r.getQuantile(0.0);
        double max = r.getQuantile(1.0);

        assertEquals(10, min, 0.001);
        assertEquals(1000, max, 0.001);
    }

    /**
     * Tests that duplicate values are handled correctly and all quantiles
     * return the same value when all samples are identical.
     */
    @Test
    public void testSingleValue_Full()
    {
        var r = new ReservoirSampling(100);

        for (int i = 0; i < 100; i++)
        {
            r.add(5);
        }

        assertEquals(5.0, r.getQuantile(0.0), 0.001);
        assertEquals(5.0, r.getQuantile(0.5), 0.001);
        assertEquals(5.0, r.getQuantile(1.0), 0.001);
    }

    /**
     * Tests that duplicate values are handled correctly and all quantiles
     * return the same value when all samples are identical.
     */
    @Test
    public void testSingleValue_Half()
    {
        var r = new ReservoirSampling(100);

        for (int i = 0; i < 50; i++)
        {
            r.add(5);
        }

        assertEquals(5.0, r.getQuantile(0.0), 0.001);
        assertEquals(5.0, r.getQuantile(0.5), 0.001);
        assertEquals(5.0, r.getQuantile(1.0), 0.001);
    }

    /**
     * Calculates the quantile using linear interpolation.
     * 
     * @param sortedArray The input array (must be sorted!)
     * @param percentile The desired percentile (e.g., 25 for Q1, 50 for Median)
     * @return The calculated value
     */
    private static int getQuantile(int[] sortedArray, double percentile) 
    {
        if (sortedArray == null || sortedArray.length == 0) 
        {
            throw new IllegalArgumentException("Array cannot be empty");
        }

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

        return (int) (lowerValue + (upperValue - lowerValue) * fraction);
    }
    
    /**
     * Helper to able to probe sampling
     */
    private static void testSampling(final FastRandom r, 
                                     final boolean shuffleOrder,
                                     final ReservoirSampling reservoir, 
                                     final int iterations,
                                     final double deviation,
                                     final BiFunction<FastRandom, Integer, Integer> valueProvider)
    {
        final int[] values = new int[iterations];
        // just to see how far we are off from a sketch
        TDigestDouble tdigest = new TDigestDouble((short) 100);
        DDSketch ddSketch = DDSketches.unboundedDense(0.01);
        
        // this is here for historical comparison in manual mode
        RuntimeHistogram histogram = new RuntimeHistogram(10); 
        
        // draw numbers first
        for (int i = 1; i <= iterations; i++)
        {
            int value = valueProvider.apply(r, i);
            values[i - 1] = value;
        }

        // make sure we have them all randomized when adding
        if (shuffleOrder)
        {
            shuffle(values, r);
        }

        // apply them to the reservoir
        for (int value : values)
        {
            reservoir.add(value);
            tdigest.update(value);
            histogram.addValue(value);
            ddSketch.accept(value);
        }

        // sort the values for quantile verification
        Arrays.sort(values);

        // print values side by ide for manual verification if needed
        for (int i = 0; i <= 100; i++)
        {
            double p = i / 100.0;

            double expectedQuantile = getQuantile(values, p);

            double actualQuantile = reservoir.getQuantile(p);
            double actualTDigest = tdigest.getQuantile(p);
            double actualDDSketch = ddSketch.getValueAtQuantile(p);
            double actualHistogram = histogram.getPercentile(i);
            
            System.out.printf("P%3.0f: expected=%8.1f, reservoir=%8.1f, tdigest=%8.1f, ddSketch=%8.1f, h, histogram=%8.1f%n", 
                              p * 100.0d, expectedQuantile, actualQuantile, actualTDigest, actualDDSketch, actualHistogram);
        }
        System.out.println();
        
        // verify that our recording values match what the reservoir has
        // and the quantiles are correct
        for (int i = 0; i <= 100; i++)
        {
            double p = i / 100.0;

            double expectedQuantile = getQuantile(values, p);

            double actualQuantile = reservoir.getQuantile(p);
            assertEquals("P"+ i, expectedQuantile, actualQuantile, deviation); // allow some tolerance
        }
    }

    private static void testSampling(final FastRandom r, 
                                     final ReservoirSampling reservoir, 
                                     final int iterations,
                                     final double deviation,
                                     final BiFunction<FastRandom, Integer, Integer> valueProvider)
    {
        testSampling(r, true, reservoir, iterations, deviation, valueProvider);
    }
    
    /**
     * Shuffles an array using the Fisher-Yates algorithm with a seeded random generator.
     * 
     * @param array the array to shuffle
     * @param r the random reproducible shuffling
     */
    private static void shuffle(int[] array, final FastRandom r)
    {
        for (int i = array.length - 1; i > 0; i--)
        {
            int j = r.nextInt(i + 1);

            // swap elements at i and j
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    @Test
    public void testSamplingDistribution()
    {
        testSampling(new FastRandom(123L), new ReservoirSampling(10, 12L), 10, 0, (r, i) -> r.nextInt(1_000));
        testSampling(new FastRandom(223L), new ReservoirSampling(50, 12L), 50, 0, (r, i) -> r.nextInt(1_000));
        testSampling(new FastRandom(323L), new ReservoirSampling(50, 12L), 10, 0, (r, i) -> r.nextInt(1_000));
        testSampling(new FastRandom(423L), new ReservoirSampling(100, 12L), 61, 0, (r, i) -> r.nextInt(1_000));

        testSampling(new FastRandom(123L), new ReservoirSampling(1000, 12L), 1000, 0, (r, i) -> r.nextInt(10_000));
        testSampling(new FastRandom(123L), new ReservoirSampling(1000, 12L), 10120, 350, (r, i) -> r.nextInt(10_000));
        testSampling(new FastRandom(123L), new ReservoirSampling(1000, 12L), 112313, 350, (r, i) -> r.nextInt(10_000));
        testSampling(new FastRandom(123L), new ReservoirSampling(1000, 12L), 1972161, 350, (r, i) -> r.nextInt(10_000));

        testSampling(new FastRandom(123L), new ReservoirSampling(1000, 12L), 1000, 0, (r, i) -> r.nextInt(10_000));
        testSampling(new FastRandom(123L), new ReservoirSampling(900, 192L), 1000, 28, (r, i) -> r.nextInt( 1_000));
        testSampling(new FastRandom(123L), new ReservoirSampling(800, 172L), 1000, 12, (r, i) -> r.nextInt( 1_000));
        testSampling(new FastRandom(123L), new ReservoirSampling(700, 882L), 1000, 39, (r, i) -> r.nextInt( 1_000));
        testSampling(new FastRandom(123L), new ReservoirSampling(600, 992L), 1000, 37, (r, i) -> r.nextInt( 1_000));
        testSampling(new FastRandom(123L), new ReservoirSampling(500, 216L), 1000, 40, (r, i) -> r.nextInt( 1_000));
        testSampling(new FastRandom(423L), new ReservoirSampling(400, 332L), 1000, 80, (r, i) -> r.nextInt( 1_000));
        testSampling(new FastRandom(123L), new ReservoirSampling(300, 332L), 1000, 87, (r, i) -> r.nextInt( 1_000));
        testSampling(new FastRandom(123L), new ReservoirSampling(200, 332L), 1000, 99, (r, i) -> r.nextInt( 1_000));
        testSampling(new FastRandom(123L), new ReservoirSampling(100, 332L), 1000, 80, (r, i) -> r.nextInt( 1_000));
    }


    @Test
    public void testSampling_Increasing_SmallSize_equals_Count()
    {
        testSampling(new FastRandom(123L), true,
                     new ReservoirSampling(10, 12L), 10, 0,
                     (r, i) -> {
                         if (i == 0) return 0;
                         if (i == 9) return 1_000;
                         return r.nextInt(1_000);
                     });
        testSampling(new FastRandom(123L), false,
                     new ReservoirSampling(10, 12L), 10, 0, 
                     (r, i) -> {
                         if (i == 0) return 0;
                         if (i == 9) return 1_000;
                         return r.nextInt(1_000);
                     });
    }
    
    @Test
    public void testSampling_RealisticSamples_FewOutliers_Size1000()
    {
        final int SIZE1 = 100_000;
        testSampling(new FastRandom(123L), true,
                     new ReservoirSampling(1000, 12L), SIZE1, 1,
                     (r, i) -> {
                         if (i < SIZE1 - 1000) return r.nextInt(500);
                         if (i < SIZE1 - 100) return r.nextInt(1000, 5000);
                         if (i < SIZE1 - 10) return r.nextInt(28000, 30000);
                         return r.nextInt(60_000);
                     });
    }
    
    @Test
    public void testSampling_RealisticSamples_Wide_Size1000()
    {
        final int SIZE1 = 100_000;
        testSampling(new FastRandom(123L), true,
                     new ReservoirSampling(1000, 12L), SIZE1, 1,
                     (r, i) -> {
                         return r.nextInt(30_000);
                     });
    }
    
    @Test
    public void testSampling_RealisticSamples_Stripes_Size1000()
    {
        final int SIZE1 = 100_000;
        testSampling(new FastRandom(123L), true,
                     new ReservoirSampling(1000, 12L), SIZE1, 1,
                     (r, i) -> {
                         if (i < SIZE1 - 50_000) return r.nextInt(200,500);
                         if (i < SIZE1 - 1_000) return r.nextInt(2000, 3000);
                         if (i < SIZE1 - 100) return r.nextInt(10000, 11000);
                         return r.nextInt(28000, 30000);
                                              
                     });
    }
    
    @Test
    public void testSampling_Increasing_Size_equals_Count()
    {
        testSampling(new FastRandom(1253L), true,
                     new ReservoirSampling(1000, 12L), 1000, 0,  
                     (r, i) -> i);
        testSampling(new FastRandom(1253L), false,
                     new ReservoirSampling(1000, 12L), 1000, 0,  
                     (r, i) -> i);
    }
}

