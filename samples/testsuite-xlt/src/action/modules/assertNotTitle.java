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
import action.modules.assertNotTitle_actions.assertNotTitleAction;
import action.modules.assertNotTitle_actions.substring;
import action.modules.assertNotTitle_actions.special;
import action.modules.assertNotTitle_actions.pageWithEmptyTitle;
import action.modules.assertNotTitle_actions.pageWithNoTitle;

/**
 * TODO: Add class description
 */
public class assertNotTitle extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public assertNotTitle()
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

        lastAction = new assertNotTitleAction(lastAction);
        lastAction.run();

        lastAction = new substring(lastAction);
        lastAction.run();

        lastAction = new special(lastAction);
        lastAction.run();

        lastAction = new pageWithEmptyTitle(lastAction);
        lastAction.run();

        lastAction = new pageWithNoTitle(lastAction);
        lastAction.run();


        return lastAction;
    }
}