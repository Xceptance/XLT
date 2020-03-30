/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package action.modules.deleteCookie_actions;

import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import action.modules.AssertCookie;

/**
 * TODO: Add class description
 */
public class delete_non_existing extends AbstractHtmlUnitScriptAction
{

    /**
     * Constructor.
     * @param prevAction The previous action.
     */
    public delete_non_existing(final AbstractHtmlPageAction prevAction)
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

    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void execute() throws Exception
    {
        HtmlPage page = getPreviousAction().getHtmlPage();
        createCookie("testsuite-xlt=xlt-testsuite");
        final AssertCookie assertCookie = new AssertCookie("testsuite-xlt", "xlt-testsuite");
        page = assertCookie.run(page);

        deleteCookie("xyz");
        final AssertCookie assertCookie0 = new AssertCookie("testsuite-xlt", "xlt-testsuite");
        page = assertCookie0.run(page);

        deleteCookie("testsuite-xlt");
        final AssertCookie assertCookie1 = new AssertCookie("testsuite-xlt", "");
        page = assertCookie1.run(page);

        // createCookie("^°!§$%&`´|üöäÜÖÄ+*~#'-_.=^°!§$%&`´|üöäÜÖÄ+*~#'-_.");
        // final AssertCookie assertCookie2 = new AssertCookie("^°!§$%&`´|üöäÜÖÄ+*~#'-_.","^°!§$%&`´|üöäÜÖÄ+*~#'-_.");
        // page = assertCookie2.run(page);
        // deleteCookie("^°!§$%&`´|üöäÜÖÄ+*~#'-_.");
        // final AssertCookie assertCookie3 = new AssertCookie("testsuite-xlt","");
        // page = assertCookie3.run(page);

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


    }
}