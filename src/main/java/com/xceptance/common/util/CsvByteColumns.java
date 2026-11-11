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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Reusable column container for byte-level CSV parsing.
 * <p>
 * Stores column boundaries as primitive integer offsets and lengths pointing directly into
 * the underlying byte buffer, completely eliminating per-column object allocations.
 */
public final class CsvByteColumns
{
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private static final int INITIAL_CAPACITY = 32;

    private static final int INTERN_CACHE_SIZE = 2048;

    private static final int INTERN_CACHE_MASK = INTERN_CACHE_SIZE - 1;

    private byte[] buffer;

    private int[] offsets;

    private int[] lengths;

    private int count;

    private String[] internStrings;

    private byte[][] internBytes;

    private int[] internHashes;

    public CsvByteColumns()
    {
        this(INITIAL_CAPACITY);
    }

    public CsvByteColumns(final int initialCapacity)
    {
        this.offsets = new int[initialCapacity];
        this.lengths = new int[initialCapacity];
        this.count = 0;
    }

    /**
     * Resets the columns container with the given source byte buffer.
     *
     * @param src
     *            the source byte buffer
     */
    public void reset(final byte[] src)
    {
        this.buffer = src;
        this.count = 0;
    }

    /**
     * Updates the underlying buffer reference without resetting the column count or parsed offsets.
     * Used when the external buffer is resized while columns are being populated.
     *
     * @param buffer
     *            the new backing byte buffer
     */
    public void setBuffer(final byte[] buffer)
    {
        this.buffer = buffer;
    }

    /**
     * Clears the columns container.
     */
    public void clear()
    {
        this.buffer = null;
        this.count = 0;
    }

    /**
     * Appends a new column boundary.
     *
     * @param offset
     *            starting offset in the byte buffer
     * @param length
     *            length of the column in bytes
     */
    public void add(final int offset, final int length)
    {
        if (count >= offsets.length)
        {
            grow();
        }
        offsets[count] = offset;
        lengths[count] = length;
        count++;
    }

    private void grow()
    {
        final int newCap = offsets.length * 2;
        offsets = Arrays.copyOf(offsets, newCap);
        lengths = Arrays.copyOf(lengths, newCap);
    }

    /**
     * Returns the number of parsed columns.
     */
    public int size()
    {
        return count;
    }

    /**
     * Returns the underlying byte buffer.
     */
    public byte[] getBuffer()
    {
        return buffer;
    }

    /**
     * Returns the starting byte offset of the given column.
     */
    public int getOffset(final int col)
    {
        return offsets[col];
    }

    /**
     * Returns the length in bytes of the given column.
     */
    public int getLength(final int col)
    {
        return lengths[col];
    }

    /**
     * Returns the byte at the specified index within the given column.
     */
    public byte byteAt(final int col, final int index)
    {
        return buffer[offsets[col] + index];
    }

    /**
     * Checks if the byte at the specified index within the given column matches the expected byte.
     */
    public boolean hasByte(final int col, final int index, final byte expected)
    {
        return lengths[col] > index && buffer[offsets[col] + index] == expected;
    }

    /**
     * Returns an isolated, independent copy of the column's bytes.
     * Safe to retain without pinning the underlying line buffer.
     */
    public byte[] copy(final int col)
    {
        final int len = lengths[col];
        if (len == 0)
        {
            return EMPTY_BYTE_ARRAY;
        }
        final byte[] copy = new byte[len];
        System.arraycopy(buffer, offsets[col], copy, 0, len);
        return copy;
    }

    /**
     * Decodes the specified column to a UTF-8 String.
     */
    public String toString(final int col)
    {
        final int len = lengths[col];
        if (len == 0)
        {
            return "";
        }
        return new String(buffer, offsets[col], len, StandardCharsets.UTF_8);
    }

    /**
     * Decodes the specified column to a canonical UTF-8 String using this instance's per-thread
     * direct-mapped byte-slice intern table, avoiding repeated heap allocations and UTF-8 decoding
     * for high-repetition columns (timer names, content types, encodings, IP addresses).
     *
     * @param col
     *            the column index
     * @return the decoded (and cached, if <= 128 bytes) UTF-8 String
     */
    public String toInternedString(final int col)
    {
        final int len = lengths[col];
        if (len == 0)
        {
            return "";
        }
        final byte[] buf = this.buffer;
        final int off = offsets[col];
        if (len > 128)
        {
            return new String(buf, off, len, StandardCharsets.UTF_8);
        }
        final int h = ByteSlice.computeHash(buf, off, len);
        String[] strings = this.internStrings;
        if (strings == null)
        {
            strings = new String[INTERN_CACHE_SIZE];
            this.internStrings = strings;
            this.internBytes = new byte[INTERN_CACHE_SIZE][];
            this.internHashes = new int[INTERN_CACHE_SIZE];
        }
        final int slot = (h ^ (h >>> 11) ^ (col * 0x9E3779B9)) & INTERN_CACHE_MASK;
        if (this.internHashes[slot] == h)
        {
            final byte[] cachedBytes = this.internBytes[slot];
            if (cachedBytes != null && cachedBytes.length == len
                && ByteSlice.equalsBytes(buf, off, cachedBytes, 0, len))
            {
                return strings[slot];
            }
        }
        final String s = new String(buf, off, len, StandardCharsets.UTF_8);
        s.hashCode();
        this.internHashes[slot] = h;
        this.internBytes[slot] = Arrays.copyOfRange(buf, off, off + len);
        strings[slot] = s;
        return s;
    }

