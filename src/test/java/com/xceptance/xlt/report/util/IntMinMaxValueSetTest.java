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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests the {@link IntMinMaxValueSet} class.
 */
public class IntMinMaxValueSetTest
{
    private int minIndex;

    @Before
    public void setUp()
    {
        minIndex = Integer.MAX_VALUE;
    }

    @Test
    public void testFirstSecondSurvivesARealTimestamp()
    {
        // firstSecond is an int, so "firstSecond * 1000" overflowed for any timestamp past 1970-01-25.
        // The other tests here use small values, where the overflow does not show.
        final long from = 1763571456000L;

        final IntMinMaxValueSet set = new IntMinMaxValueSet(100);
        set.addOrUpdateValue(from, 42);

        Assert.assertEquals(from, set.getFirstSecond());
    }

    @Test
    public void testBasics()
    {
        final IntMinMaxValueSet minMaxValueSet = new IntMinMaxValueSet();

        minMaxValueSet.addOrUpdateValue(10000, 1);
        minMaxValueSet.addOrUpdateValue(11000, 1);
        minMaxValueSet.addOrUpdateValue(12000, 1);
        minMaxValueSet.addOrUpdateValue(1034000, 1);
        minMaxValueSet.addOrUpdateValue(1034000, 1);
        minMaxValueSet.addOrUpdateValue(1034000, 5);
        minMaxValueSet.addOrUpdateValue(1000, 1);
        minMaxValueSet.addOrUpdateValue(1034000, 1);
    }

    @Test
    public void testRandomWithDefaultSize()
    {
        testRandom(IntMinMaxValueSet.DEFAULT_SIZE);
    }

    @Test
    public void testRandomWithSpecifiedSize()
    {
        testRandom(12345);
    }

    @Test
    public void testShrinking()
    {
        final IntMinMaxValueSet minMaxValueSet = new IntMinMaxValueSet();
        final int[] values = new int[100000];

        for (int i = 0; i < values.length; i++)
        {
            final long time = i * 1000;

            minMaxValueSet.addOrUpdateValue(time, 1);
            updateValue(values, time);
        }

        final int[] condensedValues = condenseValues(values, minMaxValueSet.getSize(), minMaxValueSet.getScale());
        assertEquals(condensedValues, minMaxValueSet);
    }

    @Test
    public void testShiftingAndShrinking()
    {
        final IntMinMaxValueSet minMaxValueSet = new IntMinMaxValueSet();
        final int[] values = new int[100000];

        for (int i = values.length - 1; i >= 0; i--)
        {
            final long time = i * 1000;

            minMaxValueSet.addOrUpdateValue(time, 1);
            updateValue(values, time);
        }

        final int[] condensedValues = condenseValues(values, minMaxValueSet.getSize(), minMaxValueSet.getScale());
        assertEquals(condensedValues, minMaxValueSet);
    }

    private void assertEquals(final int[] values, final IntMinMaxValueSet minMaxValueSet)
    {
        final IntMinMaxValue[] minMaxValues = minMaxValueSet.getValues();

        for (int i = 0; i < minMaxValues.length; i++)
        {
            final long v1 = values[i];
            final long v2 = (minMaxValues[i] == null) ? 0 : minMaxValues[i].getAccumulatedValue();

            Assert.assertEquals(v1, v2);
        }
    }

    private int[] condenseValues(final int[] values, final int size, final int scale)
    {
        final int[] v = new int[2 * size];

        minIndex = minIndex / scale * scale;

        for (int i = minIndex, j = 0; j < v.length; i = i + scale, j++)
        {
            for (int k = 0; k < scale; k++)
            {
                if ((i + k) < values.length)
                {
                    v[j] += values[i + k];
                }
            }
        }

        return v;
    }

    private void testRandom(final int size)
    {
        final IntMinMaxValueSet minMaxValueSet = new IntMinMaxValueSet(size);
        final int[] values = new int[100000];
        final Random rng = new Random();

        for (int i = 0; i < values.length; i++)
        {
            final long time = rng.nextInt(values.length * 1000);

            minMaxValueSet.addOrUpdateValue(time, 1);
            updateValue(values, time);
        }

        final int[] condensedValues = condenseValues(values, minMaxValueSet.getSize(), minMaxValueSet.getScale());
        assertEquals(condensedValues, minMaxValueSet);
    }

