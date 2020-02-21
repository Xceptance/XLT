package com.xceptance.xlt.engine.scripting;

import java.io.File;
import java.util.List;

/**
 * Represents a test case script read from a script file.
 */
public class TestCase extends CommandScript
{
    /**
     * The base URL for this test case.
     */
    private final String baseUrl;

    private final boolean disabled;

    private final List<ScriptElement> postSteps;

    /**
     * Constructor.
     * 
     * @param scriptFile
     *            the file the script was read from
     * @param scriptElements
     *            the script elements
     * @param baseUrl
     *            the base URL
     */
    public TestCase(final File scriptFile, final List<ScriptElement> scriptElements, final List<ScriptElement> postSteps,
                    final String baseUrl, final String disabledFlag)
    {
        super(scriptFile, scriptElements, null);
        this.postSteps = postSteps;
        this.baseUrl = baseUrl;
        disabled = Boolean.parseBoolean(disabledFlag);
    }

    /**
     * Returns the base URL for this test case.
     * 
     * @return the base URL
     */
    public String getBaseUrl()
    {
        return baseUrl;
    }

    /**
     * Returns whether or not this test case is disabled.
     * 
     * @return <code>true</code> if this test case is disabled, <code>false</code> otherwise
     */
    public boolean isDisabled()
    {
        return disabled;
    }

    /**
     * @return the afterSteps
     */
    public List<ScriptElement> getPostSteps()
    {
        return postSteps;
    }
}
