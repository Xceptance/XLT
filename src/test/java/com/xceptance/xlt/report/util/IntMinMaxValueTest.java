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
package com.xceptance.xlt.report.util;

import org.junit.Assert;
import org.junit.Test;

public class IntMinMaxValueTest
{
    @Test
    public final void testMinMaxValueInt_0()
    {
        final IntMinMaxValue v = new IntMinMaxValue(0);
        Assert.assertEquals(0, v.getAccumulatedValue());
        Assert.assertEquals(0, v.getAverageValue());
        Assert.assertEquals(0, v.getMaximumValue());
        Assert.assertEquals(0, v.getMinimumValue());
        Assert.assertEquals(1, v.getValueCount());
        Assert.assertArrayEquals(new double[]{0.0}, v.getValues(), 0.0);
    }

    @Test
    public final void testMinMaxValueInt_1()
    {
        final IntMinMaxValue v = new IntMinMaxValue(1);
        Assert.assertEquals(1, v.getAccumulatedValue());
        Assert.assertEquals(1, v.getAverageValue());
        Assert.assertEquals(1, v.getMaximumValue());
        Assert.assertEquals(1, v.getMinimumValue());
        Assert.assertEquals(1, v.getValueCount());
        Assert.assertArrayEquals(new double[]{1.0}, v.getValues(), 0.0);
    }

    /**
     * We don't support negative values, we adjust them to zero.
     */
    @Test
    public final void testMinMaxValueInt_neg2()
    {
        final IntMinMaxValue v = new IntMinMaxValue(-2);
        Assert.assertEquals(0, v.getAccumulatedValue());
        Assert.assertEquals(0, v.getAverageValue());
        Assert.assertEquals(0, v.getMaximumValue());
        Assert.assertEquals(0, v.getMinimumValue());
        Assert.assertEquals(1, v.getValueCount());
        Assert.assertArrayEquals(new double[]{0.0}, v.getValues(), 0.0);
    }

    @Test
    public final void testUpdateValue_2()
    {
        final IntMinMaxValue v = new IntMinMaxValue(2);
        v.updateValue(2);

        Assert.assertEquals(4, v.getAccumulatedValue());
        Assert.assertEquals(2, v.getAverageValue());
        Assert.assertEquals(2, v.getMaximumValue());
        Assert.assertEquals(2, v.getMinimumValue());
        Assert.assertEquals(2, v.getValueCount());
        Assert.assertArrayEquals(new double[]{2.0}, v.getValues(), 0.0);
    }

