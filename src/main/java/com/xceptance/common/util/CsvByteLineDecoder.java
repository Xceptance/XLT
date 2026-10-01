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

/**
 * Ultra-high-performance byte-level CSV line decoder.
 * <p>
 * Operates directly on raw UTF-8 byte arrays without converting the line to chars,
 * and records column bounds as primitive offsets in {@link CsvByteColumns} without allocating
 * any per-column objects.
 * <p>
 * Fully supports RFC 4180 quotes, escaped quotes (""), and commas within quotes.
 * <p>
 * <b>Note on in-place mutation:</b> When an RFC 4180 quoted column contains escaped quotes
 * ({@code ""}), they are unescaped by compacting bytes directly in the source array {@code src}
 * in-place to achieve zero allocation. Callers passing shared or read-only buffers should be aware
 * that escaped quotes will be modified in the underlying array.
 */
public final class CsvByteLineDecoder
{
    /** Byte constant representing a comma (','). */
    public static final byte COMMA = (byte) ',';

    /** Byte constant representing a double quote ('"'). */
    public static final byte QUOTE_BYTE = (byte) '"';

    private CsvByteLineDecoder()
    {
    }

    /**
     * Decodes a byte-encoded CSV line into a new {@link CsvByteColumns} instance.
     * Primarily used for convenience and testing.
     *
     * @param src
     *            the raw byte array
     * @return the populated columns container
     */
    public static CsvByteColumns parse(final byte[] src)
    {
        return parse(src, 0, src.length);
    }

    /**
     * Decodes a slice of a byte-encoded CSV line into a new {@link CsvByteColumns} instance.
     * Primarily used for convenience and testing.
     *
     * @param src
     *            the raw byte array
     * @param offset
     *            the start offset in the byte array
     * @param length
     *            the length of the line in bytes
     * @return the populated columns container
     */
    public static CsvByteColumns parse(final byte[] src, final int offset, final int length)
    {
        final CsvByteColumns columns = new CsvByteColumns(50);
        parse(columns, src, offset, length);
        return columns;
    }

    /**
     * Decodes a byte-encoded CSV line and stores column boundaries directly into the provided
     * reusable {@link CsvByteColumns} container.
     *
     * @param result
     *            the reusable columns container to populate
     * @param src
     *            the raw byte array
     * @throws CsvParserException
     *             if delimiters or quotes are malformed
     */
    public static void parse(final CsvByteColumns result, final byte[] src)
    {
        parse(result, src, 0, src.length);
    }

    /**
     * Decodes a slice of a byte-encoded CSV line and stores column boundaries directly into the provided
     * reusable {@link CsvByteColumns} container. This is the zero-allocation hot path.
     *
     * @param result
     *            the reusable columns container to populate
     * @param src
     *            the raw byte array (may be mutated in-place if unescaping double quotes "")
     * @param offset
     *            the start offset in the byte array
     * @param length
     *            the length of the line in bytes
     * @throws CsvParserException
     *             if delimiters or quotes are malformed
     */
    public static void parse(final CsvByteColumns result, final byte[] src, final int offset, final int length)
    {
        result.reset(src);

        // empty case
        if (length == 0)
        {
            result.add(offset, 0);
            return;
        }

        final int end = offset + length;
        int pos = offset;

        while (pos < end)
        {
            final byte b = src[pos];

            if (b == QUOTE_BYTE)
            {
                pos = startQuotedCol(result, src, pos, offset, end);
            }
            else
            {
                pos = startCol(result, src, pos, end);
            }
        }

        // Special case: trailing comma means another empty column behind it
        if (src[end - 1] == COMMA)
        {
            result.add(end, 0);
        }
    }

    /**
     * Reads an unquoted column up to the next comma or end-of-line.
     */
    private static int startCol(final CsvByteColumns result, final byte[] src, final int currentPos, final int end)
    {
        int pos = currentPos;
        final int start = currentPos;

        while (pos < end)
        {
            final byte b = src[pos];

            if (b == COMMA)
            {
                result.add(start, pos - start);
                return pos + 1;
            }

            pos++;
        }

        // Reached end of line
        result.add(start, pos - start);
        return pos;
    }

    /**
     * Reads a quoted column with support for escaped quotes ("").
     */
    private static int startQuotedCol(final CsvByteColumns result, final byte[] src, final int currentPos,
                                      final int lineOffset, final int end)
    {
        int pos = currentPos + 1;
        final int start = pos;

        while (pos < end)
        {
            final byte b = src[pos];

            if (b == QUOTE_BYTE)
            {
                // Check if followed by another quote (escaped quote "")
                if (pos + 1 < end && src[pos + 1] == QUOTE_BYTE)
                {
                    pos = endQuotedQuotesCol(result, src, start, pos + 1, lineOffset, end);
                    return pos + 1;
                }
                else
                {
                    // Closing quote reached
                    result.add(start, pos - start);
                    pos++;

                    // Next byte must be COMMA or end of line
                    if (pos < end && src[pos] != COMMA)
                    {
                        throw new CsvParserException("Delimiter or end of line expected at pos: " + (pos - lineOffset));
                    }

                    return pos + 1;
                }
            }
            pos++;
        }

        throw new CsvParserException("Quoted col has not been properly closed");
    }

    /**
     * Handles in-place byte shifting for escaped quotes ("").
     */
    private static int endQuotedQuotesCol(final CsvByteColumns result, final byte[] src, final int start,
                                          final int currentPos, final int lineOffset, final int end)
    {
        int pos = currentPos + 1;
        int shiftOffset = 1;

        while (pos < end)
        {
            final byte b = src[pos];
            src[pos - shiftOffset] = b;

            if (b == QUOTE_BYTE)
            {
                final byte nextByte = (pos + 1 < end) ? src[pos + 1] : 0;
                if (nextByte == QUOTE_BYTE)
                {
                    // Escaped quote (""): skip one quote and increase shift offset
                    shiftOffset++;
                    pos += 2;
                }
                else
                {
                    // End of quoted column
                    if (!(nextByte == 0 || nextByte == COMMA))
                    {
                        throw new CsvParserException("Delimiter or end of line expected at pos: " + (pos - lineOffset));
                    }

                    result.add(start, (pos - shiftOffset) - start);
                    return pos + 1;
                }
            }
            else
            {
                pos++;
            }
        }

        throw new CsvParserException("Quoted field with quotes was not ended properly at: " + (pos - lineOffset));
    }
}
