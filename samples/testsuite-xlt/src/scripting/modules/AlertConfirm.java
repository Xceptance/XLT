/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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

/**
 * TODO: Add class description
 */
public class AlertConfirm extends AbstractWebDriverModule
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
        // ~~~ navigate ~~~
        //
        startAction("navigate");
        click("link=popup");

        //
        // ~~~ alert ~~~
        //
        startAction("alert");
        click("id=popup_alert");
        assertText("id=cc_misc_head", "misc (popup_alert)");

        //
        // ~~~ confirm-true ~~~
        //
        startAction("confirm_true");
        click("id=popup_confirm");
        assertText("id=cc_misc", "misc (popup_confirm) true");

    }
}
