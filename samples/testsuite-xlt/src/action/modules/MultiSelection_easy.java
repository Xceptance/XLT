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

import action.modules.MultiSelection_easy_actions.lettersOnly_add;
import action.modules.MultiSelection_easy_actions.lettersOnly_remove;
import action.modules.MultiSelection_easy_actions.withWhitespace_add;
import action.modules.MultiSelection_easy_actions.withWhitespace_remove;
import action.modules.MultiSelection_easy_actions.doubleSelect;
import action.modules.MultiSelection_easy_actions.doubleSelect_cleanup;
import action.modules.MultiSelection_easy_actions.removeUnselected;

/**
 * TODO: Add class description
 */
public class MultiSelection_easy extends AbstractHtmlUnitActionsModule
{

    /**
     * The 'optionLocator' parameter.
     */
    private final String optionLocator;


    /**
     * Constructor.
     * @param optionLocator The 'optionLocator' parameter.
     */
    public MultiSelection_easy(final String optionLocator)
    {
        this.optionLocator = optionLocator;
    }


    /**
     * @{inheritDoc}
     */
    protected AbstractHtmlPageAction execute(final AbstractHtmlPageAction prevAction) throws Throwable
    {
        AbstractHtmlPageAction lastAction = prevAction;
        lastAction = new lettersOnly_add(lastAction, optionLocator);
        lastAction.run();

        lastAction = new lettersOnly_remove(lastAction, optionLocator);
        lastAction.run();

        lastAction = new withWhitespace_add(lastAction, optionLocator);
        lastAction.run();

        lastAction = new withWhitespace_remove(lastAction, optionLocator);
        lastAction.run();

        lastAction = new doubleSelect(lastAction, optionLocator);
        lastAction.run();

        lastAction = new doubleSelect_cleanup(lastAction, optionLocator);
        lastAction.run();

        lastAction = new removeUnselected(lastAction, optionLocator);
        lastAction.run();


        return lastAction;
    }
}