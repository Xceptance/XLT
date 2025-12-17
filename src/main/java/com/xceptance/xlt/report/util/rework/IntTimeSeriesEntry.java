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
package com.xceptance.xlt.report.util.rework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.xceptance.xlt.report.util.misc.BitCompression;

/**
 * A {@link IntTimeSeriesEntry} stores the minimum/maximum/sum/count of all the sample values added, but can also reproduce a
 * rough approximation of the distinct values added.
 */
public class IntTimeSeriesEntry
{
    // The sum for later average calculation
    private long totalValue = 0;

    // how much data have we seen
    private int count = 0;

    // how much concurrent measurements do we truly have
    private int concurrentCount = 0;
    
    // how many error have we seen
    private int errorCount = 0;
    
    // the min and max values seen
    private int maximum = Integer.MIN_VALUE;
    private int minimum = Integer.MAX_VALUE;

    /**
     * Holds an approximation of the distinct values added to this min-max value.
     * This was LowPrecisionIntValueSet before. We moved it here for less memory consumption
     * and better performance. We also limit it to 128 distinct values. We used 256 before
     * but wee we started with XLT it was 128 for a long time.
     */
    private long distinctValuesLow = 0;
    private long distinctValuesHigh = 0;

    /**
     * The value scaling factor. 2 pow scale.
     */
    private int distinctValuesScale = 0;

    /**
     * Constructor.
     */
    public IntTimeSeriesEntry()
    {
    }

    /**
     * Constructor.
     * 
     * @param value
     *            the first value to add
     */
    public IntTimeSeriesEntry(final int firstValue, final boolean failed)
    {
        updateValue(firstValue, failed);
    }

    /**
     * Adds the given sample value to this min-max value.
     * 
     * @param value
     *            the sample to add
     */
    public void updateValue(final int value, final boolean failed)
    {
        var v = value < 0 ? 0 : value;

        this.count++;
        this.errorCount += failed ? 1 : 0;
        this.totalValue += v;
        this.concurrentCount++;

        // use explicit comparisons to avoid Math.max/Math.min calls
        // and writing the values when not necessary
        if (v > this.maximum)
        {
            this.maximum = v;
        }
        if (value < this.minimum)
        {
            this.minimum = v;
        }
        
        v = scaleIfNeeded(v);
        
        // set distinct values
        if (v < 64)
        {
            distinctValuesLow |= (1L << v);
        }
        else
        {
            distinctValuesHigh |= (1L << (v - 64));
        }
    }

    /**
     * Increase the concurrency count by one for this slot.
     */
    public void updateConcurrency()
    {
        this.concurrentCount++;
    }
    
    /**
     * Adds a value to this set, we support only positive values.
     * 
     * @param value
     *            the value
     * @return the scaled value
     */
    private int scaleIfNeeded(final int value)
    {
        // adjust the value according to the current scale
        var v  = value >> distinctValuesScale; // div

        // make the value fit into the bit set by scaling the bit set as necessary
        while (v >= 128)
        {
            // we join adjacent buckets by increasing joining the bits adjacent bits
            // this means we lose precision, but we can still
            var l = distinctValuesLow = BitCompression.combineAdjacentBits(distinctValuesLow);
            l = BitCompression.compressAndShiftOddBits(l);

            var h = BitCompression.combineAdjacentBits(distinctValuesHigh);
            h = BitCompression.compressAndShiftOddBits(h);

            // join them in low
            distinctValuesLow = l | (h << 32);
            // clear high part  
            distinctValuesHigh = 0;

            // increase scale and try again
            v = value >> (++distinctValuesScale); 
        }

        return v;
    }

    /**
     * Returns the sum of the values added to this min-max value.
     * 
     * @return the accumulated value
     */
    public long getTotalValue()
    {
        return this.totalValue;
    }

    /**
     * Returns the average of the values added to this min-max value.
     * 
     * @return the average value
     */
    public int getAverageValue()
    {
        return this.count == 0 ? 0 : (int) (this.totalValue / this.count);

    }

    /**
     * Returns the maximum of the values added to this min-max value.
     * 
     * @return the maximum value
     */
    public int getMaximumValue()
    {
        return maximum == Integer.MIN_VALUE ? 0 : maximum;
    }

