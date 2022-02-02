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
package action.modules.check_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class different_selectors extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public different_selectors(final AbstractHtmlPageAction prevAction)
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
        page = check("id=in_chk_7");
        assertText("id=cc_change", "change (in_chk_7) true");
        page = check("name=in_chk_8");
        assertText("id=cc_change", "change (in_chk_8) true");
        page = check("xpath=id('in_checkbox')/input[@value='in_chk_9' and @type='checkbox']");
        assertText("id=cc_change", "change (in_chk_9) true");
        page = check("xpath=id('in_checkbox')/input[@type='checkbox'][10]");
        assertText("id=cc_change", "change (check plain) true");
        page = check("dom=document.getElementById('in_chk_2')");
        assertText("id=cc_change", "change (in_chk_2) true");
        page = check("css=input#in_chk_6");

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

        assertText("id=cc_change", "change (in_chk_6) true");

    }
}