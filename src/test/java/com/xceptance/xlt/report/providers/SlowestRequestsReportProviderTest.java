package com.xceptance.xlt.report.providers;

import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.util.XltCharBuffer;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Enclosed.class)
public class SlowestRequestsReportProviderTest
{
    private static final int MIN_RUNTIME = 3_000;

    private static final int MAX_RUNTIME = 20_000;

    private static ReportGeneratorConfiguration config;
    
    private static SlowestRequestsReportProvider reportProvider;

    public static class GeneralTests
    {
        @Before
        public void setUp()
        {
            config = Mockito.mock(ReportGeneratorConfiguration.class);
            Mockito.doReturn(2).when(config).getSlowestRequestsPerBucket();
            Mockito.doReturn(5).when(config).getSlowestRequestsTotal();
            Mockito.doReturn(MIN_RUNTIME).when(config).getSlowestRequestsMinRuntime();
            Mockito.doReturn(MAX_RUNTIME).when(config).getSlowestRequestsMaxRuntime();

            reportProvider = new SlowestRequestsReportProvider();
            reportProvider.setConfiguration(config);
        }

        /**
         * Test that all request fields are correctly copied to the report.
         */
        @Test
        public void requestDataFields()
        {
            RequestData data = new RequestData();
            data.setName("bucket1");
            data.setRunTime(4_500);
            data.setTime(1_700_000_000_000L);
            data.setRequestId("request1");
            data.setHttpMethod("GET");
            data.setUrl("xceptance.com");
            data.setFormDataEncoding("application/x-www-form-urlencoded");
            data.setFormData("value1=foo&value2=bar");
            data.setResponseId("responseId1");
            data.setResponseCode(303);
            data.setContentType("text/html");
            data.setDnsTime(100);
            data.setConnectTime(200);
            data.setSendTime(300);
            data.setServerBusyTime(400);
            data.setReceiveTime(500);
            data.setTimeToFirstBytes(600);
            data.setTimeToLastBytes(700);
            data.setBytesSent(1_000);
            data.setBytesReceived(2_000);
            data.setUsedIpAddress("1.1.1.1");
            data.setIpAddresses(new String[]{"2.2.2.2", "3.3.3.3"});

            reportProvider.processDataRecord(data);

            List<SlowRequestReport> reports = getSlowestRequestReports();
            Assert.assertEquals(1, reports.size());
            Assert.assertEquals(data.getName(), reports.get(0).name);
            Assert.assertEquals(data.getRunTime(), reports.get(0).runtime);
            Assert.assertEquals(data.getTime(), reports.get(0).time.getTime());
            Assert.assertEquals(data.getRequestId(), reports.get(0).requestId);
            Assert.assertEquals(data.getHttpMethod().toString(), reports.get(0).httpMethod);
            Assert.assertEquals(data.getUrl().toString(), reports.get(0).url);
            Assert.assertEquals(data.getFormDataEncoding().toString(), reports.get(0).formDataEncoding);
            Assert.assertEquals(data.getFormData().toString(), reports.get(0).formData);
            Assert.assertEquals(data.getResponseId(), reports.get(0).responseId);
            Assert.assertEquals(data.getResponseCode(), reports.get(0).responseCode);
            Assert.assertEquals(data.getContentType().toString(), reports.get(0).contentType);
            Assert.assertEquals(data.getDnsTime(), reports.get(0).dnsTime);
            Assert.assertEquals(data.getConnectTime(), reports.get(0).connectTime);
            Assert.assertEquals(data.getSendTime(), reports.get(0).sendTime);
            Assert.assertEquals(data.getServerBusyTime(), reports.get(0).serverBusyTime);
            Assert.assertEquals(data.getReceiveTime(), reports.get(0).receiveTime);
            Assert.assertEquals(data.getTimeToFirstBytes(), reports.get(0).timeToFirstBytes);
            Assert.assertEquals(data.getTimeToLastBytes(), reports.get(0).timeToLastBytes);
            Assert.assertEquals(data.getBytesSent(), reports.get(0).bytesSent);
            Assert.assertEquals(data.getBytesReceived(), reports.get(0).bytesReceived);
            Assert.assertEquals(data.getUsedIpAddress().toString(), reports.get(0).usedIpAddress);
            Assert.assertEquals(data.getIpAddresses().length, reports.get(0).ipAddresses.length);
            Assert.assertEquals(data.getIpAddresses()[0], reports.get(0).ipAddresses[0]);
            Assert.assertEquals(data.getIpAddresses()[1], reports.get(0).ipAddresses[1]);
        }

