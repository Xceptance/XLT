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
package action.placeholders.injectTestdata.TInjectTestData_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import action.modules.Mod_2;

/**
 * TODO: Add class description
 */
public class TInjectTestData_0 extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public TInjectTestData_0(final AbstractHtmlPageAction prevAction)
    {
        super(prevAction);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void preValidate() throws Exception
    {
        final HtmlPage page = getPreviousAction().getHtmlPage();
        Assert.assertNotNull("Failed to get page from previous action", page);

    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void execute() throws Exception
    {
        HtmlPage page = getPreviousAction().getHtmlPage();
        page = type("id=in_txt_1", resolve("${td1} - 0"));
        assertText("id=cc_keyup", "keyup (in_txt_1) fromPkgLvl2 - 0");
        assertText("id=specialchar_1", resolve("${gtd2}"));
        page = type("id=in_txt_1", resolve("${td2} - 0"));
        assertText("id=cc_keyup", "keyup (in_txt_1) fromPkgLvl1 - 0");
        assertText("id=specialchar_1", resolve("${gtd2}"));
        final Mod_2 mod_2 = new Mod_2();
        page = mod_2.run(page);


        setHtmlPage(page);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void postValidate() throws Exception
    {
        final HtmlPage page = getHtmlPage();
        Assert.assertNotNull("Failed to load page", page);


    }
}