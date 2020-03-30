/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package posters.loadtest.actions.order;

import org.junit.Assert;

import posters.loadtest.util.Account;
import posters.loadtest.util.Address;
import posters.loadtest.validators.CheckoutHeaderValidator;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.util.HtmlPageUtils;
import com.xceptance.xlt.api.validators.ContentLengthValidator;
import com.xceptance.xlt.api.validators.HtmlEndTagValidator;
import com.xceptance.xlt.api.validators.HttpResponseCodeValidator;

/**
 * Fill in and submit the billing address form.
 */
public class EnterBillingAddress extends AbstractHtmlPageAction
{
    /**
     * The billing address form.
     */
    private HtmlForm billingAddressForm;

    /**
     * The billing address.
     */
    private final Address address;

    /**
     * Account data to use.
     */
    private final Account account;

    /**
     * The submit address button
     */
    private HtmlElement submitAddressButton;

    /**
     * Constructor that takes an existing account to get a first name and last name
     * 
     * @param previousAction
     *            The previously performed action
     * @param account
     *            The account to get a first name and last name
     * @param address
     *            The address used in the billing form
     */
    public EnterBillingAddress(final AbstractHtmlPageAction previousAction, final Account account, final Address address)
    {
        super(previousAction, null);
        this.account = account;
        this.address = address;
    }

    @Override
    public void preValidate() throws Exception
    {
        // Get the result of the previous action.
        final HtmlPage page = getPreviousAction().getHtmlPage();
        Assert.assertNotNull("Failed to get page from previous action.", page);

        // Check that the form to enter a new billing address is available.
        Assert.assertTrue("Form to enter billing address not found.", HtmlPageUtils.isElementPresent(page, "id('formAddBillAddr')"));

        // Remember the billing address form.
        billingAddressForm = HtmlPageUtils.findSingleHtmlElementByID(page, "formAddBillAddr");

        // Check that the button to submit the billing address is available.
        Assert.assertTrue("Button to submit billing address not found.", HtmlPageUtils.isElementPresent(page, "id('btnAddBillAddr')"));

        // Remember the button to submit the billing address.
        submitAddressButton = HtmlPageUtils.findSingleHtmlElementByID(page, "btnAddBillAddr");
    }

    @Override
    protected void execute() throws Exception
    {
        // Fill in the billing address.
        HtmlPageUtils.setInputValue(billingAddressForm, "fullName", account.getFirstName() + " " + account.getLastName());
        HtmlPageUtils.setInputValue(billingAddressForm, "company", address.getCompany());
        HtmlPageUtils.setInputValue(billingAddressForm, "addressLine", address.getAddressLine());
        HtmlPageUtils.setInputValue(billingAddressForm, "city", address.getCity());
        HtmlPageUtils.setInputValue(billingAddressForm, "state", address.getState());
        HtmlPageUtils.setInputValue(billingAddressForm, "zip", address.getZip());

        // Submit the billing form.
        loadPageByClick(submitAddressButton);
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

        CheckoutHeaderValidator.getInstance().validate(page);

        // Check that the resulting page is the payment page.
        Assert.assertTrue("Title not found.", HtmlPageUtils.isElementPresent(page, "id('titlePayment')"));

        // Check that the form to enter a new payment method is available.
        Assert.assertTrue("Form to enter payment method not found.", HtmlPageUtils.isElementPresent(page, "id('formAddPayment')"));
    }
}
