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
            useMasterControllerTime = getBooleanProperty(PROP_USE_MASTER_CONTROLLER_TIME, false);

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
