package com.gargoylesoftware.htmlunit;

import java.net.URL;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.BaseFrameElement;
import com.gargoylesoftware.htmlunit.html.FrameWindow;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * See https://sourceforge.net/tracker/?func=detail&atid=448266&aid=3083847&group_id=47038 and #1034.
 */
public class FrameWindowsFrameElementInconsistentTest
{
    @Test
    public void test() throws Exception
    {
        BasicConfigurator.configure();

        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            // setup
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(true);

            // get the page
            final URL url = getClass().getResource(getClass().getSimpleName() + ".html");
            final HtmlPage page = webClient.getPage(url);

            // System.out.println(page.asXml());

            // test
            final BaseFrameElement frameElement = page.getHtmlElementById("iframe");
            final BaseFrameElement frameElement2 = ((FrameWindow) frameElement.getEnclosedWindow()).getFrameElement();

            // System.out.println(frameElement);
            // System.out.println(frameElement.getParentNode());
            //
            // System.out.println(frameElement2);
            // System.out.println(frameElement2.getParentNode());

            Assert.assertEquals(frameElement.hashCode(), frameElement2.hashCode());
        }
    }
}
