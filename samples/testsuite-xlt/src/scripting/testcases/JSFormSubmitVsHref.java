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
public class JSFormSubmitVsHref extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public JSFormSubmitVsHref()
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

        clickAndWait("id=form2_submit_link_1");
        assertText("xpath=/html/body[1]", "This is frame 1*");
        final Open_ExamplePage _open_ExamplePage0 = new Open_ExamplePage();
        _open_ExamplePage0.execute();

        clickAndWait("id=form2_submit_link_2");
        assertText("xpath=/html/body[1]", "This is frame 1*");
        final Open_ExamplePage _open_ExamplePage1 = new Open_ExamplePage();
        _open_ExamplePage1.execute();

        clickAndWait("id=form2_submit_link_3");
        assertText("xpath=/html/body[1]", "This is frame 2*");

    }

}