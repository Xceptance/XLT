/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
 * Log out.
 */
public class Logout extends AbstractHtmlPageAction
{
    /**
     * The logout link.
     */
    HtmlElement logoutLink;

    /**
     * Constructor.
     * 
     * @param previousAction
     *            The previously performed action
     */
    public Logout(final AbstractHtmlPageAction previousAction)
    {
        super(previousAction, null);
    }

    @Override
    public void preValidate() throws Exception
    {
        // Get the result of the previous action.
        final HtmlPage page = getPreviousAction().getHtmlPage();
        Assert.assertNotNull("Failed to get page from previous action.", page);

        // Check that the customer is logged in.
        Assert.assertTrue("No customer is logged in.", HtmlPageUtils.isElementPresent(page, "id('userMenu')//a[@class='goToAccountOverview']"));

        // Remember logout link.
        logoutLink = HtmlPageUtils.findSingleHtmlElementByXPath(page, "id('userMenu')//a[@class='goToLogout']");
    }

    @Override
    protected void execute() throws Exception
    {
        // Log out by clicking the link.
        loadPageByClick(logoutLink);
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

        // Check that no customer is logged in.
        Assert.assertTrue("A customer is still logged in.", HtmlPageUtils.isElementPresent(page, "id('userMenu')//a[@class='goToLogin']"));

        // Check that it's the home page.
        final HtmlElement blogNameElement = page.getHtmlElementById("titleIndex");
        Assert.assertNotNull("Title not found", blogNameElement);

        // Check the title.
        Assert.assertEquals("Title does not match", "Check out our new panorama posters", blogNameElement.asNormalizedText());
    }
}
