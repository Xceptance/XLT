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

public class IntMinMaxValueTest
{
    @Test
    public final void testMinMaxValue()
    {
        final IntMinMaxValue v = new IntMinMaxValue(0);
        Assert.assertEquals(0, v.getAccumulatedValue());
        Assert.assertEquals(0, v.getAverageValue());
        Assert.assertEquals(0, v.getMaximumValue());
        Assert.assertEquals(0, v.getMinimumValue());
        Assert.assertEquals(0, v.getValue());
        Assert.assertEquals(1, v.getValueCount());
    }

    @Test
    public final void testMinMaxValueInt_0()
    {
        final IntMinMaxValue v = new IntMinMaxValue(0);
        Assert.assertEquals(0, v.getAccumulatedValue());
        Assert.assertEquals(0, v.getAverageValue());
        Assert.assertEquals(0, v.getMaximumValue());
        Assert.assertEquals(0, v.getMinimumValue());
        Assert.assertEquals(0, v.getValue());
        Assert.assertEquals(1, v.getValueCount());
    }

    @Test
    public final void testMinMaxValueInt_1()
    {
        final IntMinMaxValue v = new IntMinMaxValue(1);
        Assert.assertEquals(1, v.getAccumulatedValue());
        Assert.assertEquals(1, v.getAverageValue());
        Assert.assertEquals(1, v.getMaximumValue());
        Assert.assertEquals(1, v.getMinimumValue());
        Assert.assertEquals(1, v.getValue());
        Assert.assertEquals(1, v.getValueCount());
    }

    @Test
    public final void testMinMaxValueInt_neg2()
    {
        final IntMinMaxValue v = new IntMinMaxValue(-2);
        Assert.assertEquals(-2, v.getAccumulatedValue());
        Assert.assertEquals(-2, v.getAverageValue());
        Assert.assertEquals(-2, v.getMaximumValue());
        Assert.assertEquals(-2, v.getMinimumValue());
        Assert.assertEquals(-2, v.getValue());
        Assert.assertEquals(1, v.getValueCount());
    }

    @Test
    public final void testUpdateValue()
    {
        final IntMinMaxValue v = new IntMinMaxValue(0);
        v.updateValue(2);

        Assert.assertEquals(2, v.getAccumulatedValue());
        Assert.assertEquals(1, v.getAverageValue());
        Assert.assertEquals(2, v.getMaximumValue());
        Assert.assertEquals(0, v.getMinimumValue());
        Assert.assertEquals(1, v.getValue());
        Assert.assertEquals(2, v.getValueCount());

        // next max
        v.updateValue(10);

        Assert.assertEquals(12, v.getAccumulatedValue());
        Assert.assertEquals(4, v.getAverageValue());
        Assert.assertEquals(10, v.getMaximumValue());
        Assert.assertEquals(0, v.getMinimumValue());
        Assert.assertEquals(4, v.getValue());
        Assert.assertEquals(3, v.getValueCount());

        // next min
        v.updateValue(-2);

        Assert.assertEquals(10, v.getAccumulatedValue());
        Assert.assertEquals(2, v.getAverageValue());
        Assert.assertEquals(10, v.getMaximumValue());
        Assert.assertEquals(-2, v.getMinimumValue());
        Assert.assertEquals(2, v.getValue());
        Assert.assertEquals(4, v.getValueCount());

        // next middle value
        v.updateValue(2);
        Assert.assertEquals(12, v.getAccumulatedValue());
        Assert.assertEquals(2, v.getAverageValue());
        Assert.assertEquals(10, v.getMaximumValue());
        Assert.assertEquals(-2, v.getMinimumValue());
        Assert.assertEquals(2, v.getValue());
        Assert.assertEquals(5, v.getValueCount());
    }

