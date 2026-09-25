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
import org.junit.Ignore;
import org.junit.Test;

public class ValueSetTest
{
    /**
     * Test empty condition getValues
     */
    @Test
    public final void testGetValues_Empty()
    {
        final ValueSet set = new ValueSet();
        Assert.assertEquals(0, set.getValues().length);
        Assert.assertEquals(0, set.getValueCount());
    }

    /**
     * Test empty condition getMinimumTime
     */
    @Test(expected = IllegalStateException.class)
    public final void testGetMinimumTime_Empty()
    {
        final ValueSet set = new ValueSet();
        set.getMinimumTime();
    }

    /**
     * Test empty condition getMaximumTime
     */
    @Test(expected = IllegalStateException.class)
    public final void testGetMaximumTime_Empty()
    {
        final ValueSet set = new ValueSet();
        set.getMaximumTime();
    }

    /**
     * Test empty condition getFirstSecond
     */
    @Test(expected = IllegalStateException.class)
    public final void testGetFirstSecond_Empty()
    {
        final ValueSet set = new ValueSet();
        set.getFirstSecond();
    }

    /**
     * Test empty condition getLastSecond
     */
    @Test(expected = IllegalStateException.class)
    public final void testGetLastSecond_Empty()
    {
        final ValueSet set = new ValueSet();
        set.getLastSecond();
    }

    /**
     * Test empty condition getLengthInSeconds
     */
    @Test(expected = IllegalStateException.class)
    public final void testGetLengthInSeconds_Empty()
    {
        final ValueSet set = new ValueSet();
        set.getLengthInSeconds();
    }

    @Test
    public final void testAddOrUpdateValue_OnValue()
    {
        final ValueSet set = new ValueSet();
        set.addOrUpdateValue(1337042000010L, 50);

        Assert.assertEquals(1337042000L, set.getFirstSecond());
        Assert.assertEquals(1337042000L, set.getLastSecond());
        Assert.assertEquals(1337042000010L, set.getMinimumTime());
        Assert.assertEquals(1337042000010L, set.getMaximumTime());
        Assert.assertEquals(1, set.getValueCount());
        Assert.assertEquals(1L, set.getLengthInSeconds());
        Assert.assertArrayEquals(new int[]
            {
                50
            }, set.getValues());
    }

    @Test
    public final void testAddOrUpdateValue_1secDiff()
    {
        final ValueSet set = new ValueSet();
        set.addOrUpdateValue(1337042000010L, 50);
        set.addOrUpdateValue(1337042001020L, 100);

        Assert.assertEquals(1337042000L, set.getFirstSecond());
        Assert.assertEquals(1337042001L, set.getLastSecond());
        Assert.assertEquals(1337042000010L, set.getMinimumTime());
        Assert.assertEquals(1337042001020L, set.getMaximumTime());
        Assert.assertEquals(2, set.getValueCount());
        Assert.assertEquals(2L, set.getLengthInSeconds());
        Assert.assertArrayEquals(new int[]
            {
                50, 100
            }, set.getValues());
    }

    @Test
    public final void testAddOrUpdateValue_2secDiff()
    {
        final ValueSet set = new ValueSet();
        set.addOrUpdateValue(1337042000010L, 50);
        set.addOrUpdateValue(1337042002000L, 100);

        Assert.assertEquals(1337042000L, set.getFirstSecond());
        Assert.assertEquals(1337042002L, set.getLastSecond());
        Assert.assertEquals(1337042000010L, set.getMinimumTime());
        Assert.assertEquals(1337042002000L, set.getMaximumTime());
        Assert.assertEquals(2, set.getValueCount());
        Assert.assertEquals(3L, set.getLengthInSeconds());
        Assert.assertArrayEquals(new int[]
            {
                50, 0, 100
            }, set.getValues());
    }

