/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
public class assertNotSelected extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final String selectLocator = resolve(parameters[0]);
        final String optionLocator = resolve(parameters[1]);
        final String index = resolve(parameters[2]);
        assertNotSelectedId(selectLocator, optionLocator);
        assertNotSelectedIndex(selectLocator, index);
        assertNotSelectedLabel(selectLocator, optionLocator);
        assertNotSelectedValue(selectLocator, optionLocator);

    }
}