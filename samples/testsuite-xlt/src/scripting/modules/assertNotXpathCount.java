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
package scripting.modules;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;
import scripting.modules.SelectFrame_iframe_1;

/**
 * TODO: Add class description
 */
public class assertNotXpathCount extends AbstractWebDriverModule
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
        // ~~~ existing_wrongCount ~~~
        //
        startAction("existing_wrongCount");
        assertNotXpathCount("/html/body/ol//div[@id=\"disappear\"]/a", "0");
        assertNotXpathCount("/html/body/ol//div[@id=\"disappear\"]/a", "4");
        assertNotXpathCount("/html/body/ol//div[@id='disappear']/a", "6");
        assertNotXpathCount("/html/body/ol//div[@id='disappear']/a", "2147483647");
        //
        // ~~~ non_existing_element ~~~
        //
        startAction("non_existing_element");
        assertNotXpathCount("/html/body/ol//div[@id=\"xyz\"]", "1");
        //
        // ~~~ iframe ~~~
        //
        startAction("iframe");
        final SelectFrame_iframe_1 _selectFrame_iframe_1 = new SelectFrame_iframe_1();
        _selectFrame_iframe_1.execute();

        assertNotXpathCount("/html/body/div[@id='f1_i']", "100");

    }
}