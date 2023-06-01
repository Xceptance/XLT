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
package action.modules;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitActionsModule;

import action.modules.Select_byLabel;
import action.modules.Select_byValue;
import action.modules.Select_byIndex;
import action.modules.SelectAndWait;
import action.modules.Open_ExamplePage;
import action.modules.Select_easy;
import action.modules.select_actions.multi_select;
import action.modules.select_actions.locators;

/**
 * TODO: Add class description
 */
public class select extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public select()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        final Select_byLabel select_byLabel = new Select_byLabel();
        lastAction = select_byLabel.run(lastAction);

        final Select_byValue select_byValue = new Select_byValue();
        lastAction = select_byValue.run(lastAction);

        final Select_byIndex select_byIndex = new Select_byIndex();
        lastAction = select_byIndex.run(lastAction);

        final SelectAndWait selectAndWait = new SelectAndWait();
        lastAction = selectAndWait.run(lastAction);

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        final Select_easy select_easy = new Select_easy("id");
        lastAction = select_easy.run(lastAction);

        lastAction = new multi_select(lastAction);
        lastAction.run();

        lastAction = new locators(lastAction);
        lastAction.run();


        return lastAction;
    }
}