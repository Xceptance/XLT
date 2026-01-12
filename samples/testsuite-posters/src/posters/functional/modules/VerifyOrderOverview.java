package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

/**
 * <p>Verifies that a product is in the order in a specified count.</p>
 */
public class VerifyOrderOverview
{

    /**
     * <p>Verifies that a product is in the order in a specified count.</p>
     *
     * @param index
     * @param productName
     * @param productCount
     * @param productFinish
     * @param productSize
     */
    public static void execute(String index, String productName, String productCount, String productFinish, String productSize)
    {
        // resolve any placeholder in the parameters
        index = resolve(index);
        productName = resolve(productName);
        productCount = resolve(productCount);
        productFinish = resolve(productFinish);
        productSize = resolve(productSize);
        assertElementPresent("id=titleOrderOverview");
        assertElementPresent("//table[@id='checkoutOverviewTable']/tbody/tr[last()]");
        // div[@class=pName]/
        assertText("//tr[last()]/td[2]/div/div[@class='pName font-bold']", productName);
        assertText("//tr[last()]/td[@class='pCount']", productCount);
        assertText("//tr[last()]/td[2]/div/div[3]/ul/li[1]/span[@class='pStyle']", productFinish);
        assertText("//tr[last()]/td[2]/div/div[3]/ul/li[2]/span[@class='pSize']", productSize);

    }
}