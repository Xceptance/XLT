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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    private static final int BUFFER_INTS = 2048; // 8 KB buffer for bulk int serialization
    private static final int BUFFER_DOUBLES = 1024; // 8 KB buffer for bulk double serialization

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

    /**
     * Serializes a primitive int array prefixed with its element count using bulk {@link ByteBuffer} transfer.
     *
     * @param out
     *            target data output
     * @param array
     *            int array to serialize
     * @throws IOException
     *             if an I/O error occurs
     */
    public static void writeIntArray(final DataOutput out, final int[] array) throws IOException
    {
        if (array == null)
        {
            out.writeInt(-1);
            return;
        }
        final int len = array.length;
        out.writeInt(len);
        if (len == 0)
        {
            return;
        }
        final byte[] byteBuf = new byte[Math.min(len, BUFFER_INTS) * 4];
        final ByteBuffer bb = ByteBuffer.wrap(byteBuf).order(ByteOrder.BIG_ENDIAN);
        int offset = 0;
        while (offset < len)
        {
            final int chunkLen = Math.min(len - offset, BUFFER_INTS);
            bb.clear();
            bb.asIntBuffer().put(array, offset, chunkLen);
            out.write(byteBuf, 0, chunkLen * 4);
            offset += chunkLen;
        }
    }

    /**
     * Deserializes a primitive int array prefixed with its element count using bulk {@link ByteBuffer} transfer.
     *
     * @param in
     *            source data input
     * @return deserialized int array, or {@code null} if encoded as {@code -1}
     * @throws IOException
     *             if an I/O error occurs
     */
    public static int[] readIntArray(final DataInput in) throws IOException
    {
        final int len = in.readInt();
        if (len < 0)
        {
            return null;
        }
        final int[] array = new int[len];
        if (len == 0)
        {
            return array;
        }
        final byte[] byteBuf = new byte[Math.min(len, BUFFER_INTS) * 4];
        final ByteBuffer bb = ByteBuffer.wrap(byteBuf).order(ByteOrder.BIG_ENDIAN);
        int offset = 0;
        while (offset < len)
        {
            final int chunkLen = Math.min(len - offset, BUFFER_INTS);
            in.readFully(byteBuf, 0, chunkLen * 4);
            bb.clear();
            bb.asIntBuffer().get(array, offset, chunkLen);
            offset += chunkLen;
        }
        return array;
    }

    /**
     * Serializes a primitive double array prefixed with its element count using bulk {@link ByteBuffer} transfer.
     *
     * @param out
     *            target data output
     * @param array
     *            double array to serialize
     * @throws IOException
     *             if an I/O error occurs
     */
    public static void writeDoubleArray(final DataOutput out, final double[] array) throws IOException
    {
        if (array == null)
        {
            out.writeInt(-1);
            return;
        }
        final int len = array.length;
        out.writeInt(len);
        if (len == 0)
        {
            return;
        }
        final byte[] byteBuf = new byte[Math.min(len, BUFFER_DOUBLES) * 8];
        final ByteBuffer bb = ByteBuffer.wrap(byteBuf).order(ByteOrder.BIG_ENDIAN);
        int offset = 0;
        while (offset < len)
        {
            final int chunkLen = Math.min(len - offset, BUFFER_DOUBLES);
            bb.clear();
            bb.asDoubleBuffer().put(array, offset, chunkLen);
            out.write(byteBuf, 0, chunkLen * 8);
            offset += chunkLen;
        }
    }

    /**
     * Deserializes a primitive double array prefixed with its element count using bulk {@link ByteBuffer} transfer.
     *
     * @param in
     *            source data input
     * @return deserialized double array, or {@code null} if encoded as {@code -1}
     * @throws IOException
     *             if an I/O error occurs
     */
    public static double[] readDoubleArray(final DataInput in) throws IOException
    {
        final int len = in.readInt();
        if (len < 0)
        {
            return null;
        }
        final double[] array = new double[len];
        if (len == 0)
        {
            return array;
        }
        final byte[] byteBuf = new byte[Math.min(len, BUFFER_DOUBLES) * 8];
        final ByteBuffer bb = ByteBuffer.wrap(byteBuf).order(ByteOrder.BIG_ENDIAN);
        int offset = 0;
        while (offset < len)
        {
            final int chunkLen = Math.min(len - offset, BUFFER_DOUBLES);
            in.readFully(byteBuf, 0, chunkLen * 8);
            bb.clear();
            bb.asDoubleBuffer().get(array, offset, chunkLen);
            offset += chunkLen;
        }
        return array;
    }
}
