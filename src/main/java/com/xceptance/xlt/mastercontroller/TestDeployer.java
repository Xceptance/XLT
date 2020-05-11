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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.agent.unipro.CompositeFunction;
import com.xceptance.xlt.agentcontroller.AgentController;
import com.xceptance.xlt.agentcontroller.TestUserConfiguration;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.common.XltConstants;

/**
 * The TestDeployer is responsible to deploy test users to agent controllers according to the configured test (case)
 * load profile. Any manual assignment of test users to agent controllers done in the configuration is retained, but
 * test users with no assignment are spread evenly across all agent controllers.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class TestDeployer
{
    /**
     * A comparator to compare agent IDs ("ac001-0", "ac001-1", "ac002-0", etc.) when sorting. This comparator reverses
     * the agent IDs before comparing them. This ensures that the agents of a certain agent controller do not form a
     * cluster, but are rather intermingled with the agents of all the other agent controllers. Fixes issue #2973.
     */
    private static final Comparator<String> agentIdComparator = (String s1, String s2) -> StringUtils.reverse(s1)
                                                                                                     .compareTo(StringUtils.reverse(s2));

    /**
     * the agent controller mapping
     */
    private final Map<String, AgentController> agentControllers;

    /**
     * Creates a new test deployer.
     * 
     * @param agentControllers
     *            the agent controller mapping
     */
    public TestDeployer(final Map<String, AgentController> agentControllers)
    {
        this.agentControllers = agentControllers;
    }

    /**
     * Validates the agent controller map of this instance.
     */
    private void validateAgentControllerMap()
    {
        if (agentControllers == null || agentControllers.isEmpty())
        {
            throw new IllegalArgumentException("Must specify a valid agent controller map.");
        }
        for (final AgentController ac : agentControllers.values())
        {
            if (ac.getWeight() == 0)
            {
                throw new IllegalArgumentException("Agent controller '" + ac.getName() + "' has zero weight.");
            }
            if (ac.getAgentIDs().isEmpty())
            {
                throw new IllegalArgumentException("Agent controller '" + ac.getName() + "' has no agent IDs.");
            }
            if (ac.getAgentCount() == 0)
            {
                throw new IllegalArgumentException("Agent controller '" + ac.getName() + "' has no agents.");
            }
            if (ac.getAgentCount() != ac.getAgentIDs().size())
            {
                throw new IllegalArgumentException("Number of agents and number of agent IDs of agent controller '" + ac.getName() +
                                                   "' differ.");
            }
        }
    }

    /**
     * Creates a new test deployment for the given load test profile. Deployment strategy works as follows:
     * <ol>
     * <li>Compute the weight of all known agents.</li>
     * <li>Build a priority queue and use this queue to create the agent/user assignments. The works since the queue's
     * entries are compared by their <em>weighted user count</em> and the 1st element of the queue is always the
     * <em>smallest</em> one.</li>
     * <li>For each test case:
     * <ul>
     * <li>create list of test user configurations (size = max. number of users),</li>
     * <li>pre-assign each user to an agent using the priority queue and</li>
     * <li>add user function to global user user function.</li>
     * </ul>
     * </li>
     * <li>Assign each agent a bucket of users for each configured test case as computed in the previous steps.</li>
     * <li>For each configured test case, collect the set of agents that deploy at least one user.</li>
     * <li>Iterate through the sets of agents and assign each user
     * <ul>
     * <li>the index of each agent and</li>
     * <li>the agents weights</li>
     * </ul>
     * for this user type.
     * </ol>
     * 
     * @param loadProfile
     *            the load test profile to use
     * @return new test deployment for given profile
     */
    public TestDeployment createTestDeployment(final TestLoadProfileConfiguration loadProfile)
    {
        validateAgentControllerMap();

        /*
         * Collect all agentIDs, build priority queue and construct agent weight map.
         */
        final PriorityQueue<ControllerEntry> queue = new PriorityQueue<ControllerEntry>();
        final PriorityQueue<ControllerEntry> queueCP = new PriorityQueue<ControllerEntry>();
        final Set<String> agentIDs = new HashSet<String>();

        double totalWeight = 0;
        double totalWeightCP = 0;

        for (final AgentController agentController : agentControllers.values())
        {
            final Set<String> agent_ids = agentController.getAgentIDs();
            final int weight = agentController.getWeight();
            agentIDs.addAll(agent_ids);

            if (agentController.runsClientPerformanceTests())
            {
                queueCP.add(new ControllerEntry(weight, agent_ids, true));
                totalWeightCP += weight;
            }
            else
            {
                queue.add(new ControllerEntry(weight, agent_ids, false));
                totalWeight += weight;
            }
        }

        final Map<String, Double> agentWeights = new HashMap<String, Double>();
        for (final AgentController agentController : agentControllers.values())
        {
            final double total_weight = agentController.runsClientPerformanceTests() ? totalWeightCP : totalWeight;
            final double agentWeight = (agentController.getWeight() / total_weight) / agentController.getAgentCount();
            for (final String agentID : agentController.getAgentIDs())
            {
                agentWeights.put(agentID, agentWeight);
            }
        }

        /*
         * Create test user configurations and compute number of users of each user type for each agent.
         */

        final TestDeployment testDeployment = new TestDeployment(agentIDs);
        /** agentID : (testCase : numUsers) */
        final Map<String, Map<String, Integer>> deploymentPlan = new HashMap<String, Map<String, Integer>>();
        /** testCase : userList */
        final Map<String, List<TestUserConfiguration>> usersPerTestCase = new HashMap<String, List<TestUserConfiguration>>();
        // loop through all test-case configurations and build user/agent deployment plan

        final GlobalUserFunction sched = new GlobalUserFunction();
        for (final TestCaseLoadProfileConfiguration config : loadProfile.getLoadTestConfiguration())
        {
            final List<TestUserConfiguration> userConfigs = scheduleUsers(config);
            final String testCaseName = config.getUserName();
            usersPerTestCase.put(testCaseName, userConfigs);
            sched.register(config.getUserName(), config.getNumberOfUsers());

            final int nbUsers = userConfigs.size();
            final boolean isCPTest = config.isCPTest();
            final PriorityQueue<ControllerEntry> q = isCPTest ? queueCP : queue;
            if (q == null)
            {
                throw new RuntimeException("No controller available. Please check configuration.");
            }

            for (int i = 0; i < nbUsers; i++)
            {
                // take the "smallest" agent and increment its user count
                final ControllerEntry entry = q.poll();
                final AgentEntry aEntry = entry.getLightestAgent();
                entry.incUsers(testCaseName, aEntry);

                // remember number of users for this test-case and this agent
                Map<String, Integer> assignedUsers = deploymentPlan.get(aEntry.agentID);
                if (assignedUsers == null)
                {
                    assignedUsers = new HashMap<String, Integer>();
                    deploymentPlan.put(aEntry.agentID, assignedUsers);
                }

                final int usersSoFar = zeroIfNullElseIntValue(assignedUsers.get(testCaseName));
                assignedUsers.put(testCaseName, usersSoFar + 1);

                // put agent back to queue
                q.add(entry);
            }
        }

        // compute agent specific user function
        final Map<String, Map<String, Object>> agentSpecificUsers = computeAgentSpecificUsers(sched, deploymentPlan);

        /*
         * Assign users to agents.
         */

        for (final Map.Entry<String, Map<String, Integer>> e : deploymentPlan.entrySet())
        {
            final String agentID = e.getKey();
            final Map<String, Integer> assignedUsers = e.getValue();
            final List<TestUserConfiguration> userList = testDeployment.getUserList(agentID);

            for (final String testCaseName : assignedUsers.keySet())
            {
                final int nbUsers = assignedUsers.get(testCaseName);
                final List<TestUserConfiguration> subList = usersPerTestCase.get(testCaseName).subList(0, nbUsers);
                for (final TestUserConfiguration user : subList)
                {
                    userList.add(user);
                }

                subList.clear();
            }
        }

        // determine which agents and how many agents in total execute a certain test case
        determineAgentCountAndIndex(testDeployment, agentWeights, agentSpecificUsers);

        // check whether the user number exceeds the number of permitted users
        finalizeDeployment(testDeployment, checkDeployedUserCount(testDeployment));

        return testDeployment;
    }

    /**
     * Creates a user configuration for each user of each test case. The users are arranged temporally (i.e., with an
     * individual delay) such that the resulting cluster-wide load reflects exactly the initial delay and ramp-up period
     * settings for a test case.
     * 
     * @param config
     *            the configuration of one test case
     * @return the list of user configurations created
     */
    private List<TestUserConfiguration> scheduleUsers(final TestCaseLoadProfileConfiguration config)
    {
        final List<TestUserConfiguration> userList = new ArrayList<TestUserConfiguration>();

        final String userName = config.getUserName();
        final String testCaseClassName = config.getTestCaseClassName();
        final int iterations = config.getNumberOfIterations();
        final int initialDelay = config.getInitialDelay() * 1000;
        final int warmUpPeriod = config.getWarmUpPeriod() * 1000;
        final int measurementPeriod = config.getMeasurementPeriod() * 1000;
        final int shutdownPeriod = config.getShutdownPeriod() * 1000;
        final int[][] arrivalRates = config.getArrivalRate();
        final int[][] users = config.getNumberOfUsers();

        // determine the highest number of concurrent users
        int userCount = 0;
        for (int i = 0; i < users.length; i++)
        {
            userCount = Math.max(userCount, users[i][1]);
        }

        // create the user configurations
        for (int i = 0; i < userCount; i++)
        {
            final TestUserConfiguration userConfig = new TestUserConfiguration();
            userList.add(userConfig);

            // general settings
            userConfig.setInitialDelay(initialDelay);
            userConfig.setMeasurementPeriod(measurementPeriod);
            userConfig.setNumberOfIterations(iterations);
            userConfig.setNumberOfUsers(userCount);
            userConfig.setArrivalRates(arrivalRates);
            userConfig.setShutdownPeriod(shutdownPeriod);
            userConfig.setTestCaseClassName(testCaseClassName);
            userConfig.setUserName(userName);
            userConfig.setUsers(users);
            userConfig.setWarmUpPeriod(warmUpPeriod);

            // user-specific settings
            userConfig.setInstance(i);
        }

        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            XltLogger.runTimeLogger.info(userList.toString());
        }

        return userList;
    }

    /**
     * Count the number of users configured in the given deployment plan.
     * 
     * @param deployment
     *            the deployment plan
     * @return total number of users for the given deployment plan
     */
    private int checkDeployedUserCount(final TestDeployment deployment)
    {
        int totalUsers = 0;

        for (final List<TestUserConfiguration> userList : deployment.getAllUserLists())
        {
            totalUsers += userList.size();
        }

        return totalUsers;
    }

    /**
     * Finalizes the given deployment plan.
     * 
     * @param deployment
     *            the deployment plan
     * @param totalUsers
     *            the total number of users configured in the given deployment plan
     */
    private void finalizeDeployment(final TestDeployment deployment, final int totalUsers)
    {
        int absoluteUserNumber = 0;

        for (final List<TestUserConfiguration> userList : deployment.getAllUserLists())
        {
            for (final TestUserConfiguration testUserConfiguration : userList)
            {
                testUserConfiguration.setAbsoluteUserNumber(absoluteUserNumber++);
                testUserConfiguration.setTotalUserCount(totalUsers);
            }
        }
    }

    /**
     * Walks through the given deployment and sets for each test user configuration the index of its agent and the total
     * number of agents running its test scenario. Note that agent index and count are "local" to the test case, i.e.
     * these values have nothing to do with the global agent index and count. This is because in case of only a few test
     * users and many agents, test users may not be deployed to each agent.
     * 
     * @param deployment
     *            the deployment plan
     * @param agentWeights
     *            the agentID/agentWeight mapping
     * @param agentSpecificUsers
     */
    private void determineAgentCountAndIndex(final TestDeployment deployment, final Map<String, Double> agentWeights,
                                             final Map<String, Map<String, Object>> agentSpecificUsers)
    {
        /*
         * Get all the agents to which a test case has been deployed.
         */

        final Map<String, Set<String>> agentsPerTestCase = new HashMap<String, Set<String>>();
        for (final String agentID : deployment.getAgentIDs())
        {
            for (final TestUserConfiguration user : deployment.getUserList(agentID))
            {
                final String testCaseName = user.getUserName();
                Set<String> mappedAgentIDs = agentsPerTestCase.get(testCaseName);
                if (mappedAgentIDs == null)
                {
                    mappedAgentIDs = new HashSet<String>();
                    agentsPerTestCase.put(testCaseName, mappedAgentIDs);
                }
                mappedAgentIDs.add(agentID);
            }
        }

        /*
         * Now set agent index/count, agent weights and deployed users.
         */

        for (final Entry<String, Set<String>> entry : agentsPerTestCase.entrySet())
        {
            final String testCaseName = entry.getKey();
            final List<String> agentsRunningTest = new ArrayList<>(entry.getValue());

            // sort agent IDs in a special way (#2973)
            agentsRunningTest.sort(agentIdComparator);

            /*
             * Get agent weights and deployed users.
             */

            final int agentCount = agentsRunningTest.size();
            final double[] weightFunction = new double[agentCount];

            int i = 0;
            for (final String agentID : agentsRunningTest)
            {
                weightFunction[i] = agentWeights.get(agentID);
                ++i;
            }

            /*
             * Do assignment now.
             */

            int agentIndex = 0;
            for (final String agentID : agentsRunningTest)
            {
                for (final TestUserConfiguration user : deployment.getUserList(agentID))
                {
                    if (user.getUserName().equals(testCaseName))
                    {
                        user.setWeightFunction(weightFunction);
                        user.setAgentIndex(agentIndex);
                        user.setUsers((int[][]) agentSpecificUsers.get(agentID).get(testCaseName));
                    }
                }

                agentIndex++;
            }
        }
    }

    /**
     * Computes the agent-specific user functions.
     * 
     * @param gUserFunc
     *            the global user function
     * @param deploymentPlan
     *            the deployment plan that holds the number of deployed users per user-type and agent
     * @return user-functions per user-type and agent (agentID =&gt; { user-type =&gt; user-function })
     */
    private Map<String, Map<String, Object>> computeAgentSpecificUsers(final GlobalUserFunction gUserFunc,
                                                                       final Map<String, Map<String, Integer>> deploymentPlan)
    {
        // user types in same order as added to schedule
        final List<String> userTypes = gUserFunc.userTypes;

        /** agentID : {userType : userFunction} */
        final Map<String, Map<String, Object>> agentSpecificUsers = new HashMap<String, Map<String, Object>>();

        /*
         * Construct priority queue and fill agent specific user function map
         */

        final TreeSet<ControllerEntry> controllers = new TreeSet<ControllerEntry>();
        for (final AgentController ac : agentControllers.values())
        {
            controllers.add(new ControllerEntry(ac.getWeight(), ac.getAgentIDs(), ac.runsClientPerformanceTests()));
            for (final String agentID : ac.getAgentIDs())
            {
                if (!agentSpecificUsers.containsKey(agentID))
                {
                    final Map<String, Object> agentUsers = new HashMap<String, Object>();
                    agentSpecificUsers.put(agentID, agentUsers);
                }
            }
        }

        final Map<String, Integer> lastUsers = new HashMap<String, Integer>();

        // loop through all available sampling points (of all user functions)
        for (final int time : gUserFunc.getSamplingPoints())
        {
            /** UserType : #Users */
            final Map<String, Double> curUsers = gUserFunc.getUsersAt(time);

            // loop through all user-types
            for (final String userType : userTypes)
            {
                // tell all agent controller which user type has to change
                // and make sure that set of controllers is sorted
                final ArrayList<ControllerEntry> dummy = new ArrayList<ControllerEntry>();
                ControllerEntry controller = null;
                while ((controller = controllers.pollFirst()) != null)
                {
                    controller._currentUser = userType;
                    dummy.add(controller);
                }
                controllers.addAll(dummy);

                final double nbUsers = curUsers.get(userType);
                final int last = zeroIfNullElseIntValue(lastUsers.get(userType));

                final int newUsers = nbUsers > last ? (int) Math.floor(nbUsers) : (int) Math.ceil(nbUsers);
                final int userDiff = newUsers - last;

                lastUsers.put(userType, newUsers);

                final ArrayList<ControllerEntry> workList = new ArrayList<ControllerEntry>();

                if (userDiff < 0)
                {
                    for (int i = userDiff; i < 0; i++)
                    {
                        ControllerEntry entry = null;
                        AgentEntry agent = null;

                        while (true)
                        {
                            entry = controllers.pollLast();
                            if (entry == null)
                            {
                                throw new RuntimeException("No controller available");
                            }

                            agent = entry.getHeaviestAgent(userType);
                            if (agent == null)
                            {
                                workList.add(entry);
                            }
                            else
                            {
                                break;
                            }
                        }

                        entry.decUsers(userType, agent);
                        controllers.add(entry);

                        // get user function map of the agent
                        final Map<String, Object> agentUsers = agentSpecificUsers.get(agent.agentID);

                        // get agent-specific user function for this user-type
                        int[][] testUsersOfAgent = (int[][]) agentUsers.get(userType);
                        if (testUsersOfAgent == null) // no user function available -> create new one
                        {
                            if (time == 0)
                            {
                                testUsersOfAgent = new int[1][2];
                            }
                            else
                            {
                                testUsersOfAgent = new int[2][2];
                                testUsersOfAgent[0][0] = testUsersOfAgent[0][1] = 0;
                            }
                            agentUsers.put(userType, testUsersOfAgent);
                        }
                        else if (testUsersOfAgent[testUsersOfAgent.length - 1][0] != time) // user function found
                                                                                           // and last
                                                                                           // time entry is not same
                                                                                           // as this
                                                                                           // sampling point
                        {
                            final int[][] tmp = new int[testUsersOfAgent.length + 1][2];
                            System.arraycopy(testUsersOfAgent, 0, tmp, 0, testUsersOfAgent.length);

                            testUsersOfAgent = tmp;
                            agentUsers.put(userType, testUsersOfAgent);
                        }

                        testUsersOfAgent[testUsersOfAgent.length - 1][0] = time;
                        testUsersOfAgent[testUsersOfAgent.length - 1][1] = agent.users.get(userType);

                    }
                }
                else
                {
                    for (int i = 0; i < userDiff; i++)
                    {
                        ControllerEntry entry = null;
                        AgentEntry agent = null;

                        while (true)
                        {
                            entry = controllers.pollFirst();
                            if (entry == null)
                            {
                                throw new RuntimeException("No controller available");
                            }

                            agent = entry.getLightestAgent(userType, deploymentPlan);
                            if (agent != null)
                            {
                                break;
                            }
                            else
                            {
                                workList.add(entry);
                            }
                        }

                        entry.incUsers(userType, agent);
                        controllers.add(entry);

                        // get user function map of the agent
                        final Map<String, Object> agentUsers = agentSpecificUsers.get(agent.agentID);

                        // get agent-specific user function for this user-type
                        int[][] testUsersOfAgent = (int[][]) agentUsers.get(userType);
                        if (testUsersOfAgent == null) // no user function available -> create new one
                        {
                            if (time == 0)
                            {
                                testUsersOfAgent = new int[1][2];
                            }
                            else
                            {
                                testUsersOfAgent = new int[2][2];
                                testUsersOfAgent[0][0] = testUsersOfAgent[0][1] = 0;
                            }
                            agentUsers.put(userType, testUsersOfAgent);
                        }
                        else if (testUsersOfAgent[testUsersOfAgent.length - 1][0] != time) // user function found
                                                                                           // and last
                                                                                           // time entry is not same
                                                                                           // as this
                                                                                           // sampling point
                        {
                            final int[][] tmp = new int[testUsersOfAgent.length + 1][2];
                            System.arraycopy(testUsersOfAgent, 0, tmp, 0, testUsersOfAgent.length);

                            testUsersOfAgent = tmp;
                            agentUsers.put(userType, testUsersOfAgent);
                        }

                        testUsersOfAgent[testUsersOfAgent.length - 1][0] = time;
                        testUsersOfAgent[testUsersOfAgent.length - 1][1] = agent.users.get(userType);

                    }
                }

                controllers.addAll(workList);
            }

        }

        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            final StringBuilder sb = new StringBuilder();
            for (final Map.Entry<String, Map<String, Object>> e : agentSpecificUsers.entrySet())
            {
                sb.append("agent : ").append(e.getKey());
                for (final Map.Entry<String, Object> e2 : e.getValue().entrySet())
                {
                    sb.append("{ testCase : ").append(e2.getKey());
                    final int[][] users = (int[][]) e2.getValue();
                    for (int i = 0; i < users.length; i++)
                    {
                        if (i > 0)
                        {
                            sb.append(",");
                        }
                        sb.append("[").append(users[i][0]).append(",").append(users[i][1]).append("]");
                    }
                    sb.append(" } ");
                }
                sb.append("\n");
            }

            if (XltLogger.runTimeLogger.isInfoEnabled())
            {
                XltLogger.runTimeLogger.info("agentSpecificUsers:\n" + sb.toString());
            }
        }

        return agentSpecificUsers;
    }

    private static class AgentEntry implements Comparable<AgentEntry>
    {
        private final String agentID;

        private final Map<String, Integer> users = new HashMap<String, Integer>();

        private int userCount;

        private AgentEntry(final String agentID)
        {
            this.agentID = agentID;
        }

        /**
         * Increments the number of users.
         */
        private void inc(final String userType)
        {
            final int curUsers = zeroIfNullElseIntValue(users.get(userType));
            users.put(userType, new Integer(curUsers + 1));
            ++userCount;
        }

        private void dec(final String userType)
        {
            final int curUsers = zeroIfNullElseIntValue(users.get(userType));
            if (curUsers == 0)
            {
                throw new RuntimeException("");
            }

            users.put(userType, new Integer(curUsers - 1));
            --userCount;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(final AgentEntry o)
        {
            return new Integer(userCount).compareTo(o.userCount);
        }

    }

    private static int INSTANCE_NO = 0;

    private static class ControllerEntry implements Comparable<ControllerEntry>
    {
        private final int instanceNo = INSTANCE_NO++;

        private int users;

        private final double weight;

        private final List<AgentEntry> agents;

        private final boolean runsCPTests;

        private String _currentUser = XltConstants.EMPTYSTRING;

        // Map that handles the count of different user types at this controller
        // user type : count
        private final Map<String, Integer> countOfEachUserType = new HashMap<String, Integer>();

        private ControllerEntry(final double weight, final Iterable<String> agentIDs, final boolean runsCPTests)
        {
            this.weight = weight;
            agents = new ArrayList<AgentEntry>();
            for (final String agentID : agentIDs)
            {
                agents.add(new AgentEntry(agentID));
            }
            this.runsCPTests = runsCPTests;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(final ControllerEntry o)
        {
            int result = boolCompare(runsCPTests, o.runsCPTests);
            if (result == 0)
            {
                result = getWeightedUsers().compareTo(o.getWeightedUsers());
                if (result == 0)
                {
                    result = Double.compare(o.weight, weight);
                    if (result == 0)
                    {
                        final Integer thisUsers = countOfEachUserType.get(_currentUser);
                        final Integer otherUsers = o.countOfEachUserType.get(_currentUser);

                        if (thisUsers != null && otherUsers != null)
                        {
                            result = thisUsers.compareTo(otherUsers);
                            if (result == 0)
                            {
                                result = new Integer(instanceNo).compareTo(o.instanceNo);
                            }
                        }
                        else
                        {
                            result = new Integer(instanceNo).compareTo(o.instanceNo);
                        }
                    }
                }
            }
            return result;
        }

        private Double getWeightedUsers()
        {
            return (users) / weight;
        }

        private void incUsers(final String userType, final AgentEntry aEntry)
        {
            aEntry.inc(userType);
            ++users;
            countOfEachUserType.put(userType, zeroIfNullElseIntValue(countOfEachUserType.get(userType)) + 1);
        }

        private void decUsers(final String userType, final AgentEntry aEntry)
        {
            aEntry.dec(userType);
            --users;
            countOfEachUserType.put(userType, countOfEachUserType.get(userType) - 1);
        }

        private AgentEntry getLightestAgent()
        {
            AgentEntry agent = null;
            for (final AgentEntry aEntry : agents)
            {
                if (agent == null || agent.userCount > aEntry.userCount)
                {
                    agent = aEntry;
                }
            }

            return agent;
        }

        private AgentEntry getLightestAgent(final String userType, final Map<String, Map<String, Integer>> deploymentPlan)
        {
            final List<AgentEntry> agentsSortedByUserCount = new ArrayList<AgentEntry>(agents);
            Collections.sort(agentsSortedByUserCount);

            AgentEntry agent = null;
            for (final AgentEntry aEntry : agentsSortedByUserCount)
            {
                if (agent != null && agent.userCount < aEntry.userCount)
                {
                    break;
                }

                final int aEntryUsers = zeroIfNullElseIntValue(aEntry.users.get(userType));
                final Map<String, Integer> assignedUsers = deploymentPlan.get(aEntry.agentID);
                final int maxUsers = assignedUsers == null ? 0 : zeroIfNullElseIntValue(assignedUsers.get(userType));
                if (aEntryUsers < maxUsers)
                {
                    if (agent == null || aEntryUsers < zeroIfNullElseIntValue(agent.users.get(userType)))
                    {
                        agent = aEntry;
                    }
                }
            }

            return agent;
        }

        private AgentEntry getHeaviestAgent(final String userType)
        {
            final List<AgentEntry> agentsSortedByUserCount = new ArrayList<AgentEntry>(agents);
            Collections.sort(agentsSortedByUserCount);

            AgentEntry agent = null;
            final int agentSize = agentsSortedByUserCount.size();
            for (int i = agentSize - 1; i >= 0; i--)
            {
                final AgentEntry aEntry = agentsSortedByUserCount.get(i);
                if (agent != null && agent.userCount > aEntry.userCount)
                {
                    break;
                }

                final int aEntryUsers = zeroIfNullElseIntValue(aEntry.users.get(userType));
                if (aEntryUsers > 0)
                {
                    if (agent == null || aEntryUsers > zeroIfNullElseIntValue(agent.users.get(userType)))
                    {
                        agent = aEntry;
                    }
                }
            }

            return agent;
        }
    }

    /**
     * Global user function used to compute agent-specific user functions.
     */
    private static class GlobalUserFunction
    {
        private final HashMap<String, CompositeFunction> userFunctions = new HashMap<String, CompositeFunction>();

        private final LinkedList<String> userTypes = new LinkedList<String>();

        private final TreeSet<Integer> samplings = new TreeSet<Integer>();

        private void register(final String userType, final int[][] userCount)
        {
            final CompositeFunction func = new CompositeFunction(userCount);
            userFunctions.put(userType, func);
            userTypes.add(userType);
            addSamplings(userCount, func);
        }

        private int[] getSamplingPoints()
        {
            final int[] sPoints = new int[2 * samplings.size()];
            int i = 0;
            for (final Iterator<Integer> it = samplings.iterator(); it.hasNext();)
            {
                final int si = it.next();
                if (isSpecialPoint(si))
                {
                    if (i != 0 && sPoints[i - 1] != si - 1)
                    {
                        sPoints[i++] = si - 1;
                    }
                }
                sPoints[i++] = si;
            }

            final int[] iArr = new int[i];
            System.arraycopy(sPoints, 0, iArr, 0, i);

            return iArr;
        }

        private boolean isSpecialPoint(final int x)
        {
            for (final CompositeFunction cf : userFunctions.values())
            {
                if (cf.isSpecialPoint(x))
                {
                    return true;
                }
            }

            return false;
        }

        private Map<String, Double> getUsersAt(final int time)
        {
            final HashMap<String, Double> users = new HashMap<String, Double>();
            for (final String userType : userTypes)
            {
                final CompositeFunction function = userFunctions.get(userType);
                users.put(userType, function.calculateY(time));
            }

            return users;
        }

        private void addSamplings(final int[][] userCount, final CompositeFunction func)
        {
            final int start = userCount[0][0];
            final int end = userCount[userCount.length - 1][0];

            int last = 0;
            for (int time = start; time <= end; time++)
            {
                final double users = func.calculateY(time);
                final int totalUsers = users > last ? (int) Math.floor(users) : (int) Math.ceil(users);

                if (last != totalUsers)
                {
                    samplings.add(time);
                }

                last = totalUsers;
            }
        }
    }

    private static int zeroIfNullElseIntValue(final Integer theInteger)
    {
        return theInteger == null ? 0 : theInteger.intValue();
    }

    private static int boolCompare(final boolean aBoolean, final boolean bBoolean)
    {
        int result;
        if (aBoolean == bBoolean)
        {
            result = 0;
        }
        else if (bBoolean)
        {
            result = -1;
        }
        else
        {
            result = 1;
        }
        return result;
    }
}
