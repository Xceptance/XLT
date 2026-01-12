package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

/**
 * <p>Verifies the cart overview in the header.</p>
 */
public class VerifyCartOverview
{

    /**
     * <p>Verifies the cart overview in the header.</p>
     *
     * @param productCount
     */
    public static void execute(String productCount)
    {
        // resolve any placeholder in the parameters
        productCount = resolve(productCount);
        assertText("css=.headerCartProductCount", productCount);

    }
}