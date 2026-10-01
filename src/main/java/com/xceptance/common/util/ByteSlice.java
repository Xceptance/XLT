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
 * A lightweight, memory-efficient slice of a raw byte buffer.
 * <p>
 * Supports both mutable reuse (for non-allocating cache lookups) and immutable detached copies
 * (for cache retention without pinning large line buffers in memory).
 *
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public final class ByteSlice implements Comparable<ByteSlice>
{
    private static final byte[] EMPTY_BUFFER = new byte[0];

    public static final ByteSlice EMPTY = new ByteSlice(EMPTY_BUFFER, 0, 0);

    private byte[] buffer;
    private int offset;
    private int length;
    private int hash;

    /**
     * Creates an empty mutable ByteSlice.
     */
    public ByteSlice()
    {
        this.buffer = EMPTY_BUFFER;
        this.offset = 0;
        this.length = 0;
        this.hash = 0;
    }

    /**
     * Creates a ByteSlice wrapping the specified buffer range.
     *
     * @param buffer
     *            the underlying byte buffer
     * @param offset
     *            the starting offset
     * @param length
     *            the slice length
     */
    public ByteSlice(final byte[] buffer, final int offset, final int length)
    {
        set(buffer, offset, length);
    }

    /**
     * Creates a ByteSlice wrapping the full byte array.
     *
     * @param buffer
     *            the underlying byte buffer
     */
    public ByteSlice(final byte[] buffer)
    {
        this(buffer, 0, buffer != null ? buffer.length : 0);
    }

    /**
     * Reconfigures this slice in-place to point to a new buffer range.
     * Resets the cached hash code.
     *
     * @param buffer
     *            the underlying byte buffer
     * @param offset
     *            the starting offset
     * @param length
     *            the slice length
     */
    public void set(final byte[] buffer, final int offset, final int length)
    {
        this.buffer = buffer != null ? buffer : EMPTY_BUFFER;
        this.offset = offset;
        this.length = length;
        this.hash = 0;
    }

    /**
     * Returns the underlying byte buffer.
     */
    public byte[] getBuffer()
    {
        return buffer;
    }

    /**
     * Returns the start offset in the underlying buffer.
     */
    public int getOffset()
    {
        return offset;
    }

    /**
     * Returns the length in bytes of this slice.
     */
    public int getLength()
    {
        return length;
    }

    /**
     * Returns whether this slice is empty.
     */
    public boolean isEmpty()
    {
        return length == 0;
    }

    /**
     * Returns the byte at the specified index within this slice.
     *
     * @param index
     *            index relative to slice offset (0 to length - 1)
     * @return the byte value
     */
    public byte byteAt(final int index)
    {
        if (index < 0 || index >= length)
        {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + length);
        }
        return buffer[offset + index];
    }

    /**
     * Creates an isolated, detached copy of this slice with its own allocated array.
     * Useful for caching to avoid retaining large CSV chunks in memory.
     *
     * @return a new ByteSlice with copied bytes
     */
    public ByteSlice copy()
    {
        return copyOf(this.buffer, this.offset, this.length, this.hash);
    }

    /**
     * Creates an isolated, detached copy of the given byte buffer slice.
     *
     * @param buffer
     *            the source buffer
     * @param offset
     *            start offset
     * @param length
     *            length of slice
     * @return a new detached ByteSlice
     */
    public static ByteSlice copyOf(final byte[] buffer, final int offset, final int length)
    {
        return copyOf(buffer, offset, length, 0);
    }

    private static ByteSlice copyOf(final byte[] buffer, final int offset, final int length, final int precomputedHash)
    {
        if (buffer == null || length <= 0)
        {
            return EMPTY;
        }
        final byte[] copy = Arrays.copyOfRange(buffer, offset, offset + length);
        final ByteSlice slice = new ByteSlice(copy, 0, length);
        slice.hash = precomputedHash;
        return slice;
    }

    /**
     * Searches for the first occurrence of the specified byte sequence within this slice.
     *
     * @param target
     *            the target bytes to find
     * @return index relative to slice start (0-based), or -1 if not found
     */
    public int indexOf(final byte[] target)
    {
        if (target == null || target.length == 0)
        {
            return 0;
        }
        final int targetLen = target.length;
        if (targetLen > length)
        {
            return -1;
        }

        final byte first = target[0];
        final int max = offset + (length - targetLen);

        for (int i = offset; i <= max; i++)
        {
            if (buffer[i] != first)
            {
                while (++i <= max && buffer[i] != first);
            }

            if (i <= max && (targetLen == 1 || Arrays.mismatch(buffer, i, i + targetLen, target, 0, targetLen) == -1))
            {
                return i - offset;
            }
        }
        return -1;
    }

    /**
     * Decodes this byte slice to a UTF-8 String.
     */
    @Override
    public String toString()
    {
        if (length == 0)
        {
            return "";
        }
        return new String(buffer, offset, length, StandardCharsets.UTF_8);
    }

    @Override
    public int hashCode()
    {
        int h = this.hash;
        // Decision: We intentionally do not use a sentinel (like 1 or MIN_VALUE) when h == 0.
        // Legitimate hash collisions with 0 are exceedingly rare, and omitting the extra branch
        // preserves peak performance in the hot cache lookup path.
        if (h == 0 && length > 0)
        {
            final byte[] b = this.buffer;
            final int end = offset + length;
            for (int i = offset; i < end; i++)
            {
                h = 31 * h + (b[i] & 0xFF);
            }
            this.hash = h;
        }
        return h;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof final ByteSlice other)
        {
            if (this.length != other.length)
            {
                return false;
            }
            return Arrays.mismatch(this.buffer, this.offset, this.offset + this.length,
                                   other.buffer, other.offset, other.offset + other.length) == -1;
        }
        return false;
    }

    @Override
    public int compareTo(final ByteSlice other)
    {
        return Arrays.compareUnsigned(this.buffer, this.offset, this.offset + this.length,
                                      other.buffer, other.offset, other.offset + other.length);
    }
}