        /**
         * Test that null values in the request data are processed without errors.
         */
        @Test
        public void requestDataFields_nullValues()
        {
            RequestData data = new RequestData();
            data.setName("bucket1");
            data.setRunTime(5_000);
            data.setRequestId((XltCharBuffer) null);
            data.setHttpMethod((XltCharBuffer) null);
            data.setUrl((String) null);
            data.setFormDataEncoding((XltCharBuffer) null);
            data.setFormData((XltCharBuffer) null);
            data.setResponseId((XltCharBuffer) null);
            data.setContentType((String) null);
            data.setUsedIpAddress((XltCharBuffer) null);
            data.setIpAddresses(null);

            reportProvider.processDataRecord(data);

            List<SlowRequestReport> reports = getSlowestRequestReports();
            Assert.assertEquals(1, reports.size());
            Assert.assertEquals("bucket1", reports.get(0).name);
            Assert.assertEquals(5_000, reports.get(0).runtime);
            Assert.assertNull(reports.get(0).requestId);
            Assert.assertNull(reports.get(0).httpMethod);
            Assert.assertNull(reports.get(0).url);
            Assert.assertNull(reports.get(0).formDataEncoding);
            Assert.assertNull(reports.get(0).formData);
            Assert.assertNull(reports.get(0).responseId);
            Assert.assertNull(reports.get(0).contentType);
            Assert.assertNull(reports.get(0).usedIpAddress);
            Assert.assertNull(reports.get(0).ipAddresses);
        }

        /**
         * Tests that the request limit per bucket works as intended.
         */
        @Test
        public void requestsPerBucketLimit()
        {
            // first request is added
            reportProvider.processDataRecord(createRequestData("bucket1", 5_000));
            List<SlowRequestReport> reports = getSlowestRequestReports();
            Assert.assertEquals(1, reports.size());
            Assert.assertEquals(5_000, reports.get(0).runtime);
            Assert.assertEquals("bucket1:5000", reports.get(0).requestId);

            // second request is added; requests are sorted by runtime
            reportProvider.processDataRecord(createRequestData("bucket1", 7_000));
            reports = getSlowestRequestReports();
            Assert.assertEquals(2, reports.size());
            Assert.assertEquals(7_000, reports.get(0).runtime);
            Assert.assertEquals("bucket1:7000", reports.get(0).requestId);
            Assert.assertEquals(5_000, reports.get(1).runtime);
            Assert.assertEquals("bucket1:5000", reports.get(1).requestId);

            // bucket limit is exceeded and the slowest request is removed
            reportProvider.processDataRecord(createRequestData("bucket1", 6_000));
            reports = getSlowestRequestReports();
            Assert.assertEquals(2, reports.size());
            Assert.assertEquals(7_000, reports.get(0).runtime);
            Assert.assertEquals("bucket1:7000", reports.get(0).requestId);
            Assert.assertEquals(6_000, reports.get(1).runtime);
            Assert.assertEquals("bucket1:6000", reports.get(1).requestId);

            // new request isn't added because it's faster than the existing requests in the bucket
            reportProvider.processDataRecord(createRequestData("bucket1", 4_000));
            reports = getSlowestRequestReports();
            Assert.assertEquals(2, reports.size());
            Assert.assertEquals(7_000, reports.get(0).runtime);
            Assert.assertEquals("bucket1:7000", reports.get(0).requestId);
            Assert.assertEquals(6_000, reports.get(1).runtime);
            Assert.assertEquals("bucket1:6000", reports.get(1).requestId);

            // another slower request is added at the top as expected
            reportProvider.processDataRecord(createRequestData("bucket1", 10_000));
            reports = getSlowestRequestReports();
            Assert.assertEquals(2, reports.size());
            Assert.assertEquals(10_000, reports.get(0).runtime);
            Assert.assertEquals("bucket1:10000", reports.get(0).requestId);
            Assert.assertEquals(7_000, reports.get(1).runtime);
            Assert.assertEquals("bucket1:7000", reports.get(1).requestId);

            // request from second bucket is added; no further requests are removed due to bucket limit
            reportProvider.processDataRecord(createRequestData("bucket2", 8_000));
            reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals(10_000, reports.get(0).runtime);
            Assert.assertEquals("bucket1:10000", reports.get(0).requestId);
            Assert.assertEquals(8_000, reports.get(1).runtime);
            Assert.assertEquals("bucket2:8000", reports.get(1).requestId);
            Assert.assertEquals(7_000, reports.get(2).runtime);
            Assert.assertEquals("bucket1:7000", reports.get(2).requestId);

            // another request from the second bucket
            reportProvider.processDataRecord(createRequestData("bucket2", 12_000));
            reports = getSlowestRequestReports();
            Assert.assertEquals(4, reports.size());
            Assert.assertEquals(12_000, reports.get(0).runtime);
            Assert.assertEquals("bucket2:12000", reports.get(0).requestId);
            Assert.assertEquals(10_000, reports.get(1).runtime);
            Assert.assertEquals("bucket1:10000", reports.get(1).requestId);
            Assert.assertEquals(8_000, reports.get(2).runtime);
            Assert.assertEquals("bucket2:8000", reports.get(2).requestId);
            Assert.assertEquals(7_000, reports.get(3).runtime);
            Assert.assertEquals("bucket1:7000", reports.get(3).requestId);

            // bucket limit for second bucket is exceeded as well; the 8000 ms request is removed because it was in the
            // same bucket; the 7000 ms request remains despite having a lower runtime
            reportProvider.processDataRecord(createRequestData("bucket2", 11_000));
            reports = getSlowestRequestReports();
            Assert.assertEquals(4, reports.size());
            Assert.assertEquals(12_000, reports.get(0).runtime);
            Assert.assertEquals("bucket2:12000", reports.get(0).requestId);
            Assert.assertEquals(11_000, reports.get(1).runtime);
            Assert.assertEquals("bucket2:11000", reports.get(1).requestId);
            Assert.assertEquals(10_000, reports.get(2).runtime);
            Assert.assertEquals("bucket1:10000", reports.get(2).requestId);
            Assert.assertEquals(7_000, reports.get(3).runtime);
            Assert.assertEquals("bucket1:7000", reports.get(3).requestId);
        }

