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
package scripting.modules;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;
import scripting.modules.Open_popup_w2;
import scripting.modules.SelectWindow_popup_w2;

/**
 * TODO: Add class description
 */
public class selectWindow extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        final Open_popup_w2 _open_popup_w2 = new Open_popup_w2();
        _open_popup_w2.execute();

        //
        // ~~~ select_popup ~~~
        //
        startAction("select_popup");
        final SelectWindow_popup_w2 _selectWindow_popup_w2 = new SelectWindow_popup_w2();
        _selectWindow_popup_w2.execute();

        //
        // ~~~ toggle_title ~~~
        //
        startAction("toggle_title");
        selectWindow("title=example page");
        assertTitle("example page");
        final SelectWindow_popup_w2 _selectWindow_popup_w20 = new SelectWindow_popup_w2();
        _selectWindow_popup_w20.execute();

        //
        // ~~~ toggle_null ~~~
        //
        startAction("toggle_null");
        selectWindow();
        assertTitle("example page");
        final SelectWindow_popup_w2 _selectWindow_popup_w21 = new SelectWindow_popup_w2();
        _selectWindow_popup_w21.execute();

        //
        // ~~~ toggle_emptyName ~~~
        //
        startAction("toggle_emptyName");
        selectWindow("name=");
        assertTitle("example page");
        final SelectWindow_popup_w2 _selectWindow_popup_w22 = new SelectWindow_popup_w2();
        _selectWindow_popup_w22.execute();

        //
        // ~~~ close_w2 ~~~
        //
        startAction("close_w2");
        close();
        //
        // ~~~ open_popup_w4 ~~~
        //
        startAction("open_popup_w4");
        selectWindow("title=example page");
        click("id=popup_w4");
        waitForPopUp("popup_w4");
        //
        // ~~~ toggle_emptyTitle ~~~
        //
        startAction("toggle_emptyTitle");
        selectWindow("title=");
        assertTextPresent("*This is frame 2*");
        selectWindow("title=example page");
        assertTitle("example page");
        selectWindow("title=");
        assertTextPresent("*This is frame 2*");
        //
        // ~~~ close_w4 ~~~
        //
        startAction("close_w4");
        close();
        //
        // ~~~ clean_up ~~~
        //
        startAction("clean_up");
        // necessary to get back focus on main window
        selectWindow("title=example page");

    }
}