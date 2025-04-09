package com.xceptance.xlt.report.providers;

import com.xceptance.xlt.api.engine.RequestData;
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
}
