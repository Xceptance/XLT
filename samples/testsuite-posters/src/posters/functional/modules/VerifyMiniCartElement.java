package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

/**
 * <p>Verifies the cart slider.</p>
 */
public class VerifyMiniCartElement
{

    /**
     * <p>Verifies the cart slider.</p>
     *
     * @param index
     * @param productName
     * @param productStyle
     * @param productCount
     * @param productSize
     * @param prodTotalPrice
     */
    public static void execute(String index, String productName, String productStyle, String productCount, String productSize, String prodTotalPrice)
    {
        // resolve any placeholder in the parameters
        index = resolve(index);
        productName = resolve(productName);
        productStyle = resolve(productStyle);
        productCount = resolve(productCount);
        productSize = resolve(productSize);
        prodTotalPrice = resolve(prodTotalPrice);
        click("id=headerCartOverview");
        waitForElementPresent("id=miniCartMenu");
        // validate elements
        // css=#product@{index} .productName
        assertText("//ul[@class=\"list-unstyled cartMiniElementList\"]/li[" + index + "]/ul/li[@class=\"prodName\"]", productName);
        assertText("//ul[@class=\"list-unstyled cartMiniElementList\"]/li[" + index + "]/ul/li[2]/span[@class=\"prodStyle\"]", productStyle);
        assertText("//ul[@class=\"list-unstyled cartMiniElementList\"]/li[" + index + "]/ul/li[2]/span[@class=\"prodCount\"]", productCount);
        assertText("//ul[@class=\"list-unstyled cartMiniElementList\"]/li[" + index + "]/ul/li[3]/div[contains(@class,'prodPrice')]/strong", prodTotalPrice);
        click("id=headerCartOverview");
        mouseOut("id=headerCartOverview");
        waitForNotVisible("id=miniCartMenu");

    }
}