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

import java.util.List;
import java.util.Properties;

import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import org.junit.Assert;
import org.junit.Test;

public class SlowRequestReportTest
{
    @Test
    public void comparator_ByRuntime()
    {
        final SlowRequestReport report1 = getReport(5000, null, 0);
        final SlowRequestReport report2 = getReport(2000, null, 0);

        Assert.assertEquals(-1, SlowRequestReport.COMPARATOR.compare(report1, report2));
        Assert.assertEquals(1, SlowRequestReport.COMPARATOR.compare(report2, report1));
    }

    @Test
    public void comparator_ByName()
    {
        final SlowRequestReport report1 = getReport(5000, "bucketA", 0);
        final SlowRequestReport report2 = getReport(5000, "bucketB", 0);

        Assert.assertEquals(-1, SlowRequestReport.COMPARATOR.compare(report1, report2));
        Assert.assertEquals(1, SlowRequestReport.COMPARATOR.compare(report2, report1));
    }

    @Test
    public void comparator_ByProcessingOrder()
    {
        final SlowRequestReport report1 = getReport(5000, "bucketA", 1);
        final SlowRequestReport report2 = getReport(5000, "bucketA", 2);
        final SlowRequestReport report3 = getReport(5000, "bucketA", 1);

        Assert.assertEquals(-1, SlowRequestReport.COMPARATOR.compare(report1, report2));
        Assert.assertEquals(1, SlowRequestReport.COMPARATOR.compare(report2, report1));
        Assert.assertEquals(0, SlowRequestReport.COMPARATOR.compare(report1, report3));
        Assert.assertEquals(0, SlowRequestReport.COMPARATOR.compare(report3, report1));
    }

    @Test
    public void bucketComparator_ByRuntime()
    {
        final SlowRequestReport report1 = getReport(5000, null, 0);
        final SlowRequestReport report2 = getReport(4000, null, 0);

        Assert.assertEquals(-1, SlowRequestReport.BUCKET_COMPARATOR.compare(report1, report2));
        Assert.assertEquals(1, SlowRequestReport.BUCKET_COMPARATOR.compare(report2, report1));
    }

    @Test
    public void bucketComparator_ByProcessingOrder()
    {
        final SlowRequestReport report1 = getReport(5000, null, 1);
        final SlowRequestReport report2 = getReport(5000, null, 2);
        final SlowRequestReport report3 = getReport(5000, null, 1);

        Assert.assertEquals(-1, SlowRequestReport.BUCKET_COMPARATOR.compare(report1, report2));
        Assert.assertEquals(1, SlowRequestReport.BUCKET_COMPARATOR.compare(report2, report1));
        Assert.assertEquals(0, SlowRequestReport.BUCKET_COMPARATOR.compare(report1, report3));
        Assert.assertEquals(0, SlowRequestReport.BUCKET_COMPARATOR.compare(report3, report1));
    }

    private SlowRequestReport getReport(final int runtime, final String name, final long processingOrder)
    {
        final RequestData data = new RequestData();
        data.setRunTime(runtime);
        data.setName(name);

        return new SlowRequestReport(data, processingOrder);
    }

    /**
     * C4 regression test: Merging multiple worker {@link SlowestRequestsReportProvider} instances that
     * independently recorded requests with the same bucket name and runtime must not silently drop
     * requests due to colliding per-instance {@code processingOrder} values.
     */
    @Test
    public void mergeMultipleWorkers_SameBucketAndRuntimeDoesNotDropRequests() throws Exception
    {
        final Properties props = new Properties();
        props.setProperty("com.xceptance.xlt.reportgenerator.slowestRequests.requestsPerBucket", "5");
        props.setProperty("com.xceptance.xlt.reportgenerator.slowestRequests.totalRequests", "5");
        props.setProperty("com.xceptance.xlt.reportgenerator.slowestRequests.minRuntime", "3000");
        props.setProperty("com.xceptance.xlt.reportgenerator.slowestRequests.maxRuntime", "20000");
        final ReportGeneratorConfiguration config = new ReportGeneratorConfiguration(null, null, props);

        final SlowestRequestsReportProvider master = new SlowestRequestsReportProvider();
        master.setConfiguration(config);

        final SlowestRequestsReportProvider worker1 = new SlowestRequestsReportProvider();
        worker1.setConfiguration(config);
        worker1.processDataRecord(createRequest("Checkout", 10_000, "req-worker1-0"));
        worker1.processDataRecord(createRequest("Checkout", 9_000, "req-worker1-1"));

        final SlowestRequestsReportProvider worker2 = new SlowestRequestsReportProvider();
        worker2.setConfiguration(config);
        worker2.processDataRecord(createRequest("Checkout", 10_000, "req-worker2-0"));
        worker2.processDataRecord(createRequest("Checkout", 9_000, "req-worker2-1"));

        master.merge(worker1);
        master.merge(worker2);

        final List<SlowRequestReport> reports = ((SlowestRequestsReport) master.createReportFragment()).slowestRequests;
        Assert.assertEquals("All 4 slow requests across both workers must be retained after merge",
                            4, reports.size());
        Assert.assertEquals("req-worker1-0", reports.get(0).requestId);
        Assert.assertEquals("req-worker2-0", reports.get(1).requestId);
        Assert.assertEquals("req-worker1-1", reports.get(2).requestId);
        Assert.assertEquals("req-worker2-1", reports.get(3).requestId);
    }

    private static RequestData createRequest(final String name, final int runtime, final String requestId)
    {
        final RequestData data = new RequestData();
        data.setName(name);
        data.setRunTime(runtime);
        data.setRequestId(requestId);
        return data;
    }
}
