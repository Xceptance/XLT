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
 * Tests the {@link DoubleMinMaxValueSet} and {@link DoubleMinMaxValue} classes.
 */
public class DoubleMinMaxValueSetTest
{
    private static final double EPSILON = 1e-4;

    @Test
    public void testDoubleMinMaxValueBasicsAndMerge()
    {
        final DoubleMinMaxValue v1 = new DoubleMinMaxValue(10.0);
        v1.updateValue(20.0);
        Assert.assertEquals(15.0, v1.getValue(), EPSILON);
        Assert.assertEquals(10.0, v1.getMinimumValue(), EPSILON);
        Assert.assertEquals(20.0, v1.getMaximumValue(), EPSILON);
        Assert.assertEquals(30.0, v1.getAccumulatedValue(), EPSILON);
        Assert.assertEquals(2, v1.getValueCount());

        final DoubleMinMaxValue copy = new DoubleMinMaxValue(v1);
        Assert.assertEquals(v1, copy);

        final DoubleMinMaxValue v2 = new DoubleMinMaxValue(5.0);
        v2.updateValue(30.0);

        v1.merge(v2);
        Assert.assertEquals(5.0, v1.getMinimumValue(), EPSILON);
        Assert.assertEquals(30.0, v1.getMaximumValue(), EPSILON);
        Assert.assertEquals(65.0, v1.getAccumulatedValue(), EPSILON);
        Assert.assertEquals(4, v1.getValueCount());
    }

