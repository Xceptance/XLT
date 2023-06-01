/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package scripting.testcases;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class storeEval extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public storeEval()
    {
        super("http://localhost:8080");
    }


    /**
     * Executes the test.
     *
     * @throws Throwable if anything went wrong
     */
    @Test
    public void test() throws Throwable
    {
        //
        // ~~~ OpenStartPage ~~~
        //
        startAction("OpenStartPage");
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        storeEval("(function(){\nvar e = document.getElementById('specialchar_1');\nreturn e != null ? e.textContent : '';\n})();", "storeEval_1");
        assertText("id=specialchar_1", "${storeEval_1}");
        assertNotText("id=specialchar_2", "${storeEval_1}");
        storeText("css=#priceText > span", "price");
        storeEval("'${price}'.replace(/([\\/\\\\^$*+?.()|[\\]{}])/g, '\\\\$1')", "priceRex");
        assertText("id=priceText", "regexpi:.*${priceRex}");

    }

}