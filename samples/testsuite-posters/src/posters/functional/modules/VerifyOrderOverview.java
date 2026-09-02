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