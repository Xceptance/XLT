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

import posters.functional.modules.OpenCartOverview;

/**
 * <p>Verifies that a product is in the cart in a specified count.</p>
 */
public class VerifyCartItem
{

    /**
     * <p>Verifies that a product is in the cart in a specified count.</p>
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
        //
        // ~~~ goToCartOverview ~~~
        //
        startAction("goToCartOverview");
        OpenCartOverview.execute();

        // validate selected product
        assertElementPresent("css=#product" + index);
        assertText("css=#product" + index + " .productName", productName);
        assertValue("css=#product" + index + " .productCount", productCount);
        assertText("css=#product" + index + " .productStyle", productFinish);
        assertText("css=#product" + index + " .productSize", productSize);

    }
}