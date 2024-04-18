/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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

import java.io.File;

import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;

/**
 * The {@link CustomDataProcessor} class provides common functionality of a typical data processor that deals with
 * {@link CustomData} objects. This includes:
 * <ul>
 * <li>calculation of response time statistics</li>
 * <li>creation of response time charts</li>
 * <li>creation of CSV files with all the data</li>
 * </ul>
 */
public class CustomDataProcessor extends BasicTimerDataProcessor
{
    /**
     * Constructor.
     * 
     * @param name
     *            the timer name
     * @param provider
     *            the provider that owns this data processor
     */
    public <T extends AbstractDataProcessor> CustomDataProcessor(final String name, final AbstractReportProvider provider)
    {
        super(name, provider);

        setChartDir(new File(getChartDir(), "custom"));
        setCsvDir(new File(getCsvDir(), "custom"));

        // set capping parameters
        final ReportGeneratorConfiguration config = (ReportGeneratorConfiguration) getConfiguration();
        setChartCappingInfo(config.getCustomChartCappingInfo());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TimerReport createTimerReport()
    {
        return new CustomTimerReport();
    }
}
