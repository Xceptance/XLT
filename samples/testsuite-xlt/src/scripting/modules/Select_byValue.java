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
package scripting.modules;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;
import scripting.modules.Select_easy;
import scripting.modules.Select_matching;
import scripting.modules.Select_nonunique;
import scripting.modules.Select_specialChar;

/**
 * TODO: Add class description
 */
public class Select_byValue extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        final Select_easy _select_easy = new Select_easy();
        _select_easy.execute("value");

        final Select_matching _select_matching = new Select_matching();
        _select_matching.execute("value");

        final Select_nonunique _select_nonunique = new Select_nonunique();
        _select_nonunique.execute("value");

        final Select_specialChar _select_specialChar = new Select_specialChar();
        _select_specialChar.execute("value");

        select("id=select_17", "value= ");
        assertText("id=cc_change", "change (select_17) 1 space");
        select("id=select_17", "value=  ");
        assertText("id=cc_change", "change (select_17) 2 spaces");
        //
        // ~~~ multi_select ~~~
        //
        startAction("multi_select");
        select("id=select_18", "value= ");
        assertText("id=cc_change", "change (select_18) 1 space");
        select("id=select_18", "value=  ");
        assertText("id=cc_change", "change (select_18) 2 spaces");

    }
}