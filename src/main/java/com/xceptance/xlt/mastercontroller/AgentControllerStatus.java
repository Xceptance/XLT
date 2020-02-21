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
