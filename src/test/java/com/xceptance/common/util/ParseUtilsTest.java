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

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import util.JUnitParamsUtils;

/**
 * Tests the implementation of {@link ParseUtils}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
@RunWith(JUnitParamsRunner.class)
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

    @Test
    @Parameters(value =
        {
            "192.0.2.100                            | 192.0.2.100", //
            "2001:db8:1111:2222:aaaa:bbbb:1a2b:cd34 | 2001:db8:1111:2222:aaaa:bbbb:1a2b:cd34", //
            "2001:dB8:1111:2222:aaaa:BBBB:1a2B:Cd34 | 2001:db8:1111:2222:aaaa:bbbb:1a2b:cd34", //
            "2001:db8:1:22:333:a:bb:ccc             | 2001:db8:1:22:333:a:bb:ccc", //
            "2001:dB8::1:0                          | 2001:db8:0:0:0:0:1:0" //
    })
    public void parseIpAddresses_SingleIp_DifferentIpFormats(final String ip, final String expectedResult) throws ParseException
    {
        final InetAddress[] addresses = ParseUtils.parseIpAddresses(ip);
        Assert.assertNotNull(addresses);
        Assert.assertEquals(1, addresses.length);
        Assert.assertEquals(expectedResult, addresses[0].getHostAddress());
    }

    @Test
    @Parameters(method = "provideValidDelimiters")
    public void parseIpAddresses_SingleIp_LeadingAndTrailingDelimiters(final String delimiterString) throws ParseException
    {
        final String ip = "192.0.2.100";
        // add leading and trailing delimiters to IP
        final String ipString = String.join("", delimiterString, ip, delimiterString);

        final InetAddress[] addresses = ParseUtils.parseIpAddresses(ipString);
        Assert.assertNotNull(addresses);
        Assert.assertEquals(1, addresses.length);
        Assert.assertEquals(ip, addresses[0].getHostAddress());
    }

    @Test
    @Parameters(value =
        {
            "192.0.2.100 | 2001:db8:1:22:a:bb:1a2b:cd34 | 2001:db8:0:0:0:0:1:0 | 203.0.113.200",  // unique IPs
            "192.0.2.100 | 2001:db8:0:0:0:0:1:0         | 2001:db8:0:0:0:0:1:0 | 192.0.2.100",    // duplicate IPs
    })
    public void parseIpAddresses_MultipleIps(final String ip1, final String ip2, final String ip3, final String ip4) throws ParseException
    {
        final InetAddress[] addresses = ParseUtils.parseIpAddresses(String.join(",", ip1, ip2, ip3, ip4));
        Assert.assertNotNull(addresses);
        Assert.assertEquals(4, addresses.length);
        Assert.assertEquals(ip1, addresses[0].getHostAddress());
        Assert.assertEquals(ip2, addresses[1].getHostAddress());
        Assert.assertEquals(ip3, addresses[2].getHostAddress());
        Assert.assertEquals(ip4, addresses[3].getHostAddress());
    }

    @Test
    @Parameters(method = "provideValidDelimiterCombinations")
    public void parseIpAddresses_MultipleIps_DifferentDelimiters(final String leadingDelimiter, final String delimiter1,
                                                                 final String delimiter2, final String trailingDelimiter)
        throws ParseException
    {
        final String ip1 = "192.0.2.100";
        final String ip2 = "2001:db8:1:22:a:bb:1a2b:cd34";
        final String ip3 = "203.0.113.200";
        final String ipString = String.join("", leadingDelimiter, ip1, delimiter1, ip2, delimiter2, ip3, trailingDelimiter);

        final InetAddress[] addresses = ParseUtils.parseIpAddresses(ipString);
        Assert.assertNotNull(addresses);
        Assert.assertEquals(3, addresses.length);
        Assert.assertEquals(ip1, addresses[0].getHostAddress());
        Assert.assertEquals(ip2, addresses[1].getHostAddress());
        Assert.assertEquals(ip3, addresses[2].getHostAddress());
    }

    @Test
    @Parameters(method = "provideValidDelimiters")
    public void parseIpAddresses_OnlyDelimiters(final String delimiterString) throws ParseException
    {
        final InetAddress[] addresses = ParseUtils.parseIpAddresses(delimiterString);
        Assert.assertNotNull(addresses);
        Assert.assertEquals(0, addresses.length);
    }

    @Test
    @Parameters(source = JUnitParamsUtils.BlankStringOrNullParamProvider.class)
    public void parseIpAddresses_BlankStringOrNull(final String blankStringOrNull) throws ParseException
    {
        final InetAddress[] addresses = ParseUtils.parseIpAddresses(blankStringOrNull);
        Assert.assertNotNull(addresses);
        Assert.assertEquals(0, addresses.length);
    }

    @Test(expected = ParseException.class)
    @Parameters(source = InvalidIpOverrideParamProvider.class)
    public void parseIpAddresses_InvalidIp(final String invalidIp) throws ParseException
    {
        ParseUtils.parseIpAddresses(invalidIp);
    }

    @Test
    public void parseDelimitedString_SingleValue()
    {
        final String value = "abc";

        final String[] parsedValues = ParseUtils.parseDelimitedString(value);
        Assert.assertNotNull(parsedValues);
        Assert.assertEquals(1, parsedValues.length);
        Assert.assertEquals(value, parsedValues[0]);
    }

    @Test
    @Parameters(method = "provideValidDelimiters")
    public void parseDelimitedString_SingleValue_LeadingAndTrailingDelimiters(final String delimiterString)
    {
        final String value = "abc";
        // add leading and trailing delimiters to value
        final String stringToParse = String.join("", delimiterString, value, delimiterString);

        final String[] parsedValues = ParseUtils.parseDelimitedString(stringToParse);
        Assert.assertNotNull(parsedValues);
        Assert.assertEquals(1, parsedValues.length);
        Assert.assertEquals(value, parsedValues[0]);
    }

    @Test
    @Parameters(method = "provideValidDelimiterCombinations")
    public void parseDelimitedString_MultipleValues_DifferentDelimiters(final String leadingDelimiter, final String delimiter1,
                                                                        final String delimiter2, final String trailingDelimiter)
    {
        // test that values containing non-delimiter special characters are parsed as expected
        final String value1 = "abc";
        final String value2 = "xyz.:-_~/|@123";
        final String value3 = "+-*#=!?§$%&{}[]()<>";
        final String stringToParse = String.join("", leadingDelimiter, value1, delimiter1, value2, delimiter2, value3, trailingDelimiter);

        final String[] parsedValues = ParseUtils.parseDelimitedString(stringToParse);
        Assert.assertNotNull(parsedValues);
        Assert.assertEquals(3, parsedValues.length);
        Assert.assertEquals(value1, parsedValues[0]);
        Assert.assertEquals(value2, parsedValues[1]);
        Assert.assertEquals(value3, parsedValues[2]);
    }

    @Test
    @Parameters(method = "provideValidDelimiters")
    public void parseDelimitedString_OnlyDelimiters(final String delimiterString)
    {
        final String[] parsedValues = ParseUtils.parseDelimitedString(delimiterString);
        Assert.assertNotNull(parsedValues);
        Assert.assertEquals(0, parsedValues.length);
    }

    @Test
    @Parameters(source = JUnitParamsUtils.BlankStringOrNullParamProvider.class)
    public void parseDelimitedString_BlankStringOrNull(final String blankStringOrNull)
    {
        final String[] parsedValues = ParseUtils.parseDelimitedString(blankStringOrNull);
        Assert.assertNotNull(parsedValues);
        Assert.assertEquals(0, parsedValues.length);
    }

    /**
     * Test parameter provider method. Returns Strings containing only valid delimiter characters.
     */
    @SuppressWarnings("unused")
    private Object[] provideValidDelimiters()
    {
        return JUnitParamsUtils.wrapEachParam(new Object[]
            {
                " ",                // single space
                "  ",               // multiple spaces
                "\t",               // single tab
                "\t\t",             // multiple tabs
                ",",                // single comma
                ",,",               // multiple commas
                ";",                // single semicolon
                ";;",               // multiple semicolons
                " \t,;",            // one of each delimiter
                "\t,,;\t\t  ;; , "  // random combination of delimiters
            });
    }

    /**
     * Test parameter provider method. Returns different combinations of valid delimiter Strings. Each parameter set
     * contains four values that are intended to be interpreted as a leading delimiter, two delimiters between values
     * and a trailing delimiter.
     */
    @SuppressWarnings("unused")
    private Object[] provideValidDelimiterCombinations()
    {
        return new Object[]
            {
                JUnitParamsUtils.wrapParams("", " ", " ", ""),                   // space-delimited
                JUnitParamsUtils.wrapParams("", "\t", "\t", ""),                 // tab-delimited
                JUnitParamsUtils.wrapParams("", ",", ",", ""),                   // comma-delimited
                JUnitParamsUtils.wrapParams("", ";", ";", ""),                   // semicolon-delimited
                JUnitParamsUtils.wrapParams(" ", " ", " ", " "),                 // leading & trailing space
                JUnitParamsUtils.wrapParams("\t", "\t", "\t", "\t"),             // leading & trailing tab
                JUnitParamsUtils.wrapParams(",", ",", ",", ","),                 // leading & trailing comma
                JUnitParamsUtils.wrapParams(";", ";", ";", ";"),                 // leading & trailing semicolon
                JUnitParamsUtils.wrapParams(" ", ",", ";", "\t"),                // mixed delimiters 1
                JUnitParamsUtils.wrapParams(",", ";", " ", ";"),                 // mixed delimiters 2
                JUnitParamsUtils.wrapParams("\t, ", " ,\t", "\t; ", " ;\t"),     // whitespaces around delimiters
                JUnitParamsUtils.wrapParams(" \t\t ", "   ", " \t\t ", "   "),   // multiple whitespace delimiters
                JUnitParamsUtils.wrapParams(";;", ",,", ";;", ",,"),             // multiple non-whitespace delimiters
                JUnitParamsUtils.wrapParams(" ,, \t;,;", " , ; ", " ;; \t,,, ", " ;;\t;, "), // random combination
            };
    }

    /**
     * Test parameter provider class for different invalid IP override Strings.
     */
    public static class InvalidIpOverrideParamProvider
    {
        @SuppressWarnings("unused")
        public static Object[] provideInvalidIpOverrideParams()
        {
            return JUnitParamsUtils.wrapEachParam(new Object[]
                {
                    "example.org",              // hostname instead of IP
                    "192.0.2.256",              // IPv4 address: invalid value
                    "192.0. 2.100",             // IPv4 address: contains whitespace
                    "2001:db8:1:2:3:4:5:g",     // IPv6 address: invalid value
                    "2001:db8:: 1:0",           // IPv6 address: contains whitespace
                    "[2001:db8::1:0]",          // IPv6 address: value in brackets
                    "192.0.2.100,192.0.2.256",  // one valid, one invalid address
                    "192.0.2.100|192.0.2.101"   // invalid delimiter
                });
        }
    }
}
