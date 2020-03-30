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
public class click extends AbstractWebDriverModule
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
        // ~~~ anchor ~~~
        //
        startAction("anchor");
        click("id=anc_sel1");
        assertText("id=cc_click_head", "click (anc_sel1)");
        click("anc_sel2");
        assertText("id=cc_click_head", "click (anc_sel2)");
        click("name=anc_sel3");
        assertText("id=cc_click_head", "click (anc_sel3)");
        click("link=anc_sel4");
        assertText("id=cc_click_head", "click (anc_sel4)");
        click("css=#anchor_selector #anc_sel1");
        assertText("id=cc_click_head", "click (anc_sel1)");

        //
        // ~~~ submit_button ~~~
        //
        startAction("submit_button");
        click("id=in_sub_5");
        assertText("id=cc_click_head", "click (in_sub_5)");
        click("xpath=id('in_submit')/input[@name='in_sub_6' and @type='submit']");
        assertText("id=cc_click_head", "click (in_sub_6)");

        //
        // ~~~ image ~~~
        //
        startAction("image");
        click("id=image_1");
        assertText("id=cc_click_head", "click (image_1)");

        //
        // ~~~ div ~~~
        //
        startAction("div");
        click("id=page_headline");

        //
        // ~~~ whitespace_in_link_name ~~~
        //
        startAction("whitespace_in_link_name");
        click("link=whitespace in link name");

        //
        // ~~~ whitespace_in_linked_file_name ~~~
        //
        startAction("whitespace_in_linked_file_name");
        clickAndWait("id=anc_link3");
        assertText("xpath=/html/body/h1", "white space in file name");

    }
}