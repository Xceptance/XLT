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

import action.modules.ReadCookie;

/**
 * TODO: Add class description
 */
public class AssertCookie extends AbstractHtmlUnitCommandsModule
{

    /**
     * The 'name' parameter.
     */
    private final String name;

    /**
     * The 'value' parameter.
     */
    private final String value;


    /**
     * Constructor.
     * @param name The 'name' parameter.
     * @param value The 'value' parameter.
     * 
     */
    public AssertCookie(final String name, final String value)
    {
        this.name = name;
        this.value = value;
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        final ReadCookie readCookie = new ReadCookie(name);
        resultingPage = readCookie.run(resultingPage);

        assertText("id=cookieResult", value);

        return resultingPage;
    }
}