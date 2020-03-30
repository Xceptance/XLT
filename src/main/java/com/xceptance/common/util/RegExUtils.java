/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.collection.ConcurrentLRUCache;

/**
 * The RegExUtils class provides some helper functions to make dealing with regular expressions easier.
 * <p>
 * Note that regular expressions passed in as a string are compiled with the Pattern.DOTALL and Pattern.MULTILINE flags
 * set. If this does not fit your needs, use the methods that take a compiled pattern and provide the appropriate
 * pattern object yourself. Furthermore, to improve performance regex strings - once compiled to pattern objects - are
 * cached internally for later re-use.
 * 
 * @author Jörg Werner (Xceptance Softare Technologies GmbH)
 */
public final class RegExUtils
{
    /**
     * Maps regular expression strings to their compiled equivalents.
     */
    private static final ConcurrentLRUCache<CacheKey, Pattern> patternCache = new ConcurrentLRUCache<CacheKey, Pattern>(5003);

    /**
     * Maps regular expression evaluation results to the string to save operations
     */
    private static final ConcurrentLRUCache<String, String> resultCache = new ConcurrentLRUCache<String, String>(1001);

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private RegExUtils()
    {
        // Nothing to do
    }

    /**
     * Returns how many sub strings of the specified input string match the passed pattern.
     * 
     * @param s
     *            the input string
     * @param pattern
     *            the pattern to match
     * @return the number of matches
     */
    public static int getMatchingCount(final String s, final Pattern pattern)
    {
        int count = 0;

        final Matcher matcher = pattern.matcher(s);
        while (matcher.find())
        {
            count++;
        }

        return count;
    }

    /**
     * Returns how many sub strings of the specified input string match the passed pattern.
     * 
     * @param s
     *            the input string
     * @param regex
     *            the pattern to match
     * @return the number of matches
     */
    public static int getMatchingCount(final String s, final String regex)
    {
        return getMatchingCount(s, getPattern(regex));
    }

    /**
     * Returns the compiled equivalent of the passed regular expression string. This method tries to improve performance
     * by caching the compiled pattern, so subsequent calls with the same regex will return the previously cached
     * pattern. The regex will be compiled with DOTALL and MULTILINE.
     * 
     * @param regex
     *            the regular expression string
     * @return the corresponding pattern object
     */
    public static Pattern getPattern(final String regex)
    {
        return getPattern(regex, Pattern.DOTALL | Pattern.MULTILINE);
    }

    /**
     * Returns the compiled equivalent of the passed regular expression string. This method tries to improve performance
     * by caching the compiled pattern, so subsequent calls with the same regex will return the previously cached
     * pattern.
     * 
     * @param regex
     *            the regular expression string
     * @param flags
     *            global flags to be used when compiling the regular expression
     * @throws PatternSyntaxException
     *             if the regular expression is invalid
     * @return the corresponding pattern object
     */
    public static Pattern getPattern(final String regex, final int flags)
    {
        // cache string to take flags into account
        final CacheKey cacheKey = new CacheKey(regex, flags);

        Pattern pattern = patternCache.get(cacheKey);
        if (pattern == null)
        {
            pattern = Pattern.compile(regex, flags);
            patternCache.put(cacheKey, pattern);
        }

        return pattern;
    }

    /**
     * Indicates whether there is at least one sub string of the specified input string that matches the passed pattern.
     * 
     * @param s
     *            the input string
     * @param pattern
     *            the pattern to match
     * @return true if the input matches the pattern
     */
    public static boolean isMatching(final String s, final Pattern pattern)
    {
        final Matcher matcher = pattern.matcher(s);

        return matcher.find();
    }

    /**
     * Indicates whether there is at least one sub string of the specified input string that matches the passed pattern.
     * 
     * @param s
     *            the input string
     * @param regex
     *            the pattern to match
     * @return whether the input matches the pattern
     */
    public static boolean isMatching(final String s, final String regex)
    {
        return isMatching(s, getPattern(regex));
    }

    /**
     * Returns all sub strings of the specified input string that match the passed pattern.
     * 
     * @param s
     *            the input string
     * @param pattern
     *            the pattern to match
     * @return a list of matching sub strings
     */

    public static List<String> getAllMatches(final String s, final Pattern pattern)
    {
        return getAllMatches(s, pattern, 0);
    }

