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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Test;

/**
 * Unit tests for {@link CsvByteColumns}.
 */
public class CsvByteColumnsTest
{
    @Test
    public void testInitialStateAndCapacity()
    {
        final CsvByteColumns cols = new CsvByteColumns();
        assertEquals(0, cols.size());
        assertNull(cols.getBuffer());

        final CsvByteColumns custom = new CsvByteColumns(10);
        assertEquals(0, custom.size());
    }

    @Test
    public void testAddAndGet()
    {
        final byte[] data = "foo,bar,123".getBytes(StandardCharsets.UTF_8);
        final CsvByteColumns cols = new CsvByteColumns();
        cols.reset(data);

        cols.add(0, 3); // "foo"
        cols.add(4, 3); // "bar"
        cols.add(8, 3); // "123"

        assertEquals(3, cols.size());
        assertSame(data, cols.getBuffer());

        assertEquals(0, cols.getOffset(0));
        assertEquals(3, cols.getLength(0));
        assertEquals("foo", cols.toString(0));
        assertEquals("foo", cols.getString(0));

        assertEquals(4, cols.getOffset(1));
        assertEquals(3, cols.getLength(1));
        assertEquals("bar", cols.toString(1));

        assertEquals(8, cols.getOffset(2));
        assertEquals(3, cols.getLength(2));
        assertEquals(123, cols.parseInt(2));
    }

    @Test
    public void testGrowBeyondInitialCapacity()
    {
        final CsvByteColumns cols = new CsvByteColumns(4);
        final byte[] data = new byte[100];
        cols.reset(data);

        for (int i = 0; i < 40; i++)
        {
            cols.add(i, 1);
        }

        assertEquals(40, cols.size());
        for (int i = 0; i < 40; i++)
        {
            assertEquals(i, cols.getOffset(i));
            assertEquals(1, cols.getLength(i));
        }
    }

    @Test
    public void testSetBufferPreservesColumns()
    {
        final byte[] buf1 = "col0,col1".getBytes(StandardCharsets.UTF_8);
        final CsvByteColumns cols = new CsvByteColumns();
        cols.reset(buf1);
        cols.add(0, 4);
        cols.add(5, 4);

        assertEquals(2, cols.size());
        assertEquals("col0", cols.toString(0));
        assertEquals("col1", cols.toString(1));

        // Simulate buffer resize/copying
        final byte[] buf2 = Arrays.copyOf(buf1, buf1.length * 2);
        cols.setBuffer(buf2);

        // Verify count and offsets are preserved
        assertEquals(2, cols.size());
        assertSame(buf2, cols.getBuffer());
        assertEquals("col0", cols.toString(0));
        assertEquals("col1", cols.toString(1));
    }

    @Test
    public void testResetAndClear()
    {
        final byte[] data = "a,b".getBytes(StandardCharsets.UTF_8);
        final CsvByteColumns cols = new CsvByteColumns();
        cols.reset(data);
        cols.add(0, 1);
        cols.add(2, 1);

        assertEquals(2, cols.size());

        // reset with another buffer clears count
        final byte[] data2 = "c".getBytes(StandardCharsets.UTF_8);
        cols.reset(data2);
        assertEquals(0, cols.size());
        assertSame(data2, cols.getBuffer());

        // clear sets buffer to null and count to 0
        cols.clear();
        assertEquals(0, cols.size());
        assertNull(cols.getBuffer());
    }