        /**
         * Tests that the total request limit works as intended.
         */
        @Test
        public void requestTotalLimit()
        {
            // add requests from two buckets; the limit for both buckets is reached now
            reportProvider.processDataRecord(createRequestData("bucket1", 11_000));
            reportProvider.processDataRecord(createRequestData("bucket1", 4_000));
            reportProvider.processDataRecord(createRequestData("bucket2", 7_000));
            reportProvider.processDataRecord(createRequestData("bucket2", 8_000));
            List<SlowRequestReport> reports = getSlowestRequestReports();
            Assert.assertEquals(4, reports.size());
            Assert.assertEquals(11_000, reports.get(0).runtime);
            Assert.assertEquals("bucket1:11000", reports.get(0).requestId);
            Assert.assertEquals(8_000, reports.get(1).runtime);
            Assert.assertEquals("bucket2:8000", reports.get(1).requestId);
            Assert.assertEquals(7_000, reports.get(2).runtime);
            Assert.assertEquals("bucket2:7000", reports.get(2).requestId);
            Assert.assertEquals(4_000, reports.get(3).runtime);
            Assert.assertEquals("bucket1:4000", reports.get(3).requestId);

            // add request from a third bucket; the total request limit is reached now
            reportProvider.processDataRecord(createRequestData("bucket3", 5_000));
            reports = getSlowestRequestReports();
            Assert.assertEquals(5, reports.size());
            Assert.assertEquals(11_000, reports.get(0).runtime);
            Assert.assertEquals("bucket1:11000", reports.get(0).requestId);
            Assert.assertEquals(8_000, reports.get(1).runtime);
            Assert.assertEquals("bucket2:8000", reports.get(1).requestId);
            Assert.assertEquals(7_000, reports.get(2).runtime);
            Assert.assertEquals("bucket2:7000", reports.get(2).requestId);
            Assert.assertEquals(5_000, reports.get(3).runtime);
            Assert.assertEquals("bucket3:5000", reports.get(3).requestId);
            Assert.assertEquals(4_000, reports.get(4).runtime);
            Assert.assertEquals("bucket1:4000", reports.get(4).requestId);

            // total request limit is exceeded, so the fastest request is removed
            reportProvider.processDataRecord(createRequestData("bucket3", 6_000));
            reports = getSlowestRequestReports();
            Assert.assertEquals(5, reports.size());
            Assert.assertEquals(11_000, reports.get(0).runtime);
            Assert.assertEquals("bucket1:11000", reports.get(0).requestId);
            Assert.assertEquals(8_000, reports.get(1).runtime);
            Assert.assertEquals("bucket2:8000", reports.get(1).requestId);
            Assert.assertEquals(7_000, reports.get(2).runtime);
            Assert.assertEquals("bucket2:7000", reports.get(2).requestId);
            Assert.assertEquals(6_000, reports.get(3).runtime);
            Assert.assertEquals("bucket3:6000", reports.get(3).requestId);
            Assert.assertEquals(5_000, reports.get(4).runtime);
            Assert.assertEquals("bucket3:5000", reports.get(4).requestId);

            // request from fourth bucket is added, but it's not in the report because the runtime is too low
            reportProvider.processDataRecord(createRequestData("bucket4", 4_000));
            reports = getSlowestRequestReports();
            Assert.assertEquals(5, reports.size());
            Assert.assertEquals(11_000, reports.get(0).runtime);
            Assert.assertEquals("bucket1:11000", reports.get(0).requestId);
            Assert.assertEquals(8_000, reports.get(1).runtime);
            Assert.assertEquals("bucket2:8000", reports.get(1).requestId);
            Assert.assertEquals(7_000, reports.get(2).runtime);
            Assert.assertEquals("bucket2:7000", reports.get(2).requestId);
            Assert.assertEquals(6_000, reports.get(3).runtime);
            Assert.assertEquals("bucket3:6000", reports.get(3).requestId);
            Assert.assertEquals(5_000, reports.get(4).runtime);
            Assert.assertEquals("bucket3:5000", reports.get(4).requestId);

            // request with higher runtime from fourth bucket is added and appears in the report
            reportProvider.processDataRecord(createRequestData("bucket4", 9_000));
            reports = getSlowestRequestReports();
            Assert.assertEquals(5, reports.size());
            Assert.assertEquals(11_000, reports.get(0).runtime);
            Assert.assertEquals("bucket1:11000", reports.get(0).requestId);
            Assert.assertEquals(9_000, reports.get(1).runtime);
            Assert.assertEquals("bucket4:9000", reports.get(1).requestId);
            Assert.assertEquals(8_000, reports.get(2).runtime);
            Assert.assertEquals("bucket2:8000", reports.get(2).requestId);
            Assert.assertEquals(7_000, reports.get(3).runtime);
            Assert.assertEquals("bucket2:7000", reports.get(3).requestId);
            Assert.assertEquals(6_000, reports.get(4).runtime);
            Assert.assertEquals("bucket3:6000", reports.get(4).requestId);

            // add another request from second bucket; fastest value from that bucket is removed due to bucket limit
            reportProvider.processDataRecord(createRequestData("bucket2", 10_000));
            reports = getSlowestRequestReports();
            Assert.assertEquals(5, reports.size());
            Assert.assertEquals(11_000, reports.get(0).runtime);
            Assert.assertEquals("bucket1:11000", reports.get(0).requestId);
            Assert.assertEquals(10_000, reports.get(1).runtime);
            Assert.assertEquals("bucket2:10000", reports.get(1).requestId);
            Assert.assertEquals(9_000, reports.get(2).runtime);
            Assert.assertEquals("bucket4:9000", reports.get(2).requestId);
            Assert.assertEquals(8_000, reports.get(3).runtime);
            Assert.assertEquals("bucket2:8000", reports.get(3).requestId);
            Assert.assertEquals(6_000, reports.get(4).runtime);
            Assert.assertEquals("bucket3:6000", reports.get(4).requestId);
        }

