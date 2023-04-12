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
package com.xceptance.xlt.mastercontroller;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import com.google.common.collect.ImmutableMap;
import com.xceptance.xlt.agent.RandomExecutionTimer;
import com.xceptance.xlt.agent.unipro.CompositeFunction;
import com.xceptance.xlt.agentcontroller.AgentController;
import com.xceptance.xlt.agentcontroller.TestUserConfiguration;

/**
 * Tests the implementation of {@link TestDeployer}.
 * 
 * @author Sebastian Loob (Xceptance Software Technologies GmbH)
 */
public class TestDeployerTest
{
    // handles the actual start times of all user types 
    private final HashMap<String, LinkedList<Long>> actualUserTypeStartTime = new HashMap<String, LinkedList<Long>>();

    // handles the actual stop times of all user types
    private final HashMap<String, LinkedList<Long>> actualUserTypeStopTime = new HashMap<String, LinkedList<Long>>();

    // handles the expected start times of all user types
    private final HashMap<String, LinkedList<Long>> expectedUserTypeStartTime = new HashMap<String, LinkedList<Long>>();

    // handles the expected stop times of all user types
    private final HashMap<String, LinkedList<Long>> expectedUserTypeStopTime = new HashMap<String, LinkedList<Long>>();

    @Before
    public void setUp()
    {
        actualUserTypeStartTime.clear();
        actualUserTypeStopTime.clear();
        expectedUserTypeStartTime.clear();
        expectedUserTypeStopTime.clear();
    }

    /**
     * at least one agent controller has to be specified
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAgentControllerMappingIsNull() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithOneTestCase();

        final Map<String, AgentController> agentControllers = null;

        validate(agentControllers, testLoadProfile);
    }

    /**
     * at least one agent controller has to be specified
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNoAgentControllerIsSpecified() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithOneTestCase();

        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();

        validate(agentControllers, testLoadProfile);
    }

    /**
     * total weight of agent controllers can not be zero
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTotalWeightIsZero() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithOneTestCase();

        final int count1 = 3;
        final int weight1 = 0;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 1;
        final int weight2 = 0;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController2.getName(), agentController2);
        agentControllers.put(agentController1.getName(), agentController1);

        validate(agentControllers, testLoadProfile);
    }

    /**
     * total count of agents can not be zero
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTotalCountIsZero() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithOneTestCase();

        final int count1 = 0;
        final int weight1 = 2;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 0;
        final int weight2 = 3;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController2.getName(), agentController2);
        agentControllers.put(agentController1.getName(), agentController1);

        validate(agentControllers, testLoadProfile);
    }

    /**
     * check what happens, if there is an agent controller twice
     * 
     * @throws IOException
     */
    @Test
    public void testAgentControllerIsTwice() throws IOException
    {
        // create TestLoadProfile
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithOneTestCase();

        // create AgentControllers
        final int count1 = 4;
        final int weight1 = 8;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 2;
        final int weight2 = 4;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);

        // test the Deployment
        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testOneTestCaseOneACOneAgent() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithOneTestCase();

        final int count1 = 1;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testOneTestCaseOneACMoreAgents() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithOneTestCase();

        final int count1 = 18;
        final int weight1 = 3;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testOneTestCaseTwoACOneAgentSameWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithOneTestCase();

        final int count1 = 1;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 1;
        final int weight2 = 1;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testOneTestCaseTwoACMoreAgentSameWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithOneTestCase();

        final int count1 = 3;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 2;
        final int weight2 = 1;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testOneTestCaseTwoACSameAgentsDifferentWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithOneTestCase();

        final int count1 = 3;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 3;
        final int weight2 = 3;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testOneTestCaseTwoACDifferentAgentsDifferentWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithOneTestCase();

        final int count1 = 2;
        final int weight1 = 4;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 7;
        final int weight2 = 1;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testOneTestCaseMoreACSameAgentsSameWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithOneTestCase();

