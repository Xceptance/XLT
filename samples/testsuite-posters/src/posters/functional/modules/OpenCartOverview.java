package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

/**
 * <p>Opens the cart overview page.</p>
 */
public class OpenCartOverview
{

    /**
     * <p>Opens the cart overview page.</p>
     *
     */
    public static void execute()
    {
        //
        // ~~~ OpenCartOverview ~~~
        //
        startAction("OpenCartOverview");
        click("id=headerCartOverview");
        waitForElementPresent("id=miniCartMenu");
        //
        // ~~~ GoToCart ~~~
        //
        startAction("GoToCart");
        clickAndWait("css=#miniCartMenu .goToCart");

    }
}