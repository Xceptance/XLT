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
package com.xceptance.common.io;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.junit.Assert;
import org.junit.Test;

public class XltBufferedByteLineReaderTest
{
    private String compose(String[] s, String sep)
    {
        return Arrays.stream(s).collect(Collectors.joining(sep));
    }

    private void test(String[] s, String sep, int bufferSize)
    {
        test(compose(s, sep), bufferSize);
    }

    private void test(String[] s, String sep)
    {
        test(compose(s, sep), 8192);
    }

    private void test(String src, int bufferSize)
    {
        final byte[] srcBytes = src.getBytes(StandardCharsets.UTF_8);

        final List<String> newBR = new ArrayList<>();
        try (final XltBufferedByteLineReader r = new XltBufferedByteLineReader(new ByteArrayInputStream(srcBytes), bufferSize))
        {
            byte[] line = null;
            while ((line = r.readLine()) != null)
            {
                newBR.add(new String(line, StandardCharsets.UTF_8));
            }
        }
        catch (IOException e)
        {
            Assert.fail(e.getMessage());
        }

        final List<String> original = new ArrayList<>();
        try (final BufferedReader r = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(srcBytes), StandardCharsets.UTF_8), bufferSize))
        {
            String osb = null;
            while ((osb = r.readLine()) != null)
            {
                original.add(osb);
            }
        }
        catch (IOException e)
        {
            Assert.fail(e.getMessage());
        }

        // verify
        Assert.assertEquals(original.size(), newBR.size());
        Assert.assertArrayEquals(original.toArray(), newBR.toArray());
    }

    @Test
    public void empty()
    {
        test("", 100);
        test("", 1);
    }

    @Test
    public void oneLine_NoEnding()
    {
        test("T", 100);
        test("Test Test", 100);
        test("Test TestTest TestTest TestTest TestTest TestTest TestTest TestTest TestTest TestTest TestTest Test", 1000);
        test("Test TestTest TestTest TestTest TestTest TestTest TestTest TestTest TestTest TestTest TestTest Test", 10);
    }

    @Test
    public void oneLine_And_Ending()
    {
        test("T\r\n", 20);
        test("T\r", 10);
        test("T\n", 5);
    }

    @Test
    public void happyPathThreeLines()
    {
        final String[] data = new String[]
        {
            "Test", "Foobar", "Mario and the Gang"
        };

        test(data, "\r");
        test(data, "\n");
        test(data, "\r\n");
    }

    @Test
    public void happyPathEmptyLineMiddle()
    {
        final String[] data = new String[]
        {
            "T", "", "A"
        };

        test(data, "\r");
        test(data, "\n");
        test(data, "\r\n");
    }

    @Test
    public void happyPathEmpty()
    {
        test("\r", 100);
        test("\n", 100);
        test("\r\n", 100);
    }

    @Test
    public void twoEmptyLine()
    {
        final String[] data = new String[]
        {
            "", ""
        };

        test(data, "\r");
        test(data, "\n");
        test(data, "\r\n");
    }

    @Test
    public void happyPathEmptyOnly3Full()
    {
        final String[] data = new String[]
        {
            "T", "a", "B"
        };

        test(data, "\r");
        test(data, "\n");
        test(data, "\r\n");
    }

    @Test
    public void bufferSmallerThanLine()
    {
        final String[] data = new String[]
        {
            "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789", // 100
            "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789",
            "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789",
            "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789",
            "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"
        };

        test(data, "\r", 25);
        test(data, "\n", 99);
        test(data, "\r\n", 50);
    }

    @Test
    public void tinyBuffers()
    {
        final String[] data = new String[]
        {
            "Line1", "Line2", "Line3"
        };

        for (int bufSize = 1; bufSize <= 5; bufSize++)
        {
            test(data, "\r", bufSize);
            test(data, "\n", bufSize);
            test(data, "\r\n", bufSize);
        }
    }

    @Test
    public void hugeBuffer()
    {
        final String[] data = new String[]
        {
            "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789",
            "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789",
            "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"
        };

        test(data, "\r", 1500);
        test(data, "\n", 2000);
        test(data, "\r\n", 10000);
    }

    @Test
    public void bufferLargerThanLine()
    {
        final String[] data = new String[]
        {
            "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789",
            "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"
        };

        test(data, "\r", 250);
        test(data, "\n", 250);
        test(data, "\r\n", 250);
    }

    @Test
    public void lastLineEmpty()
    {
        final String data = "A12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678E\n" +
                            "A12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678E\n";

        test(data, 10);
        test(data, 99);
        test(data, 100);
        test(data, 101);
        test(data, 1000);
    }

    @Test
    public void unicodeAcrossBufferBoundaries()
    {
        final String[] data = new String[]
        {
            "München, Bayern",
            "Größentabelle für Kleidung",
            "你好世界，这是一个测试",
            "Price: 100€ and 50¢",
            "Emoji: 🚀🎉🔥"
        };

        for (int bufSize = 2; bufSize <= 30; bufSize += 3)
        {
            test(data, "\n", bufSize);
            test(data, "\r\n", bufSize);
            test(data, "\r", bufSize);
        }
    }

    public List<byte[]> readViaByteLineReader(InputStream in)
    {
        final List<byte[]> result = new ArrayList<>();
        try (final XltBufferedByteLineReader r = new XltBufferedByteLineReader(in))
        {
            byte[] line;
            while ((line = r.readLine()) != null)
            {
                result.add(line);
            }
        }
        catch (IOException e)
        {
            Assert.fail(e.getMessage());
        }
        return result;
    }

    public List<String> readViaBufferedReader(InputStream in)
    {
        final List<String> result = new ArrayList<>();
        try (final BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)))
        {
            String osb;
            while ((osb = r.readLine()) != null)
            {
                result.add(osb);
            }
        }
        catch (IOException e)
        {
            Assert.fail(e.getMessage());
        }
        return result;
    }

    @Test
    public void compareGzipFiles() throws IOException
    {
        compare("/timers.csv.gz");
        compare("/timers-dos.csv.gz");
        compare("/timers-mac.csv.gz");
    }

    public void compare(String fileName) throws IOException
    {
        try (var i1 = new GZIPInputStream(XltBufferedByteLineReaderTest.class.getResourceAsStream(fileName));
             var i2 = new GZIPInputStream(XltBufferedByteLineReaderTest.class.getResourceAsStream(fileName)))
        {
            final List<String> regular = readViaBufferedReader(i1);
            final List<byte[]> byteLines = readViaByteLineReader(i2);

            Assert.assertEquals(regular.size(), byteLines.size());

            for (int i = 0; i < regular.size(); i++)
            {
                String expected = regular.get(i);
                String actual = new String(byteLines.get(i), StandardCharsets.UTF_8);
                Assert.assertEquals("Mismatch at line " + i, expected, actual);
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullStream()
    {
        new XltBufferedByteLineReader(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroBufferSize()
    {
        new XltBufferedByteLineReader(new ByteArrayInputStream(new byte[0]), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeBufferSize()
    {
        new XltBufferedByteLineReader(new ByteArrayInputStream(new byte[0]), -10);
    }

    @Test
    public void testCloseClosesUnderlyingStream() throws IOException
    {
        final boolean[] closed = new boolean[] { false };
        final InputStream in = new ByteArrayInputStream("hello\nworld".getBytes(StandardCharsets.UTF_8))
        {
            @Override
            public void close() throws IOException
            {
                closed[0] = true;
                super.close();
            }
        };

        final XltBufferedByteLineReader reader = new XltBufferedByteLineReader(in);
        Assert.assertFalse(closed[0]);
        reader.close();
        Assert.assertTrue(closed[0]);
    }

    @Test
    public void testRepeatedReadLineAtEof() throws IOException
    {
        final InputStream in = new ByteArrayInputStream("single line\n".getBytes(StandardCharsets.UTF_8));
        try (final XltBufferedByteLineReader reader = new XltBufferedByteLineReader(in))
        {
            Assert.assertNotNull(reader.readLine());
            Assert.assertNull(reader.readLine());
            Assert.assertNull(reader.readLine());
            Assert.assertNull(reader.readLine());
        }
    }

    @Test
    public void testEmptyLinesReturnConstant() throws IOException
    {
        final InputStream in = new ByteArrayInputStream("\n\n".getBytes(StandardCharsets.UTF_8));
        try (final XltBufferedByteLineReader reader = new XltBufferedByteLineReader(in))
        {
            final byte[] line1 = reader.readLine();
            final byte[] line2 = reader.readLine();
            Assert.assertNull(reader.readLine());

            Assert.assertNotNull(line1);
            Assert.assertNotNull(line2);
            Assert.assertEquals(0, line1.length);
            Assert.assertEquals(0, line2.length);
            Assert.assertSame(XltBufferedByteLineReader.EMPTY_BYTE_ARRAY, line1);
            Assert.assertSame(XltBufferedByteLineReader.EMPTY_BYTE_ARRAY, line2);
        }
    }

    @Test
    public void testMixedLineEndingsInSingleStream() throws IOException
    {
        final String mixed = "line1\r\nline2\nline3\rline4\r\nline5";
        final InputStream in = new ByteArrayInputStream(mixed.getBytes(StandardCharsets.UTF_8));
        try (final XltBufferedByteLineReader reader = new XltBufferedByteLineReader(in, 8))
        {
            Assert.assertEquals("line1", new String(reader.readLine(), StandardCharsets.UTF_8));
            Assert.assertEquals("line2", new String(reader.readLine(), StandardCharsets.UTF_8));
            Assert.assertEquals("line3", new String(reader.readLine(), StandardCharsets.UTF_8));
            Assert.assertEquals("line4", new String(reader.readLine(), StandardCharsets.UTF_8));
            Assert.assertEquals("line5", new String(reader.readLine(), StandardCharsets.UTF_8));
            Assert.assertNull(reader.readLine());
        }
    }
}
