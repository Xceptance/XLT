/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
import scripting.modules.assertNotSelected;

/**
 * TODO: Add class description
 */
public class assertSelected extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public assertSelected()
    {
        super("http://localhost:8080/");
    }


    /**
     * Executes the test.
     *
     * @throws Throwable if anything went wrong
     */
    @Test
    public void test() throws Throwable
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        //
        // ~~~ initial_unselected ~~~
        //
        startAction("initial_unselected");
        final scripting.modules.assertSelected _assertSelected = new scripting.modules.assertSelected();
        _assertSelected.execute("id=select_1", "select_1_a", "0");

        final assertNotSelected _assertNotSelected = new assertNotSelected();
        _assertNotSelected.execute("id=select_1", "select_1_b", "2");

        final assertNotSelected _assertNotSelected0 = new assertNotSelected();
        _assertNotSelected0.execute("id=select_9", "select_9_b", "2");

        //
        // ~~~ initial_preselected ~~~
        //
        startAction("initial_preselected");
        final scripting.modules.assertSelected _assertSelected0 = new scripting.modules.assertSelected();
        _assertSelected0.execute("id=select_22", "select_22_c", "2");

        final assertNotSelected _assertNotSelected1 = new assertNotSelected();
        _assertNotSelected1.execute("id=select_22", "select_22_a", "0");

        final scripting.modules.assertSelected _assertSelected1 = new scripting.modules.assertSelected();
        _assertSelected1.execute("id=select_24", "select_24_c", "2");

        final assertNotSelected _assertNotSelected2 = new assertNotSelected();
        _assertNotSelected2.execute("id=select_24", "select_24_a", "0");

        //
        // ~~~ unselect_preselected ~~~
        //
        startAction("unselect_preselected");
        select("id=select_22", "id=select_22_d");
        removeSelection("id=select_24", "id=select_24_c");
        final assertNotSelected _assertNotSelected3 = new assertNotSelected();
        _assertNotSelected3.execute("id=select_22", "select_22_c", "2");

        final assertNotSelected _assertNotSelected4 = new assertNotSelected();
        _assertNotSelected4.execute("id=select_24", "select_24_c", "2");

        //
        // ~~~ select ~~~
        //
        startAction("select");
        select("id=select_1", "id=select_1_c");
        addSelection("id=select_9", "id=select_9_b");
        addSelection("id=select_9", "id=select_9_c");
        final assertNotSelected _assertNotSelected5 = new assertNotSelected();
        _assertNotSelected5.execute("id=select_1", "select_1_a", "0");

        final scripting.modules.assertSelected _assertSelected2 = new scripting.modules.assertSelected();
        _assertSelected2.execute("id=select_1", "select_1_c", "2");

        final assertNotSelected _assertNotSelected6 = new assertNotSelected();
        _assertNotSelected6.execute("id=select_9", "select_9_a", "0");

        final scripting.modules.assertSelected _assertSelected3 = new scripting.modules.assertSelected();
        _assertSelected3.execute("id=select_9", "select_9_b", "1");

        final scripting.modules.assertSelected _assertSelected4 = new scripting.modules.assertSelected();
        _assertSelected4.execute("id=select_9", "select_9_c", "2");

        //
        // ~~~ noValueOption ~~~
        //
        startAction("noValueOption");
        assertSelectedValue("id=select_19", "select_19_a");
        //
        // ~~~ unselect_selected ~~~
        //
        startAction("unselect_selected");
        select("id=select_1", "id=select_1_d");
        removeSelection("id=select_9", "id=select_9_b");
        final assertNotSelected _assertNotSelected7 = new assertNotSelected();
        _assertNotSelected7.execute("id=select_1", "select_1_c", "2");

        final assertNotSelected _assertNotSelected8 = new assertNotSelected();
        _assertNotSelected8.execute("id=select_9", "select_9_b", "1");


    }

}