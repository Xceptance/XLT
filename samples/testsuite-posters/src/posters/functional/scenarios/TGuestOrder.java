package posters.functional.scenarios;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.AddToCart;
import posters.functional.modules.FillInAddressForm;
import posters.functional.modules.FillInPaymentForm;
import posters.functional.modules.OpenCartOverview;
import posters.functional.modules.OpenHomepage;
import posters.functional.modules.VerifyAddressAndPaymentOfOrder;
import posters.functional.modules.VerifyOrderOverview;

/**
 * <p>Simulates browsing the catalog, adding product(s) to the cart, checkout as guest and place the order.</p>
 */
public class TGuestOrder extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TGuestOrder()
    {
        super("https://localhost:8443");
    }


    /**
     * Executes the test.
     *
     * @throws Throwable if anything went wrong
     */
    @Test
    public void test() throws Throwable
    {
        OpenHomepage.execute();

        AddToCart.execute();

        //
        // ~~~ OpenCartOverview ~~~
        //
        startAction("OpenCartOverview");
        OpenCartOverview.execute();

        //
        // ~~~ StartCheckout ~~~
        //
        startAction("StartCheckout");
        storeText("id=orderTotal", "totalPrice");
        clickAndWait("id=btnStartCheckout");
        //
        // ~~~ EnterShippingAddress ~~~
        //
        startAction("EnterShippingAddress");
        assertElementPresent("id=titleDelAddr");
        FillInAddressForm.execute("${fullName}", "${company}", "${address}", "${city}", "${state}", "${zip}", "${country}");

        check("id=billEqualShipp-No");
        clickAndWait("id=btnAddDelAddr");
        //
        // ~~~ EnterBillingAddress ~~~
        //
        startAction("EnterBillingAddress");
        assertElementPresent("id=titleBillAddr");
        FillInAddressForm.execute("${fullName}", "${company}", "${address}", "${city}", "${state}", "${zip}", "${country}");

        clickAndWait("id=btnAddBillAddr");
        //
        // ~~~ EnterPaymentMethod ~~~
        //
        startAction("EnterPaymentMethod");
        assertElementPresent("id=titlePayment");
        FillInPaymentForm.execute("${creditCard}", "${fullName}", "${expDateMonth}", "${expDateYear}");

        clickAndWait("id=btnAddPayment");
        //
        // ~~~ Order ~~~
        //
        startAction("Order");
        VerifyOrderOverview.execute("0", "${productName}", "1", "${productFinish}", "${productSize}");

        VerifyAddressAndPaymentOfOrder.execute("${fullName}", "${fullName}", "${fullName}", "${company}", "${company}", "${address}", "${address}", "${city}", "${city}", "${state}", "${zip}", "${country}", "${creditCardCryptic}", "${expDateMonth}", "${expDateYear}");

        clickAndWait("id=btnOrder");
        assertElementPresent("id=successMessage");
        assertText("css=.headerCartProductCount", "0");

    }

}