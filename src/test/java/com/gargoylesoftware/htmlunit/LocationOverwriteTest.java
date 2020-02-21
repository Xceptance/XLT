package com.gargoylesoftware.htmlunit;

import java.net.URL;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * This test case tries to provoke an overwrite of the page by the previous page when using javascript and the location
 * feature.
 * 
 * @author Rene Schwietzke
 */
public class LocationOverwriteTest
{
    @Test
    public void overwrite() throws Exception
    {
        BasicConfigurator.configure();
        final String p1 = "<html>"
                          + "<head>"
                          + "<title>1</title>"
                          + "<script>"
                          + "function changeLocation()"
                          + "{ "
                          + "window.aMenu = \"location='valid.html'\";"
                          + "eval(window.aMenu);"
                          + "}"
                          + "</script>"
                          + "</head>"
                          + "<body>"
                          + "<script>document.write('<a href=\"http://myserver/wrong.html\" onclick=\"changeLocation(); return false;\" id=\"test\">');</script>"
                          + "</body>" + "</html>";

        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            final MockWebConnection conn = new MockWebConnection();
            webClient.setWebConnection(conn);

            conn.setResponse(new URL("http://myserver/1.html"), p1);
            conn.setResponseAsGenericHtml(new URL("http://myserver/valid.html"), "valid");
            conn.setResponseAsGenericHtml(new URL("http://myserver/wrong.html"), "wrong");

            final HtmlPage initialPage = (HtmlPage) webClient.getPage("http://myserver/1.html");
            final List<?> testLinks = initialPage.getByXPath("id('test')");

            Assert.assertNotNull(testLinks);
            Assert.assertEquals(1, testLinks.size());

            final HtmlElement link = (HtmlElement) testLinks.get(0);
            final HtmlPage targetPage = (HtmlPage) link.click();

            Assert.assertEquals("valid", targetPage.getTitleText());
        }
    }
}
