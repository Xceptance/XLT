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
package com.xceptance.xlt.report.storage.compression;

import java.util.Arrays;

import me.lemire.integercompression.FastPFOR128;
import me.lemire.integercompression.IntCompressor;
import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.SkippableComposition;
import me.lemire.integercompression.VariableByte;
import me.lemire.integercompression.differential.IntegratedBinaryPacking;
import me.lemire.integercompression.differential.IntegratedIntCompressor;
import me.lemire.integercompression.differential.IntegratedVariableByte;
import me.lemire.integercompression.differential.SkippableIntegratedComposition;

/**
 * High-performance SIMD integer and timeline compression codec wrapping Daniel Lemire's
 * {@code JavaFastPFOR} library.
 * <p>
 * <b>Architectural Rationale &amp; Compression Techniques:</b>
 * <ul>
 *   <li><b>Constant-Value RLE Fast Path:</b> In load test chunks (up to 64,000 rows), many columns are
 *       completely uniform across an entire chunk (e.g., all HTTP status codes = {@code 200}, all HTTP methods =
 *       {@code GET}, all DNS/connect times = {@code 0} on keep-alive connections, or a single agent/scenario).
 *       When all elements in an array of length \(\ge 4\) are identical, {@link #compress(int[])} encodes the
 *       entire column as a 3-integer tuple {@code {CONSTANT_MARKER, length, value}} (12 bytes instead of
 *       FastPFOR block headers). Analytical scan loops can inspect {@link #isConstant(int[])} and
 *       {@link #getConstantValue(int[])} in \(O(1)\) time without allocating or decompressing a 64,000-element
 *       {@code int[]} array.</li>
 *   <li><b>SIMD Bit-Packing (FastPFOR128):</b> Compresses blocks of 128 integers by finding the minimum
 *       bit width required to represent the majority of values, packing them using vectorized bit-shifts,
 *       and encoding exceptional outliers in a separate overflow area. This achieves speeds of hundreds of
 *       millions of integers per second on modern superscalar CPUs while saving up to 80-90% of memory.</li>
 *   <li><b>Tail Compression (VariableByte):</b> Because FastPFOR operates strictly on 128-integer blocks,
 *       any remainder elements (less than 128) at the end of an array cannot be processed by SIMD packing alone.
 *       We compose {@link FastPFOR128} with {@link VariableByte} via {@link SkippableComposition}, ensuring
 *       seamless and lossless compression of integer arrays of any arbitrary length.</li>
 *   <li><b>Integrated Differential Compression (Delta + Binary Packing):</b> For monotonically increasing
 *       sequences such as record timestamps or row offsets, differential encoding calculates successive
 *       deltas \(d_i = t_i - t_{i-1}\). Using {@link IntegratedBinaryPacking} combined with
 *       {@link IntegratedVariableByte}, differences are calculated and packed in a single fused CPU pass,
 *       dramatically reducing the entropy and bit width needed to store timeline data.</li>
 *   <li><b>Thread Safety &amp; Allocations:</b> Compression codecs in JavaFastPFOR maintain internal state
 *       and scratch buffers that are not inherently safe for concurrent access. To eliminate lock contention
 *       and avoid per-chunk memory allocation overhead during parallel ingestion and parallel report scanning,
 *       instances are maintained in {@link ThreadLocal} storage.</li>
 * </ul>
 */
public final class FastIntegerCodec
{
    /**
     * Sentinel marker stored at {@code compressed[0]} to identify a constant-value RLE-compressed column
     * {@code {CONSTANT_MARKER, uncompressedLength, constantValue}}.
     * <p>
     * Because {@link IntCompressor#compress(int[])} always writes the positive array length ({@code >= 1})
     * into {@code compressed[0]}, {@link Integer#MIN_VALUE} is mathematically disjoint from every possible
     * FastPFOR/VariableByte compressed array header.
     */
    public static final int CONSTANT_MARKER = Integer.MIN_VALUE;

    /**
     * Thread-local headless composition codec used for zero-allocation decompression into caller-supplied
     * scratch buffers via {@link #decompressTo(int[], int[])}.
     */
    private static final ThreadLocal<SkippableComposition> GENERAL_CODEC = ThreadLocal.withInitial(() ->
        new SkippableComposition(new FastPFOR128(), new VariableByte())
    );

