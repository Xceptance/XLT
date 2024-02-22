/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package action.modules.assertText_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import org.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class whitespaces extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public whitespaces(final AbstractHtmlPageAction prevAction)
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
        assertText("css=span#ws1_single_ws", "This text contains just single spaces.");
        assertText("id=ws1_multiple_ws", "This text contains multiple spaces.");
        assertText("id=ws1_single_tab", "This text contains single tabulators.");
        assertText("id=ws1_multiple_tab", "This text contains multiple tabulators.");
        assertText("id=ws1_line_break", "This text contains line breaks.");
        assertText("id=ws1_single_html_spaces", "This text contains single HTML encoded spaces.");
        assertText("id=ws1_multiple_html_spaces", "This text contains multiple HTML encoded spaces.");
        assertText("id=ws1_alternating_spaces", "This text contains alternating spaces.");
        assertText("id=ws2_274", "This text contains 274 spaces in row.");
        assertText("id=ws2_mixed_spaces", "This text contains mixed white spaces.");
        assertText("id=ws2_spaces_only", "");
        assertText("id=ws2_spaces_only", "          ");
        assertText("id=ws2_html_spaces_only", "");
        assertText("id=ws2_html_spaces_only", "          ");
        assertText("id=ws3_paragraph", "This text contains paragraph tags.");
        assertText("id=ws4_br", "This text contains HTML encoded line breaks.");
        assertText("id=ws5_a", "This text contains many div tags.");
        assertText("id=ws7_div", "Each word has its own div.");

    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void execute() throws Exception
    {
        HtmlPage page = getPreviousAction().getHtmlPage();

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

        assertText("css=span#ws1_single_ws", "This text contains just single spaces.");
        assertText("id=ws1_multiple_ws", "This text contains multiple spaces.");
        assertText("id=ws1_single_tab", "This text contains single tabulators.");
        assertText("id=ws1_multiple_tab", "This text contains multiple tabulators.");
        assertText("id=ws1_line_break", "This text contains line breaks.");
        assertText("id=ws1_single_html_spaces", "This text contains single HTML encoded spaces.");
        assertText("id=ws1_multiple_html_spaces", "This text contains multiple HTML encoded spaces.");
        assertText("id=ws1_alternating_spaces", "This text contains alternating spaces.");
        assertText("id=ws2_274", "This text contains 274 spaces in row.");
        assertText("id=ws2_mixed_spaces", "This text contains mixed white spaces.");
        assertText("id=ws2_spaces_only", "");
        assertText("id=ws2_spaces_only", "          ");
        assertText("id=ws2_html_spaces_only", "");
        assertText("id=ws2_html_spaces_only", "          ");
        assertText("id=ws3_paragraph", "This text contains paragraph tags.");
        assertText("id=ws4_br", "This text contains HTML encoded line breaks.");
        assertText("id=ws5_a", "This text contains many div tags.");
        assertText("id=ws7_div", "Each word has its own div.");

    }
}