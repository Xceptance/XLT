package com.gargoylesoftware.htmlunit;

import java.io.IOException;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class _2912_InvalidNumericCharacterReferenceTest
{
    // The offending text. Should actually be "Nimbus&#8482; 3000 is great".
    private static final String text = "Nimbus&#84823000 is great";

    @Test
    public void invalidCharacterReferenceInAttributeValue() throws FailingHttpStatusCodeException, IOException
    {
        String pageContent = "<p data-desc='" + text + "'></p>";

        test(pageContent);
    }

    @Test
    public void invalidCharacterReferenceInElementBody() throws FailingHttpStatusCodeException, IOException
    {
        String pageContent = "<p>" + text + "</p>";

        test(pageContent);
    }

    private void test(String pageContent) throws FailingHttpStatusCodeException, IOException
    {
        MockWebConnection conn = new MockWebConnection();
        conn.setDefaultResponse(pageContent);

        try (WebClient wc = new WebClient())
        {
            wc.setWebConnection(conn);

            HtmlPage page = wc.getPage("http://dummyhost/");

            System.out.println(page.asXml());
        }
    }
}