    /**
     * Thread-local compressor for general integer arrays (response times, bytes sent, response codes, dictionary IDs).
     * Composes FastPFOR128 for 128-element blocks with VariableByte for tail elements.
     */
    private static final ThreadLocal<IntCompressor> GENERAL_COMPRESSOR = ThreadLocal.withInitial(() ->
        new IntCompressor(GENERAL_CODEC.get())
    );

    /**
     * Thread-local compressor for monotonically increasing integer sequences (e.g., timestamp offsets).
     * Integrates delta calculation with binary bit-packing for maximum speed and density.
     */
    private static final ThreadLocal<IntegratedIntCompressor> DELTA_COMPRESSOR = ThreadLocal.withInitial(() ->
        new IntegratedIntCompressor(new SkippableIntegratedComposition(new IntegratedBinaryPacking(), new IntegratedVariableByte()))
    );

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private FastIntegerCodec()
    {
    }

    /**
     * Compresses an array of 32-bit integers using constant-value RLE detection followed by SIMD FastPFOR128
     * with VariableByte tail fallback.
     * <p>
     * Used for integer columns such as run times, byte counts, response codes, and interned string/timer IDs.
     *
     * @param data
     *            the uncompressed input array of integers (may be {@code null} or empty)
     * @return a newly allocated compressed array of integers, or an empty array if input is {@code null} or empty
     */
    public static int[] compress(final int[] data)
    {
        // Guard against null or empty input
        if (data == null || data.length == 0)
        {
            return new int[0];
        }

        // Fast-path: check whether all elements in the column are identical (length >= 4)
        final int len = data.length;
        if (len >= 4)
        {
            final int first = data[0];
            boolean allSame = true;
            for (int i = 1; i < len; i++)
            {
                if (data[i] != first)
                {
                    allSame = false;
                    break;
                }
            }
            if (allSame)
            {
                return new int[] { CONSTANT_MARKER, len, first };
            }
        }

        // FastPFOR + VariableByte compression using thread-local recycler
        return GENERAL_COMPRESSOR.get().compress(data);
    }

    /**
     * Returns {@code true} if the given compressed buffer was encoded as a constant-value RLE tuple
     * {@code {CONSTANT_MARKER, uncompressedLength, constantValue}}.
     *
     * @param compressed
     *            compressed integer buffer
     * @return {@code true} if all elements in the column share a single constant value
     */
    public static boolean isConstant(final int[] compressed)
    {
        return compressed != null && compressed.length == 3 && compressed[0] == CONSTANT_MARKER;
    }

    /**
     * Returns the constant integer value of a column where {@link #isConstant(int[])} is {@code true}.
     *
     * @param compressed
     *            constant-encoded integer buffer
     * @return the constant integer value shared by all rows in the column
     */
    public static int getConstantValue(final int[] compressed)
    {
        return compressed[2];
    }

    /**
     * Returns the uncompressed element count of a compressed integer buffer without decompressing it.
     *
     * @param compressed
     *            compressed integer buffer
     * @return uncompressed element count, or {@code 0} if {@code null} or empty
     */
    public static int getUncompressedLength(final int[] compressed)
    {
        if (compressed == null || compressed.length == 0)
        {
            return 0;
        }
        return isConstant(compressed) ? compressed[1] : compressed[0];
    }

    /**
     * Decompresses an integer array previously compressed with {@link #compress(int[])}.
     *
     * @param compressed
     *            the compressed integer buffer (may be {@code null} or empty)
     * @return the uncompressed array of original integers, or an empty array if input is {@code null} or empty
     */
    public static int[] decompress(final int[] compressed)
    {
        // Guard against null or empty input
        if (compressed == null || compressed.length == 0)
        {
            return new int[0];
        }

        if (isConstant(compressed))
        {
            final int len = compressed[1];
            final int val = compressed[2];
            final int[] out = new int[len];
            if (val != 0)
            {
                Arrays.fill(out, val);
            }
            return out;
        }

        // Unpack integer blocks into original sequence
        return GENERAL_COMPRESSOR.get().uncompress(compressed);
    }

