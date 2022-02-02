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
package scripting.modules;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;
import scripting.modules.StartDisappear;

/**
 * TODO: Add class description
 */
public class waitForNot extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        final StartDisappear _startDisappear = new StartDisappear();
        _startDisappear.execute("1000");

        waitForNotTitle("example page");
        waitForNotElementPresent("id=disapp_1");
        waitForNotElementPresent("name=disapp_2");
        waitForNotElementPresent("link=disapp_3");
        waitForNotElementPresent("xpath=id('disapp_4')");
        waitForNotElementPresent("dom=document.getElementById('disapp_5')");
        waitForNotText("id=disapp_6", "glob:disapp_6 : paragraph*");
        waitForNotXpathCount("//div[@id='disappear']/a[@name='disapp_9']", "3");
        waitForNotXpathCount("//div[@id='disappear']/a[@name='disapp_9']", "2147483647");
        waitForNotTextPresent("disapp_8 xcount");
        waitForNotAttribute("xpath=id('disapp_10')@name", "disapp_10");
        waitForNotClass("id=disapp_11", "disapp_11");
        waitForNotStyle("css=#disapp_11", "color: rgb(0, 191, 255)");
        //
        // ~~~ waitForNotEval ~~~
        //
        startAction("waitForNotEval");
        click("id=in_ta_2_delayed_clear");
        waitForNotEval("document.getElementById('in_ta_2').value", "in_ta_2");
        //
        // ~~~ waitForNotXpathCount ~~~
        //
        startAction("waitForNotXpathCount");
        click("id=appear_automatic");
        waitForNotXpathCount("//div[@id='appear']/a[@name='appear_2']", "0");
        //
        // ~~~ waitForNotSelected ~~~
        //
        startAction("waitForNotSelected");
        click("id=select_22_a_delayedSelect");
        waitForNotSelectedId("id=select_22", "select_22_c");
        click("id=select_22_d_delayedSelect");
        waitForNotSelectedIndex("id=select_22", "0");
        click("id=select_22_a_delayedSelect");
        waitForNotSelectedLabel("id=select_22", "select_22_d");
        click("id=select_22_d_delayedSelect");
        waitForNotSelectedValue("id=select_22", "select_22_a");
        click("id=select_24_a_delayedSelect");
        waitForNotSelectedId("id=select_24", "select_24_c");
        click("id=select_24_d_delayedSelect");
        waitForNotSelectedIndex("id=select_24", "0");
        click("id=select_24_a_delayedSelect");
        waitForNotSelectedLabel("id=select_24", "select_24_d");
        click("id=select_24_d_delayedSelect");
        waitForNotSelectedValue("id=select_24", "select_24_a");
        //
        // ~~~ waitForNotChecked ~~~
        //
        startAction("waitForNotChecked");
        click("id=in_chk_5_delayedUncheck");
        waitForNotChecked("id=in_chk_5");
        //
        // ~~~ waitForNotValue ~~~
        //
        startAction("waitForNotValue");
        click("id=in_ta_1_delayed_set");
        waitForNotValue("id=in_ta_1", "");
        click("id=in_ta_2_delayed_replace");
        waitForNotValue("id=in_ta_2", "in_ta_2");

    }
}