    /**
     * Finds all sub strings of the specified input string that match the passed pattern and, for each sub string,
     * returns the value of the matching group with the given index.
     * 
     * @param s
     *            the input string
     * @param pattern
     *            the pattern to match
     * @param matchingGroupIndex
     *            the matching group in question
     * @return a list with the values of the specified matching group
     */
    public static List<String> getAllMatches(final String s, final Pattern pattern, final int matchingGroupIndex)
    {
        final List<String> values = new ArrayList<String>();

        final Matcher matcher = pattern.matcher(s);
        while (matcher.find())
        {
            final String value = matcher.group(matchingGroupIndex);
            values.add(value);
        }

        return values;
    }

    /**
     * Returns all sub strings of the specified input string that match the passed pattern.
     * 
     * @param s
     *            the input string
     * @param regex
     *            the pattern to match
     * @return a list of matching sub strings
     */
    public static List<String> getAllMatches(final String s, final String regex)
    {
        return getAllMatches(s, getPattern(regex), 0);
    }

    /**
     * Finds all sub strings of the specified input string that match the passed pattern and, for each sub string,
     * returns the value of the matching group with the given index.
     * 
     * @param s
     *            the input string
     * @param regex
     *            the regular expression to match
     * @param matchingGroupIndex
     *            the matching group in question
     * @return a list with the values of the specified matching group
     */
    public static List<String> getAllMatches(final String s, final String regex, final int matchingGroupIndex)
    {
        return getAllMatches(s, getPattern(regex), matchingGroupIndex);
    }

    /**
     * Returns the first sub string of the specified input string that matches the passed pattern.
     * 
     * @param s
     *            the input string
     * @param pattern
     *            the pattern to match
     * @return the first matching sub string
     */
    public static String getFirstMatch(final String s, final Pattern pattern)
    {
        return getFirstMatch(s, pattern, 0);
    }

    /**
     * Finds the first sub string of the specified input string that matches the passed pattern and returns the value of
     * the matching group with the given index.
     * 
     * @param s
     *            the input string
     * @param pattern
     *            the pattern to match
     * @param matchingGroupIndex
     *            the matching group in question
     * @return the value of the specified matching group
     */
    public static String getFirstMatch(final String s, final Pattern pattern, final int matchingGroupIndex)
    {
        String value = null;

        final Matcher matcher = pattern.matcher(s);
        if (matcher.find())
        {
            value = matcher.group(matchingGroupIndex);
        }

        return value;
    }

    /**
     * Returns the first sub string of the specified input string that matches the passed pattern.
     * 
     * @param s
     *            the input string
     * @param regex
     *            the pattern to match
     * @return the first matching sub string
     */
    public static String getFirstMatch(final String s, final String regex)
    {
        return getFirstMatch(s, getPattern(regex));
    }

    /**
     * Finds the first sub string of the specified input string that matches the passed pattern and returns the value of
     * the matching group with the given index.
     * 
     * @param s
     *            the input string
     * @param regex
     *            the pattern to match
     * @param matchingGroupIndex
     *            the matching group in question
     * @return the value of the specified matching group
     */
    public static String getFirstMatch(final String s, final String regex, final int matchingGroupIndex)
    {
        return getFirstMatch(s, getPattern(regex), matchingGroupIndex);
    }

    /**
     * Finds all matches for the given regular expression in the given string s and replaces them with the given
     * replacement string. Similar to {@link String#replaceAll(String, String)}, but the pattern compiled from the
     * regular expression is cached internally.
     * 
     * @param s
     *            the input string
     * @param regex
     *            the regular expression to be matched
     * @param replacement
     *            the replacement string
     * @return the modified input string
     */
    public static String replaceAll(final String s, final String regex, final String replacement)
    {
        final Pattern pattern = getPattern(regex);
        final Matcher matcher = pattern.matcher(s);

        return matcher.replaceAll(replacement);
    }

    /**
     * Returns the given string after it has been escaped.
     * 
     * @param s
     *            the string to be escaped
     * @return escaped string
     */
    public static String escape(final String s)
    {
        // parameter validation
        if (s == null || s.length() == 0)
        {
            return s;
        }

        // check the cache to save computation
        String cachedVal = resultCache.get(s);
        // cache miss
        if (cachedVal == null)
        {
            // get quoted regex and fill cache
            cachedVal = Pattern.quote(s);
            resultCache.put(s, cachedVal);
        }

        return cachedVal;
    }

