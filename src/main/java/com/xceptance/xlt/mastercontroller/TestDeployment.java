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
package com.xceptance.xlt.mastercontroller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xceptance.xlt.agentcontroller.AgentController;
import com.xceptance.xlt.agentcontroller.TestUserConfiguration;

/**
 * The TestDeployment represents the assignment of a set of test users to their target agent controllers.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class TestDeployment
{
    private final Map<String, List<TestUserConfiguration>> testDeployment;

    public TestDeployment(final Collection<String> agentIDs)
    {
        testDeployment = new HashMap<String, List<TestUserConfiguration>>();

        for (final String agentID : agentIDs)
        {
            testDeployment.put(agentID, new ArrayList<TestUserConfiguration>());
        }
    }

    public List<TestUserConfiguration> getUserList(final String agentID)
    {
        return testDeployment.get(agentID);
    }

    public Map<String, List<TestUserConfiguration>> getAgentsUserList(final AgentController agentController)
    {
        final Map<String, List<TestUserConfiguration>> result = new HashMap<String, List<TestUserConfiguration>>();
        if (agentController != null)
        {
            final Set<String> agentIDs = agentController.getAgentIDs();
            for (final String agentID : agentIDs)
            {
                final List<TestUserConfiguration> agentUserList = testDeployment.get(agentID);
                result.put(agentID, agentUserList);
            }
        }
        return result;
    }

    public Collection<List<TestUserConfiguration>> getAllUserLists()
    {
        return testDeployment.values();
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();

        for (final String agentID : testDeployment.keySet())
        {
            buf.append(agentID).append('\n');

            final List<TestUserConfiguration> userList = testDeployment.get(agentID);

            for (final TestUserConfiguration user : userList)
            {
                buf.append("- ").append(user).append('\n');
            }
        }

        return buf.toString();
    }

    public Set<String> getAgentIDs()
    {
        return testDeployment.keySet();
    }
}
