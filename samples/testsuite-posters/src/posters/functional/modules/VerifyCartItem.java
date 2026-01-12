package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

import posters.functional.modules.OpenCartOverview;

/**
 * <p>Verifies that a product is in the cart in a specified count.</p>
 */
public class VerifyCartItem
{

    /**
     * <p>Verifies that a product is in the cart in a specified count.</p>
     *
     * @param index
     * @param productName
     * @param productCount
     * @param productFinish
     * @param productSize
     */
    public static void execute(String index, String productName, String productCount, String productFinish, String productSize)
    {
        // resolve any placeholder in the parameters
        index = resolve(index);
        productName = resolve(productName);
        productCount = resolve(productCount);
        productFinish = resolve(productFinish);
        productSize = resolve(productSize);
        //
        // ~~~ goToCartOverview ~~~
        //
        startAction("goToCartOverview");
        OpenCartOverview.execute();

        // validate selected product
        assertElementPresent("css=#product" + index);
        assertText("css=#product" + index + " .productName", productName);
        assertValue("css=#product" + index + " .productCount", productCount);
        assertText("css=#product" + index + " .productStyle", productFinish);
        assertText("css=#product" + index + " .productSize", productSize);

    }
}