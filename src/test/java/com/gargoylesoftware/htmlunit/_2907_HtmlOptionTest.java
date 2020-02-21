package com.gargoylesoftware.htmlunit;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

public class _2907_HtmlOptionTest
{
    @Test
    public void selectMultipleOptionsInMultiSelect() throws Throwable
    {
        try (WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            // setup
            String page = "<select multiple><option value='a'>a</option><option value='b'>b</option></select>";

            MockWebConnection conn = new MockWebConnection();
            conn.setDefaultResponse(page);
            webClient.setWebConnection(conn);

            // test
            HtmlPage htmlPage = webClient.getPage("http://dummy.net");

            HtmlSelect select = (HtmlSelect) htmlPage.getElementsByTagName("select").get(0);
            Assert.assertTrue(select.isMultipleSelectEnabled());

            List<HtmlOption> options = select.getOptions();
            for (HtmlOption option : options)
            {
                option.setSelected(true);
            }

            for (HtmlOption option : options)
            {
                Assert.assertTrue("Option not selected: " + option, option.isSelected());
            }
        }
    }
}
