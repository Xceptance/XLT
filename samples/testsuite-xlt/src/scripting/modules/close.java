/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
public class close extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        //
        // ~~~ popup ~~~
        //
        startAction("popup");
        final Open_popup_w2 _open_popup_w2 = new Open_popup_w2();
        _open_popup_w2.execute();

        final SelectWindow_popup_w2 _selectWindow_popup_w2 = new SelectWindow_popup_w2();
        _selectWindow_popup_w2.execute();

        close();
        //
        // ~~~ clean_up ~~~
        //
        startAction("clean_up");
        // necessary to get back focus on main window
        selectWindow("title=example page");

    }
}