package com.gargoylesoftware.htmlunit;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

/**
 * @see https://lab.xceptance.de/issues/2134
 * @see https://sourceforge.net/p/htmlunit/bugs/1619/
 */
public class _2134_UnexpectedPostParametersTest
{
    @Test
    public void test() throws Exception
    {
        try (WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            // load the test page
            HtmlPage htmlPage = webClient.getPage(getClass().getResource(getClass().getSimpleName() + ".html"));
            System.err.println(htmlPage.asXml());

            // check the web request that would have been sent when the form is submitted
            HtmlForm form = htmlPage.getFormByName("form1");
            HtmlInput submit = form.getInputByName("submit1");

            WebRequest webRequest = form.getWebRequest(submit);

            List<NameValuePair> postParams = webRequest.getRequestParameters();
            for (NameValuePair postParam : postParams)
            {
                System.err.println(postParam);
            }

            Assert.assertEquals("Unexpected number of POST parameters:", 2, postParams.size());
        }
    }
}
