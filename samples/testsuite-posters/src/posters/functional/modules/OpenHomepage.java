package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

/**
 * <p>Deletes all visible cookies and opens the homepage.</p>
 */
public class OpenHomepage
{

    /**
     * <p>Deletes all visible cookies and opens the homepage.</p>
     *
     */
    public static void execute()
    {
        //
        // ~~~ Homepage ~~~
        //
        startAction("Homepage");
        // Open homepage
        open("${posterShop_url}");
        // Delete cookies in the current context
        deleteAllVisibleCookies();
        // Open homepage again to get a fresh set of cookies
        //
        // ~~~ HomepageAgain ~~~
        //
        startAction("HomepageAgain");
        open("${posterShop_url}");

    }
}