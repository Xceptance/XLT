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
 * <p>Verifies the cart overview in the header.</p>
 */
public class VerifyCartOverview
{

    /**
     * <p>Verifies the cart overview in the header.</p>
     *
     * @param productCount
     */
    public static void execute(String productCount)
    {
        // resolve any placeholder in the parameters
        productCount = resolve(productCount);
        assertText("css=.headerCartProductCount", productCount);

    }
}