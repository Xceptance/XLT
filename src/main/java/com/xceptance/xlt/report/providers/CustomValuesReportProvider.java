/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
}
