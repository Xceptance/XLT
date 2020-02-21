package com.gargoylesoftware.htmlunit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.gargoylesoftware.htmlunit.util.UrlUtils;

/**
 * @see https://lab.xceptance.de/issues/3247
 */
@RunWith(Parameterized.class)
public class _3247_UrlResolvingSpecialCases
{
    @Parameters
    public static Object[][] data()
    {
        return new Object[][]
            {
                {
                    "/..there-used-to-be-a-time/index.html"
                },
                {
                    "/...there-used-to-be-a-time/index.html"
                }
            };
    }

    @Parameter
    public String relativeUrl;

    @Test
    public void test()
    {
        final String baseUrl = "http://www.example.com";

        final String url = UrlUtils.resolveUrl(baseUrl + "/some/path", relativeUrl);

        Assert.assertEquals(baseUrl + relativeUrl, url);
    }
}
