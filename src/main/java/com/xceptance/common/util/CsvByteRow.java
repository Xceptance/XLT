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

import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * A flyweight object representing a row of parsed CSV data over a raw {@code byte[]} array.
 * It provides methods to extract primitive types (e.g., long, boolean) and Strings 
 * directly from the byte stream without eagerly materializing characters.
 * <p>
 * This replaces {@link java.util.List}&lt;{@link XltCharBuffer}&gt; for maximum performance.
 *
 * @implNote Exclusively created by AI (Antigravity).
 * @author Xceptance
 * @since 10.0
 */
public final class CsvByteRow
{
    private static final int INITIAL_CAPACITY = 50;

    private byte[] data;
    private int[] offsets;
    private int[] lengths;
    private boolean[] quoted;

    private int fieldCount;

    public CsvByteRow()
    {
        offsets = new int[INITIAL_CAPACITY];
        lengths = new int[INITIAL_CAPACITY];
        quoted = new boolean[INITIAL_CAPACITY];
        fieldCount = 0;
    }

    /**
     * Prepares the row for a new line of data.
     * 
     * @param data the byte array containing the CSV line
     */
    public final void reset(final byte[] data)
    {
        this.data = data;
        this.fieldCount = 0;
    }

    /**
     * Appends a parsed field reference.
     * 
     * @param offset the starting offset in the byte array
     * @param length the number of bytes
     * @param isQuoted whether the field was quoted (affects how we build text)
     */
    public final void add(final int offset, final int length, final boolean isQuoted)
    {
        if (fieldCount == offsets.length)
        {
            expandCapacity();
        }
        
        offsets[fieldCount] = offset;
        lengths[fieldCount] = length;
        quoted[fieldCount] = isQuoted;
        fieldCount++;
    }

    private final void expandCapacity()
    {
        final int newCapacity = offsets.length * 2;
        final int[] newOffsets = new int[newCapacity];
        final int[] newLengths = new int[newCapacity];
        final boolean[] newQuoted = new boolean[newCapacity];
        
        System.arraycopy(offsets, 0, newOffsets, 0, fieldCount);
        System.arraycopy(lengths, 0, newLengths, 0, fieldCount);
        System.arraycopy(quoted, 0, newQuoted, 0, fieldCount);
        
        offsets = newOffsets;
        lengths = newLengths;
        quoted = newQuoted;
    }

    /**
     * Checks if the field at the specified index is empty.
     */
    public final boolean isEmpty(final int index)
    {
        return index >= fieldCount || lengths[index] == 0;
    }

    /**
     * Returns the number of parsed fields.
     * 
     * @return the field count
     */
    public final int length()
    {
        return fieldCount;
    }

    /**
     * Returns the first character of the field at the given index.
     * Important for type codes.
     */
    public final char charAt(final int index, final int charOffset)
    {
        if (isEmpty(index) || charOffset >= lengths[index])
        {
            return 0; // fallback null character
        }
        return (char) (data[offsets[index] + charOffset] & 0xFF);
    }

    /**
     * Parses a boolean from the field. Assumes "true" or "false" mapping perfectly to ASCII.
     */
    public final boolean getBoolean(final int index)
    {
        if (isEmpty(index))
        {
            return false;
        }
        
        final int len = lengths[index];
        final int off = offsets[index];

        // "true" is 4 bytes, anything else is false
        if (len == 4)
        {
            return data[off] == 't' && data[off + 1] == 'r' && 
                   data[off + 2] == 'u' && data[off + 3] == 'e';
        }
        return false;
    }

    /**
     * Returns a parsed field as a long value.
     */
    public final long getLong(final int index)
    {
        if (isEmpty(index))
        {
            return 0L;
        }
        return ByteCsvDecoder.parseLong(data, offsets[index], lengths[index]);
    }

    /**
     * Returns a parsed field as an int value.
     */
    public final int getInt(final int index)
    {
        return (int) getLong(index);
    }

    /**
     * Returns the field as a complete String.
     * Resolves quote escaping on the fly if needed.
     */
    public final String getString(final int index)
    {
        if (isEmpty(index))
        {
            return "";
        }
        
        // This leverages the ByteCsvDecoder helper which is about to be adapted
        // to handle the actual unescaping if a string is quoted.
        return ByteCsvDecoder.bytesToString(data, offsets[index], lengths[index], quoted[index]);
    }

    /**
     * Returns the field as an XltCharBuffer.
     * Used extensively for backward compatibility with names, URLs, etc.
     */
    public final XltCharBuffer getCharBuffer(final int index)
    {
        if (isEmpty(index))
        {
            return XltCharBuffer.EMPTY;
        }

        return ByteCsvDecoder.bytesToXltCharBuffer(data, offsets[index], lengths[index], quoted[index]);
    }
}
