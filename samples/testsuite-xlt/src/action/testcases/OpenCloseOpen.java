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
package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.testcases.OpenCloseOpen_actions.OpenCloseOpenAction;
import action.testcases.OpenCloseOpen_actions.OpenCloseOpenAction0;
import action.testcases.OpenCloseOpen_actions.OpenCloseOpenAction1;

/**
 * Related to #1728
 Close the last open window/tab and open new page.
 */
public class OpenCloseOpen extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public OpenCloseOpen()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        lastAction = new OpenCloseOpenAction(lastAction, "/testpages/examplePage_1.html");
        lastAction.run();

        lastAction = new OpenCloseOpenAction0(lastAction);
        lastAction.run();

        lastAction = new OpenCloseOpenAction1(lastAction, "/testpages/examplePage_1.html");
        lastAction.run();


    }
}