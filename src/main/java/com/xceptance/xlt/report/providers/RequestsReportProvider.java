/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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

    private void processTableColorizations(final List<TimerReport> requests, final ReportGeneratorConfiguration reportGeneratorConfig)
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
                    if (RegExUtils.isMatching(eachRequest.name, eachColorizationConfig.getPattern()))
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
                    if (RegExUtils.isMatching(eachRequest.name, defaultColorizationConfig.getPattern()))
                    {
                        resolvedColorizationConfig = defaultColorizationConfig;
                    }
                }

                if (resolvedColorizationConfig != null)
                {
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
}