    @Test
    public final void testMerge()
    {
        final IntMinMaxValue v1 = new IntMinMaxValue(10);
        final IntMinMaxValue v2 = new IntMinMaxValue(20);
        final IntMinMaxValue v = new IntMinMaxValue(0);

        v.merge(v1);
        Assert.assertEquals(10, v.getAccumulatedValue());
        Assert.assertEquals(5, v.getAverageValue());
        Assert.assertEquals(10, v.getMaximumValue());
        Assert.assertEquals(0, v.getMinimumValue());
        Assert.assertEquals(5, v.getValue());
        Assert.assertEquals(2, v.getValueCount());

        v.merge(v2);
        Assert.assertEquals(30, v.getAccumulatedValue());
        Assert.assertEquals(10, v.getAverageValue());
        Assert.assertEquals(20, v.getMaximumValue());
        Assert.assertEquals(0, v.getMinimumValue());
        Assert.assertEquals(10, v.getValue());
        Assert.assertEquals(3, v.getValueCount());
    }

    @Test
    public final void testToString()
    {
        final IntMinMaxValue v = new IntMinMaxValue(88);
        Assert.assertEquals("88/88/88/88/1", v.toString());

        v.updateValue(12);
        Assert.assertEquals("50/100/12/88/2", v.toString());
    }

    @Test
    public final void testEquals()
    {
        final IntMinMaxValue v = new IntMinMaxValue(76210);
        Assert.assertFalse(v.equals(null));
        Assert.assertFalse(v.equals("Foo"));
        Assert.assertTrue(v.equals(v));
        Assert.assertTrue(v.equals(new IntMinMaxValue(76210)));
        Assert.assertEquals(v.hashCode(), new IntMinMaxValue(76210).hashCode());
        Assert.assertFalse(v.equals(new IntMinMaxValue(6210)));
    }

    @Test
    public final void testCopyAndEntryConstructors()
    {
        // Null copy constructor -> default uninitialized min/max return 0
        final IntMinMaxValue fromNull = new IntMinMaxValue((IntMinMaxValue) null);
        Assert.assertEquals(0, fromNull.getMinimumValue());
        Assert.assertEquals(0, fromNull.getMaximumValue());
        Assert.assertEquals(0, fromNull.getValueCount());
        Assert.assertEquals(0, fromNull.getValues().length);

        // Non-null copy constructor preserves all fields and bitset words
        final IntMinMaxValue original = new IntMinMaxValue(10);
        original.updateValue(70);
        original.updateValue(140);
        original.updateValue(210);
        final IntMinMaxValue copy = new IntMinMaxValue(original);
        Assert.assertEquals(original, copy);
        Assert.assertEquals(original.hashCode(), copy.hashCode());
        Assert.assertArrayEquals(original.getValues(), copy.getValues(), 0.0);

        // IntTimeSeriesEntry constructor: null and empty (count == 0)
        final IntMinMaxValue fromNullEntry = new IntMinMaxValue((IntTimeSeriesEntry) null);
        Assert.assertEquals(0, fromNullEntry.getValueCount());
        Assert.assertEquals(0, fromNullEntry.getMinimumValue());
        Assert.assertEquals(0, fromNullEntry.getMaximumValue());

        final IntMinMaxValue fromEmptyEntry = new IntMinMaxValue(new IntTimeSeriesEntry());
        Assert.assertEquals(0, fromEmptyEntry.getValueCount());
        Assert.assertEquals(fromNullEntry, fromEmptyEntry);

        // IntTimeSeriesEntry constructor: populated entry
        final IntTimeSeriesEntry entry = new IntTimeSeriesEntry(50, false);
        entry.updateValue(100, false);
        entry.updateValue(150, false);
        final IntMinMaxValue fromEntry = new IntMinMaxValue(entry);
        Assert.assertEquals(3, fromEntry.getValueCount());
        Assert.assertEquals(300L, fromEntry.getAccumulatedValue());
        Assert.assertEquals(50, fromEntry.getMinimumValue());
        Assert.assertEquals(150, fromEntry.getMaximumValue());
        Assert.assertArrayEquals(new double[] { 50.0, 100.0, 150.0 }, fromEntry.getValues(), 0.0);
    }

