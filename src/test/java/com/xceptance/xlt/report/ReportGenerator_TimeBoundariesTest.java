/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test {@linkplain ReportGenerator#getTimeBoundaries(long, long, long, boolean, boolean, boolean, long, long)}. Ramp up
 * time is not cut off.
 */
@RunWith(Parameterized.class)
public class ReportGenerator_TimeBoundariesTest
{
    /**
     * Dummy properties, empty
     */
    private static final Properties PROPERTIES_DUMMY = new Properties();

    /**
     * in milliseconds
     */
    private static final long TIMESTAMP_START = 1420452000000L; // 20150105-100000

    /**
     * in milliseconds
     */
    private static final long TEST_ELAPSED_TIME = 7200000; // 2 hours

    /**
     * in milliseconds
     */
    private static final long TIMESTAMP_END = TIMESTAMP_START + TEST_ELAPSED_TIME; // 20150105-120000 // 1420459200000

    /**
     * 1 hour, in milliseconds
     */
    private static final long DURATION = 3600000;

    private static final long DURATION_NOT_SPECIFIED = -1;

    /**
     * 1 second, in milliseconds
     */
    private static final long PERIOD_1s = 1000;

    /**
     * 30 minutes, in milliseconds
     */
    private static final long PERIOD_30m = 1800000;

    /**
     * 1 hour 30 minutes = 90 minutes, in milliseconds
     */
    private static final long PERIOD_1h30m = 5400000;

    /**
     * 1 hour, in milliseconds
     */
    private static final long PERIOD_1h = 3600000;

    /**
     * 24 hours, in milliseconds
     */
    private static final long PERIOD_24h = 86400000;

    private File DUMMY_DIR;

    private FileObject DUMMY_INPUT_DIR;

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data()
    {
        /**
         * description<br>
         * from<br>
         * to<br>
         * duration<br>
         * isFromRelative<br>
         * isToRelative<br>
         * expectedFrom<br>
         * expectedTo
         */
        return Arrays.asList(new Object[][]
            {
                    {
                        "<no_limit>", 0, Long.MAX_VALUE, DURATION_NOT_SPECIFIED, false, false, 0, Long.MAX_VALUE
                    },
                    {
                        "-from +0s", 0, Long.MAX_VALUE, DURATION_NOT_SPECIFIED, true, false, TIMESTAMP_START, Long.MAX_VALUE
                    },
                    {
                        "-to +0s", 0, 0, DURATION_NOT_SPECIFIED, false, true, 0, TIMESTAMP_START
                    },
                    {
                        "from +1s", PERIOD_1s, Long.MAX_VALUE, DURATION_NOT_SPECIFIED, true, false, TIMESTAMP_START + PERIOD_1s,
                        Long.MAX_VALUE
                    },
                    {
                        "-to +1s", 0, PERIOD_1s, DURATION_NOT_SPECIFIED, false, true, 0, TIMESTAMP_START + PERIOD_1s
                    },
                    {
                        "-from -1s", -PERIOD_1s, Long.MAX_VALUE, DURATION_NOT_SPECIFIED, true, false, TIMESTAMP_END - PERIOD_1s,
                        Long.MAX_VALUE
                    },
                    {
                        "-to -1s", 0, -PERIOD_1s, DURATION_NOT_SPECIFIED, false, true, 0, TIMESTAMP_END - PERIOD_1s
                    },
                    {
                        "-from 20150105-100000", TIMESTAMP_START, Long.MAX_VALUE, DURATION_NOT_SPECIFIED, false, false, TIMESTAMP_START,
                        Long.MAX_VALUE
                    },
                    {
                        "-to 20150105-120000", 0, TIMESTAMP_END, DURATION_NOT_SPECIFIED, false, false, 0, TIMESTAMP_END
                    },
                    {
                        "-from +1h", PERIOD_1h, Long.MAX_VALUE, DURATION_NOT_SPECIFIED, true, false, TIMESTAMP_START + DURATION,
                        Long.MAX_VALUE
                    },
                    {
                        "-to +1h", 0, PERIOD_1h, DURATION_NOT_SPECIFIED, false, true, 0, TIMESTAMP_START + DURATION
                    },
                    {
                        "-from +0s -l 1h", 0, Long.MAX_VALUE, DURATION, true, false, TIMESTAMP_START, TIMESTAMP_START + DURATION
                    },
                    {
                        "-from +1s -l 1h", PERIOD_1s, Long.MAX_VALUE, DURATION, true, false, TIMESTAMP_START + PERIOD_1s,
                        TIMESTAMP_START + PERIOD_1s + DURATION
                    },
                    {
                        "-from +1s -l 0", PERIOD_1s, Long.MAX_VALUE, 0, true, false, TIMESTAMP_START + PERIOD_1s,
                        TIMESTAMP_START + PERIOD_1s
                    },
                    {
                        "-to 20150105-120000 -l 1h", 0, TIMESTAMP_END, DURATION, false, false, TIMESTAMP_END - DURATION, TIMESTAMP_END
                    },
                    {
                        "-from 20150105-103000 -to 20150105-113000", TIMESTAMP_START + PERIOD_30m, TIMESTAMP_END - PERIOD_30m,
                        DURATION_NOT_SPECIFIED, false, false, TIMESTAMP_START + PERIOD_30m, TIMESTAMP_END - PERIOD_30m
                    },
                    {
                        "-from +30m -to +1h30m", PERIOD_30m, PERIOD_1h30m, DURATION_NOT_SPECIFIED, true, true,
                        TIMESTAMP_START + PERIOD_30m, TIMESTAMP_END - PERIOD_30m
                    },
                    {
                        "<BAD BUT POSSIBLE> -from +1s -l -1", PERIOD_1s, Long.MAX_VALUE, -1, true, false, TIMESTAMP_START + PERIOD_1s,
                        Long.MAX_VALUE
                    },
                    {
                        "<BAD BUT POSSIBLE> 'from' negative, not relative, do not change", -1, Long.MAX_VALUE, DURATION_NOT_SPECIFIED,
                        false, false, -1, Long.MAX_VALUE
                    },
                    {
                        "<BAD BUT POSSIBLE> 'from' more than 24h, not relative, do not change", PERIOD_24h + 1, Long.MAX_VALUE,
                        DURATION_NOT_SPECIFIED, false, false, PERIOD_24h + 1, Long.MAX_VALUE
                    }
            });
    }

    @Parameter(value = 0)
    public String description;

    @Parameter(value = 1)
    public long from;

    @Parameter(value = 2)
    public long to;

    @Parameter(value = 3)
    public long duration;

    @Parameter(value = 4)
    public boolean isFromRelative;

    @Parameter(value = 5)
    public boolean isToRelative;

    @Parameter(value = 6)
    public long expectedFrom;

    @Parameter(value = 7)
    public long expectedTo;

    @Before
    public void init() throws IOException
    {
        DUMMY_DIR = tempFolder.getRoot();
        DUMMY_INPUT_DIR = VFS.getManager().resolveFile(DUMMY_DIR.getAbsolutePath());
    }

    /**
     * Invoke ReportGenerator's 'getTimeBoundaries' method with given parameters.
     */
    @Test
    public void check() throws Exception
    {
        // create dummy report generator
        final ReportGenerator rg = new ReportGenerator(DUMMY_INPUT_DIR, DUMMY_DIR, true, false, null, PROPERTIES_DUMMY, null, null, null, null);

        // get access to method of interest
        final Method m = rg.getClass().getDeclaredMethod("getTimeBoundaries", long.class, long.class, long.class, boolean.class,
                                                         boolean.class, boolean.class, long.class, long.class);
        m.setAccessible(true);

        // invoke method with test parameters
        final long[] calculatedTime = (long[]) m.invoke(rg, from, to, duration, false, isFromRelative, isToRelative, TIMESTAMP_START,
                                                        TEST_ELAPSED_TIME);

        // validate calculations
        Assert.assertEquals(description + ": FROM", expectedFrom, calculatedTime[0]);
        Assert.assertEquals(description + ": TO", expectedTo, calculatedTime[1]);
    }
}
