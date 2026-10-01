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

import com.xceptance.xlt.api.engine.Data;

/**
 * Mutable builder that accumulates streaming {@link Data} records of a specific type code
 * up to maximum chunk capacity (typically 64,000 rows).
 * <p>
 * <b>Lifecycle:</b>
 * Records are ingested sequentially into internal columnar buffers. When the builder reaches capacity
 * or the input stream completes, {@link #seal()} executes FastPFOR bit-packing compression across all
 * primitive arrays and constructs an immutable {@link Chunk}.
 */
public interface ChunkBuilder
{
    /**
     * Default maximum rows per columnar chunk (64,000 rows).
     * Sized to optimize L2/L3 CPU cache residency during SIMD decompressions.
     */
    int DEFAULT_CHUNK_CAPACITY = 64_000;

    /**
     * Returns the single-character type code of records this builder accepts (e.g. 'R', 'T', 'A').
     *
     * @return single-character record type code
     */
    char getTypeCode();

    /**
     * Returns the current number of records accumulated in this builder so far.
     *
     * @return current row count
     */
    int getRowCount();

    /**
     * Checks whether this builder has reached its maximum row capacity.
     *
     * @return {@code true} if builder is at capacity; {@code false} if more rows can be accepted
     */
    boolean isFull();

    /**
     * Appends a {@link Data} record to the chunk builder.
     *
     * @param record
     *            the data record to append
     * @return {@code true} if successfully appended; {@code false} if the chunk is already full
     */
    boolean append(Data record);

    /**
     * Compresses all accumulated columnar vectors, computes index bitmaps and timestamp bounds,
     * and seals this builder into an immutable {@link Chunk}.
     *
     * @return the sealed, immutable columnar chunk
     */
    Chunk seal();
}
