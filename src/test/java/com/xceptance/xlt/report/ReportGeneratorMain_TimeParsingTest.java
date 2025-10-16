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
package com.xceptance.xlt.report;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ReportGeneratorMain_TimeParsingTest
{
    private static final long DURATION_NOT_SPECIFIED = -1;

    private static final long FROM_NOT_SPECIFIED = 0;

    private static final long TO_NOT_SPECIFIED = Long.MAX_VALUE;

    private static final long TEST_START = 1420452000000L; // 20150105-100000

    private static final long TEST_END = 1420455600000L; // 20150105-110000

    private static final long PERIOD_1s = 1000;

    private static final long PERIOD_30m = 1800000;

    private static final long PERIOD_1h = 3600000;

    private static final long PERIOD_010000 = 3600000;

    private static final long PERIOD_1h30m = 5400000;

    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data()
    {
        /**
         * type PASS/FAIL commandline args<br>
         * expected From<br>
         * expected To<br>
         * expected Duration<br>
         * expected isFromRelative<br>
         * expected isToRelative
         */
        return Arrays.asList(new Object[][]
            {
                    // without limitation
                {
                    "<no_time_limitation_args>", new String[] {}, FROM_NOT_SPECIFIED, TO_NOT_SPECIFIED, DURATION_NOT_SPECIFIED, false,
                    false
                },
                // accumulated time
                {
                    "-from +1h30m", new String[]
                        {
                            "-from", "+1h30m"
                        }, PERIOD_1h30m, TO_NOT_SPECIFIED, DURATION_NOT_SPECIFIED, true, false
                },
                {
                    "-to +1h30m", new String[]
                        {
                            "-to", "+1h30m"
                        }, FROM_NOT_SPECIFIED, PERIOD_1h30m, DURATION_NOT_SPECIFIED, false, true
                },
                {
                    "-from \"+1h 30m\"", new String[]
                        {
                            "-from", "+1h 30m"
                        }, PERIOD_1h30m, TO_NOT_SPECIFIED, DURATION_NOT_SPECIFIED, true, false
                },
                // digit time
                {
                    "-from +1:00:00", new String[]
                        {
                            "-from", "+1:00:00"
                        }, PERIOD_010000, TO_NOT_SPECIFIED, DURATION_NOT_SPECIFIED, true, false
                },
                {
                    "-to +1:00:00", new String[]
                        {
                            "-to", "+1:00:00"
                        }, FROM_NOT_SPECIFIED, PERIOD_010000, DURATION_NOT_SPECIFIED, false, true
                },
                // negative time
                {
                    "-from -1s", new String[]
                        {
                            "-from", "-1s"
                        }, -PERIOD_1s, TO_NOT_SPECIFIED, DURATION_NOT_SPECIFIED, true, false
                },
                {
                    "-to -1h", new String[]
                        {
                            "-to", "-1h"
                        }, FROM_NOT_SPECIFIED, -PERIOD_1h, DURATION_NOT_SPECIFIED, false, true
                },
                {
                    "-from -1:00:00", new String[]
                        {
                            "-from", "-1:00:00"
                        }, -PERIOD_1h, TO_NOT_SPECIFIED, DURATION_NOT_SPECIFIED, true, false
                },
                {
                    "-to -1:00:00", new String[]
                        {
                            "-to", "-1:00:00"
                        }, FROM_NOT_SPECIFIED, -PERIOD_1h, DURATION_NOT_SPECIFIED, false, true
                },
                // absolute
                {
                    "-from 20150105-100000", new String[]
                        {
                            "-from", "20150105-100000"
                        }, TEST_START, TO_NOT_SPECIFIED, DURATION_NOT_SPECIFIED, false, false
                },
                {
                    "-to 20150105-100000", new String[]
                        {
                            "-to", "20150105-100000"
                        }, FROM_NOT_SPECIFIED, TEST_START, DURATION_NOT_SPECIFIED, false, false
                },
                {
                    "-from 20150105-100000 -to 20150105-110000", new String[]
                        {
                            "-from", "20150105-100000", "-to", "20150105-110000"
                        }, TEST_START, TEST_END, DURATION_NOT_SPECIFIED, false, false
                },
                // whitespace covered
                {
                    "-from \" 20150105-100000 \" -to \" 20150105-110000 \"", new String[]
                        {
                            "-from", " 20150105-100000 ", "-to", " 20150105-110000 "
                        }, TEST_START, TEST_END, DURATION_NOT_SPECIFIED, false, false
                },
                {
                    "-from +1s -l \" 3600 \"", new String[]
                        {
                            "-from", "+1s", "-l", " 3600 "
                        }, PERIOD_1s, TO_NOT_SPECIFIED, PERIOD_1h, true, false
                },
                // zero
                {
                    "-from +0s", new String[]
                        {
                            "-from", "+0s"
                        }, 0, TO_NOT_SPECIFIED, DURATION_NOT_SPECIFIED, true, false
                },
                {
                    "-to +0s", new String[]
                        {
                            "-to", "+0s"
                        }, FROM_NOT_SPECIFIED, 0, DURATION_NOT_SPECIFIED, false, true
                },
                {
                    "-from +00:00:00", new String[]
                        {
                            "-from", "+00:00:00"
                        }, 0, TO_NOT_SPECIFIED, DURATION_NOT_SPECIFIED, true, false
                },
                {
                    "-to +00:00:00", new String[]
                        {
                            "-to", "+00:00:00"
                        }, FROM_NOT_SPECIFIED, 0, DURATION_NOT_SPECIFIED, false, true
                },
                // mixed
                {
                    "-from +1:00:00 -to +1h30m", new String[]
                        {
                            "-from", "+1:00:00", "-to", "+1h30m"
                        }, PERIOD_1h, PERIOD_1h30m, DURATION_NOT_SPECIFIED, true, true
                },
                // duration
                {
                    // from zero
                    "-from +0s -l 3600", new String[]
                        {
                            "-from", "+0s", "-l", "3600"
                        }, 0, TO_NOT_SPECIFIED, PERIOD_1h, true, false
                },
                {
                    // from first sec
                    "-from +1s -l 3600", new String[]
                        {
                            "-from", "+1s", "-l", "3600"
                        }, PERIOD_1s, TO_NOT_SPECIFIED, PERIOD_1h, true, false
                },
                {
                    // from absolute time
                    "-from 20150105-100000 -l 3600", new String[]
                        {
                            "-from", "20150105-100000", "-l", "3600"
                        }, TEST_START, TO_NOT_SPECIFIED, PERIOD_1h, false, false
                },
                {
                    // duration until end of period
                    "-to +3600s -l 1800", new String[]
                        {
                            "-to", "+3600s", "-l", "1800"
                        }, FROM_NOT_SPECIFIED, PERIOD_1h, PERIOD_30m, false, true
                },
                {
                    // duration before start
                    "-to +0s -l 3600", new String[]
                        {
                            "-to", "+0s", "-l", "3600"
                        }, FROM_NOT_SPECIFIED, 0, PERIOD_1h, false, true
                },
                {
                    // duration covers just half of period
                    "-to +1800s -l 3600", new String[]
                        {
                            "-to", "+1800s", "-l", "3600"
                        }, FROM_NOT_SPECIFIED, PERIOD_30m, PERIOD_1h, false, true
                },
                {
                    // duration zero
                    "-from +1s -l 0", new String[]
                        {
                            "-from", "+1s", "-l", "0"
                        }, PERIOD_1s, TO_NOT_SPECIFIED, 0, true, false
                }
            });
    }

    @Parameter(value = 0)
    public String description;

    @Parameter(value = 1)
    public String[] args;

    @Parameter(value = 2)
    public long expectedFrom;

    @Parameter(value = 3)
    public long expectedTo;

    @Parameter(value = 4)
    public long expectedDuration;

    @Parameter(value = 5)
    public boolean expectedIsFromRelative;

    @Parameter(value = 6)
    public boolean expectedIsToRelative;

    /**
     * Invokes {@link ReportGeneratorMain} with the given arguments.
     */
    @Test
    public void check() throws Exception
    {
        final ReportGeneratorMain rgm = ReportGeneratorMainFactory.create(args);

        Assert.assertEquals("FROM", expectedFrom, rgm.getFromTime());
        Assert.assertEquals("TO", expectedTo, rgm.getToTime());
        Assert.assertEquals("DURATION", expectedDuration, rgm.getDuration());

        Assert.assertEquals("FROM RELATIVE", expectedIsFromRelative, rgm.isFromTimeRel());
        Assert.assertEquals("TO RELATIVE", expectedIsToRelative, rgm.isToTimeRel());
    }
}
