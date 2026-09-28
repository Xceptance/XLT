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

import com.xceptance.xlt.agent.JvmResourceUsageData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * Tests the merge behavior of ReportProviders.
 */
public class ReportProviderMergeTest
{
    private DummyReportGeneratorConfiguration config;

    @Before
    public void setUp()
    {
        config = DummyReportGeneratorConfiguration.getDefault();
    }

    private RequestData createRequest(final String name, final long time, final int runTime, final boolean failed,
                                      final int responseCode, final int bytesReceived, final String httpMethod)
    {
        final RequestData req = new RequestData(name);
        req.setTime(time);
        req.setRunTime(runTime);
        req.setFailed(failed);
        req.setResponseCode(responseCode);
        req.setBytesReceived(bytesReceived);
        req.setBytesSent(128);
        req.setHttpMethod(XltCharBuffer.valueOf(httpMethod));
        req.setUrl("http://localhost/" + name);
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
    public void testRequestsReportProviderMerge()
    {
        final RequestsReportProvider p1 = new RequestsReportProvider();
        p1.setConfiguration(config);

        final RequestsReportProvider p2 = new RequestsReportProvider();
        p2.setConfiguration(config);

        final RequestsReportProvider pExpected = new RequestsReportProvider();
        pExpected.setConfiguration(config);

        for (int i = 0; i < 30; i++)
        {
            final RequestData req = createRequest("ReqA", 1000L + i * 50, 50 + i * 2, false, 200, 500, "GET");
            p1.processDataRecord(req);
            pExpected.processDataRecord(req);
        }
        for (int i = 0; i < 20; i++)
        {
            final RequestData req = createRequest("ReqB", 1000L + i * 50, 100 + i * 5, i % 5 == 0, 500, 1000, "POST");
            p1.processDataRecord(req);
            pExpected.processDataRecord(req);
        }

        for (int i = 30; i < 60; i++)
        {
            final RequestData req = createRequest("ReqA", 1000L + i * 50, 50 + i * 2, false, 200, 500, "GET");
            p2.processDataRecord(req);
            pExpected.processDataRecord(req);
        }
        for (int i = 0; i < 15; i++)
        {
            final RequestData req = createRequest("ReqC", 2000L + i * 100, 200 + i * 10, false, 200, 2000, "GET");
            p2.processDataRecord(req);
            pExpected.processDataRecord(req);
        }

        // Merge p2 into p1
        p1.merge(p2);

        final RequestsReport actual = (RequestsReport) p1.createReportFragment();
        final RequestsReport expected = (RequestsReport) pExpected.createReportFragment();

        Assert.assertEquals("Number of request timers mismatch", expected.requests.size(), actual.requests.size());

        for (int i = 0; i < expected.requests.size(); i++)
        {
            final TimerReport expTimer = expected.requests.get(i);
            // find matching actual timer
            final TimerReport actTimer = actual.requests.stream()
                .filter(r -> r.name.equals(expTimer.name))
                .findFirst()
                .orElse(null);

            Assert.assertNotNull("Missing timer: " + expTimer.name, actTimer);
            Assert.assertEquals("Count mismatch for " + expTimer.name, expTimer.count, actTimer.count);
            Assert.assertEquals("Errors mismatch for " + expTimer.name, expTimer.errors, actTimer.errors);
            Assert.assertEquals("Mean mismatch for " + expTimer.name, expTimer.mean.doubleValue(), actTimer.mean.doubleValue(), 0.001);
        }
    }

    @Test
    public void testGeneralReportProviderMerge()
    {
        final GeneralReportProvider p1 = new GeneralReportProvider();
        p1.setConfiguration(config);

        final GeneralReportProvider p2 = new GeneralReportProvider();
        p2.setConfiguration(config);

        final GeneralReportProvider pExpected = new GeneralReportProvider();
        pExpected.setConfiguration(config);

        for (int i = 0; i < 25; i++)
        {
            final RequestData req = createRequest("Req", 1000L + i * 10, 50, false, 200, 500, "GET");
            p1.processDataRecord(req);
            pExpected.processDataRecord(req);

            final TransactionData txn = createTransaction("Txn", 1000L + i * 10, 100, i % 5 == 0);
            p1.processDataRecord(txn);
            pExpected.processDataRecord(txn);
        }

        for (int i = 25; i < 50; i++)
        {
            final RequestData req = createRequest("Req", 1000L + i * 10, 50, false, 200, 300, "GET");
            p2.processDataRecord(req);
            pExpected.processDataRecord(req);

            final TransactionData txn = createTransaction("Txn", 1000L + i * 10, 100, i % 3 == 0);
            p2.processDataRecord(txn);
            pExpected.processDataRecord(txn);
        }

        p1.merge(p2);

        final GeneralReport actual = (GeneralReport) p1.createReportFragment();
        final GeneralReport expected = (GeneralReport) pExpected.createReportFragment();

        Assert.assertEquals("Hits mismatch", expected.hits, actual.hits);
        Assert.assertEquals("BytesSent mismatch", expected.bytesSent, actual.bytesSent);
        Assert.assertEquals("BytesReceived mismatch", expected.bytesReceived, actual.bytesReceived);
    }

    @Test
    public void testResponseCodesReportProviderMerge()
    {
        final ResponseCodesReportProvider p1 = new ResponseCodesReportProvider();
        p1.setConfiguration(config);

        final ResponseCodesReportProvider p2 = new ResponseCodesReportProvider();
        p2.setConfiguration(config);

        final ResponseCodesReportProvider pExpected = new ResponseCodesReportProvider();
        pExpected.setConfiguration(config);

        for (int i = 0; i < 20; i++)
        {
            final RequestData req = createRequest("Req", 1000L + i * 10, 50, false, 200, 100, "GET");
            p1.processDataRecord(req);
            pExpected.processDataRecord(req);
        }
        for (int i = 0; i < 5; i++)
        {
            final RequestData req = createRequest("Req", 1000L + i * 10, 50, true, 404, 100, "GET");
            p1.processDataRecord(req);
            pExpected.processDataRecord(req);
        }

        for (int i = 0; i < 15; i++)
        {
            final RequestData req = createRequest("Req", 1000L + i * 10, 50, false, 200, 100, "GET");
            p2.processDataRecord(req);
            pExpected.processDataRecord(req);
        }
        for (int i = 0; i < 8; i++)
        {
            final RequestData req = createRequest("Req", 1000L + i * 10, 50, true, 500, 100, "GET");
            p2.processDataRecord(req);
            pExpected.processDataRecord(req);
        }

        p1.merge(p2);

        final ResponseCodesReport actual = (ResponseCodesReport) p1.createReportFragment();
        final ResponseCodesReport expected = (ResponseCodesReport) pExpected.createReportFragment();

        Assert.assertEquals("Number of response codes mismatch", expected.responseCodes.size(), actual.responseCodes.size());
        for (final ResponseCodeReport expCode : expected.responseCodes)
        {
            final ResponseCodeReport actCode = actual.responseCodes.stream()
                .filter(c -> c.code == expCode.code)
                .findFirst()
                .orElse(null);
            Assert.assertNotNull("Missing code: " + expCode.code, actCode);
            Assert.assertEquals("Count mismatch for " + expCode.code, expCode.count, actCode.count);
        }
    }

    @Test
    public void testRequestMethodsReportProviderMerge()
    {
        final RequestMethodsReportProvider p1 = new RequestMethodsReportProvider();
        p1.setConfiguration(config);

        final RequestMethodsReportProvider p2 = new RequestMethodsReportProvider();
        p2.setConfiguration(config);

        final RequestMethodsReportProvider pExpected = new RequestMethodsReportProvider();
        pExpected.setConfiguration(config);

        for (int i = 0; i < 10; i++)
        {
            final RequestData req = createRequest("Req", 1000L + i * 10, 50, false, 200, 100, "GET");
            p1.processDataRecord(req);
            pExpected.processDataRecord(req);
        }
        for (int i = 0; i < 15; i++)
        {
            final RequestData req = createRequest("Req", 1000L + i * 10, 50, false, 200, 100, "POST");
            p2.processDataRecord(req);
            pExpected.processDataRecord(req);
        }

        p1.merge(p2);

        final RequestMethodsReport actual = (RequestMethodsReport) p1.createReportFragment();
        final RequestMethodsReport expected = (RequestMethodsReport) pExpected.createReportFragment();

        Assert.assertEquals(expected.requestMethods.size(), actual.requestMethods.size());
        for (final RequestMethodReport expMethod : expected.requestMethods)
        {
            final RequestMethodReport actMethod = actual.requestMethods.stream()
                .filter(m -> m.method.equals(expMethod.method))
                .findFirst()
                .orElse(null);
            Assert.assertNotNull(actMethod);
            Assert.assertEquals(expMethod.count, actMethod.count);
        }
    }

    /**
     * Tests that merging {@link AgentsReportProvider} instances correctly preserves and propagates
     * the full agent name (e.g. 'Agent-ac0001_us-east1_00-35.227.95.149-8500') irrespective of the order
     * in which worker threads are merged.
     * <p>
     * In multi-threaded report generation:
     * - Worker threads processing transaction records only know the agent directory name ('ac0001_us-east1_00').
     * - Worker threads processing JVM resource logs parse JvmResourceUsageData which provides the full agent
     *   display name ('Agent-ac0001_us-east1_00-35.227.95.149-8500').
     * - When thread-local providers are merged into the master provider, the full agent name must never be
     *   overwritten or lost by a processor initialized only with the short agent ID.
     */
    @Test
    public void testAgentsReportProviderMerge()
    {
        final String agentId1 = "ac0001_us-east1_00";
        final String fullAgentName1 = "Agent-ac0001_us-east1_00-35.227.95.149-8500";
        final String agentId2 = "ac0002_us-east1_00";
        final String fullAgentName2 = "Agent-ac0002_us-east1_00-35.229.98.88-8500";
        final String agentId3OnlyTxn = "ac0003_us-east1_00";

        // Provider 1: simulates a worker thread that processed only transactions
        final AgentsReportProvider pTxn = new AgentsReportProvider();
        pTxn.setConfiguration(config);

        for (int i = 0; i < 20; i++)
        {
            final TransactionData txn1 = createTransaction("TLogin", 1000L + i * 10, 100, i % 4 == 0);
            txn1.setAgentName(agentId1);
            pTxn.processDataRecord(txn1);

            final TransactionData txn3 = createTransaction("TSearch", 1000L + i * 10, 80, false);
            txn3.setAgentName(agentId3OnlyTxn);
            pTxn.processDataRecord(txn3);
        }

        // Provider 2: simulates a worker thread that processed JVM resource logs
        final AgentsReportProvider pJvm = new AgentsReportProvider();
        pJvm.setConfiguration(config);

        final JvmResourceUsageData jvm1 = new JvmResourceUsageData(fullAgentName1);
        jvm1.setAgentName(agentId1);
        jvm1.setTime(1000L);
        jvm1.setCpuUsage(30.0);
        jvm1.setTotalCpuUsage(45.0);
        jvm1.setMinorGcCount(10);
        jvm1.setMinorGcTime(500);
        pJvm.processDataRecord(jvm1);

        final JvmResourceUsageData jvm2 = new JvmResourceUsageData(fullAgentName2);
        jvm2.setAgentName(agentId2);
        jvm2.setTime(1000L);
        jvm2.setCpuUsage(20.0);
        jvm2.setTotalCpuUsage(35.0);
        jvm2.setMinorGcCount(5);
        jvm2.setMinorGcTime(200);
        pJvm.processDataRecord(jvm2);

        // Scenario A: Merge JVM provider into Transaction provider (pTxn was merged first into master)
        final AgentsReportProvider masterA = new AgentsReportProvider();
        masterA.setConfiguration(config);
        masterA.merge(pTxn);
        masterA.merge(pJvm);

        final AgentsReport reportA = (AgentsReport) masterA.createReportFragment();
        Assert.assertEquals("Expected 3 agents", 3, reportA.agents.size());

        final AgentReport a1 = reportA.agents.stream().filter(a -> a.name.contains(agentId1)).findFirst().orElse(null);
        Assert.assertNotNull("Agent 1 should exist", a1);
        Assert.assertEquals("Agent 1 should have full JVM name", fullAgentName1, a1.name);
        Assert.assertEquals("Agent 1 transactions count", 20, a1.transactions);
        Assert.assertEquals("Agent 1 transactions error count", 5, a1.transactionErrors);
        Assert.assertEquals("Agent 1 minor GC count", 10, a1.minorGcCount);

        final AgentReport a2 = reportA.agents.stream().filter(a -> a.name.contains(agentId2)).findFirst().orElse(null);
        Assert.assertNotNull("Agent 2 should exist", a2);
        Assert.assertEquals("Agent 2 should have full JVM name", fullAgentName2, a2.name);
        Assert.assertEquals("Agent 2 transactions count", 0, a2.transactions);
        Assert.assertEquals("Agent 2 minor GC count", 5, a2.minorGcCount);

        final AgentReport a3 = reportA.agents.stream().filter(a -> a.name.contains(agentId3OnlyTxn)).findFirst().orElse(null);
        Assert.assertNotNull("Agent 3 should exist", a3);
        Assert.assertEquals("Agent 3 without JVM data retains short ID", agentId3OnlyTxn, a3.name);
        Assert.assertEquals("Agent 3 transactions count", 20, a3.transactions);

        // Scenario B: Reverse order - Merge Transaction provider into JVM provider (pJvm was merged first into master)
        final AgentsReportProvider masterB = new AgentsReportProvider();
        masterB.setConfiguration(config);
        masterB.merge(pJvm);
        masterB.merge(pTxn);

        final AgentsReport reportB = (AgentsReport) masterB.createReportFragment();
        Assert.assertEquals("Expected 3 agents", 3, reportB.agents.size());

        final AgentReport b1 = reportB.agents.stream().filter(a -> a.name.contains(agentId1)).findFirst().orElse(null);
        Assert.assertNotNull("Agent 1 should exist in reverse merge", b1);
        Assert.assertEquals("Agent 1 should have full JVM name in reverse merge", fullAgentName1, b1.name);
        Assert.assertEquals("Agent 1 transactions count", 20, b1.transactions);
        Assert.assertEquals("Agent 1 transactions error count", 5, b1.transactionErrors);

        final AgentReport b2 = reportB.agents.stream().filter(a -> a.name.contains(agentId2)).findFirst().orElse(null);
        Assert.assertNotNull("Agent 2 should exist in reverse merge", b2);
        Assert.assertEquals("Agent 2 should have full JVM name in reverse merge", fullAgentName2, b2.name);

        final AgentReport b3 = reportB.agents.stream().filter(a -> a.name.contains(agentId3OnlyTxn)).findFirst().orElse(null);
        Assert.assertNotNull("Agent 3 should exist in reverse merge", b3);
        Assert.assertEquals("Agent 3 without JVM data retains short ID in reverse merge", agentId3OnlyTxn, b3.name);
        Assert.assertEquals("Agent 3 transactions count", 20, b3.transactions);
    }
}
