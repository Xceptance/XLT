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
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.report.ReportProviderConfiguration;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;

/**
 * 
 */
public class RequestsReportProvider extends BasicTimerReportProvider<RequestDataProcessor>
{
    /**
     * Class logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RequestsReportProvider.class);

    /**
     * Constructor.
     */
    public RequestsReportProvider()
    {
        super(RequestDataProcessor.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final RequestsReport report = new RequestsReport();

        report.requests = createTimerReports(true);

        final ReportProviderConfiguration configuration = getConfiguration();
        if (configuration instanceof ReportGeneratorConfiguration)
        {
            processTableColorizations(report.requests, (ReportGeneratorConfiguration) configuration);
        }

        return report;
    }

    static void processTableColorizations(final List<TimerReport> requests, final ReportGeneratorConfiguration reportGeneratorConfig)
    {
        final String defaultGroupName = reportGeneratorConfig.getRequestTableColorizationDefaultGroupName();
        final List<RequestTableColorization> colorizationConfigs = new ArrayList<>(reportGeneratorConfig.getRequestTableColorizations());

        RequestTableColorization defaultColorizationConfig = null;
        for (final TimerReport eachRequest : requests)
        {
            RequestTableColorization resolvedColorizationConfig = null;
            RequestTableColorization multipleMatch = null;
            for (final RequestTableColorization eachColorizationConfig : colorizationConfigs)
            {
                if (defaultGroupName.equals(eachColorizationConfig.getGroupName()))
                {
                    if (defaultColorizationConfig == null)
                    {
                        defaultColorizationConfig = eachColorizationConfig;
                    }
                }
                else
                {
                    if (RegExUtils.isMatching(eachRequest.name, eachColorizationConfig.getNamePattern()) &&
                        RegExUtils.isMatching(Optional.ofNullable(eachRequest.labels).orElse(""), eachColorizationConfig.getLabelPattern()))
                    {
                        if (resolvedColorizationConfig != null)
                        {
                            multipleMatch = eachColorizationConfig;
                            break;
                        }
                        else
                        {
                            resolvedColorizationConfig = eachColorizationConfig;
                        }
                    }
                }
            }

            if (multipleMatch == null)
            {
                if (resolvedColorizationConfig == null && defaultColorizationConfig != null)
                {
                    // no group matches the request, but a "default" group is defined
                    eachRequest.colorizationGroupName = defaultColorizationConfig.getGroupName();
                }

                if (resolvedColorizationConfig != null)
                {
                    // exactly one group matches the request
                    eachRequest.colorizationGroupName = resolvedColorizationConfig.getGroupName();
                }
            }
            else
            {
                LOG.warn("Skipping request table colorization rule. Found multiple matching rules for \"" + eachRequest.name +
                         "\" and rules [" + multipleMatch.getGroupName() + ", " + resolvedColorizationConfig.getGroupName() + "]");
            }

        }
    }

    /**
     * High-performance batch record processing override for HTTP requests.
     * <p>
     * <b>Performance Optimizations:</b>
     * <ul>
     *   <li>Retrieves raw internal object array directly from {@link PostProcessedDataContainer#dataList}
     *       to avoid bounds checks and iterator allocation.</li>
     *   <li>Maintains local register variables {@code lastName} and {@code lastProcessor}. Since HTTP
     *       requests within a chunk often share identical timer names sequentially (e.g., repeating calls
     *       to the same endpoint), the processor lookup is completely bypassed on matching pointer equality.</li>
     *   <li>Invokes strongly typed {@link RequestDataProcessor#processDataRecord(RequestData)}, eliminating
     *       generic {@code (Data)} and {@code (TimerData)} upcasting and downcasting inside the tight loop.</li>
     * </ul>
     *
     * @param dataContainer
     *            the container holding post-processed records for this chunk
     */
    @Override
    public void processAll(final com.xceptance.xlt.api.report.PostProcessedDataContainer dataContainer)
    {
        if (dataContainer.typeCode != 'R')
        {
            super.processAll(dataContainer);
            return;
        }

        final com.xceptance.xlt.api.util.SimpleArrayList<Data> list = dataContainer.dataList;
        final Object[] array = list.getInternalArray();
        final int size = list.size();

        String lastName = null;
        RequestDataProcessor lastProcessor = null;

        for (int p = 0; p < size; p++)
        {
            final RequestData stat = (RequestData) array[p];
            final String name = stat.getName();
            RequestDataProcessor processor = lastProcessor;

            // Fast-path pointer equality check before invoking provider cache
            if (name != lastName || processor == null)
            {
                processor = getProcessor(name);
                lastName = name;
                lastProcessor = processor;
            }

            // Direct invocation of strongly typed RequestData accumulator
            processor.processDataRecord(stat);
        }

        // Compensate for sampling loss if lines were dropped
        int droppedLines = dataContainer.droppedLines;
        final int sampleFactor = dataContainer.sampleFactor;
        if (droppedLines > 0)
        {
            for (int i = 0; i < size; i++)
            {
                final RequestData stat = (RequestData) array[i];
                final String name = stat.getName();
                RequestDataProcessor processor = lastProcessor;
                if (name != lastName || processor == null)
                {
                    processor = getProcessor(name);
                    lastName = name;
                    lastProcessor = processor;
                }
                for (int y = 1; y < sampleFactor; y++)
                {
                    processor.processDataRecord(stat);
                }
                droppedLines--;
                if (droppedLines == 0)
                {
                    break;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof RequestData)
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
        // Requests report provider only aggregates HTTP Request data records ('R')
        return typeCode == 'R';
    }
}

