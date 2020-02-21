package com.gargoylesoftware.htmlunit;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Testcase which demonstrates issue #1130.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class XMLSerializerTest
{
    @Test
    public void test() throws Throwable
    {
        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            wc.getOptions().setJavaScriptEnabled(true);

            final String response = "<?xml version=\"1.0\" ?>\n" + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
                                    + "<head><title>TEST</title></head>\n" + "<body></body>\n" + "</html>\n";
            final MockWebConnection conn = new MockWebConnection();
            conn.setDefaultResponse(response);

            wc.setWebConnection(conn);

            final String script = "var t = document.createElement('textarea'); new XMLSerializer().serializeToString(t);";
            final HtmlPage page = wc.getPage("http://www.example.org");
            final ScriptResult result = page.executeJavaScript(script);
            Assert.assertEquals("<textarea xmlns=\"http://www.w3.org/1999/xhtml\"></textarea>", result.getJavaScriptResult());
        }
    }
}
