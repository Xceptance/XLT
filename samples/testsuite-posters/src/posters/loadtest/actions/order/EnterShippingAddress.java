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
package posters.loadtest.actions.order;

import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.util.HtmlPageUtils;
import com.xceptance.xlt.api.validators.ContentLengthValidator;
import com.xceptance.xlt.api.validators.HtmlEndTagValidator;
import com.xceptance.xlt.api.validators.HttpResponseCodeValidator;

import posters.loadtest.util.Account;
import posters.loadtest.util.Address;
import posters.loadtest.validators.CheckoutHeaderValidator;

/**
 * Fill in and submit the shipping address form.
 */
public class EnterShippingAddress extends AbstractHtmlPageAction
{
    /**
     * The shipping address form.
     */
    private HtmlForm shippingAddressForm;

    /**
     * The shipping address.
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
     * Constructor that takes an account object to provide a shipping address
     * 
     * @param previousAction
     *            The previously performed action
     * @param account
     *            The account to get a first name and last name
     * @param address
     *            The address used in the billing form
     */
    public EnterShippingAddress(final AbstractHtmlPageAction previousAction, final Account account, final Address address)
    {
        super(previousAction, null);
        this.account = account;
        this.address = address;
    }

    /**
     * Constructor that takes an address object to provide a shipping address
     * 
     * @param previousAction
     * @param timerName
     * @param address
     */
    public EnterShippingAddress(final AbstractHtmlPageAction previousAction, final String timerName, final Address address)
    {
        super(previousAction, timerName);
        account = new Account();
        this.address = address;
    }

    @Override
    public void preValidate() throws Exception
    {
        // Get the result of the previous action.
        final HtmlPage page = getPreviousAction().getHtmlPage();
        Assert.assertNotNull("Failed to get page from previous action.", page);

        // Check that the form to enter a new shipping address is available.
        Assert.assertTrue("Form to enter shipping address not found.", HtmlPageUtils.isElementPresent(page, "id('formAddDelAddr')"));

        // Remember the shipping address form.
        shippingAddressForm = HtmlPageUtils.findSingleHtmlElementByID(page, "formAddDelAddr");

        // Check that the button to submit the shipping address is available.
        Assert.assertTrue("Button to submit shipping address not found.", HtmlPageUtils.isElementPresent(page, "id('btnAddDelAddr')"));

        // Remember the button to submit the shipping address.
        submitAddressButton = HtmlPageUtils.findSingleHtmlElementByID(page, "btnAddDelAddr");
    }

    @Override
    protected void execute() throws Exception
    {
        // Fill in the shipping address.
        HtmlPageUtils.setInputValue(shippingAddressForm, "fullName", account.getFirstName() + " " + account.getLastName());
        HtmlPageUtils.setInputValue(shippingAddressForm, "company", address.getCompany());
        HtmlPageUtils.setInputValue(shippingAddressForm, "addressLine", address.getAddressLine());
        HtmlPageUtils.setInputValue(shippingAddressForm, "city", address.getCity());
        HtmlPageUtils.setInputValue(shippingAddressForm, "state", address.getState());
        HtmlPageUtils.setInputValue(shippingAddressForm, "zip", address.getZip());
        // We want to add a billing address separately, so both addresses are not equal.
        HtmlPageUtils.checkRadioButton(shippingAddressForm, "billEqualShipp", 1);

        // Submit the shipping address.
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

        // Check that it's the page to enter or select a billing address.
        Assert.assertTrue("Title not found.", HtmlPageUtils.isElementPresent(page, "id('titleBillAddr')"));

        // Check that the form to enter a new billing address is available.
        Assert.assertTrue("Form to enter billing address not found.", HtmlPageUtils.isElementPresent(page, "id('formAddBillAddr')"));
    }
}
