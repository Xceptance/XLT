/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

    public BigDecimal arrivalRatePercentage = null;

    @XStreamConverter(ComplexLoadFunctionXStreamConverter.class)
    public int[][] complexLoadFunction;

    public int initialDelay;

    public int measurementPeriod;

    public int numberOfIterations;

    @XStreamConverter(LoadFunctionXStreamConverter.class)
    public int[][] numberOfUsers;

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
    }
}
