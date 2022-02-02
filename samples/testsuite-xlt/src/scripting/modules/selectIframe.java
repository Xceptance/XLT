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
import scripting.modules.SelectFrame_iframe_1;
import scripting.modules.SelectFrame_iframe_12;

/**
 * TODO: Add class description
 */
public class selectIframe extends AbstractWebDriverModule
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
        // ~~~ iframe_1_byIndex ~~~
        //
        startAction("iframe_1_byIndex");
        selectWindow("title=example page");
        selectFrame("index=0");
        assertText("id=f1", "This is iframe 1.");
        //
        // ~~~ iframe_2_byIndex ~~~
        //
        startAction("iframe_2_byIndex");
        selectWindow("title=example page");
        selectFrame("index=1");
        assertText("id=f2", "This is iframe 2.");
        //
        // ~~~ iframe_1_byDom ~~~
        //
        startAction("iframe_1_byDom");
        final SelectFrame_iframe_1 _selectFrame_iframe_1 = new SelectFrame_iframe_1();
        _selectFrame_iframe_1.execute();

        assertText("id=f1", "This is iframe 1.");
        //
        // ~~~ iframe_3_byDom ~~~
        //
        startAction("iframe_3_byDom");
        selectWindow("title=example page");
        selectFrame("dom=frames[\"iframe3\"]");
        assertText("id=f3", "This is iframe 3.");
        //
        // ~~~ iframe_1_2_byDomCascade ~~~
        //
        startAction("iframe_1_2_byDomCascade");
        final SelectFrame_iframe_12 _selectFrame_iframe_12 = new SelectFrame_iframe_12();
        _selectFrame_iframe_12.execute();

        assertText("id=f2", "This is iframe 2.");
        //
        // ~~~ iframe_23_byIndexCascade ~~~
        //
        startAction("iframe_23_byIndexCascade");
        selectWindow("title=example page");
        selectFrame("index=1");
        selectFrame("index=0");
        assertText("id=f3", "This is iframe 3.");
        //
        // ~~~ frame_1_byXpath ~~~
        //
        startAction("frame_1_byXpath");
        selectWindow("title=example page");
        selectFrame("xpath=//iframe[@name='iframe1']");
        assertText("id=f1", "This is iframe 1.");
        //
        // ~~~ frame_2_byXpath ~~~
        //
        startAction("frame_2_byXpath");
        selectWindow("title=example page");
        selectFrame("xpath=//div[@id='iframe']/iframe[2]");
        assertText("id=f2", "This is iframe 2.");
        //
        // ~~~ frame_1_byName ~~~
        //
        startAction("frame_1_byName");
        selectWindow("title=example page");
        selectFrame("name=iframe1");
        assertText("id=f1", "This is iframe 1.");
        //
        // ~~~ frame_3_byName ~~~
        //
        startAction("frame_3_byName");
        selectWindow("title=example page");
        selectFrame("name=iframe3");
        assertText("id=f3", "This is iframe 3.");
        //
        // ~~~ frame_1_byID ~~~
        //
        startAction("frame_1_byID");
        selectWindow("title=example page");
        selectFrame("id=iframe1");
        assertText("id=f1", "This is iframe 1.");
        //
        // ~~~ frame_2_byID ~~~
        //
        startAction("frame_2_byID");
        selectWindow("title=example page");
        selectFrame("id=iframe2");
        assertText("id=f2", "This is iframe 2.");
        //
        // ~~~ frame_1_byID ~~~
        //
        startAction("frame_1_byID");
        selectWindow("title=example page");
        selectFrame("iframe1");
        assertText("id=f1", "This is iframe 1.");
        //
        // ~~~ frame_2_byID ~~~
        //
        startAction("frame_2_byID");
        selectWindow("title=example page");
        selectFrame("iframe2");
        assertText("id=f2", "This is iframe 2.");
        //
        // ~~~ frame_3_byID ~~~
        //
        startAction("frame_3_byID");
        selectWindow("title=example page");
        selectFrame("iframe3");
        assertText("id=f3", "This is iframe 3.");
        //
        // ~~~ top_frame ~~~
        //
        startAction("top_frame");
        selectFrame("relative=top");
        assertText("id=page_headline", "Example Page");

    }
}