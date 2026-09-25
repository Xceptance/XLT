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
package com.xceptance.xlt.report;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.report.PostProcessedDataContainer;
import com.xceptance.xlt.api.report.ReportProvider;
import com.xceptance.xlt.report.providers.DummyReportGeneratorConfiguration;
import com.xceptance.xlt.report.providers.GeneralReport;
import com.xceptance.xlt.report.providers.GeneralReportProvider;
import com.xceptance.xlt.report.providers.RequestsReport;
import com.xceptance.xlt.report.providers.RequestsReportProvider;
import com.xceptance.xlt.report.providers.TimerReport;
import com.xceptance.xlt.report.providers.TransactionsReport;
import com.xceptance.xlt.report.providers.TransactionsReportProvider;

/**
 * Tests concurrent execution and merging in StatisticsProcessor.
 */
public class StatisticsProcessorTest
{
    private DummyReportGeneratorConfiguration config;

    @Before
    public void setUp()
    {
        config = DummyReportGeneratorConfiguration.getDefault();
    }

    private RequestData createRequest(final String name, final long time, final int runTime, final boolean failed)
    {
        final RequestData req = new RequestData(name);
        req.setTime(time);
        req.setRunTime(runTime);
        req.setFailed(failed);
        req.setBytesReceived(500);
        req.setBytesSent(100);
        req.setResponseCode(200);
        req.setUrl("https://example.com/test");
        return req;
    }

    private TransactionData createTransaction(final String name, final long time, final int runTime, final boolean failed)
    {
        final TransactionData txn = new TransactionData(name);
        txn.setTime(time);
        txn.setRunTime(runTime);
        txn.setFailed(failed);
        return txn;
    }

    @Test
    public void testConcurrentProcessingAndMerge() throws Exception
    {
        final RequestsReportProvider reqProvider = new RequestsReportProvider();
        reqProvider.setConfiguration(config);

        final TransactionsReportProvider txnProvider = new TransactionsReportProvider();
        txnProvider.setConfiguration(config);

        final GeneralReportProvider genProvider = new GeneralReportProvider();
        genProvider.setConfiguration(config);

        final List<ReportProvider> providers = new ArrayList<>();
        providers.add(reqProvider);
        providers.add(txnProvider);
        providers.add(genProvider);

        final StatisticsProcessor processor = new StatisticsProcessor(providers, config);

        final int threadCount = 8;
        final int batchesPerThread = 20;
        final int recordsPerBatch = 50;
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++)
        {
            final int threadId = t;
            executor.execute(() -> {
                try
                {
                    startLatch.await();

                    for (int b = 0; b < batchesPerThread; b++)
                    {
                        final PostProcessedDataContainer container = new PostProcessedDataContainer(recordsPerBatch * 2, 1);
                        for (int r = 0; r < recordsPerBatch; r++)
                        {
                            final long time = 1000L + (threadId * 10000L) + (b * 100L) + r;
                            final RequestData req = createRequest("Req_" + (r % 5), time, 50 + r, (r % 10 == 0));
                            container.add(req);

                            final TransactionData txn = createTransaction("Txn_" + (r % 3), time, 200 + r, (r % 15 == 0));
                            container.add(txn);
                        }
                        processor.process(container);
                    }
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        Assert.assertTrue("Timed out waiting for threads", doneLatch.await(30, TimeUnit.SECONDS));
        executor.shutdown();

        // Complete processing and merge worker statistics
        processor.complete();

        // Verify Requests
        final RequestsReport reqReport = (RequestsReport) reqProvider.createReportFragment();
        final int totalRequests = threadCount * batchesPerThread * recordsPerBatch;
        int sumCount = 0;
        for (final TimerReport tr : reqReport.requests)
        {
            sumCount += tr.count;
        }
        Assert.assertEquals("Total request count mismatch", totalRequests, sumCount);

        // Verify Transactions
        final TransactionsReport txnReport = (TransactionsReport) txnProvider.createReportFragment();
        final int totalTxns = threadCount * batchesPerThread * recordsPerBatch;
        int sumTxnCount = 0;
        for (final TimerReport tr : txnReport.transactions)
        {
            sumTxnCount += tr.count;
        }
        Assert.assertEquals("Total transaction count mismatch", totalTxns, sumTxnCount);

        // Verify General Report
        final GeneralReport genReport = (GeneralReport) genProvider.createReportFragment();
        Assert.assertEquals("Total hits mismatch", totalRequests, genReport.hits);
        Assert.assertEquals("Total bytes received mismatch", totalRequests * 500L, genReport.bytesReceived);
        Assert.assertEquals("Total bytes sent mismatch", totalRequests * 100L, genReport.bytesSent);
    }
}
