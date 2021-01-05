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
package com.xceptance.xlt.mastercontroller;

import java.net.URL;

/**
 * The AgentControllerConnectionInfo bundles all information about an agent controller connection.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class AgentControllerConnectionInfo
{
    /**
     * agent controller's URL
     */
    private URL url;

    /**
     * agent controller's name
     */
    private String name;

    /**
     * agent controller's weight
     */
    private int weight;

    /**
     * agent controller's number of agents
     */
    private int numberOfAgents;

    private boolean runsClientPerformanceTests;
    
    /**
     * Returns the URL of the agent controller.
     * 
     * @return the url
     */
    public URL getUrl()
    {
        return url;
    }

    /**
     * Returns the number of agents controlled by this agent controller.
     * 
     * @return the number of agents
     */
    public int getNumberOfAgents()
    {
        return numberOfAgents;
    }

    /**
     * Sets the number of agents controlled by this agent controller.
     * 
     * @param numberOfAgents
     *            the number of agents
     */
    public void setNumberOfAgents(final int numberOfAgents)
    {
        this.numberOfAgents = numberOfAgents;
    }

    /**
     * Sets the new URL of the agent controller.
     * 
     * @param url
     *            the url to set
     */
    public void setUrl(final URL url)
    {
        this.url = url;
    }

    /**
     * Returns the name of the agent controller.
     * 
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the new name of the agent controller.
     * 
     * @param name
     *            the name to set
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * Returns the value of the 'weight' attribute.
     * 
     * @return the value of weight
     */
    public int getWeight()
    {
        return weight;
    }

    /**
     * Sets the new value of the 'weight' attribute.
     * 
     * @param weight
     *            the new weight value
     */
    public void setWeight(final int weight)
    {
        this.weight = weight;
    }

    /**
     * @return the runsClientPerformanceTests
     */
    public final boolean runsClientPerformanceTests()
    {
        return runsClientPerformanceTests;
    }

    /**
     * @param runsClientPerformanceTests the runsClientPerformanceTests to set
     */
    public final void setRunsClientPerformanceTests(boolean runsClientPerformanceTests)
    {
        this.runsClientPerformanceTests = runsClientPerformanceTests;
    }

}
