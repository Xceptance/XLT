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

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A {@link Reader} implementation that reads UTF-8-encoded text from an {@link InputStream} throwing an
 * {@link IOException} if the text is not valid UTF-8.
 * <p>
 * Note: This class buffers the content of the stream in memory so it should be used for small amounts of data only.
 */
public class Utf8Reader extends Reader
{
    /**
     * The reader we delegate to when actually dealing out the characters.
     */
    private final CharArrayReader charArrayReader;

    /**
     * Creates a new {@link Utf8Reader} instance.
     * 
     * @param inputStream
     *            the stream to read the text from
     * @throws IOException
     *             if the input stream could not be read or does not represent UTF-8-encoded text
     */
    public Utf8Reader(final InputStream inputStream) throws IOException
    {
        final byte[] bytes = inputStream.readAllBytes();
        char[] chars;

        try
        {
            chars = getAsChars(bytes, StandardCharsets.UTF_8);
        }
        catch (final CharacterCodingException cce)
        {
            throw new IOException("Data does not represent UTF-8-encoded text", cce);
        }

        charArrayReader = new CharArrayReader(chars);
    }

    /**
     * Converts the given bytes to chars according to the specified character set.
     * 
     * @param bytes
     *            the input bytes
     * @param charset
     *            the character set
     * @return the corresponding chars
     * @throws CharacterCodingException
     *             if the bytes do not represent text encoded with the given character set
     */
    private static char[] getAsChars(final byte[] bytes, final Charset charset) throws CharacterCodingException
    {
        final CharBuffer charBuffer = charset.newDecoder().decode(ByteBuffer.wrap(bytes));

        final char[] chars = new char[charBuffer.length()];
        charBuffer.get(chars);

        return chars;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException
    {
        return charArrayReader.read(cbuf, off, len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        charArrayReader.close();
    }
}
