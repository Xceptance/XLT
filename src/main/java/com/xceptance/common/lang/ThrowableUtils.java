/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for Throwables.
 */
public final class ThrowableUtils
{
    /**
     * The regular expression that matches the lines that appears in every failure stack trace, which can therefore be
     * used to cut off the non-interesting part of a stack trace.
     */
    private static final String CUT_OFF_REGEX = "((at (sun|(java\\.base/)?jdk\\.internal)\\.reflect\\.[^\n]+?\n\t)+"
                                                + "at (java\\.base/)?java\\.lang\\.reflect\\.[^\n]+?\n\t"
                                                + "at org\\.junit\\.runners\\.model\\.FrameworkMethod\\$1.runReflectiveCall"
                                                + ".+?)(Caused\\s+by|$)";

    /**
     * The pattern that finds the non-interesting part of a stack trace.
     */
    private static final Pattern CUT_OFF_PATTERN = Pattern.compile(CUT_OFF_REGEX, Pattern.DOTALL);

    /**
     * The regular expression string that finds dump directory information embedded in exception messages.
     */
    public static final String DIRECTORY_HINT_REGEX = " \\(user: '(.+)', output: '([0-9]+)'\\)";

    private static Field DETAIL_MESSAGE_FIELD;

    static
    {
        // HACK: make the private field "detailMessage" accessible
        try
        {
            DETAIL_MESSAGE_FIELD = Throwable.class.getDeclaredField("detailMessage");
            DETAIL_MESSAGE_FIELD.setAccessible(true);
        }
        catch (final Exception e)
        {
            DETAIL_MESSAGE_FIELD = null;
        }
    }

    /**
     * Sets the new message text at the given Throwable object.
     * 
     * @param t
     *            the throwable
     * @param message
     *            the message
     */
    public static void setMessage(final Throwable t, final String message)
    {
        if (DETAIL_MESSAGE_FIELD != null)
        {
            try
            {
                DETAIL_MESSAGE_FIELD.set(t, message);
            }
            catch (final Exception e)
            {
            }
        }
    }

    /**
     * Prefixes the message of a Throwable object with the given string.
     * 
     * @param t
     *            the throwable
     * @param messagePrefix
     *            the message prefix
     */
    public static void prefixMessage(final Throwable t, final String messagePrefix)
    {
        if (messagePrefix == null)
        {
            return;
        }

        final String originalMessage = getMessage(t);
        final String newMessage = (originalMessage == null) ? messagePrefix : messagePrefix + originalMessage;

        setMessage(t, newMessage);
    }

    /**
     * Constructor.
     */
    private ThrowableUtils()
    {
    }

    /**
     * Gets the detail message of the given throwable.
     * 
     * @param t
     *            the throwable
     * @return detail message of given throwable
     */
    public static String getMessage(final Throwable t)
    {
        if (DETAIL_MESSAGE_FIELD != null)
        {
            try
            {
                return (String) DETAIL_MESSAGE_FIELD.get(t);
            }
            catch (final Exception e)
            {
            }
        }

        return t.getMessage();
    }

    /**
     * Returns the stack trace of the given {@link Throwable} as string
     * 
     * @param t
     *            the {@link Throwable}
     * @return stack trace of given {@link Throwable}
     */
    public static String getStackTrace(final Throwable t)
    {
        final StringWriter sw = new StringWriter(400);
        final PrintWriter pw = new PrintWriter(sw);

        t.printStackTrace(pw);

        return sw.toString();
    }

    /**
     * Returns the minified (non-interesting parts cut-off) stacktrace of the given throwable.
     * 
     * @param t
     *            the throwable whose stacktrace should be minified
     * @return minified stacktrace of given throwable
     */
    public static String getMinifiedStackTrace(final Throwable t)
    {
        final String stackTrace = getStackTrace(t);

        // cut off the non-interesting part of the stack trace
        int startIdx = 0;
        final StringBuilder sb = new StringBuilder(1024);
        final Matcher matcher = CUT_OFF_PATTERN.matcher(stackTrace);
        while (matcher.find())
        {
            if (startIdx > 0)
            {
                sb.append(System.getProperty("line.separator", "\n"));
            }

            sb.append(stackTrace.substring(startIdx, matcher.start())).append("...");
            startIdx = matcher.end(1);
        }

        if (startIdx < stackTrace.length())
        {
            final String tail = stackTrace.substring(startIdx);
            if (StringUtils.isNotBlank(tail))
            {
                if (startIdx > 0)
                {
                    sb.append(System.getProperty("line.separator", "\n"));
                }

                sb.append(tail);
            }
        }

        return sb.toString();
    }
}
