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
package test.com.xceptance.xlt.engine.scripting.webdriver;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.engine.util.DefaultWebDriverFactory;

/**
 * Checks that certain commands return immediately when searching for non-existing elements.
 */
@Ignore("To be run manually with a real browser")
public class _2875_ElementIsClickableTest extends AbstractWebDriverScriptTestCase
{

    public _2875_ElementIsClickableTest()
    {
        super(DefaultWebDriverFactory.getWebDriver(), "http://localhost:8080");
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
        //
        // ~~~ OpenStartPage ~~~
        //
        startAction("OpenStartPage");
        open("/testpages/DuckAndCover.html");

        //
        // ~~~ LetMeGoogleThatForYou ~~~
        //
        startAction("LetMeGoogleThatForYou");
        clickAndWait("id=gogo");
        assertTitle("Google*");

    }

    /**
     * Clean up.
     */
    @After
    public void quitDriver()
    {
        // Shutdown WebDriver.
        getWebDriver().quit();
    }

}
