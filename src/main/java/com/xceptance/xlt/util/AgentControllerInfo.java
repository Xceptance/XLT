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
package com.xceptance.xlt.util;

/**
 * The {@link AgentControllerInfo} contains some basic information about the pinged agent controller, the elapsed ping
 * time and the delivered {@link PinkResponse}.
 * 
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class AgentControllerInfo
{
    /**
     * agent controller name
     */
    private String name;

    /**
     * agent controller host name
     */
    private String hostName;

    /**
     * The agent controller system info.
     */
    private AgentControllerSystemInfo agentControllerSystemInfo;

    /**
     * Time difference to master controller. Is only set if an {@link AgentControllerSystemInfo} is present;
     */
    private long timeDifference;

    /**
     * The related exception if the agent controller could not be queried.
     */
    private Exception exception;

    /**
     * The agent controller name.
     * 
     * @return the agent controller name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the agent controller name.
     * 
     * @param name
     *            the agent controller name
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * The agent controller's host name.
     * 
     * @return the agent controller's host name
     */
    public String getHostName()
    {
        return hostName;
    }

    /**
     * Set the agent controller's host name.
     * 
     * @param hostName
     *            the agent controller's host name
     */
    public void setHostName(final String hostName)
    {
        this.hostName = hostName;
    }

    /**
     * The ping response.
     * 
     * @return the agent controller system information or <code>null</code> if there was no response.
     */
    public AgentControllerSystemInfo getAgentControllerSystemInfo()
    {
        return agentControllerSystemInfo;
    }

    /**
     * Set the agent controller system information.
     * 
     * @param agentControllerSystemInfo
     *            the agent controller system information
     */
    public void setAgentControllerSystemInfo(final AgentControllerSystemInfo agentControllerSystemInfo)
    {
        this.agentControllerSystemInfo = agentControllerSystemInfo;
    }

    public void setTimeDifferenceTo(final long referenceTime)
    {
        if (agentControllerSystemInfo != null)
        {
            timeDifference = Math.abs(referenceTime - agentControllerSystemInfo.getTime());
        }
    }

    public long getTimeDifference()
    {
        return timeDifference;
    }

    public void setException(final Exception exception)
    {
        this.exception = exception;
    }

    public Exception getException()
    {
        return exception;
    }
}
