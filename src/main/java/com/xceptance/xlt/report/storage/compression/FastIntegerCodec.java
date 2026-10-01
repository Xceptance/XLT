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

import me.lemire.integercompression.FastPFOR128;
import me.lemire.integercompression.IntCompressor;
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
     * Thread-local compressor for general integer arrays (response times, bytes sent, response codes, dictionary IDs).
     * Composes FastPFOR128 for 128-element blocks with VariableByte for tail elements.
     */
    private static final ThreadLocal<IntCompressor> GENERAL_COMPRESSOR = ThreadLocal.withInitial(() ->
        new IntCompressor(new SkippableComposition(new FastPFOR128(), new VariableByte()))
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
     * Compresses an array of 32-bit integers using SIMD FastPFOR128 with VariableByte tail fallback.
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

        // FastPFOR + VariableByte compression using thread-local recycler
        return GENERAL_COMPRESSOR.get().compress(data);
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

        // Unpack integer blocks into original sequence
        return GENERAL_COMPRESSOR.get().uncompress(compressed);
    }

    /**
     * Compresses a non-decreasing (monotonically non-decreasing) array of integers using integrated
     * delta calculation followed by SIMD bit-packing.
     * <p>
     * Highly effective for timestamp offsets within a chunk: because timestamps are sorted or near-sorted,
     * deltas between successive entries are small positive integers requiring very few bits per entry.
     *
     * @param ascendingData
     *            non-decreasing integer sequence (e.g., millisecond offsets from chunk minTime)
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
