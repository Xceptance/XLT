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
 * <p>Searches the specified term.</p>
 */
public class Search
{

    /**
     * <p>Searches the specified term.</p>
     *
     * @param searchTerm
     */
    public static void execute(String searchTerm)
    {
        // resolve any placeholder in the parameters
        searchTerm = resolve(searchTerm);
        // Cick the the search button to submit
        click("id=header-search-trigger");
        waitForElementPresent("id=header-menu-search");
        type("id=s", searchTerm);
        // Cick the the search button to submit
        clickAndWait("id=btnSearch");

    }
}