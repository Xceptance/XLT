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
package com.xceptance.xlt.showcases.actions;

import org.junit.Assert;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.PromptHandler;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.validators.StandardValidator;

/**
 * Prompt provided text. Be careful with HTML special characters. Therefore we need an encoding on the page and in the
 * test case.
 */
public class PromptString extends AbstractHtmlPageAction
{
    /**
     * The timer name to use. The timer name is used to log measurements associated with this action. It can be passed
     * to the super class by the constructor.
     */
    private static final String TIMERNAME = "PromptString";

    /**
     * The button that will show the prompt box
     */
    private HtmlInput promptButton;

    /**
     * text to prompt
     */
    private final String promptText;

    /**
     * Constructor.
     * 
     * @param previousAction
     *            the action we come from
     * @param promptText
     *            what should we prompt
     */
    public PromptString(final AbstractHtmlPageAction previousAction, final String promptText)
    {
        super(previousAction, TIMERNAME);
        this.promptText = promptText;
    }

    /**
     * Verify all preconditions. The prevalidate method is a used to ensure that everything that is needed to execute
     * this action is present on the page.
     */
    @Override
    public void preValidate() throws Exception
    {
        final HtmlPage page = getPreviousAction().getHtmlPage();

        // we need the prompt button
        promptButton = page.getHtmlElementById("prompt");

        // here we set the prompt handler for this page
        // we only want to prompt the provided String
        page.getWebClient().setPromptHandler(new PromptHandler()
        {
            @Override
            public String handlePrompt(final Page page, final String message, final String defaultValue)
            {
                return promptText;
            }
        });
    }

    /**
     * Execute the request. Once pre-execution conditions have been meet, the execute method can be called to load the
     * page.
     */
    @Override
    protected void execute() throws Exception
    {

        // now click the button
        loadPageByClick(promptButton);
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

        // check if we got the correct results
        Assert.assertTrue(page.getHtmlElementById("content").asNormalizedText().contains(promptText));
    }
}
