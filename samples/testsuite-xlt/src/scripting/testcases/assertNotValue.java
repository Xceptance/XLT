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
public class assertNotValue extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public assertNotValue()
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

        // empty textarea
        assertNotValue("id=in_ta_1", "regexp:.+");
        // filled textarea
        assertNotValue("id=in_ta_2", "");
        // input with set value
        assertNotValue("id=in_txt_1", "");
        // input with no value
        assertNotValue("id=in_txt_5", "regexp:.+");
        // input with empty value
        assertNotValue("id=in_txt_13", "regexp:.+");
        // hidden input
        assertNotValue("id=invisible_hidden_input", "");
        // hidden input
        assertNotValue("id=invisible_hidden_input", "");
        // display:none
        assertNotValue("id=invisible_display_ancestor", "");
        // visibility:invisible
        assertNotValue("id=invisible_visibility_style_ancestor", "");

    }

}