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
package scripting.placeholders.overrideTestdata;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;
import scripting.placeholders.overrideTestdata.Mod_2c;
import scripting.placeholders.overrideTestdata.Mod_2b;
import scripting.placeholders.overrideTestdata.Mod_2a;
import scripting.placeholders.overrideTestdata.Mod_3;

/**
 * <p>Override test data in (sub) modules that use and define the test data themself.</p>
 */
public class TOverrideTestData extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TOverrideTestData()
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

        assertText("id=specialchar_1", "${gtd1}");
        // reset input for further testing
        type("id=in_txt_1", "${t1} - 0");
        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 0");
        final Mod_2c _mod_2c = new Mod_2c();
        _mod_2c.execute();

        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 3");
        final Mod_2b _mod_2b = new Mod_2b();
        _mod_2b.execute();

        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 2");
        final Mod_2a _mod_2a = new Mod_2a();
        _mod_2a.execute();

        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 1");
        final Mod_3 _mod_3 = new Mod_3();
        _mod_3.execute();


    }

}