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
