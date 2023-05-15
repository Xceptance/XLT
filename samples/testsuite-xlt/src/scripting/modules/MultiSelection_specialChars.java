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
public class MultiSelection_specialChars extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String optionLocator = resolve(parameters[0]);
        //
        // ~~~ add_special_chars_multiple ~~~
        //
        startAction("add_special_chars_multiple");
        addSelection("id=select_18", optionLocator + "=\\");
        assertText("id=cc_change", "change (select_18) \\");
        addSelection("id=select_18", optionLocator + "=^");
        assertText("id=cc_change", "change (select_18) \\, ^");
        addSelection("id=select_18", optionLocator + "=exact:regexp:[XYZ]{5}");
        assertText("id=cc_change", "glob:change (select_18) \\, ^, regexp:[XYZ]{5}");
        addSelection("id=select_18", optionLocator + "=:");
        assertText("id=cc_change", "glob:change (select_18) :, \\, ^, regexp:[XYZ]{5}");
        //
        // ~~~ remove_special_chars_multiple ~~~
        //
        startAction("remove_special_chars_multiple");
        removeSelection("id=select_18", optionLocator + "=\\");
        assertText("id=cc_change", "glob:change (select_18) :, ^, regexp:[XYZ]{5}");
        removeSelection("id=select_18", optionLocator + "=^");
        assertText("id=cc_change", "glob:change (select_18) :, regexp:[XYZ]{5}");
        removeSelection("id=select_18", optionLocator + "=exact:regexp:[XYZ]{5}");
        assertText("id=cc_change", "glob:change (select_18) :");
        removeSelection("id=select_18", optionLocator + "=:");
        assertText("id=cc_change", "change (select_18)");

    }
}