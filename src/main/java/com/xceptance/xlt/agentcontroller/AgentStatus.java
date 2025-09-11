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
package com.xceptance.xlt.agentcontroller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated To be removed in XLT 8.0.0.
 * @see AgentControllerStatus
 * @see AgentStatusInfo
 * @see ScenarioStatus
 */
public class AgentStatus implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final List<TestUserStatus> allStats = new ArrayList<TestUserStatus>();

    private String agentID;

    private String hostName;

    private int errorExitCode;

    /**
     * @return the agentName
     */

    public String getAgentID()
    {
        return agentID;
    }

    /**
     * @param agentName
     *            the agentName to set
     */

    public void setAgentID(final String agentID)
    {
        this.agentID = agentID;
    }

    /**
     * @return the hostName
     */

    public String getHostName()
    {
        return hostName;
    }

    /**
     * @param hostName
     *            the hostName to set
     */

    public void setHostName(final String hostName)
    {
        this.hostName = hostName;
    }

    public void addTestUserStatus(final TestUserStatus stats)
    {
        allStats.add(stats);
    }

    public List<TestUserStatus> getTestUserStatusList()
    {
        return allStats;
    }

    public void setErrorExitCode(final int exitCode)
    {
        errorExitCode = exitCode;
    }

    public int getErrorExitCode()
    {
        return errorExitCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();

        buf.append(agentID).append('@').append(hostName).append('\n');

        for (final TestUserStatus stats : allStats)
        {
            buf.append("- ").append(stats).append('\n');
        }

        return buf.toString();
    }

}
