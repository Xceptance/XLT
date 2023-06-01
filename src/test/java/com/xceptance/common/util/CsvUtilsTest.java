/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link CsvUtils} class.
 */

public class CsvUtilsTest
{
    private static final List<String> COMMA_DEC = List.of("fo,o", "bar", "baz");

    private static final String COMMA_ENC = "\"fo,o\",bar,baz";

    private static final List<String> EMPTY_DEC = List.of("");

    private static final String EMPTY_ENC = "";

    private static final List<String> EMPTY_FIELDS_DEC = List.of("", "", "");

    private static final String EMPTY_FIELDS_ENC = ",,";

    private static final List<String> DOUBLE_QUOTE_DEC = List.of("f\"o\"o", "bar", "baz");

    private static final String DOUBLE_QUOTE_ENC = "\"f\"\"o\"\"o\",bar,baz";

    private static final List<String> NORMAL_DEC = List.of("foo", "bar", "baz");

    private static final String NORMAL_ENC = "foo,bar,baz";

    private static final String[] NULL_ENTRIES =
        {
            "foo", null, "baz"
        };

    private static final List<String> WHITESPACE_DEC = List.of("fo\no", " bar", "baz\t");

    private static final String WHITESPACE_ENC = "\"fo\no\", bar,baz\t";

