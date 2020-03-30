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
import scripting.modules.SelectFrame_iframe_1;
import scripting.modules.SelectFrame_iframe_12;

/**
 * TODO: Add class description
 */
public class assertNotText extends AbstractWebDriverModule
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
        // ~~~ checkElementPresence ~~~
        //
        startAction("checkElementPresence");
        assertElementPresent("id=specialchar_1");
        assertElementPresent("id=appear");
        assertElementPresent("id=invisible_empty_div");

        //
        // ~~~ non_existing ~~~
        //
        startAction("non_existing");
        assertNotText("id=specialchar_1", "*This text does not exist in page*");

        //
        // ~~~ case_insensitive ~~~
        //
        startAction("case_insensitive");
        assertNotText("id=specialchar_1", "*LOREM IPSUM*");
        assertNotText("id=specialchar_1", "regexp:.*LOREM IPSUM.*");
        assertNotText("id=specialchar_1", "regexpi:.*LOREM PSUM.*");

        //
        // ~~~ existing_but_not_in_this_id ~~~
        //
        startAction("existing_but_not_in_this_id");
        // not in this element but in another one
        assertNotText("id=appear", "*Lorem ipsum*");

        //
        // ~~~ textfield ~~~
        //
        startAction("textfield");
        assertNotText("id=in_txt_1", "");
        assertNotText("id=in_txt_5", "regexp:.+");
        assertNotText("id=in_ta_1", "regexp:.+");
        assertNotText("id=in_ta_2", "");

        //
        // ~~~ matching_strategy ~~~
        //
        startAction("matching_strategy");
        assertNotText("id=specialchar_1", "");
        assertNotText("id=specialchar_1", "glob:");
        assertNotText("id=specialchar_1", "exact:");
        assertNotText("id=specialchar_1", "glob:ipsum");
        assertNotText("id=specialchar_1", "ipsum");

        //
        // ~~~ emptyDiv ~~~
        //
        startAction("emptyDiv");
        assertNotText("id=invisible_empty_div", "?*");

        //
        // ~~~ emptyDiv_visible ~~~
        //
        startAction("emptyDiv_visible");
        click("id=invisible_showEmptyDiv");
        assertNotText("id=invisible_empty_div", "xyz");
        assertNotText("id=invisible_empty_div", "?*");

        //
        // ~~~ invisibleDiv ~~~
        //
        startAction("invisibleDiv");
        assertNotText("xpath=id('invisible_visibility')", "?*");
        assertNotText("xpath=id('invisible_display')", "?*");

        //
        // ~~~ locator ~~~
        //
        startAction("locator");
        assertNotText("id=anc_sel1", "anc");
        assertNotText("name=anc_sel1", "anc");
        assertNotText("link=anc_sel1", "anc");
        assertNotText("xpath=id('anc_sel1')", "anc");
        assertNotText("dom=document.getElementById('anc_sel1')", "anc");
        assertNotText("css=#anchor_selector #anc_sel1", "anc");

        //
        // ~~~ iframe ~~~
        //
        startAction("iframe");
        final SelectFrame_iframe_1 _selectFrame_iframe_1 = new SelectFrame_iframe_1();
        _selectFrame_iframe_1.execute();

        assertElementPresent("id=f1");
        assertNotText("id=f1", "Example Page");

        //
        // ~~~ subframe ~~~
        //
        startAction("subframe");
        final SelectFrame_iframe_12 _selectFrame_iframe_12 = new SelectFrame_iframe_12();
        _selectFrame_iframe_12.execute();

        assertElementPresent("id=f2");
        assertNotText("id=f2", "*iframe 1*");

    }
}