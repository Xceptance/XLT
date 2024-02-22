/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
public class MultiSelection_easy extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String optionLocator = resolve(parameters[0]);
        //
        // ~~~ lettersOnly_add ~~~
        //
        startAction("lettersOnly_add");
        addSelection("name=select_9", optionLocator + "=select_9_b");
        assertText("id=cc_change", "change (select_9) select_9_b");
        addSelection("name=select_9", optionLocator + "=select_9_c");
        assertText("id=cc_change", "change (select_9) select_9_b, select_9_c");
        //
        // ~~~ lettersOnly_remove ~~~
        //
        startAction("lettersOnly_remove");
        removeSelection("name=select_9", optionLocator + "=select_9_b");
        assertText("id=cc_change", "change (select_9) select_9_c");
        removeSelection("name=select_9", optionLocator + "=select_9_c");
        assertText("id=cc_change", "change (select_9)");
        //
        // ~~~ withWhitespace_add ~~~
        //
        startAction("withWhitespace_add");
        addSelection("xpath=//select[@id='select_16']", optionLocator + "=select_16 a");
        assertText("id=cc_change", "change (select_16) select_16 a");
        addSelection("xpath=//select[@id='select_16']", optionLocator + "=select_16 b");
        assertText("id=cc_change", "change (select_16) select_16 a, select_16 b");
        addSelection("xpath=//select[@id='select_16']", optionLocator + "=select_16 c");
        assertText("id=cc_change", "change (select_16) select_16 a, select_16 b, select_16 c");
        //
        // ~~~ withWhitespace_remove ~~~
        //
        startAction("withWhitespace_remove");
        removeSelection("xpath=//select[@id='select_16']", optionLocator + "=select_16 a");
        assertText("id=cc_change", "change (select_16) select_16 b, select_16 c");
        removeSelection("xpath=//select[@id='select_16']", optionLocator + "=select_16 b");
        assertText("id=cc_change", "change (select_16) select_16 c");
        removeSelection("xpath=//select[@id='select_16']", optionLocator + "=select_16 c");
        assertText("id=cc_change", "change (select_16)");
        //
        // ~~~ doubleSelect ~~~
        //
        startAction("doubleSelect");
        addSelection("id=select_9", optionLocator + "=select_9_b");
        assertText("id=cc_change", "change (select_9) select_9_b");
        addSelection("id=select_9", optionLocator + "=select_9_b");
        assertText("id=cc_change", "change (select_9) select_9_b");
        //
        // ~~~ doubleSelect_cleanup ~~~
        //
        startAction("doubleSelect_cleanup");
        removeSelection("id=select_9", optionLocator + "=select_9_b");
        assertText("id=cc_change", "change (select_9)");
        //
        // ~~~ removeUnselected ~~~
        //
        startAction("removeUnselected");
        removeSelection("id=select_9", optionLocator + "=select_9_b");
        assertText("id=cc_change", "change (select_9)");

    }
}