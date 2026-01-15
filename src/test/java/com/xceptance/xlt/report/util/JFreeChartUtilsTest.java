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

import java.util.Date;

import org.jfree.data.time.Minute;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.xceptance.xlt.api.report.MovingAverageConfiguration;
import com.xceptance.xlt.report.ReportGeneratorMain;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * @author Sebastian Oerding
 */
@RunWith(JUnitParamsRunner.class)
public class JFreeChartUtilsTest
{
    /**
     * This tests {@link JFreeChartUtils#toMinMaxTimeSeries(IntMinMaxValueSet, String)} and causes an error due to a
     * large scale in the MinMaxValueSet and multiplication with 1000 in the method in JFreeChartUtils.
     */
    @Ignore
    @Test
    public void testToMinMaxTimeSeries_OverFlowProblem()
    {
        // System.out.println("TEST Overflow error due to large scale and multiplication with 1000");
        final IntMinMaxValueSet set = new IntMinMaxValueSet(2);
        set.addOrUpdateValue(1288483261166L, 3);
        set.addOrUpdateValue(0, 4);

        JFreeChartUtils.toMinMaxTimeSeries(set, "Test");
    }

    /**
     * Thus test was used to investigate for the problem. But it is sufficient to have a timers.csv with the timestamps
     * &quot;1288483983648&quot; and &quot;1288487582077&quot; for example.
     */
    @SuppressWarnings("unused")
    @Ignore
    @Test
    public void testDataFromBugTracker()
    {
        // You have to set an appropriate path
        final String path = null; // "/home/soerding/Downloads/xlt-4.1.7-r8411/results/20120305-113923"
        if (path != null)
        {
            ReportGeneratorMain.main(new String[]
                {
                    path
                });
        }
        else
        {
            Assert.fail("You should not call this test accidentally.");
        }
    }

    @Ignore
    @Test
    public void testNewSeconds()
    {
        final Second s1 = new Second(new Date(1288483983648L)); // Sun Oct 31 02:13:03 CEST 2010
        final Second s2 = new Second(new Date(1288487583648L)); // Sun Oct 31 02:13:03 CET 2010
        Assert.assertFalse("Equal first millisecond!", s1.getFirstMillisecond() == s2.getFirstMillisecond()); // fails
        Assert.assertFalse("Equal serial index!", s1.getSerialIndex() == s2.getSerialIndex()); // fails
        Assert.assertFalse("Equal hash code!", s1.hashCode() == s2.hashCode()); // fails
        final TimeSeries t = new TimeSeries("test");
        t.add(s1, 1.0);
        t.add(s2, 1.0); // fails
    }

