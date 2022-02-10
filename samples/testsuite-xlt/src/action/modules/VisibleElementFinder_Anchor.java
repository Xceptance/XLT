/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
import action.modules.VisibleElementFinder_Anchor_actions.VisibleElementFinder_AnchorAction;

/**
 * TODO: Add class description
 */
public class VisibleElementFinder_Anchor extends AbstractHtmlUnitActionsModule
{

    /**
     * The 'locator' parameter.
     */
    private final String locator;

    /**
     * The 'title' parameter.
     */
    private final String title;


    /**
     * Constructor.
     * @param locator The 'locator' parameter.
     * @param title The 'title' parameter.
     */
    public VisibleElementFinder_Anchor(final String locator, final String title)
    {
        this.locator = locator;
        this.title = title;
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new VisibleElementFinder_AnchorAction(lastAction, locator, title);
        lastAction.run();


        return lastAction;
    }
}