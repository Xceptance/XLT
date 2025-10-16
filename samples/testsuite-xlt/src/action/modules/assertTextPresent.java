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
import action.modules.assertTextPresent_actions.link0;
import action.modules.assertTextPresent_actions.whitespaces0;
import action.modules.assertTextPresent_actions.glob_RegEx0;
import action.modules.assertTextPresent_actions.matchingStrategy;
import action.modules.assertTextPresent_actions.empty0;
import action.modules.assertTextPresent_actions.keyspace0;
import action.modules.assertTextPresent_actions.pangram0;
import action.modules.assertTextPresent_actions.format_bold0;
import action.modules.assertTextPresent_actions.format_underline0;
import action.modules.assertTextPresent_actions.format_italic0;
import action.modules.assertTextPresent_actions.format_mixed0;
import action.modules.assertTextPresent_actions.format_lineBreaks0;
import action.modules.assertTextPresent_actions.format_table0;
import action.modules.assertTextPresent_actions.popup0;
import action.modules.assertTextPresent_actions.iframe11;
import action.modules.assertTextPresent_actions.iframe21;
import action.modules.assertTextPresent_actions.iframe31;

/**
 * TODO: Add class description
 */
public class assertTextPresent extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public assertTextPresent()
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

        lastAction = new link0(lastAction);
        lastAction.run();

        lastAction = new whitespaces0(lastAction);
        lastAction.run();

        lastAction = new glob_RegEx0(lastAction);
        lastAction.run();

        lastAction = new matchingStrategy(lastAction);
        lastAction.run();

        lastAction = new empty0(lastAction);
        lastAction.run();

        lastAction = new keyspace0(lastAction);
        lastAction.run();

        lastAction = new pangram0(lastAction);
        lastAction.run();

        lastAction = new format_bold0(lastAction);
        lastAction.run();

        lastAction = new format_underline0(lastAction);
        lastAction.run();

        lastAction = new format_italic0(lastAction);
        lastAction.run();

        lastAction = new format_mixed0(lastAction);
        lastAction.run();

        lastAction = new format_lineBreaks0(lastAction);
        lastAction.run();

        lastAction = new format_table0(lastAction);
        lastAction.run();

        lastAction = new popup0(lastAction);
        lastAction.run();

        lastAction = new iframe11(lastAction);
        lastAction.run();

        lastAction = new iframe21(lastAction);
        lastAction.run();

        lastAction = new iframe31(lastAction);
        lastAction.run();


        return lastAction;
    }
}