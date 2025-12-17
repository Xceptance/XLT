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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import com.xceptance.xlt.report.util.lucene.BitUtil;

import it.unimi.dsi.util.FastRandom;

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
        assertEquals(BitUtil.nextHighestPowerOfTwo(86400 / 32), series.getValues().length);

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
    public void testDigestPercentiles_Simple()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);

        series.addValue(msec(0), 10, false);
        series.addValue(msec(10), 20, false);
        series.addValue(msec(20), 30, false);

        assertEquals(10, series.getPercentile(0.0));
        assertEquals(10, series.getPercentile(25.0));
        assertEquals(20, series.getPercentile(50.0));
        assertEquals(30, series.getPercentile(75.0));
        assertEquals(30, series.getPercentile(100.0));
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
        assertEquals(25, series.getPercentile(25.0));
        assertEquals(50, series.getPercentile(50.0));
        assertEquals(75, series.getPercentile(75.0));
        assertEquals(99, series.getPercentile(100.0));
    }

    // test the digest function for percentiles with very equally distributed values
    @Test
    public void testDigestPercentiles_EquallyDistValues()
    {
        final IntTimeSeries series = new IntTimeSeries(3600);

        series.addValue(msec(200), 0, false);
        series.addValue(msec(200), 100, false);
        series.addValue(msec(300), 200, false);
        series.addValue(msec(400), 300, false);
        series.addValue(msec(500), 400, false);
        series.addValue(msec(600), 500, false);
        series.addValue(msec(700), 600, false);
        series.addValue(msec(700), 700, false);
        series.addValue(msec(700), 800, false);
        series.addValue(msec(700), 900, false);
        series.addValue(msec(800), 1000, false);

        assertEquals(0, series.getPercentile(0.0));
        assertEquals(100, series.getPercentile(10.0));
        assertEquals(200, series.getPercentile(20.0));
        assertEquals(300, series.getPercentile(30.0));
        assertEquals(400, series.getPercentile(40.0));
        assertEquals(500, series.getPercentile(50.0));
        assertEquals(600, series.getPercentile(60.0));
        assertEquals(700, series.getPercentile(70.0));
        assertEquals(800, series.getPercentile(80.0));
        assertEquals(900, series.getPercentile(90.0));
        assertEquals(1000, series.getPercentile(100.0));
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

        series.addValue(msec(200), 0, false);
        series.addValue(msec(200), 100, false);
        series.addValue(msec(300), 200, false);
        series.addValue(msec(400), 300, false);
        series.addValue(msec(500), 400, false);
        series.addValue(msec(600), 500, false);
        series.addValue(msec(700), 600, false);
        series.addValue(msec(700), 700, false);
        series.addValue(msec(700), 800, false);
        series.addValue(msec(700), 900, false);
        series.addValue(msec(800), 1000, false);

        var h = series.toHistogram(10);
        assertEquals(10, h.size());
        assertEquals(11, h.stream().mapToInt(b -> b.count()).sum());

        assertEquals(0, h.get(0).startValue());
        assertEquals(100, h.get(0).endValue());
        assertEquals(2, h.get(0).count());

        assertEquals(1, h.get(1).count());
        assertEquals(100, h.get(1).startValue());
        assertEquals(200, h.get(1).endValue());

        assertEquals(1, h.get(2).count());
        assertEquals(200, h.get(2).startValue());
        assertEquals(300, h.get(2).endValue());

        assertEquals(1, h.get(3).count());
        assertEquals(300, h.get(3).startValue());
        assertEquals(400, h.get(3).endValue());

        assertEquals(1, h.get(4).count());
        assertEquals(400, h.get(4).startValue());
        assertEquals(500, h.get(4).endValue());

        assertEquals(1, h.get(5).count());
        assertEquals(500, h.get(5).startValue());
        assertEquals(600, h.get(5).endValue());

        assertEquals(1, h.get(6).count());
        assertEquals(600, h.get(6).startValue());
        assertEquals(700, h.get(6).endValue());

        assertEquals(1, h.get(7).count());
        assertEquals(700, h.get(7).startValue());
        assertEquals(800, h.get(7).endValue());

        assertEquals(1, h.get(8).count());
        assertEquals(800, h.get(8).startValue());
        assertEquals(900, h.get(8).endValue());

        assertEquals(1, h.get(9).count());
        assertEquals(900, h.get(9).startValue());
        assertEquals(1000, h.get(9).endValue());
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

        assertEquals(3, series.getValues()[0].getCount());
        assertEquals(3, series.getValues()[0].getConcurrentCount());

        assertEquals(0, series.getValues()[1].getCount());
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

        assertEquals(1, series.getValues()[0].getCount());
        assertEquals(1, series.getValues()[0].getConcurrentCount());

        assertEquals(0, series.getValues()[1].getCount());
        assertEquals(0, series.getValues()[1].getConcurrentCount());

        series.addValue(BASE_TIME_MSEC, BASE_TIME_MSEC + 1500, 20, true);

        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC + 1, series.getLastSecond());

        assertEquals(2, series.getValues()[0].getCount());
        assertEquals(2, series.getValues()[0].getConcurrentCount());

        assertEquals(0, series.getValues()[1].getCount());
        assertEquals(1, series.getValues()[1].getConcurrentCount());

        assertEquals(0, series.getValues()[2].getCount());
        assertEquals(0, series.getValues()[2].getConcurrentCount());

        series.addValue(BASE_TIME_MSEC + 2000, BASE_TIME_MSEC + 3999, 30, true);

        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC + 3, series.getLastSecond());

        assertEquals(2, series.getValues()[0].getCount());
        assertEquals(2, series.getValues()[0].getConcurrentCount());

        assertEquals(0, series.getValues()[1].getCount());
        assertEquals(1, series.getValues()[1].getConcurrentCount());

        assertEquals(1, series.getValues()[2].getCount());
        assertEquals(1, series.getValues()[2].getConcurrentCount());

        assertEquals(0, series.getValues()[3].getCount());
        assertEquals(1, series.getValues()[3].getConcurrentCount());

        assertEquals(0, series.getValues()[4].getCount());
        assertEquals(0, series.getValues()[4].getConcurrentCount());

        // minimally touches the next sec... so what
        series.addValue(BASE_TIME_MSEC + 2000, BASE_TIME_MSEC + 4000, 30, true);

        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC + 4, series.getLastSecond());

        assertEquals(2, series.getValues()[0].getCount());
        assertEquals(2, series.getValues()[0].getConcurrentCount());

        assertEquals(0, series.getValues()[1].getCount());
        assertEquals(1, series.getValues()[1].getConcurrentCount());

        assertEquals(2, series.getValues()[2].getCount());
        assertEquals(2, series.getValues()[2].getConcurrentCount());

        assertEquals(0, series.getValues()[3].getCount());
        assertEquals(2, series.getValues()[3].getConcurrentCount());

        assertEquals(0, series.getValues()[4].getCount());
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
        series.addValue(BASE_TIME_MSEC, BASE_TIME_MSEC + 1500, 10, true);

        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC + 1, series.getLastSecond());

        // no growth yet
        // sec 0
        assertEquals(1, series.getValues()[0].getCount());
        assertEquals(1, series.getValues()[0].getConcurrentCount());

        // sec 1
        assertEquals(0, series.getValues()[1].getCount());
        assertEquals(1, series.getValues()[1].getConcurrentCount());

        // trigger growth but just being outside not by going over the end
        series.addValue(BASE_TIME_MSEC + 16000, 
                        BASE_TIME_MSEC + 16000 + 500, 10, true);

        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC + 16, series.getLastSecond());

        // sec 0 and 1, we correctly keep concurrent count and don't add it up
        assertEquals(1, series.getValues()[0].getCount());
        assertEquals(1, series.getValues()[0].getConcurrentCount());

        // sec 2 and 3
        assertEquals(0, series.getValues()[1].getCount());
        assertEquals(0, series.getValues()[1].getConcurrentCount());

        // sec 16 and 17
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
        series.addValue(BASE_TIME_MSEC, BASE_TIME_MSEC + 1500, 10, true);

        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC + 1, series.getLastSecond());
        assertEquals(1, series.getScale());

        // no growth yet
        // sec 0
        assertEquals(1, series.getValues()[0].getCount());
        assertEquals(1, series.getValues()[0].getConcurrentCount());

        // sec 1
        assertEquals(0, series.getValues()[1].getCount());
        assertEquals(1, series.getValues()[1].getConcurrentCount());

        // trigger growth by being beyond the end with the length
        series.addValue(BASE_TIME_MSEC + 15500, 
                        BASE_TIME_MSEC + 15500 + 1500, 10, true);

        assertEquals(2, series.getScale());
        assertEquals(BASE_TIME_SEC, series.getFirstSecond());
        assertEquals(BASE_TIME_SEC + 16, series.getLastSecond());

        // sec 0 and 1, we correctly keep concurrent count and don't add it up
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
        
        // sec 14 and 15
        assertEquals(1, series.getValues()[7].getCount());
        assertEquals(1, series.getValues()[7].getConcurrentCount());

        // sec 16 and 17
        assertEquals(0, series.getValues()[8].getCount());
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
        var r = new FastRandom(12345L);
        for (int i = 0; i < 1000000; i++) 
        {
            series.addValue(100, r.nextInt(100_000_000), false);
        }
    }
}

