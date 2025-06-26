/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.common.util;

import java.net.URI;
import java.net.URL;
import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the implementation of {@link ParseUtils}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class ParseUtilsTest
{

    // ======================
    // parseTimePeriod tests
    // ======================

    @Test
    public void testParseTime() throws Exception
    {
        final int time = 12345;

        for (final String s : new String[]
            {
                "12345", "12345s", "3h25m45s", "3:25:45"
            })
        {
            Assert.assertEquals(time, ParseUtils.parseTimePeriod(s));
        }
    }

    @Test
    public void testParseTime_s() throws Exception
    {
        Assert.assertEquals(0, ParseUtils.parseTimePeriod("0s"));
        Assert.assertEquals(1, ParseUtils.parseTimePeriod("1s"));
        Assert.assertEquals(10, ParseUtils.parseTimePeriod("10s"));
    }

    @Test
    public void testParseTime_min() throws Exception
    {
        Assert.assertEquals(0, ParseUtils.parseTimePeriod("0m"));
        Assert.assertEquals(60, ParseUtils.parseTimePeriod("1m"));
        Assert.assertEquals(10 * 60, ParseUtils.parseTimePeriod("10m"));
    }

    @Test
    public void testParseTime_h() throws Exception
    {
        Assert.assertEquals(0, ParseUtils.parseTimePeriod("0h"));
        Assert.assertEquals(3600, ParseUtils.parseTimePeriod("1h"));
        Assert.assertEquals(10 * 3600, ParseUtils.parseTimePeriod("10h"));
    }

    @Test
    public void testParseTime_m_s() throws Exception
    {
        Assert.assertEquals(10 * 60 + 10, ParseUtils.parseTimePeriod("10m 10s"));
        Assert.assertEquals(10 * 60 + 10, ParseUtils.parseTimePeriod("10m10s"));
        Assert.assertEquals(10 * 60 + 10, ParseUtils.parseTimePeriod("10:10"));
    }

    @Test
    public void testParseTime_h_m() throws Exception
    {
        Assert.assertEquals(10 * 3600 + 10 * 60, ParseUtils.parseTimePeriod("10h 10m"));
        Assert.assertEquals(10 * 3600 + 10 * 60, ParseUtils.parseTimePeriod("10h10m"));
        Assert.assertEquals(10 * 3600 + 10 * 60, ParseUtils.parseTimePeriod("10:10:00"));
    }

    @Test
    public void testParseTime_h_m_s() throws Exception
    {
        Assert.assertEquals(10 * 3600 + 10 * 60 + 10, ParseUtils.parseTimePeriod("10h 10m 10s"));
        Assert.assertEquals(10 * 3600 + 10 * 60 + 10, ParseUtils.parseTimePeriod("10h10m10s"));
        Assert.assertEquals(10 * 3600 + 10 * 60 + 10, ParseUtils.parseTimePeriod("10:10:10"));
        Assert.assertEquals(1 * 3600 + 1 * 60 + 1, ParseUtils.parseTimePeriod("1:1:1"));
    }

    @Test(expected = ParseException.class)
    public void testParseTime_InvalidFormat_01() throws Exception
    {
        ParseUtils.parseTimePeriod("0:05s");
    }

    @Test(expected = ParseException.class)
    public void testParseTime_InvalidFormat_02() throws Exception
    {
        ParseUtils.parseTimePeriod("1:60");
    }

    @Test(expected = ParseException.class)
    public void testParseTime_InvalidFormat_03() throws Exception
    {
        ParseUtils.parseTimePeriod("10h10");
    }

    @Test
    public void testParseRelativeTimePositive() throws Exception
    {
        final int time = 12345;

        for (final String s : new String[]
            {
                "+12345", "+12345s", "+3h25m45s", "+3:25:45"
            })
        {
            Assert.assertEquals(time, ParseUtils.parseRelativeTimePeriod(s));
        }
    }

    @Test
    public void testParseRelativeTimeNegative() throws Exception
    {
        final int time = -12345;

        for (final String s : new String[]
            {
                "-12345", "-12345s", "-3h25m45s", "-3:25:45"
            })
        {
            Assert.assertEquals(time, ParseUtils.parseRelativeTimePeriod(s));
        }
    }

    // ======================
    // parseClass tests
    // ======================

    @Test
    public void testParseClass() throws Exception
    {
        Assert.assertNotNull(ParseUtils.parseClass("java.lang.String"));
    }

    @Test(expected = ParseException.class)
    public void testParseClass_NoSuchClass() throws Exception
    {
        ParseUtils.parseClass("com.bar.Foo");
    }

    // ======================
    // parseURL tests
    // ======================

    @SuppressWarnings("deprecation")
    @Test
    public void testParseURL() throws Exception
    {
        final URL u = ParseUtils.parseURL("http://localhost/test");
        Assert.assertNotNull(u);
        Assert.assertEquals("http", u.getProtocol());
        Assert.assertEquals("localhost", u.getHost());
        Assert.assertEquals("/test", u.getPath());

        final URL w = ParseUtils.parseURL("file:///");
        Assert.assertNotNull(w);
        Assert.assertEquals("file", w.getProtocol());
        Assert.assertEquals("/", w.getPath());
    }

    @SuppressWarnings("deprecation")
    @Test(expected = ParseException.class)
    public void testParseURL_InvalidURLScheme() throws Exception
    {
        ParseUtils.parseURL("svn://somehost");
    }

    @Test
    public void testParseURI() throws Exception
    {
        final URI u = ParseUtils.parseURI("http://localhost/test");
        Assert.assertNotNull(u);
        Assert.assertEquals("http", u.getScheme());
        Assert.assertEquals("localhost", u.getHost());
        Assert.assertEquals("/test", u.getPath());

        final URI w = ParseUtils.parseURI("file:///");
        Assert.assertNotNull(w);
        Assert.assertEquals("file", w.getScheme());
        Assert.assertEquals("/", w.getPath());
    }

    @Test(expected = ParseException.class)
    public void testParseURI_InvalidURIScheme() throws Exception
    {
        ParseUtils.parseURI(":svn//somehost");
    }

    // ======================
    // parseDouble tests
    // ======================

    /**
     * Parse double
     * 
     * @throws ParseException
     */
    @Test
    public void parseDouble() throws ParseException
    {
        Assert.assertTrue(ParseUtils.parseDouble("0") == 0.0);
        Assert.assertTrue(ParseUtils.parseDouble("0.0") == 0.0);
        Assert.assertTrue(ParseUtils.parseDouble("-10.04") == -10.04);
        Assert.assertTrue(ParseUtils.parseDouble("10.08") == 10.08);
    }

    /**
     * Parse double
     * 
     * @throws ParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void parseDouble_Null() throws ParseException
    {
        ParseUtils.parseDouble(null);
    }

    /**
     * Parse double
     * 
     * @throws ParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void parseDouble_Empty() throws ParseException
    {
        ParseUtils.parseDouble("");
    }

    /**
     * Parse double
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void parseDouble_NoDouble() throws ParseException
    {
        ParseUtils.parseDouble("10,01");
    }

    // ======================
    // parseInt tests
    // ======================

    /**
     * Parse int
     * 
     * @throws ParseException
     */
    @Test
    public void parseInt() throws ParseException
    {
        Assert.assertTrue(ParseUtils.parseInt("0") == 0);
        Assert.assertTrue(ParseUtils.parseInt("100000000") == 100000000);
        Assert.assertTrue(ParseUtils.parseInt("-10") == -10);
        Assert.assertTrue(ParseUtils.parseInt("10") == 10);
    }

    /**
     * Parse int
     * 
     * @throws ParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void parseInt_Null() throws ParseException
    {
        ParseUtils.parseInt(null);
    }

    /**
     * Parse int
     * 
     * @throws ParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void parseInt_Empty() throws ParseException
    {
        ParseUtils.parseInt("");
    }

    /**
     * Parse int
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void parseInt_NoInt() throws ParseException
    {
        ParseUtils.parseInt("10,aa");
    }

    /**
     * Parse int
     * 
     * @throws ParseException
     */
    @Test
    public void parseIntFallback() throws ParseException
    {
        Assert.assertTrue(ParseUtils.parseInt("0", 10) == 0);
        Assert.assertTrue(ParseUtils.parseInt("100000000", 10) == 100000000);
        Assert.assertTrue(ParseUtils.parseInt("-10", 10) == -10);
        Assert.assertTrue(ParseUtils.parseInt("10", -10) == 10);
        Assert.assertTrue(ParseUtils.parseInt(" -10 ", 10) == -10);
        Assert.assertTrue(ParseUtils.parseInt("00010", -10) == 10);
    }

    /**
     * Parse int
     * 
     * @throws ParseException
     */
    @Test
    public void parseIntFallback_Null() throws ParseException
    {
        Assert.assertTrue(ParseUtils.parseInt(null, 10) == 10);
    }

    /**
     * Parse int
     * 
     * @throws ParseException
     */
    @Test
    public void parseIntFallback_Empty() throws ParseException
    {
        Assert.assertTrue(ParseUtils.parseInt("", 10) == 10);
    }

    /**
     * Parse int
     * 
     * @throws ParseException
     */
    @Test
    public void parseIntFallback_NoInt() throws ParseException
    {
        Assert.assertTrue(ParseUtils.parseInt("10,aa", 10) == 10);
    }

    // ======================
    // parseLong tests
    // ======================

    /**
     * Parse long
     * 
     * @throws ParseException
     */
    @Test
    public void parseLong() throws ParseException
    {
        Assert.assertTrue(ParseUtils.parseLong("0") == 0);
        Assert.assertTrue(ParseUtils.parseLong("100000000") == 100000000);
        Assert.assertTrue(ParseUtils.parseLong("-10") == -10);
        Assert.assertTrue(ParseUtils.parseLong("10") == 10);
        Assert.assertTrue(ParseUtils.parseLong(" 10 ") == 10);
        Assert.assertTrue(ParseUtils.parseLong("00010") == 10);
    }

    /**
     * Parse long
     * 
     * @throws ParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void parseLong_Null() throws ParseException
    {
        ParseUtils.parseLong(null);
    }

    /**
     * Parse long
     * 
     * @throws ParseException
     */
    @Test(expected = IllegalArgumentException.class)
    public void parseLong_Empty() throws ParseException
    {
        ParseUtils.parseLong("");
    }

    /**
     * Parse long
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void parseLong_NoLong() throws ParseException
    {
        ParseUtils.parseLong("10,aa");
    }

    /**
     * Parse long
     * 
     * @throws ParseException
     */
    @Test
    public void parseLongFallback() throws ParseException
    {
        Assert.assertTrue(ParseUtils.parseLong("0", 10) == 0);
        Assert.assertTrue(ParseUtils.parseLong("100000000", 10) == 100000000);
        Assert.assertTrue(ParseUtils.parseLong("-10", 10) == -10);
        Assert.assertTrue(ParseUtils.parseLong("10", -10) == 10);
        Assert.assertTrue(ParseUtils.parseLong(" 10 ", -10) == 10);
        Assert.assertTrue(ParseUtils.parseLong("0010", -10) == 10);
    }

    /**
     * Parse long
     * 
     * @throws ParseException
     */
    @Test
    public void parseLongFallback_Null() throws ParseException
    {
        Assert.assertTrue(ParseUtils.parseLong(null, 10) == 10);
    }

    /**
     * Parse long
     * 
     * @throws ParseException
     */
    @Test
    public void parseLongFallback_Empty() throws ParseException
    {
        Assert.assertTrue(ParseUtils.parseLong("", 10) == 10);
    }

    /**
     * Parse long
     * 
     * @throws ParseException
     */
    @Test
    public void parseLongFallback_NoLong() throws ParseException
    {
        Assert.assertTrue(ParseUtils.parseLong("10,aa", 10) == 10);
    }

    @Test
    public void parseAbsoluteOrRelative_AbsoluteIntValue() throws ParseException
    {
        AbsoluteOrRelativeNumber<Integer> number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseInt, "0");
        Assert.assertEquals(false, number.isRelativeNumber());
        Assert.assertEquals(0, number.getValue().intValue());

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseInt, "1");
        Assert.assertEquals(false, number.isRelativeNumber());
        Assert.assertEquals(1, number.getValue().intValue());

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseInt, "123");
        Assert.assertEquals(false, number.isRelativeNumber());
        Assert.assertEquals(123, number.getValue().intValue());

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseInt, String.valueOf(Integer.MAX_VALUE));
        Assert.assertEquals(false, number.isRelativeNumber());
        Assert.assertEquals(Integer.MAX_VALUE, number.getValue().intValue());
    }

    @Test
    public void parseAbsoluteOrRelative_AbsoluteDoubleValue() throws ParseException
    {
        AbsoluteOrRelativeNumber<Double> number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseDouble, "0.0");
        Assert.assertEquals(false, number.isRelativeNumber());
        Assert.assertEquals(0.0, number.getValue().doubleValue(), 0.0);

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseDouble, "1.2");
        Assert.assertEquals(false, number.isRelativeNumber());
        Assert.assertEquals(1.2, number.getValue().doubleValue(), 0.0);

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseDouble, "123.567890");
        Assert.assertEquals(false, number.isRelativeNumber());
        Assert.assertEquals(123.567890, number.getValue().doubleValue(), 0.0);

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseDouble, String.valueOf(Double.MAX_VALUE));
        Assert.assertEquals(false, number.isRelativeNumber());
        Assert.assertEquals(Double.MAX_VALUE, number.getValue().doubleValue(), 0.0);
    }

    @Test
    public void parseAbsoluteOrRelative_RelativeIntValue() throws ParseException
    {
        AbsoluteOrRelativeNumber<Integer> number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseInt, "+0");
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(0, number.getValue().intValue());

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseInt, "-0");
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(0, number.getValue().intValue());

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseInt, "+1");
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(1, number.getValue().intValue());

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseInt, "-1");
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(-1, number.getValue().intValue());

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseInt, "+123");
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(123, number.getValue().intValue());

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseInt, "-123");
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(-123, number.getValue().intValue());

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseInt, "+" + Integer.MAX_VALUE);
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(Integer.MAX_VALUE, number.getValue().intValue());

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseInt, "-" + Integer.MAX_VALUE);
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(-Integer.MAX_VALUE, number.getValue().intValue());
    }

    @Test
    public void parseAbsoluteOrRelative_RelativeDoubleValue() throws ParseException
    {
        AbsoluteOrRelativeNumber<Double> number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseDouble, "+0.0");
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(0.0, number.getValue().doubleValue(), 0.0);

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseDouble, "-0.0");
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(-0.0, number.getValue().doubleValue(), 0.0);

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseDouble, "+1.2");
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(1.2, number.getValue().doubleValue(), 0.0);

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseDouble, "-1.2");
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(-1.2, number.getValue().doubleValue(), 0.0);

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseDouble, "+123.4567890");
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(123.4567890, number.getValue().doubleValue(), 0.0);

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseDouble, "-123.4567890");
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(-123.4567890, number.getValue().doubleValue(), 0.0);

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseDouble, "+" + Double.MAX_VALUE);
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(Double.MAX_VALUE, number.getValue().doubleValue(), 0.0);

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseDouble, "-" + Double.MAX_VALUE);
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(-Double.MAX_VALUE, number.getValue().doubleValue(), 0.0);

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseDouble, "+" + Double.MIN_VALUE);
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(Double.MIN_VALUE, number.getValue().doubleValue(), 0.0);

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseDouble, "-" + Double.MIN_VALUE);
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(-Double.MIN_VALUE, number.getValue().doubleValue(), 0.0);
    }

    @Test
    public void parseAbsoluteOrRelative_TimePeriod() throws ParseException
    {
        AbsoluteOrRelativeNumber<Integer> number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseTimePeriod, " 1h 10m ");
        Assert.assertEquals(false, number.isRelativeNumber());
        Assert.assertEquals(4200, number.getValue().intValue());

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseTimePeriod, " + 1h 10m ");
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(4200, number.getValue().intValue());

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseTimePeriod, " - 1h 10m ");
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(-4200, number.getValue().intValue());
    }

    @Test
    public void parseAbsoluteOrRelative_extraWhitespaces() throws ParseException
    {
        AbsoluteOrRelativeNumber<Integer> number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseInt, " \t 123 \t ");
        Assert.assertEquals(false, number.isRelativeNumber());
        Assert.assertEquals(123, number.getValue().intValue());

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseInt, "  \t  +  \t  123  \t  ");
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(123, number.getValue().intValue());

        number = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseInt, "  \t  -  \t  123  \t  ");
        Assert.assertEquals(true, number.isRelativeNumber());
        Assert.assertEquals(-123, number.getValue().intValue());
    }

    @Test(expected = ParseException.class)
    public void parseAbsoluteOrRelative_invalidValue() throws ParseException
    {
        ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseTimePeriod, "++1h");
    }
}
