package com.xceptance.xlt.util;

import com.xceptance.xlt.agentcontroller.AgentController;

/**
 * Exception thrown on agent controller communication problems or command execution problems. This exception holds a
 * collection of problematic agent controllers and corresponding exceptions (if known).
 * 
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class AgentControllerException extends Exception
{
    private static final long serialVersionUID = 1L;

    /**
     * failed agent controller
     */
    private final FailedAgentControllerCollection failedAgentControllers;

    public AgentControllerException(final AgentController agentController)
    {
        this(agentController, null);
    }

    public AgentControllerException(final AgentController agentController, final Exception e)
    {
        this(new FailedAgentControllerCollection());
        failedAgentControllers.add(agentController, e);
    }

    public AgentControllerException(final FailedAgentControllerCollection failedAgentControllers)
    {
        super();
        this.failedAgentControllers = failedAgentControllers;
    }

    public AgentControllerException(final FailedAgentControllerCollection failedAgentControllers, final String message)
    {
        super(message);
        this.failedAgentControllers = failedAgentControllers;
    }

    public FailedAgentControllerCollection getFailed()
    {
        return failedAgentControllers;
    }
}
