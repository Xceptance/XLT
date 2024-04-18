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
package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Open_ExamplePage;
import action.modules.MultiSelection_byIndex_actions.add_specialChars_byIndex;
import action.modules.MultiSelection_byIndex_actions.remove_specialChars_byIndex;
import action.modules.MultiSelection_byIndex_actions.double_select_byIndex;
import action.modules.MultiSelection_byIndex_actions.remove_unselected_byIndex;

/**
 * TODO: Add class description
 */
public class MultiSelection_byIndex extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public MultiSelection_byIndex()
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

        lastAction = new add_specialChars_byIndex(lastAction);
        lastAction.run();

        lastAction = new remove_specialChars_byIndex(lastAction);
        lastAction.run();

        lastAction = new double_select_byIndex(lastAction);
        lastAction.run();

        lastAction = new remove_unselected_byIndex(lastAction);
        lastAction.run();


        return lastAction;
    }
}