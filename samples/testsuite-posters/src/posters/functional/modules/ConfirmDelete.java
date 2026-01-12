package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

/**
 * TODO: Add class description
 */
public class ConfirmDelete
{

    /**
     * TODO: Add description
     *
     */
    public static void execute()
    {
        waitForElementPresent("id=buttonDelete");
        click("id=buttonDelete");

    }
}