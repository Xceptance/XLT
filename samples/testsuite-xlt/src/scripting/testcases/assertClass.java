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
package scripting.testcases;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class assertClass extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public assertClass()
    {
        super("http://localhost:8080");
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
        // ~~~ SingleClassToMatch ~~~
        //
        startAction("SingleClassToMatch");
        assertClass("xpath=//div[@id='appear' and contains(@class,'cat')][1]", "cat");
        assertClass("//ol[@id='mainList']/li[4]/div", "cat");
        assertClass("xpath=id('appear')", "cat");
        assertClass("//div[@id='anchor_list']/ol[1]/li[1]", "a");
        //
        // ~~~ MultipleClassesToMatch ~~~
        //
        startAction("MultipleClassesToMatch");
        assertClass("id=common_confirmation_area", "confirmation_area common_confirmation_area");
        assertClass("dom=document.getElementById('common_confirmation_area')", "confirmation_area");
        assertClass("css=#common_confirmation_area", "common_confirmation_area");

    }

}