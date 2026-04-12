/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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

import com.xceptance.xlt.api.util.SimpleArrayList;
import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * A high-performance CSV decoder that operates directly on {@code byte[]} arrays,
 * avoiding the overhead of {@code InputStreamReader} char-doubling. Fields are decoded
 * into {@link XltCharBuffer} instances for compatibility with the existing
 * {@code Data.setBaseValues()} / {@code setRemainingValues()} API.
 * <p>
 * This decoder supports comma-delimited fields with standard CSV double-quote escaping.
 * For numeric fields (the majority of XLT timer data), the byte-to-char conversion is
 * trivial (ASCII subset), avoiding full charset decoding overhead.
 *
 *
 * @implNote Exclusively created by AI (Antigravity).
 * @author Xceptance
 * @since 10.0
 */
public final class ByteCsvDecoder
{
    /**
     * Byte constant representing a comma.
     */
    private static final byte COMMA = (byte) ',';

    /**
     * Byte constant representing a double quote.
     */
    private static final byte QUOTE = (byte) '"';

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private ByteCsvDecoder()
    {
    }

    /**
     * Decodes a CSV-encoded line from raw bytes and returns a filled {@link CsvByteRow}.
     *
     * @param data
     *            the raw byte array containing the CSV line
     * @param offset
     *            the starting offset in the array
     * @param length
     *            the number of bytes to process
     * @return a new CsvByteRow parsed fields
     */
    public static final CsvByteRow parse(final byte[] data, final int offset, final int length)
    {
        return parse(new CsvByteRow(), data, offset, length);
    }

    /**
     * Decodes a CSV-encoded line from raw bytes into the provided result row.
     *
     * @param row
     *            a CsvByteRow to append fields to (for memory efficiency, reused across calls)
     * @param data
     *            the raw byte array containing the CSV line
     * @param offset
     *            the starting offset in the array
     * @param length
     *            the number of bytes to process
     * @return the row passed in, filled with the parsed fields
     */
    public static final CsvByteRow parse(final CsvByteRow row,
                                                       final byte[] data,
                                                       final int offset,
                                                       final int length)
    {
        row.reset(data);
        final int end = offset + length;

        // empty case
        if (length == 0)
        {
            row.add(0, 0, false);
            return row;
        }

        int pos = offset;
        while (pos < end)
        {
            final byte b = data[pos];

            if (b == QUOTE)
            {
                pos = parseQuotedField(row, data, pos, end);
            }
            else
            {
                pos = parseUnquotedField(row, data, pos, end);
            }
        }

        // trailing comma means an additional empty field
        if (data[end - 1] == COMMA)
        {
            row.add(end, 0, false);
        }

        return row;
    }

    /**
     * Convenience method: parse an entire byte array as a single CSV line.
     *
     * @param data
     *            the raw byte array
     * @return a list of parsed fields
     */
    public static final CsvByteRow parse(final byte[] data)
    {
        return parse(data, 0, data.length);
    }

    /**
     * Convenience method with reusable result row.
     *
     * @param row
     *            the reusable result row
     * @param data
     *            the raw byte array
     * @return the result list filled with parsed fields
     */
    public static final CsvByteRow parse(final CsvByteRow row, final byte[] data)
    {
        return parse(row, data, 0, data.length);
    }

    /**
     * Parses an unquoted field. Reads bytes until a comma or end of data.
     */
    private static final int parseUnquotedField(final CsvByteRow row,
                                          final byte[] data,
                                          final int start,
                                          final int end)
    {
        int pos = start;

        while (pos < end)
        {
            if (data[pos] == COMMA)
            {
                row.add(start, pos - start, false);
                return pos + 1;
            }
            pos++;
        }

        // reached end of data
        row.add(start, pos - start, false);
        return pos;
    }

    /**
     * Parses a quoted field. Handles embedded double-quotes (escaped as "").
     */
    private static final int parseQuotedField(final CsvByteRow row,
                                        final byte[] data,
                                        final int start,
                                        final int end)
    {
        // skip opening quote
        int pos = start + 1;
        final int fieldStart = pos;

        // fast path: scan for closing quote without escaped quotes
        while (pos < end)
        {
            if (data[pos] == QUOTE)
            {
                // check for escaped quote ""
                if (pos + 1 < end && data[pos + 1] == QUOTE)
                {
                    // escaped quote found — switch to slow path that collapses quotes
                    return parseQuotedFieldWithEscapes(row, data, fieldStart, pos, end);
                }
                else
                {
                    // simple closing quote — no escapes needed
                    row.add(fieldStart, pos - fieldStart, false);
                    pos++;

                    // expect comma or end
                    if (pos < end)
                    {
                        if (data[pos] != COMMA)
                        {
                            throw new CsvParserException("Delimiter or end of line expected at pos: " + (pos - start));
                        }
                        return pos + 1;
                    }
                    return pos;
                }
            }
            pos++;
        }

        throw new CsvParserException("Quoted col has not been properly closed");
    }

