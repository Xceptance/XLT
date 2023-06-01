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
package posters.loadtest.actions.account;

import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.util.HtmlPageUtils;
import com.xceptance.xlt.api.validators.ContentLengthValidator;
import com.xceptance.xlt.api.validators.HtmlEndTagValidator;
import com.xceptance.xlt.api.validators.HttpResponseCodeValidator;

import posters.loadtest.validators.HeaderValidator;

/**
 * Browses to the registration form.
 */
public class GoToRegistrationForm extends AbstractHtmlPageAction
{
    /**
     * The link to the registration form.
     */
    private HtmlElement registerLink;

    /**
     * Constructor.
     * 
     * @param previousAction
     *            The previously performed action
     */
    public GoToRegistrationForm(final AbstractHtmlPageAction previousAction)
    {
        super(previousAction, null);
    }

    @Override
    public void preValidate() throws Exception
    {
        // Get the result of the previous action.
        final HtmlPage page = getPreviousAction().getHtmlPage();
        Assert.assertNotNull("Failed to get page from previous action.", page);

        // Check that the registration link is available.
        Assert.assertTrue("Registration link not found.", HtmlPageUtils.isElementPresent(page, "id('linkRegister')"));

        // Remember the registration link.
        registerLink = HtmlPageUtils.findSingleHtmlElementByID(page, "linkRegister");
    }

    @Override
    protected void execute() throws Exception
    {
        // Load the registration page by clicking the link.
        loadPageByClick(registerLink);
    }

    @Override
    protected void postValidate() throws Exception
    {
        // get the result of this action
        final HtmlPage page = getHtmlPage();

        // Basic checks - see action 'Homepage' for some more details how and when to use these validators
        HttpResponseCodeValidator.getInstance().validate(page);
        ContentLengthValidator.getInstance().validate(page);
        HtmlEndTagValidator.getInstance().validate(page);

        HeaderValidator.getInstance().validate(page);

        // Check that it's the registration page.
        Assert.assertTrue("Registration form not found.", HtmlPageUtils.isElementPresent(page, "id('formRegister')"));
        Assert.assertTrue("Button to create account not found.", HtmlPageUtils.isElementPresent(page, "id('btnRegister')"));
    }
}
