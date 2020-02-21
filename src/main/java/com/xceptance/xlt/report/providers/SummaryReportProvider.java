package com.xceptance.xlt.report.providers;

import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.CustomData;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.PageLoadTimingData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.api.report.ReportProviderConfiguration;

/**
 */
public class SummaryReportProvider extends AbstractReportProvider
{
    private TransactionDataProcessor transactionDataProcessor;

    private ActionDataProcessor actionDataProcessor;

    private RequestDataProcessor requestDataProcessor;

    private PageLoadTimingDataProcessor pageLoadDataProcessor;

    private CustomDataProcessor customTimerDataProcessor;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfiguration(final ReportProviderConfiguration config)
    {
        super.setConfiguration(config);

        // HACK: must not create the data processors before the configuration is set
        transactionDataProcessor = new TransactionDataProcessor("All Transactions", this);
        actionDataProcessor = new ActionDataProcessor("All Actions", this);
        requestDataProcessor = new RequestDataProcessor("All Requests", this, false);
        pageLoadDataProcessor = new PageLoadTimingDataProcessor("All Page Load Timings", this);
        customTimerDataProcessor = new CustomDataProcessor("All Custom Timers", this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final SummaryReport report = new SummaryReport();

        report.transactions = (TransactionReport) transactionDataProcessor.createTimerReport(false);
        report.actions = (ActionReport) actionDataProcessor.createTimerReport(false);
        report.requests = (RequestReport) requestDataProcessor.createTimerReport(true);
        report.pageLoadTimings = (PageLoadTimingReport) pageLoadDataProcessor.createTimerReport(false);
        report.customTimers = (CustomTimerReport) customTimerDataProcessor.createTimerReport(false);

        return report;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof RequestData)
        {
            requestDataProcessor.processDataRecord(data);
        }
        else if (data instanceof ActionData)
        {
            actionDataProcessor.processDataRecord(data);
        }
        else if (data instanceof TransactionData || data instanceof EventData)
        {
            transactionDataProcessor.processDataRecord(data);
        }
        else if (data instanceof PageLoadTimingData)
        {
            pageLoadDataProcessor.processDataRecord(data);
        }
        else if (data instanceof CustomData)
        {
            customTimerDataProcessor.processDataRecord(data);
        }
    }
}
