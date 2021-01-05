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
package posters.loadtest.actions.account;

import org.junit.Assert;

import posters.loadtest.util.Account;
import posters.loadtest.validators.HeaderValidator;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.util.HtmlPageUtils;
import com.xceptance.xlt.api.validators.ContentLengthValidator;
import com.xceptance.xlt.api.validators.HtmlEndTagValidator;
import com.xceptance.xlt.api.validators.HttpResponseCodeValidator;

/**
 * Fill in and submit the sign in form. <br>
 * The previous action should be {@link GoToSignIn}. The resulting page is the homepage.
 */
public class Login extends AbstractHtmlPageAction
{
    /**
     * The sign in form.
     */
    private HtmlForm signInForm;

    /**
     * The button to submit the sign in form.
     */
    private HtmlElement signInButton;

    /**
     * The account to log in.
     */
    private final Account account;

    /**
     * Constructor
     * 
     * @param previousAction
     *            the previously performed action
     * @param account
     *            the account to log in
     */
    public Login(final AbstractHtmlPageAction previousAction, final Account account)
    {
        super(previousAction, null);
        this.account = account;
    }

    @Override
    public void preValidate() throws Exception
    {
        // Get the result of the previous action.
        final HtmlPage page = getPreviousAction().getHtmlPage();
        Assert.assertNotNull("Failed to get page from previous action.", page);

        // Check that the sign in form is available.
        Assert.assertTrue("Sign in form not found", HtmlPageUtils.isElementPresent(page, "id('formLogin')"));

        // Remember the sign in form.
        signInForm = HtmlPageUtils.findSingleHtmlElementByID(page, "formLogin");

        // Check that the sign in button is available.
        Assert.assertTrue("Sign in button not found", HtmlPageUtils.isElementPresent(page, "id('btnSignIn')"));

        // Remember the sign in button.
        signInButton = HtmlPageUtils.findSingleHtmlElementByID(page, "btnSignIn");
    }

    @Override
    protected void execute() throws Exception
    {
        // Fill in the form.
        HtmlPageUtils.setInputValue(signInForm, "email", account.getEmail());
        HtmlPageUtils.setInputValue(signInForm, "password", account.getPassword());

        // Submit the registration form.
        loadPageByClick(signInButton);
    }

    @Override
    protected void postValidate() throws Exception
    {
        // Get the result of the action.
        final HtmlPage page = getHtmlPage();

        // Basic checks - see action 'Homepage' for some more details how and when to use these validators.
        HttpResponseCodeValidator.getInstance().validate(page);
        ContentLengthValidator.getInstance().validate(page);
        HtmlEndTagValidator.getInstance().validate(page);

        HeaderValidator.getInstance().validate(page);

        // Check that the customer is logged in.
        Assert.assertTrue("Customer is not logged in.", HtmlPageUtils.isElementPresent(page, "id('userMenu')//a[@class='goToAccountOverview']"));

        // Check that it's the homepage.
        final HtmlElement blogNameElement = page.getHtmlElementById("titleIndex");
        Assert.assertNotNull("Title not found", blogNameElement);

        // Check the title.
        Assert.assertEquals("Title does not match", "Check out our new panorama posters", blogNameElement.asText());
    }
}
