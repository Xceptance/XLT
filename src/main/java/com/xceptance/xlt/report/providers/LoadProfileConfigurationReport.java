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

import java.math.BigDecimal;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.xceptance.xlt.mastercontroller.ComplexLoadFunctionXStreamConverter;
import com.xceptance.xlt.mastercontroller.LoadFunctionXStreamConverter;
import com.xceptance.xlt.mastercontroller.TestCaseLoadProfileConfiguration;

/**
 * The {@link LoadProfileConfigurationReport} represents the load profile as configured for a certain test case. (see
 * TestCaseLoadProfileConfiguration)
 */
@XStreamAlias("testCase")
public class LoadProfileConfigurationReport
{
    @XStreamConverter(LoadFunctionXStreamConverter.class)
    public int[][] arrivalRate;

    /**
     * The lowest arrival rate of the load function, or <code>null</code> if no arrival rate is configured. The
     * {@link #arrivalRate} field is rendered for human readers as "1...3,535", complete with grouping separators;
     * this field and {@link #arrivalRateMax} carry the same information as plain numbers.
     */
    public Integer arrivalRateMin = null;

    /**
     * The highest arrival rate of the load function, or <code>null</code> if no arrival rate is configured.
     *
     * @see #arrivalRateMin
     */
    public Integer arrivalRateMax = null;

    /**
     * The arrival rate load function as <code>second:value</code> pairs, or <code>null</code> when the rate does not
     * change over the run. Min and max only say where the function starts and ends, which loses the shape of anything
     * more interesting than a single ramp - a stepped or spiky profile looks the same as a smooth climb.
     */
    public String arrivalRateProfile = null;

    public BigDecimal arrivalRatePercentage = null;

    @XStreamConverter(ComplexLoadFunctionXStreamConverter.class)
    public int[][] complexLoadFunction;

    public int initialDelay;

    public int measurementPeriod;

    public int numberOfIterations;

    @XStreamConverter(LoadFunctionXStreamConverter.class)
    public int[][] numberOfUsers;

    /**
     * The lowest user count of the load function, or <code>null</code> if no user count is configured.
     *
     * @see #arrivalRateMin
     */
    public Integer numberOfUsersMin = null;

    /**
     * The highest user count of the load function, or <code>null</code> if no user count is configured.
     *
     * @see #arrivalRateMin
     */
    public Integer numberOfUsersMax = null;

    /**
     * The user count load function as <code>second:value</code> pairs, or <code>null</code> when the count does not
     * change over the run.
     *
     * @see #arrivalRateProfile
     */
    public String numberOfUsersProfile = null;

    public BigDecimal numberOfUsersPercentage = null;

    public int rampUpPeriod;

    public int shutdownPeriod;

    public String testCaseClassName;

    public String userName;

    public int warmUpPeriod;

    public int actionThinkTime;

    public int actionThinkTimeDeviation;

    public LoadProfileConfigurationReport(TestCaseLoadProfileConfiguration tcConfig)
    {
        this.arrivalRate = tcConfig.getArrivalRate();
        this.complexLoadFunction = tcConfig.getComplexLoadFunction();
        this.initialDelay = tcConfig.getInitialDelay();
        this.measurementPeriod = tcConfig.getMeasurementPeriod();
        this.numberOfIterations = tcConfig.getNumberOfIterations();
        this.numberOfUsers = tcConfig.getNumberOfUsers();
        this.rampUpPeriod = tcConfig.getRampUpPeriod();
        this.shutdownPeriod = tcConfig.getShutdownPeriod();
        this.testCaseClassName = tcConfig.getTestCaseClassName();
        this.userName = tcConfig.getUserName();
        this.warmUpPeriod = tcConfig.getWarmUpPeriod();
        this.actionThinkTime = tcConfig.getActionThinkTime();
        this.actionThinkTimeDeviation = tcConfig.getActionThinkTimeDeviation();

        this.arrivalRateMin = minOfLoadFunction(this.arrivalRate);
        this.arrivalRateMax = maxOfLoadFunction(this.arrivalRate);
        this.arrivalRateProfile = profileOfLoadFunction(this.arrivalRate);
        this.numberOfUsersMin = minOfLoadFunction(this.numberOfUsers);
        this.numberOfUsersMax = maxOfLoadFunction(this.numberOfUsers);
        this.numberOfUsersProfile = profileOfLoadFunction(this.numberOfUsers);
    }

    /**
     * Renders a load function as <code>second:value</code> pairs, so that a profile which changes over the run keeps
     * its shape instead of collapsing to a min and a max.
     *
     * @param loadFunction
     *            the load function, may be <code>null</code>
     * @return the pairs, or <code>null</code> when the function holds fewer than two points and therefore does not
     *         change over time
     */
    private static String profileOfLoadFunction(final int[][] loadFunction)
    {
        if (loadFunction == null || loadFunction.length < 2)
        {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        for (final int[] point : loadFunction)
        {
            if (sb.length() > 0)
            {
                sb.append(' ');
            }
            sb.append(point[0]).append(':').append(point[1]);
        }

        return sb.toString();
    }

    /**
     * Returns the lowest value of the given load function.
     *
     * @param loadFunction
     *            the load function, may be <code>null</code>
     * @return the minimum, or <code>null</code> if there is no load function
     */
    private static Integer minOfLoadFunction(final int[][] loadFunction)
    {
        if (loadFunction == null || loadFunction.length == 0)
        {
            return null;
        }

        // see LoadFunctionXStreamConverter, which renders the same values for human readers
        int minimum = Integer.MAX_VALUE;
        for (final int[] array : loadFunction)
        {
            minimum = Math.min(minimum, array[1]);
        }

        return minimum;
    }

    /**
     * Returns the highest value of the given load function.
     *
     * @param loadFunction
     *            the load function, may be <code>null</code>
     * @return the maximum, or <code>null</code> if there is no load function
     */
    private static Integer maxOfLoadFunction(final int[][] loadFunction)
    {
        if (loadFunction == null || loadFunction.length == 0)
        {
            return null;
        }

        int maximum = 0;
        for (final int[] array : loadFunction)
        {
            maximum = Math.max(maximum, array[1]);
        }

        return maximum;
    }
}