        /**
         * Test that runtime values that are slightly above or below existing values are handled correctly when the
         * bucket limit is exceeded.
         */
        @Test
        public void requestsPerBucketLimit_minimalDifferences()
        {
            Mockito.doReturn(3).when(config).getSlowestRequestsPerBucket();
            reportProvider.setConfiguration(config);

            // add requests until the bucket is full
            reportProvider.processDataRecord(createRequestData("bucket1", 5_001));
            reportProvider.processDataRecord(createRequestData("bucket1", 5_003));
            reportProvider.processDataRecord(createRequestData("bucket1", 5_005));
            List<SlowRequestReport> reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals(5_005, reports.get(0).runtime);
            Assert.assertEquals(5_003, reports.get(1).runtime);
            Assert.assertEquals(5_001, reports.get(2).runtime);

            // request with a slightly faster runtime than last request isn't added to the bucket
            reportProvider.processDataRecord(createRequestData("bucket1", 5_000));
            reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals(5_005, reports.get(0).runtime);
            Assert.assertEquals(5_003, reports.get(1).runtime);
            Assert.assertEquals(5_001, reports.get(2).runtime);

            // request with a slightly slower runtime than last request is added to the bucket and sorted correctly
            reportProvider.processDataRecord(createRequestData("bucket1", 5_002));
            reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals(5_005, reports.get(0).runtime);
            Assert.assertEquals(5_003, reports.get(1).runtime);
            Assert.assertEquals(5_002, reports.get(2).runtime);

            // request with slight runtime differences to existing values is sorted correctly
            reportProvider.processDataRecord(createRequestData("bucket1", 5_004));
            reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals(5_005, reports.get(0).runtime);
            Assert.assertEquals(5_004, reports.get(1).runtime);
            Assert.assertEquals(5_003, reports.get(2).runtime);
        }