    /**
     * Handles quoted fields containing escaped double-quotes (""). These require
     * collapsing "" into " in the output.
     */
    private static final int parseQuotedFieldWithEscapes(final CsvByteRow row,
                                                   final byte[] data,
                                                   final int fieldStart,
                                                   final int firstEscapePos,
                                                   final int end)
    {
        // now continue from the escape position to find the actual closing quote.
        // We do NOT collapse quotes here, we defer that to the string extraction.
        int pos = firstEscapePos;
        while (pos < end)
        {
            if (data[pos] == QUOTE)
            {
                if (pos + 1 < end && data[pos + 1] == QUOTE)
                {
                    // escaped quote
                    pos += 2;
                }
                else
                {
                    // closing quote
                    row.add(fieldStart, pos - fieldStart, true);
                    pos++;

                    // expect comma or end
                    if (pos < end)
                    {
                        if (data[pos] != COMMA)
                        {
                            throw new CsvParserException("Delimiter or end of line expected at pos: " + pos);
                        }
                        return pos + 1;
                    }
                    return pos;
                }
            }
            else
            {
                pos++;
            }
        }

        throw new CsvParserException("Quoted field with quotes was not ended properly at: " + pos);
    }

    /**
     * Converts a range of bytes to an {@link XltCharBuffer}, deferring to quote unescaping
     * if the field was marked as having escapes. For ASCII data (the common case in XLT
     * timer files), the fast path is a simple widening cast from byte to char.
     */
    public static final XltCharBuffer bytesToXltCharBuffer(final byte[] data, final int offset, final int length, final boolean hasEscapes)
    {
        if (length == 0)
        {
            return XltCharBuffer.EMPTY;
        }

        if (!hasEscapes)
        {
            final char[] chars = new char[length];
            for (int i = 0; i < length; i++)
            {
                chars[i] = (char) (data[offset + i] & 0xFF);
            }
            return new XltCharBuffer(chars, 0, length);
        }
        else
        {
            // Slow path: copy and unescape ""
            final char[] chars = new char[length];
            int charLen = 0;
            
            int pos = offset;
            final int end = offset + length;
            
            while (pos < end)
            {
                if (data[pos] == QUOTE)
                {
                    chars[charLen++] = '"';
                    pos += 2; // skip both quotes
                }
                else
                {
                    chars[charLen++] = (char) (data[pos] & 0xFF);
                    pos++;
                }
            }
            return new XltCharBuffer(chars, 0, charLen);
        }
    }

    /**
     * Converts a range of bytes to a String, deferring to quote unescaping
     * if the field was marked as having escapes.
     */
    public static final String bytesToString(final byte[] data, final int offset, final int length, final boolean hasEscapes)
    {
        if (length == 0)
        {
            return "";
        }
        
        return bytesToXltCharBuffer(data, offset, length, hasEscapes).toString();
    }

    /**
     * Parses a long value directly from a byte array range without creating
     * any intermediate String or char[] objects.
     *
     * @param data
     *            the byte array
     * @param offset
     *            start position
     * @param length
     *            number of bytes
     * @return the parsed long value
     * @throws NumberFormatException
     *             if the bytes do not represent a valid long
     */
    public static final long parseLong(final byte[] data, final int offset, final int length)
    {
        if (length == 0)
        {
            throw new NumberFormatException("Empty input");
        }

        final int digit = data[offset];
        if (digit < '0' || digit > '9')
        {
            return parseLongSlow(data, offset, length);
        }

        long value = digit - 48;

        for (int i = 1; i < length; i++)
        {
            final int d = data[offset + i];
            if (d < '0' || d > '9')
            {
                throw new NumberFormatException("Invalid digit at position " + (offset + i));
            }

            value = ((value << 3) + (value << 1));
            value += (d - 48);
        }

        return value;
    }

    private static final long parseLongSlow(final byte[] data, final int offset, final int length)
    {
        int pos = offset;
        final int end = offset + length;
        boolean negative = false;

        if (data[pos] == '-')
        {
            negative = true;
            pos++;
        }
        else if (data[pos] == '+')
        {
            pos++;
        }

        if (pos == end)
        {
            throw new NumberFormatException("No digits found");
        }

        long value = 0;
        while (pos < end)
        {
            final int digit = data[pos] - '0';
            if (digit < 0 || digit > 9)
            {
                throw new NumberFormatException("Invalid digit at position " + pos);
            }
            value = ((value << 3) + (value << 1)) + digit;
            pos++;
        }

        return negative ? -value : value;
    }

    /**
     * Parses an int value directly from a byte array range without creating
     * any intermediate String or char[] objects.
     *
     * @param data
     *            the byte array
     * @param offset
     *            start position
     * @param length
     *            number of bytes
     * @return the parsed int value
     * @throws NumberFormatException
     *             if the bytes do not represent a valid int
     */
    public static final int parseInt(final byte[] data, final int offset, final int length)
    {
        return (int) parseLong(data, offset, length);
    }

    /**
     * Parses chars and evaluates if this is a boolean. Anything that is not true or TRUE
     * or similar to True will evaluate to false. This is optimized for speed.
     * 
     * @param data the byte array
     * @param offset start position
     * @param length number of bytes
     * @return true when bytes match case-insensitive, false in any other case
     */
    public static final boolean parseBoolean(final byte[] data, final int offset, final int length)
    {
        if (length != 4)
        {
            return false;
        }

        final byte t = data[offset];
        final byte r = data[offset + 1];
        final byte u = data[offset + 2];
        final byte e = data[offset + 3];

        // fastpath
        final boolean b1 = (t == 't' & r == 'r' & u == 'u' & e == 'e');

        // slowpath
        return b1 ? true : ((t == 't' || t == 'T') && (r == 'r' || r == 'R') && (u == 'u' || u == 'U') && (e == 'e' || e == 'E'));
    }
}
