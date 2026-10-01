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

import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry mapping single-character record type codes to their corresponding {@link ChunkTypeHandler} codecs.
 * <p>
 * <b>Extensibility &amp; Fallback Architecture:</b>
 * <ul>
 *   <li><b>Dedicated High-Performance SIMD Codecs:</b> Built-in standard record types ('R' for Requests,
 *       'T' for Transactions, 'A'/'C'/'P' for Timers/Actions/Pages, 'W'/'V' for Double Values, 'E' for Events)
 *       utilize specialized columnar layouts compressed with {@link com.xceptance.xlt.report.storage.compression.FastIntegerCodec}.</li>
 *   <li><b>Dynamic Generic Fallback:</b> If a future XLT version introduces a new record type or a custom user test
 *       emits a custom {@link com.xceptance.xlt.api.engine.Data} implementation not explicitly registered here,
 *       {@link #getHandler(char)} dynamically instantiates and registers a {@link GenericDataChunkHandler} for that
 *       character code. This ensures zero risk of breakage or unsupported-type exceptions on unknown data streams.</li>
 * </ul>
 */
public class ChunkTypeRegistry
{
    /** Process-wide singleton instance. */
    private static final ChunkTypeRegistry INSTANCE = new ChunkTypeRegistry();

    /** Thread-safe map of registered handlers keyed by type code. */
    private final ConcurrentHashMap<Character, ChunkTypeHandler> handlers = new ConcurrentHashMap<>();

    /**
     * Returns the singleton instance of {@link ChunkTypeRegistry}.
     *
     * @return the shared registry instance
     */
    public static ChunkTypeRegistry getInstance()
    {
        return INSTANCE;
    }

    /**
     * Constructs a new {@link ChunkTypeRegistry} and registers all built-in specialized chunk codecs.
     */
    public ChunkTypeRegistry()
    {
        // Specialized high-throughput HTTP Request codec
        registerHandler(new RequestChunkHandler());

        // Specialized Transaction scenario codec
        registerHandler(new TransactionChunkHandler());

        // Generic timer codecs for Action ('A'), Custom Timer ('C'), and Page ('P')
        registerHandler(new TimerChunkHandler('A'));
        registerHandler(new TimerChunkHandler('C'));
        registerHandler(new TimerChunkHandler('P'));

        // Double value metric codecs ('W' for weighted/DoubleValue, 'V' for custom values)
        registerHandler(new DoubleValueChunkHandler('W'));
        registerHandler(new DoubleValueChunkHandler('V'));

        // Event metric codec ('E')
        registerHandler(new EventChunkHandler());
    }

    /**
     * Registers a custom or specialized {@link ChunkTypeHandler} for a specific record type code.
     *
     * @param handler
     *            the handler to register
     */
    public void registerHandler(final ChunkTypeHandler handler)
    {
        handlers.put(handler.getTypeCode(), handler);
    }

    /**
     * Retrieves the {@link ChunkTypeHandler} registered for the specified record type code.
     * <p>
     * If no specialized handler has been registered for {@code typeCode}, a {@link GenericDataChunkHandler}
     * is dynamically created and cached, guaranteeing that arbitrary {@link com.xceptance.xlt.api.engine.Data}
     * instances can be stored and queried without error.
     *
     * @param typeCode
     *            the single-character type code
     * @return the resolved specialized handler, or a dynamic generic fallback handler
     */
    public ChunkTypeHandler getHandler(final char typeCode)
    {
        final ChunkTypeHandler handler = handlers.get(typeCode);
        if (handler != null)
        {
            return handler;
        }

        // Dynamically instantiate and register GenericDataChunkHandler fallback
        return handlers.computeIfAbsent(typeCode, GenericDataChunkHandler::new);
    }

    /**
     * Checks if a specialized handler has been explicitly registered for this type code.
     *
     * @param typeCode
     *            the single-character type code
     * @return true if an explicit handler is registered
     */
    public boolean hasHandler(final char typeCode)
    {
        return handlers.containsKey(typeCode);
    }
}
