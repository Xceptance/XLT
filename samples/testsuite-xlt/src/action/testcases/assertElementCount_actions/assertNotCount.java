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
package action.testcases.assertElementCount_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * TODO: Add class description
 */
public class assertNotCount extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public assertNotCount(final AbstractHtmlPageAction prevAction)
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
        assertNotElementCount("id=in_txt_1", 0);
        assertNotElementCount("xpath=id('in_txt_1')", 2);
        assertNotElementCount("css=.appear_11", 2);
        assertNotElementCount("name=in_txt_1", 2);
        assertNotElementCount("link=anc_sel1", 2);
        assertNotElementCount("dom=document.getElementById('in_txt_1')", 2);

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

        assertNotElementCount("id=in_txt_1", 0);
        assertNotElementCount("xpath=id('in_txt_1')", 2);
        assertNotElementCount("css=.appear_11", 2);
        assertNotElementCount("name=in_txt_1", 2);
        assertNotElementCount("link=anc_sel1", 2);
        assertNotElementCount("dom=document.getElementById('in_txt_1')", 2);

    }
}