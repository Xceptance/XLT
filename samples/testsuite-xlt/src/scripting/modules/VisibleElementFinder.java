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
import scripting.modules.VisibleElementFinder_Anchor;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class VisibleElementFinder extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {

        //
        // ~~~ click_by_name_link_xpath ~~~
        //
        startAction("click_by_name_link_xpath");
        final VisibleElementFinder_Anchor _visibleElementFinder_Anchor = new VisibleElementFinder_Anchor();
        _visibleElementFinder_Anchor.execute("name=in_visible_anchor", "iframe 1");

        _visibleElementFinder_Anchor.execute("link=in_visible_anchor", "iframe 2");

        _visibleElementFinder_Anchor.execute("xpath=id('in_visible_anchor')/div/a", "iframe 1");

        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();


        //
        // ~~~ check ~~~
        //
        startAction("check");
        check("xpath=id('in_visible_checkbox')/div/input");
        assertText("id=cc_change", "change (in_visible_checkbox_inv) true");
        check("id=invisible_radio_byDisplayNone");
        assertText("id=cc_change", "change (invisible_radio_byDisplayNone) true");

        //
        // ~~~ uncheck ~~~
        //
        startAction("uncheck");
        uncheck("xpath=id('in_visible_checkbox')/div/input");
        assertText("id=cc_change", "change (in_visible_checkbox_inv) false");

        //
        // ~~~ type ~~~
        //
        startAction("type");
        type("xpath=id('in_visible_inputtext')/div/input", "123");
        assertText("id=cc_keyup", "keyup (in_visible_inputtext_inv) 123");

        //
        // ~~~ select ~~~
        //
        startAction("select");
        select("xpath=id('in_visible_select')/div/select", "index=1");
        assertText("id=cc_change", "change (in_visible_select_inv) ib");

        //
        // ~~~ removeSelection ~~~
        //
        startAction("removeSelection");
        removeSelection("xpath=id('in_visible_select')/div/select", "index=1");
        assertText("id=cc_change", "change (in_visible_select_inv)");

        //
        // ~~~ select ~~~
        //
        startAction("select");
        addSelection("xpath=id('in_visible_select')/div/select", "index=1");
        assertText("id=cc_change", "change (in_visible_select_inv) ib");

    }
}