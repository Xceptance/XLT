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

/**
 * TODO: Add class description
 */
public class Select_easy extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String optionLocator = resolve(parameters[0]);
        //
        // ~~~ letters_only ~~~
        //
        startAction("letters_only");
        select("id=select_1", optionLocator + "=select_1_b");
        assertText("id=cc_change", "change (select_1) select_1_b");
        //
        // ~~~ with_whitespace ~~~
        //
        startAction("with_whitespace");
        select("id=select_14", optionLocator + "=select_14 b");
        assertText("id=cc_change", "change (select_14) select_14 b");
        //
        // ~~~ double ~~~
        //
        startAction("double");
        select("id=select_1", optionLocator + "=select_1_c");
        select("id=select_1", optionLocator + "=select_1_c");
        assertText("id=cc_change", "change (select_1) select_1_c");
        select("id=select_14", optionLocator + "=select_14 b");
        assertText("id=cc_change", "change (select_1) select_1_c");

    }
}