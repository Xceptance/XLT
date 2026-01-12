package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

/**
 * <p>Opens the account overview page.</p>
 */
public class OpenAccountOverview
{

    /**
     * <p>Opens the account overview page.</p>
     *
     */
    public static void execute()
    {
        //
        // ~~~ OpenAccountOverview ~~~
        //
        startAction("OpenAccountOverview");
        assertElementPresent("id=showUserMenu");
        click("id=showUserMenu");
        waitForElementPresent("id=userMenu");
        clickAndWait("css=.goToAccountOverview");
        assertElementPresent("id=titleAccountOverview");

    }
}