package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

/**
 * <p>Selects one category randomly.</p>
 */
public class SelectCategoryRandomly
{

    /**
     * <p>Selects one category randomly.</p>
     *
     */
    public static void execute()
    {
        //
        // ~~~ SelectCategoryRandomly ~~~
        //
        startAction("SelectCategoryRandomly");
        storeXpathCount("//div[@id='categoryMenu']/ul/li", "categoryCount");
        store("${RANDOM.Number(1,${categoryCount})}", "categoryIndex");
        storeXpathCount("//div[@id='categoryMenu']/ul/li[${categoryIndex}]/div/ul/li", "subCategoryCount");
        store("${RANDOM.Number(1,${subCategoryCount})}", "subCategoryIndex");
        mouseOver("//div[@id='categoryMenu']/ul/li[${categoryIndex}]");
        clickAndWait("//div[@id='categoryMenu']/ul/li[${categoryIndex}]/div/ul/li[${subCategoryIndex}]/a");

    }
}