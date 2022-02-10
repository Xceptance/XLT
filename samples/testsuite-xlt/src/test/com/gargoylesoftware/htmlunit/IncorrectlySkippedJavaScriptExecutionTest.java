/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package test.com.gargoylesoftware.htmlunit;

import org.junit.After;
import org.junit.Test;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import com.xceptance.xlt.api.webdriver.XltDriver;

/**
 * Added due to <a href="https://lab.xceptance.de/issues/891">891</a>
 * 
 * @author Sebastian Oerding
 */
public class IncorrectlySkippedJavaScriptExecutionTest extends AbstractWebDriverScriptTestCase
{
    /**
     * We have to give a default public no arg constructor for JUnit as there is none in the super class.
     */
    public IncorrectlySkippedJavaScriptExecutionTest()
    {
        super(new XltDriver());
    }

    /**
     * There was a bug in HtmlUnit that caused JavaScripts not to be executed under specific circumstances (having
     * anchors targeting on the same page and some more). This bug was fixed in HtmlUnit 2.9.
     */
    @Test
    public void test()
    {
        open("http://localhost:8080/testpages/examplePage_1.html");

        click("link=anc_sel1");
        assertText("id=cc_click_content", "anc_sel1");

        click("link=anc_sel2");
        assertText("id=cc_click_content", "anc_sel2");

        click("link=anc_sel3");
        assertText("id=cc_click_content", "anc_sel3");

        click("link=anc_sel4");
        assertText("id=cc_click_content", "anc_sel4");
    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}
