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

import java.io.Serializable;

import com.xceptance.common.util.ProductInformation;

/**
 * The {@link AgentControllerSystemInfo} contains information of the agent controller's XLT and Java version.
 * 
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class AgentControllerSystemInfo implements Serializable
{
    private static final long serialVersionUID = -2703599229078195365L;

    /**
     * The agent controller's operation system information string (architecture, name, version).
     */
    private final String OS_INFO = System.getProperty("os.name") + ", " + System.getProperty("os.version") + ", " +
                                   System.getProperty("os.arch");

    /**
     * The agent controller's XLT version.
     */
    private final String XLT_VERSION = ProductInformation.getProductInformation().getCondensedProductIdentifier();

    /**
     * The agent controller's Java version.
     */
    private final String JAVA_VERSION = System.getProperty("java.version") + " " + System.getProperty("java.vendor");

    /**
     * The agent controller's system time.
     */
    private long time;

    /**
     * The agent controller's status.
     */
    private String status;

    /**
     * The agent controller's XLT version information.
     * 
     * @return the agent controller's XLT version information
     */
    public String getXltVersion()
    {
        return XLT_VERSION;
    }

    /**
     * The agent controller's Java version information.
     * 
     * @return the agent controller's Java version information
     */
    public String getJavaVersion()
    {
        return JAVA_VERSION;
    }

    /**
     * The agent controller's operation system information string (architecture, name, version).
     * 
     * @return the agent controller's operation system information string (architecture, name, version)
     */
    public String getOsInfo()
    {
        return OS_INFO;
    }

    /**
     * The agent controller's system time.
     * 
     * @return the agent controller's system time
     */
    public long getTime()
    {
        return time;
    }

    /**
     * Set the agent controller's system time.
     * 
     * @param time
     *            the agent controller's system time
     */
    public void setTime(final long time)
    {
        this.time = time;
    }

    /**
     * The agent controller's status.
     * 
     * @return the agent controller's status
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * Set the agent controller's status.
     * 
     * @param status
     *            the agent controller's status
     */
    public void setStatus(final String status)
    {
        this.status = status;
    }
}
