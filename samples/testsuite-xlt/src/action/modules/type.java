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
import action.modules.type_actions.events;
import action.modules.type_actions.input_keyspace_lower;
import action.modules.type_actions.input_keyspace_upper;
import action.modules.type_actions.input_keyspace_altgr;
import action.modules.type_actions.textarea_keypsace;
import action.modules.type_actions.input_keyspace_upper0;
import action.modules.type_actions.input_keyspace_altgr0;
import action.modules.type_actions.clear_input;
import action.modules.type_actions.emptyValueTarget;
import action.modules.type_actions.HTML5inputTypes;
import action.modules.type_actions.strange;

/**
 * TODO: Add class description
 */
public class type extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public type()
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

        lastAction = new events(lastAction);
        lastAction.run();

        lastAction = new input_keyspace_lower(lastAction);
        lastAction.run();

        lastAction = new input_keyspace_upper(lastAction);
        lastAction.run();

        lastAction = new input_keyspace_altgr(lastAction);
        lastAction.run();

        lastAction = new textarea_keypsace(lastAction);
        lastAction.run();

        lastAction = new input_keyspace_upper0(lastAction);
        lastAction.run();

        lastAction = new input_keyspace_altgr0(lastAction);
        lastAction.run();

        lastAction = new clear_input(lastAction);
        lastAction.run();

        lastAction = new emptyValueTarget(lastAction);
        lastAction.run();

        lastAction = new HTML5inputTypes(lastAction);
        lastAction.run();

        lastAction = new strange(lastAction);
        lastAction.run();


        return lastAction;
    }
}