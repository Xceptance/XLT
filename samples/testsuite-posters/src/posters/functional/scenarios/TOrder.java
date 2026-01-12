package posters.functional.scenarios;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.AddRandomBillingAdressToAccount;
import posters.functional.modules.AddRandomPaymentToAccount;
import posters.functional.modules.AddRandomShippingAdressToAccount;
import posters.functional.modules.AddToCart;
import posters.functional.modules.CreateRandomUser;
import posters.functional.modules.Login;
import posters.functional.modules.OpenAccountOverview;
import posters.functional.modules.OpenCartOverview;
import posters.functional.modules.OpenHomepage;
import posters.functional.modules.VerifyAddressAndPaymentOfOrder;
import posters.functional.modules.VerifyOrderOverview;

/**
 * <p>Simulates browsing the catalog, adding product(s) to the cart, checkout as registered customer and place the order.</p>
 */
public class TOrder extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TOrder()
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

        CreateRandomUser.execute();

        Login.execute("${generatedEmail}", "${password}", "${firstName}");

        AddRandomShippingAdressToAccount.execute();

        AddRandomBillingAdressToAccount.execute();

        AddRandomPaymentToAccount.execute();

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
        clickAndWait("id=btnStartCheckout");
        //
        // ~~~ SelectShippingAddress ~~~
        //
        startAction("SelectShippingAddress");
        assertElementPresent("id=titleDelAddr");
        assertElementPresent("id=delAddr0");
        clickAndWait("id=btnUseAddressContinue");
        //
        // ~~~ SelectBillingAddress ~~~
        //
        startAction("SelectBillingAddress");
        assertElementPresent("id=titleBillAddr");
        assertElementPresent("id=billAddr0");
        clickAndWait("id=btnUseBillAddress");
        //
        // ~~~ SelectPaymentMethod ~~~
        //
        startAction("SelectPaymentMethod");
        assertElementPresent("id=titlePayment");
        assertElementPresent("id=payment0");
        clickAndWait("id=btnUsePayment");
        //
        // ~~~ Order ~~~
        //
        startAction("Order");
        VerifyOrderOverview.execute("0", "${productName}", "1", "${productFinish}", "${productSize}");

        VerifyAddressAndPaymentOfOrder.execute("${newBillFullName}", "${newShipFullName}", "${newPaymentFullName}", "${newBillCompany}", "${newShipCompany}", "${newBillAddress}", "${newShipAddress}", "${newBillCity}", "${newShipCity}", "${state}", "${zip}", "${country}", "${creditCardCryptic}", "${expDateMonth}", "${expDateYear}");

        storeText("id=totalCosts", "totalWithTax");
        clickAndWait("id=btnOrder");
        assertElementPresent("id=successMessage");
        assertText("css=.headerCartProductCount", "0");
        OpenAccountOverview.execute();

        //
        // ~~~ ViewOrderOverview ~~~
        //
        startAction("ViewOrderOverview");
        clickAndWait("id=linkOrderOverview");
        assertElementPresent("id=titleOrderHistory");
        assertText("//tr[@id='order0']/td[@class='orderInfo']/div[@class='orderTotalCosts']", "${totalWithTax}");
        // id=order0Product0Name
        assertText("//tr[@id='order0Product0']/td[@class='productInfo']/div[@class='productName']", "${productName}");
        assertText("//tr[@id='order0Product0']/td[@class='productInfo']/div[@class='productMetaInfo text-left']/small/ul/li/span[@class='productStyle']", "${productFinish}");
        assertText("//tr[@id='order0Product0']/td[@class='productInfo']/div[@class='productMetaInfo text-left']/small/ul/li/span[@class='productSize']", "${productSize}");
        assertText("//tr[@id='order0Product0']/td[@class='orderCount']", "1x");

    }

}