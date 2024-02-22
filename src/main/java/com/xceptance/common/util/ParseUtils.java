/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xceptance.common.lang.StringUtils;

/**
 * Utility class for parsing various objects or values.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public final class ParseUtils
{
    /**
     * Time base.
     */
    private static final int TIME_BASE = 60;

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private ParseUtils()
    {
    }

    /**
     * Returns the resolved class object identified by the given class name.
     * 
     * @param className
     *            name of class
     * @return resolved class object
     * @throws ParseException
     *             thrown when given class name could not be resolved
     */
    public static Class<?> parseClass(final String className) throws ParseException
    {
        try
        {
            return Class.forName(className);
        }
        catch (final ClassNotFoundException e)
        {
            throw new ParseException("Failed to parse '" + className + "' as class name.", 0);
        }
    }

    /**
     * Returns an URL object for the given URL string.
     * 
     * @param urlString
     *            URL string
     * @return URL object
     * @throws ParseException
     *             thrown when the format of the given URL string is invalid
     * @deprecated due to a bug in {@link URL#URL(String)} omitting the two slashes for the protocol
     */
    @Deprecated
    public static URL parseURL(final String urlString) throws ParseException
    {
        ParameterCheckUtils.isNotNullOrEmpty(urlString, "urlString");

        try
        {
            return new URL(urlString.trim());
        }
        catch (final MalformedURLException e)
        {
            throw new ParseException("Failed to parse '" + urlString + "' as URL.", 0);
        }
    }

    /**
     * Returns an URI object for the given URI string. This is intended as a replacement for the buggy #parseURL.
     * 
     * @param urlString
     *            URL string
     * @return URL object
     * @throws ParseException
     *             thrown when the format of the given URL string is invalid
     * @see #parseURL(String)
     */
    public static URI parseURI(final String urlString) throws ParseException
    {
        ParameterCheckUtils.isNotNullOrEmpty(urlString, "urlString");

        try
        {
            return new URI(urlString.trim());
        }
        catch (final URISyntaxException e)
        {
            throw new ParseException("Failed to parse '" + urlString + "' as URI.", 0);
        }
    }

    /**
     * Parses the given string for a time period using the time formats specified by {@link TimeFormat}. After parsing,
     * its value as total number of seconds is returned.
     * 
     * @param s
     *            time period string
     * @return value of time period as total number of seconds
     * @throws ParseException
     *             thrown if the given string doesn't represent a valid time period string
     */
    public static int parseTimePeriod(final String s) throws ParseException
    {
        ParameterCheckUtils.isNotNullOrEmpty(s, "s");

        final String timeString = StringUtils.replaceAll(s, "\\s+", "");

        for (final TimeFormat tFormat : TimeFormat.values())
        {
            final Matcher m = tFormat.getPattern().matcher(timeString);
            if (m.matches())
            {
                return tFormat.evaluate(m);
            }
        }

        throw new ParseException(String.format("Unknown format of time period '%s'.", s), 0);
    }

    /**
     * Parses the given string for a time period using the time formats specified by {@link TimeFormat}. The first
     * character may be an ASCII minus sign {@code '-'} (<code>&#92;u002D'</code>) to indicate a negative value or an
     * ASCII plus sign {@code '+'} (<code>'&#92;u002B'</code>) to indicate a positive value. After parsing, its value as
     * total number of seconds is returned.
     * 
     * @param s
     *            time period string
     * @return value of time period as total number of seconds
     * @throws ParseException
     *             thrown if the given string doesn't represent a valid time period string
     */
    public static int parseRelativeTimePeriod(final String s) throws ParseException
    {
        ParameterCheckUtils.isNotNullOrEmpty(s, "s");

        String timeString = s.trim();

        // first character can indicate a positive or negative time period
        final char operator = timeString.charAt(0);
        if (operator == '-' || operator == '+')
        {
            timeString = timeString.substring(1);
        }

        // parse without operator
        int time = parseTimePeriod(timeString);

        // apply operator
        if (operator == '-')
        {
            time *= -1;
        }

        return time;
    }

    /**
     * Parses the given string for an integer value and returns it.
     * 
     * @param s
     *            string to parse
     * @return integer value contained in the given string
     * @throws ParseException
     *             thrown if the given string is an invalid integer representation
     */
    public static int parseInt(final String s) throws ParseException
    {
        ParameterCheckUtils.isNotNullOrEmpty(s, "s");

        try
        {
            return Integer.parseInt(s.trim());
        }
        catch (final NumberFormatException nfe)
        {
            throw new ParseException(String.format("Failed to parse '%s' as integer.", s), 0);
        }
    }

    /**
     * Parses the given string for to a double value and returns it.
     * 
     * @param s
     *            string to parse
     * @return double value contained in the given string
     * @throws ParseException
     *             thrown if the given string is an invalid double representation
     */
    public static double parseDouble(final String s) throws ParseException
    {
        ParameterCheckUtils.isNotNullOrEmpty(s, "s");

        try
        {
            return Double.parseDouble(s.trim());
        }
        catch (final NumberFormatException nfe)
        {
            throw new ParseException(String.format("Failed to parse '%s' as a double.", s), 0);
        }
    }

    /**
     * Parses the given string for an integer value and returns it.
     * 
     * @param s
     *            string to parse
     * @param defaultValue
     *            the value to return in case the string cannot be parsed as an integer
     * @return integer value contained in the given string
     */
    public static int parseInt(final String s, final int defaultValue)
    {
        try
        {
            return parseInt(s);
        }
        catch (final Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * Parses the given string for a long value and returns it.
     * 
     * @param s
     *            string to parse
     * @return long value contained in the given string
     * @throws ParseException
     *             thrown if the given string is an invalid long representation
     */
    public static long parseLong(final String s) throws ParseException
    {
        ParameterCheckUtils.isNotNullOrEmpty(s, "s");

        try
        {
            return Long.parseLong(s.trim());
        }
        catch (final NumberFormatException fe)
        {
            throw new ParseException(String.format("Failed to parse '%s' as long.", s), 0);
        }
    }

    /**
     * Parses the given string for a long value and returns it.
     * 
     * @param s
     *            string to parse
     * @param defaultValue
     *            the value to return in case the string cannot be parsed as a long
     * @return long value contained in the given string
     */
    public static long parseLong(final String s, final long defaultValue)
    {
        try
        {
            return parseLong(s);
        }
        catch (final Exception e)
        {
            return defaultValue;
        }
    }

    /**
     * Time period formats.
     */
    private static enum TimeFormat
    {
        /**
         * Natural format (sequence of digits).
         */
        NATURAL("(\\d+)s?"),

        /**
         * Accumulated format (HHhMMmSSs).
         */
        ACCUMULATED("(\\d+h)?(\\d+m)?(\\d+s)?"),

        /**
         * Digit format (HH:MM:SS).
         */
        DIGIT("(?:(\\d+):)?([0-5]?\\d):([0-5]?\\d)");

        /**
         * Constructs a new time format using the given regular expression.
         * 
         * @param s
         *            regular expression of the time format values
         */
        private TimeFormat(final String s)
        {
            pattern = Pattern.compile(s);
        }

        /**
         * Regular expression of this time format values.
         */
        private final Pattern pattern;

        /**
         * Returns the regular expression of this time format values.
         * 
         * @return time format value regular expression
         */
        private Pattern getPattern()
        {
            return pattern;
        }

        /**
         * Evaluates the given matching time period string.
         * 
         * @param matcher
         *            matching time period string
         * @return value of matching time period string converted to total number of seconds
         * @throws ParseException
         *             thrown by {@link ParseUtils#parseInt(String)}
         */
        private int evaluate(final Matcher matcher) throws ParseException
        {
            if (equals(TimeFormat.NATURAL))
            {
                return parseInt(matcher.group(1));
            }

            if (equals(TimeFormat.DIGIT))
            {
                int time = 0;
                final int groups = matcher.groupCount();
                for (int i = groups; i > 0; i--)
                {
                    final String group = matcher.group(i);
                    if (group == null || group.length() == 0)
                    {
                        continue;
                    }

                    final int timeVal = parseInt(matcher.group(i));
                    time += timeVal * (Math.pow(TIME_BASE, groups - i));
                }

                return time;
            }

            if (equals(TimeFormat.ACCUMULATED))
            {
                int time = 0;
                for (int i = 1; i <= matcher.groupCount(); i++)
                {
                    final String group = matcher.group(i);
                    if (group == null || group.length() == 0)
                    {
                        continue;
                    }

                    final String s = group.substring(0, group.length() - 1);
                    final int timeVal = parseInt(s);
                    final int exp = group.endsWith("h") ? 2 : (group.endsWith("m") ? 1 : 0);

                    time += timeVal * (Math.pow(TIME_BASE, exp));

                }

                return time;
            }

            return 0;
        }
    }
}
