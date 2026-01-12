package posters.functional.errorChecking;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.OpenCartOverview;
import posters.functional.modules.OpenHomepage;

/**
 * <p>Verifies that an error is shown if the checkout is started with an empty cart.</p>
 */
public class TEmptyCart extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TEmptyCart()
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

        OpenCartOverview.execute();

        //
        // ~~~ StartCheckout ~~~
        //
        startAction("StartCheckout");
        // validate
        assertVisible("id=errorCartMessage");
        assertText("id=errorCartMessage", "Your cart is empty. Continue shopping.");

    }

}