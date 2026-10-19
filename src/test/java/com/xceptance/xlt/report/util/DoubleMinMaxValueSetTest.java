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

        // 1. Shift by < 64 buckets (10) while bits0 has a high bit (60) that crosses into bits1 AND bits1 is already non-zero
        final DoubleMinMaxValue vShiftSmall = new DoubleMinMaxValue(20.0 * w);
        vShiftSmall.updateValue(80.0 * w); // bucket 60 in bits0 (crosses into bit 6 of bits1 when shifted by 10)
        vShiftSmall.updateValue(90.0 * w); // bucket 70 in bits1 (moves to bit 16 of bits1 when shifted by 10)
        vShiftSmall.updateValue(10.0 * w); // shifts by 10 buckets (< 64)
        final double[] vals = vShiftSmall.getValues();
        Assert.assertEquals(4, vals.length);
        Assert.assertEquals(10.0 * w, vals[0], 1e-18);
        Assert.assertEquals(20.0 * w, vals[1], 1e-18);
        Assert.assertEquals(80.0 * w, vals[2], 1e-18);
        Assert.assertEquals(90.0 * w, vals[3], 1e-18);

        // Shift by > 64 buckets (70)
        final DoubleMinMaxValue vShift70 = new DoubleMinMaxValue(90.0 * w);
        vShift70.updateValue(20.0 * w);
        Assert.assertArrayEquals(new double[] { 20.0 * w, 90.0 * w }, vShift70.getValues(), 1e-18);

        // Shift by exact 64 buckets boundary (buckets == 64) when multiple bits in bits0 (bit 0 and bit 1) are set
        final DoubleMinMaxValue vShift64 = new DoubleMinMaxValue(64.0 * w);
        vShift64.updateValue(65.0 * w); // sets bit 1 of bits0 so failing to clear bits0 on 64-bit shift would leave bit 1 set
        vShift64.updateValue(0.0);      // shifts by 64 buckets, moving bits 0,1 of bits0 to bits 0,1 of bits1 and clearing bits0
        final double[] vals64 = vShift64.getValues();
        Assert.assertEquals(3, vals64.length);
        Assert.assertEquals(0.0, vals64[0], 1e-18);
        Assert.assertEquals(64.0 * w, vals64[1], 1e-18);
        Assert.assertEquals(65.0 * w, vals64[2], 1e-18);

        // Add value at exact bucket 63 (bits0 MSB)
        vShift64.updateValue(63.0 * w);
        Assert.assertEquals(4, vShift64.getValues().length);

        // 2. Verify initial rangeMax = value + w (adding -127.5 * w after 0.0 must scale to 2*w so 0.0 stays at 0.0)
        final DoubleMinMaxValue vInitRangeMax = new DoubleMinMaxValue(0.0);
        vInitRangeMax.updateValue(-127.5 * w); // < rangeMax - 128 * w (-127 * w) -> scales to 2*w and preserves 0.0
        Assert.assertArrayEquals(new double[] { -128.0 * w, 0.0 }, vInitRangeMax.getValues(), 1e-18);

        // Verify updated rangeMax = rangeMin + width * (index + 1)
        final DoubleMinMaxValue vUpdRangeMax = new DoubleMinMaxValue(0.0);
        vUpdRangeMax.updateValue(64.0 * w);  // index = 64 -> rangeMax = 65 * w
        vUpdRangeMax.updateValue(-63.5 * w); // < 65 * w - 128 * w (-63 * w) -> scales to 2*w and preserves all 3 values
        Assert.assertEquals(3, vUpdRangeMax.getValues().length);

        // Scale up at exact upper boundary (value == rangeMin + 128 * w)
        final DoubleMinMaxValue vScale = new DoubleMinMaxValue(0.0);
        vScale.updateValue(127.0 * w); // fits without scaling (index = 127 < 128)
        Assert.assertArrayEquals(new double[] { 0.0, 127.0 * w }, vScale.getValues(), 1e-18);
        vScale.updateValue(128.0 * w); // exact upper boundary -> triggers 1x scaleBitSet (bucketWidth = 2*w)
        Assert.assertArrayEquals(new double[] { 0.0, 126.0 * w, 128.0 * w }, vScale.getValues(), 1e-18);

        // Exercise the exact same bitset boundary conditions through DoubleMinMaxValueSet$Page
        final DoubleMinMaxValueSet pageSet = new DoubleMinMaxValueSet(16);
        pageSet.addOrUpdateValue(1_000L, 64.0 * w);
        pageSet.addOrUpdateValue(1_000L, 65.0 * w);
        pageSet.addOrUpdateValue(1_000L, 0.0); // buckets == 64 shift in Page.addBitValue
        Assert.assertArrayEquals(vals64, pageSet.getValues()[0].getValues(), 1e-18);

        pageSet.addOrUpdateValue(2_000L, 0.0);
        pageSet.addOrUpdateValue(2_000L, -127.5 * w); // initial rangeMax = value + width in Page.addOrUpdate
        Assert.assertArrayEquals(new double[] { -128.0 * w, 0.0 }, pageSet.getValues()[1].getValues(), 1e-18);

        pageSet.addOrUpdateValue(3_000L, 0.0);
        pageSet.addOrUpdateValue(3_000L, 127.0 * w);
        Assert.assertArrayEquals(new double[] { 0.0, 127.0 * w }, pageSet.getValues()[2].getValues(), 1e-18);
        pageSet.addOrUpdateValue(3_000L, 128.0 * w); // exact upper boundary in Page.addBitValue
        Assert.assertArrayEquals(new double[] { 0.0, 126.0 * w, 128.0 * w }, pageSet.getValues()[2].getValues(), 1e-18);

        // 3. Merge null and merge another value with bits in both bits0 and bits1, verifying getValues()
        Assert.assertSame(vShift64, vShift64.merge(null));
        final DoubleMinMaxValue mergeTarget = new DoubleMinMaxValue(64.0 * w);
        final DoubleMinMaxValue mergeSource = new DoubleMinMaxValue(64.0 * w);
        mergeSource.updateValue(10.0 * w); // bits0
        mergeSource.updateValue(80.0 * w); // bits1
        mergeTarget.merge(mergeSource);
        Assert.assertArrayEquals(mergeSource.getValues(), mergeTarget.getValues(), 1e-18);

        // 4. toString, hashCode, equals across all fields (varying one field at a time)
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

        final DoubleMinMaxValue rawBase = new DoubleMinMaxValue(60.0, 3, 10.0, 30.0, w, 10.0, 31.0, 1L, 2L);
        Assert.assertEquals(rawBase, new DoubleMinMaxValue(60.0, 3, 10.0, 30.0, w, 10.0, 31.0, 1L, 2L));
        Assert.assertNotEquals(rawBase, new DoubleMinMaxValue(65.0, 3, 10.0, 30.0, w, 10.0, 31.0, 1L, 2L));
        Assert.assertNotEquals(rawBase, new DoubleMinMaxValue(60.0, 4, 10.0, 30.0, w, 10.0, 31.0, 1L, 2L));
        Assert.assertNotEquals(rawBase, new DoubleMinMaxValue(60.0, 3, 5.0, 30.0, w, 10.0, 31.0, 1L, 2L));
        Assert.assertNotEquals(rawBase, new DoubleMinMaxValue(60.0, 3, 10.0, 35.0, w, 10.0, 31.0, 1L, 2L));
        Assert.assertNotEquals(rawBase, new DoubleMinMaxValue(60.0, 3, 10.0, 30.0, w, 12.0, 31.0, 1L, 2L));
        Assert.assertNotEquals(rawBase, new DoubleMinMaxValue(60.0, 3, 10.0, 30.0, w, 10.0, 31.0, 3L, 2L));
        Assert.assertNotEquals(rawBase, new DoubleMinMaxValue(60.0, 3, 10.0, 30.0, w, 10.0, 31.0, 1L, 4L));
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

        // Verify sparse multi-page behavior (null intermediate pages) and shrink() where only the second slot in a pair is occupied
        final DoubleMinMaxValueSet sparse = new DoubleMinMaxValueSet(100); // backing = 200 slots (4 pages)
        sparse.addOrUpdateValue(10_000L, 1.0);   // second = 10 -> slot 0 (page 0)
        sparse.addOrUpdateValue(13_000L, 2.0);   // second = 13 -> slot 3 (page 0, odd slot so pair (2,3) has occ1=false, occ2=true)
        sparse.addOrUpdateValue(160_000L, 3.0);  // second = 160 -> slot 150 (page 2, leaving page 1 null)

        final DoubleMinMaxValueSet sparseCopy = new DoubleMinMaxValueSet(100);
        sparseCopy.merge(sparse);
        Assert.assertArrayEquals(sparse.getValues(), sparseCopy.getValues());

        // Trigger shift() on sparse set (which has unoccupied intermediate slots) before shrinking
        sparse.addOrUpdateValue(8_000L, 0.5);    // second = 8 -> shifts existing sparse slots right by 2
        Assert.assertEquals(8_000L, sparse.getFirstSecond());
        Assert.assertEquals(new DoubleMinMaxValue(0.5), sparse.getValues()[0]);
        Assert.assertNull(sparse.getValues()[1]);
        Assert.assertEquals(new DoubleMinMaxValue(1.0), sparse.getValues()[2]);

        // Trigger shrink() on sparse set (scale 1 -> 2): pairs (0,1), (2,3), (4,5) where slot 4 is empty and slot 5 is occupied
        sparse.addOrUpdateValue(250_000L, 4.0);
        Assert.assertEquals(2, sparse.getScale());
        final DoubleMinMaxValue[] shrunkSparse = sparse.getValues();
        Assert.assertEquals(new DoubleMinMaxValue(0.5), shrunkSparse[0]);
        Assert.assertEquals(new DoubleMinMaxValue(1.0), shrunkSparse[1]);
        Assert.assertEquals(new DoubleMinMaxValue(2.0), shrunkSparse[2]);

        // Also test shrink() when offset == 1 and the trailing slot (size - 1) is unoccupied
        final DoubleMinMaxValueSet oddStart = new DoubleMinMaxValueSet(64); // backing = 128
        oddStart.addOrUpdateValue(1_000L, 10.0);   // firstSecond = 1 (odd -> offset = 1 when shrinking to scale 2)
        oddStart.addOrUpdateValue(200_000L, 20.0); // triggers shrink() while slot 127 is unoccupied
        Assert.assertEquals(2, oddStart.getScale());
        Assert.assertEquals(2L, oddStart.getValueCount());

        // Merge coarse small-capacity set (scale = 2, narrow time span) into fine large-capacity set (scale = 1)
        final DoubleMinMaxValueSet smallCoarse = new DoubleMinMaxValueSet(2); // backing = 4 slots
        smallCoarse.addOrUpdateValue(10_000L, 5.0);
        smallCoarse.addOrUpdateValue(14_000L, 6.0); // span = 4 >= 4 -> shrinks smallCoarse to scale = 2
        Assert.assertEquals(2, smallCoarse.getScale());

        final DoubleMinMaxValueSet largeFine = new DoubleMinMaxValueSet(64); // backing = 128 slots, scale = 1
        largeFine.addOrUpdateValue(11_000L, 100.0); // firstSecond = 11 (odd)
        largeFine.addOrUpdateValue(21_000L, 200.0); // lastSecond = 21 (odd, > smallCoarse.lastSecond)
        largeFine.merge(smallCoarse);
        Assert.assertEquals(2, largeFine.getScale());
        Assert.assertEquals(10_000L, largeFine.getFirstSecond());
        Assert.assertEquals(6, largeFine.getValues().length);
        Assert.assertEquals(new DoubleMinMaxValue(100.0).merge(new DoubleMinMaxValue(5.0)), largeFine.getValues()[0]);
        Assert.assertEquals(new DoubleMinMaxValue(200.0), largeFine.getValues()[5]);

        // Minimum logical size = 1 (backing capacity = 2) with offset = 1 (firstSecond = 1):
        // when shrinking from scale 1 to scale 2, i = 1 and j = 1 (j == i), so slot 1 must remain intact in-place
        final DoubleMinMaxValueSet tiny = new DoubleMinMaxValueSet(1);
        tiny.addOrUpdateValue(1_000L, 10.0); // slot 0 (second = 1)
        tiny.addOrUpdateValue(2_000L, 20.0); // slot 1 (second = 2)
        tiny.addOrUpdateValue(3_000L, 30.0); // triggers shrink (offset = 1, i = j = 1) and merges into slot 1 ([2..3])
        Assert.assertEquals(3L, tiny.getValueCount());
        final DoubleMinMaxValue[] tinyVals = tiny.getValues();
        Assert.assertEquals(2, tinyVals.length);
        Assert.assertEquals(new DoubleMinMaxValue(10.0), tinyVals[0]);
        Assert.assertEquals(new DoubleMinMaxValue(20.0).merge(new DoubleMinMaxValue(30.0)), tinyVals[1]);
    }
}
