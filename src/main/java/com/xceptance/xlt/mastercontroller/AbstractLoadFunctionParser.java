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
package com.xceptance.xlt.mastercontroller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import com.xceptance.common.util.AbsoluteOrRelativeNumber;
import com.xceptance.common.util.ParseUtils;
import com.xceptance.common.util.RegExUtils;

/**
 * The base class for load function parsers. A load function is defined by a sequence of time/value pairs, separated by
 * whitespace, comma, or semicolon. A time/value pair consists of a time specification followed by a slash and a value.
 * Accepts any time format that is accepted by XLT in general. Time and value can be provided in an absolute format
 * (e.g. "1m/10") or a relative format (e.g. "+1m/-10") in relation to the previous value.
 * <p>
 * Examples:<br>
 * good: <code>0s/100, +30s/150, 30m10s/+200, +1h/-250</code><br>
 * bad: <code>0/100, 0:30 / 150; 30m 10 s/ 200    3600 /250</code>
 */
public abstract class AbstractLoadFunctionParser
{
    /**
     * Regex to match (consecutive) record separators.
     */
    private static final String RE_RECORD_SEP = "[,;\\s]*";

    /**
     * Regex to match characters up to the next field separator.
     */
    private static final String RE_TIME = "[^/]+";

    /**
     * Regex to match (consecutive) field separators and whitespace in between.
     */
    private static final String RE_FIELD_SEP = "[/\\s]+";

    /**
     * Regex to match characters up to the next record separator. Value might start with "+" or "-", optionally followed
     * by whitespaces, before the remaining value is given (examples of valid values: "12", "-12", "+ 12").
     */
    private static final String RE_VALUE = "([+-]\\s*)?[^,;\\s]+";

    /**
     * Parses the passed load function string into a sequence of time/value pairs.
     *
     * @param loadFunction
     *            the load function
     * @return an array of time value pairs, with the time at index 0 and the value at index 1
     * @throws ParseException
     *             if the load function could not be parsed properly
     */
    public int[][] parse(final String loadFunction) throws ParseException
    {
        final List<int[]> pairs = new ArrayList<>();

        final Matcher recordSeparatorMatcher = RegExUtils.getPattern(RE_RECORD_SEP).matcher(loadFunction);
        final Matcher timeMatcher = RegExUtils.getPattern(RE_TIME).matcher(loadFunction);
        final Matcher fieldSeparatorMatcher = RegExUtils.getPattern(RE_FIELD_SEP).matcher(loadFunction);
        final Matcher valueMatcher = RegExUtils.getPattern(RE_VALUE).matcher(loadFunction);

        final int l = loadFunction.length();
        int i = 0;
        AbsoluteOrRelativeNumber<Integer> time;

        while (i < l)
        {
            // read record separator(s)
            if (recordSeparatorMatcher.find(i) && recordSeparatorMatcher.start() == i)
            {
                i = recordSeparatorMatcher.end();

                // leave loop if we reached the end of the string
                if (i == l)
                {
                    break;
                }
            }

            // read the time
            if (timeMatcher.find(i) && timeMatcher.start() == i)
            {
                i = timeMatcher.end();
                time = ParseUtils.parseAbsoluteOrRelative(ParseUtils::parseTimePeriod, timeMatcher.group());
            }
            else
            {
                throw new ParseException("Expected a time at index " + i, i);
            }

            // read field separator(s)
            if (fieldSeparatorMatcher.find(i) && fieldSeparatorMatcher.start() == i)
            {
                i = fieldSeparatorMatcher.end();
            }
            else
            {
                throw new ParseException("Expected a '/' at index " + i, i);
            }

            // read the value
            if (valueMatcher.find(i) && valueMatcher.start() == i)
            {
                i = valueMatcher.end();
                final AbsoluteOrRelativeNumber<Integer> value = ParseUtils.parseAbsoluteOrRelative(this::parseValue, valueMatcher.group());

                // for the first parsed pair, check if it's a valid starting point for the load function
                if (pairs.isEmpty() && !LoadFunctionUtils.isValidStartingPoint(time, value))
                {
                    // if the first pair isn't a valid starting point, add the default starting point first
                    pairs.add(new int[]
                        {
                            LoadFunctionUtils.START_TIME, LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR
                        });
                }

                // if time or value are relative, add them to the previous time or value
                final int effectiveTime = time.isRelativeNumber() ? pairs.getLast()[0] + time.getValue() : time.getValue();
                final int effectiveValue = value.isRelativeNumber() ? pairs.getLast()[1] + value.getValue() : value.getValue();
                pairs.add(new int[]
                    {
                        effectiveTime, effectiveValue
                    });
            }
            else
            {
                throw new ParseException("Expected a value at index " + i, i);
            }
        }

        // check if we have at least one pair
        final int size = pairs.size();
        if (size == 0)
        {
            throw new IllegalArgumentException("The load function must specify at least one time/value pair.");
        }

        // finally convert the list of pairs to an array
        final int[][] result = pairs.toArray(new int[size][]);

        return result;
    }

    /**
     * Parses the value of a time/value pair and returns it as its corresponding int value.
     *
     * @param s
     *            the value string
     * @return the int value that corresponds to the value string
     * @throws ParseException
     *             if the value cannot be parsed properly
     */
    abstract int parseValue(String s) throws ParseException;
}
