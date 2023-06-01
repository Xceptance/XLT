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

import java.util.ArrayList;
import java.util.List;

import com.xceptance.xlt.api.engine.TimerData;

/**
 * The BasicTimerReportProvider class provides common functionality of a typical report provider that deals with
 * {@link TimerData} objects. This includes:
 * <ul>
 * <li>calculation of response time statistics</li>
 * <li>creation of response time charts</li>
 * <li>creation of CSV files with all the data</li>
 * </ul>
 */
public abstract class BasicTimerReportProvider<T extends BasicTimerDataProcessor> extends AbstractDataProcessorBasedReportProvider<T>
{
    /**
     * Constructor.
     * 
     * @param c
     *            the data processor implementation class
     */
    protected BasicTimerReportProvider(final Class<T> c)
    {
        super(c);
    }

    /**
     * Generates a runtime timer report for each distinct timer name that appears in the list. While a runtime timer
     * report is created, a corresponding chart and a CSV file with the values are created as well.
     * 
     * @param generateHistograms
     *            whether histogram charts are to be generated
     * @return the list of timer reports
     */
    protected List<TimerReport> createTimerReports(final boolean generateHistograms)
    {
        final List<TimerReport> reports = new ArrayList<TimerReport>();

        for (final T processor : getProcessors())
        {
            final TimerReport timerReport = processor.createTimerReport(generateHistograms);

            if (timerReport != null)
            {
                reports.add(timerReport);
            }
        }

        return reports;
    }
}