    @Test
    public final void testGetValuesAndScalingAcrossAllWords()
    {
        // Boundary: 0 is non-negative and must be recorded in bucket 0
        Assert.assertArrayEquals(new double[] { 0.0 }, new IntMinMaxValue(0).getValues(), 0.0);

        // Test all 4 64-bit words at scale 0: buckets 5 (w0), 70 (w1), 135 (w2), 200 (w3)
        final IntMinMaxValue v = new IntMinMaxValue(5);
        v.updateValue(70);
        v.updateValue(135);
        v.updateValue(200);
        // Negative value is ignored in the bitset but tracked in min/accumulated
        v.updateValue(-10);
        Assert.assertEquals(-10, v.getMinimumValue());
        Assert.assertArrayEquals(new double[] { 5.0, 70.0, 135.0, 200.0 }, v.getValues(), 0.0);

        // Trigger scaling on non-empty bitset (512 requires scale = 2)
        v.updateValue(512);
        // 5>>2=1->4, 70>>2=17->68, 135>>2=33->132, 200>>2=50->200, 512>>2=128->512
        Assert.assertArrayEquals(new double[] { 4.0, 68.0, 132.0, 200.0, 512.0 }, v.getValues(), 0.0);

        // Adding a smaller value when scale > 0 shifts by scale (24 >> 2 = 6 -> 24.0)
        v.updateValue(24);
        Assert.assertArrayEquals(new double[] { 4.0, 24.0, 68.0, 132.0, 200.0, 512.0 }, v.getValues(), 0.0);

        // Fast-path scaling when first value added is >= 256
        final IntMinMaxValue largeFirst = new IntMinMaxValue(1024);
        Assert.assertArrayEquals(new double[] { 1024.0 }, largeFirst.getValues(), 0.0);
    }

    @Test
    public final void testMergeScalesAndNull()
    {
        // Same-scale merge across all 4 words (w0, w1, w2, w3)
        final IntMinMaxValue sameA = new IntMinMaxValue(10);
        final IntMinMaxValue sameB = new IntMinMaxValue(70);
        sameB.updateValue(140);
        sameB.updateValue(210);
        sameA.merge(sameB);
        Assert.assertArrayEquals(new double[] { 10.0, 70.0, 140.0, 210.0 }, sameA.getValues(), 0.0);

        final IntMinMaxValue a = new IntMinMaxValue(10);
        a.updateValue(200);
        // Merge null is a no-op
        Assert.assertSame(a, a.merge(null));
        Assert.assertEquals(2, a.getValueCount());

        // Merge b with larger scale into a (this.scale < other.scale)
        final IntMinMaxValue b = new IntMinMaxValue(1000);
        a.merge(b);
        // 1000 requires scale 2 (256<<2 = 1024): 10->8, 200->200, 1000->1000
        Assert.assertArrayEquals(new double[] { 8.0, 200.0, 1000.0 }, a.getValues(), 0.0);

        // Merge c with smaller scale into a (this.scale > other.scale), without mutating c
        final IntMinMaxValue c = new IntMinMaxValue(15);
        c.updateValue(80);
        c.updateValue(150);
        c.updateValue(220);
        final double[] cBefore = c.getValues();
        a.merge(c);
        // Verify c was not mutated by merging into a
        Assert.assertArrayEquals(cBefore, c.getValues(), 0.0);
        Assert.assertArrayEquals(new double[] { 8.0, 12.0, 80.0, 148.0, 200.0, 220.0, 1000.0 }, a.getValues(), 0.0);
    }

