/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.common.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

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
     * Test method for {@link com.xceptance.common.lang.ParseNumbers#parseLong(java.lang.String)}.
     */
    @Test
    public final void testParseLong()
    {
        {
            final String s = "1670036109465868";
            Assert.assertTrue(Long.valueOf(s) == ParseNumbers.parseLong(s));
        }
        {
            final String s = "0";
            Assert.assertTrue(Long.valueOf(s) == ParseNumbers.parseLong(s));
        }
        {
            final String s = "5";
            Assert.assertTrue(Long.valueOf(s) == ParseNumbers.parseLong(s));
        }
        {
            final String s = "12";
            Assert.assertTrue(Long.valueOf(s) == ParseNumbers.parseLong(s));
        }
        {
            final String s = "1670036";
            Assert.assertTrue(Long.valueOf(s) == ParseNumbers.parseLong(s));
        }
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseLong(java.lang.String)}.
     */
    @Test
    public final void testParseLongFallback()
    {
        {
            final String s = "-1670036109465868";
            Assert.assertTrue(Long.valueOf(s) == ParseNumbers.parseLong(s));
        }
        {
            final String s = "-0";
            Assert.assertTrue(Long.valueOf(s) == ParseNumbers.parseLong(s));
        }
        {
            final String s = "-1670036";
            Assert.assertTrue(Long.valueOf(s) == ParseNumbers.parseLong(s));
        }
        {
            final String s = "+0";
            Assert.assertTrue(Long.valueOf(s) == ParseNumbers.parseLong(s));
        }
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test
    public final void testParseInt()
    {
        {
            final String s = "1670036108";
            Assert.assertTrue(Integer.valueOf(s) == ParseNumbers.parseInt(s));
        }
        {
            final String s = "0";
            Assert.assertTrue(Integer.valueOf(s) == ParseNumbers.parseInt(s));
        }
        {
            final String s = "8";
            Assert.assertTrue(Integer.valueOf(s) == ParseNumbers.parseInt(s));
        }
        {
            final String s = "28";
            Assert.assertTrue(Integer.valueOf(s) == ParseNumbers.parseInt(s));
        }
        {
            final String s = "1670036";
            Assert.assertTrue(Integer.valueOf(s) == ParseNumbers.parseInt(s));
        }
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test
    public final void testParseIntFallback()
    {
        {
            final String s = "-1670036108";
            Assert.assertTrue(Integer.valueOf(s) == ParseNumbers.parseInt(s));
        }
        {
            final String s = "-0";
            Assert.assertTrue(Integer.valueOf(s) == ParseNumbers.parseInt(s));
        }
        {
            final String s = "-1670036";
            Assert.assertTrue(Integer.valueOf(s) == ParseNumbers.parseInt(s));
        }
        {
            final String s = "+9876";
            Assert.assertTrue(Integer.valueOf(s) == ParseNumbers.parseInt(s));
        }
    }


    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionInt_Void()
    {
        ParseNumbers.parseInt("12a");
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionInt_Empty()
    {
        ParseNumbers.parseInt("");
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionInt_Space()
    {
        ParseNumbers.parseInt(" ");
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionInt_WrongCharacter()
    {
        ParseNumbers.parseInt("aaa");
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionInt_Null()
    {
        ParseNumbers.parseInt(null);
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionLong_Void()
    {
        ParseNumbers.parseInt("12a");
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionLong_Empty()
    {
        ParseNumbers.parseLong("");
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionLong_Space()
    {
        ParseNumbers.parseLong(" ");
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionLong_WrongCharacter()
    {
        ParseNumbers.parseLong("2aa");
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionLong_Null()
    {
        ParseNumbers.parseLong(null);
    }

    // ================================================================
    // Double

    @Test
    public void doubleHappyPath()
    {
        String s = "";

        s = "0"; Assert.assertTrue(Double.parseDouble(s) == ParseNumbers.parseDouble(s));
        s = "0.0"; Assert.assertTrue(Double.parseDouble(s) == ParseNumbers.parseDouble(s));
        s = "0.000008765"; Assert.assertTrue(Double.parseDouble(s) == ParseNumbers.parseDouble(s));
        s = "1"; Assert.assertTrue(Double.parseDouble(s) == ParseNumbers.parseDouble(s));
        s = "1.0000087171"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s), 0.00000000001);

        s = "2"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s), 0.00000000001);
        s = "32"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s), 0.00000000001);
        s = "423"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s), 0.00000000001);
        s = "5234"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s), 0.00000000001);
        s = "12345"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s), 0.00000000001);
        s = "223456"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s), 0.00000000001);
        s = "5234567"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s), 0.00000000001);

        s = "1.1"; Assert.assertTrue(Double.parseDouble(s) == ParseNumbers.parseDouble(s));
        s = "12.1"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s),      0.0000000001);
        s = "123.1"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s),     0.0000000001);
        s = "1234.2"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s),    0.0000000001);
        s = "12345.3"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s),   0.0000000001);
        s = "123456.4"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s),  0.0000000001);
        s = "1234567.5"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s), 0.0000000001);

        s = "1"; Assert.assertTrue(Double.parseDouble(s) == ParseNumbers.parseDouble(s));
        s = "1.143"; Assert.assertTrue(Double.parseDouble(s) == ParseNumbers.parseDouble(s));
        s = "12.111"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s), 0.00000000001);
        s = "123.144"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s), 0.00000000001);
        s = "1234.255"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s), 0.00000000001);
        s = "12345.322"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s), 0.00000000001);
        s = "123456.433"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s), 0.00000000001);
        s = "1234567.533"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s), 0.00000000001);

        s = "1.0"; Assert.assertTrue(Double.parseDouble(s) == ParseNumbers.parseDouble(s));
        s = "1.001"; Assert.assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(s), 0.00000000001);
        s = "0.25"; Assert.assertTrue(Double.parseDouble(s) == ParseNumbers.parseDouble(s));
        s = "2.50"; Assert.assertTrue(Double.parseDouble(s) == ParseNumbers.parseDouble(s));
        s = "25.0"; Assert.assertTrue(Double.parseDouble(s) == ParseNumbers.parseDouble(s));
        s = "25.25"; Assert.assertTrue(Double.parseDouble(s) == ParseNumbers.parseDouble(s));
        s = "25.00025"; Assert.assertTrue(Double.parseDouble(s) == ParseNumbers.parseDouble(s));
        s = "0.6811"; Assert.assertTrue(Double.parseDouble(s) == ParseNumbers.parseDouble(s));
        s = "141.001"; Assert.assertTrue(Double.parseDouble(s) == ParseNumbers.parseDouble(s));

        s = "10.100000000000001"; Assert.assertTrue(Double.parseDouble(s) == ParseNumbers.parseDouble(s));
        s = "-141.001"; Assert.assertTrue(Double.parseDouble(s) == ParseNumbers.parseDouble(s));
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionDouble_Void()
    {
        ParseNumbers.parseDouble("12,11");
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionDouble_Empty()
    {
        ParseNumbers.parseDouble("");
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionDouble_Space()
    {
        ParseNumbers.parseDouble(" ");
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionDouble_WrongCharacter()
    {
        ParseNumbers.parseDouble("aaa");
    }

    /**
     * Test method for {@link com.xceptance.common.parsenumbers.FastParseNumbers#fastParseInt(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public final void testNumberFormatExceptionDouble_Null()
    {
        ParseNumbers.parseDouble(null);
    }

    /**
     * Optional int for flat map applications in streams
     */
    @Test
    public void parseOptionalInt()
    {
        assertEquals(1, ParseNumbers.parseOptionalInt("1").get().intValue());
        assertTrue(ParseNumbers.parseOptionalInt("a").isEmpty());
    }

    /**
     * Optional int for flat map applications in streams
     */
    @Test
    public void parseOptionalLong()
    {
        assertEquals(19876543567L, ParseNumbers.parseOptionalLong("19876543567").get().longValue());
        assertTrue(ParseNumbers.parseOptionalLong("a").isEmpty());
    }

    /**
     * Optional int for flat map applications in streams
     */
    @Test
    public void parseOptionalDouble()
    {
        assertTrue(1.42 == ParseNumbers.parseOptionalDouble("1.42").get().doubleValue());
        assertTrue(ParseNumbers.parseOptionalDouble("a").isEmpty());
    }

    @Test
    public void testParseIntBytes()
    {
        final String[] valid = {"0", "8", "42", "1670036108", "-1", "-123456", "+999"};
        for (String s : valid)
        {
            final byte[] b = s.getBytes(StandardCharsets.UTF_8);
            assertEquals(Integer.parseInt(s), ParseNumbers.parseInt(b, 0, b.length));

            // Test with offset and length padding
            final byte[] padded = new byte[b.length + 6];
            System.arraycopy(b, 0, padded, 3, b.length);
            assertEquals(Integer.parseInt(s), ParseNumbers.parseInt(padded, 3, b.length));
        }
    }

    @Test
    public void testParseLongBytes()
    {
        final String[] valid = {"0", "5", "12", "1670036", "1670036109465868", "-1", "-1670036109465868", "+0"};
        for (String s : valid)
        {
            final byte[] b = s.getBytes(StandardCharsets.UTF_8);
            assertEquals(Long.parseLong(s), ParseNumbers.parseLong(b, 0, b.length));

            final byte[] padded = new byte[b.length + 6];
            System.arraycopy(b, 0, padded, 2, b.length);
            assertEquals(Long.parseLong(s), ParseNumbers.parseLong(padded, 2, b.length));
        }
    }

    @Test
    public void testParseDoubleBytes()
    {
        final String[] valid = {"0", "5", "12.345", "0.001", "-42.75", "+100.5"};
        for (String s : valid)
        {
            final byte[] b = s.getBytes(StandardCharsets.UTF_8);
            assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(b, 0, b.length), 0.0001);

            final byte[] padded = new byte[b.length + 6];
            System.arraycopy(b, 0, padded, 2, b.length);
            assertEquals(Double.parseDouble(s), ParseNumbers.parseDouble(padded, 2, b.length), 0.0001);
        }
    }

    @Test
    public void testParseBooleanBytes()
    {
        final byte[] t1 = "true".getBytes(StandardCharsets.UTF_8);
        final byte[] t2 = "TRUE".getBytes(StandardCharsets.UTF_8);
        final byte[] t3 = "True".getBytes(StandardCharsets.UTF_8);
        final byte[] f1 = "false".getBytes(StandardCharsets.UTF_8);
        final byte[] f2 = "FALSE".getBytes(StandardCharsets.UTF_8);
        final byte[] f3 = "xyz".getBytes(StandardCharsets.UTF_8);

        assertTrue(ParseNumbers.parseBoolean(t1, 0, t1.length));
        assertTrue(ParseNumbers.parseBoolean(t2, 0, t2.length));
        assertTrue(ParseNumbers.parseBoolean(t3, 0, t3.length));
        Assert.assertFalse(ParseNumbers.parseBoolean(f1, 0, f1.length));
        Assert.assertFalse(ParseNumbers.parseBoolean(f2, 0, f2.length));
        Assert.assertFalse(ParseNumbers.parseBoolean(f3, 0, f3.length));
    }

    /**
     * Locks in the documented behavior where multi-dot validation is intentionally omitted
     * for performance in parseDouble(byte[], int, int).
     */
    @Test
    public void testParseDoubleMultipleDotsDocumentedBehavior()
    {
        final byte[] bytes = "1.2.3".getBytes(StandardCharsets.UTF_8);
        // Silently parsed as 12.3 because the second dot overwrites decimalPos to 3
        assertEquals(12.3, ParseNumbers.parseDouble(bytes, 0, bytes.length), 0.0001);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntBytesNonDigit()
    {
        final byte[] b = "12a".getBytes(StandardCharsets.UTF_8);
        ParseNumbers.parseInt(b, 0, b.length);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntBytesEmpty()
    {
        ParseNumbers.parseInt(new byte[0], 0, 0);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseIntBytesLoneSign()
    {
        final byte[] b = "+".getBytes(StandardCharsets.UTF_8);
        ParseNumbers.parseInt(b, 0, b.length);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseLongBytesNonDigit()
    {
        final byte[] b = "99z".getBytes(StandardCharsets.UTF_8);
        ParseNumbers.parseLong(b, 0, b.length);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseLongBytesEmpty()
    {
        ParseNumbers.parseLong(new byte[0], 0, 0);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseLongBytesLoneSign()
    {
        final byte[] b = "-".getBytes(StandardCharsets.UTF_8);
        ParseNumbers.parseLong(b, 0, b.length);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseDoubleBytesInvalid()
    {
        final byte[] b = "abc".getBytes(StandardCharsets.UTF_8);
        ParseNumbers.parseDouble(b, 0, b.length);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseDoubleBytesEmpty()
    {
        ParseNumbers.parseDouble(new byte[0], 0, 0);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseDoubleBytesLoneSign()
    {
        final byte[] b = "+".getBytes(StandardCharsets.UTF_8);
        ParseNumbers.parseDouble(b, 0, b.length);
    }
}
