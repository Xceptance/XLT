/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
