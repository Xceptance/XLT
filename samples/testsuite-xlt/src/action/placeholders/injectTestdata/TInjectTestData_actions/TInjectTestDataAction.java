/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
import org.htmlunit.html.HtmlPage;

import action.placeholders.injectTestdata.Mod_1c;
import action.placeholders.injectTestdata.Mod_1b;
import action.placeholders.injectTestdata.Mod_1a;

/**
 * TODO: Add class description
 */
public class TInjectTestDataAction extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public TInjectTestDataAction(final AbstractHtmlPageAction prevAction)
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
        assertText("id=specialchar_1", resolve("${gtd2}"));

    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void execute() throws Exception
    {
        HtmlPage page = getPreviousAction().getHtmlPage();
        page = type("id=in_txt_1", resolve("${t1}  - 0"));
        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 0");
        final Mod_1c mod_1c = new Mod_1c();
        page = mod_1c.run(page);

        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 3");
        final Mod_1b mod_1b = new Mod_1b();
        page = mod_1b.run(page);

        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 2");
        final Mod_1a mod_1a = new Mod_1a();
        page = mod_1a.run(page);


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

        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 1");

    }
}