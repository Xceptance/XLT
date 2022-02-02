/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package scripting.placeholders.injectTestdata;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;
import scripting.placeholders.injectTestdata.Mod_1c;
import scripting.placeholders.injectTestdata.Mod_1b;
import scripting.placeholders.injectTestdata.Mod_1a;
import scripting.modules.Mod_2;

/**
 * <p>Inject test data to module that doesn&#39;t define the test data itself (no override, just injection)</p>
 */
public class TInjectTestData extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TInjectTestData()
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
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        assertText("id=specialchar_1", "${gtd2}");
        type("id=in_txt_1", "${t1}  - 0");
        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 0");
        final Mod_1c _mod_1c = new Mod_1c();
        _mod_1c.execute();

        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 3");
        final Mod_1b _mod_1b = new Mod_1b();
        _mod_1b.execute();

        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 2");
        final Mod_1a _mod_1a = new Mod_1a();
        _mod_1a.execute();

        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 1");
        //
        // ~~~ TInjectTestData-0 ~~~
        //
        startAction("TInjectTestData_0");
        type("id=in_txt_1", "${td1} - 0");
        assertText("id=cc_keyup", "keyup (in_txt_1) fromPkgLvl2 - 0");
        assertText("id=specialchar_1", "${gtd2}");
        type("id=in_txt_1", "${td2} - 0");
        assertText("id=cc_keyup", "keyup (in_txt_1) fromPkgLvl1 - 0");
        assertText("id=specialchar_1", "${gtd2}");
        final Mod_2 _mod_2 = new Mod_2();
        _mod_2.execute();


    }

}