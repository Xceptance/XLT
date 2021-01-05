/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
import action.modules.mouseEvent_actions.MouseOverAction;
import action.modules.mouseEvent_actions.MouseDownAction;
import action.modules.mouseEvent_actions.MouseUpAction;
import action.modules.mouseEvent_actions.MouseOutAction;
import action.modules.mouseEvent_actions.MouseOverAction0;
import action.modules.mouseEvent_actions.MouseDownAtAction;
import action.modules.mouseEvent_actions.MouseMoveAtAction;
import action.modules.mouseEvent_actions.MouseUpAtAction;
import action.modules.mouseEvent_actions.MouseMoveAction;

/**
 * TODO: Add class description
 */
public class mouseEvent extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public mouseEvent()
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

        lastAction = new MouseOverAction(lastAction);
        lastAction.run();

        lastAction = new MouseDownAction(lastAction);
        lastAction.run();

        lastAction = new MouseUpAction(lastAction);
        lastAction.run();

        lastAction = new MouseOutAction(lastAction);
        lastAction.run();

        lastAction = new MouseOverAction0(lastAction);
        lastAction.run();

        lastAction = new MouseDownAtAction(lastAction);
        lastAction.run();

        lastAction = new MouseMoveAtAction(lastAction);
        lastAction.run();

        lastAction = new MouseUpAtAction(lastAction);
        lastAction.run();

        lastAction = new MouseMoveAction(lastAction);
        lastAction.run();


        return lastAction;
    }
}