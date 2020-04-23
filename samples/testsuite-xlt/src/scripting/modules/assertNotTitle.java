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
package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class assertNotTitle extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        assertElementPresent("xpath=//title");

        //
        // ~~~ substring ~~~
        //
        startAction("substring");
        assertNotTitle("xample");

        //
        // ~~~ special ~~~
        //
        startAction("special");
        assertNotTitle("xyz");
        assertNotTitle("");
        assertNotTitle("exact:");
        assertNotTitle("glob:");

        //
        // ~~~ pageWithEmptyTitle ~~~
        //
        startAction("pageWithEmptyTitle");
        click("id=title_empty");
        assertElementPresent("xpath=//title");
        assertNotTitle("example page");
        assertNotTitle("regexp:.+");

        //
        // ~~~ pageWithNoTitle ~~~
        //
        startAction("pageWithNoTitle");
        click("id=title_remove");
        assertNotElementPresent("xpath=//title");
        assertNotTitle("example page");
        assertNotTitle("regexp:.+");

    }
}