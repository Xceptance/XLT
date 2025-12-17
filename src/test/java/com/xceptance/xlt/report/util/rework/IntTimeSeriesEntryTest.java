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
package com.xceptance.xlt.report.util.rework;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IntTimeSeriesEntryTest
{
    @Test
    public final void constructor()
    {
        final IntTimeSeriesEntry v = new IntTimeSeriesEntry();
        assertEquals(0, v.getTotalValue());
        assertEquals(0, v.getAverageValue());
        assertEquals(0, v.getMaximumValue());
        assertEquals(0, v.getMinimumValue());
        assertEquals(0, v.getCount());
        assertEquals(0, v.getConcurrentCount());
        assertEquals(0, v.getErrorCount());
        assertArrayEquals(new double[]{}, v.getValues(), 0.0);
    }

    @Test
    public final void constructor_0()
    {
        {
            final IntTimeSeriesEntry v = new IntTimeSeriesEntry(0, false);
            assertEquals(0, v.getTotalValue());
            assertEquals(0, v.getAverageValue());
            assertEquals(0, v.getMaximumValue());
            assertEquals(0, v.getMinimumValue());
            assertEquals(1, v.getCount());
            assertEquals(1, v.getConcurrentCount());
            assertEquals(0, v.getErrorCount());
            assertArrayEquals(new double[]{0.0}, v.getValues(), 0.0);
        }
        {
            final IntTimeSeriesEntry v = new IntTimeSeriesEntry(0, true);
            assertEquals(0, v.getTotalValue());
            assertEquals(0, v.getAverageValue());
            assertEquals(0, v.getMaximumValue());
            assertEquals(0, v.getMinimumValue());
            assertEquals(1, v.getCount());
            assertEquals(1, v.getConcurrentCount());
            assertEquals(1, v.getErrorCount());
            assertArrayEquals(new double[]{0.0}, v.getValues(), 0.0);
        }
    }

    @Test
    public final void constructor_SameResultFromBothCtrs()
    {
        var expected = new double[]{0.0, 256.0, 768.0, 8704.0};

        // same values but in different order for both ctrs
        final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry();
        v1.updateValue(0, false);
        v1.updateValue(820, false);
        v1.updateValue(10, false);
        v1.updateValue(311, false);
        v1.updateValue(8761, false);

        assertEquals(9902, v1.getTotalValue());
        assertEquals(1980, v1.getAverageValue());
        assertEquals(8761, v1.getMaximumValue());
        assertEquals(0, v1.getMinimumValue());
        assertEquals(5, v1.getCount());
        assertEquals(5, v1.getConcurrentCount());
        assertEquals(0, v1.getErrorCount());
        assertArrayEquals(expected, v1.getValues(), 0.0);

        final IntTimeSeriesEntry v2 = new IntTimeSeriesEntry(820, false);
        v2.updateValue(8761, false);
        v2.updateValue(10, false);
        v2.updateValue(0, false);
        v2.updateValue(311, false);

        assertEquals(9902, v2.getTotalValue());
        assertEquals(1980, v2.getAverageValue());
        assertEquals(8761, v2.getMaximumValue());
        assertEquals(0, v2.getMinimumValue());
        assertEquals(5, v2.getCount());
        assertEquals(5, v2.getConcurrentCount());
        assertEquals(0, v1.getErrorCount());
        assertArrayEquals(expected, v2.getValues(), 0.0);
    }

    @Test
    public final void constructor_1()
    {
        {
            final IntTimeSeriesEntry v = new IntTimeSeriesEntry(1, false);
            assertEquals(1, v.getTotalValue());
            assertEquals(1, v.getAverageValue());
            assertEquals(1, v.getMaximumValue());
            assertEquals(1, v.getMinimumValue());
            assertEquals(1, v.getCount());
            assertEquals(1, v.getConcurrentCount());
            assertEquals(0, v.getErrorCount());
            assertArrayEquals(new double[]{1.0}, v.getValues(), 0.0);
        }
        {
            final IntTimeSeriesEntry v = new IntTimeSeriesEntry(1, true);
            assertEquals(1, v.getTotalValue());
            assertEquals(1, v.getAverageValue());
            assertEquals(1, v.getMaximumValue());
            assertEquals(1, v.getMinimumValue());
            assertEquals(1, v.getCount());
            assertEquals(1, v.getConcurrentCount());
            assertEquals(1, v.getErrorCount());
            assertArrayEquals(new double[]{1.0}, v.getValues(), 0.0);
        }
    }

    /**
     * We don't support negative values, we adjust them to zero.
     */
    @Test
    public final void neg2()
    {
        final IntTimeSeriesEntry v = new IntTimeSeriesEntry(-2, false);
        assertEquals(0, v.getTotalValue());
        assertEquals(0, v.getAverageValue());
        assertEquals(0, v.getMaximumValue());
        assertEquals(0, v.getMinimumValue());
        assertEquals(1, v.getCount());
        assertEquals(1, v.getConcurrentCount());
        assertEquals(0, v.getErrorCount());
        assertArrayEquals(new double[]{0.0}, v.getValues(), 0.0);
    }

    @Test
    public final void updateValue_2()
    {
        final IntTimeSeriesEntry v = new IntTimeSeriesEntry(2, true);
        v.updateValue(2, false);

        assertEquals(4, v.getTotalValue());
        assertEquals(2, v.getAverageValue());
        assertEquals(2, v.getMaximumValue());
        assertEquals(2, v.getMinimumValue());
        assertEquals(2, v.getCount());
        assertEquals(2, v.getConcurrentCount());
        assertEquals(1, v.getErrorCount());
        assertArrayEquals(new double[]{2.0}, v.getValues(), 0.0);
    }

    @Test
    public final void updateValue_5()
    {
        {
            final IntTimeSeriesEntry v = new IntTimeSeriesEntry(1, false);
            v.updateValue(2, false);
            v.updateValue(3, false);
            v.updateValue(4, false);
            v.updateValue(5, false);

            assertEquals(15, v.getTotalValue());
            assertEquals(3.0, v.getAverageValue(), 0.0);
            assertEquals(5, v.getMaximumValue());
            assertEquals(1, v.getMinimumValue());
            assertEquals(5, v.getCount());
            assertEquals(5, v.getConcurrentCount());
            assertEquals(0, v.getErrorCount());
            assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0, 5.0}, v.getValues(), 0.0);
        }
        {
            final IntTimeSeriesEntry v = new IntTimeSeriesEntry(1, false);
            v.updateValue(2, false);
            v.updateValue(3, true);
            v.updateValue(4, false);
            v.updateValue(5, true);

            assertEquals(15, v.getTotalValue());
            assertEquals(3.0, v.getAverageValue(), 0.0);
            assertEquals(5, v.getMaximumValue());
            assertEquals(1, v.getMinimumValue());
            assertEquals(5, v.getCount());
            assertEquals(5, v.getConcurrentCount());
            assertEquals(2, v.getErrorCount());
            assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0, 5.0}, v.getValues(), 0.0);   
        }
    }

    /**
     * We only update concurrency when we deal with counting the true concurrent things
     */
    @Test
    public final void updateConcurrency()
    {
        final IntTimeSeriesEntry v = new IntTimeSeriesEntry(2, true);
        v.updateValue(2, false);

        assertEquals(4, v.getTotalValue());
        assertEquals(2, v.getAverageValue());
        assertEquals(2, v.getMaximumValue());
        assertEquals(2, v.getMinimumValue());
        assertEquals(2, v.getCount());
        assertEquals(2, v.getConcurrentCount());
        assertEquals(1, v.getErrorCount());
        assertArrayEquals(new double[]{2.0}, v.getValues(), 0.0);

        v.updateConcurrency();

        // ensure no changes except concurrency
        assertEquals(4, v.getTotalValue());
        assertEquals(2, v.getAverageValue());
        assertEquals(2, v.getMaximumValue());
        assertEquals(2, v.getMinimumValue());
        assertEquals(2, v.getCount());
        assertEquals(3, v.getConcurrentCount());
        assertEquals(1, v.getErrorCount());
        assertArrayEquals(new double[]{2.0}, v.getValues(), 0.0);

        v.updateConcurrency();

        // ensure no changes except concurrency
        assertEquals(4, v.getTotalValue());
        assertEquals(2, v.getAverageValue());
        assertEquals(2, v.getMaximumValue());
        assertEquals(2, v.getMinimumValue());
        assertEquals(2, v.getCount());
        assertEquals(4, v.getConcurrentCount());
        assertEquals(1, v.getErrorCount());
        assertArrayEquals(new double[]{2.0}, v.getValues(), 0.0);
    }

    @Test
    public final void updateValue_touchDistinctValueLimit_127()
    {
        final IntTimeSeriesEntry v = new IntTimeSeriesEntry(0, false);
        v.updateValue(127, false);

        assertEquals(127, v.getTotalValue());
        assertEquals(127 / 2, v.getAverageValue(), 0.0);
        assertEquals(127, v.getMaximumValue());
        assertEquals(0, v.getMinimumValue());
        assertEquals(2, v.getCount());
        assertEquals(2, v.getConcurrentCount());
        assertEquals(0, v.getErrorCount());
        assertArrayEquals(new double[]{0.0, 127.0}, v.getValues(), 0.0);
    }

    @Test
    public final void updateValue_hitDistinctValueLimit_128()
    {
        final IntTimeSeriesEntry v = new IntTimeSeriesEntry(0, false);
        v.updateValue(128, false);

        assertEquals(128, v.getTotalValue());
        assertEquals(128 / 2, v.getAverageValue(), 0.0);
        assertEquals(128, v.getMaximumValue());
        assertEquals(0, v.getMinimumValue());
        assertEquals(2, v.getCount());
        assertEquals(2, v.getConcurrentCount());
        assertEquals(0, v.getErrorCount());
        assertArrayEquals(new double[]{0.0, 128.0}, v.getValues(), 0.0);
    }

    @Test
    public final void updateValue_crossDistinctValueLimit_129()
    {
        final IntTimeSeriesEntry v = new IntTimeSeriesEntry(0, false);
        v.updateValue(129, false);

        assertEquals(129, v.getTotalValue());
        assertEquals(129 / 2, v.getAverageValue(), 0.0);
        assertEquals(129, v.getMaximumValue());
        assertEquals(0, v.getMinimumValue());
        assertEquals(2, v.getCount());
        assertEquals(2, v.getConcurrentCount());
        assertEquals(0, v.getErrorCount());
        assertArrayEquals(new double[]{0.0, 128.0}, v.getValues(), 0.0);
    }

    @Test
    public final void updateValue_crossDistinctValueLimit_142()
    {
        final IntTimeSeriesEntry v = new IntTimeSeriesEntry(0, false);
        v.updateValue(142, false);

        assertEquals(142, v.getTotalValue());
        assertEquals(142 / 2, v.getAverageValue(), 0.0);
        assertEquals(142, v.getMaximumValue());
        assertEquals(0, v.getMinimumValue());
        assertEquals(2, v.getCount());
        assertEquals(2, v.getConcurrentCount());
        assertEquals(0, v.getErrorCount());
        assertArrayEquals(new double[]{0.0, 142.0}, v.getValues(), 0.0);
    }

    @Test
    public final void updateValue_LoosingValues()
    {
        {
            final IntTimeSeriesEntry v = new IntTimeSeriesEntry(100, true);
            v.updateValue(128, true);
            v.updateValue(255, true);
            v.updateValue(4000, true);
            v.updateValue(200, true);
            v.updateValue(1000, true);

            assertEquals(5683, v.getTotalValue());
            assertEquals(5683 / 6, v.getAverageValue(), 0.0);
            assertEquals(4000, v.getMaximumValue());
            assertEquals(100, v.getMinimumValue());
            assertEquals(6, v.getCount());
            assertEquals(6, v.getConcurrentCount());
            assertEquals(6, v.getErrorCount());
            assertArrayEquals(
                              new double[]{96.0, 128.0, 192.0, 224.0, 992.0, 4000.0}, 
                              v.getValues(), 0.0);
        }
        {
            final IntTimeSeriesEntry v = new IntTimeSeriesEntry(100, false);
            v.updateValue(1000, false);
            v.updateValue(4000, false);
            v.updateValue(128, false);
            v.updateValue(255, true);
            v.updateValue(200, false);

            assertEquals(5683, v.getTotalValue());
            assertEquals(5683 / 6, v.getAverageValue(), 0.0);
            assertEquals(4000, v.getMaximumValue());
            assertEquals(100, v.getMinimumValue());
            assertEquals(6, v.getCount());
            assertEquals(6, v.getConcurrentCount());
            assertEquals(1, v.getErrorCount());
            assertArrayEquals(
                              new double[]{96.0, 128.0, 192.0, 224.0, 992.0, 4000.0}, 
                              v.getValues(), 0.0);
        }
    }

    @Test
    public final void updateValue_MultipleValues_0()
    {
        final IntTimeSeriesEntry v = new IntTimeSeriesEntry(10, false);
        v.updateValue(20, false);
        v.updateValue(30, false);
        v.updateValue(40, false);
        v.updateValue(50, false);
        v.updateValue(60, false);
        v.updateValue(70, false);
        v.updateValue(80, false);
        v.updateValue(90, false);
        v.updateValue(100, false);
        v.updateValue(110, false);
        v.updateValue(120, false);

        assertEquals(780, v.getTotalValue());
        assertEquals(780 / 12, v.getAverageValue(), 0.0);
        assertEquals(120, v.getMaximumValue());
        assertEquals(10, v.getMinimumValue());
        assertEquals(12, v.getCount());
        assertEquals(12, v.getConcurrentCount());
        assertEquals(0, v.getErrorCount());

        assertArrayEquals(
                          new double[]{
                              10.0, 20.0, 30.0, 40.0, 50.0, 
                              60.0, 70.0, 80.0, 90.0, 100.0, 110.0,
                              120.0}, 
                          v.getValues(), 0.0);
    }

    @Test
    public final void updateValue_MultipleValues_0_Reverse()
    {
        final IntTimeSeriesEntry v = new IntTimeSeriesEntry(120, true);
        v.updateValue(110, false);
        v.updateValue(100, false);
        v.updateValue(90, false);
        v.updateValue(80, false);
        v.updateValue(70, false);
        v.updateValue(60, false);
        v.updateValue(50, false);
        v.updateValue(40, false);
        v.updateValue(30, false);
        v.updateValue(20, false);
        v.updateValue(10, false);

        assertEquals(780, v.getTotalValue());
        assertEquals(780 / 12, v.getAverageValue(), 0.0);
        assertEquals(120, v.getMaximumValue());
        assertEquals(10, v.getMinimumValue());
        assertEquals(12, v.getCount());
        assertEquals(12, v.getConcurrentCount());
        assertEquals(1, v.getErrorCount());

        assertArrayEquals(
                          new double[]{
                              10.0, 20.0, 30.0, 40.0, 50.0, 
                              60.0, 70.0, 80.0, 90.0, 100.0, 110.0,
                              120.0}, 
                          v.getValues(), 0.0);
    }

    @Test
    public final void udateValue_MultipleValues_1()
    {
        int[] in =
            {
                3, 11, 37, 53, 73, 97, 101, 111, 137, 153, 173, 197, 1000, 2000, 3000
            };
        double[] ex =
            {
                0.0, 32.0, 64.0, 96.0, 128.0, 160.0, 192.0, 992.0, 1984.0, 2976.0
            };

        IntTimeSeriesEntry v = new IntTimeSeriesEntry(in[0], false);
        for (int i = 1; i < in.length; i++)
        {
            v.updateValue(in[i], false);
        }

        assertEquals(7146, v.getTotalValue());
        assertEquals(7146 / 15, v.getAverageValue(), 0.0);
        assertEquals(3000, v.getMaximumValue());
        assertEquals(3, v.getMinimumValue());
        assertEquals(15, v.getCount());
        assertEquals(15, v.getConcurrentCount());
        assertEquals(0, v.getErrorCount());

        assertArrayEquals(
                          ex, 
                          v.getValues(), 0.0);
    }

    @Test
    public final void updateValue_MultipleValues_1_Reversed()
    {
        int[] in =
            {
                3, 11, 37, 53, 73, 97, 101, 111, 137, 153, 173, 197, 1000, 2000, 3000
            };
        double[] ex =
            {
                0.0, 32.0, 64.0, 96.0, 128.0, 160.0, 192.0, 992.0, 1984.0, 2976.0
            };

        IntTimeSeriesEntry v = new IntTimeSeriesEntry(in[in.length - 1], false);
        for (int i = in.length  - 2; i >= 0; i--)
        {
            v.updateValue(in[i], false);
        }

        assertEquals(7146, v.getTotalValue());
        assertEquals(7146 / 15, v.getAverageValue(), 0.0);
        assertEquals(3000, v.getMaximumValue());
        assertEquals(3, v.getMinimumValue());
        assertEquals(15, v.getCount());
        assertEquals(15, v.getConcurrentCount());
        assertEquals(0, v.getErrorCount());

        assertArrayEquals(
                          ex, 
                          v.getValues(), 0.0);
    }

    @Test
    public final void merge_SameScale_Unscaled()
    {
        {
            final IntTimeSeriesEntry v0 = new IntTimeSeriesEntry(10, false);
            v0.updateValue(50, true);
            final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry(20, false);
            v1.updateValue(100, false);

            v0.merge(v1);

            assertEquals(180, v0.getTotalValue());
            assertEquals(180 / 4, v0.getAverageValue(), 0.0);
            assertEquals(100, v0.getMaximumValue());
            assertEquals(10, v0.getMinimumValue());
            assertEquals(4, v0.getCount());
            // important, this does not add up, the max rules!
            assertEquals(2, v0.getConcurrentCount());
            assertEquals(1, v0.getErrorCount());

            assertArrayEquals(
                              new double[]{
                                  10.0, 20.0, 50.0, 100.0}, 
                              v0.getValues(), 0.0);
        }
        {
            // max concurrent is from v0
            final IntTimeSeriesEntry v0 = new IntTimeSeriesEntry(10, false);
            v0.updateValue(20, false);
            v0.updateValue(30, false);
            v0.updateValue(40, true);
            final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry(10, false);
            v1.updateValue(30, true);

            v0.merge(v1);

            assertEquals(140, v0.getTotalValue());
            assertEquals(140 / 6, v0.getAverageValue(), 0.0);
            assertEquals(40, v0.getMaximumValue());
            assertEquals(10, v0.getMinimumValue());
            assertEquals(6, v0.getCount());
            // important, this does not add up, the max rules!
            assertEquals(4, v0.getConcurrentCount());
            assertEquals(2, v0.getErrorCount());

            assertArrayEquals(
                              new double[]{
                                  10.0, 20.0, 30.0, 40.0}, 
                              v0.getValues(), 0.0);
        }
        {
            // max concurrent is from v1
            final IntTimeSeriesEntry v0 = new IntTimeSeriesEntry(10, false);
            v0.updateValue(20, false);
            final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry(10, false);
            v1.updateValue(30, true);
            v1.updateValue(30, false);
            v1.updateValue(40, true);

            v0.merge(v1);

            assertEquals(140, v0.getTotalValue());
            assertEquals(140 / 6, v0.getAverageValue(), 0.0);
            assertEquals(40, v0.getMaximumValue());
            assertEquals(10, v0.getMinimumValue());
            assertEquals(6, v0.getCount());
            // important, this does not add up, the max rules!
            assertEquals(4, v0.getConcurrentCount());
            assertEquals(2, v0.getErrorCount());

            assertArrayEquals(
                              new double[]{
                                  10.0, 20.0, 30.0, 40.0}, 
                              v0.getValues(), 0.0);
        }
    }

    @Test
    public final void merge_SameScale_Scaled()
    {
        final IntTimeSeriesEntry v0 = new IntTimeSeriesEntry(10, false);
        v0.updateValue(200_000, false);
        v0.updateValue(200_000, false);

        final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry(10, false);
        v1.updateValue(200_000, false);
        v1.updateValue(200_000, false);

        v0.merge(v1);

        assertEquals(800_020, v0.getTotalValue());
        assertEquals(800_020 / 6, v0.getAverageValue(), 0.0);
        assertEquals(200_000, v0.getMaximumValue());
        assertEquals(10, v0.getMinimumValue());
        assertEquals(6, v0.getCount());
        assertEquals(3, v0.getConcurrentCount());
        assertEquals(0, v0.getErrorCount());

        assertArrayEquals(
                          new double[]{
                              0.0, 198656.0}, 
                          v0.getValues(), 0.0);
    }

    @Test
    public final void merge_SameScale_Scaled_WithErrorCount()
    {
        final IntTimeSeriesEntry v0 = new IntTimeSeriesEntry(10, false);
        v0.updateValue(200_000, true);
        v0.updateValue(200_000, false);

        final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry(10, false);
        v1.updateValue(200_000, true);
        v1.updateValue(200_000, false);
        v1.updateValue(190_000, false);

        v0.merge(v1);

        assertEquals(990_020, v0.getTotalValue());
        assertEquals(990_020 / 7, v0.getAverageValue(), 0.0);
        assertEquals(200_000, v0.getMaximumValue());
        assertEquals(10, v0.getMinimumValue());
        assertEquals(7, v0.getCount());
        assertEquals(4, v0.getConcurrentCount());
        assertEquals(2, v0.getErrorCount());

        assertArrayEquals(
                          new double[]{
                              0.0, 188416.0, 198656.0}, 
                          v0.getValues(), 0.0);
    }

    @Test
    public final void merge_TypicalRuntimes_DifferentScale_FirstScales()
    {
        // 100.0, 134.0, 250.0
        final IntTimeSeriesEntry v0 = new IntTimeSeriesEntry(100, false);
        v0.updateValue(250, false);
        v0.updateValue(135, false);

        // 128.0, 1968.0, 2000.0
        final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry(130, false);
        v1.updateValue(2000, false);
        v1.updateValue(1983, false);

        // outcome: 96.0, 128.0, 240.0, 1968.0, 2000.0
        final IntTimeSeriesEntry e = new IntTimeSeriesEntry(100, false);
        e.updateValue(250, false);
        e.updateValue(135, false);
        e.updateValue(130, false);
        e.updateValue(2000, false);
        e.updateValue(1983, false);

        v0.merge(v1);

        // this won't work because of distinct value compression
        // assertTrue(e.equals(v0));
        // assertTrue(v0.equals(e));
        double[] ex =
            {
                96.0, 128.0, 1024.0, 1136.0, 1968.0, 2000.0
            };
        assertEquals(4598, v0.getTotalValue());
        assertEquals(6, v0.getCount());
        assertEquals(4598 / 6, v0.getAverageValue(), 0.0);
        assertEquals(2000, v0.getMaximumValue());
        assertEquals(100, v0.getMinimumValue());
        assertEquals(0, v0.getErrorCount());

        assertArrayEquals(
                          ex, 
                          v0.getValues(), 0.0);
    }

    @Test
    public final void merge_TypicalRuntimes_DifferentScale_SecondScales()
    {
        // 100.0, 134.0, 250.0
        // 128.0, 1968.0, 2000.0
        final IntTimeSeriesEntry v0 = new IntTimeSeriesEntry(130, false);
        v0.updateValue(2000, false);
        v0.updateValue(1983, false);

        final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry(100, false);
        v1.updateValue(250, false);
        v1.updateValue(135, false);

        // outcome: 96.0, 128.0, 240.0, 1968.0, 2000.0
        final IntTimeSeriesEntry e = new IntTimeSeriesEntry(100, false);
        e.updateValue(250, false);
        e.updateValue(135, false);
        e.updateValue(130, false);
        e.updateValue(2000, false);
        e.updateValue(1983, false);

        v0.merge(v1);

        // this won't work because of distinct value compression
        // assertTrue(e.equals(v0));
        // assertTrue(v0.equals(e));
        double[] ex =
            {
                96.0, 128.0, 1024.0, 1136.0, 1968.0, 2000.0
            };
        assertEquals(4598, v0.getTotalValue());
        assertEquals(6, v0.getCount());
        assertEquals(4598 / 6, v0.getAverageValue(), 0.0);
        assertEquals(2000, v0.getMaximumValue());
        assertEquals(100, v0.getMinimumValue());
        assertEquals(0, v0.getErrorCount());

        assertArrayEquals(
                          ex, 
                          v0.getValues(), 0.0);
    }

    @Test
    public final void merge_ErrorCount()
    {
        // 100.0, 134.0, 250.0
        // 128.0, 1968.0, 2000.0
        final IntTimeSeriesEntry v0 = new IntTimeSeriesEntry(10, false);
        v0.updateValue(100, true);

        final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry(20, false);
        v1.updateValue(110, false);
        v1.updateValue(120, true);

        v0.merge(v1);

        // this won't work because of distinct value compression
        // assertTrue(e.equals(v0));
        // assertTrue(v0.equals(e));
        double[] ex =
            {
                10.0, 20.0, 100.0, 110.0, 120.0
            };
        assertEquals(360, v0.getTotalValue());
        assertEquals(5, v0.getCount());
        assertEquals(360 / 5, v0.getAverageValue(), 0.0);
        assertEquals(120, v0.getMaximumValue());
        assertEquals(10, v0.getMinimumValue());
        assertEquals(2, v0.getErrorCount());

        assertArrayEquals(
                          ex, 
                          v0.getValues(), 0.0);
    }

    @Test
    public final void _toString()
    {
        final IntTimeSeriesEntry v = new IntTimeSeriesEntry(88, false);
        assertEquals("1 / 1 / 0 / 88 / 88 / 88 / 88 / [88.0]\n", v.toString());

        v.updateValue(12, true);
        assertEquals("2 / 2 / 1 / 100 / 50 / 12 / 88 / [12.0, 88.0]\n", v.toString());
    }

    @Test
    public final void equals_EdgeCases()
    {
        final IntTimeSeriesEntry v = new IntTimeSeriesEntry(76210, false);

        assertFalse(v.equals(null));
        assertFalse(v.equals("Foo"));
        assertTrue(v.equals(v));
    }

    @Test
    public final void equals_Identical()
    {
        {
            // two entries with same initial value but no updates
            final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry(76210, false);
            final IntTimeSeriesEntry v2 = new IntTimeSeriesEntry(76210, false);

            assertTrue(v1.equals(v2));
            assertTrue(v2.equals(v1));
        }
        {
            // two entries with same initial value and same updates
            final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry(76210, false);
            v1.updateValue(123, false);
            v1.updateValue(322, false);

            final IntTimeSeriesEntry v2 = new IntTimeSeriesEntry(76210, false);
            v2.updateValue(322, false);
            v2.updateValue(123, false);

            assertTrue(v1.equals(v2));
            assertTrue(v2.equals(v1));
        }
    }   

    @Test
    public final void equals_DifferentCount()
    {
        final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry(10, false);
        v1.updateValue(123, false);
        v1.updateValue(123, false);
        v1.updateValue(322, false);

        final IntTimeSeriesEntry v2 = new IntTimeSeriesEntry(10, false);
        v2.updateValue(323, false);
        v2.updateValue(123, false);

        assertFalse(v1.equals(v2));
        assertFalse(v2.equals(v1));
    } 

    @Test
    public final void equals_DifferentConcurrentCount()
    {
        final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry(10, false);
        v1.updateValue(323, false);
        v1.updateValue(123, false);

        final IntTimeSeriesEntry v2 = new IntTimeSeriesEntry(10, false);
        v2.updateValue(323, false);
        v2.updateValue(123, false);
        v2.updateConcurrency();

        assertFalse(v1.equals(v2));
        assertFalse(v2.equals(v1));
    }

    @Test
    public final void equals_DifferentTotal()
    {
        final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry(10, false);
        v1.updateValue(123, false);
        v1.updateValue(123, false);

        final IntTimeSeriesEntry v2 = new IntTimeSeriesEntry(10, false);
        v2.updateValue(122, false);
        v2.updateValue(123, false);

        assertFalse(v1.equals(v2));
        assertFalse(v2.equals(v1));
    } 

    @Test
    public final void equals_DifferentErrorCount()
    {
        final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry(10, false);
        v1.updateValue(123, false);
        v1.updateValue(123, true);

        final IntTimeSeriesEntry v2 = new IntTimeSeriesEntry(10, false);
        v2.updateValue(123, false);
        v2.updateValue(123, false);

        assertFalse(v1.equals(v2));
        assertFalse(v2.equals(v1));
    } 

    @Test
    public final void equals_DifferentLowerBits()
    {
        final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry(1000, false);
        v1.updateValue(2000, false);
        v1.updateValue(2000, false);
        v1.updateValue(3000, false);

        final IntTimeSeriesEntry v2 = new IntTimeSeriesEntry(1000, false);
        v2.updateValue(1000, false);
        v2.updateValue(3000, false);
        v2.updateValue(3000, false);

        assertFalse(v1.equals(v2));
        assertFalse(v2.equals(v1));
    } 

    @Test
    public final void equals_DifferentHigherBits()
    {
        final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry(1000, false);
        v1.updateValue(1500, false);
        v1.updateValue(39100, false);
        v1.updateValue(39900, false);
        v1.updateValue(40000, false);

        final IntTimeSeriesEntry v2 = new IntTimeSeriesEntry(1000, false);
        v2.updateValue(1500, false);
        v2.updateValue(39500, false);
        v2.updateValue(39500, false);
        v2.updateValue(40000, false);

        assertFalse(v1.equals(v2));
        assertFalse(v2.equals(v1));
    } 

    @Test
    public final void equals_DifferentScale()
    {
        // cannot find any test case that produces different scale but 
        // everything else is the same
    } 

    @Test
    public final void equals_DifferentMinimum()
    {
        final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry(11, false);
        v1.updateValue(123, false);
        v1.updateValue(322, false);

        final IntTimeSeriesEntry v2 = new IntTimeSeriesEntry(10, false);
        v2.updateValue(322, false);
        v2.updateValue(123, false);

        assertFalse(v1.equals(v2));
        assertFalse(v2.equals(v1));
    } 

    @Test
    public final void equals_DifferentMaximum()
    {
        final IntTimeSeriesEntry v1 = new IntTimeSeriesEntry(10, false);
        v1.updateValue(123, false);
        v1.updateValue(322, false);

        final IntTimeSeriesEntry v2 = new IntTimeSeriesEntry(10, false);
        v2.updateValue(323, false);
        v2.updateValue(123, false);

        assertFalse(v1.equals(v2));
        assertFalse(v2.equals(v1));
    } 
}
