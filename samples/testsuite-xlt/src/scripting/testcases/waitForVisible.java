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
public class waitForVisible extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public waitForVisible()
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

        type("id=timeout_field", "2000");
        click("id=invisible_visibility_show");
        waitForVisible("id=invisible_visibility_ancestor");
        click("id=invisible_visibility_hide");
        waitForNotVisible("id=invisible_visibility_ancestor");
        click("id=invisible_visibility_style_show");
        waitForVisible("id=invisible_visibility_style_ancestor");
        click("id=invisible_visibility_style_hide");
        waitForNotVisible("id=invisible_visibility_style_ancestor");
        click("id=invisible_display_show");
        waitForVisible("id=invisible_display_ancestor");
        click("id=invisible_display_hide");
        waitForNotVisible("id=invisible_display_ancestor");
        click("id=invisible_css_submit_show");
        waitForVisible("id=invisible_css_submit");
        click("id=invisible_css_submit_hide");
        waitForNotVisible("id=invisible_css_submit");
        click("id=invisible_checkbox_byDisplayNone_show");
        waitForVisible("id=invisible_checkbox_byDisplayNone");
        click("id=invisible_checkbox_byDisplayNone_hide");
        waitForNotVisible("id=invisible_checkbox_byDisplayNone");
        click("id=invisible_radio_byDisplayNone_show");
        waitForVisible("id=invisible_radio_byDisplayNone");
        click("id=invisible_radio_byDisplayNone_hide");
        waitForNotVisible("id=invisible_radio_byDisplayNone");

    }

}