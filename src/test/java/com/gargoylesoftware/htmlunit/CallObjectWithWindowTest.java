package com.gargoylesoftware.htmlunit;

import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

import org.junit.Test;

/**
 * Tests the correct handling of trying to set parent scope of a window object to itself. In this case,
 * {@link ScriptableObject#getTopLevelScope(net.sourceforge.htmlunit.corejs.javascript.Scriptable)} would hang in an
 * infinite loop.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class CallObjectWithWindowTest
{

    @Test(timeout = 5000)
    public void testCallObjectWithWindow() throws Throwable
    {
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            webClient.getOptions().setJavaScriptEnabled(true);

            final String html = "<html><head><title>Test Page</title></head><body>" + "<script type=\"application/javascript\">"
                                + "Object(window)" + "</script>" + "</body></html>";

            final MockWebConnection webConnection = new MockWebConnection();
            webClient.setWebConnection(webConnection);
            webConnection.setDefaultResponse(html);

            webClient.getPage("http://www.example.org");
        }
    }
}
