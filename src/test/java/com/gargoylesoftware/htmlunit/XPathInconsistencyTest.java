package com.gargoylesoftware.htmlunit;

import java.net.URL;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * This test case shows the different handling of error in case an element is not found by getByXPath()
 * 
 * @author Rene Schwietzke
 */
public class XPathInconsistencyTest
{
    @Test
    public void overwrite() throws Exception
    {
        BasicConfigurator.configure();
        final String p1 = "<html>" + "<head>" + "<title>1</title>" + "</head>" + "<body>" + "<div><a href=\"link.html\" id=\"test\"></div>"
                          + "</body>" + "</html>";

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            final MockWebConnection conn = new MockWebConnection();
            wc.setWebConnection(conn);

            conn.setResponse(new URL("http://myserver/1.html"), p1);

            final HtmlPage initialPage = (HtmlPage) wc.getPage("http://myserver/1.html");

            // return empty list
            final List<?> xPathByAttributeCompare = initialPage.getByXPath("//div[@id='doesNotExist']");
            Assert.assertTrue(xPathByAttributeCompare.isEmpty());

            // causes exception
            try
            {
                final List<?> xPathByID = initialPage.getByXPath("id('doesNotExist')");
                Assert.assertTrue(xPathByID.isEmpty());
            }
            catch (final com.gargoylesoftware.htmlunit.ElementNotFoundException e)
            {
                Assert.fail("Undesired exception. Empty list expected");
            }
        }
    }
}