        /**
         * Test that runtime values that are slightly above or below existing values are handled correctly when the
         * request total is exceeded.
         */
        @Test
        public void requestsTotalLimit_minimalDifferences()
        {
            Mockito.doReturn(3).when(config).getSlowestRequestsTotal();
            reportProvider.setConfiguration(config);

            // add requests to different buckets until request total is reached
            reportProvider.processDataRecord(createRequestData("bucket1", 5_001));
            reportProvider.processDataRecord(createRequestData("bucket2", 5_003));
            reportProvider.processDataRecord(createRequestData("bucket1", 5_005));
            List<SlowRequestReport> reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals(5_005, reports.get(0).runtime);
            Assert.assertEquals(5_003, reports.get(1).runtime);
            Assert.assertEquals(5_001, reports.get(2).runtime);

            // request with a slightly faster runtime than last request isn't added to final result
            reportProvider.processDataRecord(createRequestData("bucket2", 5_000));
            reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals(5_005, reports.get(0).runtime);
            Assert.assertEquals(5_003, reports.get(1).runtime);
            Assert.assertEquals(5_001, reports.get(2).runtime);

            // request with a slightly slower runtime than last request is added to the result and sorted correctly
            reportProvider.processDataRecord(createRequestData("bucket3", 5_002));
            reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals(5_005, reports.get(0).runtime);
            Assert.assertEquals(5_003, reports.get(1).runtime);
            Assert.assertEquals(5_002, reports.get(2).runtime);

            // request with slight runtime differences to existing values is sorted correctly
            reportProvider.processDataRecord(createRequestData("bucket3", 5_004));
            reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals(5_005, reports.get(0).runtime);
            Assert.assertEquals(5_004, reports.get(1).runtime);
            Assert.assertEquals(5_003, reports.get(2).runtime);
        }

        /**
         * Test that requests with a runtime beyond the min or max thresholds are ignored.
         */
        @Test
        public void minAndMaxRuntimes()
        {
            // below min runtime
            reportProvider.processDataRecord(createRequestData("bucket1", MIN_RUNTIME - 1, "bucket1:min-1"));
            Assert.assertEquals(0, getSlowestRequestReports().size());

            // above max runtime
            reportProvider.processDataRecord(createRequestData("bucket1", MAX_RUNTIME + 1, "bucket1:max+1"));
            Assert.assertEquals(0, getSlowestRequestReports().size());

            // exactly min runtime
            reportProvider.processDataRecord(createRequestData("bucket1", MIN_RUNTIME, "bucket1:min"));
            List<SlowRequestReport> reports = getSlowestRequestReports();
            Assert.assertEquals(1, reports.size());
            Assert.assertEquals(MIN_RUNTIME, reports.get(0).runtime);
            Assert.assertEquals("bucket1:min", reports.get(0).requestId);

            // exactly max runtime
            reportProvider.processDataRecord(createRequestData("bucket1", MAX_RUNTIME, "bucket1:max"));
            reports = getSlowestRequestReports();
            Assert.assertEquals(2, reports.size());
            Assert.assertEquals(MAX_RUNTIME, reports.get(0).runtime);
            Assert.assertEquals("bucket1:max", reports.get(0).requestId);
            Assert.assertEquals(MIN_RUNTIME, reports.get(1).runtime);
            Assert.assertEquals("bucket1:min", reports.get(1).requestId);
        }

