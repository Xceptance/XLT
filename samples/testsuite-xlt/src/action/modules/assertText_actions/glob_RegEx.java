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
package action.modules.assertText_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import org.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class glob_RegEx extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public glob_RegEx(final AbstractHtmlPageAction prevAction)
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
        assertText("id=specialchar_1", "Lorem ipsum * dolor sit amet, consectetuer adipiscing elit.");
        assertText("id=specialchar_1", "Lorem ipsum ??? dolor sit amet, consectetuer adipiscing elit.");
        assertText("id=specialchar_1", "regexp:Lorem ipsum [XYZ]{3} dolor sit amet, consectetuer adipiscing elit.");
        assertText("id=specialchar_1", "regexpi:lorem IPSUM [XYZ]{3} dolor SIT amet, consectetueR adipiscinG elit.");
        assertText("id=specialchar_1", "regexp:^.* [XYZ]{3} .*$");
        assertText("id=specialchar_1", "regexpi:^.* [xyz]{3} .*$");
        assertText("id=specialchar_1", "exact:Lorem ipsum XYZ dolor sit amet, consectetuer adipiscing elit.");
        assertText("id=specialchar_1", "glob:Lorem ipsum ??? dolor sit amet, consectetuer adipiscing elit.");
        assertText("id=sc_s2_2", "glob:*:*");

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

        assertText("id=specialchar_1", "Lorem ipsum * dolor sit amet, consectetuer adipiscing elit.");
        assertText("id=specialchar_1", "Lorem ipsum ??? dolor sit amet, consectetuer adipiscing elit.");
        assertText("id=specialchar_1", "regexp:Lorem ipsum [XYZ]{3} dolor sit amet, consectetuer adipiscing elit.");
        assertText("id=specialchar_1", "regexpi:lorem IPSUM [XYZ]{3} dolor SIT amet, consectetueR adipiscinG elit.");
        assertText("id=specialchar_1", "regexp:^.* [XYZ]{3} .*$");
        assertText("id=specialchar_1", "regexpi:^.* [xyz]{3} .*$");
        assertText("id=specialchar_1", "exact:Lorem ipsum XYZ dolor sit amet, consectetuer adipiscing elit.");
        assertText("id=specialchar_1", "glob:Lorem ipsum ??? dolor sit amet, consectetuer adipiscing elit.");
        assertText("id=sc_s2_2", "glob:*:*");

    }
}