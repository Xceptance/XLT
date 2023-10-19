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

import java.text.ParseException;

import com.xceptance.xlt.api.util.SimpleArrayList;
import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * The {@link CsvUtilsDecode} class provides helper methods to decode values from the CSV format. This is the high
 * performance and most efficient method. It will avoid copying data at all cost and move through the cache very
 * efficiently.
 * <p>
 * Note: This version is no longer the fasted one possible. It suffers from a JIT issue when it sees quoted lines before
 * regular lines. It has been replaced with {@link CsvLineDecoder}. But the new version only supports a comma as
 * delimiter for performance reasons, hence this class has still value.
 *
 * @author René Schwietzke
 * @since 7.0.0
 */
public final class CsvUtilsDecode
{
    /**
     * Character constant representing a comma.
     */
    private static final char COMMA = ',';

    /**
     * Character constant representing a double quote.
     */
    private static final char QUOTE_CHAR = '"';

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private CsvUtilsDecode()
    {
    }

    /**
     * Decodes the given CSV-encoded data record and returns the plain unquoted fields.
     *
     * @param s
     *            the CSV-encoded data record
     * @return the plain fields
     */
    public static SimpleArrayList<XltCharBuffer> parse(final String s)
    {
        return parse(new SimpleArrayList<>(50), XltCharBuffer.valueOf(s), COMMA);
    }

    // our bit flags for the parser
    @SuppressWarnings("unused")
    private static final int NONE = 0;

    private static final int IN_QUOTES = 1;

    private static final int START_MODE = 2;

    @SuppressWarnings("unused")
    private static final int SEPARATOR_EXPECTED = 4;

    private static final int LAST_WAS_SEPARATOR = 8;

    private static final int JUST_LEFT_QUOTES = 16;

    private static final int COPY_REST = 32;

    /**
     * Encodes the given fields to a CSV-encoded data record using the given field separator.
     *
     * @param list
     *            a list to append to, for memory efficiency, we hand one in instead of creating our own
     * @param src
     *            the buffer to read from
     * @param fieldSeparator
     *            the field separator to use
     * @return the CSV-encoded data record
     * @throws ParseException
     */
    public static SimpleArrayList<XltCharBuffer> parse(final SimpleArrayList<XltCharBuffer> result, final XltCharBuffer src,
                                                       final char fieldSeparator)
    {
        final int size = src.length();

        int state = START_MODE;
        int pos = 0;
        int start = 0;
        int offset = 0;

        Main:
        while (pos < size)
        {
            final char c = src.charAt(pos);

            state = state | COPY_REST;
            state = state & ~LAST_WAS_SEPARATOR;

            // do we have a quote?
            if (c == QUOTE_CHAR)
            {
                // our first quote because this all handled in the inner loop
                state = state | IN_QUOTES;

                pos++;

                // when we just started, we want to move the offset as well to avoid
                // too much copying
                if ((state & START_MODE) == START_MODE)
                {
                    offset++; // we also change the offset to avoid copying too much
                    start = offset;
                }

                // now read until we leave this state again or exhaust the buffer
                InQuotes:
                while (pos < size)
                {
                    final char c2 = src.charAt(pos);

                    if (c2 == QUOTE_CHAR)
                    {
                        // this is either the end quote or a quoted quote

                        // peak
                        final char peakedChar = src.peakAhead(pos + 1);
                        if (peakedChar == 0)
                        {
                            // there is nothing anymore, break here, pos is right because
                            // we just peaked and did not move the cursor
                            state = state | COPY_REST;

                            break Main;
                        }

                        // we have not reached the end, let's see what we got
                        if (peakedChar == QUOTE_CHAR)
                        {
                            // this is a quoted quote, preserve it and jump over
                            // it, but move the offset only by one because now
                            // everything has to be moved until we reach the end OR
                            // worse... more of these
                            src.put(offset, c2);

                            offset++;
                            pos += 2;

                            continue InQuotes;
                        }

                        // we have to jump over the quote, we are at the end
                        pos++;

                        state = state & ~(IN_QUOTES | START_MODE);
                        state = state | JUST_LEFT_QUOTES;

                        continue Main;
                    }

                    // ok, no quote, so this is just text in quotes
                    if (pos != offset)
                    {
                        src.put(offset, c2);
                    }
                    pos++;
                    offset++;

                    state = state & ~START_MODE;
                }

                // if we got here, quotes does not close
                // deal with it in a soft way at the moment and copy the rest
                state = state | COPY_REST;

                break Main;
            }

            if (c == fieldSeparator)
            {
                // if we just left the quotes, just reset because that is expected
                // or we already skipped a few characters
                state = state & ~(JUST_LEFT_QUOTES | COPY_REST);

                // we saw a separator and we start a new col
                state = state | (LAST_WAS_SEPARATOR | START_MODE);

                // we need the data
                result.add(src.viewFromTo(start, offset));

                pos++;
                offset = pos;
                start = pos;

                continue Main;
            }

            // this feature is not yet supported because we are in control of our data and don't have
            // whitespaces around the separator
            // if ((state & JUST_LEFT_QUOTES) == JUST_LEFT_QUOTES)
            // {
            // // we have left quotes, but not seen a separator, so we have garbage or spaces, ignore
            // pos++;
            //
            // continue Main;
            // }

            // move the char up if we have to
            if (pos != offset)
            {
                src.put(offset, c);
            }

            pos++;
            offset++;

            state = state & ~START_MODE;
        }

        // There is a rest to copy
        if ((state & COPY_REST) == COPY_REST)
        {
            result.add(src.viewFromTo(start, offset));
        }
        else if ((state & LAST_WAS_SEPARATOR) == LAST_WAS_SEPARATOR)
        {
            // we had a separator at the end, so we have an empty string to add
            result.add(XltCharBuffer.empty());
        }
        else if (size == 0)
        {
            // the rare case if an empty string
            result.add(XltCharBuffer.empty());
        }

        return result;
    }
}
