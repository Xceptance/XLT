/*
 * Copyright 2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.util.rework;

import java.util.Arrays;

import it.unimi.dsi.util.FastRandom;

/**
 * Implements reservoir sampling to maintain a fixed-size random sample of a stream of values.
 * Provides quantile queries and estimates of value distribution across quantile ranges.
 * 
 * Initial version created by CoPilot Claude Sonnet 4.5, reviewed fully manually.
 */
public class ReservoirSampling
{
    /** Maximum number of samples to maintain in the reservoir */
    private final int reservoirSize;

    /** The actual reservoir storing sampled values */
    private final int[] reservoir;

    /** Total number of values processed (including those not in reservoir) */
    private int totalCount;

    /** Random number generator for sampling decisions, we use our own unsynchronized fast version */
    private final FastRandom random;

    /**
     * Creates a new reservoir sampling instance. The seed ensures reproducible results
     * for testing. This is likely not needed in production use.
     *
     * @param reservoirSize the maximum number of samples to maintain
     * @param seed          the seed for the random number generator
     */
    public ReservoirSampling(final int reservoirSize, final long seed)
    {
        this.reservoirSize = reservoirSize;
        this.reservoir = new int[reservoirSize];
        this.totalCount = 0;
        this.random = FastRandom.get(seed);
    }

    /**
     * Creates a new reservoir sampling instance.
     *
     * @param reservoirSize the maximum number of samples to maintain
     * @param seed          the seed for the random number generator
     */
    public ReservoirSampling(final int reservoirSize)
    {
        this(reservoirSize, System.currentTimeMillis());
    }

    /**
     * Adds a value to the stream. Uses reservoir sampling algorithm to decide
     * whether to include it in the sample.
     *
     * @param value the value to add
     */
    public void add(final int value)
    {
        this.totalCount++;

        // Fill reservoir until it reaches capacity
        if (this.totalCount <= this.reservoirSize)
        {
            this.reservoir[this.totalCount - 1] = value;
        }
        else
        {
            // Replace existing value with probability reservoirSize/totalCount
            final int randomIndex = this.random.nextInt(this.totalCount);
            if (randomIndex < this.reservoirSize)
            {
                this.reservoir[randomIndex] = value;
            }
        }
    }

    /**
     * Calculates the quantile using linear interpolation.
     * 
     * @param percentile The desired percentile (e.g., 25 for Q1, 50 for Median)
     * 
     * @return The calculated value
     * 
     * @throws IllegalStateException if no data has been added
     * @throws IllegalArgumentException if p is not between 0 and 1
     */
    public int getQuantile(final double percentile) 
    {
        if (totalCount == 0)
        {
            throw new IllegalStateException("No data in reservoir");
        }
        if (percentile < 0.0 || percentile > 1.0)
        {
            throw new IllegalArgumentException("Quantile must be between 0 and 1");
        }
        
        // Sort reservoir to calculate quantile
        // we call that at the end
        final int[] sorted = Arrays.copyOf(reservoir, totalCount < this.reservoir.length ? totalCount : this.reservoir.length);
        Arrays.sort(sorted);

        // 1. Calculate the index in the sorted array
        // Formula: index = (N - 1) * p
        double index = (sorted.length - 1) * percentile;

        // 2. Split index into integer and decimal parts
        int lowerIndex = (int) index;
        double fraction = index - lowerIndex;

        // 3. Handle the edge case where the index is exactly the last element
        if (lowerIndex + 1 >= sorted.length) 
        {
            return sorted[lowerIndex];
        }

        // 4. Linear Interpolation
        // Value = LowerValue + (Difference * fraction)
        double lowerValue = sorted[lowerIndex];
        double upperValue = sorted[lowerIndex + 1];

        return (int) (lowerValue + (upperValue - lowerValue) * fraction);
    }
    
    /**
     * Estimates the number of values that fall within a quantile range.
     *
     * @param minQuantile the lower bound quantile (0.0 to 1.0)
     * @param maxQuantile the upper bound quantile (0.0 to 1.0)
     * @return estimated count of values in the specified quantile range
     * @throws IllegalArgumentException if quantiles are invalid or out of range
     */
    public long estimateCountInRange(final double minQuantile, final double maxQuantile)
    {
        if (minQuantile < 0 || minQuantile > 1 || maxQuantile < 0 || maxQuantile > 1)
        {
            throw new IllegalArgumentException("Quantiles must be between 0 and 1");
        }
        if (minQuantile > maxQuantile)
        {
            throw new IllegalArgumentException("minQuantile must be <= maxQuantile");
        }

        // Estimate based on the fraction of the quantile range
        final double rangeFraction = maxQuantile - minQuantile;

        return Math.round(totalCount * rangeFraction);
    }

    /**
     * Returns the total number of values processed.
     *
     * @return the total count of values added to the stream
     */
    public long getTotalCount()
    {
        return totalCount;
    }

    /**
     * Returns the current number of samples in the reservoir.
     *
     * @return the number of samples currently stored
     */
    public int getReservoirSize()
    {
        return totalCount < reservoirSize ? totalCount : reservoirSize;
    }
}
