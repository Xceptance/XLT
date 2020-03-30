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
import scripting.modules.StartAppear;

/**
 * TODO: Add class description
 */
public class waitFor extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        final StartAppear _startAppear = new StartAppear();
        _startAppear.execute("1000");

        waitForElementPresent("id=appear_1");
        waitForElementPresent("name=appear_2");
        waitForElementPresent("link=appear_3*");
        waitForElementPresent("xpath=//div[@id='appear']//div[2]");
        waitForElementPresent("dom=document.getElementById('appear_5')");
        waitForText("id=appear_7", "glob:appear_7 : paragraph");
        waitForTitle("appear_8");
        waitForAttribute("xpath=id('appear_9')@name", "text");
        waitForTextPresent("appear_10 link 3");
        waitForXpathCount("//div[@id='appear']/a[@name='appear_10']", 4);
        waitForClass("xpath=//input[@id='appear_9']", "appear_11");
        waitForStyle("css=#appear_9", "color: rgb(0, 191, 255)");

        //
        // ~~~ waitForEval ~~~
        //
        startAction("waitForEval");
        click("id=in_ta_1_delayed_set");
        waitForEval("document.getElementById('in_ta_1').value", "Salat schmeckt besser, wenn man ihn kurz vor dem Verzehr durch ein saftiges Steak ersetzt.");
        click("id=in_ta_2_delayed_replace");
        waitForEval("document.getElementById('in_ta_2').value", "Fettflecken halten länger, wenn man sie ab und zu mit Butter einreibt.");

        //
        // ~~~ waitForXpathCount ~~~
        //
        startAction("waitForXpathCount");
        click("xpath=id('disappear')/input[@type='submit' and @value='disappear (auto)']");
        waitForXpathCount("id('disapp_3')", 0);

        //
        // ~~~ waitForSelected ~~~
        //
        startAction("waitForSelected");
        click("id=select_22_a_delayedSelect");
        waitForSelectedId("id=select_22", "select_22_a");
        click("id=select_22_d_delayedSelect");
        waitForSelectedIndex("id=select_22", "3");
        click("id=select_22_a_delayedSelect");
        waitForSelectedLabel("id=select_22", "select_22_a");
        click("id=select_22_d_delayedSelect");
        waitForSelectedValue("id=select_22", "select_22_d");
        click("id=select_24_a_delayedSelect");
        waitForSelectedId("id=select_24", "select_24_a");
        click("id=select_24_d_delayedSelect");
        waitForSelectedIndex("id=select_24", "3");
        click("id=select_24_a_delayedSelect");
        waitForSelectedLabel("id=select_24", "select_24_a");
        click("id=select_24_d_delayedSelect");
        waitForSelectedValue("id=select_24", "select_24_d");

        //
        // ~~~ waitForChecked ~~~
        //
        startAction("waitForChecked");
        click("id=in_chk_1_delayedCheck");
        waitForChecked("id=in_chk_1");

        //
        // ~~~ waitForValue ~~~
        //
        startAction("waitForValue");
        click("id=in_ta_1_delayed_set");
        waitForValue("id=in_ta_1", "Salat schmeckt besser, wenn man ihn kurz vor dem Verzehr durch ein saftiges Steak ersetzt.");
        click("id=in_ta_2_delayed_replace");
        waitForValue("id=in_ta_2", "Fettflecken halten länger, wenn man sie ab und zu mit Butter einreibt.");

        //
        // ~~~ popup-0 ~~~
        //
        startAction("popup_0");
        click("id=popup_w2");
        waitForPopUp();
        selectWindow("name=popup_w2");
        close();
        // get back focus on main window
        selectWindow();

        //
        // ~~~ popup-1 ~~~
        //
        startAction("popup_1");
        click("id=popup_w2");
        waitForPopUp("popup_w2");
        selectWindow("name=popup_w2");
        close();
        // get back focus on main window
        selectWindow();

        //
        // ~~~ popup-2 ~~~
        //
        startAction("popup_2");
        click("id=popup_w2");
        waitForPopUp("popup_w2", 5000);
        selectWindow("name=popup_w2");
        close();
        // get back focus on main window
        selectWindow();

    }
}