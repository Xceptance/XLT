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
package com.xceptance.common.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * Tests the implementation of {@link Utf8Reader}.
 */
@RunWith(JUnitParamsRunner.class)
public class Utf8ReaderTest
{
    /**
     * Checks that the reader can read test texts encoded with the given encodings.
     */
    @Test
    @Parameters(value =
        {
            "UTF-8      | 日本の東京", //
            "UTF-8      | ッ ツ ヅ ミ べ ボ プ", //
            "UTF-8      | äöüÄÖÜßáàÁÀ", //
            "UTF-8      | foobar", //
            "ISO-8859-1 | foobar", // same bytes as with UTF-8
            "US-ASCII   | foobar", // same bytes as with UTF-8
    })
    public void read(final String charsetName, final String text) throws IOException
    {
        doRead(charsetName, text);
    }

    /**
     * Checks that the reader throws an IOException for test texts encoded with the given encodings.
     */
    @Test(expected = IOException.class)
    @Parameters(value =
        {
            "ISO-8859-1 | äöüÄÖÜßáàÁÀ",  //
            "UTF-16     | äöüÄÖÜßáàÁÀ",  //
            "UTF-16BE   | äöüÄÖÜßáàÁÀ",  //
            "UTF-16LE   | äöüÄÖÜßáàÁÀ",  //
    })
    public void read_illegalEncoding(final String charsetName, final String text) throws IOException
    {
        doRead(charsetName, text);
    }

    private void doRead(final String charsetName, final String text) throws IOException
    {
        // get the bytes of the text in the wanted encoding
        final byte[] bytes = text.getBytes(charsetName);

        // now read the bytes in again via the Utf8Reader and check the resulting text
        try (final Reader reader = new Utf8Reader(new ByteArrayInputStream(bytes)))
        {
            final char[] chars = new char[1024];
            final int charsRead = reader.read(chars);

            final String actualText = new String(chars, 0, charsRead);

            Assert.assertEquals(text.length(), charsRead);
            Assert.assertEquals(text, actualText);
        }
    }
}
