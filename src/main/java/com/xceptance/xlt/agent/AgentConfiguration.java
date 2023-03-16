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
package com.xceptance.xlt.agent;

import java.io.File;

import com.xceptance.common.util.AbstractConfiguration;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.XltExecutionContext;

/**
 * The AgentConfiguration is the central place where all configuration information of the agent can be retrieved from.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class AgentConfiguration extends AbstractConfiguration
{
    /**
     * XLT property prefix
     */
    private static final String PROP_PREFIX = XltConstants.XLT_PACKAGE_PATH + ".";

    /**
     * XLT property that defines the maximum number of errors
     */
    private static final String PROP_MAX_ERRORS = PROP_PREFIX + "maxErrors";

    /**
     * XLT property that defines whether or not the master controller time should be used
     */
    private static final String PROP_USE_MASTER_CONTROLLER_TIME = PROP_PREFIX + "useMasterControllerTime";

    /**
     * XLT property that defines the location of the agent result directory.
     */
    private static final String PROP_RESULT_DIR = PROP_PREFIX + "result-dir";

    /**
     * maximum number of errors
     */
    private int maxErrors;

    /**
     * results directory
     */
    private File resultsDirectory;

    /**
     * whether or not to use the master controller time
     */
    private boolean useMasterControllerTime;

    /**
     * Creates a new AgentConfiguration object.
     * 
     * @throws RuntimeException
     *             if an error occurs
     */
    public AgentConfiguration()
    {
        try
        {
            final String agentHome = System.getProperty(XltConstants.XLT_PACKAGE_PATH + ".agent.home");
            final File homeDirectory = new File(agentHome);
            XltExecutionContext.getCurrent().setTestSuiteHomeDir(homeDirectory);
            addProperties(XltProperties.getInstance().getProperties());

            maxErrors = getIntProperty(PROP_MAX_ERRORS, 1000);
            useMasterControllerTime = getBooleanProperty(PROP_USE_MASTER_CONTROLLER_TIME, true);

            final String resultDir = getStringProperty(PROP_RESULT_DIR, "results");
            resultsDirectory = new File(resultDir);
            if (!resultsDirectory.isAbsolute())
            {
                resultsDirectory = new File(homeDirectory, resultDir);
            }
        }
        catch (final Exception ex)
        {
            throw new RuntimeException("Failed to read agent configuration", ex);
        }
    }

    /**
     * @return the maxErrors
     */
    public int getMaxErrors()
    {
        return maxErrors;
    }

    /**
     * @return the resultsDirectory
     */
    public File getResultsDirectory()
    {
        return resultsDirectory;
    }

    /**
     * @return the useMasterControllerTime
     */
    public boolean getUseMasterControllerTime()
    {
        return useMasterControllerTime;
    }
}