    @Test
    public final void testAddOrUpdateValue_11secDiff()
    {
        final ValueSet set = new ValueSet();
        set.addOrUpdateValue(1337042000010L, 50);
        set.addOrUpdateValue(1337042002000L, 100);
        set.addOrUpdateValue(1337042011000L, 200);

        Assert.assertEquals(1337042000L, set.getFirstSecond());
        Assert.assertEquals(1337042011L, set.getLastSecond());
        Assert.assertEquals(1337042000010L, set.getMinimumTime());
        Assert.assertEquals(1337042011000L, set.getMaximumTime());
        Assert.assertEquals(3, set.getValueCount());
        Assert.assertEquals(12L, set.getLengthInSeconds());
        Assert.assertArrayEquals(new int[]
            {
                50, 0, 100, 0, 0, 0, 0, 0, 0, 0, 0, 200
            }, set.getValues());
    }

    @Test
    public final void testAddOrUpdateValue_DifferentCreationSequence()
    {
        final ValueSet set1 = new ValueSet();
        set1.addOrUpdateValue(1337042000000L, 50);
        set1.addOrUpdateValue(1337042002000L, 100);
        set1.addOrUpdateValue(1337042002010L, 200);
        set1.addOrUpdateValue(1337042012000L, 150);

        final ValueSet set2 = new ValueSet();
        set2.addOrUpdateValue(1337042012000L, 150);
        set2.addOrUpdateValue(1337042002010L, 200);
        set2.addOrUpdateValue(1337042002000L, 100);
        set2.addOrUpdateValue(1337042000000L, 50);

        final ValueSet set3 = new ValueSet();
        set3.addOrUpdateValue(1337042012000L, 150);
        set3.addOrUpdateValue(1337042002000L, 100);
        set3.addOrUpdateValue(1337042002010L, 200);
        set3.addOrUpdateValue(1337042000000L, 50);

        final ValueSet set4 = new ValueSet();
        set4.addOrUpdateValue(1337042000000L, 50);
        set4.addOrUpdateValue(1337042002010L, 200);
        set4.addOrUpdateValue(1337042002000L, 100);
        set4.addOrUpdateValue(1337042012000L, 150);

        Assert.assertEquals(set1, set2);
        Assert.assertEquals(set1, set3);
        Assert.assertEquals(set1, set4);
    }

    /**
     * TODO: Ignored until we decide whether MinMaxValueSet.getValues() can return up to size or 2x size values.
     */
    @Test
    @Ignore
    public final void testToMinMaxValueSet()
    {
        final ValueSet set = new ValueSet();
        set.addOrUpdateValue(1337042000000L, 50);
        set.addOrUpdateValue(1337042001000L, 100);
        set.addOrUpdateValue(1337042002000L, 100);
        set.addOrUpdateValue(1337042011000L, 200);
        set.addOrUpdateValue(1337042019000L, 300);

        final IntMinMaxValue MMV0 = new IntMinMaxValue(0);

        // 20 seconds
        final IntMinMaxValue[] expected = new IntMinMaxValue[]
            {
                new IntMinMaxValue(50), new IntMinMaxValue(100), new IntMinMaxValue(100), MMV0, MMV0, MMV0, MMV0, MMV0, MMV0, MMV0, MMV0,
                new IntMinMaxValue(200), MMV0, MMV0, MMV0, MMV0, MMV0, MMV0, MMV0, new IntMinMaxValue(300)
            };

        final IntMinMaxValue[] actual = set.toMinMaxValueSet(20).getValues();
        Assert.assertArrayEquals(expected, actual);

        // 20 seconds
        final IntMinMaxValue[] expected10 = new IntMinMaxValue[]
            {
                new IntMinMaxValue(50).merge(new IntMinMaxValue(100)), new IntMinMaxValue(100).merge(MMV0), new IntMinMaxValue(0).merge(MMV0),
                new IntMinMaxValue(0).merge(MMV0), new IntMinMaxValue(0).merge(MMV0), new IntMinMaxValue(0).merge(new IntMinMaxValue(200)),
                new IntMinMaxValue(0).merge(MMV0), new IntMinMaxValue(0).merge(MMV0), new IntMinMaxValue(0).merge(MMV0),
                new IntMinMaxValue(0).merge(new IntMinMaxValue(300))
            };

        final IntMinMaxValue[] actual10 = set.toMinMaxValueSet(10).getValues();
        Assert.assertArrayEquals(expected10, actual10);
    }

