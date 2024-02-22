/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
public class Texttransform extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public Texttransform()
    {
        super("http://localhost:8080/");
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

        click("link=Text Transform");
        assertText("//*[@id='text-transform']/p[contains(@class,'upcase')][1]", "THIS TEXT SHOULD BE DISPLAYED IN CAPITAL LETTERS.");
        assertText("//*[@id='text-transform']/p[contains(@class,'locase')][1]", "this text should be displayed in small letters.");
        assertText("//*[@id='text-transform']/p[contains(@class,'capital')][1]", "This Text Should Be Displayed In Capitalized Form.");
        assertText("id=text-transform", "THIS TEXT SHOULD BE DISPLAYED IN CAPITAL LETTERS. this text should be displayed in small letters. This Text Should Be Displayed In Capitalized Form.");
        assertNotText("xpath=id('text-transform')/p[@class='upcase']", "This text should be displayed in capital letters.");
        assertNotText("xpath=id('text-transform')/p[@class='locase']", "THIS TEXT SHOULD BE DISPLAYED IN SMALL LETTERS.");
        assertNotText("xpath=id('text-transform')/p[@class='capital']", "this text should be displayed in capitalized form.");

    }

}