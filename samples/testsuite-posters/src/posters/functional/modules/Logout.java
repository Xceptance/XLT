package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

/**
 * <p>Logs the current user out.</p>
 */
public class Logout
{

    /**
     * <p>Logs the current user out.</p>
     *
     */
    public static void execute()
    {
        //
        // ~~~ Logout ~~~
        //
        startAction("Logout");
        assertElementPresent("id=showUserMenu");
        click("id=showUserMenu");
        waitForElementPresent("id=userMenu");
        clickAndWait("css=.goToLogout");
        assertVisible("id=successMessage");
        assertText("id=successMessage", "× Logout successful. Have a nice day and see you soon!");

    }
}