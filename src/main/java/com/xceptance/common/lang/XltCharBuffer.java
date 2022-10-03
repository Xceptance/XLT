/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.common.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class does not implement the CharBuffer of the JDK, but uses the idea of a shared
 * character array with views. This is also a very unsafe implementation with as little
 * as possible boundary checks to achieve the maximum speed possible. To enhance use, we
 * implement CharSequence and hence can also do regex with it now. It also features common
 * string and striingbuffer methods to make it versatile and avoid the typical overhead
 * when doing conversions back and forth.
 *
 * @author rschwietzke
 * @since 7.0
 */
public class XltCharBuffer implements CharSequence, Comparable<XltCharBuffer>
{
    /**
     * Empty array
     */
    private static final char[] EMPTY_ARRAY = new char[0];

    /**
     * An empty static XltCharBuffer
     */
    public static final XltCharBuffer EMPTY = new XltCharBuffer(EMPTY_ARRAY);

    /**
     * The internal buffer, it is shared!
     */
    private final char[] src;

    /**
     * Because we are here dealing with the view of an array, we need a start
     * and a length.
     */
    private final int from;

    /**
     * The length of the view of the buffer
     */
    private final int length;

    /**
     * The hashcode. It is cached to avoid running the same operation again and
     * again. The hashcode is identical to a hashcode of a String with the same
     * content.
     */
    private int hashCode;

    /**
     * New buffer from a raw char array
     *
     * @param src a char array
     */
    public XltCharBuffer(final char[] src)
    {
        this.src = src == null ? EMPTY_ARRAY : src;
        this.from = 0;
        this.length = this.src.length;
    }

    /**
     * A new buffer from an open string builder, so we can directly
     * use its buffer and don't have to copy. This is highly unsafe, so
     * make sure you know what you are doing!
     *
     * @param src an open string builder
     */
    public XltCharBuffer(final OpenStringBuilder src)
    {
        this(src.getCharArray(), 0, src.length());
    }

    /**
     * A new buffer from a char array including a view port.
     *
     * @param src the char array, if is is null, we fix that silently
     * @param from from where to deliver the buffer
     * @param length how long should the buffer be
     */
    public XltCharBuffer(final char[] src, final int from, final int length)
    {
        if (src != null)
        {
            this.src = src;
            this.from = from;
            this.length = length;
        }
        else
        {
            this.src = EMPTY_ARRAY;
            this.from = 0;
            this.length = 0;
        }
    }

    /**
     * Just returns an empty buffer. This is a static object and not
     * a new buffer every time, so apply caution.
     *
     * @return the empty buffer
     */
    public static XltCharBuffer empty()
    {
        return EMPTY;
    }

    /**
     * Return the character at a position. This code does not run any
     * checks in regards to pos being correct (>= 0, < length). This will
     * automatically apply the view on the underlying array hence incorrect
     * pos values might return something unexpected. So know what you do or else...
     *
     * @param pos the position to return
     * @return the character at this position.
     */
    public char charAt(final int pos)
    {
        return src[from + pos];
    }

    /**
     * Set a character at this position. Similarly to charAt, this does not
     * check for correctness of pos in favor of speed.
     *
     * @param pos the pos to write to
     * @param c the character to set
     * @return this instance so put can be chained
     */
    public XltCharBuffer put(final int pos, final char c)
    {
        src[from + pos] = c;

        return this;
    }

    /**
     * Splits up this sequence into sub-sequences at splitChar markers
     * excluding the marker
     *
     * @param splitChar the split character
     * @return a list of the sub-sequences
     */
    public List<XltCharBuffer> split(final char splitChar)
    {
        final List<XltCharBuffer> result = new ArrayList<>();

        int last = -1;
        for (int i = 0; i < this.length; i++)
        {
            char c = this.charAt(i);
            if (c == splitChar)
            {
                result.add(this.substring(last + 1, i));
                last = i;
            }
        }

        last++;
        // in case there is either nothing done yet or
        // something left
        if (last == 0 || last < this.length)
        {
            result.add(this.substring(last, this.length));
        }
        else // if (last + 1 == this.length)
        {
            // the else if is not needed, this branch
            // only fires when the del is the last char
            result.add(XltCharBuffer.empty());
        }

        return result;
    }

    /**
     * Replace a character by a character sequence in this charbuffer. This will
     * create a new char array backed charbuffer.
     *
     * @param c the character to search for
     * @param s the charsequence to insert instead of the character
     * @return a new charbuffer with no references ot the old
     */
    public XltCharBuffer replace(final char c, final CharSequence s)
    {
        final OpenStringBuilder result = new OpenStringBuilder(this.length() + s.length());

        for (int i = 0; i < this.length; i++)
        {
            final char cc = this.charAt(i);
            if (cc == c)
            {
                result.append(s);
            }
            else
            {
                result.append(cc);
            }
        }

        return XltCharBuffer.valueOf(result);
    }

