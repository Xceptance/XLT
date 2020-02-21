package com.gargoylesoftware.htmlunit;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * This test shows that reapplying the innerHTML attribute's value might sometimes lead to erroneous HTML.
 * https://sourceforge.net/tracker/index.php?func=detail&aid=2262599&group_id=47038&atid=448266
 */

public class ReapplyingInnerHtmlAttributeTest
{
    @Test
    public void test() throws Exception
    {
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            // setup
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(true);

            final List<String> alerts = new ArrayList<String>();
            webClient.setAlertHandler(new CollectingAlertHandler(alerts));

            // get the page
            final URL url = getClass().getResource(getClass().getSimpleName() + "_test.html");
            final HtmlPage page = (HtmlPage) webClient.getPage(url);

            System.out.println(page.asXml());

            // check whether the expected values are returned
            Assert.assertEquals("", alerts.get(0));
        }
    }
}
