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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

/**
 * Tests the {@link IntTimeSeries} class.
 */
public class IntTimeSeriesTest
{
    // 1.1.2024 00:00:00 in seconds since epoch
    private static final long BASE_TIME_MSEC = 1_704_067_200_000l;
    private static final int BASE_TIME_SEC = (int)(BASE_TIME_MSEC / 1000);

    private static long msec(int offsetSecond)
    {
        return BASE_TIME_MSEC + offsetSecond * 1000;
    }
    private static int sec(int offsetSecond)
    {
        return BASE_TIME_SEC + offsetSecond;
    }
    private static int toPos(int sec, int slotWidth)
    {
        return sec / slotWidth;
    }

    private void checkEmpty(IntTimeSeriesEntry[] values, int from, int to)
    {
        for (int i = from; i < to; i++)
        {
            assertEquals(String.format("Pos %d incorrect", i), 0, values[i].getCount());
        }
    }

    /**
     * Ensure power of two sizes.
     */
    @Test
    public void testSizePowerOfTwo()
    {
        var series = new IntTimeSeries(10);
        assertEquals(16, series.getSize());

        series = new IntTimeSeries(1000);
        assertEquals(1024, series.getSize());

        series = new IntTimeSeries(2048);
        assertEquals(2048, series.getSize());

        series = new IntTimeSeries(3000);
        assertEquals(4096, series.getSize());

        // default
        series = new IntTimeSeries();
        assertEquals(4096, series.getSize());
    }