    /**
     * Looks ahead, otherwise returns 0. Only safety bound against ahead misses, not
     * any behind misses
     *
     * @param pos the position to look at
     * @return the content of the peaked pos or 0 if this position does not exist
     */
    public char peakAhead(final int pos)
    {
        return pos < length ? charAt(pos) : 0;
    }

    /**
     * Returns a new buffer with a view on the current. No copy is made.
     * No runtime checks
     *
     * @param from start position
     * @param length length of the view port
     * @return a new buffer
     */
    public XltCharBuffer viewByLength(final int from, final int length)
    {
        return new XltCharBuffer(this.src, this.from + from, length);
    }

    /**
     * Returns a new buffer with a view on the current. No copy is made.
     * No runtime checks
     *
     * @param from start position
     * @param to end position
     * @return a new buffer
     */
    public XltCharBuffer viewFromTo(final int from, final int to)
    {
        return new XltCharBuffer(this.src, this.from + from, to - from);
    }

    /**
     * Creates a new buffer similar to a String.substring call. There is no copy created, we still
     * look at the same buffer, but have a reduced view.
     *
     * @param from first position (inclusive)
     * @param to last position (exclusive)
     * @return
     */
    public XltCharBuffer substring(final int from, final int to)
    {
        return viewFromTo(from, to);
    }

    /**
     * Creates a new buffer similar to a String.substring call from
     * a position till the end
     * @param from first position
     * @return
     */
    public XltCharBuffer substring(final int from)
    {
        return viewByLength(from, this.length - from);
    }

    /**
     * Append a charbuffer to a stringbuilder. Internal helper.
     *
     * @param target the target
     * @param src the source
     * @return the passed target for fluid syntax
     */
    private static OpenStringBuilder append(final OpenStringBuilder target, final XltCharBuffer src)
    {
        target.append(src.src, src.from, src.length);

        return target;
    }

    /**
     * Creates a new char buffer by merging strings
     *
     * @param s1 string 1
     * @param s2 string 2
     * @return the new charbuffer
     */
    public static XltCharBuffer valueOf(final String s1, final String s2)
    {
        final OpenStringBuilder sb = new OpenStringBuilder(s1.length() + s2.length());
        sb.append(s1);
        sb.append(s2);

        return new XltCharBuffer(sb.getCharArray(), 0, sb.length());
    }

    /**
     * Creates a new char buffer by merging XltCharBuffers
     *
     * @param s1 buffer 1
     * @param s2 buffer 2
     * @return the new charbuffer
     */
    public static XltCharBuffer valueOf(final XltCharBuffer s1, final XltCharBuffer s2)
    {
        final OpenStringBuilder sb = new OpenStringBuilder(s1.length() + s2.length());
        append(sb, s1);
        append(sb, s2);

        return new XltCharBuffer(sb.getCharArray(), 0, sb.length());
    }

    /**
     * Creates a new char buffer by adding a single char
     *
     * @param s1
     * @param c
     * @return
     */
    public static XltCharBuffer valueOf(final XltCharBuffer s1, final char c)
    {
        // our problem is that a String.toCharArray already creates a copy and we
        // than copy the copy into a new array, hence wasting one full array of
        // s1 and s2

        // let's instead see if we can run with openstringbuilder nicely
        // more cpu in favour of less memory
        final OpenStringBuilder sb = new OpenStringBuilder(s1.length() + 1);
        append(sb, s1);
        sb.append(c);

        return new XltCharBuffer(sb.getCharArray(), 0, sb.length());
    }

    /**
     * Creates a new char buffer by merging strings
     *
     * @param s
     * @return
     */
    public static XltCharBuffer valueOf(final String s1, final String s2, final String s3)
    {
        // our problem is that a String.toCharArray already creates a copy and we
        // than copy the copy into a new array, hence wasting one full array of
        // s1 and s2

        // let's instead see if we can run with openstringbuilder nicely
        // more cpu in favour of less memory
        final OpenStringBuilder sb = new OpenStringBuilder(s1.length() + s2.length() + s3.length());
        sb.append(s1);
        sb.append(s2);
        sb.append(s3);

        return new XltCharBuffer(sb.getCharArray(), 0, sb.length());
    }

