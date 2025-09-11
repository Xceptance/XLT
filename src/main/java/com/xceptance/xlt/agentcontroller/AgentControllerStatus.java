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
import java.util.List;

/**
 * Contains the status data of an agent controller. At the moment, this includes the individual status of each agent and
 * the aggregated scenario status over all agents.
 */
public class AgentControllerStatus implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * The list of agent statuses for an agent controller.
     */
    private final List<AgentStatusInfo> agentStatusList;

    /**
     * The list of scenario statuses for an agent controller.
     */
    private final List<ScenarioStatus> scenarioStatusList;

    public AgentControllerStatus(final List<AgentStatusInfo> agentStatusList, final List<ScenarioStatus> scenarioStatusList)
    {
        this.agentStatusList = agentStatusList;
        this.scenarioStatusList = scenarioStatusList;
    }

    public List<AgentStatusInfo> getAgentStatusList()
    {
        return agentStatusList;
    }

    public List<ScenarioStatus> getScenarioStatusList()
    {
        return scenarioStatusList;
    }
}
