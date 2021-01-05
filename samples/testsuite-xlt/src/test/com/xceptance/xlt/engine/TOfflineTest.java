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
package test.com.xceptance.xlt.engine;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.HtmlPageUtils;
import com.xceptance.xlt.api.util.XltProperties;

/**
 * Dummy test used for offline testing.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 * 
 */
public class TOfflineTest extends AbstractTestCase
{

    private String offlineSetting;

    @Before
    public void init()
    {
        final XltProperties pros = XltProperties.getInstance();
        offlineSetting = pros.getProperty("com.xceptance.xlt.http.offline", "false");
        XltProperties.getInstance().setProperty("com.xceptance.xlt.http.offline", "true");
    }

    @After
    public void reset()
    {
        XltProperties.getInstance().setProperty("com.xceptance.xlt.http.offline", offlineSetting);
    }

    @Test
    public void test() throws Throwable
    {
        final String url = getProperty("url", null);
        final String xpath = getProperty("xpath", null);
        final String text = getProperty("text", null);

        final AbstractHtmlPageAction action = new AbstractHtmlPageAction("GotoURL")
        {

            @Override
            public void preValidate() throws Exception
            {
                Assert.assertFalse(StringUtils.isEmpty(url));
                Assert.assertFalse(StringUtils.isEmpty(xpath));
                Assert.assertFalse(StringUtils.isEmpty(text));
            }

            @Override
            protected void postValidate() throws Exception
            {
                final HtmlPage page = getHtmlPage();
                Assert.assertNotNull("Failed to load page", page);

                final HtmlElement element = HtmlPageUtils.findSingleHtmlElementByXPath(page, xpath);
                Assert.assertEquals(element.getTextContent().trim(), text);
            }

            @Override
            protected void execute() throws Exception
            {
                loadPage(url);
            }
        };
        action.run();
    }
}
