/*
 * File: TOffline.java
 * Created on: Aug 16, 2012
 * 
 * Copyright 2012
 * Xceptance Software Technologies GmbH, Germany.
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
