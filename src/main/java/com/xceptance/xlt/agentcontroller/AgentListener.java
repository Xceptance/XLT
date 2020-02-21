package com.xceptance.xlt.agentcontroller;

/**
 */
public interface AgentListener
{
    /**
     * Report agent stopped.
     * 
     * @param agentID
     */
    public void agentStopped(String agentID);

    /**
     * Report agent stopped with unclear exit code.
     * 
     * @param agentID
     * @param exitCode
     */
    public void agentExitedUnexpectedly(String agentID, int exitCode);
}
