package com.xceptance.xlt.agentcontroller;

/**
 */

public interface Agent
{
    public boolean isRunning();

    public void stop();

    public AgentStatus getStatus();

    /**
     * Sets the agent's runtime status.
     * 
     * @param status
     *            the new status
     */
    public void setStatus(AgentStatus status);
}
