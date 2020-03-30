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
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class CssSelector extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public CssSelector()
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
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        // wildcard
        assertElementPresent("css=*");
        // select by tag name
        assertElementPresent("css=div");
        // select by ID
        assertElementPresent("css=#mainList");
        // select by class
        assertElementPresent("css=.common_confirmation_area");
        // child
        assertElementPresent("css=div li");
        // direct child
        assertElementPresent("css=div > ul");
        // direct child (multiple)
        assertElementPresent("css=#mainList>li>h1");
        // first-child
        assertText("css=#mainList>li:first-child>h1", "configuration");
        // last-child
        assertText("css=#mainList>li:last-child>h1", "Stale");
        // nth-child
        assertText("css=#mainList>li:nth-child(3)>h1", "disappear");
        // select by attribute name
        assertElementPresent("css=select[id]");
        // select by attribute value
        assertElementPresent("css=select[id=select_1]");
        // select element by attribute list value
        assertElementPresent("css=div[class~=common_confirmation_area]");
        // Element with [class~=value]
        assertElementPresent("css=DIV.common_confirmation_area");
        // certain element with certain ID
        assertElementPresent("css=select#select_1");

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