/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
import scripting.modules.SelectFrame_iframe_12;

/**
 * TODO: Add class description
 */
public class assertNotElementPresent extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        //
        // ~~~ nonexisting ~~~
        //
        startAction("nonexisting");
        assertNotElementPresent("id=anc");
        assertNotElementPresent("link=anc");
        assertNotElementPresent("name=anc");
        assertNotElementPresent("xpath=id('anc')");
        assertNotElementPresent("css=anc");
        assertNotElementPresent("anc");
        //
        // ~~~ nonexisting_in_iframe1 ~~~
        //
        startAction("nonexisting_in_iframe1");
        selectWindow("title=example page");
        selectFrame("index=0");
        assertNotElementPresent("id=page_headline");
        //
        // ~~~ nonexisting_in_iframe2 ~~~
        //
        startAction("nonexisting_in_iframe2");
        final SelectFrame_iframe_12 _selectFrame_iframe_12 = new SelectFrame_iframe_12();
        _selectFrame_iframe_12.execute();

        assertNotElementPresent("id=page_headline");

    }
}