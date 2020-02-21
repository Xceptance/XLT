package com.xceptance.xlt.report.providers;

import java.io.File;

import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.util.Apdex;
import com.xceptance.xlt.report.util.ApdexCalculator;

/**
 * The {@link ActionDataProcessor} class provides common functionality of a typical data processor that deals with
 * {@link ActionData} objects. This includes:
 * <ul>
 * <li>calculation of response time statistics</li>
 * <li>creation of response time charts</li>
 * </ul>
 */
public class ActionDataProcessor extends BasicTimerDataProcessor
{
    /**
     * The Apdex calculator.
     */
    private final ApdexCalculator apdexCalculator;

    /**
     * Constructor.
     *
     * @param name
     *            the timer name
     * @param provider
     *            the provider that owns this data processor
     */
    public ActionDataProcessor(final String name, final AbstractReportProvider provider)
    {
        super(name, provider);

        setChartDir(new File(getChartDir(), "actions"));
        setCsvDir(new File(getCsvDir(), "actions"));

        // set capping parameters
        final ReportGeneratorConfiguration config = (ReportGeneratorConfiguration) getConfiguration();
        setChartCappingInfo(config.getActionChartCappingInfo());

        // apdex
        final double threshold = config.getApdexThresholdForAction(getName());
        apdexCalculator = new ApdexCalculator(threshold);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data stat)
    {
        super.processDataRecord(stat);

        final ActionData data = (ActionData) stat;

        // apdex
        apdexCalculator.addSample(data.getRunTime(), data.hasFailed());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimerReport createTimerReport(final boolean generateHistogram)
    {
        final ActionReport timerReport = (ActionReport) super.createTimerReport(generateHistogram);

        // apdex info
        final Apdex apdex = apdexCalculator.getApdex();

        timerReport.apdex.value = apdex.getValue();
        timerReport.apdex.longValue = apdex.getLongValue();

        return timerReport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TimerReport createTimerReport()
    {
        return new ActionReport();
    }
}
