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
package action.placeholders.injectTestdata;

import org.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;

import action.placeholders.injectTestdata.Mod_1c;

/**
 * Use test data but do not define them.
 */
public class Mod_1b extends AbstractHtmlUnitCommandsModule
{


    /**
     * Constructor.
     * 
     */
    public Mod_1b()
    {
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        final Mod_1c mod_1c = new Mod_1c();
        resultingPage = mod_1c.run(resultingPage);

        // assert reset
        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 3");
        assertText("id=specialchar_1", resolve("${gtd2}"));
        resultingPage = type("id=in_txt_1", resolve("${t1}  - 2"));

        return resultingPage;
    }
}