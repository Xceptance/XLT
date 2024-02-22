/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
        Assert.assertFalse(v.equals(new IntMinMaxValue(6210)));
    }
}
