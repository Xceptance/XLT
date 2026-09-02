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
 * <p>Selects one category randomly.</p>
 */
public class SelectCategoryRandomly
{

    /**
     * <p>Selects one category randomly.</p>
     *
     */
    public static void execute()
    {
        //
        // ~~~ SelectCategoryRandomly ~~~
        //
        startAction("SelectCategoryRandomly");
        storeXpathCount("//div[@id='categoryMenu']/ul/li", "categoryCount");
        store("${RANDOM.Number(1,${categoryCount})}", "categoryIndex");
        storeXpathCount("//div[@id='categoryMenu']/ul/li[${categoryIndex}]/div/ul/li", "subCategoryCount");
        store("${RANDOM.Number(1,${subCategoryCount})}", "subCategoryIndex");
        mouseOver("//div[@id='categoryMenu']/ul/li[${categoryIndex}]");
        clickAndWait("//div[@id='categoryMenu']/ul/li[${categoryIndex}]/div/ul/li[${subCategoryIndex}]/a");

    }
}