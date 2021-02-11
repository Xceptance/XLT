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
package action.testcases.waitForVisible_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class waitForVisibleAction extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public waitForVisibleAction(final AbstractHtmlPageAction prevAction)
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
        page = type("id=timeout_field", "2000");
        page = click("id=invisible_visibility_show");
        page = waitForVisible("id=invisible_visibility_ancestor");
        page = click("id=invisible_visibility_hide");
        page = waitForNotVisible("id=invisible_visibility_ancestor");
        page = click("id=invisible_visibility_style_show");
        page = waitForVisible("id=invisible_visibility_style_ancestor");
        page = click("id=invisible_visibility_style_hide");
        page = waitForNotVisible("id=invisible_visibility_style_ancestor");
        page = click("id=invisible_display_show");
        page = waitForVisible("id=invisible_display_ancestor");
        page = click("id=invisible_display_hide");
        page = waitForNotVisible("id=invisible_display_ancestor");
        page = click("id=invisible_css_submit_show");
        page = waitForVisible("id=invisible_css_submit");
        page = click("id=invisible_css_submit_hide");
        page = waitForNotVisible("id=invisible_css_submit");
        page = click("id=invisible_checkbox_byDisplayNone_show");
        page = waitForVisible("id=invisible_checkbox_byDisplayNone");
        page = click("id=invisible_checkbox_byDisplayNone_hide");
        page = waitForNotVisible("id=invisible_checkbox_byDisplayNone");
        page = click("id=invisible_radio_byDisplayNone_show");
        page = waitForVisible("id=invisible_radio_byDisplayNone");
        page = click("id=invisible_radio_byDisplayNone_hide");
        page = waitForNotVisible("id=invisible_radio_byDisplayNone");

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