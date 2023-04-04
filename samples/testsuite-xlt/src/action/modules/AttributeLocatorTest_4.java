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

import org.htmlunit.html.HtmlPage;

import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitCommandsModule;


/**
 * attribute name as parameter
 */
public class AttributeLocatorTest_4 extends AbstractHtmlUnitCommandsModule
{

    /**
     * The 'attribute' parameter.
     */
    private final String attribute;


    /**
     * Constructor.
     * @param attribute The 'attribute' parameter.
     * 
     */
    public AttributeLocatorTest_4(final String attribute)
    {
        this.attribute = attribute;
    }


    /**
     * @{inheritDoc}
     */
    protected HtmlPage execute(final HtmlPage page) throws Exception
    {
        HtmlPage resultingPage = page;
        assertAttribute("xpath=id('ws8_a')/input[1]@" + attribute, "foobar");

        return resultingPage;
    }
}