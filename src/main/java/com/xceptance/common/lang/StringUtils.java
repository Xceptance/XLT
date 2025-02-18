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
package com.xceptance.common.lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import com.xceptance.common.util.RegExUtils;

/**
 * Utility class for strings.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public final class StringUtils
{
    /**
     * Returns the CRC32 checksum of the given string as string representation.
     * 
     * @param s
     *            the input string
     * @return CRC32 checksum of the given string as string representation
     */
    public static String crc32(final String s)
    {
        // create the CRC32 hasher
        final CRC32 hasher = new CRC32();
        // hash the bytes of the input string
        hasher.update(s.getBytes());

        // return hash as string
        return Long.toString(hasher.getValue());
    }

    /**
     * Replaces the first substring of this string that matches the given
     * <a href="../util/regex/Pattern.html#sum">regular expression</a> with the given replacement. This is a replacement
     * method, that speeds up the processing due to internal caching of the compiled regular expression.
     * 
     * @param str
     *            the String to manipulate
     * @param regex
     *            the regular expression to which this string is to be matched
     * @param replacement
     *            the string to be substituted for the first match
     * @return The resulting <tt>String</tt>
     * @see java.lang.String#replaceFirst(String, String)
     */
    public static String replaceFirst(final String str, final String regex, final String replacement)
    {
        return RegExUtils.getPattern(regex, 0).matcher(str).replaceFirst(replacement);
    }

    /**
     * Replaces each substring of this string that matches the given <a href="../util/regex/Pattern.html#sum">regular
     * expression</a> with the given replacement. This is a replacement method, that speeds up the processing due to
     * internal caching of the compiled regular expression.
     * 
     * @param str
     *            the String to manipulate
     * @param regex
     *            the regular expression to which this string is to be matched
     * @param replacement
     *            the string to be substituted for each match
     * @return The resulting <tt>String</tt>
     * @see java.lang.String#replaceAll(String, String)
     */
    public static String replaceAll(final String str, final String regex, final String replacement)
    {
        return RegExUtils.getPattern(regex, 0).matcher(str).replaceAll(replacement);
    }

    /**
     * Replaces each substring of this string that matches the literal target sequence with the specified literal
     * replacement sequence. The replacement proceeds from the beginning of the string to the end, for example,
     * replacing "aa" with "b" in the string "aaa" will result in "ba" rather than "ab". This is a replacement method,
     * that speeds up the processing due to internal caching of the compiled regular expression.
     * 
     * @param str
     *            the String to manipulate
     * @param target
     *            The sequence of char values to be replaced
     * @param replacement
     *            The replacement sequence of char values
     * @return The resulting string
     * @see java.lang.String#replace(CharSequence, CharSequence)
     */
    public static String replace(final String str, final CharSequence target, final CharSequence replacement)
    {
        return RegExUtils.getPattern(target.toString(), Pattern.LITERAL).matcher(str)
                         .replaceAll(Matcher.quoteReplacement(replacement.toString()));
    }

    /**
     * Splits this string around matches of the given regular expression.
     * <p>
     * This is a replacement method, that speeds up the processing due to internal caching of the compiled regular
     * expression.
     * </p>
     * <p>
     * The length of the resulting array is limited by the given parameter value.
     * </p>
     * 
     * @param str
     *            the String to manipulate
     * @param regex
     *            the delimiting regular expression
     * @param limit
     *            the result threshold, as described above
     * @return the array of strings computed by splitting this string around matches of the given regular expression if
     *         the regular expression's syntax is invalid
     * @see java.lang.String#split(String, int)
     */
    public static String[] split(final String str, final String regex, final int limit)
    {
        return RegExUtils.getPattern(regex, 0).split(str, limit);
    }

    /**
     * Splits this string around matches of the given <a href="../util/regex/Pattern.html#sum">regular expression</a>.
     * This is a replacement method, that speeds up the processing due to internal caching of the compiled regular
     * expression.
     * 
     * @param str
     *            the String to manipulate
     * @param regex
     *            the delimiting regular expression
     * @return the array of strings computed by splitting this string around matches of the given regular expression
     * @see java.lang.String#split(String)
     */
    public static String[] split(final String str, final String regex)
    {
        return split(str, regex, 0);
    }

    /**
     * Join the given elements into a String separated by the given delimiter. The given limit determines how many
     * elements from the collection will be joined. The given suffix will be added to the resulting String if there are
     * more elements than the limit states.
     *
     * @param delimiter
     *            the delimiting char sequence
     * @param elements
     *            the elements to join
     * @param limit
     *            the max number of elements that will be joined
     * @param suffix
     *            the suffix to add to the resulting String if the limit was exceeded
     * @return the resulting String
     */
    public static String join(final CharSequence delimiter, final List<? extends CharSequence> elements, final int limit,
                              final CharSequence suffix)
    {
        if (elements.size() > limit)
        {
            return String.join(delimiter, elements.subList(0, limit)) + suffix;
        }
        else
        {
            return String.join(delimiter, elements);
        }
    }

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private StringUtils()
    {
    }
}
