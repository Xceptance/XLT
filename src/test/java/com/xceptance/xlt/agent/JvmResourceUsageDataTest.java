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
package com.xceptance.xlt.agent;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.util.XltCharBufferUtil;

/**
 * @author Sebastian Oerding
 */
public class JvmResourceUsageDataTest
{
    @Test
    public void testParse()
    {
        /*
         * - Taking powers of two to avoid problems with number representation of floats/ doubles - Using unique values
         * to easily find out what went wrong in case of a failure
         */
        final String cpuUsage = "1.0";
        final String committedMemorySize = "2";
        final String memoryUsage = "4.0";
        final String usedHeapSize = "8";
        final String totalHeapSize = "16";
        final String heapUsage = "32.0";
        final String runnableThreadCount = "64";
        final String blockedThreadCount = "128";
        final String waitingThreadCount = "256";
        final String minorGcCount = "512";
        final String minorGcTime = "1024";
        final String minorGcCpuUsage = "2048.0";
        final String fullGcCount = "4096";
        final String fullGcTime = "8192";
        final String fullGcCpuUsage = "16384.0";
        final String minorGcTimeDiff = "32768";
        final String fullGcTimeDiff = "65536";
        final String minorGcCountDiff = "131072";
        final String fullGcCountDiff = "262144";
        final String totalCpuUsage = "3.0";

        final String[] values = new String[]
            {
                "J", "JvmResourceUsageDataTest", "1", cpuUsage, committedMemorySize, memoryUsage, usedHeapSize, totalHeapSize, heapUsage,
                runnableThreadCount, blockedThreadCount, waitingThreadCount, minorGcCount, minorGcTime, minorGcCpuUsage, fullGcCount,
                fullGcTime, fullGcCpuUsage, minorGcTimeDiff, fullGcTimeDiff, minorGcCountDiff, fullGcCountDiff, totalCpuUsage
            };

        final JvmResourceUsageData data = new JvmResourceUsageData();

        data.initAllValues(XltCharBufferUtil.toSimpleArrayList(values));

        checkDoubleIsEqual("CPU", cpuUsage, data.getCpuUsage());
        checkLongIsEqual("committed memory size", committedMemorySize, data.getCommittedMemorySize());
        checkDoubleIsEqual("memory", memoryUsage, data.getMemoryUsage());
        checkLongIsEqual("used heap size", usedHeapSize, data.getUsedHeapSize());
        checkLongIsEqual("total heap size", totalHeapSize, data.getTotalHeapSize());
        checkDoubleIsEqual("heap", heapUsage, data.getHeapUsage());

        checkIntIsEqual("runnable thread count", runnableThreadCount, data.getRunnableThreadCount());
        checkIntIsEqual("waiting thread count", waitingThreadCount, data.getWaitingThreadCount());
        checkIntIsEqual("blocked thread count", blockedThreadCount, data.getBlockedThreadCount());

        checkLongIsEqual("minor Gc count", minorGcCount, data.getMinorGcCount());
        checkLongIsEqual("minor Gc time", minorGcTime, data.getMinorGcTime());
        checkDoubleIsEqual("minor Gc CPU", minorGcCpuUsage, data.getMinorGcCpuUsage());
        checkLongIsEqual("full Gc count", fullGcCount, data.getFullGcCount());
        checkLongIsEqual("full Gc time", fullGcTime, data.getFullGcTime());
        checkDoubleIsEqual("full Gc CPU", fullGcCpuUsage, data.getFullGcCpuUsage());

        checkIntIsEqual("minor Gc time diff", minorGcTimeDiff, data.getMinorGcTimeDiff());
        checkIntIsEqual("full Gc time diff", fullGcTimeDiff, data.getFullGcTimeDiff());

        checkIntIsEqual("minor Gc count diff", minorGcCountDiff, data.getMinorGcCountDiff());
        checkIntIsEqual("full Gc count diff", fullGcCountDiff, data.getFullGcCountDiff());

        checkDoubleIsEqual("total CPU", totalCpuUsage, data.getTotalCpuUsage());

        var a = data.getAllValues().toArray();
        Assert.assertArrayEquals(values, a);
    }

    private void checkDoubleIsEqual(final String valueName, final String expectedAsString, final double actual)
    {
        final double expected = Double.parseDouble(expectedAsString);
        Assert.assertTrue("Wrong " + valueName + " usage, expected \"" + expected + "\" but got \"" + actual + "\"!",
                          Double.compare(expected, actual) == 0);
    }

    private void checkLongIsEqual(final String valueName, final String expectedAsString, final long actual)
    {
        final long expected = Long.parseLong(expectedAsString);
        Assert.assertEquals("Wrong " + valueName, expected, actual);

    }

    private void checkIntIsEqual(final String valueName, final String expectedAsString, final int actual)
    {
        final int expected = Integer.parseInt(expectedAsString);
        Assert.assertEquals("Wrong " + valueName, expected, actual);
    }
}
