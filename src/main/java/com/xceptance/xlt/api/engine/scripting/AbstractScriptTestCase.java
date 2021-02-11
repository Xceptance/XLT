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
package com.xceptance.xlt.api.engine.scripting;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.api.tests.AbstractWebDriverTestCase;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.scripting.TestContext;
import com.xceptance.xlt.engine.scripting.XlteniumScriptInterpreter;
import com.xceptance.xlt.engine.scripting.junit.ScriptTestCaseRunner;
import com.xceptance.xlt.engine.util.ScriptingUtils;

/**
 * The super class for all script-based test cases. Test case scripts are interpreted and the commands found therein
 * will be executed using a {@link WebDriver} implementation.
 */
@RunWith(ScriptTestCaseRunner.class)
public abstract class AbstractScriptTestCase extends AbstractWebDriverTestCase
{
    /**
     * The default implicit wait time-out (in msecs) to be used when finding elements.
     */
    private static final int DEFAULT_IMPLICIT_WAIT_TIMEOUT = XltProperties.getInstance()
                                                                          .getProperty("com.xceptance.xlt.scripting.defaultImplicitWaitTimeout",
                                                                                       1000);

    /**
     * The name of the test script to use.
     */
    private String scriptName;

    /**
     * Constructor.
     */
    public AbstractScriptTestCase()
    {
        scriptName = ScriptingUtils.getScriptName(getClass());
    }

    /**
     * Returns the base URL to use when running the test script. All relative URLs in the script are resolved against
     * this base URL.
     * 
     * @return the base URL
     */
    public String getBaseUrl()
    {
        return TestContext.getCurrent().getBaseUrl();
    }

    /**
     * Returns the name of the test script to use.
     * 
     * @return the script name
     */
    public String getScriptName()
    {
        return scriptName;
    }

    /**
     * Sets the base URL to use when running the test script. All relative URLs in the script are resolved against this
     * base URL.
     * 
     * @param baseUrl
     *            the base URL
     */
    public void setBaseUrl(final String baseUrl)
    {
        TestContext.getCurrent().setBaseUrl(baseUrl);
    }

    /**
     * Sets the name of the test script to use.
     * 
     * @param scriptName
     *            the script name
     */
    public void setScriptName(final String scriptName)
    {
        this.scriptName = scriptName;
    }

    /**
     * Sets the {@link WebDriver} instance to use for the test.
     * <p>
     * Note that this method also sets the driver's implicit wait time-out to the value defined in the XLT
     * configuration. If you want to run your driver with a different time-out value, set this value <em>after</em>
     * calling this method:
     * 
     * <pre>
     * webDriver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
     * </pre>
     * 
     * Note that when explicitly setting the driver instance via this method, you also have to close it by yourself.
     * 
     * @param webDriver
     *            the web driver to use
     */
    @Override
    public void setWebDriver(final WebDriver webDriver)
    {
        super.setWebDriver(webDriver);

        // set the implicit wait time-out
        webDriver.manage().timeouts().implicitlyWait(DEFAULT_IMPLICIT_WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    /**
     * The one and only test method, which executes the configured script test case.
     * 
     * @throws Exception
     *             if an error occurs when executing the script
     */
    @Test
    public void test() throws Exception
    {
        executeScript(getScriptName());
    }

    /**
     * Executes the test case script with the given name using any previously set {@link WebDriver} instance, test data
     * set and base URL.
     * 
     * @param scriptName
     *            the script name
     * @throws Exception
     *             if an error occurred during script execution
     */
    protected void executeScript(final String scriptName) throws Exception
    {
        ParameterCheckUtils.isNotNullOrEmpty(scriptName, "scriptName");

        // run the test script interpreter
        final XlteniumScriptInterpreter interpreter = new XlteniumScriptInterpreter(getWebDriver());

        try
        {
            interpreter.executeScript(scriptName);
        }
        finally
        {
            interpreter.close();
        }
    }

    /**
     * Performs additional setup tasks for XML-based script test cases. Don't call this method directly, it will be
     * called implicitly by the JUnit framework.
     */
    @Before
    public final void __setUpAbstractScriptTestCase()
    {
        TestContext.getCurrent().pushScope(this);
    }

    /**
     * Performs additional cleanup tasks for XML-based script test cases. Don't call this method directly, it will be
     * called implicitly by the JUnit framework.
     */
    @After
    public final void __cleanUpAbstractScriptTestCase()
    {
        TestContext.getCurrent().shutDown();
    }
}