    private void updateValue(final int[] values, final long time)
    {
        final int index = (int) (time / 1000);

        values[index]++;

        if (index < minIndex)
        {
            minIndex = index;
        }
    }

    @Test
    public void testConstructor()
    {
        final IntMinMaxValueSet set = new IntMinMaxValueSet(128);
        Assert.assertEquals(1, set.getScale());
        Assert.assertEquals(128, set.getSize());
    }

    @Test
    public void testSimpleTest()
    {
        final long from = 10000000L;
        final int size = 128;
        final IntMinMaxValueSet set = new IntMinMaxValueSet(size);

        for (int i = 0; i < size; i++)
        {
            set.addOrUpdateValue(from + (i * 1000L), 10);
        }

        Assert.assertEquals((from / 1000L * 1000L), set.getFirstSecond());
        Assert.assertEquals(from, set.getMinimumTime());
        Assert.assertEquals(from + (size - 1) * 1000L, set.getMaximumTime());
        Assert.assertEquals(128, set.getValueCount());

        final IntMinMaxValue[] actual = set.getValues();
        Assert.assertEquals(size, actual.length);

        final IntMinMaxValue[] expected = new IntMinMaxValue[size];
        for (int i = 0; i < size; i++)
        {
            expected[i] = new IntMinMaxValue(10);
        }
        Assert.assertArrayEquals(expected, actual);
    }