    /**
     * Test that moving average series is calculated correctly for a source series with no time gaps between data
     * points.
     */
    @Test
    @Parameters(method = "provideMovingAverageConfigs")
    public void createMovingAverageTimeSeries(final MovingAverageConfiguration movingAverageConfig)
    {
        // Prepare source series with not time gaps between data points
        final TimeSeries series = new TimeSeries("Test");
        series.add(getSecond(10), 100);
        series.add(getSecond(11), 200);
        series.add(getSecond(12), 900);
        series.add(getSecond(13), 700);
        series.add(getSecond(14), 650);
        series.add(getSecond(15), 300);
        series.add(getSecond(16), 2200);
        series.add(getSecond(17), 200);
        series.add(getSecond(18), 0);
        series.add(getSecond(19), 422.5);
        series.add(getSecond(20), 27.5);
        series.add(getSecond(21), 450);

        // Calculate average over the last 25% of values or the last 3 seconds. Since there are 12 points in total with
        // no gaps, this results in the average over the last 3 points in both cases.
        final TimeSeries result = JFreeChartUtils.createMovingAverageTimeSeries(series, movingAverageConfig);

        // Validate result name and size
        Assert.assertEquals("Test Average (" + movingAverageConfig.getName() + ")", result.getKey());
        Assert.assertEquals(12, result.getItemCount());

        // Time periods of the result series should match time periods of the source series
        Assert.assertEquals(getSecond(10), result.getTimePeriod(0));
        Assert.assertEquals(getSecond(11), result.getTimePeriod(1));
        Assert.assertEquals(getSecond(12), result.getTimePeriod(2));
        Assert.assertEquals(getSecond(13), result.getTimePeriod(3));
        Assert.assertEquals(getSecond(14), result.getTimePeriod(4));
        Assert.assertEquals(getSecond(15), result.getTimePeriod(5));
        Assert.assertEquals(getSecond(16), result.getTimePeriod(6));
        Assert.assertEquals(getSecond(17), result.getTimePeriod(7));
        Assert.assertEquals(getSecond(18), result.getTimePeriod(8));
        Assert.assertEquals(getSecond(19), result.getTimePeriod(9));
        Assert.assertEquals(getSecond(20), result.getTimePeriod(10));
        Assert.assertEquals(getSecond(21), result.getTimePeriod(11));

        // Validate result values are average over the last 3 points of the source series
        Assert.assertEquals(100.0, result.getValue(0));
        Assert.assertEquals(150.0, result.getValue(1));
        Assert.assertEquals(400.0, result.getValue(2));
        Assert.assertEquals(600.0, result.getValue(3));
        Assert.assertEquals(750.0, result.getValue(4));
        Assert.assertEquals(550.0, result.getValue(5));
        Assert.assertEquals(1050.0, result.getValue(6));
        Assert.assertEquals(900.0, result.getValue(7));
        Assert.assertEquals(800.0, result.getValue(8));
        Assert.assertEquals(207.5, result.getValue(9));
        Assert.assertEquals(150.0, result.getValue(10));
        Assert.assertEquals(300.0, result.getValue(11));
    }

    /**
     * Test percentage average is calculated correctly if there are time gaps between points in the source series.
     */
    @Test
    public void createMovingAverageTimeSeries_percentageAverage_gapsBetweenPoints()
    {
        // Prepare series with gaps between time values
        final TimeSeries series = new TimeSeries("Test");
        series.add(getSecond(10), 100);
        series.add(getSecond(25), 200);
        series.add(getSecond(200), 900);
        series.add(getSecond(500), 700);
        series.add(getSecond(750), 650);
        series.add(getSecond(1521), 300);
        series.add(getSecond(2700), 2200);
        series.add(getSecond(2701), 200);
        series.add(getSecond(3600), 0);
        series.add(getSecond(3601), 422.5);

        // Calculate moving average over the last 33% of values.
        // In this case this means "over the last 3 data points"; time gaps between points are ignored.
        final TimeSeries result = JFreeChartUtils.createMovingAverageTimeSeries(series,
                                                                                MovingAverageConfiguration.createPercentageConfig(33));

        // Validate result series name and size
        Assert.assertEquals("Test Average (33%)", result.getKey());
        Assert.assertEquals(10, result.getItemCount());

        // Time periods in result series should match time periods of the source series
        Assert.assertEquals(getSecond(10), result.getTimePeriod(0));
        Assert.assertEquals(getSecond(25), result.getTimePeriod(1));
        Assert.assertEquals(getSecond(200), result.getTimePeriod(2));
        Assert.assertEquals(getSecond(500), result.getTimePeriod(3));
        Assert.assertEquals(getSecond(750), result.getTimePeriod(4));
        Assert.assertEquals(getSecond(1521), result.getTimePeriod(5));
        Assert.assertEquals(getSecond(2700), result.getTimePeriod(6));
        Assert.assertEquals(getSecond(2701), result.getTimePeriod(7));
        Assert.assertEquals(getSecond(3600), result.getTimePeriod(8));
        Assert.assertEquals(getSecond(3601), result.getTimePeriod(9));

        // Validate result values are the averages over the last 3 data points regardless of the time between points
        Assert.assertEquals(100.0, result.getValue(0));
        Assert.assertEquals(150.0, result.getValue(1));
        Assert.assertEquals(400.0, result.getValue(2));
        Assert.assertEquals(600.0, result.getValue(3));
        Assert.assertEquals(750.0, result.getValue(4));
        Assert.assertEquals(550.0, result.getValue(5));
        Assert.assertEquals(1050.0, result.getValue(6));
        Assert.assertEquals(900.0, result.getValue(7));
        Assert.assertEquals(800.0, result.getValue(8));
        Assert.assertEquals(207.5, result.getValue(9));
    }

