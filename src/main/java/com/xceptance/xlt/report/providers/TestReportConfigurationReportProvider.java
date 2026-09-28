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

import java.util.ArrayList;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.util.ReportUtils;

/**
 *
 */
public class TestReportConfigurationReportProvider extends AbstractReportProvider
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final TestReportConfigurationReport configReport = new TestReportConfigurationReport();

        // set the configured percentiles
        configReport.runtimePercentiles = new ArrayList<>();

        final double[] percentiles = ((ReportGeneratorConfiguration) getConfiguration()).getRuntimePercentiles();
        for (final double percentile : percentiles)
        {
            configReport.runtimePercentiles.add(ReportUtils.formatValue(percentile));
        }

        // add the configured runtime intervals used to segment the value range
        // (preprocessed for easier XSL transformation)
        final int[] boundaries = ((ReportGeneratorConfiguration) getConfiguration()).getRuntimeIntervalBoundaries();
        final ArrayList<RuntimeInterval> intervals = new ArrayList<RuntimeInterval>();

        if (boundaries.length > 0)
        {
            int lowerBoundary = 0;
            for (final int boundary : boundaries)
            {
                intervals.add(new RuntimeInterval("" + lowerBoundary, "" + boundary));
                lowerBoundary = boundary;
            }

            intervals.add(new RuntimeInterval("" + lowerBoundary, ""));
        }
        configReport.runtimeIntervals = intervals;
        
        configReport.requestTableColorization =  ((ReportGeneratorConfiguration) getConfiguration()).getRequestTableColorizations();

        //
        return configReport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        // Static configuration provider does not process runtime data records
    }

    /**
     * Declares that this provider does not process runtime data records.
     * Tells {@link com.xceptance.xlt.report.StatisticsProcessor} to omit this provider
     * from worker thread provider sets entirely, saving CPU and synchronization overhead.
     *
     * @return {@code false} always
     */
    @Override
    public boolean wantsDataRecords()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean acceptsType(final char typeCode)
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processAll(final com.xceptance.xlt.api.report.PostProcessedDataContainer dataContainer)
    {
        // No-op
    }
}
