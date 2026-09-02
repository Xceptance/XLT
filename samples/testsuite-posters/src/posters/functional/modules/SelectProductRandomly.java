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