/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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

import java.util.List;

import com.xceptance.xlt.agentcontroller.AgentControllerStatus;
import com.xceptance.xlt.agentcontroller.AgentStatusInfo;
import com.xceptance.xlt.agentcontroller.ScenarioStatus;

/**
 * The mastercontroller-side representation of the agent controller status data. Similar to
 * {@link AgentControllerStatus}, but enriched with a potential exception that might have occurred when last requesting
 * the status from the agent controller.
 */
public class AgentControllerStatusInfo extends AgentControllerStatus
{
    /**
     * The exception that may have occurred when last requesting the status from an agent controller.
     */
    private Exception exception;

    public AgentControllerStatusInfo(final AgentControllerStatus agentControllerStatus)
    {
        this(agentControllerStatus.getAgentStatusList(), agentControllerStatus.getScenarioStatusList(), null);
    }

    public AgentControllerStatusInfo(final Exception exception)
    {
        this(null, null, exception);
    }

    private AgentControllerStatusInfo(final List<AgentStatusInfo> status, final List<ScenarioStatus> scenarioStatusList,
                                      final Exception exception)
    {
        super(status, scenarioStatusList);
        this.exception = exception;
    }

    public Exception getException()
    {
        return exception;
    }

    public void setException(final Exception e)
    {
        this.exception = e;
    }
}
