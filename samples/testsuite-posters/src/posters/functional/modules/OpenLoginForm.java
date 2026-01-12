package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

/**
 * <p>Opens the log-in form.</p>
 */
public class OpenLoginForm
{

    /**
     * <p>Opens the log-in form.</p>
     *
     */
    public static void execute()
    {
        //
        // ~~~ OpenLoginForm ~~~
        //
        startAction("OpenLoginForm");
        assertElementPresent("id=showUserMenu");
        click("id=showUserMenu");
        waitForElementPresent("id=userMenu");
        clickAndWait("css=#userMenu .goToLogin");

    }
}