    /**
     * TODO: Ignored until we decide whether MinMaxValueSet.getValues() can return up to size or 2x size values.
     */
    @Test
    @Ignore
    public void testSimpleTest_Scale2_128values_64target()
    {
        final int scale = 2;
        final long from = 10000000L;
        final int size = 128;
        final IntMinMaxValueSet set = new IntMinMaxValueSet(size / scale);

        for (int i = 0; i < size; i++)
        {
            set.addOrUpdateValue(from + (i * 1000L), 10);
        }

        Assert.assertEquals((from / 1000L * 1000L), set.getFirstSecond());
        Assert.assertEquals(from, set.getMinimumTime());
        Assert.assertEquals(from + (size - 1) * 1000L, set.getMaximumTime());
        Assert.assertEquals(128, set.getValueCount());

        final IntMinMaxValue[] actual = set.getValues();
        Assert.assertEquals(size / scale, actual.length);

        final IntMinMaxValue[] expected = new IntMinMaxValue[size / scale];
        for (int i = 0; i < size / scale; i++)
        {
            expected[i] = new IntMinMaxValue(10);
            expected[i].merge(new IntMinMaxValue(10));
        }
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testSimpleTest_Scale2_256values_128target()
    {
        final long from = 10000000L;
        final int size = 128;
        final IntMinMaxValueSet set = new IntMinMaxValueSet(size);

        for (int i = 0; i < size; i++)
        {
            set.addOrUpdateValue(from + (i * 1000L), 10);
            set.addOrUpdateValue(from + (i * 1000L) + 10, 12); // new value with 10 ms offset
        }

        Assert.assertEquals((from / 1000L * 1000L), set.getFirstSecond());
        Assert.assertEquals(from, set.getMinimumTime());
        Assert.assertEquals(from + (size - 1) * 1000L + 10, set.getMaximumTime());
        Assert.assertEquals(256, set.getValueCount());

        final IntMinMaxValue[] actual = set.getValues();
        Assert.assertEquals(size, actual.length);

        final IntMinMaxValue[] expected = new IntMinMaxValue[size];
        for (int i = 0; i < size; i++)
        {
            expected[i] = new IntMinMaxValue(10);
            expected[i].merge(new IntMinMaxValue(12));
        }
        Assert.assertArrayEquals(expected, actual);
    }

    /**
     * TODO: Ignored until we decide whether MinMaxValueSet.getValues() can return up to size or 2x size values.
     */
    @Test
    @Ignore
    public void testSimpleTest_CauseCompress()
    {
        final long from = 10000000L;
        final int size = 128;
        final IntMinMaxValueSet set = new IntMinMaxValueSet(size);

        for (int i = 0; i < size; i++)
        {
            set.addOrUpdateValue(from + (i * 1000L), 10);
        }
        for (int i = size; i < 2 * size; i++)
        {
            set.addOrUpdateValue(from + (i * 1000L) + 10, 30);
        }

        Assert.assertEquals((from / 1000L * 1000L), set.getFirstSecond());
        Assert.assertEquals(from, set.getMinimumTime());
        Assert.assertEquals(from + (2 * size - 1) * 1000L + 10, set.getMaximumTime());
        Assert.assertEquals(256, set.getValueCount());

        final IntMinMaxValue[] actual = set.getValues();
        Assert.assertEquals(size, actual.length);

        final IntMinMaxValue[] expected = new IntMinMaxValue[size];
        for (int i = 0; i < size / 2; i++)
        {
            expected[i] = new IntMinMaxValue(10);
            expected[i].merge(new IntMinMaxValue(10));
        }
        for (int i = size / 2; i < size; i++)
        {
            expected[i] = new IntMinMaxValue(30);
            expected[i].merge(new IntMinMaxValue(30));
        }
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testMergeNullOrEmpty()
    {
        final IntMinMaxValueSet set = new IntMinMaxValueSet(100);
        set.addOrUpdateValue(10000L, 50);

        // merge null
        set.merge(null);
        Assert.assertEquals(1, set.getValueCount());

        // merge empty
        set.merge(new IntMinMaxValueSet(100));
        Assert.assertEquals(1, set.getValueCount());

        // merge into empty
        final IntMinMaxValueSet empty = new IntMinMaxValueSet(100);
        empty.merge(set);
        Assert.assertEquals(1, empty.getValueCount());
        Assert.assertEquals(set.getFirstSecond(), empty.getFirstSecond());
        Assert.assertEquals(set.getMinimumTime(), empty.getMinimumTime());
        Assert.assertEquals(set.getMaximumTime(), empty.getMaximumTime());
        Assert.assertEquals(set.getScale(), empty.getScale());
        Assert.assertArrayEquals(set.getValues(), empty.getValues());
    }

    @Test
    public void testMergeSameScale()
    {
        final int size = 128;
        final IntMinMaxValueSet sequential = new IntMinMaxValueSet(size);
        final IntMinMaxValueSet set1 = new IntMinMaxValueSet(size);
        final IntMinMaxValueSet set2 = new IntMinMaxValueSet(size);

        final long base = 10000000L;
        // set1: 0 to 40 seconds
        for (int i = 0; i < 40; i++)
        {
            sequential.addOrUpdateValue(base + i * 1000L, 10 + i);
            set1.addOrUpdateValue(base + i * 1000L, 10 + i);
        }
        // set2: 20 to 60 seconds with sparse gaps (step by 2) so both non-null and null slots in other are exercised
        for (int i = 20; i < 60; i += 2)
        {
            sequential.addOrUpdateValue(base + i * 1000L, 20 + i);
            set2.addOrUpdateValue(base + i * 1000L, 20 + i);
        }

        set1.merge(set2);

        Assert.assertEquals(sequential.getValueCount(), set1.getValueCount());
        Assert.assertEquals(sequential.getFirstSecond(), set1.getFirstSecond());
        Assert.assertEquals(sequential.getMinimumTime(), set1.getMinimumTime());
        Assert.assertEquals(sequential.getMaximumTime(), set1.getMaximumTime());
        Assert.assertEquals(sequential.getScale(), set1.getScale());
        Assert.assertArrayEquals(sequential.getValues(), set1.getValues());
    }

    @Test
    public void testMergeDifferentScale()
    {
        final int size = 64;
        final IntMinMaxValueSet setA = new IntMinMaxValueSet(size);
        final IntMinMaxValueSet setB = new IntMinMaxValueSet(size);

        final long base = 10000000L;
        // setA: spread over 200 seconds -> forces scale up
        for (int i = 0; i < 200; i += 2)
        {
            setA.addOrUpdateValue(base + i * 1000L, 50);
        }
        Assert.assertTrue(setA.getScale() > 1);

        // setB: only 10 seconds -> scale is 1
        for (int i = 0; i < 10; i++)
        {
            setB.addOrUpdateValue(base + i * 1000L, 30);
        }
        Assert.assertEquals(1, setB.getScale());

        final long countA = setA.getValueCount();
        final long countB = setB.getValueCount();

        // merge B into A
        setA.merge(setB);
        Assert.assertEquals(countA + countB, setA.getValueCount());
        Assert.assertEquals(base, setA.getMinimumTime());
        Assert.assertEquals(base + 198 * 1000L, setA.getMaximumTime());

        // Also test merging coarse setA into fine-scale setC (triggers this.scale < other.scale loop)
        final IntMinMaxValueSet setC = new IntMinMaxValueSet(size);
        final IntMinMaxValueSet expectedC = new IntMinMaxValueSet(size);
        for (int i = 1; i < 20; i++)
        {
            setC.addOrUpdateValue(base + i * 1000L, 25 + i);
            expectedC.addOrUpdateValue(base + i * 1000L, 25 + i);
        }
        for (int i = 0; i < 200; i += 2)
        {
            expectedC.addOrUpdateValue(base + i * 1000L, 50);
        }
        for (int i = 0; i < 10; i++)
        {
            expectedC.addOrUpdateValue(base + i * 1000L, 30);
        }
        Assert.assertEquals(1, setC.getScale());
        setC.merge(setA);
        Assert.assertEquals(setA.getScale(), setC.getScale());
        Assert.assertEquals(countA + countB + 19, setC.getValueCount());
        Assert.assertEquals(expectedC.getFirstSecond(), setC.getFirstSecond());
        Assert.assertArrayEquals(expectedC.getValues(), setC.getValues());
    }

    @Test
    public void testEmptySetBehaviorAndExceptions()
    {
        final IntMinMaxValueSet empty = new IntMinMaxValueSet(64);
        Assert.assertEquals(0L, empty.getValueCount());
        Assert.assertEquals(0, empty.getValues().length);

        try
        {
            empty.getFirstSecond();
            Assert.fail("Expected IllegalStateException");
        }
        catch (final IllegalStateException expected)
        {
            // expected
        }

        try
        {
            empty.getMinimumTime();
            Assert.fail("Expected IllegalStateException");
        }
        catch (final IllegalStateException expected)
        {
            // expected
        }

        try
        {
            empty.getMaximumTime();
            Assert.fail("Expected IllegalStateException");
        }
        catch (final IllegalStateException expected)
        {
            // expected
        }
    }

    @Test
    public void testSparseMultiPageBitSetScalingAndNegativeValues()
    {
        // 1. Negative values are recorded in min/max/sum/count but ignored in the low-precision bitset; 0 IS recorded
        final IntMinMaxValueSet negSet = new IntMinMaxValueSet(64);
        negSet.addOrUpdateValue(10_000L, -5);
        negSet.addOrUpdateValue(10_000L, 0);
        negSet.addOrUpdateValue(10_000L, 12);
        final IntMinMaxValue negSlot = negSet.getValues()[0];
        Assert.assertEquals(-5, negSlot.getMinimumValue());
        Assert.assertEquals(12, negSlot.getMaximumValue());
        Assert.assertEquals(7L, negSlot.getAccumulatedValue());
        Assert.assertEquals(3, negSlot.getValueCount());
        Assert.assertArrayEquals(new double[] { 0.0, 12.0 }, negSlot.getValues(), 0.0);

        // 2. Adding a second value to a slot that ALREADY has bitScale > 0 (verifies value >>= sc)
        // and Page.mergeFrom across all 4 words (w0, w1, w2, w3) when dstScale < srcScale and srcScale < dstScale
        final IntMinMaxValueSet setSmallFirst = new IntMinMaxValueSet(64);
        setSmallFirst.addOrUpdateValue(0L, 10);       // slot 0: bitScale = 0
        setSmallFirst.addOrUpdateValue(1_000L, 1024); // slot 1: bitScale = 3 (1024 >> 3 = 128, w2)
        setSmallFirst.addOrUpdateValue(1_000L, 16);   // slot 1 already has bitScale = 3 -> 16 >> 3 = 2 (w0)
        setSmallFirst.addOrUpdateValue(1_000L, 600);  // 600 >> 3 = 75 (w1)
        setSmallFirst.addOrUpdateValue(1_000L, 1600); // 1600 >> 3 = 200 (w3)
        setSmallFirst.addOrUpdateValue(2_000L, 2048); // slot 2: bitScale = 4 (2048 >> 4 = 128)
        setSmallFirst.addOrUpdateValue(3_000L, 8);    // slot 3: bitScale = 0, w0
        setSmallFirst.addOrUpdateValue(3_000L, 80);   // slot 3: w1
        setSmallFirst.addOrUpdateValue(3_000L, 150);  // slot 3: w2
        setSmallFirst.addOrUpdateValue(3_000L, 220);  // slot 3: w3
        // Trigger shrink() so (slot 0, slot 1) merges with dstScale < srcScale across all 4 words
        // and (slot 2, slot 3) merges with srcScale < dstScale across all 4 words
        setSmallFirst.addOrUpdateValue(200_000L, 50);

        final IntMinMaxValue expectedSlot1 = new IntMinMaxValue(1024);
        expectedSlot1.updateValue(16);
        expectedSlot1.updateValue(600);
        expectedSlot1.updateValue(1600);
        final IntMinMaxValue expectedPair01 = new IntMinMaxValue(10).merge(expectedSlot1);

        final IntMinMaxValue expectedSlot3 = new IntMinMaxValue(8);
        expectedSlot3.updateValue(80);
        expectedSlot3.updateValue(150);
        expectedSlot3.updateValue(220);
        final IntMinMaxValue expectedPair23 = new IntMinMaxValue(2048).merge(expectedSlot3);

        final IntMinMaxValue[] shrunkVals = setSmallFirst.getValues();
        Assert.assertEquals(expectedPair01, shrunkVals[0]);
        Assert.assertArrayEquals(expectedPair01.getValues(), shrunkVals[0].getValues(), 0.0);
        Assert.assertEquals(expectedPair23, shrunkVals[1]);
        Assert.assertArrayEquals(expectedPair23.getValues(), shrunkVals[1].getValues(), 0.0);

        // 3. Backward insertion when scale > 1 (scale = 2, scale2 = 1) that shifts without further shrinking
        setSmallFirst.addOrUpdateValue(-4_000L, 77);
        Assert.assertEquals(-4_000L, setSmallFirst.getFirstSecond());
        Assert.assertEquals(new IntMinMaxValue(77), setSmallFirst.getValues()[0]);
        Assert.assertEquals(expectedPair01, setSmallFirst.getValues()[2]);

        // 4. Merge coarse small-capacity set (scale = 2, narrow time span) into fine large-capacity set (scale = 1)
        // where resolveSlotIndex alone would NOT shrink the target set
        final IntMinMaxValueSet smallCoarse = new IntMinMaxValueSet(2); // backing = 4 slots
        smallCoarse.addOrUpdateValue(10_000L, 5);
        smallCoarse.addOrUpdateValue(14_000L, 6); // span = 4 >= 4 -> shrinks smallCoarse to scale = 2
        Assert.assertEquals(2, smallCoarse.getScale());

        final IntMinMaxValueSet largeFine = new IntMinMaxValueSet(64); // backing = 128 slots, scale = 1
        largeFine.addOrUpdateValue(11_000L, 100); // firstSecond = 11 (odd)
        largeFine.addOrUpdateValue(21_000L, 200); // lastSecond = 21 (odd, > smallCoarse.lastSecond so only masked in merge loop)
        largeFine.merge(smallCoarse);
        Assert.assertEquals(2, largeFine.getScale());
        Assert.assertEquals(10_000L, largeFine.getFirstSecond());
        // Slots for [10..11], [12..13], [14..15], [16..17], [18..19], [20..21] -> 6 slots
        Assert.assertEquals(6, largeFine.getValues().length);
        Assert.assertEquals(new IntMinMaxValue(100).merge(new IntMinMaxValue(5)), largeFine.getValues()[0]);
        Assert.assertEquals(new IntMinMaxValue(200), largeFine.getValues()[5]);

        // 5. Multi-page sparse set where page 1 (slots 64..127) remains null while page 0 and page 2 are populated
        final IntMinMaxValueSet sparseA = new IntMinMaxValueSet(100); // backing = 200 slots (4 pages)
        sparseA.addOrUpdateValue(10_000L, 100);          // slot 0 (page 0)
        sparseA.addOrUpdateValue(160_000L, 200);         // slot 150 (page 2), leaving page 1 null
        final IntMinMaxValue[] sparseVals = sparseA.getValues();
        Assert.assertEquals(151, sparseVals.length);
        Assert.assertNotNull(sparseVals[0]);
        Assert.assertNull(sparseVals[70]); // inside null page 1
        Assert.assertNotNull(sparseVals[150]);

        final IntMinMaxValueSet sparseB = new IntMinMaxValueSet(100);
        sparseB.merge(sparseA);
        Assert.assertArrayEquals(sparseVals, sparseB.getValues());
    }
}
