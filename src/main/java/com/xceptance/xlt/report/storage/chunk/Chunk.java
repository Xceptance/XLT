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
package com.xceptance.xlt.report.storage.chunk;

import org.roaringbitmap.RoaringBitmap;

/**
 * Base interface representing an immutable, sealed columnar chunk of load test records in ChunkDB.
 * <p>
 * <b>Columnar Storage Model:</b>
 * Rather than holding an array of Java object references, each sealed chunk contains contiguous
 * primitive arrays compressed using SIMD FastPFOR and bit-packing algorithms. Each chunk also
 * exposes bounding box metadata (minimum and maximum timestamps) and {@link RoaringBitmap} sets
 * of contained 16-bit dictionary IDs (timer names and agent/test case pairs) to support fast
 * zero-allocation query pruning before any column is decompressed.
 */
public interface Chunk
{
    /**
     * Returns the single-character record type code representing the kind of records stored in this chunk.
     * Examples: 'R' (Request), 'T' (Transaction), 'A' (Action), 'C' (Custom Timer), 'P' (Page),
     * 'W' (Double Value / Weighted), 'E' (Event), or 'G' (Generic).
     *
     * @return single-character record type code
     */
    char getTypeCode();

    /**
     * Returns the total number of records contained in this chunk (typically up to 64,000 rows).
     *
     * @return row count
     */
    int getRowCount();

    /**
     * Returns the earliest timestamp (milliseconds since Unix epoch) among all records in this chunk.
     *
     * @return minimum timestamp in milliseconds
     */
    long getMinTime();

    /**
     * Returns the latest timestamp (milliseconds since Unix epoch) among all records in this chunk.
     *
     * @return maximum timestamp in milliseconds
     */
    long getMaxTime();

    /**
     * Returns a {@link RoaringBitmap} containing all distinct 16-bit interned timer name IDs present in this chunk.
     * <p>
     * Used by query engines for \(O(1)\) bitwise intersection pruning against query timer filters.
     *
     * @return bitmap of contained timer name IDs, or {@code null} if this record type has no timer names
     */
    RoaringBitmap getTimerNameIds();

    /**
     * Returns a {@link RoaringBitmap} containing all distinct 16-bit interned (agent, testCase) pair IDs
     * present in this chunk.
     * <p>
     * Used by query engines for \(O(1)\) bitwise intersection pruning against query agent/test case filters.
     *
     * @return bitmap of contained agent and test case IDs, or {@code null} if this record type has no agent/test case pairs
     */
    RoaringBitmap getAgentTestCaseIds();
}
