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
package action.testcases;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.testcases.JSFormSubmitVsHref_actions.JSFormSubmitVsHrefAction;
import action.testcases.JSFormSubmitVsHref_actions.JSFormSubmitVsHrefAction0;
import action.testcases.JSFormSubmitVsHref_actions.JSFormSubmitVsHrefAction1;

/**
 * TODO: Add class description
 */
public class JSFormSubmitVsHref extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public JSFormSubmitVsHref()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new JSFormSubmitVsHrefAction(lastAction);
        lastAction.run();

        final Open_ExamplePage open_ExamplePage0 = new Open_ExamplePage();
        lastAction = open_ExamplePage0.run(lastAction);

        lastAction = new JSFormSubmitVsHrefAction0(lastAction);
        lastAction.run();

        final Open_ExamplePage open_ExamplePage1 = new Open_ExamplePage();
        lastAction = open_ExamplePage1.run(lastAction);

        lastAction = new JSFormSubmitVsHrefAction1(lastAction);
        lastAction.run();


    }
}