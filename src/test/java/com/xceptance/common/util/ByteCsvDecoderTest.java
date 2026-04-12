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
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * Tests for {@link ByteCsvDecoder}. These test cases mirror the {@link CsvLineDecoderTest}
 * cases to ensure byte-identical behavior.
 *
 * @implNote Exclusively created by AI (Antigravity).
 */
public final class ByteCsvDecoderTest
{
    final void test(String s, String... expected)
    {
        final byte[] data = s.replace("'", "\"").getBytes(StandardCharsets.UTF_8);
        final CsvByteRow result = ByteCsvDecoder.parse(data, 0, data.length);

        Assert.assertEquals(expected.length, result.length());
        for (int i = 0; i < expected.length; i++)
        {
            Assert.assertEquals(expected[i].replace("'", "\""), result.getString(i));
        }
    }

    final void testException(String s, String expectedMsg)
    {
        try
        {
            final byte[] data = s.replace("'", "\"").getBytes(StandardCharsets.UTF_8);
            ByteCsvDecoder.parse(data, 0, data.length);
        }
        catch (CsvParserException e)
        {
            final var msg = e.getMessage();
            assertEquals(expectedMsg, msg);
            return;
        }
        fail("No exception was raised");
    }

    // === Basic field tests (same as CsvLineDecoderTest) ===

    @Test
    public final void empty()
    {
        test("", "");
    }

    @Test
    public final void justChars()
    {
        test("a", "a");
        test("ab", "ab");
        test("abc", "abc");
    }

    @Test
    public final void justDelimiters()
    {
        test(",", "", "");
        test(",,", "", "", "");
        test(",,,", "", "", "", "");
    }

    @Test
    public final void delimiterAtTheStart()
    {
        test(",", "", "");
        test(",a", "", "a");
        test(",,a", "", "", "a");
    }

    @Test
    public final void delimiterAtTheEnd()
    {
        test(",", "", "");
        test("a,", "a", "");
        test("aa,", "aa", "");
        test("abc,,", "abc", "", "");
        test("abc,cccc,", "abc", "cccc", "");
    }

    @Test
    public final void delimiterInTheMiddle()
    {
        test(",", "", "");
        test(",,", "", "", "");
        test("a,b", "a", "b");
        test("a,,b", "a", "", "b");
        test("a123,,b123", "a123", "", "b123");
        test("a123,,b123,,c123", "a123", "", "b123", "", "c123");
    }

    @Test
    public final void spaces()
    {
        test(" ,", " ", "");
        test(", ", "", " ");
        test(" , ", " ", " ");
        test("a, ,b", "a", " ", "b");
    }

    @Test
    public final void happyQuoteless()
    {
        test("a,b", "a", "b");
        test("a,b,c", "a", "b", "c");
        test("aa,bb,cc", "aa", "bb", "cc");
        test("aaa,bb,c", "aaa", "bb", "c");
        test("a,bb,ccc", "a", "bb", "ccc");
        test("a,bb,ccc,ddddd,ee,ffff", "a", "bb", "ccc", "ddddd", "ee", "ffff");
    }

    // === Quoted fields ===

    @Test
    public final void minimalQuotes()
    {
        test("''", "");
        test("'',''", "", "");
        test("'','',''", "", "", "");
    }

    @Test
    public final void quotesLast()
    {
        test(",''", "", "");
        test("a,''", "a", "");
        test("ab,cd,''", "ab", "cd", "");
        test("ab,cd,'e'", "ab", "cd", "e");
        test("ab,cd,'ef'", "ab", "cd", "ef");
        test("ab,cd,'ef1'", "ab", "cd", "ef1");
    }

    @Test
    public final void quotesAndText()
    {
        test("'a','b'", "a", "b");
        test("'a'", "a");
        test("'aa'", "aa");
        test("'aaa'", "aaa");
        test("'aaaa'", "aaaa");
        test("'aa','bb'", "aa", "bb");
        test("'aaa','bbb'", "aaa", "bbb");
    }

    @Test
    public final void quotesEverywhere()
    {
        test("'a'", "a");
        test("abc,'123'", "abc", "123");
        test("'abc',123", "abc", "123");
        test("'abc',123,'4445'", "abc", "123", "4445");
        test("abc1,'1234',45", "abc1", "1234", "45");
    }

    // === Quoted quotes ===

    @Test
    public final void quotedQuotesSimple()
    {
        test("'''',''''", "'", "'");
        test("''''", "'");
        test(",''''", "", "'");
        test("'''',", "'", "");
    }

    @Test
    public final void quotedQuotesComplex()
    {
        test("''''''", "''");
        test("'''a'''", "'a'");
        test("''''''''''''''''''", "''''''''");
    }

