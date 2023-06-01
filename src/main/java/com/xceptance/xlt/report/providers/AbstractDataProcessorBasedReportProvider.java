/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
     * Returns the data processor responsible for timers with the given name.
     * 
     * @param name
     *            the timer name
     * @return the data processor
     */
    protected T getProcessor(final String name)
    {
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
                throw new RuntimeException("", ex);
            }

            processors.put(name, processor);
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
}
