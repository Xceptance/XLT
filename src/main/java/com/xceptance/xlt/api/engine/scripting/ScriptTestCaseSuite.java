package com.xceptance.xlt.api.engine.scripting;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.runner.RunWith;

import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.scripting.junit.ScriptTestCaseSuiteRunner;

/**
 * A special test case class, which acts as a suite of script test cases. Add this test case to your list of JUnit test
 * cases and it will run all test scripts configured in the property
 * "com.xceptance.xlt.api.engine.scripting.ScriptTestCaseSuite.testCases", each possibly multiple if there is an
 * associated data set file with more than one data set (data-driven test). A sample configuration could look like this
 * 
 * <pre>
 * com.xceptance.xlt.api.engine.scripting.ScriptTestCaseSuite.testCases = com.yourcompany.xlt.tests.TLogin Login Logout
 * </pre>
 * 
 * With this configuration, the framework executes the class "com.yourcompany.xlt.tests.TLogin" and the scripts "Login"
 * and "Logout".
 */
@RunWith(ScriptTestCaseSuiteRunner.class)
public class ScriptTestCaseSuite
{
    private static final String PROP_TEST_CASES = ScriptTestCaseSuite.class.getName() + ".testCases";

    public static List<String> getTestCases()
    {
        String scriptNames = XltProperties.getInstance().getProperty(ScriptTestCaseSuite.PROP_TEST_CASES, "");

        // re-construct the fully qualified script names from path-like entries
        scriptNames = scriptNames.replace('/', '.');
        scriptNames = scriptNames.replace('\\', '.');

        final String[] testCaseNames = StringUtils.split(scriptNames, ",; ");

        return Arrays.asList(testCaseNames);
    }
}
