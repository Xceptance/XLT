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
package com.xceptance.xlt.showcases.actions;

import java.io.File;

import org.junit.Assert;

import com.gargoylesoftware.htmlunit.html.HtmlHeading2;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.util.HtmlPageUtils;
import com.xceptance.xlt.api.validators.StandardValidator;

/**
 * We upload the test.txt file from the config/data folder.
 */
public class UploadFile extends AbstractHtmlPageAction
{
    /**
     * The timer name to use. The timer name is used to log measurements associated with this action. It can be passed
     * to the super class by the constructor.
     */
    private static final String TIMERNAME = "UploadFile";

    /**
     * The Input for the file path
     */
    private HtmlInput fileInput;

    /**
     * The submit button.
     */
    private HtmlInput submit;

    /**
     * Constructor.
     * 
     * @param previousAction
     *            the action we come from
     */
    public UploadFile(final AbstractHtmlPageAction previousAction)
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

        // catch the submit button
        submit = HtmlPageUtils.findSingleHtmlElementByXPath(page, "//form//input[@name='submit']");

        // catch the input for the file path
        fileInput = HtmlPageUtils.findSingleHtmlElementByXPath(page, "//form//input[@name='userfile1']");

        // now get the file
        final File file = new File("config/data/test.txt");

        // and enter the path into the input
        fileInput.setValueAttribute(file.getAbsolutePath());
    }

    /**
     * Execute the request. Once pre-execution conditions have been meet, the execute method can be called to load the
     * page.
     */
    @Override
    protected void execute() throws Exception
    {
        // load the page by click on the anchor
        loadPageByClick(submit);
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

        final HtmlHeading2 heading = HtmlPageUtils.findSingleHtmlElementByXPath(page, "/html/body/h2");
        // check if we get the success message
        Assert.assertEquals("File successfully uploaded.", heading.getTextContent());
    }
}
