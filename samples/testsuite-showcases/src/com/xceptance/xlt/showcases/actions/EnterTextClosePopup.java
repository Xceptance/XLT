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
package com.xceptance.xlt.showcases.actions;

import org.htmlunit.html.HtmlButton;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlTextArea;
import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.validators.StandardValidator;

/**
 * We enter the text to the textarea and press apply. This will transfer the text to the "opener" page and close the
 * popup.
 */
public class EnterTextClosePopup extends AbstractHtmlPageAction
{
    /**
     * The timer name to use. The timer name is used to log measurements associated with this action. It can be passed
     * to the super class by the constructor.
     */
    private static final String TIMERNAME = "EnterTextClosePopup";

    /**
     * The apply button.
     */
    private HtmlButton applyButton;

    /**
     * the provided text
     */
    private final String text;

    /**
     * Constructor.
     * 
     * @param previousAction
     *            the action we come from
     */
    public EnterTextClosePopup(final AbstractHtmlPageAction previousAction, final String text)
    {
        super(previousAction, TIMERNAME);
        this.text = text;
    }

    /**
     * Verify all preconditions. The prevalidate method is a used to ensure that everything that is needed to execute
     * this action is present on the page.
     */
    @Override
    public void preValidate() throws Exception
    {
        final HtmlPage page = getPreviousAction().getHtmlPage();

        // we need the alert button
        applyButton = page.getHtmlElementById("apply");

        // set the provided text to the text area
        final HtmlTextArea text = page.getHtmlElementById("text");
        text.setTextContent(this.text);
    }

    /**
     * Execute the request. Once pre-execution conditions have been meet, the execute method can be called to load the
     * page.
     */
    @Override
    protected void execute() throws Exception
    {
        // now load the new page
        loadPageByClick(applyButton);
    }

    /**
     * Validate the correctness of the result.
     */
    @Override
    protected void postValidate() throws Exception
    {

        final HtmlPage page = getHtmlPage();
        // First, we check all common criteria. This code can be bundled and
        // reused
        // if needed. For the purpose of the programming example, we leave it
        // here as
        // detailed as possible.
        // We add a catch block to the test running.
        // Messages are logged.
        StandardValidator.getInstance().validate(page);

        // check if the content div contains the provided text
        final HtmlElement content = page.getHtmlElementById("content");
        Assert.assertTrue(content.getTextContent().equals(text));
    }
}
