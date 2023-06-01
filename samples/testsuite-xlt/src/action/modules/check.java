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

import action.modules.Open_ExamplePage;
import action.modules.check_actions.initial;
import action.modules.check_actions.CheckAction;
import action.modules.check_actions.double_check;
import action.modules.check_actions.UncheckAction;
import action.modules.check_actions.double_uncheck;
import action.modules.check_actions.sequence;
import action.modules.check_actions.radio_button0;
import action.modules.check_actions.different_selectors;
import action.modules.check_actions.different_selectors0;

/**
 * TODO: Add class description
 */
public class check extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public check()
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

        lastAction = new initial(lastAction);
        lastAction.run();

        lastAction = new CheckAction(lastAction);
        lastAction.run();

        lastAction = new double_check(lastAction);
        lastAction.run();

        lastAction = new UncheckAction(lastAction);
        lastAction.run();

        lastAction = new double_uncheck(lastAction);
        lastAction.run();

        lastAction = new sequence(lastAction);
        lastAction.run();

        lastAction = new radio_button0(lastAction);
        lastAction.run();

        lastAction = new different_selectors(lastAction);
        lastAction.run();

        lastAction = new different_selectors0(lastAction);
        lastAction.run();


        return lastAction;
    }
}