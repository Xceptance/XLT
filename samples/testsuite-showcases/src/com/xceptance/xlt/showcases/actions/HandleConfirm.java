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

import org.htmlunit.ConfirmHandler;
import org.htmlunit.Page;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.validators.StandardValidator;

/**
 * We will push the specified button and check if the result is correct.
 */
public class HandleConfirm extends AbstractHtmlPageAction
{
    /**
     * The timer name to use. The timer name is used to log measurements associated with this action. It can be passed
     * to the super class by the constructor.
     */
    private static final String TIMERNAME = "HandleConfirm";

    /**
     * The button that will show the confirm message.
     */
    private HtmlInput confirmButton;

    /**
     * which button should be pushed
     */
    private final boolean confirm;

    /**
     * Constructor.
     * 
     * @param previousAction
     *            the action we come from
     * @param confirm
     *            what should we do
     */
    public HandleConfirm(final AbstractHtmlPageAction previousAction, final boolean confirm)
    {
        super(previousAction, TIMERNAME);
        this.confirm = confirm;
    }

    /**
     * Verify all preconditions. The prevalidate method is a used to ensure that everything that is needed to execute
     * this action is present on the page.
     */
    @Override
    public void preValidate() throws Exception
    {
        final HtmlPage page = getPreviousAction().getHtmlPage();

        // we need the confirm button
        confirmButton = page.getHtmlElementById("confirm");

        // here we set the confirm handler for this page
        // dependent on the confirm variable the OK or Cancel button is pressed
        page.getWebClient().setConfirmHandler(new ConfirmHandler()
        {
            @Override
            public boolean handleConfirm(final Page page, final String message)
            {
                if (confirm)
                {
                    return true;
                }
                else
                {
                    return false;
                }
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
        loadPageByClick(confirmButton);
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
        if (confirm)
        {
            Assert.assertTrue(page.getHtmlElementById("content").asNormalizedText().contains("To be!"));
        }
        else
        {
            Assert.assertTrue(page.getHtmlElementById("content").asNormalizedText().contains("Not to be!"));
        }
    }
}
