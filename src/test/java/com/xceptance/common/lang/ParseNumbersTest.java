package com.xceptance.common.lang;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for parsing longs and ints.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class ParseNumbersTest
{
    /**
     * Verify constructior
     */
    @Test
    public void testConstructor()
    {
        Assert.assertTrue(ReflectionUtils.classHasOnlyPrivateConstructors(ParseNumbers.class));
    }

    /**
     * Test method for {@link com.xceptance.common.lang.ParseNumbers#parseLong(java.lang.String)}.
     */
    @Test
    public final void testParseLong()
    {
        {
            final String s = "1670036109465868";
            Assert.assertEquals((long) Long.valueOf(s), ParseNumbers.parseLong(s));
        }
        {
            final String s = "0";
            Assert.assertEquals((long) Long.valueOf(s), ParseNumbers.parseLong(s));
        }
        {
            final String s = "1670036";
            Assert.assertEquals((long) Long.valueOf(s), ParseNumbers.parseLong(s));
        }
    }

    /**
     * Test method for {@link com.xceptance.common.lang.ParseNumbers#parseLong(java.lang.String)}.
     */
    @Test
    public final void testParseLongFallback()
    {
        {
            final String s = "-1670036109465868";
            Assert.assertEquals((long) Long.valueOf(s), ParseNumbers.parseLong(s));
        }
        {
            final String s = "-0";
            Assert.assertEquals((long) Long.valueOf(s), ParseNumbers.parseLong(s));
        }
        {
            final String s = "-1670036";
            Assert.assertEquals((long) Long.valueOf(s), ParseNumbers.parseLong(s));
        }
    }

    /**
     * Test method for {@link com.xceptance.common.lang.ParseNumbers#parseInt(java.lang.String)}.
     */
    @Test
    public final void testParseInt()
    {
        {
            final String s = "1670036108";
            Assert.assertEquals((int) Integer.valueOf(s), ParseNumbers.parseInt(s));
        }
        {
            final String s = "0";
            Assert.assertEquals((int) Integer.valueOf(s), ParseNumbers.parseInt(s));
        }
        {
            final String s = "1670036";
            Assert.assertEquals((int) Integer.valueOf(s), ParseNumbers.parseInt(s));
        }
    }

    /**
     * Test method for {@link com.xceptance.common.lang.ParseNumbers#parseInt(java.lang.String)}.
     */
    @Test
    public final void testParseIntFallback()
    {
        {
            final String s = "-1670036108";
            Assert.assertEquals((int) Integer.valueOf(s), ParseNumbers.parseInt(s));
        }
        {
            final String s = "-0";
            Assert.assertEquals((int) Integer.valueOf(s), ParseNumbers.parseInt(s));
        }
        {
            final String s = "-1670036";
            Assert.assertEquals((int) Integer.valueOf(s), ParseNumbers.parseInt(s));
        }
    }

    /**
     * Test method for {@link com.xceptance.common.lang.ParseNumbers#parseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionInt_Empty()
    {
        final String s = "";
        ParseNumbers.parseInt(s);
    }

    /**
     * Test method for {@link com.xceptance.common.lang.ParseNumbers#parseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionInt_Space()
    {
        final String s = " ";
        ParseNumbers.parseInt(s);
    }

    /**
     * Test method for {@link com.xceptance.common.lang.ParseNumbers#parseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionInt_WrongCharacter()
    {
        final String s = "aaa";
        ParseNumbers.parseInt(s);
    }

    /**
     * Test method for {@link com.xceptance.common.lang.ParseNumbers#parseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionInt_Null()
    {
        final String s = null;
        ParseNumbers.parseInt(s);
    }

    /**
     * Test method for {@link com.xceptance.common.lang.ParseNumbers#parseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionLong_Empty()
    {
        final String s = "";
        ParseNumbers.parseLong(s);
    }

    /**
     * Test method for {@link com.xceptance.common.lang.ParseNumbers#parseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionLong_Space()
    {
        final String s = " ";
        ParseNumbers.parseLong(s);
    }

    /**
     * Test method for {@link com.xceptance.common.lang.ParseNumbers#parseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionLong_WrongCharacter()
    {
        final String s = "aaa";
        ParseNumbers.parseLong(s);
    }

    /**
     * Test method for {@link com.xceptance.common.lang.ParseNumbers#parseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionLong_Null()
    {
        final String s = null;
        ParseNumbers.parseLong(s);
    }
}
