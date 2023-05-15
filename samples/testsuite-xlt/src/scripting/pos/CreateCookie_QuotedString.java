/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package scripting.pos;

import org.junit.After;
import org.junit.Test;
import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.AssertCookie;
import scripting.modules.Open_ExamplePage;


/**
 * 
 */
public class CreateCookie_QuotedString extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public CreateCookie_QuotedString()
    {
        super(new XltDriver(true), "http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
    	final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        deleteCookie("x_qs");
        createCookie("x_qs=\"quoted string\"");
        final AssertCookie _assertCookie = new AssertCookie();
        _assertCookie.execute("x_qs", "\"quoted string\"");

    }

    @After
    public void after()
    {
        getWebDriver().quit();
    }
}