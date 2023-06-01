/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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

import java.util.ArrayList;
import java.util.List;

import org.htmlunit.CollectingAlertHandler;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlButton;
import org.htmlunit.html.HtmlPage;
import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.validators.StandardValidator;

/**
 * This action will add an alert handler to the web client and afterwards execute two alerts. The first alert is executed
 * through an onclick, the second through onload.
 */
public class ExecuteAlerts extends AbstractHtmlPageAction
{
    /**
     * The timer name to use. The timer name is used to log measurements associated with this action. It can be passed
     * to the super class by the constructor.
     */
    private static final String TIMERNAME = "ExecuteAlerts";

    /**
     * The alert button.
     */
    private HtmlButton alertButton;

    /**
     * anchor to the onload page
     */
    private HtmlAnchor onLoadAnchor;

    /**
     * this list will catch all alert messages we get
     */
    private final List<String> collectedAlerts = new ArrayList<String>();

    /**
     * Constructor.
     * 
     * @param previousAction
     *            the action we come from
     */
    public ExecuteAlerts(final AbstractHtmlPageAction previousAction)
    {
        super(previousAction, TIMERNAME);
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
        alertButton = page.getHtmlElementById("alert");

        onLoadAnchor = page.getAnchorByName("onloadalert");

        // here we set the alert handler for this page
        // so the collectedAlerts list will catch all alert messages
        page.getWebClient().setAlertHandler(new CollectingAlertHandler(collectedAlerts));
    }

    /**
     * Execute the request. Once pre-execution conditions have been meet, the execute method can be called to load the
     * page.
     */
    @Override
    protected void execute() throws Exception
    {
        // now push the button
        alertButton.click();

        // now load the new page to get an onload alert
        loadPageByClick(onLoadAnchor);
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

        // check if we get the expected alert message from button click
        Assert.assertEquals(collectedAlerts.get(0).toString(), "Javascript Alert");

        // check if we get the expected alert message from onload
        Assert.assertEquals(collectedAlerts.get(1).toString(), "onload Javascript");
    }
}
