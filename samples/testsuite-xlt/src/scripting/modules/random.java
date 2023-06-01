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
package scripting.modules;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;
import scripting.modules.Random_ModuleWithParam;

/**
 * TODO: Add class description
 */
public class random extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        //
        // ~~~ random_alphanumeric_string ~~~
        //
        startAction("random_alphanumeric_string");
        type("id=in_txt_1", "${RANDOM.String(5)}");
        assertText("id=cc_keyup", "regexp:keyup \\(in_txt_1\\) [a-z]{5}");
        //
        // ~~~ random_number ~~~
        //
        startAction("random_number");
        type("id=in_txt_1", "${RANDOM.Number(10,99)}");
        assertText("id=cc_keyup", "regexp:keyup \\(in_txt_1\\) \\d{2}");
        type("id=in_txt_1", "${RANDOM.Number(9)}");
        assertText("id=cc_keyup", "regexp:keyup \\(in_txt_1\\) \\d{1}");
        //
        // ~~~ timestamp ~~~
        //
        startAction("timestamp");
        type("id=in_txt_1", "${NOW}");
        assertText("id=cc_keyup", "regexp:keyup \\(in_txt_1\\) \\d{13}");
        //
        // ~~~ double ~~~
        //
        startAction("double");
        type("id=in_txt_2", "${RANDOM.String(8)}");
        assertNotText("id=in_txt_2", "exact:${RANDOM.String(8)}");
        //
        // ~~~ randomParam ~~~
        //
        startAction("randomParam");
        final Random_ModuleWithParam _random_ModuleWithParam = new Random_ModuleWithParam();
        _random_ModuleWithParam.execute("${RANDOM.Number(0,5)}");

        //
        // ~~~ randomParamWithPlaceholder ~~~
        //
        startAction("randomParamWithPlaceholder");
        final Random_ModuleWithParam _random_ModuleWithParam0 = new Random_ModuleWithParam();
        _random_ModuleWithParam0.execute("${RANDOM.Number(0,${myCount})}");


    }
}