    /**
     * Test time average is calculated correctly if there are time gaps between points in the source series.
     */
    @Test
    public void createMovingAverageTimeSeries_timeAverage_gapsBetweenPoints()
    {
        // Prepare series with gaps between time values, so the number of data points in the average interval fluctuates
        final TimeSeries series = new TimeSeries("Test");
        series.add(getSecond(10), 100);
        series.add(getSecond(25), 200);
        series.add(getSecond(200), 300);
        series.add(getSecond(500), 1000);
        series.add(getSecond(750), 150);
        series.add(getSecond(1521), 850);
        series.add(getSecond(2700), 1300);
        series.add(getSecond(2701), 100);
        series.add(getSecond(3600), 50);
        series.add(getSecond(3601), 2);

        // Calculate moving average over the last 900 seconds (i.e. 15 minutes)
        final TimeSeries result = JFreeChartUtils.createMovingAverageTimeSeries(series, MovingAverageConfiguration.createTimeConfig(900));

        // Validate result series name and size
        Assert.assertEquals("Test Average (900s)", result.getKey());
        Assert.assertEquals(10, result.getItemCount());

        // Time periods in result series should match time periods of the source series
        Assert.assertEquals(getSecond(10), result.getTimePeriod(0));
        Assert.assertEquals(getSecond(25), result.getTimePeriod(1));
        Assert.assertEquals(getSecond(200), result.getTimePeriod(2));
        Assert.assertEquals(getSecond(500), result.getTimePeriod(3));
        Assert.assertEquals(getSecond(750), result.getTimePeriod(4));
        Assert.assertEquals(getSecond(1521), result.getTimePeriod(5));
        Assert.assertEquals(getSecond(2700), result.getTimePeriod(6));
        Assert.assertEquals(getSecond(2701), result.getTimePeriod(7));
        Assert.assertEquals(getSecond(3600), result.getTimePeriod(8));
        Assert.assertEquals(getSecond(3601), result.getTimePeriod(9));

        // Validate result values are the averages over the last X data points in the average time interval
        Assert.assertEquals(100.0, result.getValue(0)); // average over 1 value
        Assert.assertEquals(150.0, result.getValue(1)); // average over 2 values
        Assert.assertEquals(200.0, result.getValue(2)); // average over 3 values
        Assert.assertEquals(400.0, result.getValue(3)); // average over 4 values
        Assert.assertEquals(350.0, result.getValue(4)); // average over 5 values
        Assert.assertEquals(500.0, result.getValue(5)); // average over 2 values
        Assert.assertEquals(1300.0, result.getValue(6)); // average over 1 value
        Assert.assertEquals(700.0, result.getValue(7)); // average over 2 values
        Assert.assertEquals(75.0, result.getValue(8)); // average over 2 values
        Assert.assertEquals(26.0, result.getValue(9)); // average over 2 values
    }

    /**
     * Test percentage average is calculated correctly for the min percentage value or even lower values (i.e. "1%" or
     * less). Values lower than the minimum of "1%" should still result in the average over 1% of values.
     */
    @Test
    @Parameters(value =
        {
            "1", "0", "-1"
    })
    public void createMovingAverageTimeSeries_percentageAverage_minOrLowerValues(final int percentage)
    {
        // Prepare a simple series with 200 values, so the average over the last 1% of values is the average over the
        // last 2 data points
        final TimeSeries series = new TimeSeries("Test");
        for (int i = 0; i < 200; i++)
        {
            // Use iteration index as the time and value for simplicity
            series.add(getSecond(i), i);
        }

        // The used percentage values result in "the average over the last 1% of values". In this case, this is the
        // average over the last 2 data points.
        final TimeSeries result = JFreeChartUtils.createMovingAverageTimeSeries(series,
                                                                                MovingAverageConfiguration.createPercentageConfig(percentage));

        // Validate result series name and size
        Assert.assertEquals("Test Average (" + percentage + "%)", result.getKey());
        Assert.assertEquals(200, result.getItemCount());

        // Validate first point of the result series matches source series
        Assert.assertEquals(getSecond(0), result.getTimePeriod(0));
        Assert.assertEquals(0.0, result.getValue(0));

        // The remaining points of the result series should be the average over the last 2 data points
        for (int i = 1; i < 200; i++)
        {
            Assert.assertEquals(getSecond(i), result.getTimePeriod(i));
            Assert.assertEquals(((double) (2 * i - 1)) / 2, result.getValue(i));
        }
    }

