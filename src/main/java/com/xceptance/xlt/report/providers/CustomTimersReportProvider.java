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

import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.engine.Data;

/**
 * 
 */
public class CustomTimersReportProvider extends BasicTimerReportProvider<CustomDataProcessor>
{
    /**
     * Constructor.
     */
    public CustomTimersReportProvider()
    {
        super(CustomDataProcessor.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final CustomTimersReport report = new CustomTimersReport();

        report.customTimers = createTimerReports(false);

        return report;
    }

    /**
     * High-performance batch record processing override for custom timer metrics.
     * Iterates directly over the raw object array in {@link PostProcessedDataContainer#dataList},
     * avoiding virtual method dispatch and bounds checking for every record.
     *
     * @param dataContainer
     *            the container holding post-processed records for this chunk
     */
    @Override
    public void processAll(final com.xceptance.xlt.api.report.PostProcessedDataContainer dataContainer)
    {
        final com.xceptance.xlt.api.util.SimpleArrayList<Data> list = dataContainer.dataList;
        final Object[] array = list.getInternalArray();
        final int size = list.size();

        for (int p = 0; p < size; p++)
        {
            final Data data = (Data) array[p];
            if (data instanceof CustomData)
            {
                final CustomDataProcessor processor = getProcessor(data.getName());
                processor.processDataRecord(data);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof CustomData)
        {
            super.processDataRecord(data);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean acceptsType(final char typeCode)
    {
        // Custom timers aggregate CustomData ('C')
        return typeCode == 'C';
    }
}

