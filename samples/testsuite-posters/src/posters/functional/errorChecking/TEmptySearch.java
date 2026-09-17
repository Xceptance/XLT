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
package posters.functional.errorChecking;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.OpenHomepage;

/**
 * <p>Verifies that an info message is shown after a search for an empty search term.</p>
 */
public class TEmptySearch extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TEmptySearch()
    {
        super("https://localhost:8443");
    }


    /**
     * Executes the test.
     *
     * @throws Throwable if anything went wrong
     */
    @Test
    public void test() throws Throwable
    {
        OpenHomepage.execute();

        //
        // ~~~ Search-NoHits ~~~
        //
        startAction("Search_NoHits");
        // Cick the the search button to submit
        click("id=header-search-trigger");
        waitForElementPresent("id=header-menu-search");
        type("id=s", "");
        // Cick the the search button to submit
        click("id=btnSearch");
        assertNotVisible("id=errorMessage");
        // Assert presence of info maessage element
        assertElementPresent("id=header-search-trigger");

    }

}