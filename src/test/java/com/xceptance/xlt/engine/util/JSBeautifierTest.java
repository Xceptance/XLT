package com.xceptance.xlt.engine.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the implementation of {@link JSBeautifier}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class JSBeautifierTest
{
    @Test
    public void testBeautify() throws Throwable
    {
        final String input = "var a,v,x,d=e,f=function(){v=a;return false};return v";
        final String result = JSBeautifier.beautify(input);
        Assert.assertNotNull(result);
        Assert.assertFalse("Code not beautified", input.equals(result));
        Assert.assertTrue("Insufficient lines of beautified code:\n" + result, result.split("[\\r\\n]+").length > 6);
    }

    @Test
    public void testBeautifyUnchanged() throws Throwable
    {
        final String input1 = "";
        Assert.assertEquals(input1, JSBeautifier.beautify(input1));

        final String input2 = "var x, y, z;";
        Assert.assertEquals(input2, JSBeautifier.beautify(input2));
    }
}
