package posters.loadtest.flows;

import posters.loadtest.actions.order.EnterBillingAddress;
import posters.loadtest.actions.order.EnterPaymentMethod;
import posters.loadtest.actions.order.EnterShippingAddress;
import posters.loadtest.actions.order.StartCheckout;
import posters.loadtest.util.Account;
import posters.loadtest.util.Address;
import posters.loadtest.util.CreditCard;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;

/**
 * Perform the checkout steps: Start checkout from cart overview page, enter shipping and billing address and payment
 * method but do not submit the order. The checkout is encapsulated in a flow that combines a sequence of several XLT
 * actions. Different test cases can call this method now to reuse the flow. This is a concept for code structuring you
 * can implement if needed, yet explicit support is neither available in the XLT framework nor necessary when you
 * manually create a flow.
 */
public class CheckoutFlow
{
    /**
     * The previous action
     */
    private final AbstractHtmlPageAction previousAction;

    /**
     * This account data will be used to create a new account.
     */
    private final Account account;

    /**
     * Create new address. Use this address as shipping address.
     */
    private final Address shippingAddress = new Address();

    /**
     * Create new address. Use this address as billing address.
     */
    private final Address billingAddress = new Address();

    /**
     * Create new credit card. Use this credit card as payment method.
     */
    private final CreditCard creditCard;

    /**
     * Constructor
     * 
     * @param previousAction
     *            The previously performed action
     * @param account
     *            The account used in the checkout
     */
    public CheckoutFlow(final AbstractHtmlPageAction previousAction, final Account account)
    {
        this.previousAction = previousAction;
        this.account = account;
        creditCard = new CreditCard(account);
    }

    /**
     * {@inheritDoc}
     */
    public AbstractHtmlPageAction run() throws Throwable
    {
        // Start the checkout.
        final StartCheckout startCheckout = new StartCheckout(previousAction);
        startCheckout.run();

        // Enter the shipping address.
        final EnterShippingAddress enterShippingAddress = new EnterShippingAddress(startCheckout, account, shippingAddress);
        enterShippingAddress.run();

        // Enter the billing address.
        final EnterBillingAddress enterBillingAddress = new EnterBillingAddress(enterShippingAddress, account, billingAddress);
        enterBillingAddress.run();

        // Enter the payment method.
        final EnterPaymentMethod enterPaymentMethod = new EnterPaymentMethod(enterBillingAddress, creditCard);
        enterPaymentMethod.run();

        // Return the last action of this flow to be the input for subsequent actions in a test case.
        return enterPaymentMethod;
    }
}
