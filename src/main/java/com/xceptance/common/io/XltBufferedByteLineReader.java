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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Fast buffered line reader that reads lines of raw bytes directly from an {@link InputStream}
 * without intermediate decoding into {@code char[]} or {@code String}.
 * <p>
 * Handles {@code \n}, {@code \r\n}, and legacy {@code \r} line terminators.
 *
 * @author Rene Schwietzke
 */
public class XltBufferedByteLineReader implements Closeable
{
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private final InputStream in;

    private final byte[] buffer;

    private int bufferPos;

    private int bufferLength;

    private static final int DEFAULT_BUFFER_SIZE = 2 * 8192;

    private boolean skipNL = false;

    private boolean eof = false;

    public XltBufferedByteLineReader(final InputStream in)
    {
        this(in, DEFAULT_BUFFER_SIZE);
    }

    public XltBufferedByteLineReader(final InputStream in, final int bufferSize)
    {
        if (in == null)
        {
            throw new IllegalArgumentException("InputStream must not be null");
        }
        if (bufferSize <= 0)
        {
            throw new IllegalArgumentException("Buffer size must be > 0");
        }
        this.in = in;
        this.buffer = new byte[bufferSize];
    }

    @Override
    public void close() throws IOException
    {
        if (in != null)
        {
            in.close();
        }
    }

    /**
     * Refill the internal byte buffer from the stream.
     */
    private int fill() throws IOException
    {
        bufferPos = 0;
        int read = 0;
        do
        {
            read = in.read(buffer);
        }
        while (read == 0);

        return bufferLength = read;
    }

    /**
     * Reads a line of bytes from the input stream.
     *
     * @return the line as a byte array (excluding newline characters), or {@code null} if the end of the stream has been reached
     * @throws IOException
     *             if an I/O error occurs
     */
    public byte[] readLine() throws IOException
    {
        if (eof)
        {
            return null;
        }

        byte[] sb = null;
        int lastFill = 0;
        int start = bufferPos;
        int sbLength = 0;

        for (;;)
        {
            if (bufferPos == bufferLength)
            {
                lastFill = fill();
                start = 0;
            }

            if (lastFill == -1)
            {
                eof = true;
                if (sb == null)
                {
                    return null;
                }
                return sbLength == 0 ? EMPTY_BYTE_ARRAY : Arrays.copyOf(sb, sbLength);
            }

            if (skipNL && buffer[bufferPos] == (byte) '\n')
            {
                start = ++bufferPos;
            }
            skipNL = false;

            boolean eol = false;
            int i;
            for (i = bufferPos; i < bufferLength; i++)
            {
                final byte b = buffer[i];
                if (b == (byte) '\r')
                {
                    skipNL = true;
                    eol = true;
                    break;
                }
                if (b == (byte) '\n')
                {
                    eol = true;
                    break;
                }
            }
            bufferPos = i;
            final int l = i - start;

            if (eol)
            {
                bufferPos++;
                if (sb == null)
                {
                    // Fast path: line was completely within the buffer
                    if (l == 0)
                    {
                        return EMPTY_BYTE_ARRAY;
                    }
                    return Arrays.copyOfRange(buffer, start, start + l);
                }
                else
                {
                    sb = append(buffer, start, l, sb, sbLength);
                    sbLength += l;
                    if (sbLength == 0)
                    {
                        return EMPTY_BYTE_ARRAY;
                    }
                    if (sbLength == sb.length)
                    {
                        return sb;
                    }
                    return Arrays.copyOf(sb, sbLength);
                }
            }
            else if (start < bufferPos)
            {
                // Buffer ended before finding EOL, accumulate chunk
                sb = append(buffer, start, l, sb, sbLength);
                sbLength += l;
            }
        }
    }

    private static byte[] append(final byte[] src, final int start, final int length, byte[] dest, final int currentLength)
    {
        if (dest == null)
        {
            dest = new byte[Math.max(80, length)];
            System.arraycopy(src, start, dest, 0, length);
            return dest;
        }

        if (length > 0)
        {
            final int newLength = currentLength + length;
            if (newLength > dest.length)
            {
                final int expanded = Math.max(newLength, dest.length * 2);
                final byte[] old = dest;
                dest = new byte[expanded];
                System.arraycopy(old, 0, dest, 0, currentLength);
            }
            System.arraycopy(src, start, dest, currentLength, length);
        }

        return dest;
    }
}
