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

    @Override
    public void processAll(final com.xceptance.xlt.api.report.PostProcessedDataContainer dataContainer)
    {
        final java.util.ArrayList<TransactionData> transactions = dataContainer.getTransactions();
        int size = transactions.size();
        for (int i = 0; i < size; i++)
        {
            super.processDataRecord(transactions.get(i));
        }

        final java.util.ArrayList<EventData> events = dataContainer.getEvents();
        size = events.size();
        for (int i = 0; i < size; i++)
        {
            final EventData event = events.get(i);
            final TransactionDataProcessor processor = getProcessor(event.getTestCaseName());
            processor.processDataRecord(event);
        }
    }
}
