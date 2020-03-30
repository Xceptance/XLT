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
package action.modules.Select_byIndex_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class Select_byIndexAction extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public Select_byIndexAction(final AbstractHtmlPageAction prevAction)
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
        // space
        page = select("id=select_17", "index=1");
        assertText("id=cc_change", "change (select_17) empty");
        // space
        page = select("id=select_17", "index=2");
        assertText("id=cc_change", "change (select_17) 1 space");
        // spaces
        page = select("id=select_17", "index=3");
        assertText("id=cc_change", "change (select_17)  2 spaces");
        page = select("id=select_17", "index=4");
        assertText("id=cc_change", "change (select_17)  \\");
        page = select("id=select_17", "index=5");
        assertText("id=cc_change", "change (select_17)  ^");
        page = select("id=select_17", "index=6");
        assertText("id=cc_change", "glob:change (select_17)  regexp:[XYZ]{5}");
        page = select("id=select_17", "index=0");
        assertText("id=cc_change", "glob:change (select_17)  :");
        page = select("id=select_17", "index=7");
        assertText("id=cc_change", "glob:change (select_17)  select_17a");
        page = select("id=select_17", "index=8");

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

        assertText("id=cc_change", "glob:change (select_17)  select_17b");

    }
}