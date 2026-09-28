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
package com.xceptance.xlt.report.providers;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.xceptance.common.collection.FastHashMap;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.api.report.PostProcessedDataContainer;
import com.xceptance.xlt.api.report.ReportProvider;
import com.xceptance.xlt.api.util.SimpleArrayList;

/**
 * The {@link AbstractDataProcessorBasedReportProvider} class provides common functionality of a typical report
 * provider, which internally uses {@link AbstractDataProcessor} instances to calculate statistics.
 */
public abstract class AbstractDataProcessorBasedReportProvider<T extends AbstractDataProcessor> extends AbstractReportProvider
{
    /**
     * The data processor class.
     */
    private final Class<T> implClass;

    /**
     * A mapping from timer names to data processor instances.
     */
    private final FastHashMap<String, T> processors = new FastHashMap<String, T>(11, 0.5f);

    /**
     * Size of the direct-mapped processor cache (must be a power of two).
     */
    private static final int PROCESSOR_CACHE_SIZE = 32;

    /**
     * Bitmask for fast modulo calculation into the direct-mapped processor cache.
     */
    private static final int PROCESSOR_CACHE_MASK = PROCESSOR_CACHE_SIZE - 1;

    /**
     * Direct-mapped cache of timer names to eliminate hash map lookups when timer names
     * alternate across records in a chunk.
     */
    private final String[] cachedNames = new String[PROCESSOR_CACHE_SIZE];

    /**
     * Direct-mapped cache of processor instances corresponding to {@link #cachedNames}.
     */
    @SuppressWarnings("unchecked")
    private final T[] cachedProcessors = (T[]) new AbstractDataProcessor[PROCESSOR_CACHE_SIZE];

    /**
     * Fast-path single-item cache: the name of the most recently resolved timer.
     * When consecutive records share the exact same timer name reference (the most common pattern
     * within a single chunk), this avoids slot hashing and array access entirely.
     */
    private String lastTimerName;

    /**
     * Fast-path single-item cache: the processor instance for {@link #lastTimerName}.
     */
    private T lastProcessor;

    /**
     * Creates a new {@link AbstractDataProcessorBasedReportProvider} instance.
     * 
     * @param c
     *            the data processor implementation class
     */
    protected AbstractDataProcessorBasedReportProvider(final Class<T> c)
    {
        this.implClass = c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data stat)
    {
        final T processor = getProcessor(stat.getName());
        processor.processDataRecord(stat);
    }

    /**
     * Batch record processing for processor-based report providers.
     * <p>
     * Delegates to {@link AbstractReportProvider#processAll(PostProcessedDataContainer)}, which iterates
     * over the container's records and invokes {@link #processDataRecord(Data)} for each item. This ensures
     * that all subclass-specific type guards (such as {@code instanceof} checks in concrete providers like
     * {@link CustomValuesReportProvider}, {@link ActionsReportProvider}, or {@link RequestsReportProvider})
     * are strictly respected when processing mixed, heterogeneous, or untyped chunks.
     * </p>
     * <p>
     * Subclasses that specialize in a single homogeneous chunk type (such as 'R', 'A', 'V') provide their
     * own optimized batch loops for that specific type, falling back to this method when encountering chunks
     * with mixed or differing type codes.
     * </p>
     *
     * @param dataContainer
     *            the container holding post-processed records for this chunk
     */
    @Override
    public void processAll(final PostProcessedDataContainer dataContainer)
    {
        // Delegate to base class processAll which safely calls processDataRecord(stat) per record,
        // honoring any instanceof guards and filtering defined by the concrete report provider subclass.
        super.processAll(dataContainer);
    }

    /**
     * Returns the data processor responsible for timers with the given name.
     * <p>
     * <b>Caching Hierarchy:</b>
     * <ol>
     *   <li>Level 1: Fast-path identity check against {@link #lastTimerName}. In sequential chunk processing,
     *       hundreds of consecutive records often share the identical interned timer name pointer. This check
     *       resolves in 1 CPU cycle without hashing or array reads.</li>
     *   <li>Level 2: 32-slot direct-mapped array cache indexed by {@code name.hashCode() & 31}. When records
     *       alternate between a small set of timers, this eliminates hash table lookups and lock synchronization.</li>
     *   <li>Level 3: Underlying {@link FastHashMap} lookup and lazy constructor instantiation.</li>
     * </ol>
     * 
     * @param name
     *            the timer name
     * @return the data processor
     */
    protected T getProcessor(final String name)
    {
        if (name != null)
        {
            // Level 1: Immediate reference-equality hit for consecutive identical timer records
            if (name == lastTimerName && lastProcessor != null)
            {
                return lastProcessor;
            }

            // Level 2: Direct-mapped 32-slot array cache
            final int slot = name.hashCode() & PROCESSOR_CACHE_MASK;
            final String cachedName = cachedNames[slot];

            if (cachedName != null && (cachedName == name || cachedName.equals(name)))
            {
                final T processor = cachedProcessors[slot];
                lastTimerName = name;
                lastProcessor = processor;
                return processor;
            }

            // Level 3: Map lookup / lazy instantiation
            T processor = processors.get(name);
            if (processor == null)
            {
                // lazily create a processor for that timer name
                try
                {
                    final Constructor<T> constructor = implClass.getConstructor(String.class, AbstractReportProvider.class);
                    processor = constructor.newInstance(name, this);
                }
                catch (final Exception ex)
                {
                    throw new RuntimeException("Failed to instantiate processor for timer: " + name, ex);
                }

                processors.put(name, processor);
            }

            cachedNames[slot] = name;
            cachedProcessors[slot] = processor;
            lastTimerName = name;
            lastProcessor = processor;

            return processor;
        }

        // Fallback for null timer names
        T processor = processors.get(null);
        if (processor == null)
        {
            try
            {
                final Constructor<T> constructor = implClass.getConstructor(String.class, AbstractReportProvider.class);
                processor = constructor.newInstance(null, this);
            }
            catch (final Exception ex)
            {
                throw new RuntimeException("Failed to instantiate processor for null name", ex);
            }

            processors.put(null, processor);
        }

        return processor;
    }

    /**
     * Returns the collection of data processor instances used by this report provider.
     * 
     * @return the data processors
     */
    protected Collection<T> getProcessors()
    {
        // return the processors sorted by timer name
        final List<String> keys = processors.keys();
        Collections.sort(keys);
        
        final List<T> values = new ArrayList<>();
        for (String k : keys)
        {
            values.add(processors.get(k));
        }
        
        return Collections.unmodifiableCollection(values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(final ReportProvider other)
    {
        if (other instanceof AbstractDataProcessorBasedReportProvider)
        {
            @SuppressWarnings("unchecked")
            final AbstractDataProcessorBasedReportProvider<T> o = (AbstractDataProcessorBasedReportProvider<T>) other;
            merge(o);
        }
    }

    /**
     * Merges another {@link AbstractDataProcessorBasedReportProvider} into this instance.
     *
     * @param other
     *            the other provider to merge
     */
    public void merge(final AbstractDataProcessorBasedReportProvider<T> other)
    {
        if (other == null)
        {
            return;
        }

        for (final String name : other.processors.keys())
        {
            final T otherProcessor = other.processors.get(name);
            final T myProcessor = this.processors.get(name);
            if (myProcessor == null)
            {
                this.processors.put(name, otherProcessor);
            }
            else
            {
                myProcessor.merge(otherProcessor);
            }
        }
    }
}