    /**
     * Creates a new char buffer by merging strings
     *
     * @param s
     * @return
     */
    public static XltCharBuffer valueOf(final XltCharBuffer s1, final XltCharBuffer s2, final XltCharBuffer s3)
    {
        // our problem is that a String.toCharArray already creates a copy and we
        // than copy the copy into a new array, hence wasting one full array of
        // s1 and s2

        // let's instead see if we can run with openstringbuilder nicely
        // more cpu in favour of less memory
        final OpenStringBuilder sb = new OpenStringBuilder(s1.length() + s2.length() + s3.length());
        append(sb, s1);
        append(sb, s2);
        append(sb, s3);

        // getCharArray does not create a copy, hence OpenStringBuilder from now on should not be used anymore, because it would modify
        // the XltCharBuffer as well. Speed over luxury.
        return new XltCharBuffer(sb.getCharArray(), 0, sb.length());
    }

    /**
     * Creates a new char buffer by merging strings
     *
     * @param s1 string 1
     * @param s2 string 2
     * @param s3 string 3
     * @param more more strings
     * @return a new char buffer
     */
    public static XltCharBuffer valueOf(final String s1, final String s2, final String s3, final String... more)
    {
        // shortcut
        if (more == null || more.length == 0)
        {
            return valueOf(s1, s2, s3);
        }

        // new total size
        int newSize = s1.length() + s2.length() + s3.length();
        for (int i = 0; i < more.length; i++)
        {
            newSize += more[i].length();
        }

        final OpenStringBuilder sb = new OpenStringBuilder(newSize);
        sb.append(s1);
        sb.append(s2);
        sb.append(s3);

        for (int i = 0; i < more.length; i++)
        {
            sb.append(more[i]);
        }

        return new XltCharBuffer(sb.getCharArray(), 0, sb.length());
    }

    /**
     * Create a new char buffer from a char array without copying it. It assume that the
     * full array is valid and because we don't copy, we don't have immutability!
     *
     * @param s the char array to use
     * @return a new charbuffer instance
     */
    public static XltCharBuffer valueOf(final char[] s)
    {
        return new XltCharBuffer(s);
    }

    /**
     * Create a new char buffer from a string. Because a string does provide array access,
     * we use the returned copy by toCharArray to set up the char buffer.
     *
     * @param s the string to use
     * @return a new charbuffer instance
     */
    public static XltCharBuffer valueOf(final String s)
    {
        return s == null ? null : new XltCharBuffer(s.toCharArray());
    }

    /**
     * A new charbuffer from an open string builder. We don't copy the underlying array,
     * hence string builder and char buffer refer to the same underlying data!
     *
     * @param s the builder to get the array from
     * @return a new charbuffer instance
     */
    public static XltCharBuffer valueOf(final OpenStringBuilder s)
    {
        return new XltCharBuffer(s.getCharArray(), 0, s.length());
    }

    /**
     * Just return the content of this buffer as string. This is of course
     * a copy operation.
     *
     * @return a string representation of this buffer
     */
    @Override
    public String toString()
    {
        return String.valueOf(src, from, length);
    }

    /**
     * Returns a copy of the backing char array for the range of this buffer
     * aka not more than needed
     *
     * @return a copy of the relevant portion of the backing array
     */
    public char[] toCharArray()
    {
        final char[] target = new char[length];

        System.arraycopy(src, from, target, 0, length);

        return target;
    }

    /**
     * Code shared by String and StringBuffer to do searches. The
     * source is the character array being searched, and the target
     * is the string being searched for.
     *
     * @param   source       the characters being searched.
     * @param   sourceOffset offset of the source string.
     * @param   sourceCount  count of the source string.
     * @param   target       the characters being searched for.
     * @param   targetOffset offset of the target string.
     * @param   targetCount  count of the target string.
     * @param   fromIndex    the index to begin searching from.
     *
     * @return the first position both array match
     */
    private static int indexOf(char[] source, int sourceOffset, int sourceCount,
                               char[] target, int targetOffset, int targetCount,
                               int fromIndex) {

        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }

        char first = target[targetOffset];
        int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (source[i] != first) {
                while (++i <= max && source[i] != first);
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && source[j] == target[k]; j++, k++);

                if (j == end) {
                    /* Found whole string. */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }

    /**
     * Find the first occurrence of a char
     *
     * @param c the char to search
     * @return the position or -1 otherwise
     */
    public int indexOf(final char c)
    {
        final int end = length + this.from;
        for (int i = this.from; i < end; i++)
        {
            if (this.src[i] == c)
            {
                return i - this.from;
            }
        }

        return -1;
    }

    /**
     * Search for the first occurrence of another buffer in this buffer
     *
     * @param s the buffer to be search for
     * @return the first found position or -1 if not found
     */
    public int indexOf(final XltCharBuffer s)
    {
        return indexOf(this.src, from, length, s.src, s.from, s.length, 0);
    }

    /**
     * Search for the first occurrence of another buffer in this buffer
     *
     * @param s the buffer to be search for
     * @return the first found position or -1 if not found
     */
    public int indexOf(final XltCharBuffer s, final int fromIndex)
    {
        return indexOf(this.src, from, length, s.src, s.from, s.length, fromIndex);
    }

