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
package action.testcases.assertAttribute_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import org.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class matching_strategies extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public matching_strategies(final AbstractHtmlPageAction prevAction)
    {
        super(prevAction);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void preValidate() throws Exception
    {
        final HtmlPage page = getPreviousAction().getHtmlPage();
        Assert.assertNotNull("Failed to get page from previous action", page);
        // exact
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "exact:foobar");
        // explicit glob
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "glob:foo*");
        // regexp
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "regexp:fo{2}ba\\w");
        // regexp
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "regexp:.+");
        // regexpi
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "regexpi:fo{2}BA\\w");
        // empty attribute value
        assertAttribute("xpath=id('in_txt_13')@value", "");
        // special chars: keyspace characters
        assertAttribute("xpath=id('special_char_set4_1')@value", "glob:1234567890 qwertzuiop asdfghjkl yxcvbnm QWERTZUIOP ASDFGHJKL YXCVBNM äöü ÄÖÜ ß !\"§$%&/()=? `´^° <> ,;.:-_ #'+*²³{[]}\\ @€~ |µ ©«» ¼×");
        // hidden element
        assertAttribute("xpath=id('special_char_set4_2')@value", "special_char_set4_2");

    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void execute() throws Exception
    {
        HtmlPage page = getPreviousAction().getHtmlPage();

        setHtmlPage(page);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void postValidate() throws Exception
    {
        final HtmlPage page = getHtmlPage();
        Assert.assertNotNull("Failed to load page", page);

        // exact
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "exact:foobar");
        // explicit glob
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "glob:foo*");
        // regexp
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "regexp:fo{2}ba\\w");
        // regexp
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "regexp:.+");
        // regexpi
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "regexpi:fo{2}BA\\w");
        // empty attribute value
        assertAttribute("xpath=id('in_txt_13')@value", "");
        // special chars: keyspace characters
        assertAttribute("xpath=id('special_char_set4_1')@value", "glob:1234567890 qwertzuiop asdfghjkl yxcvbnm QWERTZUIOP ASDFGHJKL YXCVBNM äöü ÄÖÜ ß !\"§$%&/()=? `´^° <> ,;.:-_ #'+*²³{[]}\\ @€~ |µ ©«» ¼×");
        // hidden element
        assertAttribute("xpath=id('special_char_set4_2')@value", "special_char_set4_2");

    }
}