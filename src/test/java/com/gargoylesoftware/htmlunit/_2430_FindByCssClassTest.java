package com.gargoylesoftware.htmlunit;

import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @see https://lab.xceptance.de/issues/2430
 * @see http://sourceforge.net/p/htmlunit/bugs/1685/
 */
@RunWith(Parameterized.class)
public class _2430_FindByCssClassTest
{
    static
    {
        BasicConfigurator.configure();
    }

    @Parameters
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][]
            {
                    {
                        "foo"
                    },
                    {
                        "foo "
                    },
                    {
                        " foo"
                    },
                    {
                        "\tfoo"
                    },
                    {
                        "foo\t"
                    },
                    {
                        "\nfoo"
                    },
                    {
                        "foo\n"
                    },
                    {
                        "\tfoo\n"
                    },
                    {
                        "\nfoo\t"
                    },
            });
    }

    @Parameter
    public String classAttributeValue;

    @Test
    public void test() throws Throwable
    {
        // set up mock response
        String page = "<div class='" + classAttributeValue + "'></div>";

        MockWebConnection conn = new MockWebConnection();
        conn.setDefaultResponse(page);

        // set up web client
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            webClient.setWebConnection(conn);

            // test
            HtmlPage htmlPage = webClient.getPage("http://dummy.net");
            Assert.assertEquals("Unexpected number of matches:", 1, htmlPage.querySelectorAll(".foo").size());
        }
    }
}
