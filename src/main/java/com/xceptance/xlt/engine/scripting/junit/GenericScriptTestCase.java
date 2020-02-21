package com.xceptance.xlt.engine.scripting.junit;

import com.xceptance.xlt.api.engine.scripting.AbstractScriptTestCase;

/**
 * A common test case class, which can be parameterized to run any test script.
 */
public class GenericScriptTestCase extends AbstractScriptTestCase
{
    /**
     * Returns the name of the configured script as the test name. Otherwise all script test cases would be named
     * "GenericScriptTestCase" in the results.
     */
    @Override
    protected String getTestName()
    {
        return getScriptName();
    }
}
