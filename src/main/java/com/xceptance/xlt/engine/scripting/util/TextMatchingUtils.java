/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.scripting.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to match text against a pattern using one of the built-in text matching strategies. This class behaves
 * slightly different than similar code in Selenium as it normalizes (sequences of) whitespace characters to a single
 * space character before trying to match a text against a pattern.
 */
public final class TextMatchingUtils
{
    /**
     * A regex to locate whitespace.
     */
    private static final Pattern NORMALIZE_WHITESPACE_PATTERN = Pattern.compile("\\s+");

    /**
     * A regex to split Selenium patterns into strategy name and value.
     */
    private static final Pattern TEXT_MATCHING_STRATEGY_AND_VALUE_PATTERN = Pattern.compile("^(\\p{Alpha}+):(.*)", Pattern.DOTALL);

    /**
     * The default text matching strategy ("glob").
     */
    private static final TextMatchingStrategy DEFAULT_TEXT_MATCHING_STRATEGY = new GlobTextMatchingStrategy();

    /**
     * The built-in text matching strategies.
     */
    private static final Map<String, TextMatchingStrategy> textMatchingStrategies;

    static
    {
        textMatchingStrategies = new HashMap<String, TextMatchingStrategy>();

        textMatchingStrategies.put("glob", DEFAULT_TEXT_MATCHING_STRATEGY);
        textMatchingStrategies.put("regexp", new RegExTextMatchingStrategy());
        textMatchingStrategies.put("regexpi", new RegExTextMatchingStrategy(true));
        textMatchingStrategies.put("exact", new ExactTextMatchingStrategy());
    }

    /**
     * Determines whether the given text matches the specified pattern. The pattern might be prefixed with a strategy
     * name, e.g. "exact:***".
     * 
     * @param text
     *            the text to check
     * @param pattern
     *            the pattern to match
     * @return <code>true</code> if the text matches the pattern, <code>false</code> otherwise
     */
    public static boolean isAMatch(String text, String pattern, final boolean strict, final boolean normalizeWhitespace)
    {
        pattern = normalizeWhitespace ? pattern.trim() : pattern;

        // determine the text matching strategy from the pattern
        TextMatchingStrategy strategy = null;
        String use = null;

        final Matcher matcher = TEXT_MATCHING_STRATEGY_AND_VALUE_PATTERN.matcher(pattern);
        if (matcher.matches())
        {
            final String strategyName = matcher.group(1);
            use = matcher.group(2);

            strategy = textMatchingStrategies.get(strategyName);
        }

        if (strategy == null)
        {
            // no or unknown strategy given in the pattern -> fall back to default
            strategy = DEFAULT_TEXT_MATCHING_STRATEGY;
            use = pattern;
        }

        // normalize whitespace
        if (normalizeWhitespace)
        {
            use = normalizeWhitespace(use);
            text = normalizeWhitespace(text);
        }

        // perform the check
        return strategy.isAMatch(use, text, strict);
    }

    /**
     * Normalizes any whitespace in the given text such that any sequence of whitespace characters is replaced with a
     * single space character.
     * 
     * @param s
     *            the text
     * @return the text with normalized whitespace
     */
    private static String normalizeWhitespace(String s)
    {
        // convert non-breaking spaces to normal spaces first
        s = s.replace('\u00a0', ' ');

        // now trim to get rid of trailing spaces
        s = s.trim();

        return NORMALIZE_WHITESPACE_PATTERN.matcher(s).replaceAll(" ");
    }

    /**
     * Private constructor to avoid object instantiation.
     */
    private TextMatchingUtils()
    {
    }
}
