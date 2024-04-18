/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
import java.io.Reader;

import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * This class combines a BufferedReader and an OpenStringBuilder to keep the read and copy effort low. This class got
 * inspired by the BufferedReader from the JDK as well as the BufferedReader from Android. It is a rewrite to parse the
 * stream correctly and efficiently.
 *
 * @author Rene Schwietzke
 * @since 7.0.0
 */
public class XltBufferedLineReader implements Closeable
{
    // where to read out data from
    private Reader reader;

    // the buffer with the data from disk, we try to always have
    // the guarantee to read from the start, when we refill, we will
    // reset it in a way that we start from the beginning again
    private char[] buffer;

    // last read buffer pos
    private int bufferPos;

    // real buffer length
    private int bufferLength;

    // follow the JDK 21 change to set this to 16k by default
    private static final int BUFFERSIZE = 2 * 8192;

    // in case we read a \r, we might have to skip the following \n
    private boolean skipNL = false;

    private boolean eof = false;

    public XltBufferedLineReader(final Reader reader)
    {
        this.reader = reader;
        this.buffer = new char[BUFFERSIZE];
    }

    public XltBufferedLineReader(final Reader reader, final int bufferSize)
    {
        this.reader = reader;
        this.buffer = new char[bufferSize];
    }

    @Override
    public void close() throws IOException
    {
        if (reader != null)
        {
            reader.close();
        }
    }

    /**
     * Fill the buffer
     *
     * @return how much we read
     * @throws IOException
     */
    private int fill() throws IOException
    {
        // we can read the entire array
        bufferPos = 0;

        // read until we got something or reached end
        int read = 0;
        do
        {
            read = reader.read(buffer);
        }
        while (read == 0);

        return bufferLength = read;
    }

    /**
     * Our readline part
     * 
     * @throws IOException
     */
    public XltCharBuffer readLine() throws IOException
    {
        if (eof)
        {
            return null;
        }

        char[] sb = null;
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
                // end reached
                eof = true;

                // save the rest
                return sb == null ? null : new XltCharBuffer(sb, 0, sbLength);
            }

            // do we have to skip a newline?
            if (skipNL && buffer[bufferPos] == '\n')
            {
                start = ++bufferPos;
            }
            skipNL = false;

            // run till we have a '\r'
            boolean eol = false;
            int i;
            for (i = bufferPos; i < bufferLength; i++)
            {
                final char c = buffer[i];
                if (c <= '\r') // help to save comparisons
                {
                    if (c == '\r')
                    {
                        skipNL = eol = true;
                        break;
                    }
                    if (c == '\n')
                    {
                        eol = true;
                        break;
                    }
                }
            }
            bufferPos = i;
            final int l = i - start;

            if (eol)
            {
                sb = append(buffer, start, l, sb, sbLength, l);
                sbLength = sbLength + l;
                bufferPos++;

                return new XltCharBuffer(sb, 0, sbLength);
            }
            else if (start < bufferPos)
            {
                // buffer empty and not end in sight
                // save what we have
                sb = append(buffer, start, l, sb, sbLength, 80);
                sbLength = sbLength + l;
            }
        }
    }

    /**
     * Append the char to the buffer and return it or create a new and copy the data and return that.
     *
     * @param start
     *            starting position inclusive
     * @param end
     *            end position exclusive
     * @return the array with the appended data, either new or the old
     */
    private static char[] append(char[] src, final int start, final int length, char[] dest, final int currentLength,
                                 final int initialCapacity)
    {
        if (dest == null)
        {
            dest = new char[Math.max(initialCapacity, length)];
            System.arraycopy(src, start, dest, 0, length);

            return dest;
        }

        if (length > 0)
        {
            final int newLength = currentLength + length;
            if (newLength > dest.length)
            {
                // grow
                final char[] old = dest;
                dest = new char[newLength * 2];
                System.arraycopy(old, 0, dest, 0, currentLength);
            }
            System.arraycopy(src, start, dest, currentLength, length);
        }

        return dest;
    }
}
