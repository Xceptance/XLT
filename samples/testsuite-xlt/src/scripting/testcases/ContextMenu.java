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
package scripting.testcases;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class ContextMenu extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public ContextMenu()
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

        click("link=Misc");
        click("id=cc_clear_button");
        contextMenu("id=cm-area");
        assertText("id=cc_mousedown_content", "regexp:2 \\(x: \\d+, y: \\d+\\)");
        storeText("id=cc_mousedown_content", "md");
        assertText("id=cc_contextmenu_content", "regexp:2 \\(x: \\d+, y: \\d+\\)");
        storeText("id=cc_contextmenu_content", "cm");
        assertText("id=cc_mouseup_content", "regexp:2 \\(x: \\d+, y: \\d+\\)");
        storeText("id=cc_mouseup_content", "mu");
        click("id=cc_clear_button");

        // TODO: GH#286
        /*
        contextMenuAt("id=cm-area", "20, 34");
        assertText("id=cc_mousedown_content", "regexp:2 \\(x: \\d+, y: \\d+\\)");
        assertText("id=cc_contextmenu_content", "regexp:2 \\(x: \\d+, y: \\d+\\)");
        assertText("id=cc_mouseup_content", "regexp:2 \\(x: \\d+, y: \\d+\\)");
        assertNotText("id=cc_mousedown_content", "exact:${md}");
        assertNotText("id=cc_contextmenu_content", "exact:${cm}");
        assertNotText("id=cc_mouseup_content", "exact:${mu}");
        */
    }

}