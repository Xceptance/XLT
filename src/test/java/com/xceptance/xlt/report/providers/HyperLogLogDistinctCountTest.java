package com.xceptance.xlt.report.providers;

import org.junit.Assert;
import org.junit.Test;

import com.dynatrace.hash4j.distinctcount.HyperLogLog;
import com.dynatrace.hash4j.hashing.Hashing;

/**
 * Tests for the HyperLogLog-based distinct URL counting functionality. RequestDataProcessor uses:
 * HyperLogLog.create(21) It uses Hashing.wyhashFinal4().hashCharsToLong() to hash the URL characters.
 */
public class HyperLogLogDistinctCountTest
{
    // Same configuration as RequestDataProcessor
    private static final int PRECISION = 21;

    /**
     * Helper to match production hashing logic for integers (converted to String)
     */
    private long hash(int i)
    {
        return Hashing.wyhashFinal3().hashCharsToLong(String.valueOf(i));
    }

    /**
     * Helper to match production hashing logic for Strings
     */
    private long hash(String s)
    {
        return Hashing.wyhashFinal3().hashCharsToLong(s);
    }

    /**
     * Tests that HyperLogLog correctly counts a small number of distinct values.
     */
    @Test
    public void testSmallDistinctCount()
    {
        final HyperLogLog sketch = HyperLogLog.create(PRECISION);

        // Add 10 distinct hash values
        for (int i = 1; i <= 10; i++)
        {
            sketch.add(hash(i));
        }

        final double estimate = sketch.getDistinctCountEstimate();

        // For small counts, HyperLogLog should be very accurate
        Assert.assertEquals("Distinct count should be close to 10", 10.0, estimate, 1.0);
    }

    /**
     * Tests that adding duplicate values does not increase the count.
     */
    @Test
    public void testDuplicatesDoNotIncreaseCount()
    {
        final HyperLogLog sketch = HyperLogLog.create(PRECISION);

        // Add the same 5 values multiple times
        for (int repeat = 0; repeat < 100; repeat++)
        {
            for (int i = 1; i <= 5; i++)
            {
                sketch.add(hash(i));
            }
        }

        final double estimate = sketch.getDistinctCountEstimate();

        // Should still be close to 5
        Assert.assertEquals("Duplicates should not increase count", 5.0, estimate, 1.0);
    }

    /**
     * Tests that HyperLogLog correctly estimates a larger number of distinct values.
     */
    @Test
    public void testLargeDistinctCount()
    {
        final HyperLogLog sketch = HyperLogLog.create(PRECISION);

        final int expectedCount = 100_000;

        for (int i = 1; i <= expectedCount; i++)
        {
            sketch.add(hash(i));
        }

        final double estimate = sketch.getDistinctCountEstimate();

        // Allow 1% error for 100k values
        final double tolerance = expectedCount * 0.01;
        Assert.assertEquals("Distinct count should be close to " + expectedCount, expectedCount, estimate, tolerance);
    }

    /**
     * Tests that HyperLogLog works correctly with URL-style strings.
     */
    @Test
    public void testWithUrlStrings()
    {
        final HyperLogLog sketch = HyperLogLog.create(PRECISION);

        String url1 = "http://example.com/page1";
        String url2 = "http://example.com/page2";
        String url3 = "http://example.com/page3";

        sketch.add(hash(url1));
        sketch.add(hash(url2));
        sketch.add(hash(url3));
        sketch.add(hash(url1)); // duplicate

        final double estimate = sketch.getDistinctCountEstimate();

        // Should be 3 distinct URLs
        Assert.assertEquals("Should count 3 distinct URLs", 3.0, estimate, 1.0);
    }

    /**
     * Tests that HyperLogLog handles a mix of positive and negative hash values (simulated by Strings).
     */
    @Test
    public void testMixedValues()
    {
        final HyperLogLog sketch = HyperLogLog.create(PRECISION);

        // Add values
        sketch.add(hash(1));
        sketch.add(hash(-1));
        sketch.add(hash(Integer.MAX_VALUE));
        sketch.add(hash(Integer.MIN_VALUE));
        sketch.add(hash(0));

        final double estimate = sketch.getDistinctCountEstimate();

        // Should count 5 distinct values
        Assert.assertEquals("Should count 5 distinct values", 5.0, estimate, 1.0);
    }

    /**
     * Tests realistic URL distribution with duplicates.
     */
    @Test
    public void testRealisticUrlScenario()
    {
        final HyperLogLog sketch = HyperLogLog.create(PRECISION);

        // Simulate a realistic scenario: 1000 unique URLs, each hit 10 times
        final int uniqueUrls = 1000;
        final int hitsPerUrl = 10;

        for (int hit = 0; hit < hitsPerUrl; hit++)
        {
            for (int urlId = 0; urlId < uniqueUrls; urlId++)
            {
                String url = "http://example.com/page/" + urlId;
                sketch.add(hash(url));
            }
        }

        final double estimate = sketch.getDistinctCountEstimate();

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
        final HyperLogLog sketch = HyperLogLog.create(PRECISION);

        for (int i = 1; i <= 1000; i++)
        {
            sketch.add(hash(i));
        }

        // This is how RequestDataProcessor uses it
        final int intEstimate = (int) sketch.getDistinctCountEstimate();

        // Should be close to 1000
        Assert.assertTrue("Int cast should be close to 1000, was: " + intEstimate, intEstimate >= 980 && intEstimate <= 1020);
    }

    /**
     * Tests very large distinct count (simulating heavy load test).
     */
    @Test
    public void testVeryLargeDistinctCount()
    {
        final HyperLogLog sketch = HyperLogLog.create(PRECISION);

        final int expectedCount = 1_000_000;

        for (int i = 0; i < expectedCount; i++)
        {
            String url = "http://example.com/product/?r=" + i;
            sketch.add(hash(url));
        }

        final double estimate = sketch.getDistinctCountEstimate();

        // Allow 1% error for 1M values
        final double tolerance = expectedCount * 0.01;
        Assert.assertEquals("Distinct count should be close to " + expectedCount, expectedCount, estimate, tolerance);
    }
}