        /**
         * Test that processing the same requests results in the same report, regardless of the processing order.
         * (Processing order only matters if two requests have the same runtime, bucket name and start time)
         */
        @Test
        public void resultIndependentOfProcessingOrder()
        {
            // prepare test request data
            final List<RequestData> requests = new ArrayList<>();
            requests.add(createRequestData("bucket1", 10_000));
            requests.add(createRequestData("bucket3", 9_000));
            requests.add(createRequestData("bucket2", 8_000));
            requests.add(createRequestData("bucket2", 7_000));
            requests.add(createRequestData("bucket2", 6_000));
            requests.add(createRequestData("bucket1", 5_000));
            requests.add(createRequestData("bucket1", 4_000));
            requests.add(createRequestData("bucket3", 3_000));

            final List<List<SlowRequestReport>> reportsList = new ArrayList<>();

            // process requests from slowest to fastest
            reportProvider = new SlowestRequestsReportProvider();
            reportProvider.setConfiguration(config);
            reportProvider.processDataRecord(requests.get(0));
            reportProvider.processDataRecord(requests.get(1));
            reportProvider.processDataRecord(requests.get(2));
            reportProvider.processDataRecord(requests.get(3));
            reportProvider.processDataRecord(requests.get(4));
            reportProvider.processDataRecord(requests.get(5));
            reportProvider.processDataRecord(requests.get(6));
            reportProvider.processDataRecord(requests.get(7));
            reportsList.add(getSlowestRequestReports());

            // process requests from fastest to slowest
            reportProvider = new SlowestRequestsReportProvider();
            reportProvider.setConfiguration(config);
            reportProvider.processDataRecord(requests.get(7));
            reportProvider.processDataRecord(requests.get(6));
            reportProvider.processDataRecord(requests.get(5));
            reportProvider.processDataRecord(requests.get(4));
            reportProvider.processDataRecord(requests.get(3));
            reportProvider.processDataRecord(requests.get(2));
            reportProvider.processDataRecord(requests.get(1));
            reportProvider.processDataRecord(requests.get(0));
            reportsList.add(getSlowestRequestReports());

            // process requests in arbitrary order
            reportProvider = new SlowestRequestsReportProvider();
            reportProvider.setConfiguration(config);
            reportProvider.processDataRecord(requests.get(6));
            reportProvider.processDataRecord(requests.get(1));
            reportProvider.processDataRecord(requests.get(3));
            reportProvider.processDataRecord(requests.get(7));
            reportProvider.processDataRecord(requests.get(2));
            reportProvider.processDataRecord(requests.get(0));
            reportProvider.processDataRecord(requests.get(4));
            reportProvider.processDataRecord(requests.get(5));
            reportsList.add(getSlowestRequestReports());

            for (final List<SlowRequestReport> reports : reportsList)
            {
                Assert.assertEquals(5, reports.size());
                Assert.assertEquals("bucket1:10000", reports.get(0).requestId);
                Assert.assertEquals("bucket3:9000", reports.get(1).requestId);
                Assert.assertEquals("bucket2:8000", reports.get(2).requestId);
                Assert.assertEquals("bucket2:7000", reports.get(3).requestId);
                Assert.assertEquals("bucket1:5000", reports.get(4).requestId);
            }
        }

        /**
         * Test that requests with the same runtime are sorted by bucket name.
         */
        @Test
        public void sameRuntime_OrderedByBucketName()
        {
            Mockito.doReturn(3).when(config).getSlowestRequestsTotal();
            reportProvider.setConfiguration(config);

            // add first request
            reportProvider.processDataRecord(createRequestData("bucketC", 5_000));
            List<SlowRequestReport> reports = getSlowestRequestReports();
            Assert.assertEquals(1, reports.size());
            Assert.assertEquals("bucketC", reports.get(0).name);

            // new request's name comes after existing request's name
            reportProvider.processDataRecord(createRequestData("bucketD", 5_000));
            reports = getSlowestRequestReports();
            Assert.assertEquals(2, reports.size());
            Assert.assertEquals("bucketC", reports.get(0).name);
            Assert.assertEquals("bucketD", reports.get(1).name);

            // new request's name comes before existing request's names
            reportProvider.processDataRecord(createRequestData("bucketA", 5_000));
            reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals("bucketA", reports.get(0).name);
            Assert.assertEquals("bucketC", reports.get(1).name);
            Assert.assertEquals("bucketD", reports.get(2).name);

            // new request's name comes after existing request's name and isn't added due to request total
            reportProvider.processDataRecord(createRequestData("bucketE", 5_000));
            reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals("bucketA", reports.get(0).name);
            Assert.assertEquals("bucketC", reports.get(1).name);
            Assert.assertEquals("bucketD", reports.get(2).name);

            // new request's name comes before some existing request's names; last request is removed due to request total
            reportProvider.processDataRecord(createRequestData("bucketB", 5_000));
            reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals("bucketA", reports.get(0).name);
            Assert.assertEquals("bucketB", reports.get(1).name);
            Assert.assertEquals("bucketC", reports.get(2).name);
        }

