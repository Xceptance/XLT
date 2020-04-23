/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.mastercontroller;

import org.apache.commons.lang3.ArrayUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * The {@link TestCaseLoadProfileConfiguration} represents the load profile as configured for a certain test case.
 */
@XStreamAlias("testCase")
public class TestCaseLoadProfileConfiguration
{
    @XStreamConverter(LoadFunctionXStreamConverter.class)
    private int[][] arrivalRate;

    @XStreamConverter(ComplexLoadFunctionXStreamConverter.class)
    private int[][] complexLoadFunction;

    private int initialDelay;

    @XStreamOmitField
    private int[][] loadFactor;

    private int measurementPeriod;

    private int numberOfIterations;

    @XStreamConverter(LoadFunctionXStreamConverter.class)
    private int[][] numberOfUsers;

    private int rampUpPeriod;

    private int shutdownPeriod;

    private String testCaseClassName;

    private String userName;

    private int warmUpPeriod;

    public int[][] getArrivalRate()
    {
        return arrivalRate;
    }

    public int[][] getComplexLoadFunction()
    {
        return complexLoadFunction;
    }

    public int getInitialDelay()
    {
        return initialDelay;
    }

    /**
     * Returns the load factor as two-dimensional array of non-negative integral values whereas the 1st dimension
     * specifies the time offset in seconds and the 2nd dimension specifies the load factor in per mil at the given time
     * offset.
     * 
     * @return the load factor
     */
    public int[][] getLoadFactor()
    {
        return loadFactor;
    }

    public int getMeasurementPeriod()
    {
        return measurementPeriod;
    }

    public int getNumberOfIterations()
    {
        return numberOfIterations;
    }

    public int[][] getNumberOfUsers()
    {
        return numberOfUsers;
    }

    public int getRampUpPeriod()
    {
        return rampUpPeriod;
    }

    public final int getShutdownPeriod()
    {
        return shutdownPeriod;
    }

    public String getTestCaseClassName()
    {
        return testCaseClassName;
    }

    public String getUserName()
    {
        return userName;
    }

    public int getWarmUpPeriod()
    {
        return warmUpPeriod;
    }

    public void setArrivalRate(final int[][] arrivalRate)
    {
        this.arrivalRate = arrivalRate;
    }

    public void setComplexLoadFunction(final int[][] complexLoadFunction)
    {
        this.complexLoadFunction = complexLoadFunction;
    }

    public void setInitialDelay(final int initialDelay)
    {
        this.initialDelay = Math.max(initialDelay, 0);
    }

    public void setLoadFactor(final int[][] loadFactor)
    {
        this.loadFactor = loadFactor;
    }

    public void setMeasurementPeriod(final int measurementPeriod)
    {
        this.measurementPeriod = Math.max(measurementPeriod, 0);
    }

    public void setNumberOfIterations(final int numberOfIterations)
    {
        this.numberOfIterations = Math.max(numberOfIterations, 0);
    }

    public void setNumberOfUsers(final int[][] numberOfUsers)
    {
        this.numberOfUsers = numberOfUsers;
    }

    public void setRampUpPeriod(final int rampUpPeriod)
    {
        this.rampUpPeriod = rampUpPeriod;
    }

    public final void setShutdownPeriod(final int shutdownPeriod)
    {
        this.shutdownPeriod = Math.max(shutdownPeriod, 0);
    }

    public void setTestCaseClassName(final String testCaseClass)
    {
        testCaseClassName = testCaseClass;
    }

    public void setUserName(final String userName)
    {
        this.userName = userName;
    }

    public void setWarmUpPeriod(final int warmUpPeriod)
    {
        this.warmUpPeriod = Math.max(warmUpPeriod, 0);
    }

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();

        buf.append(getUserName()).append(": ");

        buf.append(" users: ").append(ArrayUtils.toString(numberOfUsers));

        if (arrivalRate != null)
        {
            buf.append(" arrivalRates: ").append(ArrayUtils.toString(arrivalRate));
        }

        return buf.toString();
    }

    @XStreamOmitField
    private boolean isCPTest;

    public boolean isCPTest()
    {
        return isCPTest;
    }

    public void setCPTest(final boolean isCPTest)
    {
        this.isCPTest = isCPTest;
    }

    private int actionThinkTime;

    private int actionThinkTimeDeviation;

    public int getActionThinkTime()
    {
        return actionThinkTime;
    }

    public void setActionThinkTime(final int actionThinkTime)
    {
        this.actionThinkTime = actionThinkTime;
    }

    public int getActionThinkTimeDeviation()
    {
        return actionThinkTimeDeviation;
    }

    public void setActionThinkTimeDeviation(final int actionThinkTimeDeviation)
    {
        this.actionThinkTimeDeviation = actionThinkTimeDeviation;
    }
}