    /**
     * Decompresses an integer array previously compressed with {@link #compress(int[])} directly into the
     * caller-provided {@code reuseBuffer} when it has sufficient capacity ({@code reuseBuffer.length >= uncompressedLength}),
     * avoiding a 256 KB {@code int[64000]} heap allocation per column during high-throughput scans.
     *
     * @param compressed
     *            the compressed integer buffer (may be {@code null} or empty)
     * @param reuseBuffer
     *            optional reusable destination buffer; if {@code null} or smaller than the uncompressed length,
     *            a new array of exact length is allocated and returned
     * @return {@code reuseBuffer} (populated in {@code [0, uncompressedLength)}) or a newly allocated array
     */
    public static int[] decompressTo(final int[] compressed, final int[] reuseBuffer)
    {
        if (compressed == null || compressed.length == 0)
        {
            return new int[0];
        }

        if (isConstant(compressed))
        {
            final int len = compressed[1];
            final int val = compressed[2];
            final int[] out = (reuseBuffer != null && reuseBuffer.length >= len) ? reuseBuffer : new int[len];
            Arrays.fill(out, 0, len, val);
            return out;
        }

        final int uncompressedLength = compressed[0];
        if (reuseBuffer == null || reuseBuffer.length < uncompressedLength)
        {
            return GENERAL_COMPRESSOR.get().uncompress(compressed);
        }

        // FastPFOR128 assumes 0-bitwidth blocks in the output array are zero-initialized
        Arrays.fill(reuseBuffer, 0, uncompressedLength, 0);
        final IntWrapper inpos = new IntWrapper(1);
        final IntWrapper outpos = new IntWrapper(0);
        GENERAL_CODEC.get().headlessUncompress(compressed, inpos, compressed.length - 1, reuseBuffer, outpos, uncompressedLength);
        return reuseBuffer;
    }

    /**
     * Compresses a non-decreasing (monotonically non-decreasing) array of integers using integrated
     * delta calculation followed by SIMD bit-packing.
     * <p>
     * Highly effective for strictly sorted integer sequences. Note: In ChunkDB multi-threaded ingestion,
     * records arrive concurrently across worker threads and network sockets, meaning per-chunk timestamps
     * are generally not monotonically ascending. Standard {@link #compress(int[])} is therefore used for
     * chunk timestamp columns.
     *
     * @param ascendingData
     *            strictly non-decreasing integer sequence
     * @return a compressed array of integers representing the differential sequence
     */
    public static int[] compressDelta(final int[] ascendingData)
    {
        // Guard against null or empty input
        if (ascendingData == null || ascendingData.length == 0)
        {
            return new int[0];
        }

        // Fused delta + binary packing compression
        return DELTA_COMPRESSOR.get().compress(ascendingData);
    }

    /**
     * Decompresses an integer sequence previously compressed with {@link #compressDelta(int[])}.
     * Reconstructs the original ascending sequence via cumulative sum prefix decoding.
     *
     * @param compressed
     *            the compressed integer buffer
     * @return the original uncompressed ascending sequence of integers
     */
    public static int[] decompressDelta(final int[] compressed)
    {
        // Guard against null or empty input
        if (compressed == null || compressed.length == 0)
        {
            return new int[0];
        }

        // Integrated decompression with running prefix sum restoration
        return DELTA_COMPRESSOR.get().uncompress(compressed);
    }

    /**
     * Compresses an array of 16-bit short values (such as dictionary ID columns) by widening them
     * to unsigned integers and running FastPFOR SIMD compression.
     * <p>
     * Because values are within the range {@code [0, 65535]}, the leading 16 bits are zeroes, allowing
     * FastPFOR bit-packing to pack multiple values into a single 32-bit machine word.
     *
     * @param shorts
     *            array of 16-bit short values
     * @return compressed integer buffer
     */
    public static int[] compressShorts(final short[] shorts)
    {
        // Guard against null or empty input
        if (shorts == null || shorts.length == 0)
        {
            return new int[0];
        }

        // Widen shorts to unsigned 32-bit ints to feed FastPFOR
        final int[] ints = new int[shorts.length];
        for (int i = 0; i < shorts.length; i++)
        {
            ints[i] = Short.toUnsignedInt(shorts[i]);
        }

        return compress(ints);
    }

    /**
     * Decompresses an integer buffer into an array of 16-bit short values.
     *
     * @param compressed
     *            compressed integer buffer
     * @return decompressed array of 16-bit short values
     */
    public static short[] decompressShorts(final int[] compressed)
    {
        // Guard against null or empty input
        if (compressed == null || compressed.length == 0)
        {
            return new short[0];
        }

        // Decompress to ints first
        final int[] ints = decompress(compressed);

        // Narrow down to short array
        final short[] shorts = new short[ints.length];
        for (int i = 0; i < ints.length; i++)
        {
            shorts[i] = (short) ints[i];
        }

        return shorts;
    }
}
