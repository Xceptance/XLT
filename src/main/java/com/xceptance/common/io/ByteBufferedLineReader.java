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
 * A high-performance buffered line reader that operates directly on raw byte streams,
 * avoiding the overhead of {@code InputStreamReader}'s byte-to-char decoding. Each line
 * is returned as a compact {@code byte[]} array.
 * <p>
 * This reader handles {@code \n}, {@code \r}, and {@code \r\n} line terminators.
 * It is designed for XLT timer CSV files which are ASCII-compatible UTF-8.
 *
 *
 * @implNote Exclusively created by AI (Antigravity).
 * @author Xceptance
 * @since 10.0
 */
public final class ByteBufferedLineReader implements Closeable
{
    private final InputStream in;
    private final byte[] buffer;
    private int bufferPos;
    private int bufferLength;
    private boolean skipLF = false;
    private boolean eof = false;

    private static final int DEFAULT_BUFFER_SIZE = 2 * 8192;

    public ByteBufferedLineReader(final InputStream in)
    {
        this(in, DEFAULT_BUFFER_SIZE);
    }

    public ByteBufferedLineReader(final InputStream in, final int bufferSize)
    {
        this.in = in;
        this.buffer = new byte[bufferSize];
    }

    @Override
    public final void close() throws IOException
    {
        if (in != null)
        {
            in.close();
        }
    }

    /**
     * Reads a single line from the stream.
     *
     * @return the line as a byte array (without line terminator), or {@code null} at EOF
     * @throws IOException
     *             on I/O error
     */
    public final byte[] readLine() throws IOException
    {
        if (eof)
        {
            return null;
        }

        byte[] result = null;
        int resultLength = 0;
        int start = bufferPos;

        for (;;)
        {
            if (bufferPos >= bufferLength)
            {
                // save any partial line data before refilling
                if (start < bufferPos && bufferPos > 0)
                {
                    final int len = bufferPos - start;
                    result = append(buffer, start, len, result, resultLength, len + 80);
                    resultLength += len;
                }

                final int read = fill();
                start = 0;

                if (read == -1)
                {
                    eof = true;
                    return (result == null || resultLength == 0) ? null : Arrays.copyOf(result, resultLength);
                }
            }

            // skip LF after CR
            if (skipLF && buffer[bufferPos] == '\n')
            {
                start = ++bufferPos;
            }
            skipLF = false;

            // scan for line terminator
            for (int i = bufferPos; i < bufferLength; i++)
            {
                final byte b = buffer[i];
                if (b == '\n' || b == '\r')
                {
                    if (b == '\r')
                    {
                        skipLF = true;
                    }

                    bufferPos = i + 1;

                    // Fast path: if no data was accumulated from previous buffer fills,
                    // we can return a direct slice from the buffer — avoids the append()
                    // intermediate array and saves one allocation per line.
                    if (result == null)
                    {
                        return Arrays.copyOfRange(buffer, start, i);
                    }

                    // Slow path: line spans multiple buffer fills — append remainder
                    final int lineLen = i - start;
                    result = append(buffer, start, lineLen, result, resultLength, lineLen);
                    resultLength += lineLen;
                    return Arrays.copyOf(result, resultLength);
                }
            }


            // no terminator found — save what we have and refill
            final int len = bufferLength - start;
            if (len > 0)
            {
                result = append(buffer, start, len, result, resultLength, len + 80);
                resultLength += len;
            }
            bufferPos = bufferLength;
            start = bufferLength;
        }
    }

    private final int fill() throws IOException
    {
        bufferPos = 0;
        int read;
        do
        {
            read = in.read(buffer);
        }
        while (read == 0);

        bufferLength = Math.max(read, 0);
        return read;
    }

    private static final byte[] append(final byte[] src, final int srcOffset, final int length,
                                 byte[] dest, final int destLength, final int initialCapacity)
    {
        if (dest == null)
        {
            dest = new byte[Math.max(initialCapacity, length)];
            System.arraycopy(src, srcOffset, dest, 0, length);
            return dest;
        }

        final int newLength = destLength + length;
        if (newLength > dest.length)
        {
            dest = Arrays.copyOf(dest, newLength * 2);
        }
        System.arraycopy(src, srcOffset, dest, destLength, length);
        return dest;
    }

    /**
     * Reads a single line exactly into a zero-allocation leased buffer without runtime heap assignments natively.
     * 
     * @return the written byte length or -1.
     */
    public final int readInto(final byte[] dest, final int destOffset) throws IOException
    {
        if (eof)
        {
            return -1;
        }

        int start = bufferPos;
        int currentDestOffset = destOffset;

        for (;;)
        {
            if (bufferPos >= bufferLength)
            {
                if (start < bufferPos && bufferPos > 0)
                {
                    final int len = bufferPos - start;
                    System.arraycopy(buffer, start, dest, currentDestOffset, len);
                    currentDestOffset += len;
                }

                final int read = fill();
                start = 0;

                if (read == -1)
                {
                    eof = true;
                    final int bytesWritten = currentDestOffset - destOffset;
                    return (bytesWritten == 0) ? -1 : bytesWritten;
                }
            }

            if (skipLF && buffer[bufferPos] == '\n')
            {
                start = ++bufferPos;
            }
            skipLF = false;

            for (int i = bufferPos; i < bufferLength; i++)
            {
                final byte b = buffer[i];
                if (b == '\n' || b == '\r')
                {
                    if (b == '\r') skipLF = true;

                    bufferPos = i + 1;
                    final int lineLen = i - start;
                    System.arraycopy(buffer, start, dest, currentDestOffset, lineLen);
                    return (currentDestOffset + lineLen) - destOffset;
                }
            }

            final int len = bufferLength - start;
            if (len > 0)
            {
                System.arraycopy(buffer, start, dest, currentDestOffset, len);
                currentDestOffset += len;
            }
            bufferPos = bufferLength;
            start = bufferLength;
        }
    }
}
