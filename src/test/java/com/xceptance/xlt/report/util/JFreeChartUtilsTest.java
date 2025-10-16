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

import java.util.Date;

import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.xceptance.xlt.report.ReportGeneratorMain;

/**
 * @author Sebastian Oerding
 */
public class JFreeChartUtilsTest
{
    /**
     * This tests {@link JFreeChartUtils#toMinMaxTimeSeries(IntMinMaxValueSet, String)} and causes an error due to a large
     * scale in the MinMaxValueSet and multiplication with 1000 in the method in JFreeChartUtils.
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
}