        final int count1 = 2;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 2;
        final int weight2 = 1;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final int count3 = 2;
        final int weight3 = 1;
        final AgentController agentController3 = createAgentController("AC3", weight3, count3);
        final int count4 = 2;
        final int weight4 = 1;
        final AgentController agentController4 = createAgentController("AC4", weight4, count4);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);
        agentControllers.put(agentController3.getName(), agentController3);
        agentControllers.put(agentController4.getName(), agentController4);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testOneTestCaseMoreACDifferentAgentsSameWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithOneTestCase();

        final int count1 = 1;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 2;
        final int weight2 = 1;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final int count3 = 3;
        final int weight3 = 1;
        final AgentController agentController3 = createAgentController("AC3", weight3, count3);
        final int count4 = 4;
        final int weight4 = 1;
        final AgentController agentController4 = createAgentController("AC4", weight4, count4);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);
        agentControllers.put(agentController3.getName(), agentController3);
        agentControllers.put(agentController4.getName(), agentController4);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testOneTestCaseMoreACDifferentAgentsDifferentWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithOneTestCase();

        final int count1 = 1;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 2;
        final int weight2 = 2;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final int count3 = 3;
        final int weight3 = 4;
        final AgentController agentController3 = createAgentController("AC3", weight3, count3);
        final int count4 = 4;
        final int weight4 = 8;
        final AgentController agentController4 = createAgentController("AC4", weight4, count4);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);
        agentControllers.put(agentController3.getName(), agentController3);
        agentControllers.put(agentController4.getName(), agentController4);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testTwoTestCasesOneACOneAgent() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithTwoEqualTestCases();

        final int count1 = 1;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testTwoTestCasesOneACMoreAgents() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithTwoEqualTestCases();

        final int count1 = 18;
        final int weight1 = 3;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testTwoTestCasesTwoACOneAgentSameWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithTwoEqualTestCases();

        final int count1 = 1;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 1;
        final int weight2 = 1;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testTwoTestCasesTwoACDifferentAgentsSameWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithTwoEqualTestCases();

        final int count1 = 3;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 2;
        final int weight2 = 1;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testTwoTestCasesTwoACSameAgentDifferentWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithTwoEqualTestCases();

        final int count1 = 5;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 5;
        final int weight2 = 3;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testTwoTestCasesTwoACDifferentAgentsDifferentWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithTwoEqualTestCases();

        final int count1 = 2;
        final int weight1 = 4;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 7;
        final int weight2 = 1;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testTwoTestCasesMoreACOneAgentSameWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithTwoEqualTestCases();

        final int count1 = 1;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 1;
        final int weight2 = 1;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final int count3 = 1;
        final int weight3 = 1;
        final AgentController agentController3 = createAgentController("AC3", weight3, count3);
        final int count4 = 1;
        final int weight4 = 1;
        final AgentController agentController4 = createAgentController("AC4", weight4, count4);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);
        agentControllers.put(agentController3.getName(), agentController3);
        agentControllers.put(agentController4.getName(), agentController4);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testTwoTestCasesMoreACSameAgentsSameWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithTwoEqualTestCases();

        final int count1 = 3;
        final int weight1 = 5;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 3;
        final int weight2 = 5;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final int count3 = 3;
        final int weight3 = 5;
        final AgentController agentController3 = createAgentController("AC3", weight3, count3);
        final int count4 = 3;
        final int weight4 = 5;
        final AgentController agentController4 = createAgentController("AC4", weight4, count4);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);
        agentControllers.put(agentController3.getName(), agentController3);
        agentControllers.put(agentController4.getName(), agentController4);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testTwoTestCasesMoreACDifferentAgentsSameWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithTwoEqualTestCases();

        final int count1 = 1;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 2;
        final int weight2 = 1;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final int count3 = 3;
        final int weight3 = 1;
        final AgentController agentController3 = createAgentController("AC3", weight3, count3);
        final int count4 = 4;
        final int weight4 = 1;
        final AgentController agentController4 = createAgentController("AC4", weight4, count4);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);
        agentControllers.put(agentController3.getName(), agentController3);
        agentControllers.put(agentController4.getName(), agentController4);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testTwoTestCasesMoreACDifferentAgentsDifferentWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithTwoEqualTestCases();

        final int count1 = 1;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 2;
        final int weight2 = 2;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final int count3 = 3;
        final int weight3 = 4;
        final AgentController agentController3 = createAgentController("AC3", weight3, count3);
        final int count4 = 4;
        final int weight4 = 8;
        final AgentController agentController4 = createAgentController("AC4", weight4, count4);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);
        agentControllers.put(agentController3.getName(), agentController3);
        agentControllers.put(agentController4.getName(), agentController4);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testTwoDifferentTestCasesOneACOneAgent() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithTwoDifferentTestCases();

        final int count1 = 1;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testTwoDifferentTestCasesOneACMoreAgents() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithTwoDifferentTestCases();

        final int count1 = 37;
        final int weight1 = 3;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testTwoDifferentTestCasesTwoACOneAgentSameWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithTwoDifferentTestCases();

        final int count1 = 1;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 1;
        final int weight2 = 1;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testTwoDifferentTestCasesTwoACDifferentAgentsSameWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithTwoDifferentTestCases();

        final int count1 = 3;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 2;
        final int weight2 = 1;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testTwoDifferentTestCasesTwoACSameAgentsDifferentWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithTwoDifferentTestCases();

        final int count1 = 3;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 3;
        final int weight2 = 3;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testTwoDifferentTestCasesMoreACOneAgentSameWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithTwoDifferentTestCases();

        final int count1 = 1;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 1;
        final int weight2 = 1;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final int count3 = 1;
        final int weight3 = 1;
        final AgentController agentController3 = createAgentController("AC3", weight3, count3);
        final int count4 = 1;
        final int weight4 = 1;
        final AgentController agentController4 = createAgentController("AC4", weight4, count4);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);
        agentControllers.put(agentController3.getName(), agentController3);
        agentControllers.put(agentController4.getName(), agentController4);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testALotOfDifferentTestCasesOneACOneAgent() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithMoreDifferentTestCases();

        final int count1 = 1;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testALotOfDifferentTestCasesOneACMoreAgents() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithMoreDifferentTestCases();

        final int count1 = 13;
        final int weight1 = 3;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testALotOfDifferentTestCasesTwoACOneAgentSameWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithMoreDifferentTestCases();

        final int count1 = 1;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 1;
        final int weight2 = 1;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testALotOfDifferentTestCasesMoreACOneAgentSameWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithMoreDifferentTestCases();

        final int count1 = 1;
        final int weight1 = 1;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 1;
        final int weight2 = 1;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final int count3 = 1;
        final int weight3 = 1;
        final AgentController agentController3 = createAgentController("AC3", weight3, count3);
        final int count4 = 1;
        final int weight4 = 1;
        final AgentController agentController4 = createAgentController("AC4", weight4, count4);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);
        agentControllers.put(agentController3.getName(), agentController3);
        agentControllers.put(agentController4.getName(), agentController4);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testALotOfDifferentTestCasesMoreACDifferentAgentsDifferentWeight() throws IOException
    {
        final TestLoadProfileConfiguration testLoadProfile = createTestLoadProfileWithMoreDifferentTestCases();

        final int count1 = 5;
        final int weight1 = 7;
        final AgentController agentController1 = createAgentController("AC1", weight1, count1);
        final int count2 = 7;
        final int weight2 = 10;
        final AgentController agentController2 = createAgentController("AC2", weight2, count2);
        final int count3 = 11;
        final int weight3 = 15;
        final AgentController agentController3 = createAgentController("AC3", weight3, count3);
        final int count4 = 3;
        final int weight4 = 7;
        final AgentController agentController4 = createAgentController("AC4", weight4, count4);
        final int count5 = 13;
        final int weight5 = 17;
        final AgentController agentController5 = createAgentController("AC5", weight5, count5);
        final int count6 = 4;
        final int weight6 = 8;
        final AgentController agentController6 = createAgentController("AC6", weight6, count6);
        final int count7 = 7;
        final int weight7 = 10;
        final AgentController agentController7 = createAgentController("AC7", weight7, count7);
        final int count8 = 11;
        final int weight8 = 19;
        final AgentController agentController8 = createAgentController("AC8", weight8, count8);
        final int count9 = 5;
        final int weight9 = 7;
        final AgentController agentController9 = createAgentController("AC9", weight9, count9);
        final int count10 = 13;
        final int weight10 = 17;
        final AgentController agentController10 = createAgentController("AC10", weight10, count10);
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        agentControllers.put(agentController1.getName(), agentController1);
        agentControllers.put(agentController2.getName(), agentController2);
        agentControllers.put(agentController3.getName(), agentController3);
        agentControllers.put(agentController4.getName(), agentController4);
        agentControllers.put(agentController5.getName(), agentController5);
        agentControllers.put(agentController6.getName(), agentController6);
        agentControllers.put(agentController7.getName(), agentController7);
        agentControllers.put(agentController8.getName(), agentController8);
        agentControllers.put(agentController9.getName(), agentController9);
        agentControllers.put(agentController10.getName(), agentController10);

        validate(agentControllers, testLoadProfile);
    }

    @Test
    public void testLTAndCPTwoAC() throws Exception
    {
        final TestLoadProfileConfiguration loadProfile = new TestLoadProfileConfiguration();
        final int[][] numberOfUsers = new int[][]
            {
                {
                    0, 10
                }
            };
        final TestCaseLoadProfileConfiguration tc1Config = createTestCaseLoadProfile("CPTest", numberOfUsers);
        tc1Config.setCPTest(true);
        loadProfile.addTestCaseLoadProfileConfiguration(tc1Config);
        loadProfile.addTestCaseLoadProfileConfiguration(createTestCaseLoadProfile("LoadTestCase", numberOfUsers));

        final AgentController agentController1 = createAgentController("AC1", 1, 3, true);
        final AgentController agentController2 = createAgentController("AC2", 3, 3);
        final Map<String, AgentController> agentControllers = ImmutableMap.of("AC1", agentController1, "AC2", agentController2);

        final TestDeployment deployment = createTestDeployment(agentControllers, loadProfile);

        {
            final Map<String, List<TestUserConfiguration>> acUserList = deployment.getAgentsUserList(agentController1);
            final Set<String> agentIDs = agentController1.getAgentIDs();

            Assert.assertEquals(3, agentIDs.size());

            int nbUsers = 0;
            for (final String agentID : agentIDs)
            {
                Assert.assertTrue(acUserList.containsKey(agentID));
                final List<TestUserConfiguration> list = acUserList.get(agentID);
                for (final TestUserConfiguration tuConfig : list)
                {
                    Assert.assertEquals("CPTest", tuConfig.getUserName());
                }
                nbUsers += list.size();
            }

            Assert.assertEquals(10, nbUsers);
        }

        {
            final Map<String, List<TestUserConfiguration>> acUserList = deployment.getAgentsUserList(agentController2);
            final Set<String> agentIDs = agentController2.getAgentIDs();

            Assert.assertEquals(3, agentIDs.size());

            int nbUsers = 0;
            for (final String agentID : agentIDs)
            {
                Assert.assertTrue(acUserList.containsKey(agentID));
                final List<TestUserConfiguration> list = acUserList.get(agentID);
                for (final TestUserConfiguration tuConfig : list)
                {
                    Assert.assertEquals("LoadTestCase", tuConfig.getUserName());
                }
                nbUsers += list.size();
            }

            Assert.assertEquals(10, nbUsers);
        }

    }

    /**
     * returns user count per agent
     * 
     * @param testDeployment
     * @return
     */
    private Map<String, Integer> userPerAgent(final TestDeployment testDeployment)
    {
        final Map<String, Integer> testsPerAgent = new HashMap<String, Integer>();
        for (final String agentID : testDeployment.getAgentIDs())
        {
            final int userCount = testDeployment.getUserList(agentID).size();
            testsPerAgent.put(agentID, userCount);
        }
        return testsPerAgent;
    }

    /**
     * returns user count per agent controller
     * 
     * @param testDeployment
     * @return
     */
    private Map<String, Integer> userPerAgentController(final TestDeployment testDeployment)
    {
        final Map<String, Integer> countAgentController = new HashMap<String, Integer>();
        for (final String agentID : testDeployment.getAgentIDs())
        {
            // user count of current agent
            final int userCount = testDeployment.getUserList(agentID).size();
            // get the name of the AgentController
            final String agentController = agentID.substring(0, agentID.lastIndexOf("-"));
            // add the user count to the total user count of the current agent controller
            if (countAgentController.containsKey(agentController))
            {
                countAgentController.put(agentController, (countAgentController.remove(agentController) + userCount));
            }
            else
            {
                countAgentController.put(agentController, userCount);
            }
        }
        return countAgentController;
    }

    /**
     * returns user count of different test cases per agent controller
     * 
     * @param testDeployment
     * @return
     */
    private Map<String, Integer> differentTestsPerAgentController(final TestDeployment testDeployment)
    {
        final Map<String, Integer> countTestCasesPerAgentController = new HashMap<String, Integer>();
        for (final String agentID : testDeployment.getAgentIDs())
        {
            final String agentController = agentID.substring(0, agentID.lastIndexOf("-"));
            for (final TestUserConfiguration userList : testDeployment.getUserList(agentID))
            {
                // key is the connection of agent controller and its test case
                final String key = agentController + "-->" + userList.getUserName();
                if (countTestCasesPerAgentController.containsKey(key))
                {
                    countTestCasesPerAgentController.put(key, countTestCasesPerAgentController.remove(key) + 1);
                }
                else
                {
                    countTestCasesPerAgentController.put(key, 1);
                }
            }
        }
        // if a connection of agent controller and test case is unused, create it with count = zero
        final Queue<String> agentControllerTestCaseName = new LinkedList<String>();
        final Queue<String> agentControllers = new LinkedList<String>();
        final Queue<String> testCaseName = new LinkedList<String>();
        // get all agent controller
        for (final String agentID : testDeployment.getAgentIDs())
        {
            if (!agentControllers.contains(agentID.substring(0, agentID.lastIndexOf("-"))))
            {
                agentControllers.add(agentID.substring(0, agentID.lastIndexOf("-")));
            }
        }
        // get all test cases
        for (final List<TestUserConfiguration> testUserConfiguration : testDeployment.getAllUserLists())
        {
            if (!testUserConfiguration.isEmpty())
            {
                final String dummy = testUserConfiguration.get(0).getUserName();
                if (!testCaseName.contains(dummy))
                {
                    testCaseName.add(dummy);
                }
            }
        }
        // create all possible connections
        for (final String agentController : agentControllers)
        {
            for (final String testCase : testCaseName)
            {
                agentControllerTestCaseName.add(agentController + "-->" + testCase);
            }
        }
        // check, if a connection is unused
        while (!agentControllerTestCaseName.isEmpty())
        {
            final String dummy = agentControllerTestCaseName.poll();
            if (!countTestCasesPerAgentController.containsKey(dummy))
            {
                countTestCasesPerAgentController.put(dummy, 0);
            }
        }
        return countTestCasesPerAgentController;
    }

    /**
     * returns user count of different test cases per agent
     * 
     * @param testDeployment
     * @return
     */
    private Map<String, Integer> differentTestsPerAgent(final TestDeployment testDeployment)
    {
        final Map<String, Integer> countTestCasesPerAgent = new HashMap<String, Integer>();
        for (final String agentID : testDeployment.getAgentIDs())
        {
            for (final TestUserConfiguration userList : testDeployment.getUserList(agentID))
            {
                // key is the connection of agent controller and its test case
                final String key = agentID + "-->" + userList.getUserName();
                if (countTestCasesPerAgent.containsKey(key))
                {
                    countTestCasesPerAgent.put(key, countTestCasesPerAgent.remove(key) + 1);
                }
                else
                {
                    countTestCasesPerAgent.put(key, 1);
                }
            }
        }
        // if a connection of agent controller and test case is unused, create it with count = zero
        final Queue<String> agentTestCaseName = new LinkedList<String>();
        final Queue<String> agents = new LinkedList<String>();
        final Queue<String> testCaseName = new LinkedList<String>();
        // get all agents
        for (final String agentID : testDeployment.getAgentIDs())
        {
            agents.add(agentID);
        }
        // get all test cases
        for (final List<TestUserConfiguration> testUserConfiguration : testDeployment.getAllUserLists())
        {
            if (!testUserConfiguration.isEmpty())
            {
                final String dummy = testUserConfiguration.get(0).getUserName();
                if (!testCaseName.contains(dummy))
                {
                    testCaseName.add(dummy);
                }
            }
        }
        for (final String agent : agents)
        {
            for (final String testCase : testCaseName)
            {
                agentTestCaseName.add(agent + "-->" + testCase);
            }
        }
        // check, if a connection is unused
        while (!agentTestCaseName.isEmpty())
        {
            final String dummy = agentTestCaseName.poll();
            if (!countTestCasesPerAgent.containsKey(dummy))
            {
                countTestCasesPerAgent.put(dummy, 0);
            }
        }
        return countTestCasesPerAgent;
    }

    /**
     * checks, if the user count per agent is equal to the expected value
     * 
     * @param userPerAgent
     * @param agentControllers
     */
    private void checkCountUserPerAgent(final Map<String, Integer> userPerAgent, final Map<String, AgentController> agentControllers)
    {
        int totalUserCount = 0;
        final int totalWeight = computeTotalWeight(agentControllers);
        // compute the total count of users
        for (final String agent : userPerAgent.keySet())
        {
            totalUserCount += userPerAgent.get(agent);
        }
        // check the user count at each agent
        for (final String agent : userPerAgent.keySet())
        {
            final String agentController = agent.substring(0, agent.lastIndexOf("-"));
            double expected = totalUserCount * ((double) agentControllers.get(agentController).getWeight() / totalWeight) /
                              agentControllers.get(agentController).getAgentCount();
            if (expected % 1 == 0)
            {
                assertEquals("Failure, user count at " + agent, expected, userPerAgent.get(agent), 0.0);
            }
            else
            {
                expected = expected - expected % 1 + 0.5;
                assertEquals("Failure, user count at " + agent, expected, userPerAgent.get(agent), 0.5);
            }
        }
    }

    /**
     * checks, if the user count of different tests per agent is equal to the expected value
     * 
     * @param testsPerAgent
     * @param agentControllers
     */
    private void checkCountDifferentTestsPerAgent(final Map<String, Integer> testsPerAgent,
                                                  final Map<String, AgentController> agentControllers)
    {
        final int totalWeight = computeTotalWeight(agentControllers);
        // compute the user count of different tests
        // user type : user count
        final Map<String, Integer> countTests = new HashMap<String, Integer>();
        for (final String agentTest : testsPerAgent.keySet())
        {
            final String test = agentTest.substring(agentTest.lastIndexOf(">") + 1, agentTest.length());
            if (countTests.containsKey(test))
            {
                countTests.put(test, countTests.remove(test) + testsPerAgent.get(agentTest));
            }
            else
            {
                countTests.put(test, testsPerAgent.get(agentTest));
            }
        }
        // check the user count at each agent
        for (final String agentTest : testsPerAgent.keySet())
        {
            final String agentController = agentTest.substring(0, agentTest.indexOf('-'));
            final int userCount = countTests.get(agentTest.substring(agentTest.lastIndexOf(">") + 1, agentTest.length()));
            double expected = userCount * ((double) agentControllers.get(agentController).getWeight() / totalWeight) /
                              agentControllers.get(agentController).getAgentCount();
            if (expected % 1 == 0)
            {
                assertEquals("Failure, count of tests at " + agentTest, expected, testsPerAgent.get(agentTest), 0.0);
            }
            else
            {
                expected = Math.round(expected);
                assertEquals("Failure, count of tests at " + agentTest, expected, testsPerAgent.get(agentTest), 1.0);
            }
        }
    }

    /**
     * checks, if the user count per agent controller is equal to the expected value
     * 
     * @param userPerAgentController
     * @param agentControllers
     */
    private void checkCountUserPerAgentController(final Map<String, Integer> userPerAgentController,
                                                  final Map<String, AgentController> agentControllers)
    {
        int totalUserCount = 0;
        final int totalWeight = computeTotalWeight(agentControllers);
        // compute the total user count of all agent controllers
        for (final String agentController : userPerAgentController.keySet())
        {
            totalUserCount += userPerAgentController.get(agentController);
        }
        // check the user count at each agent controller
        for (final String agentController : agentControllers.keySet())
        {
            double expected = totalUserCount * ((double) agentControllers.get(agentController).getWeight() / totalWeight);
            if (expected % 1 == 0)
            {
                assertEquals("Failure, user count at " + agentController, expected, userPerAgentController.get(agentController), 0.0);
            }
            else
            {
                expected = expected - expected % 1 + 0.5;
                assertEquals("Failure, user count at " + agentController, expected, userPerAgentController.get(agentController), 0.5);
            }
        }
    }

    /**
     * checks, if the user count of different tests per agent controller is equal to the expected value
     * 
     * @param testsPerAgentController
     * @param agentControllers
     */
    private void checkCountDifferentTestsPerAgentController(final Map<String, Integer> testsPerAgentController,
                                                            final Map<String, AgentController> agentControllers)
    {
        final int totalWeight = computeTotalWeight(agentControllers);
        // compute the user count of different tests
        // user type : user count
        final Map<String, Integer> countTests = new HashMap<String, Integer>();
        for (final String agentControllerTest : testsPerAgentController.keySet())
        {
            final String test = agentControllerTest.substring(agentControllerTest.lastIndexOf(">") + 1, agentControllerTest.length());
            if (countTests.containsKey(test))
            {
                countTests.put(test, countTests.remove(test) + testsPerAgentController.get(agentControllerTest));
            }
            else
            {
                countTests.put(test, testsPerAgentController.get(agentControllerTest));
            }
        }
        // check the user count at each agent controller
        for (final String agentControllerTest : testsPerAgentController.keySet())
        {
            final int userCount = countTests.get(agentControllerTest.substring(agentControllerTest.lastIndexOf(">") + 1,
                                                                               agentControllerTest.length()));
            final String agentController = agentControllerTest.substring(0, agentControllerTest.indexOf('-'));
            double expected = userCount * ((double) agentControllers.get(agentController).getWeight() / totalWeight);
            if (expected % 1 == 0)
            {
                assertEquals("failure, count of tests at " + agentControllerTest, expected,
                             testsPerAgentController.get(agentControllerTest), 0.0);
            }
            else
            {
                expected = Math.round(expected);
                assertEquals("failure, count of tests at " + agentControllerTest, expected,
                             testsPerAgentController.get(agentControllerTest), 1.0);
            }
        }
    }

    /**
     * checks, if the start and stop times of all user types are equal to the expected values
     */
    private void checkIfStartAndStopsAreEqual()
    {
        // check all start times
        for (final String expectedUser : expectedUserTypeStartTime.keySet())
        {
            Assert.assertTrue(expectedUser + " wasn' t started.", actualUserTypeStartTime.containsKey(expectedUser));
            final LinkedList<Long> startTimes = expectedUserTypeStartTime.get(expectedUser);
            final LinkedList<Long> actualStartTimes = actualUserTypeStartTime.get(expectedUser);
            for (int i = 0; i < startTimes.size(); i++)
            {
                final Long l = actualStartTimes.get(i);
                if (l != null)
                {
                    Assert.assertEquals("Start time is not equal: ", startTimes.get(i), l);
                }
            }
        }
        // check all stop times
        for (final String expectedUser : expectedUserTypeStopTime.keySet())
        {
            Assert.assertTrue(expectedUser + " wasn' t started.", actualUserTypeStopTime.containsKey(expectedUser));
            final LinkedList<Long> stopTimes = expectedUserTypeStopTime.get(expectedUser);
            final LinkedList<Long> actualStopTimes = actualUserTypeStopTime.get(expectedUser);
            for (int i = 0; i < stopTimes.size(); i++)
            {
                final Long l = actualStopTimes.get(i);
                if (l != null)
                {
                    Assert.assertEquals("Stop time is not equal: ", stopTimes.get(i), l);
                }
            }
        }
        // at least, the mapping of start respectively stop times has to be equal
        Assert.assertEquals(expectedUserTypeStartTime, actualUserTypeStartTime);
        Assert.assertEquals(expectedUserTypeStopTime, actualUserTypeStopTime);
    }

    /**
     * checks the user count function at each agent<br>
     * different user types at one agent will be added to one user count
     * 
     * @param testDeployment
     * @param agentControllers
     * @param loadProfile
     */
    private void checkUserCountFunctionAtEachAgent(final TestDeployment testDeployment, final Map<String, AgentController> agentControllers,
                                                   final TestLoadProfileConfiguration loadProfile)
    {
        // remember the longest measurement period
        final int measurementPeriod = computeLongestMeasurementPeriod(loadProfile);
        // remember the total agent weight
        final int totalAgentWeight = computeTotalWeight(agentControllers);
        // remember all user count functions per agent
        // agent : user functions
        final Map<String, List<int[][]>> userFunctions = computeUserFunctions(agentControllers, testDeployment);
        // check all agents
        for (final AgentController agentController : agentControllers.values())
        {
            for (final String agentID : agentController.getAgentIDs())
            {
                int[] expectedLastTotal = new int[loadProfile.getLoadTestConfiguration().size()];
                final int[] actualLastTotal = new int[userFunctions.get(agentID).size()];
                final int[] idxUser = new int[userFunctions.get(agentID).size()];
                // check each second, if the user count per agent is equal to the expected one
                for (int time = 0; time <= measurementPeriod; time++)
                {
                    int idx = 0;
                    // get expected user count
                    final int[][] dummy = getTotalUserCount(loadProfile, time, expectedLastTotal);
                    expectedLastTotal = dummy[0];
                    final int expectedTotalUserCount = dummy[1][0];
                    // get actual user count at the current agent
                    int actual = 0;
                    for (final int[][] function : userFunctions.get(agentID))
                    {
                        int totalUsers = actualLastTotal[idx];
                        while (idxUser[idx] < function.length && function[idxUser[idx]][0] <= time)
                        {
                            totalUsers = function[idxUser[idx]++][1];
                        }
                        actual += totalUsers;
                        actualLastTotal[idx] = totalUsers;
                        idx++;
                    }
                    double expectedAtEachAgent = ((expectedTotalUserCount + 0.0) *
                                                  ((agentController.getWeight() + 0.0) / totalAgentWeight)) /
                                                 agentController.getAgentCount();
                    if (expectedAtEachAgent % 1 == 0)
                    {
                        Assert.assertEquals("Failure after " + time + "s at Agent " + agentID, expectedAtEachAgent, actual, 1.0);
                    }
                    else
                    {
                        expectedAtEachAgent = expectedAtEachAgent - expectedAtEachAgent % 1 + 0.5;
                        Assert.assertEquals("Failure after " + time + "s at Agent " + agentID, expectedAtEachAgent, actual, 1.5);
                    }
                }
            }
        }
    }

    /**
     * check the user count function at each agent controller<br>
     * different user types at one agent controller will be added to one user count
     * 
     * @param testDeployment
     * @param agentControllers
     * @param loadProfile
     */
    private void checkUserCountFunctionAtEachAgentController(final TestDeployment testDeployment,
                                                             final Map<String, AgentController> agentControllers,
                                                             final TestLoadProfileConfiguration loadProfile)
    {
        // remember the longest measurement period
        final int measurementPeriod = computeLongestMeasurementPeriod(loadProfile);
        // remember the total agent weight
        final int totalAgentWeight = computeTotalWeight(agentControllers);
        // remember all user count functions per agent
        // agent : user functions
        final Map<String, List<int[][]>> userFunctions = computeUserFunctions(agentControllers, testDeployment);
        // check all agents
        for (final AgentController agentController : agentControllers.values())
        {
            // expected last user count for each user type
            int[] expectedLastTotal = new int[loadProfile.getLoadTestConfiguration().size()];
            for (int time = 0; time <= measurementPeriod; time++)
            {
                int actual = 0;
                // get expected user count
                final int[][] dummy = getTotalUserCount(loadProfile, time, expectedLastTotal);
                expectedLastTotal = dummy[0];
                final int expectedTotalUserCount = dummy[1][0];
                // get the actual user count at each agent controller
                for (final String agentID : agentController.getAgentIDs())
                {
                    final int[] actualLastTotal = new int[userFunctions.get(agentID).size()];
                    final int[] idxUser = new int[userFunctions.get(agentID).size()];
                    int idx = 0;
                    for (final int[][] function : userFunctions.get(agentID))
                    {
                        int totalUsers = actualLastTotal[idx];
                        while (idxUser[idx] < function.length && function[idxUser[idx]][0] <= time)
                        {
                            totalUsers = function[idxUser[idx]++][1];
                        }
                        actual += totalUsers;
                        actualLastTotal[idx] = totalUsers;
                        idx++;
                    }
                }
                double expectedAtEachAgent = ((expectedTotalUserCount + 0.0) * ((agentController.getWeight() + 0.0) / totalAgentWeight));
                if (expectedAtEachAgent % 1 == 0)
                {
                    Assert.assertEquals("Failure after " + time + "s at " + agentController.getName(), expectedAtEachAgent, actual, 1.0);
                }
                else
                {
                    expectedAtEachAgent = expectedAtEachAgent - expectedAtEachAgent % 1 + 0.5;
                    Assert.assertEquals("Failure after " + time + "s at " + agentController.getName(), expectedAtEachAgent, actual, 1.5);
                }
            }
        }
    }

    /**
     * returns the user count at each agent and the total user count at the given time
     */
    private int[][] getTotalUserCount(final TestLoadProfileConfiguration loadProfile, final int time, final int[] lastTotal)
    {
        // result[0][...] : user count at each agent
        // result[1][0] : total user count
        final int[][] result = new int[2][];
        final int[] totalUser = new int[1];
        // specified the current test case
        int idx = 0;
        // get the user count for all test cases
        for (final TestCaseLoadProfileConfiguration testCaseLoadProfile : loadProfile.getLoadTestConfiguration())
        {
            // compute the active user at the given time
            final int[][] users = testCaseLoadProfile.getNumberOfUsers();
            final CompositeFunction userCount = new CompositeFunction(users);
            final double count = userCount.calculateY(time);
            final int user = count > lastTotal[idx] ? (int) Math.floor(count) : (int) Math.ceil(count);
            // add the current user to the total user count
            totalUser[0] += user;
            // adjust the last user count at this test case
            lastTotal[idx] = user;
            // get the next test case
            idx++;
        }
        result[0] = lastTotal;
        result[1] = totalUser;
        return result;
    }

    /**
     * checks the user count at all agents for each user type
     * 
     * @param testDeployment
     * @param agentControllers
     * @param loadProfile
     */
    private void checkUserCountFunctionForEachUserTypeAtEachAgent(final TestDeployment testDeployment,
                                                                  final Map<String, AgentController> agentControllers,
                                                                  final TestLoadProfileConfiguration loadProfile)
    {
        // remember the longest measurement period
        final int measurementPeriod = computeLongestMeasurementPeriod(loadProfile);
        // remember the total agent weight
        final int totalAgentWeight = computeTotalWeight(agentControllers);
        // remember all user count functions per agent
        // agent : user name : user function
        final Map<String, Map<String, int[][]>> userFunctions = new HashMap<String, Map<String, int[][]>>();
        // get all user count functions
        for (final AgentController agentController : agentControllers.values())
        {
            for (final String agentID : agentController.getAgentIDs())
            {
                if (testDeployment.getUserList(agentID) != null)
                {
                    // remember, whether this user count function at the current agent is already saved
                    // agent id : user types
                    final Map<String, List<String>> started = new HashMap<String, List<String>>();
                    started.put(agentID, new ArrayList<String>());
                    for (final TestUserConfiguration testUserConfiguration : testDeployment.getUserList(agentID))
                    {
                        final boolean isStarted = started.get(agentID).contains(testUserConfiguration.getUserName());
                        // just remember the same user count function at the same agent once
                        if (!isStarted)
                        {
                            // add the user function to the agent
                            if (userFunctions.containsKey(agentID))
                            {
                                userFunctions.get(agentID).put(testUserConfiguration.getUserName(), testUserConfiguration.getUsers());
                            }
                            else
                            {
                                final Map<String, int[][]> function = new HashMap<String, int[][]>();
                                function.put(testUserConfiguration.getUserName(), testUserConfiguration.getUsers());
                                userFunctions.put(agentID, function);
                            }
                            // remember the current configuration
                            started.get(agentID).add(testUserConfiguration.getUserName());
                        }
                    }
                }
            }
        }
        // check all agents
        for (final AgentController agentController : agentControllers.values())
        {
            for (final String agentID : agentController.getAgentIDs())
            {
                for (final TestCaseLoadProfileConfiguration testCaseLoadProfile : loadProfile.getLoadTestConfiguration())
                {
                    int expectedLastTotal = 0;
                    int actualLastTotal = 0;
                    int idx = 0;
                    final int[][] users = userFunctions.get(agentID).get(testCaseLoadProfile.getUserName());
                    // check each second, if the user count per agent is equal to the expected one
                    for (int time = 0; time <= measurementPeriod; time++)
                    {
                        // get expected user count
                        expectedLastTotal = getUserCount(testCaseLoadProfile, time, expectedLastTotal);
                        // get actual user count
                        int totalUsers = actualLastTotal;
                        if (users != null)
                        {
                            while (idx < users.length && users[idx][0] <= time)
                            {
                                totalUsers = users[idx++][1];
                            }
                        }
                        actualLastTotal = totalUsers;
                        double expectedAtEachAgent = ((expectedLastTotal + 0.0) *
                                                      ((agentController.getWeight() + 0.0) / totalAgentWeight)) /
                                                     agentController.getAgentCount();
                        if (expectedAtEachAgent % 1 == 0)
                        {
                            Assert.assertEquals("Failure after " + time + "s at Agent " + agentID, expectedAtEachAgent, actualLastTotal,
                                                2.0);
                        }
                        else
                        {
                            expectedAtEachAgent = expectedAtEachAgent - expectedAtEachAgent % 1 + 0.5;
                            Assert.assertEquals("Failure after " + time + "s at Agent " + agentID + " at test " +
                                                testCaseLoadProfile.getUserName(), expectedAtEachAgent, actualLastTotal, 2.5);
                        }
                    }
                }
            }
        }
    }

    /**
     * returns the user count at the given time for the specified test case
     */
    private int getUserCount(final TestCaseLoadProfileConfiguration testCaseLoadProfile, final int time, final int lastTotal)
    {
        final int[][] users = testCaseLoadProfile.getNumberOfUsers();
        final CompositeFunction userCount = new CompositeFunction(users);
        final double count = userCount.calculateY(time);
        return count > lastTotal ? (int) Math.floor(count) : (int) Math.ceil(count);
    }

    /**
     * executes the validations
     * 
     * @param agentControllers
     * @param testLoadProfile
     */
    private void validate(final Map<String, AgentController> agentControllers, final TestLoadProfileConfiguration testLoadProfile)
    {
        // create the test deployment
        final TestDeployment deployment = createTestDeployment(agentControllers, testLoadProfile);
        // check the test deployment
        validateDeployment(deployment, agentControllers);
        // check all user functions
        validateUserFunction(deployment, agentControllers, testLoadProfile);
    }

    /**
     * executes the different tests to validate the test deployment
     * 
     * @param testDeployment
     * @param agentControllers
     */
    private void validateDeployment(final TestDeployment testDeployment, final Map<String, AgentController> agentControllers)
    {
        // user per agent controller
        final Map<String, Integer> userPerAgentController = userPerAgentController(testDeployment);
        checkCountUserPerAgentController(userPerAgentController, agentControllers);

        // user per agent
        final Map<String, Integer> userPerAgent = userPerAgent(testDeployment);
        checkCountUserPerAgent(userPerAgent, agentControllers);

        // different tests per agent controller
        final Map<String, Integer> testsPerAgentController = differentTestsPerAgentController(testDeployment);
        checkCountDifferentTestsPerAgentController(testsPerAgentController, agentControllers);

        // different tests per agent
        final Map<String, Integer> testsPerAgent = differentTestsPerAgent(testDeployment);
        checkCountDifferentTestsPerAgent(testsPerAgent, agentControllers);
    }

    /**
     * executes the different tests to validate the user functions
     * 
     * @param deployment
     * @param agentControllers
     * @param testLoadProfile
     */
    private void validateUserFunction(final TestDeployment deployment, final Map<String, AgentController> agentControllers,
                                      final TestLoadProfileConfiguration testLoadProfile)
    {
        // start the user of the deployment
        executeDeployment(deployment);
        // compute the expected user starts and stops
        computeExpectedStartAndStopTimes(testLoadProfile);
        // sort the lists with the actual start and stop times
        for (final String user : actualUserTypeStartTime.keySet())
        {
            final LinkedList<Long> startTimes = actualUserTypeStartTime.get(user);
            final LinkedList<Long> stopTimes = actualUserTypeStopTime.get(user);
            if (startTimes != null)
            {
                Collections.sort(startTimes);
            }
            if (stopTimes != null)
            {
                Collections.sort(stopTimes);
            }
        }
        // check, if the actual start and stop times are equal to the expected one
        checkIfStartAndStopsAreEqual();
        // check the user count at all agents
        checkUserCountFunctionAtEachAgent(deployment, agentControllers, testLoadProfile);
        // check the user count at all agents for each user type
        checkUserCountFunctionForEachUserTypeAtEachAgent(deployment, agentControllers, testLoadProfile);
        // check the user count at all agent controllers
        checkUserCountFunctionAtEachAgentController(deployment, agentControllers, testLoadProfile);
    }

    /**
     * starts the user count controller timer task of the random execution timer<br>
     * remembers the actual start and stop times of all user types in a map
     */
    private void executeDeployment(final TestDeployment testDeployment)
    {
        // get the test user configuration list for each agent
        for (final String agentID : testDeployment.getAgentIDs())
        {
            if (testDeployment.getUserList(agentID) != null)
            {
                // remember, whether this agent has started this user
                final Map<String, TestUserConfiguration> started = new HashMap<String, TestUserConfiguration>();
                for (final TestUserConfiguration testUserConfiguration : testDeployment.getUserList(agentID))
                {
                    final String userName = testUserConfiguration.getUserName();
                    final TestUserConfiguration isStarted = started.get(userName);
                    // just start this user, if it did not already started
                    if (isStarted == null)
                    {
                        // get an instance of the random execution timer
                        final RandomExecutionTimer executionTimer = PowerMockito.mock(RandomExecutionTimer.class);
                        // create a timer task
                        final RandomExecutionTimer.UserCountControllerTimerTask timerTask = new RandomExecutionTimer.UserCountControllerTimerTask(testUserConfiguration.getUsers(),
                                                                                                                                                  executionTimer,
                                                                                                                                                  0);
                        int lastTotal = 0;
                        for (long time = 0L; time <= testUserConfiguration.getMeasurementPeriod() / 1000; time++)
                        {
                            timerTask.run(time);

                            final int userDiff = timerTask.getLastTotal() - lastTotal;

                            // user stopped
                            if (userDiff < 0)
                            {
                                for (int i = userDiff; i < 0; i++)
                                {
                                    // remember stop
                                    setActualUserTypeStopTime(userName, time);
                                }
                            }
                            // user started
                            else
                            {
                                for (int i = 0; i < userDiff; i++)
                                {
                                    // remember start
                                    setActualUserTypeStartTime(userName, time);
                                }
                            }

                            lastTotal += userDiff;
                        }
                        started.put(userName, testUserConfiguration);
                    }
                }
            }
        }
    }

    /**
     * computes the expected start and stop times of all user types and remembers the times in a map
     */
    private void computeExpectedStartAndStopTimes(final TestLoadProfileConfiguration testLoadProfile)
    {
        final List<TestCaseLoadProfileConfiguration> testCaseLoadProfiles = testLoadProfile.getLoadTestConfiguration();
        // compute the start times for all test cases
        for (final TestCaseLoadProfileConfiguration testCaseLoadProfile : testCaseLoadProfiles)
        {
            final String name = testCaseLoadProfile.getUserName();
            final int[][] users = testCaseLoadProfile.getNumberOfUsers();
            final int measurementPeriod = testCaseLoadProfile.getMeasurementPeriod();
            final CompositeFunction userCount = new CompositeFunction(users);
            int lastStart = 0;
            // compute the start times for each second of the measurement
            for (long time = 0; time <= measurementPeriod; time++)
            {
                final double count = userCount.calculateY(time);
                final int toRelease = count > lastStart ? (int) Math.floor(count) - lastStart : (int) Math.ceil(count) - lastStart;
                // user has to start
                if (toRelease > 0)
                {
                    for (int i = 0; i < toRelease; i++)
                    {
                        // remember the start time in a map
                        setExpectedUserTypeStartTime(name, time);
                    }
                }
                // user has to stop
                else if (toRelease < 0)
                {
                    for (int i = toRelease; i < 0; i++)
                    {
                        // remember the stop time in a map
                        setExpectedUserTypeStopTime(name, time);
                    }
                }
                // remember the user count in this second
                lastStart += toRelease;
            }
        }
    }

    /**
     * returns the total weight of all agent controllers in the mapping
     * 
     * @param agentControllers
     * @return
     */
    private int computeTotalWeight(final Map<String, AgentController> agentControllers)
    {
        int totalAgentWeight = 0;
        for (final AgentController agentController : agentControllers.values())
        {
            totalAgentWeight += agentController.getWeight();
        }
        return totalAgentWeight;
    }

    /**
     * returns the longest measurement period of all test cases
     * 
     * @return
     */
    private int computeLongestMeasurementPeriod(final TestLoadProfileConfiguration loadProfile)
    {
        int measurementPeriod = 0;
        for (final TestCaseLoadProfileConfiguration configuration : loadProfile.getLoadTestConfiguration())
        {
            if (configuration.getMeasurementPeriod() > measurementPeriod)
            {
                measurementPeriod = configuration.getMeasurementPeriod();
            }
        }
        return measurementPeriod;
    }

    /**
     * returns for each agent its user function
     */
    private Map<String, List<int[][]>> computeUserFunctions(final Map<String, AgentController> agentControllers,
                                                            final TestDeployment testDeployment)
    {
        final Map<String, List<int[][]>> userFunctions = new HashMap<String, List<int[][]>>();
        // get all user count functions
        for (final AgentController agentController : agentControllers.values())
        {
            for (final String agentID : agentController.getAgentIDs())
            {
                if (testDeployment.getUserList(agentID) != null)
                {
                    // remember, whether this user count function at the current agent is already saved
                    // agent id : user types
                    final Map<String, List<String>> started = new HashMap<String, List<String>>();
                    started.put(agentID, new ArrayList<String>());
                    for (final TestUserConfiguration testUserConfiguration : testDeployment.getUserList(agentID))
                    {
                        final boolean isStarted = started.get(agentID).contains(testUserConfiguration.getUserName());
                        // just remember the same user count function at the same agent once
                        if (!isStarted)
                        {
                            // add the user function to the agent
                            if (userFunctions.containsKey(agentID))
                            {
                                userFunctions.get(agentID).add(testUserConfiguration.getUsers());
                            }
                            else
                            {
                                final List<int[][]> functions = new ArrayList<int[][]>();
                                functions.add(testUserConfiguration.getUsers());
                                userFunctions.put(agentID, functions);
                            }
                            // remember the current configuration
                            started.get(agentID).add(testUserConfiguration.getUserName());
                        }
                    }
                }
            }
        }
        return userFunctions;
    }

    /**
     * creates a new test case load profile configuration
     * 
     * @param userName
     *            name of the user
     * @param numberOfUsers
     *            user count
     * @return TestCaseLoadProfileConfiguration
     */
    private TestCaseLoadProfileConfiguration createTestCaseLoadProfile(final String userName, final int[][] numberOfUsers)
    {
        final TestCaseLoadProfileConfiguration testCaseLoadProfile = new TestCaseLoadProfileConfiguration();
        testCaseLoadProfile.setUserName(userName);
        testCaseLoadProfile.setMeasurementPeriod(300);
        testCaseLoadProfile.setNumberOfUsers(numberOfUsers);
        return testCaseLoadProfile;
    }

    /**
     * creates a new agent controller
     * 
     * @param agentName
     *            the name of the agent controller
     * @param weight
     *            the weight of the agent controller
     * @param count
     *            the count of agents of the agent controller
     * @return AgentController
     * @throws IOException
     */
    private AgentController createAgentController(final String agentName, final int weight, final int count, final boolean runsCPTests)
        throws IOException
    {
        final AgentController agentController = new TestAgentController();
        agentController.init(agentName, null, weight, count, 0, runsCPTests);
        return agentController;
    }

    /**
     * creates a new agent controller
     * 
     * @param agentName
     *            the name of the agent controller
     * @param weight
     *            the weight of the agent controller
     * @param count
     *            the count of agents of the agent controller
     * @return AgentController
     * @throws IOException
     */
    private AgentController createAgentController(final String agentName, final int weight, final int count) throws IOException
    {
        return createAgentController(agentName, weight, count, false);
    }

    private TestDeployment createTestDeployment(final Map<String, AgentController> agentControllers,
                                                final TestLoadProfileConfiguration testLoadProfile)
    {
        // create TestDeployer
        final TestDeployer testDeployer = new TestDeployer(agentControllers);

        // create TestDeployment
        final TestDeployment testDeployment = testDeployer.createTestDeployment(testLoadProfile);

        return testDeployment;
    }

    private TestLoadProfileConfiguration createTestLoadProfileWithOneTestCase()
    {
        final int[][] users1 =
            {
                {
                    0, 3
                },
                {
                    10, 54
                },
                {
                    37, 156
                },
                {
                    50, 12
                }
            };
        final TestCaseLoadProfileConfiguration testCaseLoadProfile1 = createTestCaseLoadProfile("Test1", users1);
        final TestLoadProfileConfiguration testLoadProfile = new TestLoadProfileConfiguration();
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile1);
        return testLoadProfile;
    }

    private TestLoadProfileConfiguration createTestLoadProfileWithTwoEqualTestCases()
    {
        final int[][] users1 =
            {
                {
                    0, 5
                },
                {
                    20, 4
                },
                {
                    86, 176
                },
                {
                    134, 12
                },
                {
                    134, 37
                },
                {
                    220, 37
                },
                {
                    220, 54
                }
            };
        final TestCaseLoadProfileConfiguration testCaseLoadProfile1 = createTestCaseLoadProfile("Test1", users1);
        final TestCaseLoadProfileConfiguration testCaseLoadProfile2 = createTestCaseLoadProfile("Test2", users1);
        final TestLoadProfileConfiguration testLoadProfile = new TestLoadProfileConfiguration();
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile1);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile2);
        return testLoadProfile;
    }

    private TestLoadProfileConfiguration createTestLoadProfileWithTwoDifferentTestCases()
    {
        final int[][] users1 =
            {
                {
                    0, 1
                },
                {
                    20, 4
                },
                {
                    46, 37
                },
                {
                    134, 12
                },
                {
                    211, 37
                },
                {
                    220, 37
                },
                {
                    220, 54
                }
            };
        final TestCaseLoadProfileConfiguration testCaseLoadProfile1 = createTestCaseLoadProfile("Test1", users1);
        final int[][] users2 =
            {
                {
                    0, 0
                },
                {
                    20, 0
                },
                {
                    50, 30
                },
                {
                    98, 30
                },
                {
                    98, 12
                }
            };
        final TestCaseLoadProfileConfiguration testCaseLoadProfile2 = createTestCaseLoadProfile("Test2", users2);
        final TestLoadProfileConfiguration testLoadProfile = new TestLoadProfileConfiguration();
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile1);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile2);
        return testLoadProfile;
    }

    private TestLoadProfileConfiguration createTestLoadProfileWithMoreDifferentTestCases()
    {
        final int[][] users1 =
            {
                {
                    0, 20
                }
            };
        final TestCaseLoadProfileConfiguration testCaseLoadProfile1 = createTestCaseLoadProfile("Test1", users1);
        final int[][] users2 =
            {
                {
                    0, 0
                },
                {
                    50, 0
                },
                {
                    50, 57
                }
            };
        final TestCaseLoadProfileConfiguration testCaseLoadProfile2 = createTestCaseLoadProfile("Test2", users2);
        final int[][] users3 =
            {
                {
                    0, 5
                },
                {
                    18, 5
                },
                {
                    18, 63
                },
                {
                    97, 63
                },
                {
                    97, 33
                },
                {
                    123, 33
                },
                {
                    123, 4
                }
            };
        final TestCaseLoadProfileConfiguration testCaseLoadProfile3 = createTestCaseLoadProfile("Test3", users3);
        final int[][] users4 =
            {
                {
                    0, 0
                },
                {
                    20, 0
                },
                {
                    46, 5
                },
                {
                    98, 80
                },
                {
                    156, 76
                },
                {
                    178, 55
                },
                {
                    259, 2
                },
                {
                    319, 48
                }
            };
        final TestCaseLoadProfileConfiguration testCaseLoadProfile4 = createTestCaseLoadProfile("Test4", users4);
        final int[][] users5 =
            {
                {
                    0, 0
                },
                {
                    20, 0
                },
                {
                    45, 37
                },
                {
                    46, 15
                },
                {
                    75, 15
                },
                {
                    146, 0
                },
                {
                    289, 98
                }
            };
        final TestCaseLoadProfileConfiguration testCaseLoadProfile5 = createTestCaseLoadProfile("Test5", users5);
        final int[][] users6 =
            {
                {
                    0, 1
                },
                {
                    4, 57
                },
                {
                    7, 75
                },
                {
                    15, 0
                },
                {
                    20, 0
                },
                {
                    20, 13
                },
                {
                    25, 0
                },
                {
                    28, 0
                },
                {
                    30, 67
                },
                {
                    35, 78
                },
                {
                    40, 10
                },
                {
                    45, 0
                },
                {
                    55, 18
                }
            };
        final TestCaseLoadProfileConfiguration testCaseLoadProfile6 = createTestCaseLoadProfile("Test6", users6);
        final TestLoadProfileConfiguration testLoadProfile = new TestLoadProfileConfiguration();
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile1);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile2);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile3);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile4);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile5);
        testLoadProfile.addTestCaseLoadProfileConfiguration(testCaseLoadProfile6);
        return testLoadProfile;
    }

    /**
     * adds a new expected start time
     */
    private void setExpectedUserTypeStartTime(final String userType, final Long time)
    {
        LinkedList<Long> startTimes = expectedUserTypeStartTime.get(userType);
        if (startTimes == null)
        {
            startTimes = new LinkedList<Long>();
            expectedUserTypeStartTime.put(userType, startTimes);
        }
        startTimes.addLast(time);
    }

    /**
     * adds a new expected stop time
     */
    private void setExpectedUserTypeStopTime(final String userType, final Long time)
    {
        LinkedList<Long> stopTimes = expectedUserTypeStopTime.get(userType);
        if (stopTimes == null)
        {
            stopTimes = new LinkedList<Long>();
            expectedUserTypeStopTime.put(userType, stopTimes);
        }
        stopTimes.addLast(time);
    }

    /**
     * adds a new actual start time
     */
    private void setActualUserTypeStartTime(final String userName, final Long time)
    {
        LinkedList<Long> startTimes = actualUserTypeStartTime.get(userName);
        if (startTimes == null)
        {
            startTimes = new LinkedList<Long>();
            actualUserTypeStartTime.put(userName, startTimes);
        }
        startTimes.addLast(time);
    }

    /**
     * adds a new actual stop time
     */
    private void setActualUserTypeStopTime(final String userName, final Long time)
    {
        LinkedList<Long> stopTimes = actualUserTypeStopTime.get(userName);
        if (stopTimes == null)
        {
            stopTimes = new LinkedList<Long>();
            actualUserTypeStopTime.put(userName, stopTimes);
        }
        stopTimes.addLast(time);
    }

    @Test
    public void _issue2973() throws Exception
    {
        // create load profile
        final TestCaseLoadProfileConfiguration config = new TestCaseLoadProfileConfiguration();

        config.setTestCaseClassName("foo.TFoo");
        config.setArrivalRate(null);
        config.setNumberOfUsers(new IntValueLoadFunctionParser().parse("0/1000"));
        config.setNumberOfIterations(0);
        config.setShutdownPeriod(5 * 60 * 1000);
        config.setWarmUpPeriod(0);
        config.setMeasurementPeriod(30 * 60 * 1000);
        config.setInitialDelay(0);
        config.setUserName("TFoo");
        config.setComplexLoadFunction(null);
        config.setRampUpPeriod(-1);
        config.setLoadFactor(new DoubleValueLoadFunctionParser().parse("0/1.0"));
        config.setCPTest(false);
        config.setActionThinkTime(500);
        config.setActionThinkTimeDeviation(250);

        TestLoadProfileConfiguration testLoadProfile = new TestLoadProfileConfiguration();
        testLoadProfile.addTestCaseLoadProfileConfiguration(config);

        // create 10 agent controllers with 10 agents each, same weight
        final Map<String, AgentController> agentControllers = new HashMap<String, AgentController>();
        for (int i = 0; i < 10; i++)
        {
            final AgentController agentController = createAgentController(String.format("ac%03d", i), 1, 10);
            agentControllers.put(agentController.getName(), agentController);
        }

        // create the deployment plan
        final TestDeployment deployment = createTestDeployment(agentControllers, testLoadProfile);

        // now validate that the assigned agent indexes are at least 10 apart (i.e. the next agent is on another agent
        // machine)
        final List<String> sortedAgentIds = new ArrayList<>(deployment.getAgentIDs());
        Collections.sort(sortedAgentIds);

        String lastAgentId = null;
        int lastAgentIndex = -1;
        for (final String agentId : sortedAgentIds)
        {
            final TestUserConfiguration user = deployment.getUserList(agentId).get(0);
            final int agentIndex = user.getAgentIndex();

            // System.out.printf("%s - %2d - %2d\n", agentId, agentIndex, lastAgentIndex);

            if (lastAgentId != null)
            {
                Assert.assertTrue(String.format("Suboptimal load distribution: %s follows %s index-wise, but is on the same machine",
                                                agentId, lastAgentId),
                                  Math.abs(lastAgentIndex - agentIndex) >= 10);
            }

            lastAgentId = agentId;
            lastAgentIndex = agentIndex;
        }
    }
}
