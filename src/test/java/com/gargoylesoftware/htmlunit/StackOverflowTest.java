package com.gargoylesoftware.htmlunit;

import java.net.URL;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

/**
 * See https://sourceforge.net/tracker/?func=detail&atid=448266&aid=2089341&group_id=47038 for more details.
 */

public class StackOverflowTest
{
    @Test
    public void test() throws Exception
    {
        // setup
        BasicConfigurator.configure();

        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(true);

            final URL url = getClass().getResource(getClass().getSimpleName() + "_test.html");

            // test (throws StackOverflowError until fixed)
            webClient.getPage(url);
        }
    }
}