    @Test
    public void testCopy()
    {
        final byte[] data = "hello,,world".getBytes(StandardCharsets.UTF_8);
        final CsvByteColumns cols = new CsvByteColumns();
        cols.reset(data);
        cols.add(0, 5); // "hello"
        cols.add(6, 0); // "" (empty)
        cols.add(7, 5); // "world"

        final byte[] copy0 = cols.copy(0);
        assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), copy0);

        // Empty column should return the static shared EMPTY_BYTE_ARRAY
        final byte[] copy1 = cols.copy(1);
        assertEquals(0, copy1.length);
        assertSame(CsvByteColumns.EMPTY_BYTE_ARRAY, copy1);

        final byte[] copy2 = cols.copy(2);
        assertArrayEquals("world".getBytes(StandardCharsets.UTF_8), copy2);
    }

    @Test
    public void testIsEmptyAndToStringEmpty()
    {
        final byte[] data = ",".getBytes(StandardCharsets.UTF_8);
        final CsvByteColumns cols = new CsvByteColumns();
        cols.reset(data);
        cols.add(0, 0);
        cols.add(1, 0);

        assertTrue(cols.isEmpty(0));
        assertTrue(cols.isEmpty(1));
        assertEquals("", cols.toString(0));
        assertEquals("", cols.toString(1));
    }

    @Test
    public void testByteAtAndCharAt()
    {
        final byte[] data = "ABC".getBytes(StandardCharsets.UTF_8);
        final CsvByteColumns cols = new CsvByteColumns();
        cols.reset(data);
        cols.add(0, 3);

        assertEquals((byte) 'A', cols.byteAt(0, 0));
        assertEquals((byte) 'B', cols.byteAt(0, 1));
        assertEquals((byte) 'C', cols.byteAt(0, 2));

        assertEquals('A', cols.charAt(0, 0));
        assertEquals('B', cols.charAt(0, 1));
        assertEquals('C', cols.charAt(0, 2));
    }

    @Test
    public void testHasByte()
    {
        final byte[] data = "T,,".getBytes(StandardCharsets.UTF_8);
        final CsvByteColumns cols = new CsvByteColumns();
        cols.reset(data);
        cols.add(0, 1); // "T"
        cols.add(2, 0); // ""

        assertTrue(cols.hasByte(0, 0, (byte) 'T'));
        assertFalse(cols.hasByte(0, 0, (byte) 'R'));
        assertFalse(cols.hasByte(0, 1, (byte) 'T')); // index >= length

        // Empty column
        assertFalse(cols.hasByte(1, 0, (byte) 'T'));
        assertFalse(cols.hasByte(1, -1, (byte) 'T'));
    }

    @Test
    public void testEqualsStringAscii()
    {
        final byte[] data = "GET,POST,OPTIONS".getBytes(StandardCharsets.UTF_8);
        final CsvByteColumns cols = new CsvByteColumns();
        cols.reset(data);
        cols.add(0, 3);
        cols.add(4, 4);
        cols.add(9, 7);

        assertTrue(cols.equals(0, "GET"));
        assertFalse(cols.equals(0, "POST"));
        assertFalse(cols.equals(0, "GE")); // length mismatch
        assertFalse(cols.equals(0, "GETS")); // length mismatch

        assertTrue(cols.equals(1, "POST"));
        assertTrue(cols.equals(2, "OPTIONS"));
    }

    @Test
    public void testEqualsByteArraySimd()
    {
        final byte[] b1 = "long_test_string_for_simd_verification_0123456789_abcdefghijklmnopqrstuvwxyz".getBytes(StandardCharsets.UTF_8);
        final CsvByteColumns cols = new CsvByteColumns();
        cols.reset(b1);
        cols.add(0, b1.length);

        final byte[] match = b1.clone();
        assertTrue(cols.equals(0, match));

        assertFalse(cols.equals(0, (byte[]) null));

        final byte[] lengthMismatch = Arrays.copyOf(b1, b1.length - 1);
        assertFalse(cols.equals(0, lengthMismatch));

        final byte[] contentMismatch = b1.clone();
        contentMismatch[50] = (byte) '!';
        assertFalse(cols.equals(0, contentMismatch));
    }

    @Test
    public void testParsePrimitiveNumbers()
    {
        final byte[] data = "123,456789012345,12.345,true,false".getBytes(StandardCharsets.UTF_8);
        final CsvByteColumns cols = new CsvByteColumns();
        cols.reset(data);
        cols.add(0, 3);
        cols.add(4, 12);
        cols.add(17, 6);
        cols.add(24, 4);
        cols.add(29, 5);

        assertEquals(123, cols.parseInt(0));
        assertEquals(456789012345L, cols.parseLong(1));
        assertEquals(12.345, cols.parseDouble(2), 0.0001);
        assertTrue(cols.parseBoolean(3));
        assertFalse(cols.parseBoolean(4));
    }
}
