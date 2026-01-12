package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

/**
 * <p>Searches the specified term.</p>
 */
public class Search
{

    /**
     * <p>Searches the specified term.</p>
     *
     * @param searchTerm
     */
    public static void execute(String searchTerm)
    {
        // resolve any placeholder in the parameters
        searchTerm = resolve(searchTerm);
        // Cick the the search button to submit
        click("id=header-search-trigger");
        waitForElementPresent("id=header-menu-search");
        type("id=s", searchTerm);
        // Cick the the search button to submit
        clickAndWait("id=btnSearch");

    }
}