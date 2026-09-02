/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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