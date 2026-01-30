package com.xceptance.xlt.report.providers;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

import org.apache.datasketches.hll.HllSketch;
import org.apache.datasketches.hll.TgtHllType;

/**
 * Tests for HllSketch-based distinct URL counting functionality used in RequestDataProcessor.
 */
public class DataSketchesHllDistinctCountTest
{
    // Same configuration as RequestDataProcessor
    private static final int LOG_K = 20;

    /**
     * Tests that HllSketch correctly counts a small number of distinct values.
     */
    @Test
    public void testSmallDistinctCount()
    {
        final HllSketch sketch = new HllSketch(LOG_K, TgtHllType.HLL_8);

        // Add 10 distinct values
        for (int i = 1; i <= 10; i++)
        {
            sketch.update(i);
        }

        final double estimate = sketch.getEstimate();

        // For small counts, HllSketch should be very accurate
        Assert.assertEquals("Distinct count should be close to 10", 10.0, estimate, 1.0);
    }

    /**
     * Tests that adding duplicate values does not increase the count.
     */
    @Test
    public void testDuplicatesDoNotIncreaseCount()
    {
        final HllSketch sketch = new HllSketch(LOG_K, TgtHllType.HLL_8);

        // Add the same 5 values multiple times
        for (int repeat = 0; repeat < 100; repeat++)
        {
            for (int i = 1; i <= 5; i++)
            {
                sketch.update(i);
            }
        }

        final double estimate = sketch.getEstimate();

        // Should still be close to 5
        Assert.assertEquals("Duplicates should not increase count", 5.0, estimate, 1.0);
    }

    /**
     * Tests that HllSketch correctly estimates a larger number of distinct values.
     */
    @Test
    public void testLargeDistinctCount()
    {
        final HllSketch sketch = new HllSketch(LOG_K, TgtHllType.HLL_8);

        final int expectedCount = 100_000;

        for (int i = 1; i <= expectedCount; i++)
        {
            sketch.update(i);
        }

        final double estimate = sketch.getEstimate();

        // Allow 2% error for 100k values (standard HLL error)
        final double tolerance = expectedCount * 0.02;
        Assert.assertEquals("Distinct count should be close to " + expectedCount, expectedCount, estimate, tolerance);
    }

    /**
     * Tests that HllSketch works correctly with URL-style strings.
     */
    @Test
    public void testWithUrlStrings()
    {
        final HllSketch sketch = new HllSketch(LOG_K, TgtHllType.HLL_8);

        String url1 = "http://example.com/page1";
        String url2 = "http://example.com/page2";
        String url3 = "http://example.com/page3";

        sketch.update(url1.hashCode());
        sketch.update(url2.hashCode());
        sketch.update(url3.hashCode());
        sketch.update(url1.hashCode()); // duplicate

        final double estimate = sketch.getEstimate();

        // Should be 3 distinct URLs
        Assert.assertEquals("Should count 3 distinct URLs", 3.0, estimate, 1.0);
    }

    /**
     * Tests realistic URL distribution with duplicates.
     */
    @Test
    public void testRealisticUrlScenario()
    {
        final HllSketch sketch = new HllSketch(LOG_K, TgtHllType.HLL_8);

        // Simulate a realistic scenario: 1000 unique URLs, each hit 10 times
        final int uniqueUrls = 1000;
        final int hitsPerUrl = 10;

        for (int hit = 0; hit < hitsPerUrl; hit++)
        {
            for (int urlId = 0; urlId < uniqueUrls; urlId++)
            {
                String url = "http://example.com/page/" + urlId;
                sketch.update(url.hashCode());
            }
        }

        final double estimate = sketch.getEstimate();

        // Should estimate close to uniqueUrls (allow 2% tolerance)
        final double tolerance = uniqueUrls * 0.02;
        Assert.assertEquals("Should estimate close to unique URL count", uniqueUrls, estimate, tolerance);
    }

    /**
     * Tests that casting to int (as done in RequestDataProcessor) works correctly.
     */
    @Test
    public void testCastToInt()
    {
        final HllSketch sketch = new HllSketch(LOG_K, TgtHllType.HLL_8);

        for (int i = 1; i <= 1000; i++)
        {
            sketch.update(i);
        }

        // This is how RequestDataProcessor uses it
        final int intEstimate = (int) sketch.getEstimate();

        // Should be close to 1000
        Assert.assertTrue("Int cast should be close to 1000, was: " + intEstimate, intEstimate >= 980 && intEstimate <= 1020);
    }

    /**
     * Tests very large distinct count (simulating heavy load test).
     */
    @Test
    public void testVeryLargeDistinctCount()
    {
        final HllSketch sketch = new HllSketch(LOG_K, TgtHllType.HLL_8);

        final int expectedCount = 1_000_000;

        for (int i = 0; i < expectedCount; i++)
        {
            String url = "http://example.com/product/?r=" + i;
            sketch.update(url.hashCode());
        }

        final double estimate = sketch.getEstimate();

        // Allow 2% error for 1M values
        final double tolerance = expectedCount * 0.01;
        Assert.assertEquals("Distinct count should be close to " + expectedCount, expectedCount, estimate, tolerance);
    }

    /**
     * Mimics production URLs with UUIDs
     */
    @Test
    public void testMimicsProductionUrls()
    {
        final HllSketch sketch = new HllSketch(LOG_K, TgtHllType.HLL_8);

        final int expectedCount = 87_151;

        for (int i = 0; i < expectedCount; i++)
        {
            String url = "https://anytestserver-test-anywhere.com/acmecorp/proxy/api/shopper/shopper-context/v1/organizations/f_iuqz_abcd_prd/shopper-context/" +
                         UUID.randomUUID().toString() + "?siteId=SiteWithAName";
            sketch.update(url.hashCode());
        }

        final double estimate = sketch.getEstimate();

        // Allow 1% error for 87k values
        final double tolerance = expectedCount * 0.01;
        Assert.assertEquals("Distinct count should be close to " + expectedCount, expectedCount, estimate, tolerance);
    }
}
