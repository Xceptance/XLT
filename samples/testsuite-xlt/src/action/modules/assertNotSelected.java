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
package action.modules;

import org.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * TODO: Add class description
 */
public class assertNotSelected extends AbstractHtmlUnitCommandsModule
{

    /**
     * The 'selectLocator' parameter.
     */
    private final String selectLocator;

    /**
     * The 'optionLocator' parameter.
     */
    private final String optionLocator;

    /**
     * The 'index' parameter.
     */
    private final String index;


    /**
     * Constructor.
     * @param selectLocator The 'selectLocator' parameter.
     * @param optionLocator The 'optionLocator' parameter.
     * @param index The 'index' parameter.
     * 
     */
    public assertNotSelected(final String selectLocator, final String optionLocator, final String index)
    {
        this.selectLocator = selectLocator;
        this.optionLocator = optionLocator;
        this.index = index;
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        assertNotSelectedId(selectLocator, optionLocator);
        assertNotSelectedIndex(selectLocator, index);
        assertNotSelectedLabel(selectLocator, optionLocator);
        assertNotSelectedValue(selectLocator, optionLocator);

        return resultingPage;
    }
}