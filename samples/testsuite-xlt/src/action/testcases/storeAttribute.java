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

import action.modules.Open_ExamplePage;
import action.testcases.storeAttribute_actions.simple_inputValue;
import action.testcases.storeAttribute_actions.link_reference;
import action.testcases.storeAttribute_actions.empty_attribute;
import action.testcases.storeAttribute_actions.white_spaces;
import action.testcases.storeAttribute_actions.specialChar_crossCheck;

/**
 * TODO: Add class description
 */
public class storeAttribute extends AbstractHtmlUnitScriptTestCase
{

    /**
     * Constructor.
     */
    public storeAttribute()
    {
        super("http://localhost:8080");
    }

    @Test
    public void test() throws Throwable
    {
        AbstractHtmlPageAction lastAction = null;

        final Open_ExamplePage open_ExamplePage = new Open_ExamplePage();
        lastAction = open_ExamplePage.run(lastAction);

        lastAction = new simple_inputValue(lastAction);
        lastAction.run();

        lastAction = new link_reference(lastAction);
        lastAction.run();

        lastAction = new empty_attribute(lastAction);
        lastAction.run();

        lastAction = new white_spaces(lastAction);
        lastAction.run();

        lastAction = new specialChar_crossCheck(lastAction);
        lastAction.run();


    }
}