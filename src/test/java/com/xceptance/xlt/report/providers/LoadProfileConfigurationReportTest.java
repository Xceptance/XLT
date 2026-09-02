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
package com.xceptance.xlt.report.providers;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.mastercontroller.TestCaseLoadProfileConfiguration;

/**
 * Unit tests for the plain numeric load function values of {@link LoadProfileConfigurationReport}. The load function
 * itself is rendered for human readers as "1...3,535", grouping separators and all, so consumers that need to compute
 * with the values read the min/max fields instead.
 */
public class LoadProfileConfigurationReportTest
{
    @Test
    public void testVaryingLoadFunctionYieldsMinAndMax()
    {
        final TestCaseLoadProfileConfiguration config = new TestCaseLoadProfileConfiguration();
        config.setArrivalRate(new int[][]
            {
                {
                    0, 1
                },
                {
                    600, 3535
                },
                {
                    1200, 2000
                }
            });

        final LoadProfileConfigurationReport report = new LoadProfileConfigurationReport(config);

        Assert.assertEquals(Integer.valueOf(1), report.arrivalRateMin);
        Assert.assertEquals(Integer.valueOf(3535), report.arrivalRateMax);
    }

    @Test
    public void testFlatLoadFunctionYieldsEqualMinAndMax()
    {
        final TestCaseLoadProfileConfiguration config = new TestCaseLoadProfileConfiguration();
        config.setNumberOfUsers(new int[][]
            {
                {
                    0, 1021
                }
            });

        final LoadProfileConfigurationReport report = new LoadProfileConfigurationReport(config);

        Assert.assertEquals(Integer.valueOf(1021), report.numberOfUsersMin);
        Assert.assertEquals(Integer.valueOf(1021), report.numberOfUsersMax);
    }

    @Test
    public void testMissingLoadFunctionYieldsNull()
    {
        final LoadProfileConfigurationReport report = new LoadProfileConfigurationReport(new TestCaseLoadProfileConfiguration());

        Assert.assertNull(report.arrivalRateMin);
        Assert.assertNull(report.arrivalRateMax);
    }

    @Test
    public void testRenderedLoadFunctionIsUntouched()
    {
        final int[][] loadFunction = new int[][]
            {
                {
                    0, 1
                },
                {
                    600, 3535
                }
            };

        final TestCaseLoadProfileConfiguration config = new TestCaseLoadProfileConfiguration();
        config.setArrivalRate(loadFunction);

        final LoadProfileConfigurationReport report = new LoadProfileConfigurationReport(config);

        // the array the HTML report renders from must still be the one that was configured
        Assert.assertArrayEquals(loadFunction, report.arrivalRate);
    }
}
