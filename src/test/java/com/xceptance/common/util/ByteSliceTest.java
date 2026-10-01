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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class ByteSliceTest
{
    @Test
    public void testEmpty()
    {
        final ByteSlice slice = new ByteSlice();
        assertTrue(slice.isEmpty());
        assertEquals(0, slice.getLength());
        assertEquals("", slice.toString());
        assertEquals(ByteSlice.EMPTY, slice);
    }

    @Test
    public void testWrapAndSet()
    {
        final byte[] data = "https://example.com/api/test?foo=bar".getBytes(StandardCharsets.UTF_8);
        final ByteSlice slice = new ByteSlice(data, 8, 11); // "example.com"

        assertEquals(11, slice.getLength());
        assertEquals(8, slice.getOffset());
        assertEquals("example.com", slice.toString());
        assertEquals((byte) 'e', slice.byteAt(0));
        assertEquals((byte) 'm', slice.byteAt(10));

        // Mutate in-place
        slice.set(data, 19, 9); // "/api/test"
        assertEquals(9, slice.getLength());
        assertEquals("/api/test", slice.toString());
    }

    @Test
    public void testEqualsAndHashCode()
    {
        final byte[] data1 = "prefix_https://example.com/test_suffix".getBytes(StandardCharsets.UTF_8);
        final byte[] data2 = "https://example.com/test".getBytes(StandardCharsets.UTF_8);

        final ByteSlice slice1 = new ByteSlice(data1, 7, 24);
        final ByteSlice slice2 = new ByteSlice(data2, 0, 24);

        assertEquals(slice1, slice2);
        assertEquals(slice1.hashCode(), slice2.hashCode());

        final ByteSlice copy = slice1.copy();
        assertEquals(slice1, copy);
        assertEquals(slice1.hashCode(), copy.hashCode());
        assertNotEquals(System.identityHashCode(slice1.getBuffer()), System.identityHashCode(copy.getBuffer()));
    }

    @Test
    public void testIndexOf()
    {
        final byte[] data = "https://example.com/search?q=test".getBytes(StandardCharsets.UTF_8);
        final ByteSlice slice = new ByteSlice(data, 0, data.length);

        // Found in middle
        final byte[] search1 = "search".getBytes(StandardCharsets.UTF_8);
        assertEquals(20, slice.indexOf(search1));

        // Single byte
        final byte[] singleByte = "?".getBytes(StandardCharsets.UTF_8);
        assertEquals(26, slice.indexOf(singleByte));

        // Start
        final byte[] start = "https".getBytes(StandardCharsets.UTF_8);
        assertEquals(0, slice.indexOf(start));

        // End
        final byte[] end = "test".getBytes(StandardCharsets.UTF_8);
        assertEquals(29, slice.indexOf(end));

        // Not found
        final byte[] search2 = "notfound".getBytes(StandardCharsets.UTF_8);
        assertEquals(-1, slice.indexOf(search2));

        // Empty target
        final byte[] empty = new byte[0];
        assertEquals(0, slice.indexOf(empty));

        // Null target
        assertEquals(0, slice.indexOf(null));

        // Target longer than slice
        final byte[] tooLong = new byte[data.length + 1];
        assertEquals(-1, slice.indexOf(tooLong));

        // Slice with offset and repeated partial matches
        final byte[] repeatedData = "prefix_ababaabac_suffix".getBytes(StandardCharsets.UTF_8);
        final ByteSlice subSlice = new ByteSlice(repeatedData, 7, 9); // "ababaabac"
        final byte[] target = "abac".getBytes(StandardCharsets.UTF_8);
        assertEquals(5, subSlice.indexOf(target));
    }

    @Test
    public void testCompareTo()
    {
        final ByteSlice a = new ByteSlice("abc".getBytes(StandardCharsets.UTF_8));
        final ByteSlice b = new ByteSlice("abd".getBytes(StandardCharsets.UTF_8));
        final ByteSlice c = new ByteSlice("abcd".getBytes(StandardCharsets.UTF_8));
        final ByteSlice aCopy = new ByteSlice("abc".getBytes(StandardCharsets.UTF_8));

        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertTrue(a.compareTo(c) < 0);
        assertEquals(0, a.compareTo(aCopy));
    }

    @Test
    public void testUtf8MultiByte()
    {
        final String unicodeStr = "https://example.com/münchen?größe=groß";
        final byte[] bytes = unicodeStr.getBytes(StandardCharsets.UTF_8);
        final ByteSlice slice = new ByteSlice(bytes);

        assertEquals(unicodeStr, slice.toString());
        assertEquals(slice, slice.copy());
        assertEquals(slice.hashCode(), slice.copy().hashCode());
    }

    @Test
    public void testHashCodeConsistencyAndZeroHash()
    {
        final ByteSlice empty = new ByteSlice();
        assertEquals(0, empty.hashCode());
        // Call multiple times to verify idempotence
        assertEquals(0, empty.hashCode());

        final ByteSlice slice = new ByteSlice("sample".getBytes(StandardCharsets.UTF_8));
        final int h1 = slice.hashCode();
        final int h2 = slice.hashCode();
        assertEquals(h1, h2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testByteAtNegativeIndex()
    {
        final ByteSlice slice = new ByteSlice("test".getBytes(StandardCharsets.UTF_8));
        slice.byteAt(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testByteAtOutOfBoundsIndex()
    {
        final ByteSlice slice = new ByteSlice("test".getBytes(StandardCharsets.UTF_8));
        slice.byteAt(4);
    }

    @Test
    public void testEqualsSpecialCases()
    {
        final ByteSlice slice = new ByteSlice("test".getBytes(StandardCharsets.UTF_8));
        assertTrue(slice.equals(slice));
        assertFalse(slice.equals(null));
        assertFalse(slice.equals("test"));
        assertFalse(slice.equals(Integer.valueOf(42)));
    }

    @Test
    public void testCompareToUnsigned()
    {
        // 0x7F (127) vs 0x80 (-128 in signed byte, but 128 in unsigned byte)
        final byte[] b1 = new byte[] { 0x7F };
        final byte[] b2 = new byte[] { (byte) 0x80 };
        final ByteSlice s1 = new ByteSlice(b1);
        final ByteSlice s2 = new ByteSlice(b2);

        assertTrue("0x7F should be < 0x80 in unsigned comparison", s1.compareTo(s2) < 0);
        assertTrue("0x80 should be > 0x7F in unsigned comparison", s2.compareTo(s1) > 0);
    }

    @Test
    public void testSimdVectorizedEqualsAndCompare()
    {
        // Test blocks larger than AVX-512 register size (64 bytes)
        final byte[] long1 = new byte[128];
        final byte[] long2 = new byte[128];
        for (int i = 0; i < 128; i++)
        {
            long1[i] = (byte) (i & 0x7F);
            long2[i] = (byte) (i & 0x7F);
        }
        final ByteSlice s1 = new ByteSlice(long1);
        final ByteSlice s2 = new ByteSlice(long2);

        assertEquals(s1, s2);
        assertEquals(0, s1.compareTo(s2));

        // Mismatch at byte 100
        long2[100] = (byte) 0xFF;
        assertNotEquals(s1, s2);
        assertTrue(s1.compareTo(s2) < 0);
    }
}
