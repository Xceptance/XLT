/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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

public class MinMaxValueTest
{
    @Test
    public final void testMinMaxValue()
    {
        final MinMaxValue set = new MinMaxValue();
        Assert.assertEquals(0, set.getAccumulatedValue());
        Assert.assertEquals(0, set.getAverageValue());
        Assert.assertEquals(0, set.getMaximumValue());
        Assert.assertEquals(0, set.getMinimumValue());
        Assert.assertEquals(0, set.getValue());
        Assert.assertEquals(0, set.getValueCount());
    }

    @Test
    public final void testMinMaxValueInt_0()
    {
        final MinMaxValue set = new MinMaxValue(0);
        Assert.assertEquals(0, set.getAccumulatedValue());
        Assert.assertEquals(0, set.getAverageValue());
        Assert.assertEquals(0, set.getMaximumValue());
        Assert.assertEquals(0, set.getMinimumValue());
        Assert.assertEquals(0, set.getValue());
        Assert.assertEquals(1, set.getValueCount());
    }

    @Test
    public final void testMinMaxValueInt_1()
    {
        final MinMaxValue set = new MinMaxValue(1);
        Assert.assertEquals(1, set.getAccumulatedValue());
        Assert.assertEquals(1, set.getAverageValue());
        Assert.assertEquals(1, set.getMaximumValue());
        Assert.assertEquals(1, set.getMinimumValue());
        Assert.assertEquals(1, set.getValue());
        Assert.assertEquals(1, set.getValueCount());
    }

    @Test
    public final void testMinMaxValueInt_neg2()
    {
        final MinMaxValue set = new MinMaxValue(-2);
        Assert.assertEquals(-2, set.getAccumulatedValue());
        Assert.assertEquals(-2, set.getAverageValue());
        Assert.assertEquals(-2, set.getMaximumValue());
        Assert.assertEquals(-2, set.getMinimumValue());
        Assert.assertEquals(-2, set.getValue());
        Assert.assertEquals(1, set.getValueCount());
    }

    @Test
    public final void testUpdateValue()
    {
        final MinMaxValue set = new MinMaxValue(0);
        set.updateValue(2);

        Assert.assertEquals(2, set.getAccumulatedValue());
        Assert.assertEquals(1, set.getAverageValue());
        Assert.assertEquals(2, set.getMaximumValue());
        Assert.assertEquals(0, set.getMinimumValue());
        Assert.assertEquals(1, set.getValue());
        Assert.assertEquals(2, set.getValueCount());

        // next max
        set.updateValue(10);

        Assert.assertEquals(12, set.getAccumulatedValue());
        Assert.assertEquals(4, set.getAverageValue());
        Assert.assertEquals(10, set.getMaximumValue());
        Assert.assertEquals(0, set.getMinimumValue());
        Assert.assertEquals(4, set.getValue());
        Assert.assertEquals(3, set.getValueCount());

        // next min
        set.updateValue(-2);

        Assert.assertEquals(10, set.getAccumulatedValue());
        Assert.assertEquals(2, set.getAverageValue());
        Assert.assertEquals(10, set.getMaximumValue());
        Assert.assertEquals(-2, set.getMinimumValue());
        Assert.assertEquals(2, set.getValue());
        Assert.assertEquals(4, set.getValueCount());

        // next middle value
        set.updateValue(2);
        Assert.assertEquals(12, set.getAccumulatedValue());
        Assert.assertEquals(2, set.getAverageValue());
        Assert.assertEquals(10, set.getMaximumValue());
        Assert.assertEquals(-2, set.getMinimumValue());
        Assert.assertEquals(2, set.getValue());
        Assert.assertEquals(5, set.getValueCount());
    }

    @Test
    public final void testUpdateValue_0()
    {
        final MinMaxValue set = new MinMaxValue();
        set.updateValue(2);

        Assert.assertEquals(2, set.getAccumulatedValue());
        Assert.assertEquals(2, set.getAverageValue());
        Assert.assertEquals(2, set.getMaximumValue());
        Assert.assertEquals(2, set.getMinimumValue());
        Assert.assertEquals(2, set.getValue());
        Assert.assertEquals(1, set.getValueCount());
    }

    @Test
    public final void testMerge()
    {
        final MinMaxValue set1 = new MinMaxValue(10);
        final MinMaxValue set2 = new MinMaxValue(20);
        final MinMaxValue set = new MinMaxValue(0);

        set.merge(set1);
        Assert.assertEquals(10, set.getAccumulatedValue());
        Assert.assertEquals(5, set.getAverageValue());
        Assert.assertEquals(10, set.getMaximumValue());
        Assert.assertEquals(0, set.getMinimumValue());
        Assert.assertEquals(5, set.getValue());
        Assert.assertEquals(2, set.getValueCount());

        set.merge(set2);
        Assert.assertEquals(30, set.getAccumulatedValue());
        Assert.assertEquals(10, set.getAverageValue());
        Assert.assertEquals(20, set.getMaximumValue());
        Assert.assertEquals(0, set.getMinimumValue());
        Assert.assertEquals(10, set.getValue());
        Assert.assertEquals(3, set.getValueCount());
    }

    @Test
    public final void testMerge_0_0()
    {
        final MinMaxValue set1 = new MinMaxValue();
        final MinMaxValue set2 = new MinMaxValue();

        set1.merge(set2);
        Assert.assertEquals(0, set1.getAccumulatedValue());
        Assert.assertEquals(0, set1.getAverageValue());
        Assert.assertEquals(0, set1.getMaximumValue());
        Assert.assertEquals(0, set1.getMinimumValue());
        Assert.assertEquals(0, set1.getValue());
        Assert.assertEquals(0, set1.getValueCount());
    }

    @Test
    public final void testMerge_0_1()
    {
        final MinMaxValue set1 = new MinMaxValue();
        final MinMaxValue set2 = new MinMaxValue(1);

        set1.merge(set2);
        Assert.assertEquals(1, set1.getAccumulatedValue());
        Assert.assertEquals(1, set1.getAverageValue());
        Assert.assertEquals(1, set1.getMaximumValue());
        Assert.assertEquals(1, set1.getMinimumValue());
        Assert.assertEquals(1, set1.getValue());
        Assert.assertEquals(1, set1.getValueCount());
    }

    @Test
    public final void testMerge_1_0()
    {
        final MinMaxValue set1 = new MinMaxValue(1);
        final MinMaxValue set2 = new MinMaxValue();

        set1.merge(set2);
        Assert.assertEquals(1, set1.getAccumulatedValue());
        Assert.assertEquals(1, set1.getAverageValue());
        Assert.assertEquals(1, set1.getMaximumValue());
        Assert.assertEquals(1, set1.getMinimumValue());
        Assert.assertEquals(1, set1.getValue());
        Assert.assertEquals(1, set1.getValueCount());
    }

    @Test
    public final void testToString()
    {
        final MinMaxValue set = new MinMaxValue(88);
        Assert.assertEquals("88/88/88/88/1", set.toString());

        set.updateValue(12);
        Assert.assertEquals("50/100/12/88/2", set.toString());
    }

    @Test
    public final void testEquals()
    {
        final MinMaxValue set = new MinMaxValue(76210);
        Assert.assertFalse(set.equals(null));
        Assert.assertFalse(set.equals("Foo"));
        Assert.assertTrue(set.equals(set));
        Assert.assertTrue(set.equals(new MinMaxValue(76210)));
        Assert.assertFalse(set.equals(new MinMaxValue(6210)));
    }

}
