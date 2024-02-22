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

import action.modules.MultiSelection_byLabel;
import action.modules.MultiSelection_byValue;
import action.modules.MultiSelection_byIndex;
import action.modules.Open_ExamplePage;
import action.modules.MultiSelection_easy;
import action.modules.multiSelection_actions.locators;

/**
 * TODO: Add class description
 */
public class multiSelection extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public multiSelection()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        final MultiSelection_byLabel multiSelection_byLabel = new MultiSelection_byLabel();
        lastAction = multiSelection_byLabel.run(lastAction);

        final MultiSelection_byValue multiSelection_byValue = new MultiSelection_byValue();
        lastAction = multiSelection_byValue.run(lastAction);

        final MultiSelection_byIndex multiSelection_byIndex = new MultiSelection_byIndex();
        lastAction = multiSelection_byIndex.run(lastAction);

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        final MultiSelection_easy multiSelection_easy = new MultiSelection_easy("id");
        lastAction = multiSelection_easy.run(lastAction);

        lastAction = new locators(lastAction);
        lastAction.run();


        return lastAction;
    }
}