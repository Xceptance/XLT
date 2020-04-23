/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package scripting.testcases;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

/**
 * Related to #1728
 Close the last open window/tab and open new page.
 */
public class OpenCloseOpen extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public OpenCloseOpen()
    {
        super(new XltDriver(true), "http://localhost:8080");
    }


    /**
     * Executes the test.
     *
     * @throws Throwable if anything went wrong
     */
    @Test
    public void test() throws Throwable
    {
        open("/testpages/examplePage_1.html");
        assertTitle("example page");
        close();
        open("/testpages/examplePage_1.html");
        assertTitle("example page");

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