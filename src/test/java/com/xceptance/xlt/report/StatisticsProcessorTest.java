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

        // Verify min/max timestamps and getReportProviders()
        Assert.assertEquals(1000L, processor.getMinimumTime());
        Assert.assertTrue(processor.getMaximumTime() > 1000L);
        Assert.assertEquals(3, processor.getReportProviders().size());
        // Calling complete() again is a no-op
        processor.complete();
    }

    /**
     * Tests constructor variants, provider filtering (wantsDataRecords), configuration fallback when
     * configuration is null vs explicitly provided, empty completion, and direct min/max timestamp updates.
     */
    @Test
    public void testConstructorsConfigurationFallbacksAndMinMax()
    {
        // 1. Empty provider list with 1-arg constructor
        final StatisticsProcessor emptyProc = new StatisticsProcessor(new ArrayList<>());
        Assert.assertEquals(0L, emptyProc.getMinimumTime());
        Assert.assertEquals(0L, emptyProc.getMaximumTime());
        emptyProc.updateMinMax(500L, 1500L);
        emptyProc.updateMinMax(300L, 1200L);
        emptyProc.updateMinMax(700L, 2000L);
        Assert.assertEquals(300L, emptyProc.getMinimumTime());
        Assert.assertEquals(2000L, emptyProc.getMaximumTime());
        // Complete with no worker sets created
        emptyProc.complete();
        Assert.assertNull(emptyProc.threadLocalProviderSet);
        Assert.assertTrue(emptyProc.getReportProviders().isEmpty());

        // 2. 2-arg constructor where explicit configuration is passed, while master provider has null configuration!
        // Ensures this.configuration = configuration (line 152) and worker.setConfiguration(configuration) (line 182)
        // are strictly used rather than falling back to master.getConfiguration().
        final RequestsReportProvider unconfiguredMaster = new RequestsReportProvider();
        final List<ReportProvider> unconfiguredList = new ArrayList<>();
        unconfiguredList.add(unconfiguredMaster);
        final StatisticsProcessor procExplicitConfig = new StatisticsProcessor(unconfiguredList, config);
        final PostProcessedDataContainer cExplicit = new PostProcessedDataContainer(2, 1);
        cExplicit.typeCode = 'R';
        cExplicit.add(createRequest("ExplicitCfgReq", 1100L, 30, false));
        procExplicitConfig.process(cExplicit);
        procExplicitConfig.complete();
        unconfiguredMaster.setConfiguration(config);
        final RequestsReport explicitReport = (RequestsReport) unconfiguredMaster.createReportFragment();
        Assert.assertEquals(1, explicitReport.requests.size());
        Assert.assertEquals(1, explicitReport.requests.get(0).count);

        // 3. 1-arg constructor where first active provider is an AbstractReportProvider with configuration,
        // and another provider does not want data records (filtered out).
        final RequestsReportProvider reqProvider = new RequestsReportProvider();
        reqProvider.setConfiguration(config);
        final ReportProvider nonDataProvider = new CustomTestReportProvider(false, false, 'R');

        final List<ReportProvider> listWithAbstractFirst = new ArrayList<>();
        listWithAbstractFirst.add(nonDataProvider);
        listWithAbstractFirst.add(reqProvider);

        final StatisticsProcessor procFromAbstract = new StatisticsProcessor(listWithAbstractFirst);
        final PostProcessedDataContainer cAbstract = new PostProcessedDataContainer(2, 1);
        cAbstract.typeCode = 'R';
        cAbstract.add(createRequest("AbstractCfgReq", 1200L, 35, false));
        procFromAbstract.process(cAbstract);
        Assert.assertEquals(1, procFromAbstract.getReportProviders().size());
        final RequestsReport abstractReport = (RequestsReport) reqProvider.createReportFragment();
        Assert.assertEquals(1, abstractReport.requests.size());
        Assert.assertEquals(1, abstractReport.requests.get(0).count);

        // 4. 1-arg constructor where first active provider is NOT an AbstractReportProvider (so this.configuration == null),
        // followed by an AbstractReportProvider (exercises createWorkerProviderSet's else-if branch at line 184).
        final CustomTestReportProvider customFirst = new CustomTestReportProvider(true, false, 'R');
        final RequestsReportProvider reqSecond = new RequestsReportProvider();
        reqSecond.setConfiguration(config);

        final List<ReportProvider> mixedList = new ArrayList<>();
        mixedList.add(customFirst);
        mixedList.add(reqSecond);

        final StatisticsProcessor procMixed = new StatisticsProcessor(mixedList);
        final PostProcessedDataContainer container = new PostProcessedDataContainer(2, 1);
        container.typeCode = 'R';
        container.add(createRequest("R1", 1234L, 40, false));
        procMixed.process(container);

        // Capture the created WorkerProviderSet before complete() clears allWorkerProviderSets
        Assert.assertEquals(1, procMixed.allWorkerProviderSets.size());
        final StatisticsProcessor.WorkerProviderSet workerSet = procMixed.allWorkerProviderSets.peek();
        Assert.assertNotNull(workerSet);
        Assert.assertNotNull(workerSet.providersByTypeCode['R']);

        Assert.assertEquals(1234L, procMixed.getMinimumTime());
        Assert.assertEquals(1234L, procMixed.getMaximumTime());
        Assert.assertEquals(2, procMixed.getReportProviderCount());

        final java.util.concurrent.atomic.AtomicInteger callbackCount = new java.util.concurrent.atomic.AtomicInteger();
        procMixed.complete(callbackCount::incrementAndGet);
        Assert.assertEquals(2, callbackCount.get());
        Assert.assertEquals(2, procMixed.getReportProviders().size());

        // Verify complete() cleared allWorkerProviderSets, nulled out routing tables, and detached worker providers
        Assert.assertTrue(procMixed.allWorkerProviderSets.isEmpty());
        for (int c = 0; c < 128; c++)
        {
            Assert.assertNull(workerSet.providersByTypeCode[c]);
        }
        for (final ReportProvider detached : workerSet.allProviders)
        {
            Assert.assertNull(detached);
        }

        // Verify both customFirst and reqSecond actually processed and merged the record
        Assert.assertEquals(1, customFirst.mergedCount);
        Assert.assertEquals(1, customFirst.processAllCalls);
        Assert.assertEquals(1, customFirst.processedCount);
        final RequestsReport mixedReport = (RequestsReport) reqSecond.createReportFragment();
        Assert.assertEquals(1, mixedReport.requests.size());
        Assert.assertEquals(1, mixedReport.requests.get(0).count);

        // Verify calling complete() a second time is guarded by completed.compareAndSet(false, true)
        // while still advancing the optional progress callback for each provider
        procMixed.allWorkerProviderSets.add(workerSet);
        final java.util.concurrent.atomic.AtomicInteger secondCallbackCount = new java.util.concurrent.atomic.AtomicInteger();
        procMixed.complete(secondCallbackCount::incrementAndGet);
        Assert.assertEquals(2, secondCallbackCount.get());
        procMixed.complete();
        Assert.assertEquals("Second complete() call must be a no-op", 1, procMixed.allWorkerProviderSets.size());
    }

    /**
     * Verifies that a provider class lacking a no-arg constructor causes worker instantiation
     * to log and throw a RuntimeException.
     */
    @Test(expected = RuntimeException.class)
    public void testWorkerProviderInstantiationFailure()
    {
        // Anonymous inner class has no zero-argument constructor for reflection
        final ReportProvider uninstantiableProvider = new CustomTestReportProvider(true, false, 'R')
        {
        };

        final List<ReportProvider> providers = new ArrayList<>();
        providers.add(uninstantiableProvider);

        final StatisticsProcessor processor = new StatisticsProcessor(providers, config);
        final PostProcessedDataContainer container = new PostProcessedDataContainer(1, 1);
        container.add(createRequest("Fail", 1000L, 10, false));
        processor.process(container);
    }

    /**
     * Tests typed and untyped processing, empty containers, ASCII type code boundaries (0, 1, 127, 128),
     * worker provider exception handling, and dropped-lines sampling compensation.
     */
    @Test
    public void testProcessTypedUntypedExceptionsAndDroppedLines()
    {
        final RequestsReportProvider reqProvider = new RequestsReportProvider();
        reqProvider.setConfiguration(config);
        final ThrowingReportProvider throwingProvider = new ThrowingReportProvider();
        final CustomTestReportProvider rOnlyCustomProvider = new CustomTestReportProvider(true, false, 'R');

        final List<ReportProvider> providers = new ArrayList<>();
        providers.add(reqProvider);
        providers.add(throwingProvider);
        providers.add(rOnlyCustomProvider);

        final StatisticsProcessor processor = new StatisticsProcessor(providers, config);

        // 1. Empty container is ignored completely
        final PostProcessedDataContainer emptyContainer = new PostProcessedDataContainer(2, 1);
        processor.process(emptyContainer, 'R');
        Assert.assertEquals(0L, processor.getMinimumTime());

        // 2. Typed container ('R') dispatches via processAll and catches Throwable from ThrowingReportProvider
        final PostProcessedDataContainer typedContainer = new PostProcessedDataContainer(2, 1);
        typedContainer.typeCode = 'R';
        typedContainer.add(createRequest("TypedReq", 2000L, 30, false));
        processor.process(typedContainer);
        Assert.assertEquals(2000L, processor.getMinimumTime());

        // Boundary typeCode = 1 and typeCode = 127 (valid ASCII fast-path where rOnlyCustomProvider does NOT accept 1 or 127)
        final PostProcessedDataContainer boundary1Container = new PostProcessedDataContainer(2, 1);
        boundary1Container.add(createTransaction("Boundary1Txn", 2010L, 10, false));
        processor.process(boundary1Container, (char) 1);
        processor.process(boundary1Container, (char) 127);

        // 3. Untyped container (typeCode == 0) with sampleFactor = 2 and droppedLines = 0
        // (verifies droppedLines > 0 boundary does not trigger when droppedLines == 0 even if sampleFactor > 1),
        // and exercises catch blocks for both relevantProviders (tc < 128) and all providers (tc >= 128)
        final PostProcessedDataContainer errorContainer = new PostProcessedDataContainer(4, 2);
        errorContainer.droppedLines = 0;
        errorContainer.add(createRequest("ThrowReq", 2050L, 15, false));
        errorContainer.add(createTransaction("UntypedTxnNotAcceptedByR", 2055L, 15, false));
        errorContainer.add(new NonAsciiData("ThrowNonAscii", 2060L));
        processor.process(errorContainer, (char) 0);

        // 4. Container with exact boundary typeCode == 128 falls back to per-record routing.
        // Include:
        // - Item 0: TransactionData (skipped during droppedLines compensation)
        // - Item 1: RequestData ("SampledReq1") -> sampled (droppedLines: 3 -> 2)
        // - Item 2: RequestData ("SampledReq2") -> sampled (droppedLines: 2 -> 1)
        // - Item 3: NonAsciiData (tc == 128, >= 128) -> sampled via 'all' (droppedLines: 1 -> 0, triggers break!)
        // - Item 4: RequestData ("UnreachedInSampling") -> NOT sampled because loop broke at droppedLines == 0
        final PostProcessedDataContainer fallbackContainer = new PostProcessedDataContainer(8, 3);
        fallbackContainer.droppedLines = 3;
        fallbackContainer.add(createTransaction("TxnSkippedInSampling", 2100L, 100, false));
        fallbackContainer.add(createRequest("SampledReq1", 2200L, 40, false));
        fallbackContainer.add(createRequest("SampledReq2", 2250L, 45, false));
        fallbackContainer.add(new NonAsciiData("NonAscii", 2300L));
        fallbackContainer.add(createRequest("UnreachedInSampling", 2400L, 50, false));
        processor.process(fallbackContainer, (char) 128);

        // 5. Also exercise droppedLines when droppedLines exceeds non-transaction count (loop finishes naturally),
        // including a TransactionData record so relevantProviders for 'T' is exercised without matching rOnlyCustomProvider.
        final PostProcessedDataContainer fullLoopSampling = new PostProcessedDataContainer(4, 2);
        fullLoopSampling.droppedLines = 5;
        fullLoopSampling.add(createRequest("SampledReq3", 2500L, 20, false));
        fullLoopSampling.add(new com.xceptance.xlt.api.engine.ActionData("ActionNotAcceptedByR")
        {
            {
                setTime(2550L);
            }
        });
        processor.process(fullLoopSampling, (char) 0);

        processor.complete();

        final RequestsReport report = (RequestsReport) reqProvider.createReportFragment();
        int totalCount = 0;
        for (final TimerReport tr : report.requests)
        {
            totalCount += tr.count;
        }
        // TypedReq (1) + ThrowReq (1) + SampledReq1 (1 + 2) + SampledReq2 (1 + 2) + UnreachedInSampling (1) + SampledReq3 (1 + 1) = 11
        Assert.assertEquals(11, totalCount);

        // Verify rOnlyCustomProvider counts:
        // - processAllCalls: 1 (from typedContainer 'R'; boundary 1 and 127 were not accepted by 'R')
        // - processedCount:
        //   * typedContainer: 1 ('R')
        //   * errorContainer: 1 (ThrowReq 'R') + 1 (ThrowNonAscii tc=128 via 'all') = 2 (UntypedTxn 'T' was filtered out!)
        //   * fallbackContainer normal loop: 3 ('R' records) + 1 (NonAscii tc=128 via 'all') = 4
        //   * fallbackContainer droppedLines loop (sampleFactor=3 -> +2 each): 2*2 ('R' records) + 2 (NonAscii via 'all') = 6
        //   * fullLoopSampling: 1 + 1 (SampledReq3 'R'; ActionNotAcceptedByR 'A' was filtered out!) = 2
        //   Total processedCount = 1 + 2 + 4 + 6 + 2 = 15
        Assert.assertEquals(1, rOnlyCustomProvider.processAllCalls);
        Assert.assertEquals(15, rOnlyCustomProvider.processedCount);
    }

    /**
     * Public static test provider with a no-arg constructor for testing worker instantiation without AbstractReportProvider.
     */
    public static class CustomTestReportProvider implements ReportProvider
    {
        private final boolean wantsRecords;
        private final boolean throwOnProcess;
        private final char acceptedType;
        int processedCount;
        int processAllCalls;
        int mergedCount;

        public CustomTestReportProvider()
        {
            this(true, false, 'R');
        }

        CustomTestReportProvider(final boolean wantsRecords, final boolean throwOnProcess, final char acceptedType)
        {
            this.wantsRecords = wantsRecords;
            this.throwOnProcess = throwOnProcess;
            this.acceptedType = acceptedType;
        }

        @Override
        public boolean acceptsType(final char typeCode)
        {
            return typeCode == acceptedType;
        }

        @Override
        public Object createReportFragment()
        {
            return null;
        }

        @Override
        public void processDataRecord(final com.xceptance.xlt.api.engine.Data data)
        {
            if (throwOnProcess && data.getName() != null && data.getName().startsWith("Throw"))
            {
                throw new RuntimeException("Simulated worker failure");
            }
            processedCount++;
        }

        @Override
        public void processAll(final PostProcessedDataContainer dataContainer)
        {
            processAllCalls++;
            for (int i = 0; i < dataContainer.data.size(); i++)
            {
                processDataRecord(dataContainer.data.get(i));
            }
        }

        @Override
        public boolean lock()
        {
            return true;
        }

        @Override
        public void unlock()
        {
        }

        @Override
        public void setConfiguration(final com.xceptance.xlt.api.report.ReportProviderConfiguration config)
        {
        }

        @Override
        public boolean wantsDataRecords()
        {
            return wantsRecords;
        }

        @Override
        public void merge(final ReportProvider other)
        {
            mergedCount++;
            if (other instanceof CustomTestReportProvider)
            {
                final CustomTestReportProvider o = (CustomTestReportProvider) other;
                this.processedCount += o.processedCount;
                this.processAllCalls += o.processAllCalls;
            }
        }
    }

    /**
     * Public static test provider that throws on both batch and single-record processing to test catch blocks.
     */
    public static class ThrowingReportProvider extends CustomTestReportProvider
    {
        public ThrowingReportProvider()
        {
            super(true, true, 'R');
        }

        @Override
        public void processAll(final PostProcessedDataContainer dataContainer)
        {
            throw new IllegalStateException("Simulated batch failure");
        }
    }

    /**
     * Data record returning the exact boundary non-ASCII type code (128) to exercise fallback routing in StatisticsProcessor.
     */
    private static final class NonAsciiData extends com.xceptance.xlt.api.engine.AbstractData
    {
        NonAsciiData(final String name, final long time)
        {
            super(name, (char) 128);
            setTime(time);
        }
    }
}
