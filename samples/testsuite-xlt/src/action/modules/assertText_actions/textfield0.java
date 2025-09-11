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
package action.modules.assertText_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import org.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class textfield0 extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public textfield0(final AbstractHtmlPageAction prevAction)
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
        assertText("id=in_txt_1", "in_txt_1");
        assertText("id=in_txt_1", "regexp:in_[tx]{3}_1");
        assertText("id=in_txt_1", "in_t?t_1");
        assertText("id=in_txt_5", "");
        assertText("id=in_ta_1", "");
        assertText("id=in_ta_2", "in_ta_2");
        assertText("id=in_ta_2", "regexp:in_\\w+?_2");
        assertText("id=in_ta_2", "in_t?_2");

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

        assertText("id=in_txt_1", "in_txt_1");
        assertText("id=in_txt_1", "regexp:in_[tx]{3}_1");
        assertText("id=in_txt_1", "in_t?t_1");
        assertText("id=in_txt_5", "");
        assertText("id=in_ta_1", "");
        assertText("id=in_ta_2", "in_ta_2");
        assertText("id=in_ta_2", "regexp:in_\\w+?_2");
        assertText("id=in_ta_2", "in_t?_2");

    }
}