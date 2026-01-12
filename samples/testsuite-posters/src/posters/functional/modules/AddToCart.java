package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

import posters.functional.modules.Browse;

/**
 * <p>Browses to a product of a category and add this product to the cart.</p>
 */
public class AddToCart
{

    /**
     * <p>Browses to a product of a category and add this product to the cart.</p>
     *
     */
    public static void execute()
    {
        Browse.execute();

        //
        // ~~~ AddToCart ~~~
        //
        startAction("AddToCart");
        // style
        storeXpathCount("//div[@id='selectStyle']/div[@class='radio']", "finishCount");
        store("${RANDOM.Number(${finishCount})}", "finishIndex");
        check("//div[@id='selectStyle']/div[@class='radio'][${finishIndex}+1]/label/input");
        storeText("//div[@id='selectStyle']/div[@class='radio'][${finishIndex}+1]/label", "productFinish");
        // size
        storeXpathCount("//select[@id='selectSize']/option", "availableSizeCount");
        store("${RANDOM.Number(${availableSizeCount})}", "availableSizeIndex");
        select("id=selectSize", "index=${availableSizeIndex}");
        storeText("//select[@id='selectSize']/option[${availableSizeIndex}+1]", "productSize");
        //
        // ~~~ AddProductToCart ~~~
        //
        startAction("AddProductToCart");
        click("id=btnAddToCart");
        storeText("id=prodPrice", "productPrice");

    }
}