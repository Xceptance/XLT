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

import java.util.Arrays;
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
     * Primitive zero-allocation cache to prevent generating JVM object wrappers on every string resolve.
     * Uses direct-mapping to overwrite on hash collisions, keeping logic branching ultra-lean.
     */
    public static final class ByteStringCache
    {
        @jdk.jfr.Name("com.xceptance.xlt.ByteStringCacheLookup")
        @jdk.jfr.Label("Byte String Cache Lookup")
        @jdk.jfr.Category({"XLT", "Parser", "Cache"})
        public static class CacheLookupEvent extends jdk.jfr.Event
        {
            @jdk.jfr.Label("Hit")
            public boolean hit;

            @jdk.jfr.Label("Length")
            public int length;
        }

        private static final int MASK = 1023; // 1024 slots
        private final String[] values = new String[1024];
        private final byte[][] keys = new byte[1024][];
        private final int[] lengths = new int[1024];

        public final String get(final byte[] data, final int offset, final int length, final boolean quoted)
        {
            if (length == 0)
            {
                return "";
            }
            
            int h = 0;
            // Unroll slightly or just fast hash
            for (int i = 0; i < length; i++)
            {
                h = 31 * h + data[offset + i];
            }
            final int ptr = (h ^ (h >>> 16)) & MASK;

            final String val = values[ptr];
            final byte[] keyData = keys[ptr];

            // evaluate hit
            boolean isHit = false;

            if (val != null && keyData != null && lengths[ptr] == length)
            {
                boolean match = true;
                for (int i = 0; i < length; i++)
                {
                    if (keyData[i] != data[offset + i])
                    {
                        match = false;
                        break;
                    }
                }
                if (match)
                {
                    isHit = true;
                }
            }

            // HotSpot JVM Escape Analysis intrinsic: this instantiation inherently guarantees zero
            // heap allocation overhead when JFR is inactive. The Just-In-Time (JIT) compiler natively targets
            // the `.isEnabled()` block and aggressively eliminates the `new` allocation entirely from the execution tree.
            final CacheLookupEvent event = new CacheLookupEvent();
            if (event.isEnabled())
            {
                event.hit = isHit;
                event.length = length;
                event.commit();
            }

            if (isHit)
            {
                return val;
            }

            // miss (or collision) -> overwrite
            byte[] keyBuffer = keys[ptr];
            if (keyBuffer == null || keyBuffer.length < length)
            {
                // Allocate with headroom to minimize reallocation if later strings are longer
                keyBuffer = new byte[Math.max(64, length)];
                keys[ptr] = keyBuffer;
            }
            
            System.arraycopy(data, offset, keyBuffer, 0, length);
            lengths[ptr] = length;

            final String newVal = ByteCsvDecoder.bytesToString(data, offset, length, quoted);
            values[ptr] = newVal;
            
            return newVal;
        }
    }

    // An instance-local string intern cache used by the executing parser thread
    private ByteStringCache stringCache;

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
        return ByteCsvDecoder.parseBoolean(data, offsets[index], lengths[index]);
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
     * Sets an optional deduplication cache to reduce String allocations.
     */
    public void setStringCache(final ByteStringCache stringCache)
    {
        this.stringCache = stringCache;
    }

    /**
     * Returns the field as a complete String.
     * Resolves quote escaping on the fly if needed, and uses an LRU cache if attached.
     */
    public final String getString(final int index)
    {
        if (isEmpty(index))
        {
            return "";
        }
        
        if (stringCache != null)
        {
            return stringCache.get(data, offsets[index], lengths[index], quoted[index]);
        }
        else
        {
            return ByteCsvDecoder.bytesToString(data, offsets[index], lengths[index], quoted[index]);
        }
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
