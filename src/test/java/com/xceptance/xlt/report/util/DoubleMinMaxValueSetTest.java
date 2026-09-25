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
        // set2: 20 to 60 seconds (overlapping 20..40, disjoint 40..60)
        for (int i = 20; i < 60; i++)
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
    }
}
