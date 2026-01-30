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

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import net.agkn.hll.HLL;

/**
 * Tests for the HyperLogLog-based distinct URL counting functionality used in {@link RequestDataProcessor}. This tests
 * the current java-hll (net.agkn:hll) library behavior to ensure any future library migration maintains the same
 * behavior. RequestDataProcessor uses: new HLL(21, 5) with addRaw() and cardinality()
 */
public class HLLDistinctCountTest
{
    // Same configuration as RequestDataProcessor
    private static final int LOG2M = 21;

    private static final int REGISTER_WIDTH = 5;

    /**
     * Tests that HLL correctly counts a small number of distinct values.
     */
    @Test
    public void testSmallDistinctCount()
    {
        final HLL hll = new HLL(LOG2M, REGISTER_WIDTH);

        // Add 10 distinct hash values
        for (int i = 1; i <= 10; i++)
        {
            hll.addRaw(i);
        }

        final long estimate = hll.cardinality();

        // For small counts, HLL should be very accurate
        Assert.assertTrue("Distinct count should be close to 10, was: " + estimate, estimate >= 9 && estimate <= 11);
    }

    /**
     * Tests that adding duplicate values does not increase the count. This verifies the idempotency property of HLL.
     */
    @Test
    public void testDuplicatesDoNotIncreaseCount()
    {
        final HLL hll = new HLL(LOG2M, REGISTER_WIDTH);

        // Add the same 5 values multiple times
        for (int repeat = 0; repeat < 100; repeat++)
        {
            for (int i = 1; i <= 5; i++)
            {
                hll.addRaw(i);
            }
        }

        final long estimate = hll.cardinality();

        // Should still be close to 5, not 500
        Assert.assertTrue("Duplicates should not increase count, was: " + estimate, estimate >= 4 && estimate <= 6);
    }

    /**
     * Tests that HLL correctly estimates a larger number of distinct values.
     */
    @Test
    public void testLargeDistinctCount()
    {
        final HLL hll = new HLL(LOG2M, REGISTER_WIDTH);

        final int expectedCount = 100_000;

        for (int i = 1; i <= expectedCount; i++)
        {
            hll.addRaw(i);
        }

        final long estimate = hll.cardinality();

        // Allow 1% error for 100k values
        final double tolerance = expectedCount * 0.01;
        Assert.assertEquals("Distinct count should be close to " + expectedCount, expectedCount, estimate, tolerance);
    }

    /**
     * Tests that HLL works correctly with URL-style hash codes, which can be negative values.
     */
    @Test
    public void testWithUrlHashCodes()
    {
        final HLL hll = new HLL(LOG2M, REGISTER_WIDTH);

        // Simulate URL hash codes (can be negative)
        int hash1 = "http://example.com/page1".hashCode();
        int hash2 = "http://example.com/page2".hashCode();
        int hash3 = "http://example.com/page3".hashCode();

        hll.addRaw(hash1);
        hll.addRaw(hash2);
        hll.addRaw(hash3);
        hll.addRaw(hash1); // duplicate

        final long estimate = hll.cardinality();

        // Should be 3 distinct URLs
        Assert.assertEquals("Should count 3 distinct URLs", 3, estimate);
    }

    /**
     * Tests that HLL handles a mix of positive and negative hash values.
     */
    @Test
    public void testMixedPositiveNegativeHashes()
    {
        final HLL hll = new HLL(LOG2M, REGISTER_WIDTH);

        // Add positive and negative hash values
        hll.addRaw(1);
        hll.addRaw(-1);
        hll.addRaw(Integer.MAX_VALUE);
        hll.addRaw(Integer.MIN_VALUE);
        hll.addRaw(0);

        final long estimate = hll.cardinality();

        // Should count 5 distinct values
        Assert.assertTrue("Should count 5 distinct values, was: " + estimate, estimate >= 4 && estimate <= 6);
    }

    /**
     * Tests realistic URL hash distribution with duplicates.
     */
    @Test
    public void testRealisticUrlScenario()
    {
        final HLL hll = new HLL(LOG2M, REGISTER_WIDTH);

        // Simulate a realistic scenario: 1000 unique URLs, each hit 10 times
        final int uniqueUrls = 1000;
        final int hitsPerUrl = 10;

        for (int hit = 0; hit < hitsPerUrl; hit++)
        {
            for (int urlId = 0; urlId < uniqueUrls; urlId++)
            {
                // Use a hash that simulates URL hash patterns
                int hash = ("http://example.com/page/" + urlId).hashCode();
                hll.addRaw(hash);
            }
        }

        final long estimate = hll.cardinality();

        // Should estimate close to uniqueUrls (allow 2% tolerance)
        final double tolerance = uniqueUrls * 0.02;
        Assert.assertEquals("Should estimate close to unique URL count", uniqueUrls, estimate, tolerance);
    }

    /**
     * Tests that casting to int (as done in RequestDataProcessor) works correctly. RequestDataProcessor does: (int)
     * distinctUrlsHLL.cardinality()
     */
    @Test
    public void testCastToInt()
    {
        final HLL hll = new HLL(LOG2M, REGISTER_WIDTH);

        for (int i = 1; i <= 1000; i++)
        {
            hll.addRaw(i);
        }

        // This is how RequestDataProcessor uses it
        final int intEstimate = (int) hll.cardinality();

        // Should be close to 1000
        Assert.assertTrue("Int cast should be close to 1000, was: " + intEstimate, intEstimate >= 980 && intEstimate <= 1020);
    }

    /**
     * Tests very large distinct count (simulating heavy load test). Uses URL-style hashes which distribute better than
     * sequential integers.
     */
    @Test
    public void testVeryLargeDistinctCount()
    {
        final HLL hll = new HLL(21, 5);

        final int expectedCount = 1_000_000;

        // Use URL-style hashes for better distribution
        for (int i = 0; i < expectedCount; i++)
        {
            int intHash = ("http://example.com/product/?r=" + i * 17).hashCode();
            hll.addRaw(intHash);
        }

        final long estimate = hll.cardinality();

        // Allow 1% error for 1M values
        final double tolerance = expectedCount * 0.01;
        Assert.assertEquals("Distinct count should be close to " + expectedCount, expectedCount, estimate, tolerance);
    }
}
