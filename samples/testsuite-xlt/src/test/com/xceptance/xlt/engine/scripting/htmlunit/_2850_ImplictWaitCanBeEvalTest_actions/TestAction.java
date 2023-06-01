/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package test.com.xceptance.xlt.engine.scripting.htmlunit._2850_ImplictWaitCanBeEvalTest_actions;

import java.net.URL;
import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.engine.scripting.AbstractHtmlUnitScriptAction;
import com.xceptance.xlt.engine.util.TimerUtils;
import org.htmlunit.html.HtmlPage;

public class TestAction extends AbstractHtmlUnitScriptAction
{
    /**
     * Start URL as string.
     */
    private final String urlString;

    /**
     * Start URL as URL object.
     */
    private URL url;

    /**
     * Constructor.
     *
     * @param prevAction
     *            The previous action.
     * @param urlString
     *            The start URL as string.
     */
    public TestAction(final AbstractHtmlPageAction prevAction, final String urlString)
    {
        super(prevAction);
        this.urlString = urlString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preValidate() throws Exception
    {
        final String baseURL = getBaseUrl();
        if (baseURL != null && baseURL.trim().length() > 0)
        {
            url = new URL(new URL(baseURL), urlString);
        }
        else
        {
            url = new URL(urlString);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void execute() throws Exception
    {
        HtmlPage page;
        page = open(url);

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

        final String locator = "css=#foorz";
        final String xpath = "id('foorz')";
        final long maxRuntime = 4000;

        long start = TimerUtils.get().getStartTime();
        {
            assertNotElementPresent(locator);
            assertNotElementCount(locator, 5);
            assertNotXpathCount(xpath, 5);
            assertElementCount(locator, 0);
            assertXpathCount(xpath, 0);

            waitForNotElementPresent(locator);
            waitForNotElementCount(locator, 5);
            waitForNotXpathCount(xpath, 5);
            waitForElementCount(locator, 0);
            waitForXpathCount(xpath, 0);
        }
        long runtime = TimerUtils.get().getElapsedTime(start);

        Assert.assertTrue(String.format("Test runtime (%d ms) exceeded maximum runtime (%d ms)", runtime, maxRuntime),
                          runtime <= maxRuntime);
    }
}
