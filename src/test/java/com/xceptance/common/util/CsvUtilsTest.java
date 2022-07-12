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
package com.xceptance.common.util;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link CsvUtils} class.
 */

public class CsvUtilsTest
{
    private static final String[] COMMA_DEC =
        {
            "fo,o", "bar", "baz"
        };

    private static final String COMMA_ENC = "\"fo,o\",bar,baz";

    private static final String[] EMPTY_DEC =
        {
            ""
        };

    private static final String EMPTY_ENC = "";

    private static final String[] EMPTY_FIELDS_DEC =
        {
            "", "", ""
        };

    private static final String EMPTY_FIELDS_ENC = ",,";

    private static final String[] DOUBLE_QUOTE_DEC =
        {
            "f\"o\"o", "bar", "baz"
        };

    private static final String DOUBLE_QUOTE_ENC = "\"f\"\"o\"\"o\",bar,baz";

    private static final String[] NORMAL_DEC =
        {
            "foo", "bar", "baz"
        };

    private static final String NORMAL_ENC = "foo,bar,baz";

    private static final String[] NULL_ENTRIES =
        {
            "foo", null, "baz"
        };

    private static final String[] WHITESPACE_DEC =
        {
            "fo\no", " bar", "baz\t"
        };

    private static final String WHITESPACE_ENC = "\"fo\no\", bar,baz\t";

    @Test
    public void testDecode()
    {
        Assert.assertArrayEquals(NORMAL_DEC, CsvUtils.decode(NORMAL_ENC));
        Assert.assertArrayEquals(EMPTY_DEC, CsvUtils.decode(EMPTY_ENC));
        Assert.assertArrayEquals(EMPTY_FIELDS_DEC, CsvUtils.decode(EMPTY_FIELDS_ENC));
        Assert.assertArrayEquals(WHITESPACE_DEC, CsvUtils.decode(WHITESPACE_ENC));
        Assert.assertArrayEquals(DOUBLE_QUOTE_DEC, CsvUtils.decode(DOUBLE_QUOTE_ENC));
        Assert.assertArrayEquals(COMMA_DEC, CsvUtils.decode(COMMA_ENC));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeWithNull()
    {
        CsvUtils.decode(null);
    }

    @Test
    public void testEncode()
    {
        Assert.assertEquals(NORMAL_ENC, CsvUtils.encode(NORMAL_DEC));
        Assert.assertEquals(EMPTY_ENC, CsvUtils.encode(EMPTY_DEC));
        Assert.assertEquals(EMPTY_FIELDS_ENC, CsvUtils.encode(EMPTY_FIELDS_DEC));
        Assert.assertEquals(WHITESPACE_ENC, CsvUtils.encode(WHITESPACE_DEC));
        Assert.assertEquals(DOUBLE_QUOTE_ENC, CsvUtils.encode(DOUBLE_QUOTE_DEC));
        Assert.assertEquals(COMMA_ENC, CsvUtils.encode(COMMA_DEC));
    }

    @Test
    public void testEncodeList()
    {
        Assert.assertEquals(NORMAL_ENC, CsvUtils.encode(List.of(NORMAL_DEC)));
        Assert.assertEquals(EMPTY_ENC, CsvUtils.encode(List.of(EMPTY_DEC)));
        Assert.assertEquals(EMPTY_FIELDS_ENC, CsvUtils.encode(List.of(EMPTY_FIELDS_DEC)));
        Assert.assertEquals(WHITESPACE_ENC, CsvUtils.encode(List.of(WHITESPACE_DEC)));
        Assert.assertEquals(DOUBLE_QUOTE_ENC, CsvUtils.encode(List.of(DOUBLE_QUOTE_DEC)));
        Assert.assertEquals(COMMA_ENC, CsvUtils.encode(List.of(COMMA_DEC)));
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
    @Test(expected = IllegalArgumentException.class)
    public void decodeField_SingleQuote()
    {
        CsvUtils.decodeField("\"foob\"ar\"");
    }

    /**
     * Encode and decode fields
     */
    @Test
    public void encodeDecodeField()
    {
        String s = "";
        Assert.assertEquals(s, CsvUtils.decodeField(CsvUtils.encodeField(s)));
        s = "foobar";
        Assert.assertEquals(s, CsvUtils.decodeField(CsvUtils.encodeField(s)));
        s = "foo,bar";
        Assert.assertEquals(s, CsvUtils.decodeField(CsvUtils.encodeField(s)));
        s = "foo,";
        Assert.assertEquals(s, CsvUtils.decodeField(CsvUtils.encodeField(s)));
        s = ",foo";
        Assert.assertEquals(s, CsvUtils.decodeField(CsvUtils.encodeField(s)));
        s = "\"foo,bar\"";
        Assert.assertEquals(s, CsvUtils.decodeField(CsvUtils.encodeField(s)));
        s = "foo\"bar";
        Assert.assertEquals(s, CsvUtils.decodeField(CsvUtils.encodeField(s)));
        s = "\"";
        Assert.assertEquals(s, CsvUtils.decodeField(CsvUtils.encodeField(s)));
        s = "\"\"";
        Assert.assertEquals(s, CsvUtils.decodeField(CsvUtils.encodeField(s)));
        s = "\"\"\"";
        Assert.assertEquals(s, CsvUtils.decodeField(CsvUtils.encodeField(s)));
    }

    /**
     * Encode and decode fields
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
}
