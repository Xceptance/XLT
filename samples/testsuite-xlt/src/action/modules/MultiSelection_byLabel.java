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
import action.modules.MultiSelection_easy;
import action.modules.MultiSelection_byLabel_actions.MultiSelection_byLabelAction;
import action.modules.MultiSelection_specialChars;
import action.modules.MultiSelection_byLabel_actions.MultiSelection_byLabelAction0;
import action.modules.MultiSelection_byLabel_actions.label_whitespace;
import action.modules.MultiSelection_byLabel_actions.label_empty;

/**
 * TODO: Add class description
 */
public class MultiSelection_byLabel extends AbstractHtmlUnitActionsModule
{


    /**
     * Constructor.
     */
    public MultiSelection_byLabel()
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

        final MultiSelection_easy multiSelection_easy = new MultiSelection_easy("label");
        lastAction = multiSelection_easy.run(lastAction);

        lastAction = new MultiSelection_byLabelAction(lastAction);
        lastAction.run();

        final MultiSelection_specialChars multiSelection_specialChars = new MultiSelection_specialChars("label");
        lastAction = multiSelection_specialChars.run(lastAction);

        lastAction = new MultiSelection_byLabelAction0(lastAction);
        lastAction.run();

        lastAction = new label_whitespace(lastAction);
        lastAction.run();

        lastAction = new label_empty(lastAction);
        lastAction.run();


        return lastAction;
    }
}