    /**
     * Test time average is calculated correctly for the min time interval value or even lower values (i.e. "1s" or
     * less). Config values lower than the minimum of "1s" should still result in the average over a 1s interval.
     */
    @Test
    @Parameters(value =
        {
            "1", "0", "-1"
    })
    public void createMovingAverageTimeSeries_timeAverage_minOrLowerValues(final int seconds)
    {
        // Prepare source series
        final TimeSeries series = new TimeSeries("Test");
        series.add(getSecond(11), 100);
        series.add(getSecond(12), 200);
        series.add(getSecond(13), 900);
        series.add(getSecond(15), 500);
        series.add(getSecond(16), 400);

        // The used second values result in "the average over the last 1 second". Since the resolution of the source
        // series is 1 second, this is the average over 1 data point, i.e. the result is identical to the source series.
        final TimeSeries result = JFreeChartUtils.createMovingAverageTimeSeries(series,
                                                                                MovingAverageConfiguration.createTimeConfig(seconds));

        // Validate result series name and size
        Assert.assertEquals("Test Average (" + seconds + "s)", result.getKey());
        Assert.assertEquals(5, result.getItemCount());

        // Time periods of the result series should match time periods of the source series
        Assert.assertEquals(getSecond(11), result.getTimePeriod(0));
        Assert.assertEquals(getSecond(12), result.getTimePeriod(1));
        Assert.assertEquals(getSecond(13), result.getTimePeriod(2));
        Assert.assertEquals(getSecond(15), result.getTimePeriod(3));
        Assert.assertEquals(getSecond(16), result.getTimePeriod(4));

        // Result values match values from the source series
        Assert.assertEquals(100.0, result.getValue(0));
        Assert.assertEquals(200.0, result.getValue(1));
        Assert.assertEquals(900.0, result.getValue(2));
        Assert.assertEquals(500.0, result.getValue(3));
        Assert.assertEquals(400.0, result.getValue(4));
    }

    /**
     * Test moving average is calculated correctly for configurations that reach or exceed the max value (i.e.
     * percentages of "100%" or higher, or times that are equal or greater than the source series runtime). In those
     * cases, the result series should return the averages over all previous points in the series.
     */
    @Test
    @Parameters(method = "provideMovingAverageConfigsWithMaxOrHigherValues")
    public void createMovingAverageTimeSeries_maxOrHigherValues(final MovingAverageConfiguration config)
    {
        // Prepare source series with a total time range of 3600 seconds
        final TimeSeries series = new TimeSeries("Test");
        series.add(getSecond(11), 100);
        series.add(getSecond(12), 200);
        series.add(getSecond(100), 900);
        series.add(getSecond(1500), 800);
        series.add(getSecond(3610), 1000);

        // Config values match or exceed 100% or the max series runtime, so average is calculated over all points in the
        // series so far
        final TimeSeries result = JFreeChartUtils.createMovingAverageTimeSeries(series, config);

        // Validate result series name and size
        Assert.assertEquals("Test Average (" + config.getName() + ")", result.getKey());
        Assert.assertEquals(5, result.getItemCount());

        // Time periods of the result series should match time periods of the source series
        Assert.assertEquals(getSecond(11), result.getTimePeriod(0));
        Assert.assertEquals(getSecond(12), result.getTimePeriod(1));
        Assert.assertEquals(getSecond(100), result.getTimePeriod(2));
        Assert.assertEquals(getSecond(1500), result.getTimePeriod(3));
        Assert.assertEquals(getSecond(3610), result.getTimePeriod(4));

        // Result values are average over all values in the series so far
        Assert.assertEquals(100.0, result.getValue(0)); // average over 1 value
        Assert.assertEquals(150.0, result.getValue(1)); // average over 2 values
        Assert.assertEquals(400.0, result.getValue(2)); // average over 3 values
        Assert.assertEquals(500.0, result.getValue(3)); // average over 4 values
        Assert.assertEquals(600.0, result.getValue(4)); // average over 5 values
    }