    /**
     * Fast-path decoder for HTTP method columns that matches standard methods ({@code GET}, {@code POST},
     * {@code PUT}, {@code HEAD}, {@code PATCH}, {@code DELETE}, {@code OPTIONS}) directly against raw bytes
     * without allocating or hashing, falling back to {@link #toInternedString(int)} for custom methods.
     *
     * @param col
     *            the column index
     * @return the HTTP method string
     */
    public String toHttpMethodString(final int col)
    {
        final int len = lengths[col];
        if (len == 0)
        {
            return "";
        }
        final byte[] buf = this.buffer;
        final int off = offsets[col];
        if (len == 3)
        {
            if (buf[off] == 'G' && buf[off + 1] == 'E' && buf[off + 2] == 'T')
            {
                return "GET";
            }
            if (buf[off] == 'P' && buf[off + 1] == 'U' && buf[off + 2] == 'T')
            {
                return "PUT";
            }
        }
        else if (len == 4)
        {
            if (buf[off] == 'P' && buf[off + 1] == 'O' && buf[off + 2] == 'S' && buf[off + 3] == 'T')
            {
                return "POST";
            }
            if (buf[off] == 'H' && buf[off + 1] == 'E' && buf[off + 2] == 'A' && buf[off + 3] == 'D')
            {
                return "HEAD";
            }
        }
        else if (len == 5 && buf[off] == 'P' && buf[off + 1] == 'A' && buf[off + 2] == 'T' && buf[off + 3] == 'C' && buf[off + 4] == 'H')
        {
            return "PATCH";
        }
        else if (len == 6 && buf[off] == 'D' && buf[off + 1] == 'E' && buf[off + 2] == 'L' && buf[off + 3] == 'E' && buf[off + 4] == 'T' && buf[off + 5] == 'E')
        {
            return "DELETE";
        }
        else if (len == 7 && buf[off] == 'O' && buf[off + 1] == 'P' && buf[off + 2] == 'T' && buf[off + 3] == 'I' && buf[off + 4] == 'O' && buf[off + 5] == 'N' && buf[off + 6] == 'S')
        {
            return "OPTIONS";
        }
        return toInternedString(col);
    }

    /**
     * Decodes the specified column to a UTF-8 String.
     */
    public String getString(final int col)
    {
        return toString(col);
    }

    /**
     * Compares the column's bytes against an ASCII-only String without allocating a new String.
     * <p>
     * Note: This method only supports pure ASCII characters (0-127). For multi-byte UTF-8
     * sequences, use {@link #toString(int)} or a byte-based comparison.
     */
    public boolean equals(final int col, final String s)
    {
        final int len = lengths[col];
        if (len != s.length())
        {
            return false;
        }
        final int off = offsets[col];
        for (int i = 0; i < len; i++)
        {
            if (buffer[off + i] != (byte) s.charAt(i))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the column's bytes against a target byte array using SIMD mismatch.
     */
    public boolean equals(final int col, final byte[] bytes)
    {
        if (bytes == null || lengths[col] != bytes.length)
        {
            return false;
        }
        return Arrays.mismatch(buffer, offsets[col], offsets[col] + lengths[col],
                               bytes, 0, bytes.length) == -1;
    }

    /**
     * Parses the specified column as an integer.
     */
    public int parseInt(final int col)
    {
        return com.xceptance.common.lang.ParseNumbers.parseInt(buffer, offsets[col], lengths[col]);
    }

    /**
     * Parses the specified column as a long.
     */
    public long parseLong(final int col)
    {
        return com.xceptance.common.lang.ParseNumbers.parseLong(buffer, offsets[col], lengths[col]);
    }

    /**
     * Parses the specified column as a double.
     */
    public double parseDouble(final int col)
    {
        return com.xceptance.common.lang.ParseNumbers.parseDouble(buffer, offsets[col], lengths[col]);
    }

    /**
     * Parses the specified column as a boolean.
     */
    public boolean parseBoolean(final int col)
    {
        return com.xceptance.common.lang.ParseNumbers.parseBoolean(buffer, offsets[col], lengths[col]);
    }

    /**
     * Checks if the specified column is empty (length == 0).
     */
    public boolean isEmpty(final int col)
    {
        return lengths[col] == 0;
    }

    /**
     * Returns the ASCII char at the specified index within the given column.
     */
    public char charAt(final int col, final int index)
    {
        return (char) (buffer[offsets[col] + index] & 0xFF);
    }
}

