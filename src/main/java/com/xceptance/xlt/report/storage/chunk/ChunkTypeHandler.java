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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.function.Consumer;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.PostProcessedDataContainer;
import com.xceptance.xlt.report.storage.dictionary.GlobalDictionaries;
import com.xceptance.xlt.report.storage.query.ScanPredicate;

/**
 * Extensible codec interface responsible for encoding, decoding, building, and scanning
 * columnar chunks for a specific {@link Data} record type in ChunkDB.
 * <p>
 * <b>Extensibility &amp; Codec Polymorphism:</b>
 * Each record type (e.g. 'R' for HTTP Requests, 'T' for Transactions, 'A' for Actions,
 * 'W' for Double Values, 'E' for Events, 'G' for Generic fallback) implements this interface.
 * When new data types are added in future XLT versions, implementing this interface and registering
 * it in {@link ChunkTypeRegistry} seamlessly integrates them into ChunkDB compression, storage, and queries.
 */
public interface ChunkTypeHandler
{
    /**
     * Returns the single-character type code handled by this implementation (e.g. 'R', 'T', 'A').
     *
     * @return single-character type code
     */
    char getTypeCode();

    /**
     * Creates a new mutable chunk builder configured for accumulating records of this type.
     *
     * @param dicts
     *            global dictionaries for string and pair interning
     * @return a new {@link ChunkBuilder} instance
     */
    ChunkBuilder newChunkBuilder(GlobalDictionaries dicts);

    /**
     * Serializes a sealed chunk of this type to the binary data output stream.
     *
     * @param chunk
     *            the sealed chunk to serialize
     * @param out
     *            the target binary data output stream
     * @throws IOException
     *             if an I/O error occurs
     */
    void writeChunk(Chunk chunk, DataOutput out) throws IOException;

    /**
     * Deserializes a sealed chunk of this type from the binary data input stream.
     *
     * @param in
     *            the source binary data input stream
     * @param dicts
     *            global dictionaries for ID resolution
     * @return the deserialized {@link Chunk} instance
     * @throws IOException
     *             if an I/O error occurs
     */
    Chunk readChunk(DataInput in, GlobalDictionaries dicts) throws IOException;

    /**
     * Decompresses columns and scans rows in the specified chunk that satisfy the given predicate,
     * decoding and streaming reconstituted {@link Data} records directly to the consumer callback.
     *
     * @param chunk
     *            the chunk to scan
     * @param predicate
     *            filter predicate specifying time bounds and ID sets
     * @param consumer
     *            callback receiving matching {@link Data} instances
     * @param dicts
     *            global dictionaries for resolving string and name attributes
     */
    void scan(Chunk chunk, ScanPredicate predicate, Consumer<Data> consumer, GlobalDictionaries dicts);

    /**
     * Decompresses columns and scans rows in the specified chunk that satisfy the given predicate,
     * decoding and streaming reconstituted {@link Data} records directly into a {@link PostProcessedDataContainer}.
     * <p>
     * Handlers may override this method to take advantage of zero-allocation container pooling and bulk
     * min/max timestamp maintenance.
     *
     * @param chunk
     *            the chunk to scan
     * @param predicate
     *            filter predicate specifying time bounds and ID sets
     * @param container
     *            the reusable data container
     * @param dicts
     *            global dictionaries for resolving string and name attributes
     */
    default void scan(Chunk chunk, ScanPredicate predicate, PostProcessedDataContainer container, GlobalDictionaries dicts)
    {
        scan(chunk, predicate, container::add, dicts);
    }

    /**
     * Resolves an in-memory chunk instance from a potentially disk-backed {@link Chunk} descriptor.
     * If {@code chunk} is a {@link DiskChunk}, its compressed payload is loaded from disk and deserialized.
     * Otherwise, the in-memory chunk is returned directly.
     *
     * @param chunk
     *            the candidate chunk descriptor
     * @param dicts
     *            global dictionaries for string and ID resolution
     * @return reconstituted in-memory chunk instance
     */
    default Chunk resolveChunk(final Chunk chunk, final GlobalDictionaries dicts)
    {
        return (chunk instanceof DiskChunk dc) ? dc.load(this, dicts) : chunk;
    }
}
