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
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.placeholders.injectTestdata.Mod_1c;

/**
 * <p>Use test data but do not define them.</p>
 */
public class Mod_1b extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Mod_1c _mod_1c = new Mod_1c();
        _mod_1c.execute();

        // assert reset
        assertText("id=cc_keyup", "keyup (in_txt_1) fromTestcase - 3");
        assertText("id=specialchar_1", "${gtd2}");
        type("id=in_txt_1", "${t1}  - 2");

    }
}