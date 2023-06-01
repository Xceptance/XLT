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
package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.testcases.assertSelected_actions.initial_unselected;
import action.testcases.assertSelected_actions.initial_preselected;
import action.testcases.assertSelected_actions.unselect_preselected;
import action.testcases.assertSelected_actions.SelectAction;
import action.testcases.assertSelected_actions.noValueOption;
import action.testcases.assertSelected_actions.unselect_selected;

/**
 * TODO: Add class description
 */
public class assertSelected extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertSelected()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new initial_unselected(lastAction);
        lastAction.run();

        lastAction = new initial_preselected(lastAction);
        lastAction.run();

        lastAction = new unselect_preselected(lastAction);
        lastAction.run();

        lastAction = new SelectAction(lastAction);
        lastAction.run();

        lastAction = new noValueOption(lastAction);
        lastAction.run();

        lastAction = new unselect_selected(lastAction);
        lastAction.run();


    }
}