        @Test(expected = IllegalArgumentException.class)
        public void invalidConfig_RequestsPerBucketIsZero()
        {
            Mockito.doReturn(0).when(config).getSlowestRequestsPerBucket();
            reportProvider.setConfiguration(config);
        }

        @Test(expected = IllegalArgumentException.class)
        public void invalidConfig_RequestsTotalIsZero()
        {
            Mockito.doReturn(0).when(config).getSlowestRequestsTotal();
            reportProvider.setConfiguration(config);
        }

        @Test(expected = IllegalArgumentException.class)
        public void invalidConfig_MinRuntimeIsZero()
        {
            Mockito.doReturn(0).when(config).getSlowestRequestsMinRuntime();
            reportProvider.setConfiguration(config);
        }

        @Test(expected = IllegalArgumentException.class)
        public void invalidConfig_MaxRuntimeIsZero()
        {
            Mockito.doReturn(0).when(config).getSlowestRequestsMaxRuntime();
            reportProvider.setConfiguration(config);
        }

        @Test(expected = IllegalArgumentException.class)
        public void invalidConfig_MinRuntimeGreaterMaxRuntime()
        {
            // the min runtime must not be greater than the max runtime
            Mockito.doReturn(5_001).when(config).getSlowestRequestsMinRuntime();
            Mockito.doReturn(5_000).when(config).getSlowestRequestsMaxRuntime();
            reportProvider.setConfiguration(config);
        }
    }

    /**
     * This subclass includes tests about handling requests with duplicate runtime and name. The test is parameterized
     * to run the same tests with two different settings for the requests per bucket and requests total properties. For
     * these particular test cases the result should be the same in both cases.
     */
    @RunWith(Parameterized.class)
    public static class DuplicateRuntimeAndNameTests
    {
        @Parameters(name = "bucket limit: {0}, total limit: {1}")
        public static Collection<Object[]> data()
        {
            return Arrays.asList(new Object[][]
                                     {
                                         { 10, 3 }, { 3, 10 }
                                     });
        }

        @Parameter(0)
        public int requestsPerBucket;

        @Parameter(1)
        public int requestsTotal;

        @Before
        public void setUp()
        {
            config = Mockito.mock(ReportGeneratorConfiguration.class);
            Mockito.doReturn(requestsPerBucket).when(config).getSlowestRequestsPerBucket();
            Mockito.doReturn(requestsTotal).when(config).getSlowestRequestsTotal();
            Mockito.doReturn(MIN_RUNTIME).when(config).getSlowestRequestsMinRuntime();
            Mockito.doReturn(MAX_RUNTIME).when(config).getSlowestRequestsMaxRuntime();

            reportProvider = new SlowestRequestsReportProvider();
            reportProvider.setConfiguration(config);
        }

        /**
         * Test that requests with the same runtime and bucket name are sorted by request start time.
         */
        @Test
        public void sameRuntime_OrderedByStartTime()
        {
            // add first request
            reportProvider.processDataRecord(createRequestData("bucket1", 5_000, null, 5));
            List<SlowRequestReport> reports = getSlowestRequestReports();
            Assert.assertEquals(1, reports.size());
            Assert.assertEquals(5, reports.get(0).time.getTime());

            // new request has later start time
            reportProvider.processDataRecord(createRequestData("bucket1", 5_000, null, 6));
            reports = getSlowestRequestReports();
            Assert.assertEquals(2, reports.size());
            Assert.assertEquals(5, reports.get(0).time.getTime());
            Assert.assertEquals(6, reports.get(1).time.getTime());

            // new request has earlier start time
            reportProvider.processDataRecord(createRequestData("bucket1", 5_000, null, 3));
            reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals(3, reports.get(0).time.getTime());
            Assert.assertEquals(5, reports.get(1).time.getTime());
            Assert.assertEquals(6, reports.get(2).time.getTime());

            // new request has later start time and isn't in report due to bucket/total limit
            reportProvider.processDataRecord(createRequestData("bucket1", 5_000, null, 7));
            reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals(3, reports.get(0).time.getTime());
            Assert.assertEquals(5, reports.get(1).time.getTime());
            Assert.assertEquals(6, reports.get(2).time.getTime());

            // new request has earlier start time; last request is removed due to bucket/total limit
            reportProvider.processDataRecord(createRequestData("bucket1", 5_000, null, 4));
            reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals(3, reports.get(0).time.getTime());
            Assert.assertEquals(4, reports.get(1).time.getTime());
            Assert.assertEquals(5, reports.get(2).time.getTime());
        }

