/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
}