    /**
     * Returns the number of capturing groups defined by the given regular expression.
     * 
     * @param pattern
     *            the regular expression
     * @return number of capturing groups or <code>-1</code> if passed regular expression is empty (or <code>null</code>)
     */
    public static int getCaptureGroupCount(String pattern)
    {
        if (pattern == null || pattern.length() == 0)
        {
            return -1;
        }

        // 1st step: Remove all escaped chars.
        pattern = replaceAll(pattern, "\\\\.", "");
        // 2nd step: Remove all character classes.
        pattern = replaceAll(pattern, "\\[[^\\]]+\\]", "");
        // 3rd step. Now count all left parentheses that are not followed by a question mark.
        return getMatchingCount(pattern, "\\((?![?])");
    }

    /**
     * Helper to get the caching right
     * 
     * @author rschwietzke
     */
    static class CacheKey
    {
        private final String pattern;

        private final int flags;

        private final int hashCode;

        public CacheKey(final String pattern, final int flags)
        {
            super();
            this.pattern = pattern;
            this.flags = flags;

            // Since CacheKey is immutable compute hash code initially for better performance
            final int prime = 31;
            int result = 1;
            result = prime * result + flags;
            result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
            hashCode = result;
        }

        @Override
        public int hashCode()
        {
            return hashCode;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final CacheKey other = (CacheKey) obj;
            if (flags != other.flags)
            {
                return false;
            }
            if (pattern == null)
            {
                if (other.pattern != null)
                {
                    return false;
                }
            }
            else if (!pattern.equals(other.pattern))
            {
                return false;
            }
            return true;
        }
    }

    /**
     * Remove all matchings for the given regular expression from the given string. This method makes use of pattern
     * caching.
     * 
     * @param s
     *            the string to parse
     * @param regex
     *            the regular expression to match
     * @return the process result
     */
    public static String removeAll(final String s, final String regex)
    {
        return removeAll(s, regex, 0);
    }

    /**
     * Remove the given group from the regular expression's matching. Note, not the group is removed from the whole
     * string. This method makes use of pattern caching.
     * <p>
     * Examples:
     * <table border=1>
     * <thead style='font-weight:bold'>
     * <tr>
     * <td>Sample text</td>
     * <td>Regular Expression</td>
     * <td>Group</td>
     * <td>Result</td>
     * <td>Comment</td>
     * </tr>
     * </thead>
     * <tr>
     * <td>FooBoo</td>
     * <td>F(o)o</td>
     * <td>0</td>
     * <td>Boo</td>
     * <td style='font-style:italic'>Remove completely</td>
     * </tr>
     * <tr>
     * <td>FooBooFooBoo</td>
     * <td>F(o)o</td>
     * <td>1</td>
     * <td>FoBooFoBoo</td>
     * <td style='font-style:italic'>'o' is not removed from complete string but from current matching</td>
     * </tr>
     * </table>
     * </p>
     * 
     * @param s
     *            string to parse
     * @param regex
     *            regular expression to match
     * @param group
     *            the regular expression's group to remove from the string
     * @return the process result
     */
    public static String removeAll(final String s, final String regex, final int group)
    {
        // is there something to do?
        if (StringUtils.isEmpty(s) || StringUtils.isEmpty(regex))
        {
            return s;
        }

        final StringBuilder result = new StringBuilder(s);

        // parse the string with the (cached) matcher
        final Matcher matcher = getPattern(regex).matcher(s);
        int matchIndex = 0;
        while (matcher.find())
        {
            // get the complete matching as well as the desired group
            final String matching = matcher.group(0);
            final String groupMatching = matcher.group(group);
            
            // stop , if no matching for this group
            if(StringUtils.isEmpty(groupMatching))
            {
                break;
            }
            
            // remove the group from the matching
            final String modifiedMatching = StringUtils.substringBefore(matching, groupMatching) +
                                            StringUtils.substringAfter(matching, groupMatching);

            // OK, it's a change. So build the new result
            result.replace(matcher.start() - matchIndex, matcher.end() - matchIndex, modifiedMatching);
            matchIndex += groupMatching.length();

            // and position the matcher behind the last matching
            matcher.region(matcher.end(), s.length());
        }
        return result.toString();
    }
}
