/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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

/**
 * Helper class that checks a given string for a correct balance of opening and closing delimiters.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class Balancer
{
    /**
     * The opening delimiter.
     */
    private final String openingDelimiter;

    /**
     * The closing delimiter.
     */
    private final String closingDelimiter;

    /**
     * The error message string.
     */
    private static final String ERROR_MSG = "The string '%s' is unbalanced.";

    /**
     * Constructor.
     * 
     * @param openingDelimiter
     *            the opening delimiter to be used
     * @param closingDelimiter
     *            the closing delimiter to be used
     */
    public Balancer(final String openingDelimiter, final String closingDelimiter)
    {
        // validate parameters
        ParameterCheckUtils.isNotNullOrEmpty(openingDelimiter, "openingDelimiter");
        ParameterCheckUtils.isNotNullOrEmpty(closingDelimiter, "closingDelimiter");

        this.openingDelimiter = openingDelimiter;
        this.closingDelimiter = closingDelimiter;
    }

    /**
     * Checks the given string for a correct balance of start and stop delimiters.
     * 
     * @param s
     *            the string to be checked
     * @throws IllegalArgumentException
     *             thrown when the given string is unbalanced.
     */
    public void check(final String s)
    {
        // parameter validation
        if (s == null || s.length() == 0)
        {
            return;
        }

        // initialize counter
        int counter = 0;

        // determine minimum and maximum length of delimiters
        final int delimMinLength = Math.min(openingDelimiter.length(), closingDelimiter.length());
        final int delimMaxLength = Math.max(openingDelimiter.length(), closingDelimiter.length());

        // loop through the characters of the input string
        for (int i = 0; i < s.length() - delimMinLength + 1; i++)
        {

            final boolean eatMost = i < s.length() - delimMaxLength + 1;
            final String snippet = s.substring(i, i + (eatMost ? delimMaxLength : delimMinLength));

            final int result = checkSnippet(snippet);
            if (result < 0)
            {
                if (counter == 0)
                {
                    continue;
                    // final String errMsg = String.format(ERROR_MSG, s);
                    // throw new IllegalArgumentException("Expected '" + openingDelimiter + "' at index " + i
                    // + " of input string. " + errMsg);
                }

                i += closingDelimiter.length() - 1;
            }
            else if (result > 0)
            {
                i += openingDelimiter.length() - 1;
            }

            counter += result;

        }

        if (counter != 0)
        {
            final String errMsg = String.format(ERROR_MSG, s);
            throw new IllegalArgumentException("There are " + counter + " unclosed references to variable values. " + errMsg);
        }
    }

    /**
     * Checks a given snippet for occurrence of an opening or closing delimiter respectively.
     * 
     * @param snippet
     *            the snippet to check
     * @return <code>1</code> if the given snippet contains an opening delimiter, <code>-1</code> if the snippet contains
     *         a closing delimiter and <code>0</code> otherwise
     */
    private int checkSnippet(final String snippet)
    {
        if (snippet.length() >= openingDelimiter.length())
        {
            if (snippet.startsWith(openingDelimiter))
            {
                return 1;
            }
        }

        if (snippet.length() >= closingDelimiter.length())
        {
            if (snippet.startsWith(closingDelimiter))
            {
                return -1;
            }
        }

        return 0;
    }

}
