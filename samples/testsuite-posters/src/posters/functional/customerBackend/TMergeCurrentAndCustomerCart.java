package posters.functional.customerBackend;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.AddToCart;
import posters.functional.modules.ConfirmDelete;
import posters.functional.modules.CreateRandomUser;
import posters.functional.modules.Login;
import posters.functional.modules.Logout;
import posters.functional.modules.OpenCartOverview;
import posters.functional.modules.OpenHomepage;
import posters.functional.modules.VerifyCartOverview;

/**
 * <p>Simulates browsing the catalog, adding product(s) to the cart, log in and get one cart which includes the products added just now and the products of the customer&#39;s cart.</p>
 */
public class TMergeCurrentAndCustomerCart extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TMergeCurrentAndCustomerCart()
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

        AddToCart.execute();

        OpenCartOverview.execute();

        storeText("css=#product0 .productName", "product1Name");
        assertText("css=.headerCartProductCount", "1");
        Logout.execute();

        assertText("css=.headerCartProductCount", "0");
        AddToCart.execute();

        //
        // ~~~ OpenCartOverview ~~~
        //
        startAction("OpenCartOverview");
        OpenCartOverview.execute();

        storeText("css=#product0 .productName", "product2Name");
        VerifyCartOverview.execute("1");

        Login.execute("${generatedEmail}", "${password}", "${firstName}");

        //
        // ~~~ GetMergedCart ~~~
        //
        startAction("GetMergedCart");
        OpenCartOverview.execute();

        assertText("css=#product0 .productName", "${product2Name}");
        assertValue("css=#product0 .productCount", "1");
        assertText("css=#product1 .productName", "${product1Name}");
        assertValue("css=#product1 .productCount", "1");
        VerifyCartOverview.execute("2");

        //
        // ~~~ RemoveProductFromCart ~~~
        //
        startAction("RemoveProductFromCart");
        click("css=#btnRemoveProdCount0");
        ConfirmDelete.execute();

        waitForNotElementPresent("css=#product0");
        //
        // ~~~ RemoveProductFromCart ~~~
        //
        startAction("RemoveProductFromCart");
        click("css=#btnRemoveProdCount1");
        ConfirmDelete.execute();

        waitForNotElementPresent("css=#product1");
        waitForElementPresent("css=#errorCartMessage");
        VerifyCartOverview.execute("0");


    }

}