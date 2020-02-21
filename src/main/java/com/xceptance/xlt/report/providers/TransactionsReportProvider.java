package com.xceptance.xlt.report.providers;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.TransactionData;

/**
 * 
 */
public class TransactionsReportProvider extends BasicTimerReportProvider<TransactionDataProcessor>
{
    /**
     * Constructor.
     */
    public TransactionsReportProvider()
    {
        super(TransactionDataProcessor.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final TransactionsReport report = new TransactionsReport();

        report.transactions = createTimerReports(false);

        return report;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof TransactionData)
        {
            super.processDataRecord(data);
        }
        else if (data instanceof EventData)
        {
            final TransactionDataProcessor processor = getProcessor(((EventData) data).getTestCaseName());
            processor.processDataRecord(data);
        }
    }
}
