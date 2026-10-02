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
package com.xceptance.xlt.report.storage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Common I/O serialization utilities for ChunkDB binary storage formats.
 * <p>
 * Standard {@link DataOutput#writeUTF(String)} is limited to 65,535 bytes in UTF-8 representation
 * and throws {@link java.io.UTFDataFormatException} if a string (such as an exception stack trace
 * or large payload) exceeds that limit. The utilities here use a 32-bit length prefix followed by
 * raw UTF-8 bytes to safely support arbitrarily large strings without truncation or exceptions.
 */
public final class StorageIoUtils
{
    private StorageIoUtils()
    {
    }

    /**
     * Serializes a string to the given data output using a 32-bit signed integer length prefix
     * followed by UTF-8 bytes. A {@code null} string is encoded as length {@code -1}.
     *
     * @param out
     *            target data output
     * @param str
     *            string to write (may be null)
     * @throws IOException
     *             if an I/O error occurs
     */
    public static void writeUtfString(final DataOutput out, final String str) throws IOException
    {
        if (str == null)
        {
            out.writeInt(-1);
        }
        else
        {
            final byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            out.writeInt(bytes.length);
            out.write(bytes);
        }
    }

    /**
     * Deserializes a string previously written by {@link #writeUtfString(DataOutput, String)}.
     *
     * @param in
     *            source data input
     * @return deserialized string, or {@code null} if encoded as {@code -1}
     * @throws IOException
     *             if an I/O error occurs
     */
    public static String readUtfString(final DataInput in) throws IOException
    {
        final int len = in.readInt();
        if (len < 0)
        {
            return null;
        }
        if (len == 0)
        {
            return "";
        }
        final byte[] bytes = new byte[len];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
