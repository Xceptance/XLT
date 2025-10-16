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
package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.assertNotTextPresent_actions.non_existing0;
import action.modules.assertNotTextPresent_actions.case_insensitive0;
import action.modules.assertNotTextPresent_actions.iframe0;
import action.modules.assertNotTextPresent_actions.subframe0;

/**
 * TODO: Add class description
 */
public class assertNotTextPresent extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public assertNotTextPresent()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new non_existing0(lastAction);
        lastAction.run();

        lastAction = new case_insensitive0(lastAction);
        lastAction.run();

        lastAction = new iframe0(lastAction);
        lastAction.run();

        lastAction = new subframe0(lastAction);
        lastAction.run();


        return lastAction;
    }
}