    /**
     * Checks whether or not a buffer ends with the content of another buffer
     *
     * @param s the buffer that has to be machting the end of this buffer
     * @return true if the end matches, false otherwise
     */
    public boolean endsWith(final XltCharBuffer s)
    {
        if (s.length > this.length)
        {
            return false;
        }

        return indexOf(s, this.length - s.length) > -1;
    }

    /**
     * Checks if the start of the buffer matches another buffer
     * @param s the buffer to match the start against
     * @return true if the start matches, false otherwise
     */
    public boolean startsWith(final XltCharBuffer s)
    {
        return indexOf(s, 0) == 0;
    }

    /**
     * Returns the last occurrence of a buffer in this buffer
     *
     * @param s the buffer to looks for
     * @return the position of the last occurrence or -1
     */
    public int lastIndexOf(final XltCharBuffer s)
    {
        return lastIndexOf(s, this.length);
    }

    /**
     * Returns the last occurrence of a buffer in this starting from a certain offset
     * and searching backwards(!) from there
     *
     * @param s the buffer to looks for
     * @param from the offset to start from
     * @return the position of the last occurrence or -1
     */
    public int lastIndexOf(final XltCharBuffer s, int from)
    {
        // what I search is longer, so it won't match
        if (s.length > length)
        {
            return -1;
        }
        // if the remaining part is too short, it won't match
        if (from > length - s.length)
        {
            from = length - s.length;
        }

        outer:
            for (int i = from; i >= 0; i--)
            {
                for (int si = 0; si < s.length; si++)
                {
                    if (s.charAt(si) != charAt(i + si))
                    {
                        continue outer;
                    }
                }
                return i;
            }

        return -1;
    }

    /**
     * Returns the length of this buffer
     */
    public int length()
    {
        return length;
    }

    /**
     * Optimized hashcode calculation for large strings using all execution units of the CPU.
     * You are not supposed to call this directly, it is rather public for testing. This is a trade
     * off between cpu and branches.
     *
     * Assume we are not mutating... if we mutate, we would have to reset the hashCode
     *
     * Taken from JDK 19 - JDK-8282664, Code and Idea by Richard Startin
     * https://twitter.com/richardstartin
     *
     * @return the hash code
     */
    @Override
    public int hashCode()
    {
        // it was cached before
        if (hashCode != 0)
        {
            return hashCode;
        }

        int h = 0;
        int i = from;
        int l, l2;
        l = length & ~(8 - 1);
        l2 = length + from;

        for (; i < l; i += 8) {
            h = -1807454463 * h +
                 1742810335 * src[i+0] +
                  887503681 * src[i+1] +
                   28629151 * src[i+2] +
                     923521 * src[i+3] +
                      29791 * src[i+4] +
                        961 * src[i+5] +
                         31 * src[i+6] +
                          1 * src[i+7];
        }

        for (; i < l2; i++) {
            h = 31 * h + src[i];
        }

        return h;
    }

    /**
     * Returns the empty string if the provided buffer is null the buffer otherwise
     */
    public static XltCharBuffer emptyWhenNull(final XltCharBuffer s)
    {
        return s == null ? XltCharBuffer.empty() : s;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }

        final XltCharBuffer other = (XltCharBuffer) obj;
        if (this.length == other.length)
        {
            return Arrays.equals(this.src, from, from + length, other.src, other.from, other.from + length);
        }

        return false;
    }

    /*
     * Returns a {@code CharSequence} that is a subsequence of this sequence.
     * The subsequence starts with the {@code char} value at the specified index and
     * ends with the {@code char} value at index {@code end - 1}.  The length
     * (in {@code char}s) of the
     * returned sequence is {@code end - start}, so if {@code start == end}
     * then an empty sequence is returned.
     *
     * @param   start   the start index, inclusive
     * @param   end     the end index, exclusive
     *
     * @return  the specified subsequence
     * */
    @Override
    public CharSequence subSequence(int start, int end)
    {
        return substring(start, end);
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure
     * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
     * for all {@code x} and {@code y}.  (This
     * implies that {@code x.compareTo(y)} must throw an exception iff
     * {@code y.compareTo(x)} throws an exception.)
     *
     * @param other the buffer to compare to
     * @retuen -1, if this is smaller than other, 0 if the same, 1 if this is larger
     */
    @Override
    public int compareTo(XltCharBuffer other)
    {
        return Arrays.compare(this.src, from, from + length,
                              other.src, other.from, other.from + other.length);
    }

    public String toDebugString()
    {
        return String.format("Base=%s\nCurrent=%s\nfrom=%d, length=%d", new String(src), this, from, length);
    }
}
