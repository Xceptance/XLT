/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package it.unimi.dsi.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test cases for the FastRandom class mainly to see if the distribution is uniform enough.
 * AI generated test using Claude Sonnet 4.5.
 */
public class FastRandomTest
{
    @Test
    public void testRandomDistribution()
    {
        final int bucketCount = 100;
        final int samplesPerBucket = 10_000;
        final int totalSamples = bucketCount * samplesPerBucket;

        FastRandom random = new FastRandom(42L);
        int[] buckets = new int[bucketCount];

        // Fill buckets with random numbers
        for (int i = 0; i < totalSamples; i++)
        {
            int value = random.nextInt(bucketCount);
            buckets[value]++;
        }

        // Calculate expected count per bucket and acceptable deviation
        double expectedPerBucket = (double) totalSamples / bucketCount;
        double maxDeviation = expectedPerBucket * 0.05; // 5% tolerance

        // Verify each bucket is within acceptable range
        for (int i = 0; i < bucketCount; i++)
        {
            double deviation = Math.abs(buckets[i] - expectedPerBucket);
            assertTrue("Bucket " + i + " deviation too large: " + deviation + " (expected ~" + expectedPerBucket + ")",
                       deviation <= maxDeviation);
        }

        // Chi-squared test for goodness of fit
        double chiSquared = 0.0;
        for (int i = 0; i < bucketCount; i++)
        {
            double diff = buckets[i] - expectedPerBucket;
            chiSquared += (diff * diff) / expectedPerBucket;
        }

        // For 99 degrees of freedom, critical value at p=0.01 is ~135
        assertTrue("Chi-squared test failed: " + chiSquared + " (should be < 135)", chiSquared < 135);
    }

    @Test
    public void testRandomDoubleDistribution()
    {
        final int bucketCount = 10;
        final int totalSamples = 100_000;

        FastRandom random = new FastRandom(123L);
        int[] buckets = new int[bucketCount];

        // Test nextDouble() distribution
        for (int i = 0; i < totalSamples; i++)
        {
            double value = random.nextDouble();
            assertTrue("Value out of range: " + value, value >= 0.0 && value < 1.0);

            int bucket = (int) (value * bucketCount);
            buckets[bucket]++;
        }

        // Verify uniform distribution
        double expectedPerBucket = (double) totalSamples / bucketCount;
        double maxDeviation = expectedPerBucket * 0.03; // 3% tolerance

        for (int i = 0; i < bucketCount; i++)
        {
            double deviation = Math.abs(buckets[i] - expectedPerBucket);
            assertTrue("Bucket " + i + " deviation: " + deviation, deviation <= maxDeviation);
        }
    }

    @Test
    public void testRandomBooleanDistribution()
    {
        final int totalSamples = 100_000;
        FastRandom random = new FastRandom(456L);

        int trueCount = 0;
        int falseCount = 0;

        for (int i = 0; i < totalSamples; i++)
        {
            if (random.nextBoolean())
            {
                trueCount++;
            }
            else
            {
                falseCount++;
            }
        }

        // Should be roughly 50/50
        double expected = totalSamples / 2.0;
        double maxDeviation = expected * 0.01; // 1% tolerance

        assertTrue("True count deviation too large", Math.abs(trueCount - expected) <= maxDeviation);
        assertTrue("False count deviation too large", Math.abs(falseCount - expected) <= maxDeviation);
    }

}
