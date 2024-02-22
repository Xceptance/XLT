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
package action.placeholders.injectTestdata;

import org.junit.Test;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptTestCase;

import action.modules.Open_ExamplePage;
import action.placeholders.injectTestdata.TInjectTestData_actions.TInjectTestDataAction;
import action.placeholders.injectTestdata.TInjectTestData_actions.TInjectTestData_0;

/**
 * Inject test data to module that doesn't define the test data itself (no override, just injection)
 */
public class TInjectTestData extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public TInjectTestData()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new TInjectTestDataAction(lastAction);
        lastAction.run();

        lastAction = new TInjectTestData_0(lastAction);
        lastAction.run();


    }
}