    /**
     * Test time average is calculated correctly if the time period setting of the source series is "Minute" instead of
     * "Second".
     */
    @Test
    public void createMovingAverageTimeSeries_timeAverage_seriesTimePeriodSettingIsMinute()
    {
        // Prepare source series with a "Minute" time period setting instead of "Second"
        final TimeSeries series = new TimeSeries("Test");
        series.add(getMinute(2), 100);
        series.add(getMinute(3), 200);
        series.add(getMinute(4), 900);
        series.add(getMinute(7), 700);
        series.add(getMinute(9), 600);

        // Calculate average over the last 121 seconds (i.e. 2 minutes and 1 second)
        final TimeSeries result = JFreeChartUtils.createMovingAverageTimeSeries(series, MovingAverageConfiguration.createTimeConfig(121));

        // Time periods of the result series should match time periods of the source series
        Assert.assertEquals(getMinute(2), result.getTimePeriod(0));
        Assert.assertEquals(getMinute(3), result.getTimePeriod(1));
        Assert.assertEquals(getMinute(4), result.getTimePeriod(2));
        Assert.assertEquals(getMinute(7), result.getTimePeriod(3));
        Assert.assertEquals(getMinute(9), result.getTimePeriod(4));

        // Result values are average over all values in the time interval
        Assert.assertEquals(100.0, result.getValue(0)); // average over 1 value
        Assert.assertEquals(150.0, result.getValue(1)); // average over 2 values
        Assert.assertEquals(400.0, result.getValue(2)); // average over 3 values
        Assert.assertEquals(700.0, result.getValue(3)); // average over 1 values
        Assert.assertEquals(650.0, result.getValue(4)); // average over 2 values
    }

    /**
     * Test moving average calculation returns an empty result series if the source series is empty.
     */
    @Test
    @Parameters(method = "provideMovingAverageConfigs")
    public void createMovingAverageTimeSeries_emptySeries(final MovingAverageConfiguration config)
    {
        final TimeSeries result = JFreeChartUtils.createMovingAverageTimeSeries(new TimeSeries("Test"), config);
        Assert.assertEquals("Test Average (" + config.getName() + ")", result.getKey());
        Assert.assertEquals(0, result.getItemCount());
    }

    /**
     * Helper method to get a "Second" object for the given second number.
     */
    private Second getSecond(final int second)
    {
        return new Second(new Date(second * 1000L));
    }

    /**
     * Helper method to get a "Minute" object for the given minute number.
     */
    private Minute getMinute(final int minute)
    {
        return new Minute(new Date(minute * 60L * 1000L));
    }

    /**
     * Provides simple moving average configurations.
     */
    @SuppressWarnings("unused")
    private Object[] provideMovingAverageConfigs()
    {
        return new Object[]
            {
                MovingAverageConfiguration.createPercentageConfig(25), MovingAverageConfiguration.createTimeConfig(3)
            };
    }

    /**
     * Provides moving average configurations that reach or exceed the max possible values (i.e. percentages of "100%"
     * or higher, or times that match or exceed the total time range of the source series). This method assumes a total
     * source series time range of 3600s and should be used in tests accordingly.
     */
    @SuppressWarnings("unused")
    private Object[] provideMovingAverageConfigsWithMaxOrHigherValues()
    {
        return new Object[]
            {
                // percentage averages set to 100% or more
                MovingAverageConfiguration.createPercentageConfig(100),                 // at the limit
                MovingAverageConfiguration.createPercentageConfig(101),                 // above the limit
                MovingAverageConfiguration.createPercentageConfig(Integer.MAX_VALUE),   // max value
                // time averages set to 3600s or more
                MovingAverageConfiguration.createTimeConfig(3600),              // at the limit
                MovingAverageConfiguration.createTimeConfig(3601),              // above the limit
                MovingAverageConfiguration.createTimeConfig(Integer.MAX_VALUE)          // max value
            };
    }
}
