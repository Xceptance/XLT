package com.gargoylesoftware.htmlunit;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * This test shows that the attribute values of HTML elements, which have been cloned via JavaScript, are always empty
 * on JavaScript level. On DOM level, however, they are available as expected.
 */

public class AttributeValuesLostAfterCloningElementTest
{
    @Test
    public void test() throws Exception
    {
        // setup
        BasicConfigurator.configure();

        try (final WebClient webClient = new WebClient())
        {
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(true);

            final List<String> alerts = new ArrayList<String>();
            webClient.setAlertHandler(new CollectingAlertHandler(alerts));

            // clone a div via JS
            final URL url = getClass().getResource(getClass().getSimpleName() + "_test.html");
            final HtmlPage page = (HtmlPage) webClient.getPage(url);

            // System.out.println(page.asXml());

            // check whether the attribute is available on DOM level
            final HtmlElement element = page.getHtmlElementById("bar");
            final String value = element.getAttribute("id");
            Assert.assertEquals("bar", value);

            // check whether the attribute was available to JavaScript code
            Assert.assertEquals("id = bar", alerts.get(0));
        }
    }
}