    @Test
    public void testDecode()
    {
        Assert.assertEquals(NORMAL_DEC, CsvUtils.decodeToList(NORMAL_ENC));
        Assert.assertEquals(EMPTY_DEC, CsvUtils.decodeToList(EMPTY_ENC));
        Assert.assertEquals(EMPTY_FIELDS_DEC, CsvUtils.decodeToList(EMPTY_FIELDS_ENC));
        Assert.assertEquals(WHITESPACE_DEC, CsvUtils.decodeToList(WHITESPACE_ENC));
        Assert.assertEquals(DOUBLE_QUOTE_DEC, CsvUtils.decodeToList(DOUBLE_QUOTE_ENC));
        Assert.assertEquals(COMMA_DEC, CsvUtils.decodeToList(COMMA_ENC));

        var t = new String[0];
        Assert.assertArrayEquals(NORMAL_DEC.toArray(t), CsvUtils.decode(NORMAL_ENC));
        Assert.assertArrayEquals(EMPTY_DEC.toArray(t), CsvUtils.decode(EMPTY_ENC));
        Assert.assertArrayEquals(EMPTY_FIELDS_DEC.toArray(t), CsvUtils.decode(EMPTY_FIELDS_ENC));
        Assert.assertArrayEquals(WHITESPACE_DEC.toArray(t), CsvUtils.decode(WHITESPACE_ENC));
        Assert.assertArrayEquals(DOUBLE_QUOTE_DEC.toArray(t), CsvUtils.decode(DOUBLE_QUOTE_ENC));
        Assert.assertArrayEquals(COMMA_DEC.toArray(t), CsvUtils.decode(COMMA_ENC));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeWithNull()
    {
        CsvUtils.decode(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeWithNullToList()
    {
        CsvUtils.decodeToList(null);
    }

    @Test
    public void testEncode()
    {
        Assert.assertEquals(NORMAL_ENC, CsvUtils.encode(NORMAL_DEC).toString());
        Assert.assertEquals(EMPTY_ENC, CsvUtils.encode(EMPTY_DEC).toString());
        Assert.assertEquals(EMPTY_FIELDS_ENC, CsvUtils.encode(EMPTY_FIELDS_DEC).toString());
        Assert.assertEquals(WHITESPACE_ENC, CsvUtils.encode(WHITESPACE_DEC).toString());
        Assert.assertEquals(DOUBLE_QUOTE_ENC, CsvUtils.encode(DOUBLE_QUOTE_DEC).toString());
        Assert.assertEquals(COMMA_ENC, CsvUtils.encode(COMMA_DEC).toString());
    }

    @Test
    public void testEncodeList()
    {
        Assert.assertEquals(NORMAL_ENC, CsvUtils.encode(NORMAL_DEC).toString());
        Assert.assertEquals(EMPTY_ENC, CsvUtils.encode(EMPTY_DEC).toString());
        Assert.assertEquals(EMPTY_FIELDS_ENC, CsvUtils.encode(EMPTY_FIELDS_DEC).toString());
        Assert.assertEquals(WHITESPACE_ENC, CsvUtils.encode(WHITESPACE_DEC).toString());
        Assert.assertEquals(DOUBLE_QUOTE_ENC, CsvUtils.encode(DOUBLE_QUOTE_DEC).toString());
        Assert.assertEquals(COMMA_ENC, CsvUtils.encode(COMMA_DEC).toString());
    }

    public void testEncodeWithEmptyArray()
    {
        CsvUtils.encode(List.of(new String[0]));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEncodeWithNullArrayEntries()
    {
        CsvUtils.encode(Arrays.asList((NULL_ENTRIES)));
    }

    /**
     * Decode field as null
     */
    @Test
    public void decodeField_Null()
    {
        Assert.assertNull(CsvUtils.decodeField(null));
    }

    /**
     * Decode field with too many quotes
     */
    @Test
    public void decodeFieldWithIncorrectCountOfQuotes()
    {
        final Consumer<String> f = s ->
        {
            try
            {
                // just to make the test code better readable
                CsvUtils.decodeField(s.replace("'", "\""));
                Assert.fail(String.format("Did not fail as planned: %s", s));
            }
            catch(IllegalArgumentException e)
            {
                // all good
            }
        };

        // f.accept("'"); we don't handle this case
        f.accept("'''");
        f.accept("'Foo'bar'");
        f.accept("''Foo'bar'");
        f.accept("'Foo'bar''");
    }

    /**
     * Decode fields
     */
    @Test
    public void decodeField()
    {
        final BiConsumer<String, String> f = (expected, data) ->
        {
            // for better visibility, we use ' instead of ""
            var e = expected.replace("'", "\"");
            var d = data.replace("'", "\"");

            Assert.assertEquals(e, CsvUtils.decodeField(d));
        };

        // not quoted
        f.accept("", "");
        f.accept("a", "a");
        f.accept("Foo", "Foo");
        f.accept("foo,bar", "foo,bar");

        // unbalanced
        f.accept("foo'", "foo'");
        f.accept("'foo", "'foo");

        // no quotes in quotes
        f.accept("", "''");
        f.accept("a", "'a'");
        f.accept("abc", "'abc'");

        // proper quotes in quotes
        f.accept("a'c", "'a''c'");
        f.accept("a''c", "'a''''c'");
        f.accept("a'", "'a'''");
        f.accept("'c", "'''c'");
        f.accept("a''c", "'a''''c'");
        f.accept("a'c'd", "'a''c''d'");

        // unhandled edge cases, we only decode what is fully quoted
        f.accept("'abc", "'abc");
        f.accept("abc'", "abc'");
        f.accept("a'c", "a'c");
    }

    /**
     * Encode and decode fields
     */
    @Test
    public void encodeDecodeField()
    {
        final Consumer<String> f = s -> Assert.assertEquals(s, CsvUtils.decodeField(CsvUtils.encodeField(s)));

        f.accept("");
        f.accept("foobar");
        f.accept("foo,bar");
        f.accept("foo,");
        f.accept(",foo");
        f.accept("\"foo,bar\"");
        f.accept("foo\"bar");
        f.accept("\"");
        f.accept("\"\"");
        f.accept("\"\"\"");
    }

    /**
     * Encode fields
     */
    @Test
    public void encodeField()
    {
        Assert.assertNull(CsvUtils.encodeField(null));

        Assert.assertEquals("", CsvUtils.encodeField(""));
        Assert.assertEquals(" ", CsvUtils.encodeField(" "));
        Assert.assertEquals("\"\"\"\"", CsvUtils.encodeField("\""));
        Assert.assertEquals("\"\"\"a\"\"\"", CsvUtils.encodeField("\"a\""));
        Assert.assertEquals("foobar", CsvUtils.encodeField("foobar"));
        Assert.assertEquals("\"foo,bar\"", CsvUtils.encodeField("foo,bar"));
        Assert.assertEquals("\",foo,bar,\"", CsvUtils.encodeField(",foo,bar,"));
        Assert.assertEquals("\"\"\",\"\"\"", CsvUtils.encodeField("\",\""));
    }

    /**
     * Encode fields
     */
    @Test
    public void allMustQuoteChars()
    {
        Assert.assertEquals("\"\"\"\"", CsvUtils.encodeField("\""));
        Assert.assertEquals("\"a#c\"", CsvUtils.encodeField("a#c", '#'));
        Assert.assertEquals("\"\n\"", CsvUtils.encodeField("\n"));
        Assert.assertEquals("\"\r\"", CsvUtils.encodeField("\r"));
    }
}