    @Test
    public final void quotedQuotesAndText()
    {
        test("'a'''", "a'");
        test("'''a'", "'a");
        test("'a''b',cb", "a'b", "cb");
        test("'a''b',''''", "a'b", "'");
        test("'''',''''", "'", "'");
    }

    @Test
    public final void delimiterInQuotes()
    {
        test("','", ",");
        test("',',','", ",", ",");
        test("''','", "',");
        test("','''", ",'");
        test("''' '''',''',''" , "' '','" , "");
    }

    @Test
    public final void regularMixedLines()
    {
        test("a,b,c,'d,e',f,,", "a", "b", "c", "d,e", "f", "", "");
        test("abc,'123','456',,,,',,,','1012'", "abc", "123", "456", "", "", "", ",,,", "1012");
    }

    // === Error cases ===

    @Test
    public final void noEndQuote()
    {
        testException("'", "Quoted col has not been properly closed");
        testException("'abcd,", "Quoted col has not been properly closed");
        testException("'','" , "Quoted col has not been properly closed");
        testException("abc,'cdef" , "Quoted col has not been properly closed");
    }

    @Test
    public final void noDelimiterAfterClosingQuote()
    {
        testException("'' ,", "Delimiter or end of line expected at pos: 2");
        testException("'' ", "Delimiter or end of line expected at pos: 2");
    }

    @Test
    public final void brokenQuotedQuotes()
    {
        testException("'''", "Quoted field with quotes was not ended properly at: 3");
        testException("'''''", "Quoted field with quotes was not ended properly at: 5");
        testException("'','''", "Quoted field with quotes was not ended properly at: 6");
        testException("'','''',''',''" , "Quoted field with quotes was not ended properly at: 14");
    }

    // === Inline numeric parsing tests ===

    @Test
    public final void parseLongSimple()
    {
        Assert.assertEquals(0, ByteCsvDecoder.parseLong("0".getBytes(), 0, 1));
        Assert.assertEquals(1, ByteCsvDecoder.parseLong("1".getBytes(), 0, 1));
        Assert.assertEquals(123, ByteCsvDecoder.parseLong("123".getBytes(), 0, 3));
        Assert.assertEquals(1234567890123L, ByteCsvDecoder.parseLong("1234567890123".getBytes(), 0, 13));
    }

    @Test
    public final void parseLongNegative()
    {
        Assert.assertEquals(-1, ByteCsvDecoder.parseLong("-1".getBytes(), 0, 2));
        Assert.assertEquals(-999, ByteCsvDecoder.parseLong("-999".getBytes(), 0, 4));
    }

    @Test
    public final void parseLongWithOffset()
    {
        final byte[] data = "abc,12345,xyz".getBytes();
        Assert.assertEquals(12345, ByteCsvDecoder.parseLong(data, 4, 5));
    }

    @Test(expected = NumberFormatException.class)
    public final void parseLongEmpty()
    {
        ByteCsvDecoder.parseLong("".getBytes(), 0, 0);
    }

    @Test(expected = NumberFormatException.class)
    public final void parseLongInvalidDigit()
    {
        ByteCsvDecoder.parseLong("12x".getBytes(), 0, 3);
    }

    @Test
    public final void parseIntSimple()
    {
        Assert.assertEquals(42, ByteCsvDecoder.parseInt("42".getBytes(), 0, 2));
        Assert.assertEquals(-7, ByteCsvDecoder.parseInt("-7".getBytes(), 0, 2));
    }

    // === Parity: byte decoder produces identical output to char decoder ===

    @Test
    public final void parityWithCsvLineDecoder()
    {
        final String[] testLines = {
            "T,TOrder,1624271441111,0,true,1234",
            "R,Homepage,1624271441222,233,true,200,12345,http://localhost:8080/,text/html,1234,0",
            "A,Homepage,1624271441111",
            "E,SomeEvent,1624271441333,,Regular",
            "a,bb,ccc,\"d,e\",f,,",
            "abc,\"123\",\"456\",,,,\",,,\",\"1012\"",
            ""
        };

        for (final String line : testLines)
        {
            final List<XltCharBuffer> charResult = CsvLineDecoder.parse(line);
            final byte[] lineBytes = line.getBytes(StandardCharsets.UTF_8);
            final CsvByteRow byteResult = ByteCsvDecoder.parse(lineBytes, 0, lineBytes.length);

            Assert.assertEquals("Field count mismatch for: " + line, charResult.size(), byteResult.length());
            for (int i = 0; i < charResult.size(); i++)
            {
                Assert.assertEquals("Field " + i + " mismatch for: " + line,
                                    charResult.get(i).toString(), byteResult.getString(i));
            }
        }
    }
}
