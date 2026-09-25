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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TransactionData;

/**
 * Tests the merge behavior of data processors.
 */
public class DataProcessorMergeTest
{
    private DummyReportGeneratorConfiguration config;
    private RequestsReportProvider requestsProvider;
    private TransactionsReportProvider transactionsProvider;
    private ActionsReportProvider actionsProvider;

    @Before
    public void setUp()
    {
        config = DummyReportGeneratorConfiguration.getDefault();

        requestsProvider = new RequestsReportProvider();
        requestsProvider.setConfiguration(config);

        transactionsProvider = new TransactionsReportProvider();
        transactionsProvider.setConfiguration(config);

        actionsProvider = new ActionsReportProvider();
        actionsProvider.setConfiguration(config);
    }

    private RequestData createRequest(final String name, final long time, final int runTime, final boolean failed,
                                     final int responseCode, final int bytesReceived, final String url)
    {
        final RequestData req = new RequestData(name);
        req.setTime(time);
        req.setRunTime(runTime);
        req.setFailed(failed);
        req.setResponseCode(responseCode);
        req.setBytesReceived(bytesReceived);
        req.setBytesSent(64);
        req.setUrl(url);
        req.setConnectTime(10);
        req.setSendTime(5);
        req.setServerBusyTime(15);
        req.setReceiveTime(20);
        req.setTimeToFirstBytes(30);
        req.setTimeToLastBytes(runTime);
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

    private ActionData createAction(final String name, final long time, final int runTime, final boolean failed)
    {
        final ActionData act = new ActionData(name);
        act.setTime(time);
        act.setRunTime(runTime);
        act.setFailed(failed);
        return act;
    }

    @Test
    public void testRequestDataProcessorMerge()
    {
        final RequestDataProcessor p1 = new RequestDataProcessor("Req1", requestsProvider, true);
        final RequestDataProcessor p2 = new RequestDataProcessor("Req1", requestsProvider, true);
        final RequestDataProcessor pExpected = new RequestDataProcessor("Req1", requestsProvider, true);

        // Feed batch 1 to p1
        for (int i = 0; i < 50; i++)
        {
            final RequestData req = createRequest("Req1", 1000L + i * 100, 100 + i * 5, i % 10 == 0,
                                                  i % 10 == 0 ? 500 : 200, 1024 + i * 10, "http://host/path" + (i % 20));
            p1.processDataRecord(req);
            pExpected.processDataRecord(req);
        }

        // Feed batch 2 to p2
        for (int i = 50; i < 120; i++)
        {
            final RequestData req = createRequest("Req1", 1000L + i * 100, 100 + i * 5, i % 7 == 0,
                                                  i % 7 == 0 ? 503 : 200, 1024 + i * 10, "http://host/path" + (i % 30));
            p2.processDataRecord(req);
            pExpected.processDataRecord(req);
        }

        // Merge p2 into p1
        p1.merge(p2);

        final RequestReport rActual = (RequestReport) p1.createTimerReport(true);
        final RequestReport rExpected = (RequestReport) pExpected.createTimerReport(true);

        Assert.assertEquals("Count mismatch", rExpected.count, rActual.count);
        Assert.assertEquals("Errors mismatch", rExpected.errors, rActual.errors);
        Assert.assertEquals("Min runtime mismatch", rExpected.min, rActual.min);
        Assert.assertEquals("Max runtime mismatch", rExpected.max, rActual.max);
        Assert.assertEquals("Mean runtime mismatch", rExpected.mean.doubleValue(), rActual.mean.doubleValue(), 0.001);
        Assert.assertEquals("Bytes sent totalCount mismatch", rExpected.bytesSent.totalCount, rActual.bytesSent.totalCount);
        Assert.assertEquals("Bytes received totalCount mismatch", rExpected.bytesReceived.totalCount, rActual.bytesReceived.totalCount);

        // Check connect time stats
        Assert.assertEquals("Connect time min mismatch", rExpected.connectTime.min, rActual.connectTime.min);
        Assert.assertEquals("Connect time mean mismatch", rExpected.connectTime.mean.doubleValue(), rActual.connectTime.mean.doubleValue(), 0.001);

        // Check distinct URLs count
        Assert.assertEquals("Distinct URLs mismatch", rExpected.urls.total, rActual.urls.total);
    }

    @Test
    public void testTransactionDataProcessorMerge()
    {
        final TransactionDataProcessor p1 = new TransactionDataProcessor("Txn1", transactionsProvider);
        final TransactionDataProcessor p2 = new TransactionDataProcessor("Txn1", transactionsProvider);
        final TransactionDataProcessor pExpected = new TransactionDataProcessor("Txn1", transactionsProvider);

        for (int i = 0; i < 40; i++)
        {
            final TransactionData txn = createTransaction("Txn1", 5000L + i * 200, 500 + i * 10, i % 5 == 0);
            p1.processDataRecord(txn);
            pExpected.processDataRecord(txn);
        }

        for (int i = 40; i < 100; i++)
        {
            final TransactionData txn = createTransaction("Txn1", 5000L + i * 200, 500 + i * 10, i % 8 == 0);
            p2.processDataRecord(txn);
            pExpected.processDataRecord(txn);
        }

        p1.merge(p2);

        final TransactionReport rActual = (TransactionReport) p1.createTimerReport(false);
        final TransactionReport rExpected = (TransactionReport) pExpected.createTimerReport(false);

        Assert.assertEquals("Count mismatch", rExpected.count, rActual.count);
        Assert.assertEquals("Errors mismatch", rExpected.errors, rActual.errors);
        Assert.assertEquals("Min runtime mismatch", rExpected.min, rActual.min);
        Assert.assertEquals("Max runtime mismatch", rExpected.max, rActual.max);
        Assert.assertEquals("Mean runtime mismatch", rExpected.mean.doubleValue(), rActual.mean.doubleValue(), 0.001);
    }

    @Test
    public void testActionDataProcessorMerge()
    {
        final ActionDataProcessor p1 = new ActionDataProcessor("Act1", actionsProvider);
        final ActionDataProcessor p2 = new ActionDataProcessor("Act1", actionsProvider);
        final ActionDataProcessor pExpected = new ActionDataProcessor("Act1", actionsProvider);

        for (int i = 0; i < 30; i++)
        {
            final ActionData act = createAction("Act1", 2000L + i * 300, 200 + i * 20, i % 6 == 0);
            p1.processDataRecord(act);
            pExpected.processDataRecord(act);
        }

        for (int i = 30; i < 75; i++)
        {
            final ActionData act = createAction("Act1", 2000L + i * 300, 200 + i * 20, i % 4 == 0);
            p2.processDataRecord(act);
            pExpected.processDataRecord(act);
        }

        p1.merge(p2);

        final ActionReport rActual = (ActionReport) p1.createTimerReport(false);
        final ActionReport rExpected = (ActionReport) pExpected.createTimerReport(false);

        Assert.assertEquals("Count mismatch", rExpected.count, rActual.count);
        Assert.assertEquals("Errors mismatch", rExpected.errors, rActual.errors);
        Assert.assertEquals("Min runtime mismatch", rExpected.min, rActual.min);
        Assert.assertEquals("Max runtime mismatch", rExpected.max, rActual.max);
        Assert.assertEquals("Mean runtime mismatch", rExpected.mean.doubleValue(), rActual.mean.doubleValue(), 0.001);
        Assert.assertEquals("Apdex mismatch", rExpected.apdex.value.doubleValue(), rActual.apdex.value.doubleValue(), 0.01);
    }
}
