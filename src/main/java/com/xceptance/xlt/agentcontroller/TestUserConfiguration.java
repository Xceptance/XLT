/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.agentcontroller;

import java.io.Serializable;

import org.apache.commons.lang3.ArrayUtils;

/**
 * 
 */
public class TestUserConfiguration implements Serializable
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    private int absoluteUserNumber;

    /**
     * The index of the agent that execute this test user (values: [0, <var>numberOfAgents</var>-1]).
     */
    private int agentIndex;

    private int[][] arrivalRates;

    private int initialDelay;

    private int instance;

    private int measurementPeriod;

    private int numberOfIterations;

    private int numberOfUsers;

    private int shutdownPeriod;

    private String testCaseClassName;

    private int totalUserCount;

    private String userName;

    private int[][] users;

    private int warmUpPeriod;

    private double[] weightFunction;

    public int getAbsoluteUserNumber()
    {
        return absoluteUserNumber;
    }

    public int getAgentIndex()
    {
        return agentIndex;
    }

    public int[][] getArrivalRates()
    {
        return arrivalRates;
    }

    public int getInitialDelay()
    {
        return initialDelay;
    }

    public int getInstance()
    {
        return instance;
    }

    public int getMeasurementPeriod()
    {
        return measurementPeriod;
    }

    public int getNumberOfIterations()
    {
        return numberOfIterations;
    }

    public int getNumberOfUsers()
    {
        return numberOfUsers;
    }

    public int getShutdownPeriod()
    {
        return shutdownPeriod;
    }

    public String getTestCaseClassName()
    {
        return testCaseClassName;
    }

    public int getTotalUserCount()
    {
        return totalUserCount;
    }

    public String getUserId()
    {
        return userName + "-" + instance;
    }

    public String getUserName()
    {
        return userName;
    }

    public int[][] getUsers()
    {
        return users;
    }

    public int getWarmUpPeriod()
    {
        return warmUpPeriod;
    }

    public void setAbsoluteUserNumber(final int absoluteUserNumber)
    {
        this.absoluteUserNumber = absoluteUserNumber;
    }

    public void setAgentIndex(final int agentIndex)
    {
        this.agentIndex = agentIndex;
    }

    public void setArrivalRates(final int[][] arrivalRates)
    {
        this.arrivalRates = arrivalRates;
    }

    public void setInitialDelay(final int initialDelay)
    {
        this.initialDelay = initialDelay;
    }

    public void setInstance(final int instance)
    {
        this.instance = instance;
    }

    public void setMeasurementPeriod(final int measurementPeriod)
    {
        this.measurementPeriod = measurementPeriod;
    }

    public void setNumberOfIterations(final int warmupTime)
    {
        numberOfIterations = warmupTime;
    }

    public void setNumberOfUsers(final int numberOfUsers)
    {
        this.numberOfUsers = numberOfUsers;
    }

    public void setShutdownPeriod(final int shutdownPeriod)
    {
        this.shutdownPeriod = shutdownPeriod;
    }

    public void setTestCaseClassName(final String testCaseClass)
    {
        testCaseClassName = testCaseClass;
    }

    public void setTotalUserCount(final int totalUserCount)
    {
        this.totalUserCount = totalUserCount;
    }

    public void setUserName(final String userName)
    {
        this.userName = userName;
    }

    public void setUsers(final int[][] users)
    {
        this.users = users;
    }

    public void setWarmUpPeriod(final int warmUpPeriod)
    {
        this.warmUpPeriod = warmUpPeriod;
    }

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();

        buf.append(getUserId()).append(": ");

        buf.append(" users: ").append(ArrayUtils.toString(users));

        if (arrivalRates != null)
        {
            buf.append(" arrivalRates: ").append(ArrayUtils.toString(arrivalRates));
        }

        return buf.toString();
    }

    /**
     * @param weightFunction
     */
    public void setWeightFunction(final double[] weightFunction)
    {
        this.weightFunction = weightFunction;
    }

    /**
     * @return the weightFunction
     */
    public double[] getWeightFunction()
    {
        return weightFunction;
    }

}
