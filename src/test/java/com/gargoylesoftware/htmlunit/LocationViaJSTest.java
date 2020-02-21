package com.gargoylesoftware.htmlunit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Test the problem to get a new location with JS and a background thread. It seems that the context is missing for the
 * browserversion.
 * 
 * @author Rene Schwietzke
 */
public class LocationViaJSTest
{
    protected String getContent(final String resourceName) throws IOException
    {
        InputStream in = null;

        try
        {
            in = getClass().getClassLoader().getResourceAsStream("com/gargoylesoftware/htmlunit/" + resourceName);

            return IOUtils.toString(in, StandardCharsets.UTF_8);
        }
        finally
        {
            in.close();
        }
    }

    @Test
    public void overwrite() throws Exception
    {
        BasicConfigurator.configure();
        final String p1 = "<html>" + "<head>" + "   <title>1</title>"
                          + "   <script src=\"prototype-1.6.0.2.js\" type=\"text/javascript\"></script>" + "</head>" + "<body>"
                          + "<script>" + "   setTimeout('finishCreateAccount()', 4000);" + "   function finishCreateAccount() {"
                          + "       completionUrl = \"/2.html\";" + "       document.location.replace(completionUrl);" + "   }"
                          + "</script>" + "" + "<div><a href=\"link.html\" id=\"test\"></div>" + "</body>" + "</html>";
        final String p2 = "<html>" + "<head>" + "   <title>2</title>"
                          + "   <script src=\"prototype-1.6.0.2.js\" type=\"text/javascript\"></script>" + "</head>" + "<body>"
                          + "<div id=\"id2\">Page2</div>" + "</body>" + "</html>";

        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            final MockWebConnection conn = new MockWebConnection();
            wc.setWebConnection(conn);

            conn.setResponse(new URL("http://myserver/1.html"), p1);
            conn.setResponse(new URL("http://myserver/2.html"), p2);
            conn.setResponse(new URL("http://myserver/prototype-1.6.0.2.js"), getContent("prototype-1.6.0.2.js"));

            final HtmlPage initialPage = (HtmlPage) wc.getPage("http://myserver/1.html");
            initialPage.getEnclosingWindow().getJobManager().waitForJobs(10000);
        }
    }
}
