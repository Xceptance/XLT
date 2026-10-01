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

import org.junit.Assert;
import org.junit.Test;

public class CsvByteLineDecoderTest
{
    private void test(String s, String... expected)
    {
        final String converted = s.replace("'", "\"");
        final byte[] bytes = converted.getBytes(StandardCharsets.UTF_8);
        final CsvByteColumns result = CsvByteLineDecoder.parse(bytes);

        Assert.assertEquals(expected.length, result.size());
        for (int i = 0; i < expected.length; i++)
        {
            Assert.assertEquals(expected[i].replace("'", "\""), result.toString(i));
        }
    }

    private void testWithOffset(String s, String... expected)
    {
        final String converted = s.replace("'", "\"");
        final byte[] raw = converted.getBytes(StandardCharsets.UTF_8);
        final byte[] padded = new byte[raw.length + 10];
        System.arraycopy(raw, 0, padded, 5, raw.length);

        final CsvByteColumns result = CsvByteLineDecoder.parse(padded, 5, raw.length);

        Assert.assertEquals(expected.length, result.size());
        for (int i = 0; i < expected.length; i++)
        {
            Assert.assertEquals(expected[i].replace("'", "\""), result.toString(i));
        }
    }

    private void testException(String s, String expected)
    {
        try
        {
            final String converted = s.replace("'", "\"");
            final byte[] bytes = converted.getBytes(StandardCharsets.UTF_8);
            CsvByteLineDecoder.parse(bytes);
        }
        catch (CsvParserException e)
        {
            assertEquals(expected, e.getMessage());
            return;
        }
        fail("No exception was raised for input: " + s);
    }

    @Test
    public void empty()
    {
        test("", "");
        testWithOffset("", "");
    }

    @Test
    public void justChars()
    {
        test("a", "a");
        test("ab", "ab");
        test("abc", "abc");
    }

    @Test
    public void justDelimiters()
    {
        test(",", "", "");
        test(",,", "", "", "");
        test(",,,", "", "", "", "");
    }

    @Test
    public void delimiterAtTheStart()
    {
        test(",", "", "");
        test(",a", "", "a");
        test(",,a", "", "", "a");
    }

    @Test
    public void delimiterAtTheEnd()
    {
        test(",", "", "");
        test("a,", "a", "");
        test("aa,", "aa", "");
        test("abc,,", "abc", "", "");
        test("abc,cccc,", "abc", "cccc", "");
    }

    @Test
    public void delimiterInTheMiddle()
    {
        test(",", "", "");
        test(",,", "", "", "");
        test("a,b", "a", "b");
        test("a,,b", "a", "", "b");
        test("a123,,b123", "a123", "", "b123");
        test("a123,,b123,,c123", "a123", "", "b123", "", "c123");
    }

    @Test
    public void spaces()
    {
        test(" ,", " ", "");
        test(", ", "", " ");
        test(" , ", " ", " ");
        test("a, ,b", "a", " ", "b");
    }

    @Test
    public void happyQuoteless()
    {
        test("a,b", "a", "b");
        test("a,b,c", "a", "b", "c");
        test("aa,bb,cc", "aa", "bb", "cc");
        test("aaa,bb,c", "aaa", "bb", "c");
        test("a,bb,ccc", "a", "bb", "ccc");
        test("a,bb,ccc,ddddd,ee,ffff", "a", "bb", "ccc", "ddddd", "ee", "ffff");
    }

    @Test
    public void minimalQuotes()
    {
        test("''", "");
        test("'',''", "", "");
        test("'','',''", "", "", "");
    }

    @Test
    public void quotesLast()
    {
        test(",''", "", "");
        test("a,''", "a", "");
        test("ab,cd,''", "ab", "cd", "");
        test("ab,cd,'e'", "ab", "cd", "e");
        test("ab,cd,'ef'", "ab", "cd", "ef");
        test("ab,cd,'ef1'", "ab", "cd", "ef1");
    }

    @Test
    public void quotesAndText()
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
    public void quotesEverywhere()
    {
        test("'a'", "a");
        test("abc,'123'", "abc", "123");
        test("'abc',123", "abc", "123");
        test("'abc',123,'4445'", "abc", "123", "4445");
        test("abc1,'1234',45", "abc1", "1234", "45");
    }

    @Test
    public void quotedQuotesSimple()
    {
        test("'''',''''", "'", "'");
        test("''''", "'");
        test(",''''", "", "'");
        test("'''',", "'", "");
    }

    @Test
    public void quotedQuotesComplex()
    {
        test("''''''", "''");
        test("'''a'''", "'a'");
        test("''''''''''''''''''", "''''''''");
    }

