package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

/**
 * <p>Selects one product randomly.</p>
 */
public class SelectProductRandomly
{

    /**
     * <p>Selects one product randomly.</p>
     *
     */
    public static void execute()
    {
        //
        // ~~~ SelectProductRandomly ~~~
        //
        startAction("SelectProductRandomly");
        storeXpathCount("//div[@id='productOverview']/div", "productRowCount");
        store("${RANDOM.Number(1,${productRowCount})}", "productIndex");
        // store info from the random product
        storeText("//div[@id='product${productIndex}']/div/a/div[@class='pInfo']/h4[@class='text-primary pName']", "productName");
        clickAndWait("//div[@id='product${productIndex}']/div/a");
        assertText("id=titleProductName", "${productName}");

    }
}