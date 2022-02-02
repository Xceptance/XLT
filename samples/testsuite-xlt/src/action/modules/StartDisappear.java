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
package action.modules;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;

import action.modules.SetGlobalTimeout;

/**
 * TODO: Add class description
 */
public class StartDisappear extends AbstractHtmlUnitCommandsModule
{

    /**
     * The 'delay' parameter.
     */
    private final String delay;


    /**
     * Constructor.
     * @param delay The 'delay' parameter.
     * 
     */
    public StartDisappear(final String delay)
    {
        this.delay = delay;
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        final SetGlobalTimeout setGlobalTimeout = new SetGlobalTimeout(delay);
        resultingPage = setGlobalTimeout.run(resultingPage);

        resultingPage = click("xpath=id('disappear')/input[@value='disappear (auto)' and @type='submit']");

        return resultingPage;
    }
}