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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.xceptance.xlt.mastercontroller.TestCaseLoadProfileConfiguration;

@RunWith(Parameterized.class)
public class ReportGenerator_RampUpOffsetTest
{
    @Parameters
    public static Object[][] Data()
    {
        final TestCaseLoadProfileConfiguration iD_1h = makeProfile(3600, 0);
        final TestCaseLoadProfileConfiguration rU_15m = makeProfile(0, 900);
        final TestCaseLoadProfileConfiguration rU_20m = makeProfile(0, 1200);

        final TestCaseLoadProfileConfiguration iD_2h_rU_1h = makeProfile(7200, 3600);
        final TestCaseLoadProfileConfiguration iD_5m_rU_15m = makeProfile(300, 900);

        return new Object[][]
            {
                {
                    Arrays.asList(iD_1h, rU_15m, rU_20m), Long.valueOf(1200L)
                },
                {
                    Arrays.asList(rU_15m, rU_20m), Long.valueOf(1200L)
                },
                {
                    Arrays.asList(iD_1h, rU_15m), Long.valueOf(900L)
                },
                {
                    Arrays.asList(iD_1h), Long.valueOf(0)
                },
                {
                    Arrays.asList(iD_2h_rU_1h, iD_1h, rU_15m, rU_20m), Long.valueOf(10800L)
                },
                {
                    Arrays.asList(iD_2h_rU_1h, rU_15m, rU_20m), Long.valueOf(10800L)
                },
                {
                    Arrays.asList(iD_2h_rU_1h, rU_20m), Long.valueOf(10800L)
                },
                {
                    Arrays.asList(iD_2h_rU_1h), Long.valueOf(3600L)
                },
                {
                    Arrays.asList(iD_5m_rU_15m, rU_15m), Long.valueOf(1200L)
                },
                {
                    Arrays.asList(iD_5m_rU_15m, iD_2h_rU_1h), Long.valueOf(10500L)
                },
                {
                    Arrays.asList(iD_5m_rU_15m), Long.valueOf(900L)
                }
            };
    }

    private final List<TestCaseLoadProfileConfiguration> _profiles;

    private final long _rampUpOffset;

    public ReportGenerator_RampUpOffsetTest(final List<TestCaseLoadProfileConfiguration> profiles, final long rampUpOffset)
    {
        _profiles = profiles;
        _rampUpOffset = rampUpOffset;
    }

    @Test
    public void computeRampUpOffset() throws Exception
    {
        final long computedOffset = ReportGenerator.computeRampUpOffset(_profiles);

        Assert.assertEquals("Unexpected ramp-up offset for " + _profiles, _rampUpOffset, computedOffset);
    }

    private static TestCaseLoadProfileConfiguration makeProfile(final int initialDelay, final int rampUpPeriod)
    {
        final TestCaseLoadProfileConfiguration profile = new TestCaseLoadProfileConfiguration();
        profile.setInitialDelay(initialDelay);
        profile.setRampUpPeriod(rampUpPeriod);

        return profile;
    }
}