    @Test
    public final void testUpdateValue_5()
    {
        final IntMinMaxValue v = new IntMinMaxValue(1);
        v.updateValue(2);
        v.updateValue(3);
        v.updateValue(4);
        v.updateValue(5);

        Assert.assertEquals(15, v.getAccumulatedValue());
        Assert.assertEquals(3.0, v.getAverageValue(), 0.0);
        Assert.assertEquals(5, v.getMaximumValue());
        Assert.assertEquals(1, v.getMinimumValue());
        Assert.assertEquals(5, v.getValueCount());
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0, 5.0}, v.getValues(), 0.0);
    }

    @Test
    public final void testUpdateValue_touchDistinctValueLimit_127()
    {
        final IntMinMaxValue v = new IntMinMaxValue(0);
        v.updateValue(127);

        Assert.assertEquals(127, v.getAccumulatedValue());
        Assert.assertEquals(127 / 2, v.getAverageValue(), 0.0);
        Assert.assertEquals(127, v.getMaximumValue());
        Assert.assertEquals(0, v.getMinimumValue());
        Assert.assertEquals(2, v.getValueCount());
        Assert.assertArrayEquals(new double[]{0.0, 127.0}, v.getValues(), 0.0);
    }

    @Test
    public final void testUpdateValue_hitDistinctValueLimit_128()
    {
        final IntMinMaxValue v = new IntMinMaxValue(0);
        v.updateValue(128);

        Assert.assertEquals(128, v.getAccumulatedValue());
        Assert.assertEquals(128 / 2, v.getAverageValue(), 0.0);
        Assert.assertEquals(128, v.getMaximumValue());
        Assert.assertEquals(0, v.getMinimumValue());
        Assert.assertEquals(2, v.getValueCount());
        Assert.assertArrayEquals(new double[]{0.0, 128.0}, v.getValues(), 0.0);
    }

    @Test
    public final void testUpdateValue_crossDistinctValueLimit_129()
    {
        final IntMinMaxValue v = new IntMinMaxValue(0);
        v.updateValue(129);

        Assert.assertEquals(129, v.getAccumulatedValue());
        Assert.assertEquals(129 / 2, v.getAverageValue(), 0.0);
        Assert.assertEquals(129, v.getMaximumValue());
        Assert.assertEquals(0, v.getMinimumValue());
        Assert.assertEquals(2, v.getValueCount());
        Assert.assertArrayEquals(new double[]{0.0, 128.0}, v.getValues(), 0.0);
    }

    @Test
    public final void testUpdateValue_crossDistinctValueLimit_142()
    {
        final IntMinMaxValue v = new IntMinMaxValue(0);
        v.updateValue(142);

        Assert.assertEquals(142, v.getAccumulatedValue());
        Assert.assertEquals(142 / 2, v.getAverageValue(), 0.0);
        Assert.assertEquals(142, v.getMaximumValue());
        Assert.assertEquals(0, v.getMinimumValue());
        Assert.assertEquals(2, v.getValueCount());
        Assert.assertArrayEquals(new double[]{0.0, 142.0}, v.getValues(), 0.0);
    }

    @Test
    public final void testUpdateValue_MultipleValues_0()
    {
        final IntMinMaxValue v = new IntMinMaxValue(10);
        v.updateValue(20);
        v.updateValue(30);
        v.updateValue(40);
        v.updateValue(50);
        v.updateValue(60);
        v.updateValue(70);
        v.updateValue(80);
        v.updateValue(90);
        v.updateValue(100);
        v.updateValue(110);
        v.updateValue(120);

        Assert.assertEquals(780, v.getAccumulatedValue());
        Assert.assertEquals(12, v.getValueCount());
        Assert.assertEquals(780 / 12, v.getAverageValue(), 0.0);
        Assert.assertEquals(120, v.getMaximumValue());
        Assert.assertEquals(10, v.getMinimumValue());
        Assert.assertArrayEquals(
                                 new double[]{
                                     10.0, 20.0, 30.0, 40.0, 50.0, 
                                     60.0, 70.0, 80.0, 90.0, 100.0, 110.0,
                                     120.0}, 
                                 v.getValues(), 0.0);
    }

    @Test
    public final void testUpdateValue_MultipleValues_0_Reverse()
    {
        final IntMinMaxValue v = new IntMinMaxValue(120);
        v.updateValue(110);
        v.updateValue(100);
        v.updateValue(90);
        v.updateValue(80);
        v.updateValue(70);
        v.updateValue(60);
        v.updateValue(50);
        v.updateValue(40);
        v.updateValue(30);
        v.updateValue(20);
        v.updateValue(10);

        Assert.assertEquals(780, v.getAccumulatedValue());
        Assert.assertEquals(12, v.getValueCount());
        Assert.assertEquals(780 / 12, v.getAverageValue(), 0.0);
        Assert.assertEquals(120, v.getMaximumValue());
        Assert.assertEquals(10, v.getMinimumValue());
        Assert.assertArrayEquals(
                                 new double[]{
                                     10.0, 20.0, 30.0, 40.0, 50.0, 
                                     60.0, 70.0, 80.0, 90.0, 100.0, 110.0,
                                     120.0}, 
                                 v.getValues(), 0.0);
    }

    @Test
    public final void testUpdateValue_MultipleValues_1()
    {
        int[] in =
            {
                3, 11, 37, 53, 73, 97, 101, 111, 137, 153, 173, 197, 1000, 2000, 3000
            };
        double[] ex =
            {
                0.0, 32.0, 64.0, 96.0, 128.0, 160.0, 192.0, 992.0, 1984.0, 2976.0
            };

        IntMinMaxValue v = new IntMinMaxValue(in[0]);
        for (int i = 1; i < in.length; i++)
        {
            v.updateValue(in[i]);
        }

        Assert.assertEquals(7146, v.getAccumulatedValue());
        Assert.assertEquals(15, v.getValueCount());
        Assert.assertEquals(7146 / 15, v.getAverageValue(), 0.0);
        Assert.assertEquals(3000, v.getMaximumValue());
        Assert.assertEquals(3, v.getMinimumValue());
        Assert.assertArrayEquals(
                                 ex, 
                                 v.getValues(), 0.0);
    }
    
    @Test
    public final void testUpdateValue_MultipleValues_1_Reversed()
    {
        int[] in =
            {
                3, 11, 37, 53, 73, 97, 101, 111, 137, 153, 173, 197, 1000, 2000, 3000
            };
        double[] ex =
            {
                0.0, 32.0, 64.0, 96.0, 128.0, 160.0, 192.0, 992.0, 1984.0, 2976.0
            };

        IntMinMaxValue v = new IntMinMaxValue(in[in.length - 1]);
        for (int i = in.length  - 2; i >= 0; i--)
        {
            v.updateValue(in[i]);
        }

        Assert.assertEquals(7146, v.getAccumulatedValue());
        Assert.assertEquals(15, v.getValueCount());
        Assert.assertEquals(7146 / 15, v.getAverageValue(), 0.0);
        Assert.assertEquals(3000, v.getMaximumValue());
        Assert.assertEquals(3, v.getMinimumValue());
        Assert.assertArrayEquals(
                                 ex, 
                                 v.getValues(), 0.0);
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
        Assert.assertEquals(2, v.getValueCount());

        v.merge(v2);
        Assert.assertEquals(30, v.getAccumulatedValue());
        Assert.assertEquals(10, v.getAverageValue());
        Assert.assertEquals(20, v.getMaximumValue());
        Assert.assertEquals(0, v.getMinimumValue());
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
