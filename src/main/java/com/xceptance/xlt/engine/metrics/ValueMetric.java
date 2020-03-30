/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.metrics;

/**
 * A metric for fluctuating values. Maintains the minimum, the maximum, and the mean value calculated from all values
 * added.
 * <p>
 * Note: This class is thread-safe.
 */
public class ValueMetric implements Metric
{
    /**
     * The current internal state.
     */
    private Snapshot snapshot = new Snapshot();

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void update(final int value)
    {
        snapshot.update(value);
    }

    /**
     * Returns a snapshot of the current internal state and resets the internal state.
     */
    public synchronized Snapshot getSnapshotAndClear()
    {
        final Snapshot oldSnapshot = snapshot;
        snapshot = new Snapshot();

        return oldSnapshot;
    }

    /**
     * Represents the current internal state.
     */
    public static final class Snapshot
    {
        /**
         * The number of values added.
         */
        private int count;

        /**
         * The maximum value.
         */
        private int maximum = Integer.MIN_VALUE;

        /**
         * The minimum value.
         */
        private int minimum = Integer.MAX_VALUE;

        /**
         * The sum of all values.
         */
        private long sum;

        /**
         * Returns the number of values added.
         *
         * @return the number of values
         */
        public long getCount()
        {
            return count;
        }

        /**
         * Returns the maximum of the values added.
         *
         * @return the maximum
         */
        public int getMaximum()
        {
            return (count == 0) ? 0 : maximum;
        }

        /**
         * Returns the mean of the values added.
         *
         * @return the mean
         */
        public double getMean()
        {
            return (count == 0) ? 0.0 : (double) sum / (double) count;
        }

        /**
         * Returns the minimum of the values added.
         *
         * @return the minimum
         */
        public int getMinimum()
        {
            return (count == 0) ? 0 : minimum;
        }

        /**
         * Returns the sum of all values added.
         *
         * @return the sum
         */
        public long getSum()
        {
            return sum;
        }

        /**
         * Updates the internal state with the given value.
         *
         * @param value
         *            the value
         */
        private void update(final int value)
        {
            count++;

            if (value > maximum)
            {
                maximum = value;
            }

            if (value < minimum)
            {
                minimum = value;
            }

            sum += value;
        }
    }
}
