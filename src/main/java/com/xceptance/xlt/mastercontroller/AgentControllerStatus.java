/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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

import java.util.Collections;
import java.util.Set;

import com.xceptance.xlt.agentcontroller.AgentStatus;

/**
 * Agent controller's status query result.
 * 
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class AgentControllerStatus
{
    /**
     * Agent controller's status.
     */
    private final Set<AgentStatus> agentStatuses;

    /**
     * Exception if occurred.
     */
    private Exception exception;

    /**
     * @param status
     *            agent controller's status
     */
    public AgentControllerStatus(final Set<AgentStatus> status)
    {
        this(status, null);
    }

    /**
     * @param exception
     *            exception if occurred
     */
    public AgentControllerStatus(final Exception exception)
    {
        this(null, exception);
    }

    private AgentControllerStatus(final Set<AgentStatus> status, final Exception exception)
    {
        agentStatuses = status;
        this.exception = exception;
    }

    /**
     * Get the agent controller's last known status.
     * 
     * @return the agent controller's last known status
     */
    public Set<AgentStatus> getAgentStatuses()
    {
        return agentStatuses != null ? Collections.unmodifiableSet(agentStatuses) : Collections.<AgentStatus>emptySet();
    }

    /**
     * Get the exception that occurred.
     * 
     * @return the exception that occurred
     */
    public synchronized Exception getException()
    {
        return exception;
    }

    /**
     * Set the exception that occurred during status retrieval attempt.
     * @param e
     *            the exception
     */
    public synchronized void setException(final Exception e)
    {
        this.exception = e;
    }
}
