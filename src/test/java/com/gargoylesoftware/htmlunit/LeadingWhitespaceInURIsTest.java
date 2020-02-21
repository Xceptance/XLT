package com.gargoylesoftware.htmlunit;

import java.net.URL;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

/**
 * Tests whether URIs with leading whitespace are handled correctly.
 * 
 * @see https://sourceforge.net/p/htmlunit/bugs/627/
 * @see https://sourceforge.net/p/htmlunit/bugs/1728/
 * @see https://lab.xceptance.de/issues/2547
 */
public class LeadingWhitespaceInURIsTest
{
    @Test
    public void test() throws Exception
    {
        final String html = "<html><head>" + "<base href='\nhttp://localhost/'>"
                            + "<script type='text/javascript' src='\nhttp://localhost/script.js'></script>"
                            + "<script type='text/javascript' src='\nscript.js'></script>" + "</head><body>foo</body></html>";
        final String js = "";

        // setup
        BasicConfigurator.configure();

        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(true);

            final MockWebConnection connection = new MockWebConnection();
            webClient.setWebConnection(connection);

            connection.setResponse(new URL("http://localhost/index.html"), html);
            connection.setResponse(new URL("http://localhost/script.js"), js);

            // test
            webClient.getPage("http://localhost/index.html");
        }
    }
}
