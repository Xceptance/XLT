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
}