    /**
     * Returns the minimum of the values added to this min-max value.
     * 
     * @return the minimum value
     */
    public int getMinimumValue()
    {
        return minimum == Integer.MAX_VALUE ? 0 : minimum;
    }

    /**
     * Returns the number of errors added to this min-max value.
     */
    public int getErrorCount()
    {
        return this.errorCount;
    }
    
    /**
     * Returns an approximation of the distinct values added to this min-max value.
     * 
     * @return the values
     */
    public double[] getValues()
    {
        final List<Double> values = new ArrayList<>(128);

        for (int i = 0; i < 128; i++) 
        {
            final boolean set = (i < 64) ? ((distinctValuesLow & (1L << i)) != 0) 
                                         : ((distinctValuesHigh & (1L << (i - 64))) != 0);
            final double v = (1L << distinctValuesScale ) * i; 
            if (set)
            {
                values.add(v);
            }
        }

        return values.stream().mapToDouble(Double::doubleValue).toArray();
    }

    /**
     * Returns the number of values added 
     * 
     * @return the count
     */
    public int getCount()
    {
        return this.count;
    }

    /**
     * Returns the number of concurrent measurements added
     * 
     * @return the count
     */
    public int getConcurrentCount()
    {
        return this.concurrentCount;
    }
    
    /**
     * Merges the data. Attention: This might modify the given item
     * because we have to scale it to our scale or vice versa.
     * 
     * @param item
     *            the other value
     */
    public IntTimeSeriesEntry merge(final IntTimeSeriesEntry item)
    {
        // we can only merge the same scale
        if (item.distinctValuesScale > this.distinctValuesScale)
        {
            // we need to scale up our values
            while (item.distinctValuesScale > this.distinctValuesScale)
            {
                distinctValuesLow = BitCompression.combineAdjacentBits(distinctValuesLow);
                distinctValuesLow = BitCompression.compressAndShiftOddBits(distinctValuesLow);

                distinctValuesHigh = BitCompression.combineAdjacentBits(distinctValuesHigh);
                distinctValuesHigh = BitCompression.compressAndShiftOddBits(distinctValuesHigh);

                // increase scale
                distinctValuesScale++;
            }
        }
        else if (item.distinctValuesScale < this.distinctValuesScale)
        {
            // we need to scale up the other values
            while (item.distinctValuesScale < this.distinctValuesScale)
            {
                item.distinctValuesLow = BitCompression.combineAdjacentBits(item.distinctValuesLow);
                item.distinctValuesLow = BitCompression.compressAndShiftOddBits(item.distinctValuesLow);

                item.distinctValuesHigh = BitCompression.combineAdjacentBits(item.distinctValuesHigh);
                item.distinctValuesHigh = BitCompression.compressAndShiftOddBits(item.distinctValuesHigh);

                // increase scale
                item.distinctValuesScale++;
            }
        }
        
        maximum = Math.max(maximum, item.maximum);
        minimum = Math.min(minimum, item.minimum);

        totalValue += item.totalValue;
        count += item.count;
        // because we count concurrency per entry, we have to take the maximum here
        // and cannot sum them up
        concurrentCount = Math.max(concurrentCount, item.concurrentCount);
        errorCount += item.errorCount;

        distinctValuesLow |= item.distinctValuesLow;
        distinctValuesHigh |= item.distinctValuesHigh;

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        // that is only for debugging
        return 
            getCount() + " / " +
            getConcurrentCount() + " / " +
            getErrorCount() + " / " +
            getTotalValue() + " / " +
            getAverageValue() + " / " +
            getMinimumValue() + " / " + 
            getMaximumValue() + " / " + 
            Arrays.toString(getValues()) + "\n";
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final IntTimeSeriesEntry other = (IntTimeSeriesEntry) obj;
        if (count != other.count)
        {
            return false;
        }
        if (maximum != other.maximum)
        {
            return false;
        }
        if (minimum != other.minimum)
        {
            return false;
        }
        if (totalValue != other.totalValue)
        {
            return false;
        }
        if (errorCount != other.errorCount)
        {
            return false;
        }
        if (distinctValuesLow != other.distinctValuesLow)
        {
            return false;
        }
        if (distinctValuesHigh != other.distinctValuesHigh)
        {
            return false;
        }
        if (concurrentCount != other.concurrentCount)
        {
            return false;
        }
        if (distinctValuesScale != other.distinctValuesScale)
        {
            return false;
        }
        return true;
    }
}
