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
import com.xceptance.xlt.report.ReportGeneratorConfiguration;

/**
 * Tests for {@link RequestDataProcessor}.
 * <p>
 * Created by AI (Gemini 2.5 Pro).
 * </p>
 */
public class RequestDataProcessorTest
{
    private RequestDataProcessor processor;

    @Before
    public void setup() throws Exception
    {
        final java.io.File tempDir = java.nio.file.Files.createTempDirectory("xlt-report").toFile();
        final ReportGeneratorConfiguration config = new DummyReportGeneratorConfiguration()
        {
            @Override
            public java.io.File getCsvDirectory()
            {
                return new java.io.File(tempDir, "csv");
            }
            @Override
            public java.io.File getChartDirectory()
            {
                return new java.io.File(tempDir, "charts");
            }
        };
        final RequestsReportProvider provider = new RequestsReportProvider();
        provider.setConfiguration(config);

        this.processor = new RequestDataProcessor("TestTimer", provider, false);
    }

    @Test
    public void testProcessDataRecord_ProcessesCachedRequests()
    {
        // First request - not cached
        final RequestData req1 = new RequestData("TestTimer");
        req1.setTime(1000);
        req1.setRunTime(100);
        req1.setBytesSent(100);
        req1.setBytesReceived(200);
        req1.setCached(false);

        this.processor.processDataRecord(req1);

        // Second request - cached
        final RequestData req2 = new RequestData("TestTimer");
        req2.setTime(2000);
        req2.setRunTime(0);
        req2.setBytesSent(0);
        req2.setBytesReceived(200);
        req2.setCached(true);

        this.processor.processDataRecord(req2);

        // Generate report and verify only req1 was processed
        final RequestReport report = (RequestReport) this.processor.createTimerReport(false);

        // We expect 2 requests in the statistics, the cached one should NOT be ignored anymore
        Assert.assertEquals(2, report.count);
        Assert.assertEquals(50, report.mean.intValue());
        Assert.assertEquals(0, report.min);
        Assert.assertEquals(100, report.max);
    }
}
