/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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

/**
 * A {@link MinMaxValue} stores the minimum/maximum/sum/count of all the sample values added, but can also reproduce a
 * rough approximation of the distinct values added.
 */
public class MinMaxValue
{
    private long accumulatedValue;

    private int maximum = Integer.MIN_VALUE;

    private int minimum = Integer.MAX_VALUE;

    private int valueCount;

    /**
     * Holds an approximation of the distinct values added to this min-max value.
     */
    private final LowPrecisionIntValueSet valueSet = new LowPrecisionIntValueSet();

    /**
     * Constructor.
     */
    public MinMaxValue()
    {
    }

    /**
     * Constructor.
     * 
     * @param value
     *            the first value to add
     */
    public MinMaxValue(final int value)
    {
        accumulatedValue = value;
        maximum = value;
        minimum = value;
        valueCount = 1;

        valueSet.addValue(value);
    }

    /**
     * Returns the sum of the values added to this min-max value.
     * 
     * @return the accumulated value
     */
    public long getAccumulatedValue()
    {
        return accumulatedValue;
    }

    /**
     * Returns the average of the values added to this min-max value.
     * 
     * @return the average value
     */
    public int getAverageValue()
    {
        if (valueCount > 0)
        {
            return (int) (accumulatedValue / valueCount);
        }
        else
        {
            return 0;
        }
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
     * Returns the average of the values added to this min-max value.
     * 
     * @return the average value
     */
    public int getValue()
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
     * Returns the number of values added to this min-max value.
     * 
     * @return the value count
     */
    public int getValueCount()
    {
        return valueCount;
    }

    /**
     * Merges the data of the given min-max value into this min-max value.
     * 
     * @param item
     *            the other value
     */
    MinMaxValue merge(final MinMaxValue item)
    {
        if (item.getValueCount() > 0)
        {
            // only, if we already have counted something
            maximum = Math.max(maximum, item.maximum);
            minimum = Math.min(minimum, item.minimum);

            accumulatedValue += item.accumulatedValue;
            valueCount += item.valueCount;

            valueSet.merge(item.valueSet);
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "" + getValue() + "/" + getAccumulatedValue() + "/" + getMinimumValue() + "/" + getMaximumValue() + "/" + getValueCount();
    }

    /**
     * Adds the given sample value to this min-max value.
     * 
     * @param sample
     *            the sample to add
     */
    public void updateValue(final int sample)
    {
        // did we counted at all already?
        if (valueCount > 0)
        {
            if (sample > maximum)
            {
                maximum = sample;
            }
            else if (sample < minimum)
            {
                minimum = sample;
            }
        }
        else
        {
            maximum = sample;
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
        final MinMaxValue other = (MinMaxValue) obj;
        if (accumulatedValue != other.accumulatedValue)
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
        if (maximum != other.maximum)
        {
            return false;
        }
        if (minimum != other.minimum)
        {
            return false;
        }
        if (valueCount != other.valueCount)
        {
            return false;
        }
        return true;
    }
}