    // simple test without growth
    @Test
    public void testSimple_0()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);
        series.addValue(BASE_TIME_MSEC, 10, false);

        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC, series.getLastSecond());
        assertEquals(1, series.getCount());
        assertEquals(0, series.getErrorCount());
        assertEquals(1, series.getScale());
        assertEquals(1, series.getSlotWidth());

        assertEquals(1, series.getValues()[0].getCount());
        checkEmpty(series.getValues(), 1, 3600);
    }

    // simple test without growth
    @Test
    public void testSimple_0_errorCount()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);
        series.addValue(BASE_TIME_MSEC, 10, true);

        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC, series.getLastSecond());
        assertEquals(1, series.getCount());
        assertEquals(1, series.getErrorCount());
        assertEquals(1, series.getScale());
        assertEquals(1, series.getSlotWidth());

        assertEquals(1, series.getValues()[0].getCount());
        checkEmpty(series.getValues(), 1, 3600);
    }
    
    // pos 0, 0, 1
    @Test
    public void testSimple_1()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);
        series.addValue(BASE_TIME_MSEC, 10, false);
        series.addValue(BASE_TIME_MSEC + 1000, 20, false);
        series.addValue(BASE_TIME_MSEC, 100, false);

        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC + 1, series.getLastSecond());
        assertEquals(3, series.getCount());
        assertEquals(0, series.getErrorCount());
        assertEquals(1, series.getScale());
        assertEquals(1, series.getSlotWidth());

        assertEquals(2, series.getValues()[0].getCount());
        assertEquals(1, series.getValues()[1].getCount());
        checkEmpty(series.getValues(), 2, 3600);
    }

    // pos 0, 3599, 0, 3599
    @Test
    public void testSimple_2()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);
        series.addValue(msec(0), 10, false);
        series.addValue(msec(3599), 20, true);
        series.addValue(msec(0), 100, false);
        series.addValue(msec(3599), 20, true);

        assertEquals(sec(0), series.getFirstSecond());
        assertEquals(sec(3599), series.getLastSecond());
        assertEquals(4, series.getCount());
        assertEquals(2, series.getErrorCount());
        assertEquals(1, series.getScale());
        assertEquals(1, series.getSlotWidth());

        assertEquals(2, series.getValues()[0].getCount());
        assertEquals(2, series.getValues()[3599].getCount());
        checkEmpty(series.getValues(), 1, 3599);
    }

    // pos 0, -1, 0, -1, enough room to move right without condensing
    @Test
    public void testMoveRight_1()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);
        series.addValue(msec(0), 10, false);
        series.addValue(msec(-1), 20, false);
        series.addValue(msec(0), 11, false);
        series.addValue(msec(-1), 21, false);
        series.addValue(msec(1), 42, false);

        assertEquals(sec(-1), series.getFirstSecond());
        assertEquals(sec(1), series.getLastSecond());
        assertEquals(5, series.getCount());
        assertEquals(0, series.getErrorCount());
        assertEquals(1, series.getScale());
        assertEquals(1, series.getSlotWidth());

        assertEquals(2, series.getValues()[0].getCount());
        assertEquals(2, series.getValues()[1].getCount());
        assertEquals(1, series.getValues()[2].getCount());

        assertEquals(41, series.getValues()[0].getTotalValue());
        assertEquals(21, series.getValues()[1].getTotalValue());
        assertEquals(42, series.getValues()[2].getTotalValue());

        checkEmpty(series.getValues(), 3, 3600);
    }

    // move right but we don't have to condense despite values 
    // being present at the right edge, edge case of before
    // pos 0, 1, 1799, -1, 3598
    @Test
    public void testMoveRight_2()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);
        series.addValue(msec(0), 10, false);
        series.addValue(msec(1), 20, false);
        series.addValue(msec(1799), 11, false);
        series.addValue(msec(3598), 21, false);
        series.addValue(msec(-1), 420, false);

        assertEquals(sec(-1), series.getFirstSecond());
        assertEquals(sec(3598), series.getLastSecond());
        assertEquals(5, series.getCount());
        assertEquals(1, series.getScale());
        assertEquals(1, series.getSlotWidth());

        assertEquals(1, series.getValues()[0].getCount());
        assertEquals(1, series.getValues()[1].getCount());
        assertEquals(1, series.getValues()[2].getCount());
        assertEquals(1, series.getValues()[1800].getCount());
        assertEquals(1, series.getValues()[3599].getCount());

        assertEquals(420, series.getValues()[0].getTotalValue());
        assertEquals(10, series.getValues()[1].getTotalValue());
        assertEquals(20, series.getValues()[2].getTotalValue());
        assertEquals(11, series.getValues()[1800].getTotalValue());
        assertEquals(21, series.getValues()[3599].getTotalValue());

        checkEmpty(series.getValues(), 3, 1800);
        checkEmpty(series.getValues(), 1801, 3599);
    }

    // move right but right is occupied so we have to condense
    // 0, 1, 2, 3, 15, -1
    @Test
    public void testMoveRightCondense_1()
    {
        final IntTimeSeries series = new IntTimeSeries(16);
        series.addValue(msec(0), 10, false);
        series.addValue(msec(1), 11, false);
        series.addValue(msec(2), 23, false);
        series.addValue(msec(3), 24, false);
        series.addValue(msec(15), 100, false);
        series.addValue(msec(-1), 77, false);

        assertEquals(sec(-1), series.getFirstSecond());
        assertEquals(sec(15), series.getLastSecond());
        assertEquals(6, series.getCount());
        assertEquals(2, series.getScale());
        assertEquals(2, series.getSlotWidth());

        // -1, moved by 1
        assertEquals(1, series.getValues()[0].getCount());
        // 0 and 1, moved by 1
        assertEquals(2, series.getValues()[1].getCount());
        // 2 and 3, moved by 1
        assertEquals(2, series.getValues()[2].getCount());
        // 3599, moved by 1
        assertEquals(1, series.getValues()[toPos(15 + 1, 2)].getCount());

        assertEquals(77, series.getValues()[0].getTotalValue());
        assertEquals(21, series.getValues()[1].getTotalValue());
        assertEquals(47, series.getValues()[2].getTotalValue());
        assertEquals(100, series.getValues()[toPos(15 + 1, 2)].getTotalValue());

        checkEmpty(series.getValues(), 3, toPos(15 + 1, 2));
        checkEmpty(series.getValues(), toPos(15 + 1, 2) + 1, 16);
    }

    // move right but because we have values already, we must condense
    // pos 0 to 3599, -7301 
    @Test
    public void testMoveRightCondense_2()
    {
        final IntTimeSeries series = new IntTimeSeries(16);
        for (int i = 0; i < 16; i++)
        {
            series.addValue(msec(i), i + 1, true);
        }

        assertEquals(sec(0), series.getFirstSecond());
        assertEquals(sec(15), series.getLastSecond());
        assertEquals(16, series.getCount());
        assertEquals(16, series.getErrorCount());
        assertEquals(1, series.getScale());

        var v = series.getValues();
        for (int i = 0; i < 16; i++)
        {
            assertEquals(1, v[i].getCount());
            assertEquals(1, v[i].getErrorCount());
            assertEquals(i + 1, v[i].getTotalValue());

        }

        // now move right by -20 seconds
        series.addValue(msec(-21), 999, false);
    }

    // condense 2x, because we got new values larger than size
    // 0, 1, 2, 3, 3599, 3600, 14400
    // scale 1  0 - 3600
    // scale 2  0 - 7200
    // scale 3  0 - 14400
    @Test
    public void testCondense_1()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);
        series.addValue(msec(0), 10, false);
        series.addValue(msec(1), 11, false);
        series.addValue(msec(2), 23, false);
        series.addValue(msec(3), 24, false);
        series.addValue(msec(3599), 100, false);
        series.addValue(msec(3600), 77, false);
        series.addValue(msec(14000), 123, false);

        assertEquals(sec(0), series.getFirstSecond());
        assertEquals(sec(14000), series.getLastSecond());
        assertEquals(7, series.getCount());
        assertEquals(3, series.getScale());
        assertEquals(4, series.getSlotWidth());

        assertEquals(4, series.getValues()[toPos(0, 4)].getCount());
        assertEquals(1, series.getValues()[toPos(3599, 4)].getCount());
        assertEquals(1, series.getValues()[toPos(3600, 4)].getCount());
        assertEquals(1, series.getValues()[toPos(14000, 4)].getCount());

        assertEquals(10 + 11 + 23 + 24, series.getValues()[toPos(0, 4)].getTotalValue());
        assertEquals(100, series.getValues()[toPos(3599, 4)].getTotalValue());
        assertEquals(77, series.getValues()[toPos(3600, 4)].getTotalValue());
        assertEquals(123, series.getValues()[toPos(14000, 4)].getTotalValue());

        checkEmpty(series.getValues(), 1, toPos(3599, 4));
        // four wide aka 3600 is not 3600 to 3603
        checkEmpty(series.getValues(), toPos(3604, 4), toPos(14000, 4));
        checkEmpty(series.getValues(), toPos(14004, 4), toPos(14400, 4));
    }

    // simulate an entire day of data with one value each second
    @Test
    public void testFullDay()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);

        // we have three agents with measurements but they typically arrive
        // not in sorted order but each separately 
        var totalSeconds = 86400;
        for (int i = 0; i < totalSeconds; i++)
        {
            series.addValue(msec(i), 100 * (i / 3600) + 100, false);
        }
        for (int i = 0; i < totalSeconds; i++)
        {
            series.addValue(msec(i), 100 * (i / 3600) + 200, false);
        }
        for (int i = 0; i < totalSeconds; i++)
        {
            series.addValue(msec(i), 100 * (i / 3600) + 300, false);
        }

        assertEquals(sec(0), series.getFirstSecond());
        // the last second is the start of the slot second and not the end
        assertEquals(sec(totalSeconds) - series.getSlotWidth(), series.getLastSecond());
        assertEquals(3 * totalSeconds, series.getCount());
        assertEquals(6, series.getScale());

        // the size 
        assertEquals(IntTimeSeries.nextHighestPowerOfTwo(86400 / 32), series.getValues().length);

        // 3 series, each second once, 32 aka scale 5 seconds per slot 
        assertEquals(3 * 32, series.getValues()[toPos(0, 32)].getCount());
        assertEquals(3 * 32, series.getValues()[toPos(1234, 32)].getCount());
        assertEquals(3 * 32, series.getValues()[toPos(4321, 32)].getCount());
        assertEquals(3 * 32, series.getValues()[toPos(86399, 32)].getCount());

        assertEquals(32 * 100 + 32 * 200 + 32 * 300, 
                     series.getValues()[toPos(0, 32)].getTotalValue());

        // 3600 is the first hour but because the slots fall together towards the smaller
        // seconds, we get data from hour 0 in, hence we offset by a slots
        assertEquals(32 * (100 + 100) + 32 * (100 + 200) + 32 * (100 + 300), 
                     series.getValues()[toPos(3600 + 32, 32)].getTotalValue());
        assertEquals(32 * (200 + 100) + 32 * (200 + 200) + 32 * (200 + 300), 
                     series.getValues()[toPos(7200 + 32, 32)].getTotalValue());
    }

    // test the digest function for percentiles
    @Test
    public void testDigestPercentiles_Simple_Uncompressed()
    {
        // default precision is 8 
        final IntTimeSeries series = new IntTimeSeries(3600);

        series.addValue(msec(1000), 10, false);
        series.addValue(msec(2000), 20, false);
        series.addValue(msec(3000), 30, false);

        assertEquals(10 >> 3 << 3, series.getPercentile(0.0));
        assertEquals(20 >> 3 << 3, series.getPercentile(50.0));
        assertEquals(30 >> 3 << 3, series.getPercentile(75.0));
        assertEquals(30 >> 3 << 3, series.getPercentile(100.0));
    }
    
    // test the digest function for percentiles
    @Test
    public void testDigestPercentiles_Simple_Compressed()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);

        series.addValue(msec(0), 10, false); // 10 >> 3 << 3 = 8
        series.addValue(msec(10), 20, false); // 20 >> 3 << 3 = 16
        series.addValue(msec(20), 30, false); // 30 >> 3 << 3 = 24

        assertEquals(8, series.getPercentile(0.0)); // 10 >> 3 << 3 = 8
        assertEquals(8, series.getPercentile(25.0)); // 10 >> 3 << 3 = 8
        assertEquals(16, series.getPercentile(50.0)); // 20 >> 3 << 3 = 16
        assertEquals(24, series.getPercentile(75.0)); // 30 >> 3 << 3 = 24
        assertEquals(24, series.getPercentile(100.0)); // 30 >> 3 << 3 = 24
    }

    // test the digest function for percentiles
    @Test
    public void testDigestPercentiles_Full()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);

        for (int i = 0; i < 100; i++)
        {
            series.addValue(msec(100 * i), i, false);
        }

        assertEquals(0, series.getPercentile(0.0));
        assertEquals(16, series.getPercentile(20.0));
        assertEquals(24, series.getPercentile(25.0));
        assertEquals(48, series.getPercentile(50.0));
        assertEquals(64, series.getPercentile(70.0));
        assertEquals(72, series.getPercentile(75.0));
        assertEquals(96, series.getPercentile(100.0));
    }

    // test the digest function for percentiles with very equally distributed values
    @Test
    public void testDigestPercentiles_EquallyDistValues()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);

        series.addValue(msec(200), 0, false); // 0 >> 3 << 3 = 0
        series.addValue(msec(200), 100, false); // 100 >> 3 << 3 = 96
        series.addValue(msec(300), 200, false); // 200 >> 3 << 3 = 192
        series.addValue(msec(400), 300, false); // 300 >> 3 << 3 = 256
        series.addValue(msec(500), 400, false); // 400 >> 3 << 3 = 512
        series.addValue(msec(600), 500, false); // 500 >> 3 << 3 = 496
        series.addValue(msec(700), 600, false); // 600 >> 3 << 3 = 592
        series.addValue(msec(700), 700, false); // 700 >> 3 << 3 = 696
        series.addValue(msec(700), 800, false); // 800 >> 3 << 3 = 768
        series.addValue(msec(700), 900, false); // 900 >> 3 << 3 = 896
        series.addValue(msec(800), 1000, false); // 1000 >> 3 << 3 = 992

        assertEquals(0 >> 3 << 3, series.getPercentile(0.0));
        assertEquals(100 >> 3 << 3, series.getPercentile(10.0));
        assertEquals(200 >> 3 << 3, series.getPercentile(20.0));
        assertEquals(300 >> 3 << 3, series.getPercentile(30.0));
        assertEquals(400 >> 3 << 3, series.getPercentile(40.0));
        assertEquals(500 >> 3 << 3, series.getPercentile(50.0));
        assertEquals(600 >> 3 << 3, series.getPercentile(60.0));
        assertEquals(700 >> 3 << 3, series.getPercentile(70.0));
        assertEquals(800 >> 3 << 3, series.getPercentile(80.0));
        assertEquals(900 >> 3 << 3, series.getPercentile(90.0));
        assertEquals(1000 >> 3 << 3, series.getPercentile(100.0));
    }
    
    // test the digest function for percentiles with very equally distributed values
    @Test
    public void testDigestPercentiles_NP_AlwaysFull()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);

        series.addValue(msec(200), 100, false); // 100 >> 3 << 3 = 96
        series.addValue(msec(300), 200, false); // 200 >> 3 << 3 = 192
        series.addValue(msec(400), 300, false); // 300 >> 3 << 3 = 256
        series.addValue(msec(500), 400, false); // 400 >> 3 << 3 = 512
        series.addValue(msec(600), 500, false); // 500 >> 3 << 3 = 496
        series.addValue(msec(700), 600, false); // 600 >> 3 << 3 = 592
        series.addValue(msec(700), 700, false); // 700 >> 3 << 3 = 696
        series.addValue(msec(700), 800, false); // 800 >> 3 << 3 = 768
        series.addValue(msec(700), 900, false); // 900 >> 3 << 3 = 896
        series.addValue(msec(800), 1000, false); // 1000 >> 3 << 3 = 992

        assertEquals((100 >> 3 << 3), series.getPercentile(0.0));
        assertEquals(((100 >> 3 << 3) + (200 >> 3 << 3)) / 2, series.getPercentile(10.0));
        assertEquals(((200 >> 3 << 3) + (300 >> 3 << 3)) / 2, series.getPercentile(20.0));
        assertEquals(((300 >> 3 << 3) + (400 >> 3 << 3)) / 2, series.getPercentile(30.0));
        assertEquals(((400 >> 3 << 3) + (500 >> 3 << 3)) / 2, series.getPercentile(40.0));
        assertEquals(((500 >> 3 << 3) + (600 >> 3 << 3)) / 2, series.getPercentile(50.0));
        assertEquals(((600 >> 3 << 3) + (700 >> 3 << 3)) / 2, series.getPercentile(60.0));
        assertEquals(((700 >> 3 << 3) + (800 >> 3 << 3)) / 2, series.getPercentile(70.0));
        assertEquals(((800 >> 3 << 3) + (900 >> 3 << 3)) / 2, series.getPercentile(80.0));
        assertEquals(((900 >> 3 << 3) + (1000 >> 3 << 3)) / 2, series.getPercentile(90.0));
        assertEquals(1000 >> 3 << 3, series.getPercentile(100.0));
    }
    
    // test histogram when empty
    @Test
    public void testHistogramEmpty()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);
        var h = series.toHistogram(10);
        assertEquals(0, series.toHistogram(10).size());
    }
    
    // test the histogram function 
    @Test
    public void testHistogramSimple()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);

        series.addValue(msec(0), 0, false);
        series.addValue(msec(1000), 100, false);
        series.addValue(msec(2000), 200, false);
        series.addValue(msec(3000), 300, false);
        series.addValue(msec(4000), 400, false);
        series.addValue(msec(5000), 500, false);
        series.addValue(msec(6000), 600, false);
        series.addValue(msec(7000), 700, false);
        series.addValue(msec(8000), 800, false);
        series.addValue(msec(9000), 900, false);
        series.addValue(msec(10000), 1000, false);

        var h = series.toHistogram(10);
        assertEquals(10, h.size());
        
        // ok, because we size the buckets based on values width and not based on the
        // actual buckets, we might get more data points than expected
        assertEquals(16, h.stream().mapToLong(b -> b.count()).sum());

        assertEquals("0, 99, 2", h.get(0).toString()); // 0 >> 3 = 0, 99 >> 3 = 12
        assertEquals("100, 199, 1", h.get(1).toString()); // 100 >> 3 = 12, 199 >> 3 = 24
        assertEquals("200, 299, 2", h.get(2).toString()); // 200 >> 3 = 24, 299 >> 3 = 36
        assertEquals("300, 399, 1", h.get(3).toString()); // 300 >> 3 = 36, 399 >> 3 = 49
        assertEquals("400, 499, 2", h.get(4).toString()); // 400 >> 3 = 49, 499 >> 3 = 61
        assertEquals("500, 599, 1", h.get(5).toString()); // 500 >> 3 = 62, 599 >> 3 = 74
        assertEquals("600, 699, 2", h.get(6).toString()); // 600 >> 3 = 75, 699 >> 3 = 87
        assertEquals("700, 799, 1", h.get(7).toString()); // 700 >> 3 = 87, 799 >> 3 = 99
        assertEquals("800, 899, 2", h.get(8).toString()); // 800 >> 3 = 100, 899 >> 3 = 112
        assertEquals("900, 1000, 2", h.get(9).toString()); // 900 >> 3 = 112, 1000 >> 3 = 125
    }

    /**
     * Test the concurrent count handling without growth
     */
    @Test
    public void concurrentCountSimple_NotCrossing() 
    {
        final IntTimeSeries series = new IntTimeSeries(3600);
        series.addValue(BASE_TIME_MSEC, BASE_TIME_MSEC + 500, 10, true);
        series.addValue(BASE_TIME_MSEC, BASE_TIME_MSEC + 500, 20, true);

        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC, series.getLastSecond());

        assertEquals(2, series.getValues()[0].getCount());
        assertEquals(2, series.getValues()[0].getConcurrentCount());
        // neighbor is empty
        assertEquals(0, series.getValues()[1].getConcurrentCount());
    }
    
    /**
     * Test the concurrent count handling without growth
     */
    @Test
    public void concurrentCountSimple_CrossingMinimal() 
    {
        final IntTimeSeries series = new IntTimeSeries(3600);
        series.addValue(BASE_TIME_MSEC, BASE_TIME_MSEC + 500, 10, true);
        series.addValue(BASE_TIME_MSEC, BASE_TIME_MSEC + 999, 10, true);
        series.addValue(BASE_TIME_MSEC, BASE_TIME_MSEC + 1000, 20, true);

        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC + 1, series.getLastSecond());

        // First two operations finished within second 0 (+500ms and +999ms).
        // Third operation finished at +1000ms (second 1).
        assertEquals(2, series.getValues()[0].getCount());
        assertEquals(3, series.getValues()[0].getConcurrentCount());

        assertEquals(1, series.getValues()[1].getCount());
        assertEquals(1, series.getValues()[1].getConcurrentCount());

        // neighbor is empty
        assertEquals(0, series.getValues()[2].getConcurrentCount());
    }

    /**
     * Test the concurrent count handling without growth crossing slot boundary
     */
    @Test
    public void concurrentCountSimple_Crossing() 
    {
        final IntTimeSeries series = new IntTimeSeries(3600);
        series.addValue(BASE_TIME_MSEC, BASE_TIME_MSEC + 500, 10, true);

        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC, series.getLastSecond());

        // First operation: starts at 0ms, finishes at 500ms (sec 0)
        assertEquals(1, series.getValues()[0].getCount());
        assertEquals(1, series.getValues()[0].getConcurrentCount());

        assertEquals(0, series.getValues()[1].getCount());
        assertEquals(0, series.getValues()[1].getConcurrentCount());

        // Second operation: starts at 0ms (sec 0), finishes at 1500ms (sec 1)
        series.addValue(BASE_TIME_MSEC, BASE_TIME_MSEC + 1500, 20, true);

        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC + 1, series.getLastSecond());

        // sec 0: count is 1 (first op completed here), concurrent is 2 (both ops active)
        assertEquals(1, series.getValues()[0].getCount());
        assertEquals(2, series.getValues()[0].getConcurrentCount());

        // sec 1: count is 1 (second op completed here), concurrent is 1 (second op active)
        assertEquals(1, series.getValues()[1].getCount());
        assertEquals(1, series.getValues()[1].getConcurrentCount());

        assertEquals(0, series.getValues()[2].getCount());
        assertEquals(0, series.getValues()[2].getConcurrentCount());

        // Third operation: starts at 2000ms (sec 2), finishes at 3999ms (sec 3)
        series.addValue(BASE_TIME_MSEC + 2000, BASE_TIME_MSEC + 3999, 30, true);

        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC + 3, series.getLastSecond());

        assertEquals(1, series.getValues()[0].getCount());
        assertEquals(2, series.getValues()[0].getConcurrentCount());

        assertEquals(1, series.getValues()[1].getCount());
        assertEquals(1, series.getValues()[1].getConcurrentCount());

        // sec 2: op 3 active, but not completed yet
        assertEquals(0, series.getValues()[2].getCount());
        assertEquals(1, series.getValues()[2].getConcurrentCount());

        // sec 3: op 3 completed
        assertEquals(1, series.getValues()[3].getCount());
        assertEquals(1, series.getValues()[3].getConcurrentCount());

        assertEquals(0, series.getValues()[4].getCount());
        assertEquals(0, series.getValues()[4].getConcurrentCount());

        // Fourth operation: starts at 2000ms (sec 2), finishes at 4000ms (sec 4)
        series.addValue(BASE_TIME_MSEC + 2000, BASE_TIME_MSEC + 4000, 30, true);

        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC + 4, series.getLastSecond());

        assertEquals(1, series.getValues()[0].getCount());
        assertEquals(2, series.getValues()[0].getConcurrentCount());

        assertEquals(1, series.getValues()[1].getCount());
        assertEquals(1, series.getValues()[1].getConcurrentCount());

        // sec 2: op 3 and op 4 active
        assertEquals(0, series.getValues()[2].getCount());
        assertEquals(2, series.getValues()[2].getConcurrentCount());

        // sec 3: op 3 completed (count=1), op 3 and op 4 both active (concurrent=2)
        assertEquals(1, series.getValues()[3].getCount());
        assertEquals(2, series.getValues()[3].getConcurrentCount());

        // sec 4: op 4 completed (count=1), op 4 active (concurrent=1)
        assertEquals(1, series.getValues()[4].getCount());
        assertEquals(1, series.getValues()[4].getConcurrentCount());

        assertEquals(0, series.getValues()[5].getCount());
        assertEquals(0, series.getValues()[5].getConcurrentCount());
    }

    /**
     * Test the concurrent count handling with growth and crossing slot boundary
     */
    @Test
    public void concurrentCountGrowth_Crossing_StartPushes() 
    {
        final IntTimeSeries series = new IntTimeSeries(16);
        // starts at 0ms (sec 0), finishes at 1500ms (sec 1)
        series.addValue(BASE_TIME_MSEC, BASE_TIME_MSEC + 1500, 10, true);

        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC + 1, series.getLastSecond());

        // no growth yet
        // sec 0: active, but not completed yet
        assertEquals(0, series.getValues()[0].getCount());
        assertEquals(1, series.getValues()[0].getConcurrentCount());

        // sec 1: completed
        assertEquals(1, series.getValues()[1].getCount());
        assertEquals(1, series.getValues()[1].getConcurrentCount());

        // trigger growth but just being outside not by going over the end
        // starts at 16000ms (sec 16), finishes at 16500ms (sec 16)
        series.addValue(BASE_TIME_MSEC + 16000, 
                        BASE_TIME_MSEC + 16000 + 500, 10, true);

        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC + 16, series.getLastSecond());

        // sec 0 and 1 condensed into slot 0: total count is 0 + 1 = 1, concurrent count is max(1, 1) = 1
        assertEquals(1, series.getValues()[0].getCount());
        assertEquals(1, series.getValues()[0].getConcurrentCount());

        // sec 2 and 3
        assertEquals(0, series.getValues()[1].getCount());
        assertEquals(0, series.getValues()[1].getConcurrentCount());

        // sec 16 and 17 (slot 8): operation started and finished at sec 16
        assertEquals(1, series.getValues()[8].getCount());
        assertEquals(1, series.getValues()[8].getConcurrentCount());

        // sec 18 and 19
        assertEquals(0, series.getValues()[9].getCount());
        assertEquals(0, series.getValues()[9].getConcurrentCount());
    }

    /**
     * Test the concurrent count handling with growth and crossing slot boundary
     */
    @Test
    public void concurrentCountGrowth_Crossing_EndPushes() 
    {
        final IntTimeSeries series = new IntTimeSeries(16);
        // starts at 0ms (sec 0), finishes at 1500ms (sec 1)
        series.addValue(BASE_TIME_MSEC, BASE_TIME_MSEC + 1500, 10, true);

        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC + 1, series.getLastSecond());
        assertEquals(1, series.getScale());

        // no growth yet
        // sec 0: active, not completed
        assertEquals(0, series.getValues()[0].getCount());
        assertEquals(1, series.getValues()[0].getConcurrentCount());

        // sec 1: completed
        assertEquals(1, series.getValues()[1].getCount());
        assertEquals(1, series.getValues()[1].getConcurrentCount());

        // trigger growth by being beyond the end with the length
        // starts at 15500ms (sec 15), finishes at 17000ms (sec 17)
        series.addValue(BASE_TIME_MSEC + 15500, 
                        BASE_TIME_MSEC + 15500 + 1500, 10, true);

        assertEquals(2, series.getScale());
        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC + 16, series.getLastSecond());

        // sec 0 and 1 condensed into slot 0: total count is 0 + 1 = 1, concurrent count is max(1, 1) = 1
        assertEquals(1, series.getValues()[0].getCount());
        assertEquals(1, series.getValues()[0].getConcurrentCount());

        // sec 2 and 3
        assertEquals(0, series.getValues()[1].getCount());
        assertEquals(0, series.getValues()[1].getConcurrentCount());

        for (int i = 2; i < 7; i++)
        {
            assertEquals(0, series.getValues()[i].getCount());
            assertEquals(0, series.getValues()[i].getConcurrentCount());
        }
        
        // sec 14 and 15 (slot 7): operation active during sec 15, but completed in sec 17 (slot 8)
        assertEquals(0, series.getValues()[7].getCount());
        assertEquals(1, series.getValues()[7].getConcurrentCount());

        // sec 16 and 17 (slot 8): operation completed in sec 17
        assertEquals(1, series.getValues()[8].getCount());
        assertEquals(1, series.getValues()[8].getConcurrentCount());

        // rest
        for (int i = 9; i < series.getValues().length; i++)
        {
            assertEquals(0, series.getValues()[i].getCount());
            assertEquals(0, series.getValues()[i].getConcurrentCount());
        }
    }

    // test standard deviation calculation
    @Test
    public void testStandardDeviation_Empty()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);
        assertEquals(0.0, series.getStandardDeviation(), 0.0);
    }

    // test mean
    @Test
    public void testMean_Simple_Empty()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);
        assertEquals(0.0, series.getMean(), 0.0);
    }
    
    // test mean
    @Test
    public void testMean_Simple()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);
        series.addValue(msec(0), 10, false);
        series.addValue(msec(0), 20, false);
        series.addValue(msec(0), 30, false);

        // mean is (10 + 20 + 30) / 3 = 60 / 3 = 20
        assertEquals(20.0, series.getMean(), 0.0);
    }
    
    // test standard deviation calculation
    @Test
    public void testStandardDeviation_Simple()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);
        series.addValue(msec(0), 10, false);
        series.addValue(msec(0), 20, false);
        series.addValue(msec(0), 30, false);

        // mean is 20
        // variance is ((10-20)^2 + (20-20)^2 + (30-20)^2) / 3 = (100 + 0 + 100) / 3 = 66.666..
        // stddev is sqrt(variance) = sqrt(66.666...) = 8.16496580927726
        assertEquals(8.16496580927726, series.getStandardDeviation(), 0.000001);
    }
    
    @Test
    public void getFirstSecond_throwsExceptionWhenEmpty()
    {
        IntTimeSeries series = new IntTimeSeries();
        // Should throw because no values have been added yet
        assertThrows(IllegalStateException.class, series::getFirstSecond);
    }
    
    @Test
    public void getLastSecond_throwsExceptionWhenEmpty()
    {
        IntTimeSeries series = new IntTimeSeries();
        // Should throw because no values have been added yet
        assertThrows(IllegalStateException.class, series::getLastSecond);
    }

    @Test
    public void testTDigestDoubleArrayIndexOutOfBounds() 
    {
        IntTimeSeries series = new IntTimeSeries();
        var r = new Random(12345L);
        for (int i = 0; i < 100000; i++) 
        {
            series.addValue(100, r.nextInt(100_000_000), false);
        }
    }

    @Test
    public void testMergeEmpty()
    {
        final IntTimeSeries s1 = new IntTimeSeries();
        final IntTimeSeries s2 = new IntTimeSeries();
        s1.merge(s2);
        assertEquals(-1, s1.getLastPosUsed());

        s2.addValue(msec(10), 100, false);
        s1.merge(s2);
        assertEquals(s2.getFirstSecond(), s1.getFirstSecond());
        assertEquals(1, s1.getCount());
        assertEquals(100, s1.getTotalValue());
    }

    @Test
    public void testMergeOverlappingSameScale()
    {
        final IntTimeSeries s1 = new IntTimeSeries(64);
        final IntTimeSeries s2 = new IntTimeSeries(64);

        s1.addValue(msec(0), 100, false);
        s1.addValue(msec(5), 200, false);

        s2.addValue(msec(5), 300, true);
        s2.addValue(msec(10), 400, false);

        s1.merge(s2);

        assertEquals(4, s1.getCount());
        assertEquals(1, s1.getErrorCount());
        assertEquals(1000, s1.getTotalValue());
        assertEquals(250.0, s1.getMean(), 0.001);
        assertEquals(100, s1.getStatistics().minValue);
        assertEquals(400, s1.getStatistics().maxValue);
        assertEquals(2, s1.getValues()[5].getCount());
        assertEquals(1, s1.getValues()[5].getErrorCount());
    }

    @Test
    public void testMergeWithCondensing()
    {
        final IntTimeSeries s1 = new IntTimeSeries(16);
        final IntTimeSeries s2 = new IntTimeSeries(16);

        // s1 spans 0..10
        s1.addValue(msec(0), 50, false);
        s1.addValue(msec(10), 150, false);

        // s2 spans 20..30 (total span 0..30 > 16, requires condensing to scale 2)
        s2.addValue(msec(20), 200, false);
        s2.addValue(msec(30), 400, true);

        s1.merge(s2);

        assertTrue(s1.getScale() >= 2);
        assertEquals(4, s1.getCount());
        assertEquals(1, s1.getErrorCount());
        assertEquals(800, s1.getTotalValue());
    }

    @Test
    public void testToRunTimeTimeSeriesAndHistogram()
    {
        final IntTimeSeries s = new IntTimeSeries(64);
        s.addValue(msec(1), 100, false);
        s.addValue(msec(1), 200, false);
        s.addValue(msec(2), 300, true);

        final var rt = s.toRunTimeTimeSeries("Runtime");
        assertEquals(2, rt.getItemCount());

        final var countTs = s.toCountPerSecondTimeSeries("Count/s");
        assertEquals(2, countTs.getItemCount());

        final var errTs = s.toErrorsPerSecondTimeSeries("Errors/s");
        assertEquals(2, errTs.getItemCount());

        final var hist = s.toHistogramSeries("Distribution", 10);
        assertTrue(hist.getItemCount() > 0);
    }

    /**
     * Tests that IntTimeSeries dynamically expands its buffer when scale is 1 and
     * capacity is less than maxCapacity, avoiding loss of 1-second resolution.
     */
    @Test
    public void testDynamicGrowthUpToMaxCapacity()
    {
        final IntTimeSeries s = new IntTimeSeries(16, 64);
        assertEquals(16, s.getSize());
        assertEquals(64, s.getMaxCapacity());
        assertEquals(1, s.getScale());

        s.addValue(msec(0), 100, false);
        assertEquals(16, s.getSize());
        assertEquals(1, s.getScale());

        // Span 0..20 requires 21 slots > 16. Should grow to 32 while retaining scale 1!
        s.addValue(msec(20), 100, false);
        assertEquals(32, s.getSize());
        assertEquals(1, s.getScale());

        // Span 0..50 requires 51 slots > 32. Should grow to 64 while retaining scale 1!
        s.addValue(msec(50), 100, false);
        assertEquals(64, s.getSize());
        assertEquals(1, s.getScale());

        // Span 0..100 requires 101 slots > 64 (maxCapacity). Now condensing should occur!
        s.addValue(msec(100), 100, false);
        assertEquals(64, s.getSize());
        assertTrue("Scale must increase when exceeding maxCapacity", s.getScale() >= 2);
    }

    /**
     * Tests that toResolution downsamples an independent copy to target display resolution,
     * while preserving peak 1-second error burst counts.
     */
    @Test
    public void testToResolutionAndPeakErrorCountPreservation()
    {
        final IntTimeSeries s = new IntTimeSeries(16, 64);
        // sec 0: 1 error
        s.addValue(msec(0), 100, true);
        // sec 1: 5 errors burst
        for (int i = 0; i < 5; i++)
        {
            s.addValue(msec(1), 100, true);
        }
        // sec 2: 0 errors
        s.addValue(msec(2), 100, false);
        // sec 3: 2 errors
        s.addValue(msec(3), 100, true);
        s.addValue(msec(3), 100, true);

        assertEquals(1, s.getScale());
        assertEquals(16, s.getSize());

        // Downsample to targetWidth = 2 slots
        final IntTimeSeries condensed = s.toResolution(2);
        // Original series remains completely unmodified
        assertEquals(1, s.getScale());
        assertEquals(16, s.getSize());

        // Condensed copy has condensed to fit target width
        assertTrue(condensed.getScale() > 1);
        final var errTs = condensed.toErrorsPerSecondTimeSeries("Errors/s");
        assertTrue(errTs.getItemCount() > 0);

        // First condensed slot covers sec 0 and sec 1. Peak burst in that window was 5 errors/s.
        // It must report 5.0 (preserving peak burst), NOT (1 + 5) / 2 = 3.0.
        final Number firstSlotErrors = errTs.getValue(0);
        assertEquals(5.0, firstSlotErrors.doubleValue(), 0.001);
    }

    /**
     * Tests that the copy constructor performs a deep copy of entries and histogram.
     */
    @Test
    public void testDeepCopyConstructor()
    {
        final IntTimeSeries s = new IntTimeSeries(16, 64);
        s.addValue(msec(0), 100, true);
        s.addValue(msec(5), 200, false);

        final IntTimeSeries copy = new IntTimeSeries(s);
        assertEquals(s.getSize(), copy.getSize());
        assertEquals(s.getMaxCapacity(), copy.getMaxCapacity());
        assertEquals(s.getScale(), copy.getScale());
        assertEquals(s.getCount(), copy.getCount());
        assertEquals(s.getErrorCount(), copy.getErrorCount());
        assertEquals(s.getTotalValue(), copy.getTotalValue());

        // Mutate original; copy must remain unaffected
        s.addValue(msec(10), 300, true);
        assertEquals(3, s.getCount());
        assertEquals(2, copy.getCount());
    }

    /**
     * Tests that merge dynamically expands capacity when both series are at scale 1.
     */
    @Test
    public void testMergeDynamicGrowth()
    {
        final IntTimeSeries s1 = new IntTimeSeries(16, 64);
        s1.addValue(msec(0), 100, false);

        final IntTimeSeries s2 = new IntTimeSeries(16, 64);
        s2.addValue(msec(25), 200, true);

        // Merging span 0..25 is 26 seconds > 16. Buffer should grow to 32 while scale remains 1.
        s1.merge(s2);
        assertEquals(32, s1.getSize());
        assertEquals(1, s1.getScale());
        assertEquals(2, s1.getCount());
        assertEquals(1, s1.getErrorCount());
    }

    /**
     * Tests that newly created time series with large capacities lazily allocate slots upon write,
     * and chart conversion methods handle sparse/unpopulated slots without throwing NullPointerException.
     */
    @Test
    public void testLazyAllocationAndChartGeneration()
    {
        // Allocate a series with 4096 initial capacity
        final IntTimeSeries s = new IntTimeSeries();
        assertEquals(4096, s.getSize());

        // Add just two distinct points separated in time
        s.addValue(msec(10), 150, false);
        s.addValue(msec(50), 300, true);

        assertEquals(2, s.getCount());
        assertEquals(1, s.getErrorCount());

        // Verify chart generation methods do not NPE on unpopulated/sparse slots
        assertNotNull(s.toRunTimeTimeSeries("Runtime"));
        assertNotNull(s.toCountPerSecondTimeSeries("Count/s"));
        assertNotNull(s.toErrorsPerSecondTimeSeries("Errors/s"));
        assertNotNull(s.toErrorRateTimeSeries("Error Rate"));

        // Condensing and resolution downsampling must preserve sparse integrity
        final IntTimeSeries condensed = s.toResolution(500);
        assertNotNull(condensed);
        assertEquals(2, condensed.getCount());
        assertEquals(1, condensed.getErrorCount());
    }

    /**
     * Tests that merging parallel time series (from different worker threads parsing the same timeline)
     * correctly sums instantaneous burst rates (errors and count) for the same time slot, and that subsequent
     * resolution downsampling preserves that peak burst across wider intervals.
     */
    @Test
    public void testMergeParallelThreadsSumsBurstCounts()
    {
        final IntTimeSeries t1 = new IntTimeSeries(16, 64);
        final IntTimeSeries t2 = new IntTimeSeries(16, 64);

        // Thread 1 sees 20 errors at sec 5
        for (int i = 0; i < 20; i++)
        {
            t1.addValue(msec(5), 100, true);
        }

        // Thread 2 sees 25 errors at sec 5
        for (int i = 0; i < 25; i++)
        {
            t2.addValue(msec(5), 100, true);
        }

        // Merge thread 2 into thread 1
        t1.merge(t2);

        // At sec 5, total errors across both threads must be 45
        assertEquals(45, t1.getErrorCount());
        final var errTs1 = t1.toErrorsPerSecondTimeSeries("Errors/s");
        assertEquals(1, errTs1.getItemCount());
        assertEquals(45.0, errTs1.getValue(0).doubleValue(), 0.001);

        // When condensed to fewer slots (e.g. resolution = 4), peak burst across the bucket must still be 45!
        final IntTimeSeries condensed = t1.toResolution(4);
        final var errTsCondensed = condensed.toErrorsPerSecondTimeSeries("Errors/s");
        double maxInCondensed = 0.0;
        for (int i = 0; i < errTsCondensed.getItemCount(); i++)
        {
            maxInCondensed = Math.max(maxInCondensed, errTsCondensed.getValue(i).doubleValue());
        }
        assertEquals(45.0, maxInCondensed, 0.001);
    }

    /**
     * Verifies that 64-slot primitive pages are allocated lazily on demand and that untouched pages remain null.
     */
    @Test
    public void testPagedLazyAllocationAndCleanupOnCondense()
    {
        final IntTimeSeries s = new IntTimeSeries(4096, 32768);
        assertEquals(0, s.getAllocatedPageCount());

        // Write to page 0 (sec 5), page 7 (sec 500), and page 31 (sec 2000)
        s.addValue(msec(5), 100, false);
        assertEquals(1, s.getAllocatedPageCount());

        s.addValue(msec(500), 200, false);
        assertEquals(2, s.getAllocatedPageCount());

        s.addValue(msec(2000), 300, false);
        assertEquals(3, s.getAllocatedPageCount());

        // Condense repeatedly until all 3 points fall into page 0 (scale 6 -> slotWidth 32 -> 2000/32 = 62 < 64)
        s.condenseToScale(6);
        assertEquals(1, s.getAllocatedPageCount());
        assertEquals(3, s.getCount());
        assertEquals(600, s.getTotalValue());
    }

    /**
     * Verifies that auxiliary per-page arrays (errorCounts, concurrentCounts, maxCounts, maxErrorCounts)
     * remain null until a slot on that page actually diverges from default invariants.
     */
    @Test
    public void testPageLazyAuxiliaryArraysInvariants()
    {
        final IntTimeSeries s = new IntTimeSeries(256, 256);

        // 1. Page 0: only non-failing, single-second samples -> no auxiliary arrays needed
        s.addValue(msec(0), 50, false);
        s.addValue(msec(0), 150, false);
        s.addValue(msec(63), 100, false);

        assertFalse(s.hasPageErrorCounts(0));
        assertFalse(s.hasPageConcurrentCounts(0));
        assertFalse(s.hasPageMaxCounts(0));
        assertFalse(s.hasPageMaxErrorCounts(0));
        assertEquals(2, s.getSlotCount(0));
        assertEquals(2, s.getSlotConcurrentCount(0));
        assertEquals(2, s.getSlotMaxCount(0));
        assertEquals(0, s.getSlotErrorCount(0));
        assertEquals(0, s.getSlotMaxErrorCount(0));

        // 2. Page 1 (slots 64..127): add a failed sample at sec 64 -> only errorCounts allocated on page 1
        s.addValue(msec(64), 200, true);
        assertFalse(s.hasPageErrorCounts(0));
        assertTrue(s.hasPageErrorCounts(1));
        assertFalse(s.hasPageConcurrentCounts(1));
        assertFalse(s.hasPageMaxCounts(1));
        assertFalse(s.hasPageMaxErrorCounts(1));
        assertEquals(1, s.getSlotErrorCount(64));
        assertEquals(1, s.getSlotMaxErrorCount(64));

        // 3. Page 2 (slots 128..191): add a sample spanning sec 128..130 -> concurrentCounts allocated on page 2
        s.addValue(msec(128), msec(130), 300, false);
        assertFalse(s.hasPageConcurrentCounts(0));
        assertFalse(s.hasPageConcurrentCounts(1));
        assertTrue(s.hasPageConcurrentCounts(2));
        assertEquals(0, s.getSlotCount(128));
        assertEquals(1, s.getSlotConcurrentCount(128));
        assertEquals(0, s.getSlotCount(129));
        assertEquals(1, s.getSlotConcurrentCount(129));
        assertEquals(1, s.getSlotCount(130));
        assertEquals(1, s.getSlotConcurrentCount(130));

        // Verify that existing slots in page 0 still preserve concurrentCount == count when page 0 later gets concurrency
        s.addValue(msec(10), msec(11), 80, false);
        assertTrue(s.hasPageConcurrentCounts(0));
        assertEquals(2, s.getSlotCount(0));
        assertEquals(2, s.getSlotConcurrentCount(0));
        assertEquals(1, s.getSlotCount(63));
        assertEquals(1, s.getSlotConcurrentCount(63));
        assertEquals(0, s.getSlotCount(10));
        assertEquals(1, s.getSlotConcurrentCount(10));
        assertEquals(1, s.getSlotCount(11));
        assertEquals(1, s.getSlotConcurrentCount(11));

        // 4. Condense one step: slot 0 (count 2) and slot 1 (count 0) -> condensed slot 0 has count 2, maxCount 2.
        // Add another sample to sec 1 before condensing so count(0)+count(1) = 3 != max(2, 1) = 2!
        s.addValue(msec(1), 50, false);
        // Also add 2 errors at sec 64 (total 3 errors) and 1 error at sec 65 so condensed errorCount(4) != maxErrorCount(3)
        s.addValue(msec(64), 200, true);
        s.addValue(msec(64), 200, true);
        s.addValue(msec(65), 200, true);

        assertFalse(s.hasPageMaxCounts(0));
        assertFalse(s.hasPageMaxErrorCounts(1));

        s.condenseOneStep();

        // After condensing, page 0 holds condensed slots 0..63 (from original pages 0 and 1!)
        assertTrue(s.hasPageMaxCounts(0));
        assertTrue(s.hasPageMaxErrorCounts(0));
        // Condensed slot 0 (from sec 0 and sec 1): count = 2 + 1 = 3, maxCount = max(2, 1) = 2
        assertEquals(3, s.getSlotCount(0));
        assertEquals(2, s.getSlotMaxCount(0));
        // Condensed slot 32 (from sec 64 and sec 65): errorCount = 3 + 1 = 4, maxErrorCount = max(3, 1) = 3
        assertEquals(4, s.getSlotErrorCount(32));
        assertEquals(3, s.getSlotMaxErrorCount(32));
    }

    /**
     * Verifies that same-scale thread merges keep maxCounts and maxErrorCounts unallocated at scale 1
     * because additive burst counts equal total slot counts at 1-second resolution.
     */
    @Test
    public void testSameScaleMergeKeepsMaxArraysUnallocated()
    {
        final IntTimeSeries s1 = new IntTimeSeries(128, 128);
        final IntTimeSeries s2 = new IntTimeSeries(128, 128);

        s1.addValue(msec(0), 50, false);
        s1.addValue(msec(10), 100, true);
        s1.addValue(msec(10), 120, false);

        s2.addValue(msec(10), 140, true);
        s2.addValue(msec(70), 200, false);

        s1.merge(s2);

        assertEquals(1, s1.getScale());
        assertFalse(s1.hasPageMaxCounts(0));
        assertFalse(s1.hasPageMaxErrorCounts(0));
        assertFalse(s1.hasPageMaxCounts(1));
        assertFalse(s1.hasPageMaxErrorCounts(1));

        assertEquals(3, s1.getSlotCount(10));
        assertEquals(3, s1.getSlotMaxCount(10));
        assertEquals(2, s1.getSlotErrorCount(10));
        assertEquals(2, s1.getSlotMaxErrorCount(10));
    }

    /**
     * Tests operations spanning across multiple 64-slot page boundaries (slots 63 -> 64 and 127 -> 128).
     */
    @Test
    public void testCrossPageBoundaryConcurrencyAndShiftRight()
    {
        final IntTimeSeries s = new IntTimeSeries(256, 512);

        // Operation spanning from sec 0 (page 0) across page 1 (64..127) to sec 132 (page 2)
        s.addValue(msec(0), msec(132), 132000, true);

        assertEquals(3, s.getAllocatedPageCount());
        for (int pos = 0; pos < 132; pos++)
        {
            assertEquals("Pos " + pos, 0, s.getSlotCount(pos));
            assertEquals("Pos " + pos, 1, s.getSlotConcurrentCount(pos));
            assertEquals("Pos " + pos, 0, s.getSlotErrorCount(pos));
        }
        assertEquals(1, s.getSlotCount(132));
        assertEquals(1, s.getSlotConcurrentCount(132));
        assertEquals(1, s.getSlotErrorCount(132));
        assertEquals(132000L, s.getSlotTotalValue(132));

        // Now add an earlier sample at sec -70 (shifts everything right by 70 slots across page boundaries!)
        s.addValue(msec(-70), 50, false);

        assertEquals(sec(-70), s.getFirstSecond());
        // Slot 0 is sec -70
        assertEquals(1, s.getSlotCount(0));
        assertEquals(50L, s.getSlotTotalValue(0));
        assertEquals(0, s.getSlotErrorCount(0));

        // Slots 1..69 (sec -69 .. sec -1) must be completely empty
        for (int pos = 1; pos < 70; pos++)
        {
            assertEquals("Empty pos " + pos, 0, s.getSlotCount(pos));
            assertEquals("Empty pos " + pos, 0, s.getSlotConcurrentCount(pos));
            assertEquals("Empty pos " + pos, 0, s.getSlotErrorCount(pos));
        }

        // Slots 70..201 (sec 0 .. sec 131) must have concurrency = 1, count = 0
        for (int pos = 70; pos < 202; pos++)
        {
            assertEquals("Shifted active pos " + pos, 0, s.getSlotCount(pos));
            assertEquals("Shifted active pos " + pos, 1, s.getSlotConcurrentCount(pos));
        }

        // Slot 202 (sec 132) must have the completion record
        assertEquals(1, s.getSlotCount(202));
        assertEquals(1, s.getSlotConcurrentCount(202));
        assertEquals(1, s.getSlotErrorCount(202));
        assertEquals(132000L, s.getSlotTotalValue(202));
        assertEquals(132000, s.getSlotMinimumValue(202));
        assertEquals(132000, s.getSlotMaximumValue(202));
    }

    /**
     * Tests condensing across multiple 64-slot pages where page 0 (0..63) and page 1 (64..127)
     * condense into page 0 (0..31 and 32..63), and page 2 (128..191) + page 3 (192..255) condense into page 1.
     */
    @Test
    public void testMultiPageCondenseSlotMapping()
    {
        final IntTimeSeries s = new IntTimeSeries(256, 256);

        // Populate boundary slots of pages 0, 1, 2, 3:
        // Page 0: slot 0, slot 62, slot 63
        s.addValue(msec(0), 10, false);
        s.addValue(msec(62), 20, false);
        s.addValue(msec(63), 30, true);

        // Page 1: slot 64, slot 127
        s.addValue(msec(64), 40, false);
        s.addValue(msec(127), 50, true);

        // Page 2: slot 128, slot 191
        s.addValue(msec(128), 60, false);
        s.addValue(msec(191), 70, false);

        // Page 3: slot 192, slot 255
        s.addValue(msec(192), 80, true);
        s.addValue(msec(255), 90, false);

        assertEquals(4, s.getAllocatedPageCount());

        s.condenseOneStep();

        assertEquals(2, s.getScale());
        assertEquals(2, s.getAllocatedPageCount());
        assertEquals(127, s.getLastPosUsed());

        // Check condensed slots in Page 0 (0..63):
        // Slot 0 (from 0, 1): count 1, total 10
        assertEquals(1, s.getSlotCount(0));
        assertEquals(10L, s.getSlotTotalValue(0));

        // Slot 31 (from 62, 63): count 2, total 50, min 20, max 30, error 1
        assertEquals(2, s.getSlotCount(31));
        assertEquals(50L, s.getSlotTotalValue(31));
        assertEquals(20, s.getSlotMinimumValue(31));
        assertEquals(30, s.getSlotMaximumValue(31));
        assertEquals(1, s.getSlotErrorCount(31));

        // Slot 32 (from 64, 65): count 1, total 40
        assertEquals(1, s.getSlotCount(32));
        assertEquals(40L, s.getSlotTotalValue(32));

        // Slot 63 (from 126, 127): count 1, total 50, error 1
        assertEquals(1, s.getSlotCount(63));
        assertEquals(50L, s.getSlotTotalValue(63));
        assertEquals(1, s.getSlotErrorCount(63));

        // Check condensed slots in Page 1 (64..127):
        // Slot 64 (from 128, 129): count 1, total 60
        assertEquals(1, s.getSlotCount(64));
        assertEquals(60L, s.getSlotTotalValue(64));

        // Slot 95 (from 190, 191): count 1, total 70
        assertEquals(1, s.getSlotCount(95));
        assertEquals(70L, s.getSlotTotalValue(95));

        // Slot 96 (from 192, 193): count 1, total 80, error 1
        assertEquals(1, s.getSlotCount(96));
        assertEquals(80L, s.getSlotTotalValue(96));
        assertEquals(1, s.getSlotErrorCount(96));

        // Slot 127 (from 254, 255): count 1, total 90
        assertEquals(1, s.getSlotCount(127));
        assertEquals(90L, s.getSlotTotalValue(127));
    }

    /**
     * Tests all combinations of concurrency-only slots (count == 0, concurrentCount > 0)
     * during merge and condense operations.
     */
    @Test
    public void testConcurrencyOnlySlotMergeAndCondenseCombinations()
    {
        // Series A: op spanning sec 0..3 -> sec 0, 1, 2 have count=0, concurrentCount=1; sec 3 has count=1, concurrentCount=1
        final IntTimeSeries a = new IntTimeSeries(64, 64);
        a.addValue(msec(0), msec(3), 100, false);
        a.addValue(msec(0), msec(3), 200, false);
        // Now in A: sec 0, 1, 2 have count=0, concurrent=2; sec 3 has count=2, concurrent=2

        // Series B:
        // - sec 0: concurrency-only with concurrent=5 (from op spanning 0..4)
        // - sec 1: completion with count=1, concurrent=1 (plus 5 from spanning op = 6)
        // - sec 2: empty if we didn't span, let's test another series C for count > 0 into concurrency-only
        final IntTimeSeries b = new IntTimeSeries(64, 64);
        for (int i = 0; i < 5; i++)
        {
            b.addValue(msec(0), msec(1), 50, false);
        }
        // In B: sec 0 has count=0, concurrent=5; sec 1 has count=5, concurrent=5; sec 3 has count=0, concurrent=0
        // Add concurrency-only at sec 3 in B (by spanning sec 3..4)
        for (int i = 0; i < 4; i++)
        {
            b.addValue(msec(3), msec(4), 80, true);
        }
        // Now in B: sec 3 has count=0, concurrent=4; sec 4 has count=4, concurrent=4, error=4

        a.merge(b);

        // Slot 0: both A (conc=2) and B (conc=5) had count=0 -> result count=0, conc=max(2, 5)=5
        assertEquals(0, a.getSlotCount(0));
        assertEquals(5, a.getSlotConcurrentCount(0));

        // Slot 1: A had count=0, conc=2; B had count=5, conc=5 -> result count=5, conc=max(2, 5)=5, total=250
        assertEquals(5, a.getSlotCount(1));
        assertEquals(5, a.getSlotConcurrentCount(1));
        assertEquals(250L, a.getSlotTotalValue(1));

        // Slot 2: A had count=0, conc=2; B was empty -> result count=0, conc=2
        assertEquals(0, a.getSlotCount(2));
        assertEquals(2, a.getSlotConcurrentCount(2));

        // Slot 3: A had count=2, conc=2, total=300; B had count=0, conc=4 -> result count=2, conc=max(2, 4)=4, total=300
        assertEquals(2, a.getSlotCount(3));
        assertEquals(4, a.getSlotConcurrentCount(3));
        assertEquals(300L, a.getSlotTotalValue(3));

        // Slot 4: A was empty; B had count=4, conc=4, error=4 -> result count=4, conc=4, error=4
        assertEquals(4, a.getSlotCount(4));
        assertEquals(4, a.getSlotConcurrentCount(4));
        assertEquals(4, a.getSlotErrorCount(4));

        // Now condense A one step:
        // Condensed slot 0 combines old slot 0 (count=0, conc=5) and old slot 1 (count=5, conc=5) -> count=5, conc=5
        // Condensed slot 1 combines old slot 2 (count=0, conc=2) and old slot 3 (count=2, conc=4) -> count=2, conc=4
        a.condenseOneStep();
        assertEquals(5, a.getSlotCount(0));
        assertEquals(5, a.getSlotConcurrentCount(0));
        assertEquals(250L, a.getSlotTotalValue(0));

        assertEquals(2, a.getSlotCount(1));
        assertEquals(4, a.getSlotConcurrentCount(1));
        assertEquals(300L, a.getSlotTotalValue(1));
    }

    /**
     * Verifies that the distinct values bitset and scaling inside Page slots match standalone IntTimeSeriesEntry
     * across small values (< 64), medium values (64..127), large values requiring multiple scaleUp steps,
     * and slot merges/condenses with mismatched distinctValuesScale.
     */
    @Test
    public void testDistinctValuesBitsetAndScaleAlignmentMatchesEntry()
    {
        final IntTimeSeries s1 = new IntTimeSeries(64, 64);
        final IntTimeSeries s2 = new IntTimeSeries(64, 64);

        final IntTimeSeriesEntry ref1 = new IntTimeSeriesEntry();
        final IntTimeSeriesEntry ref2 = new IntTimeSeriesEntry();

        final int[] sampleSet1 = { 0, 1, 63, 64, 65, 127 }; // stays at scale 0 (both low and high words used)
        final int[] sampleSet2 = { 15, 90, 250, 1200, 15000 }; // triggers multiple scaleUp steps

        for (final int v : sampleSet1)
        {
            s1.addValue(msec(0), v, false);
            ref1.updateValue(v, false);
        }
        assertArrayEquals(ref1.getValues(), s1.getEntry(0).getValues(), 0.0001);
        assertEquals(ref1, s1.getEntry(0));

        for (final int v : sampleSet2)
        {
            s2.addValue(msec(0), v, true);
            ref2.updateValue(v, true);
        }
        assertArrayEquals(ref2.getValues(), s2.getEntry(0).getValues(), 0.0001);
        assertEquals(ref2, s2.getEntry(0));

        // Test merge when dstScale (0) < srcScale (>0)
        final IntTimeSeries mergedAsc = new IntTimeSeries(s1);
        mergedAsc.merge(s2);
        final IntTimeSeriesEntry refMergedAsc = new IntTimeSeriesEntry(ref1).merge(new IntTimeSeriesEntry(ref2));
        assertEquals(refMergedAsc, mergedAsc.getEntry(0));
        assertArrayEquals(refMergedAsc.getValues(), mergedAsc.getEntry(0).getValues(), 0.0001);

        // Test merge when dstScale (>0) > srcScale (0)
        final IntTimeSeries mergedDesc = new IntTimeSeries(s2);
        mergedDesc.merge(s1);
        final IntTimeSeriesEntry refMergedDesc = new IntTimeSeriesEntry(ref2).merge(new IntTimeSeriesEntry(ref1));
        assertEquals(refMergedDesc, mergedDesc.getEntry(0));
        assertArrayEquals(refMergedDesc.getValues(), mergedDesc.getEntry(0).getValues(), 0.0001);

        // Test condense when slot 0 has scale 0 and slot 1 has scale > 0
        final IntTimeSeries condenseSeries = new IntTimeSeries(64, 64);
        for (final int v : sampleSet1)
        {
            condenseSeries.addValue(msec(0), v, false);
        }
        for (final int v : sampleSet2)
        {
            condenseSeries.addValue(msec(1), v, true);
        }
        condenseSeries.condenseOneStep();
        final IntTimeSeriesEntry refCondensed = new IntTimeSeriesEntry(ref1).condenseWith(new IntTimeSeriesEntry(ref2));
        assertEquals(refCondensed, condenseSeries.getEntry(0));
        assertArrayEquals(refCondensed.getValues(), condenseSeries.getEntry(0).getValues(), 0.0001);
    }

    /**
     * Randomized equivalence test verifying that all direct slot getters and materialized entries
     * match a reference array of IntTimeSeriesEntry across multi-page growth, concurrency, errors,
     * condensing, and merging.
     */
    @Test
    public void testRandomizedEquivalenceWithReferenceEntries()
    {
        final Random rng = new Random(42L);
        final int capacity = 256;
        final IntTimeSeries s1 = new IntTimeSeries(16, capacity);
        final IntTimeSeries s2 = new IntTimeSeries(16, capacity);
        final IntTimeSeriesEntry[] ref1 = new IntTimeSeriesEntry[capacity];
        final IntTimeSeriesEntry[] ref2 = new IntTimeSeriesEntry[capacity];
        for (int i = 0; i < capacity; i++)
        {
            ref1[i] = new IntTimeSeriesEntry();
            ref2[i] = new IntTimeSeriesEntry();
        }

        // Anchor second 0 on both series so slot index == second offset
        s1.addValue(msec(0), 10, false);
        ref1[0].updateValue(10, false);
        s2.addValue(msec(0), 20, true);
        ref2[0].updateValue(20, true);

        for (int n = 0; n < 2000; n++)
        {
            final int startSec = rng.nextInt(capacity - 10);
            final int durationSec = rng.nextInt(6);
            final int endSec = startSec + durationSec;
            final int value = rng.nextInt(50_000);
            final boolean failed = rng.nextInt(5) == 0;

            if (rng.nextBoolean())
            {
                s1.addValue(msec(startSec), msec(endSec), value, failed);
                ref1[endSec].updateValue(value, failed);
                for (int p = startSec; p < endSec; p++)
                {
                    ref1[p].updateConcurrency();
                }
            }
            else
            {
                s2.addValue(msec(startSec), msec(endSec), value, failed);
                ref2[endSec].updateValue(value, failed);
                for (int p = startSec; p < endSec; p++)
                {
                    ref2[p].updateConcurrency();
                }
            }
        }

        // Verify all slots in s1 and s2 before merge
        for (int i = 0; i < capacity; i++)
        {
            assertSlotMatchesReference("s1 slot " + i, s1, i, ref1[i]);
            assertSlotMatchesReference("s2 slot " + i, s2, i, ref2[i]);
        }

        // Merge s2 into s1 and ref2 into ref1
        s1.merge(s2);
        for (int i = 0; i < capacity; i++)
        {
            ref1[i].merge(ref2[i]);
            assertSlotMatchesReference("merged slot " + i, s1, i, ref1[i]);
        }

        // Condense two steps and verify every condensed slot matches reference condenseWith
        for (int step = 0; step < 2; step++)
        {
            s1.condenseOneStep();
            final int activeSlots = capacity >> (step + 1);
            for (int i = 0; i < activeSlots; i++)
            {
                ref1[i] = ref1[2 * i].condenseWith(ref1[2 * i + 1]);
                assertSlotMatchesReference("condensed step " + step + " slot " + i, s1, i, ref1[i]);
            }
            for (int i = activeSlots; i < capacity; i++)
            {
                assertSlotMatchesReference("cleared step " + step + " slot " + i, s1, i, new IntTimeSeriesEntry());
            }
        }
    }

    /**
     * Verifies out-of-bounds slot getters safely return 0 or an empty IntTimeSeriesEntry.
     */
    @Test
    public void testOutOfBoundsSlotGetters()
    {
        final IntTimeSeries s = new IntTimeSeries(64, 64);
        s.addValue(msec(0), 100, true);

        for (final int pos : new int[] { -1, -100, 64, 1000 })
        {
            assertEquals(0, s.getSlotCount(pos));
            assertEquals(0, s.getSlotErrorCount(pos));
            assertEquals(0, s.getSlotMaxErrorCount(pos));
            assertEquals(0, s.getSlotMaxCount(pos));
            assertEquals(0, s.getSlotConcurrentCount(pos));
            assertEquals(0L, s.getSlotTotalValue(pos));
            assertEquals(0, s.getSlotAverageValue(pos));
            assertEquals(0, s.getSlotMinimumValue(pos));
            assertEquals(0, s.getSlotMaximumValue(pos));
            assertEquals(new IntTimeSeriesEntry(), s.getEntry(pos));
        }
    }

    /**
     * Tests merging a larger series into an empty series that has a smaller maxCapacity,
     * ensuring it safely condenses instead of overflowing page bounds.
     */
    @Test
    public void testMergeIntoEmptyWithSmallerMaxCapacity()
    {
        final IntTimeSeries target = new IntTimeSeries(16, 16);
        final IntTimeSeries source = new IntTimeSeries(128, 128);

        source.addValue(msec(0), 100, false);
        source.addValue(msec(50), 300, true);

        target.merge(source);

        assertEquals(2, target.getCount());
        assertEquals(1, target.getErrorCount());
        assertEquals(400L, target.getTotalValue());
        assertTrue(target.getScale() > 1);
    }

    /**
     * Tests merging two non-empty series that are at different scales (scale 1 and scale 3)
     * across multiple pages.
     */
    @Test
    public void testMergeDifferentScalesAcrossPages()
    {
        final IntTimeSeries s1 = new IntTimeSeries(128, 128);
        final IntTimeSeries s2 = new IntTimeSeries(128, 128);

        // s1 at scale 1: points at sec 0, sec 64, sec 100
        s1.addValue(msec(0), 100, false);
        s1.addValue(msec(64), 200, true);
        s1.addValue(msec(100), 300, false);
        assertEquals(1, s1.getScale());

        // s2 condensed to scale 3 (slotWidth = 4s): points at sec 0, sec 400
        s2.addValue(msec(0), 400, true);
        s2.addValue(msec(400), 500, false);
        assertEquals(3, s2.getScale());

        s1.merge(s2);

        assertEquals(3, s1.getScale());
        assertEquals(5, s1.getCount());
        assertEquals(2, s1.getErrorCount());
        assertEquals(1500L, s1.getTotalValue());
        // Slot 0 (sec 0..3): combined sec 0 from s1 (100, false) and sec 0 from s2 (400, true)
        assertEquals(2, s1.getSlotCount(0));
        assertEquals(1, s1.getSlotErrorCount(0));
        assertEquals(500L, s1.getSlotTotalValue(0));
        // Slot 16 (sec 64..67): from s1 sec 64
        assertEquals(1, s1.getSlotCount(16));
        assertEquals(1, s1.getSlotErrorCount(16));
        assertEquals(200L, s1.getSlotTotalValue(16));
        // Slot 25 (sec 100..103): from s1 sec 100
        assertEquals(1, s1.getSlotCount(25));
        assertEquals(300L, s1.getSlotTotalValue(25));
        // Slot 100 (sec 400..403): from s2 sec 400
        assertEquals(1, s1.getSlotCount(100));
        assertEquals(500L, s1.getSlotTotalValue(100));
    }

    /**
     * Tests shifting right repeatedly with dynamic capacity expansion (64 -> 128 -> 256)
     * followed by condensing when maxCapacity is exceeded.
     */
    @Test
    public void testShiftRightWithDynamicGrowthAndCondensingAcrossPages()
    {
        final IntTimeSeries s = new IntTimeSeries(64, 256);

        s.addValue(msec(200), 100, false);
        assertEquals(64, s.getSize());
        assertEquals(1, s.getScale());

        // Shift right by 100s -> span 100..200 (101 slots), grows to 128 at scale 1
        s.addValue(msec(100), 200, true);
        assertEquals(128, s.getSize());
        assertEquals(1, s.getScale());
        assertEquals(1, s.getSlotCount(0));
        assertEquals(200L, s.getSlotTotalValue(0));
        assertEquals(1, s.getSlotCount(100));
        assertEquals(100L, s.getSlotTotalValue(100));

        // Shift right by another 100s -> span 0..200 (201 slots), grows to 256 at scale 1
        s.addValue(msec(0), 300, false);
        assertEquals(256, s.getSize());
        assertEquals(1, s.getScale());
        assertEquals(1, s.getSlotCount(0));
        assertEquals(300L, s.getSlotTotalValue(0));
        assertEquals(1, s.getSlotCount(100));
        assertEquals(200L, s.getSlotTotalValue(100));
        assertEquals(1, s.getSlotErrorCount(100));
        assertEquals(1, s.getSlotCount(200));
        assertEquals(100L, s.getSlotTotalValue(200));

        // Shift right by another 100s -> span -100..200 (301 slots > maxCapacity 256), condenses to scale 2!
        s.addValue(msec(-100), 400, true);
        assertEquals(256, s.getSize());
        assertEquals(2, s.getScale());
        assertEquals(4, s.getCount());
        assertEquals(2, s.getErrorCount());
        assertEquals(1000L, s.getTotalValue());
    }

    /**
     * Verifies TimeSeriesDownsampler bucket calculations on a multi-page IntTimeSeries.
     */
    @Test
    public void testTimeSeriesDownsamplerMultiPageIntegration()
    {
        final IntTimeSeries s = new IntTimeSeries(256, 256);
        // Bucket 0 (sec 0..59, page 0): 2 samples (100ms, 300ms, 1 error) -> mean 200, countRate 2/60, errorRate 1/60
        s.addValue(msec(0), 100, false);
        s.addValue(msec(30), 300, true);

        // Bucket 1 (sec 60..119, crossing page 0 -> page 1 at sec 64): 3 samples (60ms, 90ms, 150ms, 2 errors) -> mean 100
        s.addValue(msec(63), 60, true);
        s.addValue(msec(64), 90, false);
        s.addValue(msec(100), 150, true);

        // Bucket 2 (sec 120..179, crossing page 1 -> page 2 at sec 128): empty
        // Bucket 3 (sec 180..239, page 2 & page 3): 1 sample (500ms, 0 errors) -> mean 500
        s.addValue(msec(200), 500, false);

        final int[] means = TimeSeriesDownsampler.meanPerBucket(s, sec(0), 4, 60);
        assertArrayEquals(new int[] { 200, 100, 0, 500 }, means);

        final double[] countRates = TimeSeriesDownsampler.countRatePerBucket(s, sec(0), 4, 60);
        assertArrayEquals(new double[] { 2.0 / 60.0, 3.0 / 60.0, 0.0, 1.0 / 60.0 }, countRates, 0.00001);

        final double[] errorRates = TimeSeriesDownsampler.errorRatePerBucket(s, sec(0), 4, 60);
        assertArrayEquals(new double[] { 1.0 / 60.0, 2.0 / 60.0, 0.0, 0.0 }, errorRates, 0.00001);
    }

    /**
     * Exercises Page copy constructor when all four lazy auxiliary arrays (errorCounts, concurrentCounts,
     * maxCounts, maxErrorCounts) are allocated, as well as updating a slot after condensing and testing
     * exact distinct-value bitmap boundaries (0, 63, 64, 127, 128).
     */
    @Test
    public void testPageCopyAndPostCondenseUpdateAndBitmapBoundaries()
    {
        final IntTimeSeries s = new IntTimeSeries(64, 64);

        // Anchor firstSecond at sec 0, then populate slot 5 to test non-zero slot index when concurrentCounts == null
        s.addValue(msec(0), 200, true);
        s.addValue(msec(5), 63, false);
        s.addValue(msec(5), 64, false);
        s.addValue(msec(5), 127, false);
        assertFalse(s.hasPageConcurrentCounts(0));
        assertEquals(3, s.getSlotConcurrentCount(5));
        assertArrayEquals(new double[] { 63.0, 64.0, 127.0 }, s.getEntry(5).getValues(), 0.0);

        // Trigger scaleUp at exact boundary 128
        s.addValue(msec(5), 128, false);
        assertArrayEquals(new double[] { 62.0, 64.0, 126.0, 128.0 }, s.getEntry(5).getValues(), 0.0);

        // Populate errors and concurrency across slots 0 and 1, then condense so all 4 auxiliary arrays exist on page 0
        s.addValue(msec(0), msec(1), 100, true);
        s.addValue(msec(1), 300, true);
        s.condenseOneStep();

        assertTrue(s.hasPageErrorCounts(0));
        assertTrue(s.hasPageConcurrentCounts(0));
        assertTrue(s.hasPageMaxCounts(0));
        assertTrue(s.hasPageMaxErrorCounts(0));

        // Condensed slot 0 currently has count=3, maxCount=2, errorCount=3, maxErrorCount=2
        assertEquals(3, s.getSlotCount(0));
        assertEquals(2, s.getSlotMaxCount(0));
        assertEquals(3, s.getSlotErrorCount(0));
        assertEquals(2, s.getSlotMaxErrorCount(0));

        // Deep copy via copy constructor when all 4 lazy arrays are non-null
        final IntTimeSeries copy = new IntTimeSeries(s);
        assertTrue(copy.hasPageErrorCounts(0));
        assertTrue(copy.hasPageConcurrentCounts(0));
        assertTrue(copy.hasPageMaxCounts(0));
        assertTrue(copy.hasPageMaxErrorCounts(0));
        assertEquals(s.getEntry(0), copy.getEntry(0));
        assertEquals(s.getEntry(2), copy.getEntry(2));

        // Now add a new failing value to slot 0 of the already-condensed series
        s.addValue(msec(0), 50, true);
        assertEquals(4, s.getSlotCount(0));
        assertEquals(3, s.getSlotMaxCount(0));
        assertEquals(4, s.getSlotErrorCount(0));
        assertEquals(3, s.getSlotMaxErrorCount(0));
    }

    /**
     * Tests nextHighestPowerOfTwo, page directory sizing, negative sample clamping,
     * and preserving null lazy arrays during copySlot/combineSlot across slots with different counts.
     */
    @Test
    public void testPowerOfTwoPageDirectoryAndLazyInvariantsOnCopyAndCombine()
    {
        assertEquals(1, IntTimeSeries.nextHighestPowerOfTwo(-5));
        assertEquals(1, IntTimeSeries.nextHighestPowerOfTwo(0));
        assertEquals(1, IntTimeSeries.nextHighestPowerOfTwo(1));
        assertEquals(2, IntTimeSeries.nextHighestPowerOfTwo(2));
        assertEquals(4, IntTimeSeries.nextHighestPowerOfTwo(3));
        assertEquals(64, IntTimeSeries.nextHighestPowerOfTwo(64));
        assertEquals(128, IntTimeSeries.nextHighestPowerOfTwo(65));

        final IntTimeSeries s1 = new IntTimeSeries(64, 256);
        assertEquals(1, s1.getPageDirectoryLength());

        // Add negative value (clamped to 0) at slot 0 (count=1) and two non-failing values at slot 2 (count=2)
        // Slot 2 has count=2 != slot 0's count=1, testing setConcurrentCount's s * 3 index calculation!
        s1.addValue(msec(0), -50, false);
        s1.addValue(msec(2), 100, false);
        s1.addValue(msec(2), 200, false);
        assertEquals(0, s1.getSlotMinimumValue(0));
        assertEquals(0, s1.getSlotMaximumValue(0));
        assertEquals(0, s1.getStatistics().minValue);
        assertFalse(s1.hasPageErrorCounts(0));
        assertFalse(s1.hasPageConcurrentCounts(0));

        // Condense s1: slot 0 gets slot 0 (count=1, conc=1, err=0), slot 1 gets slot 2 (count=2, conc=2, err=0)
        // Neither errorCounts nor concurrentCounts should be allocated during copySlot!
        final IntTimeSeries condensed = new IntTimeSeries(s1);
        condensed.condenseOneStep();
        assertFalse(condensed.hasPageErrorCounts(0));
        assertFalse(condensed.hasPageConcurrentCounts(0));
        assertEquals(1, condensed.getSlotCount(0));
        assertEquals(2, condensed.getSlotCount(1));

        // Merge s1 with another error-free series that populates non-overlapping slot 4 (count=3, conc=3)
        final IntTimeSeries s2 = new IntTimeSeries(64, 256);
        s2.addValue(msec(4), 300, false);
        s1.merge(s2);
        assertFalse(s1.hasPageErrorCounts(0));
        assertFalse(s1.hasPageConcurrentCounts(0));
        assertEquals(1, s1.getSlotCount(0));
        assertEquals(2, s1.getSlotCount(2));
        assertEquals(1, s1.getSlotCount(4));
    }

    /**
     * Tests copy constructor, merge with earlier startSecond (triggering shiftRight inside merge),
     * sparse null pages across copy/merge, and toResolution boundary conditions.
     */
    @Test
    public void testCopyAndMergeSparsePagesAndShiftRightInMergeAndToResolution()
    {
        // 1. Copy constructor on empty series
        final IntTimeSeries empty = new IntTimeSeries(64, 128);
        final IntTimeSeries emptyCopy = new IntTimeSeries(empty);
        assertEquals(-1, emptyCopy.getLastPosUsed());
        assertEquals(0, emptyCopy.getAllocatedPageCount());

        // 2. Series with Page 0 and Page 2 allocated, Page 1 null (sparse pages)
        final IntTimeSeries sparse = new IntTimeSeries(256, 256);
        sparse.addValue(msec(0), 100, false);    // Page 0, slot 0
        sparse.addValue(msec(130), 200, true);   // Page 2, slot 2 (Page 1 is null!)
        assertEquals(2, sparse.getAllocatedPageCount());
        assertEquals(4, sparse.getPageDirectoryLength());

        // Copy constructor preserves null Page 1
        final IntTimeSeries sparseCopy = new IntTimeSeries(sparse);
        assertEquals(2, sparseCopy.getAllocatedPageCount());
        assertEquals(100L, sparseCopy.getSlotTotalValue(0));
        assertEquals(200L, sparseCopy.getSlotTotalValue(130));

        // Merge into empty series (fast-path) preserves null Page 1 and merges histogram/stats
        final IntTimeSeries fastTarget = new IntTimeSeries(64, 256);
        fastTarget.merge(sparse);
        assertEquals(256, fastTarget.getSize());
        assertEquals(2, fastTarget.getAllocatedPageCount());
        assertEquals(148, fastTarget.getMedianValue());
        assertEquals(2, fastTarget.getHistogram().getValueCount());

        // 3. Merge into non-empty series where 'other' starts EARLIER than 'this' (triggers shiftRight in merge)
        // and both are at scale 1 requiring growToCapacity
        final IntTimeSeries later = new IntTimeSeries(64, 256);
        later.addValue(msec(100), 400, false); // starts at sec 100
        later.merge(sparse);                   // sparse starts at sec 0, ends at sec 130 -> span 131 grows to 256 and shifts right!
        assertEquals(256, later.getSize());
        assertEquals(sec(0), later.getFirstSecond());
        assertEquals(130, later.getLastPosUsed());
        assertEquals(100L, later.getSlotTotalValue(0));
        assertEquals(400L, later.getSlotTotalValue(100));
        assertEquals(200L, later.getSlotTotalValue(130));
        assertEquals(3, later.getCount());
        assertEquals(100, later.getStatistics().minValue);
        assertEquals(400, later.getStatistics().maxValue);
        assertTrue(later.getStandardDeviation() > 0.0);

        // 4. toResolution boundary conditions: targetWidth <= 0, targetWidth == lastPosUsed + 1, targetWidth == lastPosUsed
        assertEquals(1, later.toResolution(0).getScale());
        assertEquals(1, later.toResolution(-10).getScale());
        assertEquals(1, later.toResolution(131).getScale()); // lastPosUsed (130) < 131 -> no condense
        final IntTimeSeries condensed130 = later.toResolution(130); // lastPosUsed (130) >= 130 -> condenses 1 step to 65
        assertEquals(2, condensed130.getScale());
        assertEquals(65, condensed130.getLastPosUsed());
    }

    /**
     * Tests JFreeChart export methods, statistics on empty and constant-value series,
     * and vertical/horizontal histogram series generation.
     */
    @Test
    public void testChartExportsStatisticsAndHistograms()
    {
        // 1. Empty series statistics and chart exports
        final IntTimeSeries empty = new IntTimeSeries(64, 64);
        final IntTimeSeries.Statistics emptyStats = empty.getStatistics();
        assertEquals(0L, emptyStats.count);
        assertEquals(0, emptyStats.minValue);
        assertEquals(0, emptyStats.maxValue);
        assertEquals(0.0, emptyStats.mean, 0.0);
        assertEquals(0.0, emptyStats.standardDeviation, 0.0);
        assertEquals(0, empty.toRunTimeTimeSeries("rt").getItemCount());
        assertEquals(0, empty.toMinMaxTimeSeries("mm").getItemCount());
        assertEquals(0, empty.toCountPerSecondTimeSeries("cps").getItemCount());
        assertEquals(0, empty.toErrorsPerSecondTimeSeries("eps").getItemCount());
        assertEquals(0, empty.toErrorRateTimeSeries("er").getItemCount());
        assertEquals(0, empty.toHistogramSeries("hs", 5).getItemCount());
        assertEquals(0, empty.toVerticalHistogramSeries("vhs", 5).getItemCount());

        // 2. Constant-value series (max == min -> variance == 0.0 and histogram bucketWidth == 1.0)
        final IntTimeSeries constant = new IntTimeSeries(64, 64);
        constant.addValue(msec(0), 100, false);
        constant.addValue(msec(1), 100, false);
        assertEquals(0.0, constant.getStandardDeviation(), 0.0);
        assertEquals(96, constant.getMedianValue());
        assertEquals(2, constant.getHistogram().getValueCount());
        final var constHist = constant.toHistogram(4);
        assertEquals(4, constHist.size());

        // Bimodal series (0 and 500) across 10 buckets leaves middle buckets empty (count == 0), testing count > 0 filter
        final IntTimeSeries bimodal = new IntTimeSeries(64, 64);
        bimodal.addValue(msec(0), 0, false);
        bimodal.addValue(msec(1), 500, false);
        assertEquals(2, bimodal.toHistogramSeries("hs", 10).getItemCount());
        final var vSeries = bimodal.toVerticalHistogramSeries("vhs", 10);
        assertEquals(2, vSeries.getItemCount());
        assertEquals(1.0, vSeries.getYValue(0), 0.001);

        // 3. Multi-page condensed series chart exports (with null middle page before condense, and slotWidth = 2)
        final IntTimeSeries multi = new IntTimeSeries(256, 256);
        multi.addValue(msec(0), 100, true);    // Page 0, slot 0: count=1, err=1
        multi.addValue(msec(0), 300, false);   // Page 0, slot 0: count=2, err=1
        multi.addValue(msec(130), 200, false); // Page 2, slot 2: count=1, err=0 (Page 1 is null!)
        assertEquals(2, multi.toRunTimeTimeSeries("rt").getItemCount());
        assertEquals(2, multi.toMinMaxTimeSeries("mm").getItemCount());

        // Condense to scale 2 (slotWidth = 2s):
        // Slot 0 (sec 0): count=2, err=1, maxErr=1 -> count/s = 1.0, errRate = 50.0%
        // Slot 65 (sec 130): count=1, err=0 -> count/s = 0.5, errRate = 0.0%
        multi.condenseOneStep();
        assertEquals(2, multi.getSlotWidth());

        final var rtSeries = multi.toMinMaxTimeSeries("mm");
        assertEquals(2, rtSeries.getItemCount());
        assertEquals(multi.getFirstSecond() * 1000L, rtSeries.getTimePeriod(0).getFirstMillisecond());
        assertEquals((multi.getFirstSecond() + 130L) * 1000L, rtSeries.getTimePeriod(1).getFirstMillisecond());

        final var cpsSeries = multi.toCountPerSecondTimeSeries("cps");
        assertEquals(66, cpsSeries.getItemCount());
        assertEquals(1.0, cpsSeries.getValue(0).doubleValue(), 0.0001);
        assertEquals(0.0, cpsSeries.getValue(1).doubleValue(), 0.0001);
        assertEquals(0.5, cpsSeries.getValue(65).doubleValue(), 0.0001);

        final var epsSeries = multi.toErrorsPerSecondTimeSeries("eps");
        assertEquals(66, epsSeries.getItemCount());
        assertEquals(1.0, epsSeries.getValue(0).doubleValue(), 0.0001);
        assertEquals(0.0, epsSeries.getValue(65).doubleValue(), 0.0001);

        final var erSeries = multi.toErrorRateTimeSeries("er");
        assertEquals(2, erSeries.getItemCount());
        assertEquals(multi.getFirstSecond() * 1000L, erSeries.getTimePeriod(0).getFirstMillisecond());
        assertEquals(50.0, erSeries.getValue(0).doubleValue(), 0.0001);
        assertEquals((multi.getFirstSecond() + 130L) * 1000L, erSeries.getTimePeriod(1).getFirstMillisecond());
        assertEquals(0.0, erSeries.getValue(1).doubleValue(), 0.0001);
    }

    private static void assertSlotMatchesReference(final String label, final IntTimeSeries series, final int pos, final IntTimeSeriesEntry expected)
    {
        assertEquals(label + " count", expected.getCount(), series.getSlotCount(pos));
        assertEquals(label + " errorCount", expected.getErrorCount(), series.getSlotErrorCount(pos));
        assertEquals(label + " maxErrorCount", expected.getMaxErrorCount(), series.getSlotMaxErrorCount(pos));
        assertEquals(label + " maxCount", expected.getMaxCount(), series.getSlotMaxCount(pos));
        assertEquals(label + " concurrentCount", expected.getConcurrentCount(), series.getSlotConcurrentCount(pos));
        assertEquals(label + " totalValue", expected.getTotalValue(), series.getSlotTotalValue(pos));
        assertEquals(label + " averageValue", expected.getAverageValue(), series.getSlotAverageValue(pos));
        assertEquals(label + " minimumValue", expected.getMinimumValue(), series.getSlotMinimumValue(pos));
        assertEquals(label + " maximumValue", expected.getMaximumValue(), series.getSlotMaximumValue(pos));
        assertEquals(label + " entry", expected, series.getEntry(pos));
    }
}

