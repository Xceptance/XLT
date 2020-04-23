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

import org.apache.commons.lang3.StringUtils;

/**
 * The {@link StringMatcher} is used to check whether a string does (not) comply with certain rules, which are defined
 * by regular expressions. The string is checked whether it:
 * <ul>
 * <li>does NOT match a set of "exclude" reg-ex patterns and</li>
 * <li>does match a set of "include" reg-ex patterns.</li>
 * </ul>
 * Usually, either the "include" or the "exclude" setting is used, whichever is easier to define. If both settings are
 * used at the same time, the "exclude" patterns take precedence. An empty pattern set means that any string is included
 * and none is excluded, respectively.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class StringMatcher
{
    /**
     * The list of patterns describing the strings NOT to accept.
     */
    private final List<Pattern> excludePatterns;

    /**
     * The list of patterns describing the strings to accept.
     */
    private final List<Pattern> includePatterns;

    /**
     * whether the patterns must match the whole input string or can match a substring only.
     */
    private final boolean fullMatch;

    /**
     * Creates a new StringMatcher object and initializes it with the given include and exclude patterns.
     * 
     * @param includePatternsSpec
     *            a string with the include patterns, may be null
     * @param excludePatternsSpec
     *            a string with the exclude patterns, may be null
     */
    public StringMatcher(final String includePatternsSpec, final String excludePatternsSpec)
    {
        this(includePatternsSpec, excludePatternsSpec, false);
    }

    /**
     * Creates a new StringMatcher object and initializes it with the given include and exclude patterns.
     * 
     * @param includePatternsSpec
     *            a string with the include patterns, may be null
     * @param excludePatternsSpec
     *            a string with the exclude patterns, may be null
     * @param fullMatch
     *            whether the patterns must match the whole input string (<code>true</code>) or a substring only (
     *            <code>false</code>)
     */
    public StringMatcher(final String includePatternsSpec, final String excludePatternsSpec, boolean fullMatch)
    {
        includePatterns = buildPatternList(includePatternsSpec);
        excludePatterns = buildPatternList(excludePatternsSpec);
        this.fullMatch = fullMatch;
    }

    /**
     * Creates a list of Pattern objects from the passed regex patterns string. The regex patterns are separated by
     * commas.
     * 
     * @param patternsString
     *            the list of regex pattern strings
     * @return the list of patterns objects
     */
    private List<Pattern> buildPatternList(final String patternsString)
    {
        final List<Pattern> patterns = new ArrayList<Pattern>();

        if (patternsString != null)
        {
            for (final String patternString : StringUtils.split(patternsString, " ,;"))
            {
                final Pattern pattern = Pattern.compile(patternString);
                patterns.add(pattern);
            }
        }

        return patterns;
    }

    /**
     * Checks whether the passed string is accepted.
     * 
     * @param s
     *            the string to check
     * @return whether the string is accepted
     */
    public boolean isAccepted(final String s)
    {
        // do not accept the string if it matches an exclude pattern
        for (final Pattern pattern : excludePatterns)
        {
            final Matcher matcher = pattern.matcher(s);
            if (!fullMatch && matcher.find() || fullMatch && matcher.matches())
            {
                return false;
            }
        }

        // do accept the string if there are no include patterns
        if (includePatterns.isEmpty())
        {
            return true;
        }

        // do accept the string if it matches an include pattern
        for (final Pattern pattern : includePatterns)
        {
            final Matcher matcher = pattern.matcher(s);
            if (!fullMatch && matcher.find() || fullMatch && matcher.matches())
            {
                return true;
            }
        }

        // do not accept the string
        return false;
    }
}
