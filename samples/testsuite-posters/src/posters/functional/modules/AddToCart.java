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