    @Test
    public final void testEqualsAllFieldsAndHashCode()
    {
        final IntMinMaxValue base = new IntMinMaxValue(10);
        base.updateValue(20);
        base.updateValue(30); // count=3, sum=60, min=10, max=30
        Assert.assertNotEquals(0, base.hashCode());

        // Different accumulatedValue ONLY (all other fields including w0..w3 identical)
        final IntMinMaxValue rawBase = new IntMinMaxValue(60L, 3, 10, 30, 0, 1L, 2L, 4L, 8L);
        Assert.assertEquals(rawBase, new IntMinMaxValue(60L, 3, 10, 30, 0, 1L, 2L, 4L, 8L));
        Assert.assertNotEquals(rawBase, new IntMinMaxValue(65L, 3, 10, 30, 0, 1L, 2L, 4L, 8L));
        Assert.assertNotEquals(rawBase, new IntMinMaxValue(60L, 3, 10, 35, 0, 1L, 2L, 4L, 8L));

        // Different minimum with same sum, max, count, and bitset (negative values don't touch bitset)
        final IntMinMaxValue minA = new IntMinMaxValue(20);
        minA.updateValue(-2);
        minA.updateValue(-8); // sum=10, max=20, min=-8, count=3
        final IntMinMaxValue minB = new IntMinMaxValue(20);
        minB.updateValue(-5);
        minB.updateValue(-5); // sum=10, max=20, min=-5, count=3
        Assert.assertNotEquals(minA, minB);

        // Different valueCount with same sum, min, max, and bitset (extra 0 doesn't change sum/min/max/bitset)
        final IntMinMaxValue countA = new IntMinMaxValue(0);
        countA.updateValue(10); // sum=10, min=0, max=10, count=2
        final IntMinMaxValue countB = new IntMinMaxValue(0);
        countB.updateValue(0);
        countB.updateValue(10); // sum=10, min=0, max=10, count=3
        Assert.assertNotEquals(countA, countB);

        // Different scale with same sum, min, max, count (negative values don't set bits, but merging an empty high-scale value changes scale)
        final IntMinMaxValue scaleA = new IntMinMaxValue(-10);
        final IntMinMaxValue scaleB = new IntMinMaxValue(-5);
        final IntMinMaxValue highScaleEmpty = new IntMinMaxValue(512); // scale=2
        // Create two instances that only differ in w0, w1, w2, w3
        final IntMinMaxValue w0A = new IntMinMaxValue(-100);
        w0A.updateValue(1);
        w0A.updateValue(9);
        w0A.updateValue(100); // sum=10, min=-100, max=100, count=4, w0 bits 1,9
        final IntMinMaxValue w0B = new IntMinMaxValue(-100);
        w0B.updateValue(2);
        w0B.updateValue(8);
        w0B.updateValue(100); // sum=10, min=-100, max=100, count=4, w0 bits 2,8
        Assert.assertNotEquals(w0A, w0B);

        // Differ in w1 (64..127)
        final IntMinMaxValue w1A = new IntMinMaxValue(-200);
        w1A.updateValue(65);
        w1A.updateValue(75);
        w1A.updateValue(200); // sum=140, min=-200, max=200, count=4
        final IntMinMaxValue w1B = new IntMinMaxValue(-200);
        w1B.updateValue(66);
        w1B.updateValue(74);
        w1B.updateValue(200);
        Assert.assertNotEquals(w1A, w1B);

        // Differ in w2 (128..191)
        final IntMinMaxValue w2A = new IntMinMaxValue(-200);
        w2A.updateValue(130);
        w2A.updateValue(140);
        w2A.updateValue(200);
        final IntMinMaxValue w2B = new IntMinMaxValue(-200);
        w2B.updateValue(131);
        w2B.updateValue(139);
        w2B.updateValue(200);
        Assert.assertNotEquals(w2A, w2B);

        // Differ in w3 (192..255)
        final IntMinMaxValue w3A = new IntMinMaxValue(-250);
        w3A.updateValue(195);
        w3A.updateValue(205);
        w3A.updateValue(250);
        final IntMinMaxValue w3B = new IntMinMaxValue(-250);
        w3B.updateValue(196);
        w3B.updateValue(204);
        w3B.updateValue(250);
        Assert.assertNotEquals(w3A, w3B);
    }
}
