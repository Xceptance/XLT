package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

import posters.functional.modules.SelectCategoryRandomly;
import posters.functional.modules.SelectProductRandomly;

/**
 * <p>Browses to a product of a category.</p>
 */
public class Browse
{

    /**
     * <p>Browses to a product of a category.</p>
     *
     */
    public static void execute()
    {
        SelectCategoryRandomly.execute();

        SelectProductRandomly.execute();


    }
}