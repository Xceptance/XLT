package com.gargoylesoftware.htmlunit;

import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @see https://lab.xceptance.de/issues/1767
 * @see http://sourceforge.net/p/htmlunit/bugs/1498/
 */
public class _1767_JQueryMobileTest
{
    private HtmlPage page;

    private WebClient webClient;

    private CollectingAlertHandler alertHandler;

    @Before
    public void setup() throws Throwable
    {
        webClient = new WebClient(BrowserVersion.CHROME);
        alertHandler = new CollectingAlertHandler();
        webClient.setAlertHandler(alertHandler);

        /*
         * Get the page as it was deployed on localhost
         */
        final URL url = this.getClass().getResource(this.getClass().getSimpleName() + ".html");
        page = webClient.getPage(url);
    }

    @Test
    public void testPageInitEvent() throws Throwable
    {
        final List<String> alerts = alertHandler.getCollectedAlerts();
        Assert.assertFalse("'pageinit' event not triggered", alerts.isEmpty());
    }

    @Test
    public void testGradeA() throws Throwable
    {
        final Boolean isGradeA = (Boolean) page.executeJavaScript("jQuery.mobile.gradeA()").getJavaScriptResult();
        Assert.assertTrue("JQuery mobile grade A classification failed", isGradeA);
    }

    @Test
    public void testMatchMedia() throws Throwable
    {
        final Boolean haveMatchMedia = (Boolean) page.executeJavaScript("'matchMedia' in window").getJavaScriptResult();
        Assert.assertTrue("Browser doesn't support CSS 3 Media Queries", haveMatchMedia);
    }
}
