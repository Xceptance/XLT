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
package com.xceptance.xlt.report.util;

import com.xceptance.common.util.ParameterCheckUtils;

/**
 * Value set for {@link DoubleMinMaxValue}s.
 * 
 * @see IntMinMaxValueSet
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class DoubleMinMaxValueSet
{
    /**
     * The default initial value set size.
     */
    public static final int DEFAULT_SIZE = 1024;

    /**
     * The smallest time [s] for which a min/max value exists.
     */
    private int firstSecond;

    /**
     * The biggest time [s] for which a min/max value exists.
     */
    private int lastSecond;

    /**
     * The maximum time [ms] for which a value has been added to this set.
     */
    private long maximumTime;

    /**
     * The minimum time [ms] for which a value has been added to this set.
     */
    private long minimumTime;

    /**
     * The number of seconds a single min/max value represents. Always a power of 2.
     */
    private int scale = 1;

    /**
     * The number of different min/max values that can be stored in this value set.
     */
    private final int size;

    /**
     * The total number of values added to this value set.
     */
    private long valueCount;

    /**
     * The min/max values maintained by this value set.
     */
    private final DoubleMinMaxValue[] values;

    /**
     * Creates a {@link DoubleMinMaxValueSet} instance with a size of {@link #DEFAULT_SIZE}.
     */
    public DoubleMinMaxValueSet()
    {
        this(DEFAULT_SIZE);
    }

    /**
     * Creates a {@link DoubleMinMaxValueSet} instance with the specified size.
     * 
     * @param size
     *            the size
     */
    public DoubleMinMaxValueSet(final int size)
    {
        ParameterCheckUtils.isGreaterThan(size, 0, "size");

        // double the size to have at least size min/max values even after shrinking
        this.size = size * 2;
        values = new DoubleMinMaxValue[this.size];
    }

    /**
     * Adds a value for a certain time-stamp to this value set.
     * 
     * @param time
     *            the time-stamp
     * @param value
     *            the value
     */
    public void addOrUpdateValue(final long time, final double value)
    {
        // get the corresponding second
        int second = ((int) (time / 1000)) & ~(scale - 1);

        // check whether this is the first value added
        if (valueCount == 0)
        {
            // yes, that's easy
            firstSecond = lastSecond = second;
            values[0] = new DoubleMinMaxValue(value);

            // maintain statistics
            minimumTime = maximumTime = time;
            valueCount = 1;
        }
        else
        {
            // no, there are values in the set already

            // check whether we have to shrink the value set first
            if (second != firstSecond)
            {
                // decide on the way of shrinking
                if (second > firstSecond)
                {
                    // repeat as long as the second falls after the current size
                    while ((second - firstSecond) / scale >= size)
                    {
                        scale = scale * 2;

                        shrink();

                        second = second & ~(scale - 1);
                        firstSecond = firstSecond & ~(scale - 1);
                        lastSecond = lastSecond & ~(scale - 1);
                    }

                    // maintain upper boundary
                    if (second > lastSecond)
                    {
                        lastSecond = second;
                    }
                }
                else
                {
                    // repeat as long as the second still falls outside (before) the current size
                    while ((lastSecond - second) / scale >= size)
                    {
                        scale = scale * 2;

                        shrink();

                        second = second & ~(scale - 1);
                        firstSecond = firstSecond & ~(scale - 1);
                        lastSecond = lastSecond & ~(scale - 1);
                    }

                    // shift if necessary
                    if (second < firstSecond)
                    {
                        final int indexDiff = (firstSecond - second) / scale;

                        shift(indexDiff);

                        // maintain lower boundary
                        firstSecond = second;
                    }
                }
            }

            // calculate final index and update value
            final int index = (second - firstSecond) / scale;
            final DoubleMinMaxValue item = values[index];
            if (item == null)
            {
                values[index] = new DoubleMinMaxValue(value);
            }
            else
            {
                item.updateValue(value);
            }

            // maintain statistics
            valueCount++;

            if (time < minimumTime)
            {
                minimumTime = time;
            }

            if (time > maximumTime)
            {
                maximumTime = time;
            }
        }
    }

    /**
     * Returns the smallest second for which a min/max value exists in this set.
     * 
     * @return the first second
     */
    public long getFirstSecond()
    {
        if (valueCount == 0)
        {
            throw new IllegalStateException("No first second available as no values have been added so far.");
        }

        return firstSecond * 1000;
    }

    /**
     * Returns the maximum time [ms] for which a value has been added to this set.
     * 
     * @return the maximum time
     */
    public long getMaximumTime()
    {
        if (valueCount == 0)
        {
            throw new IllegalStateException("No maximum time available as no values have been added so far.");
        }

        return maximumTime;
    }

    /**
     * Returns the minimum time [ms] for which a value has been added to this set.
     * 
     * @return the minimum time
     */
    public long getMinimumTime()
    {
        if (valueCount == 0)
        {
            throw new IllegalStateException("No minimum time available as no values have been added so far.");
        }

        return minimumTime;
    }

    /**
     * Returns the number of seconds a single min/max value represents. Always a power of 2.
     * 
     * @return the scale
     */
    public int getScale()
    {
        return scale;
    }

    /**
     */
    public int getSize()
    {
        return size / 2;
    }

    /**
     * Returns the total number of values added to this value set.
     * 
     * @return the number of values
     */
    public long getValueCount()
    {
        return valueCount;
    }

    /**
     * Returns the min/max values maintained by this set.
     * 
     * @return the min/max values
     */
    public DoubleMinMaxValue[] getValues()
    {
        DoubleMinMaxValue[] copy;

        if (valueCount == 0)
        {
            copy = new DoubleMinMaxValue[0];
        }
        else
        {
            final int length = (lastSecond - firstSecond) / scale + 1;
            copy = new DoubleMinMaxValue[length];

            System.arraycopy(values, 0, copy, 0, length);
        }

        return copy;
    }

    /**
     * Shifts the content of the min/max value array to the right for the specified number of elements.
     * 
     * @param indexDiff
     *            the destination index
     */
    private void shift(final int indexDiff)
    {
        System.arraycopy(values, 0, values, indexDiff, size - indexDiff);

        for (int i = 0; i < indexDiff; i++)
        {
            values[i] = null;
        }
    }

    /**
     * Shrinks the min/max value array by merging two consecutive min/max values into one value.
     */
    private void shrink()
    {
        final int offset = (firstSecond % scale > 0) ? 1 : 0;

        // loop over value pairs, skipping the first value if necessary
        int i, j;
        for (i = offset, j = offset; i < size - 1; i = i + 2, j++)
        {
            final DoubleMinMaxValue v1 = values[i];
            final DoubleMinMaxValue v2 = values[i + 1];
            DoubleMinMaxValue rv;

            if (v1 != null && v2 != null)
            {
                rv = v1.merge(v2);
            }
            else if (v1 != null && v2 == null)
            {
                rv = v1;
            }
            else if (v1 == null && v2 != null)
            {
                rv = v2;
            }
            else
            {
                rv = null;
            }

            // clear the old values
            values[i] = null;
            values[i + 1] = null;

            // set the new value
            values[j] = rv;
        }

        // check whether we have one last entry left to deal with
        if (i < size)
        {
            values[j] = values[i];
            values[i] = null;
        }
    }
    
    /**
     * A {@link DoubleMinMaxValue} stores the minimum/maximum/sum/count of all the sample values added, but can also
     * reproduce a rough approximation of the distinct values added.
     * 
     * @see IntMinMaxValue
     * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
     */
    public static class DoubleMinMaxValue
    {
        private double accumulatedValue;

        private double maximum;

        private double minimum;

        private int valueCount;

        /**
         * Holds an approximation of the distinct values added to this min-max value.
         */
        private final DoubleLowPrecisionValueSet valueSet = new DoubleLowPrecisionValueSet();

        /**
         * Constructor.
         * 
         * @param value
         *            the first value to add
         */
        public DoubleMinMaxValue(final double value)
        {
            accumulatedValue = value;
            maximum = value;
            minimum = value;
            valueCount = 1;

            valueSet.addValue(value);
        }

        /**
         * @return the accumulatedValue
         */
        public double getAccumulatedValue()
        {
            return accumulatedValue;
        }

        /**
         * @return the average value
         */
        public double getAverageValue()
        {
            return accumulatedValue / valueCount;
        }

        /**
         * @return the maximum
         */
        public double getMaximumValue()
        {
            return maximum;
        }

        /**
         * @return the minimum
         */
        public double getMinimumValue()
        {
            return minimum;
        }

        /**
         * @return the average value
         */
        public double getValue()
        {
            return getAverageValue();
        }

        /**
         * Returns an approximation of the distinct values added to this min-max value.
         * 
         * @return the values
         */
        public double[] getValues()
        {
            return valueSet.getValues();
        }

        /**
         * @return the valueCount
         */
        public int getValueCount()
        {
            return valueCount;
        }

        /**
         * @param item
         */
        DoubleMinMaxValue merge(final DoubleMinMaxValue item)
        {
            if (item != null)
            {
                maximum = Math.max(maximum, item.maximum);
                minimum = Math.min(minimum, item.minimum);
                accumulatedValue += item.accumulatedValue;
                valueCount += item.valueCount;

                valueSet.merge(item.valueSet);
            }

            return this;
        }

        /**
         */
        @Override
        public String toString()
        {
            return "" + getValue() + "/" + getAccumulatedValue() + "/" + getMinimumValue() + "/" + getMaximumValue() + "/" + getValueCount();
        }

        /**
         * @param sample
         */
        public void updateValue(final double sample)
        {
            if (sample > maximum)
            {
                maximum = sample;
            }
            else if (sample < minimum)
            {
                minimum = sample;
            }

            accumulatedValue += sample;
            valueCount++;

            valueSet.addValue(sample);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            long temp;
            temp = Double.doubleToLongBits(accumulatedValue);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(maximum);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(minimum);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + valueCount;
            result = prime * result + ((valueSet == null) ? 0 : valueSet.hashCode());
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj)
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
            DoubleMinMaxValue other = (DoubleMinMaxValue) obj;
            if (Double.doubleToLongBits(accumulatedValue) != Double.doubleToLongBits(other.accumulatedValue))
            {
                return false;
            }
            if (Double.doubleToLongBits(maximum) != Double.doubleToLongBits(other.maximum))
            {
                return false;
            }
            if (Double.doubleToLongBits(minimum) != Double.doubleToLongBits(other.minimum))
            {
                return false;
            }
            if (valueCount != other.valueCount)
            {
                return false;
            }
            if (valueSet == null)
            {
                if (other.valueSet != null)
                {
                    return false;
                }
            }
            else if (!valueSet.equals(other.valueSet))
            {
                return false;
            }
            return true;
        }
    }
}
