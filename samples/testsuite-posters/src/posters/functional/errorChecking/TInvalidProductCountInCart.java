package posters.functional.errorChecking;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.AddToCart;
import posters.functional.modules.OpenCartOverview;
import posters.functional.modules.OpenHomepage;

/**
 * <p>Verifies that an error is shown if the product count of the cart couldn&#39;t be updated.</p>
 */
public class TInvalidProductCountInCart extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TInvalidProductCountInCart()
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
        // ~~~ UpdateProductCount ~~~
        //
        startAction("UpdateProductCount");
        assertValue("//input[@id='productCount0']", "1");
        click("//button[@id='btnUpdateProdCount0']");
        type("//input[@id='productCount0']", "10000");
        click("id=btnUpdateProdCount0");
        // validate
        waitForText("id=errorMessage", "× The product count could not be updated. Please try again.");
        assertVisible("id=errorMessage");
        assertText("//input[@id='productCount0']", "1000");

    }

}