    @Test
    public void testMergeNullOrEmpty()
    {
        final DoubleMinMaxValueSet set = new DoubleMinMaxValueSet(100);
        set.addOrUpdateValue(10000L, 50.0);

        // merge null
        set.merge(null);
        Assert.assertEquals(1, set.getValueCount());

        // merge empty
        set.merge(new DoubleMinMaxValueSet(100));
        Assert.assertEquals(1, set.getValueCount());

        // merge into empty
        final DoubleMinMaxValueSet empty = new DoubleMinMaxValueSet(100);
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
        final DoubleMinMaxValueSet sequential = new DoubleMinMaxValueSet(size);
        final DoubleMinMaxValueSet set1 = new DoubleMinMaxValueSet(size);
        final DoubleMinMaxValueSet set2 = new DoubleMinMaxValueSet(size);

        final long base = 10000000L;
        // set1: 0 to 40 seconds
        for (int i = 0; i < 40; i++)
        {
            sequential.addOrUpdateValue(base + i * 1000L, 10.0 + i);
            set1.addOrUpdateValue(base + i * 1000L, 10.0 + i);
        }
        // set2: 20 to 60 seconds with sparse gaps (step by 2) so both non-null and null slots in other are exercised
        for (int i = 20; i < 60; i += 2)
        {
            sequential.addOrUpdateValue(base + i * 1000L, 20.0 + i);
            set2.addOrUpdateValue(base + i * 1000L, 20.0 + i);
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
        final DoubleMinMaxValueSet setA = new DoubleMinMaxValueSet(size);
        final DoubleMinMaxValueSet setB = new DoubleMinMaxValueSet(size);

        final long base = 10000000L;
        // setA: spread over 200 seconds -> forces scale up
        for (int i = 0; i < 200; i += 2)
        {
            setA.addOrUpdateValue(base + i * 1000L, 50.0);
        }
        Assert.assertTrue(setA.getScale() > 1);

        // setB: only 10 seconds -> scale is 1
        for (int i = 0; i < 10; i++)
        {
            setB.addOrUpdateValue(base + i * 1000L, 30.0);
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
        final DoubleMinMaxValueSet setC = new DoubleMinMaxValueSet(size);
        final DoubleMinMaxValueSet expectedC = new DoubleMinMaxValueSet(size);
        for (int i = 1; i < 20; i++)
        {
            setC.addOrUpdateValue(base + i * 1000L, 25.0 + i);
            expectedC.addOrUpdateValue(base + i * 1000L, 25.0 + i);
        }
        // Populate expectedC with the same values that are in setA (200s + 10s) without calling merge()
        for (int i = 0; i < 200; i += 2)
        {
            expectedC.addOrUpdateValue(base + i * 1000L, 50.0);
        }
        for (int i = 0; i < 10; i++)
        {
            expectedC.addOrUpdateValue(base + i * 1000L, 30.0);
        }
        Assert.assertEquals(1, setC.getScale());
        setC.merge(setA);
        Assert.assertEquals(setA.getScale(), setC.getScale());
        Assert.assertEquals(countA + countB + 19, setC.getValueCount());
        Assert.assertEquals(expectedC.getFirstSecond(), setC.getFirstSecond());
        Assert.assertArrayEquals(expectedC.getValues(), setC.getValues());
    }

    @Test
    public void testDoubleMinMaxValueBitSetShiftScaleAndEquals()
    {
        final double w = AbstractFixedSizeDoubleValueSet.INITIAL_BUCKET_WIDTH;

        // 1. Shift by < 64 buckets (10), == 64 buckets, and > 64 buckets (70) without scaling
        final DoubleMinMaxValue vShiftSmall = new DoubleMinMaxValue(100.0 * w);
        vShiftSmall.updateValue(90.0 * w); // shifts by 10 buckets (< 64)
        vShiftSmall.updateValue(20.0 * w); // shifts by another 70 buckets (> 64), pushing first value to bucket 80 (bits1)
        final double[] vals = vShiftSmall.getValues();
        Assert.assertEquals(3, vals.length);
        Assert.assertEquals(20.0 * w, vals[0], 1e-18);
        Assert.assertEquals(90.0 * w, vals[1], 1e-18);
        Assert.assertEquals(100.0 * w, vals[2], 1e-18);

        // Shift by exact 64 buckets boundary (buckets == 64)
        final DoubleMinMaxValue vShift64 = new DoubleMinMaxValue(64.0 * w);
        vShift64.updateValue(0.0); // diff = 0 -> shifts by 64 buckets, moving bucket 0 to bucket 64 (bit 0 of bits1)
        final double[] vals64 = vShift64.getValues();
        Assert.assertEquals(2, vals64.length);
        Assert.assertEquals(0.0, vals64[0], 1e-18);
        Assert.assertEquals(64.0 * w, vals64[1], 1e-18);

        // Add value at exact bucket 63 (bits0 MSB) and bucket 64 (bits1 LSB) without shifting
        vShift64.updateValue(63.0 * w);
        Assert.assertEquals(3, vShift64.getValues().length);

        // 2. Scale up by adding value at exact upper boundary (rangeMin + 128 * w) and exact lower boundary (rangeMax - 128 * bucketWidth)
        final DoubleMinMaxValue vScale = new DoubleMinMaxValue(64.0 * w); // rangeMin = 0.0, rangeMax = 128 * w
        vScale.updateValue(0.0); // bucket 0 (bits0)
        vScale.updateValue(100.0 * w); // bucket 100 (bits1)
        vScale.updateValue(128.0 * w); // exact boundary value == rangeMin + 128 * w -> triggers 1x scaleBitSet (bucketWidth = 2*w)
        final double[] scaledVals = vScale.getValues();
        Assert.assertEquals(4, scaledVals.length);
        Assert.assertEquals(0.0, scaledVals[0], 1e-18);
        Assert.assertEquals(64.0 * w, scaledVals[1], 1e-18);
        Assert.assertEquals(100.0 * w, scaledVals[2], 1e-18);
        Assert.assertEquals(128.0 * w, scaledVals[3], 1e-18);

        // Trigger lower-bound scale in adjustRange: value < rangeMax - 128 * bucketWidth
        vScale.updateValue(-200.0 * w);
        Assert.assertEquals(5, vScale.getValues().length);

        // 3. Merge null and merge another value with bits in both bits0 and bits1, verifying getValues()
        Assert.assertSame(vShift64, vShift64.merge(null));
        final DoubleMinMaxValue mergeTarget = new DoubleMinMaxValue(64.0 * w); // rangeMin = 0
        final DoubleMinMaxValue mergeSource = new DoubleMinMaxValue(64.0 * w); // rangeMin = 0
        mergeSource.updateValue(10.0 * w); // bits0
        mergeSource.updateValue(80.0 * w); // bits1
        mergeTarget.merge(mergeSource);
        Assert.assertArrayEquals(mergeSource.getValues(), mergeTarget.getValues(), 1e-18);

        // 4. toString, hashCode, equals across all fields
        final DoubleMinMaxValue strVal = new DoubleMinMaxValue(10.0);
        strVal.updateValue(20.0);
        Assert.assertEquals("15.0/30.0/10.0/20.0/2", strVal.toString());
        Assert.assertNotEquals(0, strVal.hashCode());

        Assert.assertTrue(strVal.equals(strVal));
        Assert.assertFalse(strVal.equals(null));
        Assert.assertFalse(strVal.equals("not a DoubleMinMaxValue"));

        final DoubleMinMaxValue strValCopy = new DoubleMinMaxValue(strVal);
        Assert.assertEquals(strVal, strValCopy);
        Assert.assertEquals(strVal.hashCode(), strValCopy.hashCode());

        // Differ in accumulatedValue only
        final DoubleMinMaxValue base = new DoubleMinMaxValue(10.0);
        base.updateValue(20.0);
        base.updateValue(30.0);
        final DoubleMinMaxValue diffSum = new DoubleMinMaxValue(10.0);
        diffSum.updateValue(25.0);
        diffSum.updateValue(30.0);
        Assert.assertNotEquals(base, diffSum);

        // Differ in maximum (same accumulatedValue = 60.0)
        final DoubleMinMaxValue diffMax = new DoubleMinMaxValue(10.0);
        diffMax.updateValue(15.0);
        diffMax.updateValue(35.0);
        Assert.assertNotEquals(base, diffMax);

        // Differ in minimum
        final DoubleMinMaxValue diffMin = new DoubleMinMaxValue(5.0);
        diffMin.updateValue(25.0);
        diffMin.updateValue(30.0);
        Assert.assertNotEquals(base, diffMin);

        // Differ in valueCount
        final DoubleMinMaxValue countA = new DoubleMinMaxValue(0.0);
        countA.updateValue(10.0);
        final DoubleMinMaxValue countB = new DoubleMinMaxValue(0.0);
        countB.updateValue(0.0);
        countB.updateValue(10.0);
        Assert.assertNotEquals(countA, countB);

        // Differ in rangeMin (same min=0.0, max=10.0, sum=10.1, count=3, but starting at 0.1 shifts rangeMin to -0.025)
        final DoubleMinMaxValue rangeOrderA = new DoubleMinMaxValue(0.0);
        rangeOrderA.updateValue(10.0);
        rangeOrderA.updateValue(0.1);
        final DoubleMinMaxValue rangeOrderB = new DoubleMinMaxValue(0.1);
        rangeOrderB.updateValue(10.0);
        rangeOrderB.updateValue(0.0);
        Assert.assertNotEquals(rangeOrderA, rangeOrderB);

        // Differ in bits0 (same min=0, max=100, sum=200, count=4, rangeMin, but different lower-half bucket in bits0)
        final DoubleMinMaxValue bits0A = new DoubleMinMaxValue(0.0);
        bits0A.updateValue(100.0);
        bits0A.updateValue(10.0);
        bits0A.updateValue(90.0);
        final DoubleMinMaxValue bits0B = new DoubleMinMaxValue(0.0);
        bits0B.updateValue(100.0);
        bits0B.updateValue(20.0);
        bits0B.updateValue(80.0);
        Assert.assertNotEquals(bits0A, bits0B);

        // Differ in bits1 only (same min=0, max=100, sum=260, count=4, rangeMin, same bits0, different upper-half buckets >= 64 in bits1)
        final DoubleMinMaxValue bits1A = new DoubleMinMaxValue(0.0);
        bits1A.updateValue(100.0);
        bits1A.updateValue(66.0);
        bits1A.updateValue(94.0);
        final DoubleMinMaxValue bits1B = new DoubleMinMaxValue(0.0);
        bits1B.updateValue(100.0);
        bits1B.updateValue(70.0);
        bits1B.updateValue(90.0);
        Assert.assertNotEquals(bits1A, bits1B);
    }

    @Test
    public void testDoubleMinMaxValueSetShrinkingShiftingAndExceptions()
    {
        // Default constructor and getSize
        final DoubleMinMaxValueSet defaultSet = new DoubleMinMaxValueSet();
        Assert.assertEquals(DoubleMinMaxValueSet.DEFAULT_SIZE, defaultSet.getSize());
        Assert.assertEquals(0L, defaultSet.getValueCount());
        Assert.assertEquals(0, defaultSet.getValues().length);

        try
        {
            new DoubleMinMaxValueSet(0);
            Assert.fail("Expected IllegalArgumentException");
        }
        catch (final IllegalArgumentException expected)
        {
            // expected
        }

        try
        {
            defaultSet.getFirstSecond();
            Assert.fail("Expected IllegalStateException");
        }
        catch (final IllegalStateException expected)
        {
            // expected
        }

        try
        {
            defaultSet.getMinimumTime();
            Assert.fail("Expected IllegalStateException");
        }
        catch (final IllegalStateException expected)
        {
            // expected
        }

        try
        {
            defaultSet.getMaximumTime();
            Assert.fail("Expected IllegalStateException");
        }
        catch (final IllegalStateException expected)
        {
            // expected
        }

        // Verify forward and backward insertion across scales 1, 2, 4, 8 with exact getValues() and getFirstSecond() checks
        final long from = 123456789000L;
        for (final int scale : new int[] { 1, 2, 4, 8 })
        {
            final int size = 100 * scale;
            final DoubleMinMaxValueSet forwardSet = new DoubleMinMaxValueSet(64);
            final DoubleMinMaxValueSet backwardSet = new DoubleMinMaxValueSet(64);

            for (int i = 0; i < size; i++)
            {
                forwardSet.addOrUpdateValue(from + i * 1000L, i * 1.5);
                backwardSet.addOrUpdateValue(from + (size - 1 - i) * 1000L, (size - 1 - i) * 1.5);
            }

            for (final DoubleMinMaxValueSet set : new DoubleMinMaxValueSet[] { forwardSet, backwardSet })
            {
                Assert.assertEquals(scale, set.getScale());
                Assert.assertEquals(from, set.getMinimumTime());
                Assert.assertEquals(from + (size - 1) * 1000L, set.getMaximumTime());
                Assert.assertEquals(size, set.getValueCount());

                final int firstSec = (int) (from / 1000L);
                final int lastSec = (int) ((from + (size - 1) * 1000L) / 1000L);
                final int offset = (firstSec % scale == 0) ? 0 : 1;
                final int expectedSlots = (lastSec - firstSec) / scale + 1 + offset;
                Assert.assertEquals((firstSec / scale * scale) * 1000L, set.getFirstSecond());

                final DoubleMinMaxValue[] actual = set.getValues();
                Assert.assertEquals(expectedSlots, actual.length);

                final DoubleMinMaxValue[] expected = new DoubleMinMaxValue[expectedSlots];
                int index = (firstSec % scale == 0) ? -1 : 0;
                for (int i = 0; i < size; i++)
                {
                    if ((firstSec + i) % scale == 0)
                    {
                        index++;
                    }
                    if (expected[index] == null)
                    {
                        expected[index] = new DoubleMinMaxValue(i * 1.5);
                    }
                    else
                    {
                        expected[index].merge(new DoubleMinMaxValue(i * 1.5));
                    }
                }
                Assert.assertArrayEquals(expected, actual);
            }
        }
    }
}
