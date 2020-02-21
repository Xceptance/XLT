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