        /**
         * Test that requests with the same runtime, name and start time are sorted by processing order.
         */
        @Test
        public void sameRuntime_OrderedByProcessingOrder()
        {
            // add first request
            reportProvider.processDataRecord(createRequestData("bucket1", 5_000, "request1", 5));
            List<SlowRequestReport> reports = getSlowestRequestReports();
            Assert.assertEquals(1, reports.size());
            Assert.assertEquals("request1", reports.get(0).requestId);

            // new request with same runtime, name and start time is appended at the end because it was processed later
            reportProvider.processDataRecord(createRequestData("bucket1", 5_000, "request2", 5));
            reports = getSlowestRequestReports();
            Assert.assertEquals(2, reports.size());
            Assert.assertEquals("request1", reports.get(0).requestId);
            Assert.assertEquals("request2", reports.get(1).requestId);

            // another new request with same runtime, name and start time is appended
            reportProvider.processDataRecord(createRequestData("bucket1", 5_000, "request3", 5));
            reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals("request1", reports.get(0).requestId);
            Assert.assertEquals("request2", reports.get(1).requestId);
            Assert.assertEquals("request3", reports.get(2).requestId);

            // new request with same runtime, name and start time is appended but not in the report due to the bucket/total limit
            reportProvider.processDataRecord(createRequestData("bucket1", 5_000, "request4", 5));
            reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals("request1", reports.get(0).requestId);
            Assert.assertEquals("request2", reports.get(1).requestId);
            Assert.assertEquals("request3", reports.get(2).requestId);

            // request with slower runtime is added above previous requests
            reportProvider.processDataRecord(createRequestData("bucket1", 6_000, "request5", 5));
            reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals("request5", reports.get(0).requestId);
            Assert.assertEquals("request1", reports.get(1).requestId);
            Assert.assertEquals("request2", reports.get(2).requestId);

            // another request with the slower runtime is appended after the previous one due to processing order
            reportProvider.processDataRecord(createRequestData("bucket1", 6_000, "request6", 5));
            reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals("request5", reports.get(0).requestId);
            Assert.assertEquals("request6", reports.get(1).requestId);
            Assert.assertEquals("request1", reports.get(2).requestId);

            // another request with the slower runtime is appended
            reportProvider.processDataRecord(createRequestData("bucket1", 6_000, "request7", 5));
            reports = getSlowestRequestReports();
            Assert.assertEquals(3, reports.size());
            Assert.assertEquals("request5", reports.get(0).requestId);
            Assert.assertEquals("request6", reports.get(1).requestId);
            Assert.assertEquals("request7", reports.get(2).requestId);
        }
    }

    /**
     * Create request test data with name and runtime. The requestId is automatically set to contain name and runtime.
     */
    private static RequestData createRequestData(final String name, final int runtime)
    {
        return createRequestData(name, runtime, name + ":" + runtime, 0);
    }

    /**
     * Create request test data with name, runtime and requestId.
     */
    private static RequestData createRequestData(final String name, final int runtime, final String requestId)
    {
        return createRequestData(name, runtime, requestId, 0);
    }

    /**
     * Create request test data with name, runtime, requestId and start time.
     */
    private static RequestData createRequestData(final String name, final int runtime, final String requestId, final long time)
    {
        RequestData data = new RequestData();
        data.setName(name);
        data.setRunTime(runtime);
        data.setRequestId(requestId);
        data.setTime(time);

        return data;
    }

    /**
     * Get the slowest requests that the provider would show in the report.
     */
    private static List<SlowRequestReport> getSlowestRequestReports()
    {
        return ((SlowestRequestsReport) reportProvider.createReportFragment()).slowestRequests;
    }
}
