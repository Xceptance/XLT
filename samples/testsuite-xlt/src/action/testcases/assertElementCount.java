/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
import action.testcases.assertElementCount_actions.assertCount;
import action.testcases.assertElementCount_actions.assertNotCount;
import action.testcases.assertElementCount_actions.assertNotElement;
import action.testcases.assertElementCount_actions.waitFor;
import action.testcases.assertElementCount_actions.waitForNot;

/**
 * TODO: Add class description
 */
public class assertElementCount extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public assertElementCount()
    {
        super("http://localhost:8080/");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new assertCount(lastAction);
        lastAction.run();

        lastAction = new assertNotCount(lastAction);
        lastAction.run();

        lastAction = new assertNotElement(lastAction);
        lastAction.run();

        lastAction = new waitFor(lastAction);
        lastAction.run();

        lastAction = new waitForNot(lastAction);
        lastAction.run();


    }
}