    @Test
    public void quotedQuotesAndText()
    {
        test("'a'''", "a'");
        test("'''a'", "'a");
        test("'a''b',cb", "a'b", "cb");
        test("'a''b',''''", "a'b", "'");
        test("'''',''''", "'", "'");
    }

    @Test
    public void delimiterInQuotes()
    {
        test("','", ",");
        test("',',','", ",", ",");
        test("''','", "',");
        test("','''", ",'");
        test("''' '''',''',''", "' '','", "");
    }

    @Test
    public void regularMixedLines()
    {
        test("a,b,c,'d,e',f,,", "a", "b", "c", "d,e", "f", "", "");
        test("abc,'123','456',,,,',,,','1012'", "abc", "123", "456", "", "", "", ",,,", "1012");
    }

    @Test
    public void unicodeSupport()
    {
        test("München,Größentabelle,€100", "München", "Größentabelle", "€100");
        test("'München','Größentabelle','€100'", "München", "Größentabelle", "€100");
        test("'München, Bayern','Größe: ''XL'''", "München, Bayern", "Größe: 'XL'");
        test("你好,世界", "你好", "世界");
    }

    @Test
    public void noEndQuote()
    {
        testException("'", "Quoted col has not been properly closed");
        testException("'abcd,", "Quoted col has not been properly closed");
        testException("'','", "Quoted col has not been properly closed");
        testException("abc,'cdef", "Quoted col has not been properly closed");
    }

    @Test
    public void noDelimiterAfterClosingQuote()
    {
        testException("'' ,", "Delimiter or end of line expected at pos: 2");
        testException("'' ", "Delimiter or end of line expected at pos: 2");
    }

    @Test
    public void brokenQuotedQuotes()
    {
        testException("'''", "Quoted field with quotes was not ended properly at: 3");
        testException("'''''", "Quoted field with quotes was not ended properly at: 5");
        testException("'','''", "Quoted field with quotes was not ended properly at: 6");
        testException("'','''',''',''", "Quoted field with quotes was not ended properly at: 14");
    }

    @Test
    public void parseLongLine()
    {
        final String line = "T,TBrowse,1571766200603,12786,true,\"java.lang.AssertionError: Response code does not match expected:<200> but was:<410> (user: 'TBrowse-165', output: '1571766200603')\\   at org.junit.Assert.fail(Assert.java:88)\\   at org.junit.Assert.failNotEquals(Assert.java:834)\\ at org.junit.Assert.assertEquals(Assert.java:645)\\  at com.xceptance.xlt.api.validators.HttpResponseCodeValidator.validate(HttpResponseCodeValidator.java:51)\\  at com.xceptance.xlt.api.validators.StandardValidator.validate(StandardValidator.java:28)\\  at com.xceptance.xlt.loadtest.validators.Validator.validateBasics(Validator.java:79)\\   at com.xceptance.xlt.loadtest.validators.Validator.validateCommonPage(Validator.java:40)\\   at com.xceptance.xlt.loadtest.validators.Validator.validateCategoryPage(Validator.java:276)\\    at com.xceptance.xlt.loadtest.actions.catalog.RefineByCategory.postValidate(RefineByCategory.java:86)\\  at com.xceptance.xlt.api.actions.AbstractAction.run(AbstractAction.java:383)\\   at com.xceptance.xlt.api.actions.AbstractWebAction.run(AbstractWebAction.java:136)\\ at com.xceptance.xlt.api.actions.AbstractHtmlPageAction.run(AbstractHtmlPageAction.java:124)\\   at com.xceptance.xlt.loadtest.actions.AbstractHtmlPageAction.runIfPossible(AbstractHtmlPageAction.java:297)\\    at com.xceptance.xlt.loadtest.flows.CategoryFlow.refineCategory(CategoryFlow.java:85)\\  at com.xceptance.xlt.loadtest.flows.CategoryFlow.run(CategoryFlow.java:43)\\ at com.xceptance.xlt.loadtest.tests.TBrowse.test(TBrowse.java:31)\\  at com.xceptance.xlt.loadtest.tests.AbstractTestCase.run(AbstractTestCase.java:59)\\ ...\",RefineByCategory";
        final byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
        final CsvByteColumns result = CsvByteLineDecoder.parse(bytes);

        Assert.assertEquals(7, result.size());
        Assert.assertEquals("T", result.getString(0));
        Assert.assertEquals("TBrowse", result.getString(1));
        Assert.assertEquals("1571766200603", result.getString(2));
        Assert.assertEquals("12786", result.getString(3));
        Assert.assertEquals("true", result.getString(4));
        Assert.assertTrue(result.getString(5).startsWith("java.lang.AssertionError"));
        Assert.assertTrue(result.getString(5).endsWith("..."));
        Assert.assertEquals("RefineByCategory", result.getString(6));
    }
}
