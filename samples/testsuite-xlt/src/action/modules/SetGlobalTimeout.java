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
package action.modules;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * TODO: Add class description
 */
public class SetGlobalTimeout extends AbstractHtmlUnitCommandsModule
{

    /**
     * The 'timeout' parameter.
     */
    private final String timeout;


    /**
     * Constructor.
     * @param timeout The 'timeout' parameter.
     * 
     */
    public SetGlobalTimeout(final String timeout)
    {
        this.timeout = timeout;
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        resultingPage = type("id=timeout_field", timeout);
        resultingPage = click("xpath=/html/body/ul/li[3]/a");

        return resultingPage;
    }
}