    @Test
    public final void testMergeNullOrEmpty()
    {
        final ValueSet set1 = new ValueSet();
        set1.addOrUpdateValue(1000L, 5);

        // merge null
        set1.merge(null);
        Assert.assertEquals(1, set1.getValueCount());

        // merge empty
        set1.merge(new ValueSet());
        Assert.assertEquals(1, set1.getValueCount());

        // merge into empty
        final ValueSet empty = new ValueSet();
        empty.merge(set1);
        Assert.assertEquals(1, empty.getValueCount());
        Assert.assertEquals(set1.getFirstSecond(), empty.getFirstSecond());
        Assert.assertEquals(set1.getLastSecond(), empty.getLastSecond());
        Assert.assertArrayEquals(set1.getValues(), empty.getValues());
    }

    @Test
    public final void testMergeEarlierAndLater()
    {
        final ValueSet sequential = new ValueSet();
        final ValueSet setMiddle = new ValueSet();
        final ValueSet setEarlier = new ValueSet();
        final ValueSet setLater = new ValueSet();

        // 1000000 ms = 1000 s
        sequential.addOrUpdateValue(1000000L, 10);
        setMiddle.addOrUpdateValue(1000000L, 10);

        // Earlier: 990000 ms = 990 s (diff = 10s)
        sequential.addOrUpdateValue(990000L, 20);
        setEarlier.addOrUpdateValue(990000L, 20);

        // Later: 1020000 ms = 1020 s (diff = 20s)
        sequential.addOrUpdateValue(1020000L, 30);
        setLater.addOrUpdateValue(1020000L, 30);

        setMiddle.merge(setEarlier);
        setMiddle.merge(setLater);

        Assert.assertEquals(sequential.getFirstSecond(), setMiddle.getFirstSecond());
        Assert.assertEquals(sequential.getLastSecond(), setMiddle.getLastSecond());
        Assert.assertEquals(sequential.getMinimumTime(), setMiddle.getMinimumTime());
        Assert.assertEquals(sequential.getMaximumTime(), setMiddle.getMaximumTime());
        Assert.assertEquals(sequential.getValueCount(), setMiddle.getValueCount());
        Assert.assertArrayEquals(sequential.getValues(), setMiddle.getValues());
    }

    @Test
    public final void testMergeOverlappingAndRandom()
    {
        final java.util.Random rng = new java.util.Random(12345);
        final ValueSet sequential = new ValueSet();
        final ValueSet setA = new ValueSet();
        final ValueSet setB = new ValueSet();

        final long baseTime = 1600000000000L;

        for (int i = 0; i < 2000; i++)
        {
            final long time = baseTime + rng.nextInt(500) * 1000L + rng.nextInt(1000);
            final int value = rng.nextInt(100) + 1;

            sequential.addOrUpdateValue(time, value);
            if (rng.nextBoolean())
            {
                setA.addOrUpdateValue(time, value);
            }
            else
            {
                setB.addOrUpdateValue(time, value);
            }
        }

        setA.merge(setB);

        Assert.assertEquals(sequential.getFirstSecond(), setA.getFirstSecond());
        Assert.assertEquals(sequential.getLastSecond(), setA.getLastSecond());
        Assert.assertEquals(sequential.getMinimumTime(), setA.getMinimumTime());
        Assert.assertEquals(sequential.getMaximumTime(), setA.getMaximumTime());
        Assert.assertEquals(sequential.getValueCount(), setA.getValueCount());
        Assert.assertArrayEquals(sequential.getValues(), setA.getValues());
    }
}
