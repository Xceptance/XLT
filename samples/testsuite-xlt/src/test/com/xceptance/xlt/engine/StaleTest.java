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
package test.com.xceptance.xlt.engine;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import scripting.modules.Open_ExamplePage;

/**
 * Running this test you are able to create "StaleElementReferenceException". You have to make sure
 * "com.xceptance.xlt.scripting.commandRetries" in "dev.properties" is set to 0 to see the exception. Maybe you need to
 * run it several times to get the right timing.
 * <p>
 * Related to: XLT Improvement #2507
 * </p>
 */
@Ignore("Need to be run manually")
public class StaleTest extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public StaleTest()
    {
        super(new XltDriver(true), "http://localhost:8080/");
    }

    /**
     * Executes the test.
     *
     * @throws Throwable
     *             if anything went wrong
     */
    @Test
    public void test() throws Throwable
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        // start removing and adding the element by clicking
        click("css=#staleButton");

        // try to touch the element often enough to create a "StaleElementReferenceException"
        for (int i = 0; i < 3333; i++)
        {
            storeText("css=#stale>div", "count");
        }
    }

    /**
     * Clean up.
     */
    @After
    public void after()
    {
        // Shutdown WebDriver.
        getWebDriver().quit();
    }
}
