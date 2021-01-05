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
package action.modules.waitFor_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class waitForSelected extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public waitForSelected(final AbstractHtmlPageAction prevAction)
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
        page = click("id=select_22_a_delayedSelect");
        page = waitForSelectedId("id=select_22", "select_22_a");
        page = click("id=select_22_d_delayedSelect");
        page = waitForSelectedIndex("id=select_22", "3");
        page = click("id=select_22_a_delayedSelect");
        page = waitForSelectedLabel("id=select_22", "select_22_a");
        page = click("id=select_22_d_delayedSelect");
        page = waitForSelectedValue("id=select_22", "select_22_d");
        page = click("id=select_24_a_delayedSelect");
        page = waitForSelectedId("id=select_24", "select_24_a");
        page = click("id=select_24_d_delayedSelect");
        page = waitForSelectedIndex("id=select_24", "3");
        page = click("id=select_24_a_delayedSelect");
        page = waitForSelectedLabel("id=select_24", "select_24_a");
        page = click("id=select_24_d_delayedSelect");
        page = waitForSelectedValue("id=select_24", "select_24_d");

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