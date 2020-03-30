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
package com.xceptance.xlt.report.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LowPrecisionIntValueSetTest
{
    private LowPrecisionIntValueSet valueSet;

    int buckets = 100;

    @Before
    public void before()
    {
        valueSet = new LowPrecisionIntValueSet(buckets);
    }

    @Test
    public void getValues_noValuesAdded()
    {
        // do not add any value

        // check that we get an empty array back
        final double[] values = valueSet.getValues();
        Assert.assertEquals(0, values.length);
    }

    @Test
    public void getValues_noScalingRequired()
    {
        // add no more values than buckets -> no scaling necessary
        for (int i = 0; i < buckets; i++)
        {
            valueSet.addValue(i);
        }

        // check that we get the same values back
        final double[] values = valueSet.getValues();
        Assert.assertEquals(buckets, values.length);
        for (int i = 0; i < buckets; i++)
        {
            Assert.assertEquals(i, values[i], 0);
        }
    }

    @Test
    public void getValues_scalingRequired()
    {
        // add twice as many values than buckets -> scaling necessary
        for (int i = 0; i < buckets * 2; i++)
        {
            valueSet.addValue(i);
        }

        // check that we get back approximated values only
        final double[] values = valueSet.getValues();
        Assert.assertEquals(buckets, values.length);
        for (int i = 0; i < buckets; i++)
        {
            Assert.assertEquals(i * 2, values[i], 0);
        }
    }

    @Test
    public void getValues_specificValues()
    {
        // add only some specific values -> no scaling necessary
        double[] v =
            {
                3, 11, 37, 53, 73, 97
            };

        for (double i : v)
        {
            valueSet.addValue((int) i);
        }

        // check that we get the same values back
        final double[] values = valueSet.getValues();
        Assert.assertArrayEquals(v, values, 0);
    }

    @Test
    public void merge()
    {
        LowPrecisionIntValueSet anotherValueSet = new LowPrecisionIntValueSet(buckets);

        valueSet.addValue(0);          // requires no scaling
        valueSet.addValue(99);         // requires no scaling
        anotherValueSet.addValue(100); // requires scaling
        anotherValueSet.addValue(199); // requires no further scaling

        valueSet.merge(anotherValueSet);

        // check that valueSet was also scaled (99 -> 98) and contains the values of the other set
        final double[] values = valueSet.getValues();
        Assert.assertEquals(4, values.length);
        Assert.assertEquals(0, values[0], 0);
        Assert.assertEquals(98, values[1], 0);
        Assert.assertEquals(100, values[2], 0);
        Assert.assertEquals(198, values[3], 0);
    }
}
