package com.gargoylesoftware.htmlunit;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.gargoylesoftware.htmlunit.util.UrlUtils;
import com.xceptance.common.lang.ReflectionUtils;

/**
 * @see https://lab.xceptance.de/issues/3054
 */
@RunWith(Parameterized.class)
public class _3054_EscapePercentSignTest
{

    private final String input;

    private final String output;

    public _3054_EscapePercentSignTest(final String aInput, final String aOutput)
    {
        input = aInput;
        output = aOutput;
    }

    @Parameters(name = "enc({0})={1}")
    public static Object[][] data()
    {
        return new Object[][]
        {
          {
            "foo%%20bar", "foo%25%20bar"
          },
          {
            "foo%20bar", "foo%20bar"
          },
          {
            "foo%ar", "foo%25ar"
          },
          {
            "foo%%xyz", "foo%25%25xyz"
          },
          {
            "foo%20%xyz", "foo%20%25xyz"
          },
          {
            "foo%2x%bar", "foo%252x%bar"
          }
        };
    }

    @Test
    public void testEncPrcnt() throws Throwable
    {
        final byte[] bytes = input.getBytes(StandardCharsets.US_ASCII);
        final String encodedString = ReflectionUtils.callStaticMethod(UrlUtils.class, "encodePercentSign", bytes);
        Assert.assertEquals(output, encodedString);
    }

}
