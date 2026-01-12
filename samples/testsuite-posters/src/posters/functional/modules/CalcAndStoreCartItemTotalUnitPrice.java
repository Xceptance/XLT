package posters.functional.modules;
import static com.xceptance.xlt.api.engine.scripting.StaticScriptCommands.*;

/**
 * <p>Store a unit price and then calculate the total price (Quantity &#42; Unit-Price)</p>
 */
public class CalcAndStoreCartItemTotalUnitPrice
{

    /**
     * <p>Store a unit price and then calculate the total price (Quantity &#42; Unit-Price)</p>
     *
     * @param index
     * @param currency
     * @param prodPrice
     * @param subOrderPrice_varDynamic
     */
    public static void execute(String index, String currency, String prodPrice, String subOrderPrice_varDynamic)
    {
        // resolve any placeholder in the parameters
        index = resolve(index);
        currency = resolve(currency);
        prodPrice = resolve(prodPrice);
        subOrderPrice_varDynamic = resolve(subOrderPrice_varDynamic);
        storeText("css=#product" + index + " td .unitPriceShort", "unitPriceShort_varDynamic");
        storeText("css=#productCount" + index, "quantity_varDynamic");
        storeEval(" \"$\"+(Math.round((${unitPriceShort_varDynamic} * ${quantity_varDynamic}) * 100 ) / 100).toFixed(2)", subOrderPrice_varDynamic);

    }
}