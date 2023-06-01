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

import action.modules.VisibleElementFinder_Anchor;
import action.modules.Open_ExamplePage;
import action.modules.VisibleElementFinder_actions.CheckAction0;
import action.modules.VisibleElementFinder_actions.UncheckAction0;
import action.modules.VisibleElementFinder_actions.TypeAction;
import action.modules.VisibleElementFinder_actions.SelectAction;
import action.modules.VisibleElementFinder_actions.RemoveSelectionAction;
import action.modules.VisibleElementFinder_actions.SelectAction0;

/**
 * TODO: Add class description
 */
public class VisibleElementFinder extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public VisibleElementFinder()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        final VisibleElementFinder_Anchor visibleElementFinder_Anchor = new VisibleElementFinder_Anchor("name=in_visible_anchor", "iframe 1");
        lastAction = visibleElementFinder_Anchor.run(lastAction);

        final VisibleElementFinder_Anchor visibleElementFinder_Anchor0 = new VisibleElementFinder_Anchor("link=in_visible_anchor", "iframe 2");
        lastAction = visibleElementFinder_Anchor0.run(lastAction);

        final VisibleElementFinder_Anchor visibleElementFinder_Anchor1 = new VisibleElementFinder_Anchor("xpath=id('in_visible_anchor')/div/a", "iframe 1");
        lastAction = visibleElementFinder_Anchor1.run(lastAction);

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new CheckAction0(lastAction);
        lastAction.run();

        lastAction = new UncheckAction0(lastAction);
        lastAction.run();

        lastAction = new TypeAction(lastAction);
        lastAction.run();

        lastAction = new SelectAction(lastAction);
        lastAction.run();

        lastAction = new RemoveSelectionAction(lastAction);
        lastAction.run();

        lastAction = new SelectAction0(lastAction);
        lastAction.run();


        return lastAction;
    }
}