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
package action.modules.multiSelection_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import org.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class locators extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public locators(final AbstractHtmlPageAction prevAction)
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
        page = addSelection("id=select_9", "label=select_9_b");
        assertText("id=cc_change", "change (select_9) select_9_b");
        page = removeSelection("id=select_9", "label=select_9_b");
        assertText("id=cc_change", "change (select_9)");
        page = addSelection("name=select_10", "label=select_10_b");
        assertText("id=cc_change", "change (select_10) select_10_b");
        page = removeSelection("name=select_10", "label=select_10_b");
        assertText("id=cc_change", "change (select_10)");
        page = addSelection("xpath=//select[@id='select_9']", "label=select_9_b");
        assertText("id=cc_change", "change (select_9) select_9_b");
        page = removeSelection("xpath=//select[@id='select_9']", "label=select_9_b");
        assertText("id=cc_change", "change (select_9)");
        page = addSelection("dom=document.getElementById('select_10')", "label=select_10_b");
        assertText("id=cc_change", "change (select_10) select_10_b");
        page = removeSelection("dom=document.getElementById('select_10')", "label=select_10_b");
        assertText("id=cc_change", "change (select_10)");
        page = addSelection("css=#select_9", "label=select_9_b");
        assertText("id=cc_change", "change (select_9) select_9_b");
        page = removeSelection("css=#select_9", "label=select_9_b");

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

        assertText("id=cc_change", "change (select_9)");

    }
}