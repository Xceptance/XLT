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
package action.testcases.ContextMenu_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class ContextMenuAction extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public ContextMenuAction(final AbstractHtmlPageAction prevAction)
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
        page = click("link=Misc");
        page = click("id=cc_clear_button");
        page = contextMenu("id=cm-area");
        assertText("id=cc_mousedown_content", "regexp:2 \\(x: \\d+, y: \\d+\\)");
        storeText("id=cc_mousedown_content", "md");
        assertText("id=cc_contextmenu_content", "regexp:2 \\(x: \\d+, y: \\d+\\)");
        storeText("id=cc_contextmenu_content", "cm");
        assertText("id=cc_mouseup_content", "regexp:2 \\(x: \\d+, y: \\d+\\)");
        storeText("id=cc_mouseup_content", "mu");
        page = click("id=cc_clear_button");
        page = contextMenuAt("id=cm-area", "20, 34");

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

        assertText("id=cc_mousedown_content", "regexp:2 \\(x: \\d+, y: \\d+\\)");
        assertText("id=cc_contextmenu_content", "regexp:2 \\(x: \\d+, y: \\d+\\)");
        assertText("id=cc_mouseup_content", "regexp:2 \\(x: \\d+, y: \\d+\\)");
        assertNotText("id=cc_mousedown_content", resolve("exact:${md}"));
        assertNotText("id=cc_contextmenu_content", resolve("exact:${cm}"));
        assertNotText("id=cc_mouseup_content", resolve("exact:${mu}"));

    }
}