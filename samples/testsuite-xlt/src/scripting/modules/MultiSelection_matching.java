/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
public class MultiSelection_matching extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String optionLocator = parameters[0];
        addSelection("name=select_9", optionLocator + "=regexp:select_9_[ae]");
        assertText("id=cc_change", "change (select_9) select_9_a");
        removeSelection("name=select_9", optionLocator + "=regexp:select_9_[ae]");
        assertText("id=cc_change", "change (select_9)");

    }
}