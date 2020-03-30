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
public class storeAttribute extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public storeAttribute()
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


        //
        // ~~~ simple_inputValue ~~~
        //
        startAction("simple_inputValue");
        storeAttribute("id=in_txt_1@value", "txt_value");
        assertText("id=in_txt_1", resolve("${txt_value}"));
        type("id=in_txt_1", "new_txt_1");
        assertNotText("xpath=//input[@id='in_txt_1']", resolve("${txt_value}"));

        //
        // ~~~ link_reference ~~~
        //
        startAction("link_reference");
        storeAttribute("css=#anc_link1@href", "link_ref");
        assertAttribute("css=#anc_link1@href", resolve("${link_ref}"));

        //
        // ~~~ empty_attribute ~~~
        //
        startAction("empty_attribute");
        storeAttribute("xpath=//select[@id='select_17']/option[2]@value", "option_value");
        assertText("id=in_txt_5", resolve("${option_value}"));

        //
        // ~~~ white_spaces ~~~
        //
        startAction("white_spaces");
        storeAttribute("xpath=//select[@id='select_17']/option[4]@value", "option_value");
        assertValue("xpath=//select[@id='select_17']/option[4]", resolve("${option_value}"));

        //
        // ~~~ specialChar_crossCheck ~~~
        //
        startAction("specialChar_crossCheck");
        storeAttribute("xpath=//*[@id='select_17']/option[6]@value", "option_value");
        assertAttribute("xpath=//*[@id='select_17']/option[6]@title", resolve("${option_value}"));
        storeAttribute("xpath=//*[@id='select_17']/option[6]@title", "option_title");
        assertValue("xpath=//*[@id='select_17']/option[6]", resolve("${option_title}"));

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