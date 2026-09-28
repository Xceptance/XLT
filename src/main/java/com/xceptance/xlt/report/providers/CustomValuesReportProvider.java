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

import com.xceptance.xlt.api.engine.CustomValue;
import com.xceptance.xlt.api.engine.Data;

/**
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class CustomValuesReportProvider extends AbstractDataProcessorBasedReportProvider<CustomValueProcessor>
{
    /**
     * Constructor.
     */
    public CustomValuesReportProvider()
    {
        super(CustomValueProcessor.class);
    }

    /**
     * High-performance batch record processing override for custom values.
     * Iterates directly over the raw object array in {@link PostProcessedDataContainer#dataList},
     * avoiding virtual method dispatch and bounds checking for every record.
     *
     * @param dataContainer
     *            the container holding post-processed records for this chunk
     */
    @Override
    public void processAll(final com.xceptance.xlt.api.report.PostProcessedDataContainer dataContainer)
    {
        if (dataContainer.typeCode != 'V')
        {
            super.processAll(dataContainer);
            return;
        }

        final com.xceptance.xlt.api.util.SimpleArrayList<Data> list = dataContainer.dataList;
        final Object[] array = list.getInternalArray();
        final int size = list.size();

        String lastName = null;
        CustomValueProcessor lastProcessor = null;

        for (int p = 0; p < size; p++)
        {
            final CustomValue data = (CustomValue) array[p];
            final String name = data.getName();

            // Local register cache for sequential runs of the same custom value metric name
            CustomValueProcessor processor = lastProcessor;
            if (name != lastName || processor == null)
            {
                processor = getProcessor(name);
                lastName = name;
                lastProcessor = processor;
            }
            processor.processDataRecord(data);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof CustomValue)
        {
            super.processDataRecord(data);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final CustomValueReports reports = new CustomValueReports();

        for (final CustomValueProcessor processor : getProcessors())
        {
            final CustomValueReport customValueReport = processor.createReportFragment();

            reports.customValueReports.add(customValueReport);
        }

        return reports;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Custom values ({@link CustomValue}) are scalar double measurements serialized exclusively
     * in double-value chunks ('V'). Custom stopwatch timers ({@link com.xceptance.xlt.api.engine.CustomData})
     * are stored in timer chunks ('C') and handled separately by {@link CustomTimersReportProvider}.
     */
    @Override
    public boolean acceptsType(final char typeCode)
    {
        return typeCode == 'V';
    }
}

