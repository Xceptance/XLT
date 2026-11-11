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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
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
public final class ByteSlice implements CharSequence, Comparable<ByteSlice>
{
    private static final VarHandle LONG_VIEW = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);
    private static final VarHandle INT_VIEW = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);

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
     * Reconfigures this slice in-place with a pre-computed hash code.
     *
     * @param buffer
     *            the underlying byte buffer
     * @param offset
     *            the starting offset
     * @param length
     *            the slice length
     * @param precomputedHash
     *            the pre-computed hash code (or 0 if not yet computed)
     */
    public void set(final byte[] buffer, final int offset, final int length, final int precomputedHash)
    {
        this.buffer = buffer != null ? buffer : EMPTY_BUFFER;
        this.offset = offset;
        this.length = length;
        this.hash = precomputedHash;
    }

    /**
     * Returns whether all bytes in this slice are pure 7-bit ASCII ({@code >= 0}).
     */
    public boolean isAscii()
    {
        final byte[] b = this.buffer;
        final int end = offset + length;
        for (int i = offset; i < end; i++)
        {
            if (b[i] < 0)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public int length()
    {
        return length;
    }

    @Override
    public char charAt(final int index)
    {
        if (index < 0 || index >= length)
        {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + length);
        }
        return (char) (buffer[offset + index] & 0xFF);
    }

    @Override
    public CharSequence subSequence(final int start, final int end)
    {
        if (start < 0 || end < start || end > length)
        {
            throw new IndexOutOfBoundsException("start=" + start + ", end=" + end + ", length=" + length);
        }
        if (start == 0 && end == length)
        {
            return this;
        }
        return new ByteSlice(buffer, offset + start, end - start);
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

    /**
     * Computes a fast 32-bit hash code over {@code b[offset .. offset + length)} using 64-bit word loads.
     *
     * @param b
     *            the byte array
     * @param offset
     *            the start offset
     * @param length
     *            the number of bytes
     * @return the computed hash code (0 if {@code length <= 0})
     */
    public static int computeHash(final byte[] b, final int offset, final int length)
    {
        if (length <= 0)
        {
            return 0;
        }
        if (length >= 8)
        {
            long h = length * 0x9E3779B97F4A7C15L;
            final int limit = offset + length - 8;
            for (int i = offset; i < limit; i += 8)
            {
                h = Long.rotateLeft(h ^ (long) LONG_VIEW.get(b, i), 27) * 0x9E3779B97F4A7C15L;
            }
            h ^= (long) LONG_VIEW.get(b, limit);
            h ^= h >>> 33;
            h *= 0xFF51AFD7ED558CCDL;
            return (int) (h ^ (h >>> 32));
        }
        if (length >= 4)
        {
            final int w0 = (int) INT_VIEW.get(b, offset);
            final int w1 = (int) INT_VIEW.get(b, offset + length - 4);
            long h = ((w0 & 0xFFFFFFFFL) << 32) | (w1 & 0xFFFFFFFFL);
            h ^= length * 0x9E3779B97F4A7C15L;
            h ^= h >>> 33;
            h *= 0xFF51AFD7ED558CCDL;
            return (int) (h ^ (h >>> 32));
        }
        int h = length;
        for (int i = 0; i < length; i++)
        {
            h = 31 * h + b[offset + i];
        }
        return h;
    }

    /**
     * Compares two byte array slices of identical length {@code len} using 64-bit word loads.
     */
    public static boolean equalsBytes(final byte[] b1, final int o1, final byte[] b2, final int o2, final int len)
    {
        if (len >= 8)
        {
            if ((long) LONG_VIEW.get(b1, o1 + len - 8) != (long) LONG_VIEW.get(b2, o2 + len - 8))
            {
                return false;
            }
            final int limit = len - 8;
            for (int i = 0; i < limit; i += 8)
            {
                if ((long) LONG_VIEW.get(b1, o1 + i) != (long) LONG_VIEW.get(b2, o2 + i))
                {
                    return false;
                }
            }
            return true;
        }
        if (len >= 4)
        {
            return (int) INT_VIEW.get(b1, o1) == (int) INT_VIEW.get(b2, o2)
                && (int) INT_VIEW.get(b1, o1 + len - 4) == (int) INT_VIEW.get(b2, o2 + len - 4);
        }
        for (int i = 0; i < len; i++)
        {
            if (b1[o1 + i] != b2[o2 + i])
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        final int h = this.hash;
        return (h != 0 || this.length == 0) ? h : computeAndCacheHash();
    }

    private int computeAndCacheHash()
    {
        final int h = computeHash(this.buffer, this.offset, this.length);
        this.hash = h;
        return h;
    }

    @Override
    public boolean equals(final Object obj)
    {
        return this == obj || (obj instanceof ByteSlice other && equalsSlice(other));
    }

    public boolean equalsSlice(final ByteSlice other)
    {
        final int len = this.length;
        if (len != other.length || (this.hash != 0 && other.hash != 0 && this.hash != other.hash))
        {
            return false;
        }
        return equalsBytes(this.buffer, this.offset, other.buffer, other.offset, len);
    }

    @Override
    public int compareTo(final ByteSlice other)
    {
        return Arrays.compareUnsigned(this.buffer, this.offset, this.offset + this.length,
                                      other.buffer, other.offset, other.offset + other.length);
    }
}
