package com.gargoylesoftware.htmlunit;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;

/**
 * See http://sourceforge.net/tracker/index.php?func=detail&aid=3534330&group_id=47038&atid=448266 and #1600.
 */
public class _1600_OpacityValueTest
{
    @Test
    public void ff45() throws Exception
    {
        test(new WebClient(BrowserVersion.FIREFOX_60), "0.55");
    }

    @Test
    public void ie() throws Exception
    {
        test(new WebClient(BrowserVersion.INTERNET_EXPLORER), "0.55");
    }

    @Test
    public void chrome() throws Exception
    {
        test(new WebClient(BrowserVersion.CHROME), "0.55");
    }

    private void test(final WebClient webClient, final String expectedOpacityValue) throws Exception
    {
        // setup
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(true);

        final List<String> alerts = new ArrayList<String>();
        webClient.setAlertHandler(new CollectingAlertHandler(alerts));

        // get the page
        final URL url = getClass().getResource(getClass().getSimpleName() + ".html");
        final HtmlPage htmlPage = webClient.getPage(url);

        // test
        final HtmlElement e = htmlPage.getHtmlElementById("div");
        final String opacity = ((HTMLElement) e.getScriptableObject()).getStyle().getOpacity();

        Assert.assertEquals(expectedOpacityValue, opacity);
    }
}
