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
import scripting.modules.SelectFrame_iframe_12;
import scripting.modules.SelectFrame_iframe_123;

/**
 * TODO: Add class description
 */
public class assertElementPresent extends AbstractWebDriverModule
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
        // ~~~ anchor_link ~~~
        //
        startAction("anchor_link");
        assertElementPresent("id=anc_sel1");
        assertElementPresent("name=anc_sel1");
        assertElementPresent("link=anc_sel1");
        assertElementPresent("xpath=id('anc_sel1')");
        assertElementPresent("dom=document.getElementById('anc_sel1')");
        assertElementPresent("css=#anchor_selector #anc_sel1");
        assertElementPresent("anc_sel1");

        //
        // ~~~ anchor_name ~~~
        //
        startAction("anchor_name");
        assertElementPresent("id=anchor_mark");

        //
        // ~~~ image ~~~
        //
        startAction("image");
        assertElementPresent("id=image_1");

        //
        // ~~~ checkbox ~~~
        //
        startAction("checkbox");
        assertElementPresent("id=in_chk_1");

        //
        // ~~~ radio_button ~~~
        //
        startAction("radio_button");
        assertElementPresent("id=in_rad_1");

        //
        // ~~~ submit_button ~~~
        //
        startAction("submit_button");
        assertElementPresent("id=in_sub_1");

        //
        // ~~~ text_input_field ~~~
        //
        startAction("text_input_field");
        assertElementPresent("id=in_txt_1");

        //
        // ~~~ h1 ~~~
        //
        startAction("h1");
        assertElementPresent("xpath=/html[1]/body[1]/ol[1]/li[1]/h1[1]");

        //
        // ~~~ not_visibile ~~~
        //
        startAction("not_visibile");
        assertElementPresent("id=invisible_visibility");
        assertElementPresent("id=invisible_visibility_ancestor");

        //
        // ~~~ not_displayed ~~~
        //
        startAction("not_displayed");
        assertElementPresent("id=invisible_display");
        assertElementPresent("id=invisible_display_ancestor");

        //
        // ~~~ hidden_input ~~~
        //
        startAction("hidden_input");
        assertElementPresent("id=invisible_hidden_input");

        //
        // ~~~ empty ~~~
        //
        startAction("empty");
        assertElementPresent("id=invisible_empty_div");
        assertElementPresent("id=form4");

        //
        // ~~~ iframe1 ~~~
        //
        startAction("iframe1");
        final SelectFrame_iframe_12 _selectFrame_iframe_12 = new SelectFrame_iframe_12();
        _selectFrame_iframe_12.execute();

        assertElementPresent("id=f2_ia");

        //
        // ~~~ iframe2 ~~~
        //
        startAction("iframe2");
        final SelectFrame_iframe_123 _selectFrame_iframe_123 = new SelectFrame_iframe_123();
        _selectFrame_iframe_123.execute();

        assertElementPresent("xpath=id('f3_ib')");

    }
}