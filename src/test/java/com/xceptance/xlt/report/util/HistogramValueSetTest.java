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
package com.xceptance.xlt.report.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link HistogramValueSet} class.
 */
public class HistogramValueSetTest
{
    @Test
    public void testBasics()
    {
        final double minValue = 0.0;
        final double maxValue = 100.0;
        final int numberOfBins = 100;

        final HistogramValueSet valueSet = new HistogramValueSet(minValue, maxValue, numberOfBins);

        valueSet.addValue(-1);
        valueSet.addValue(0);
        valueSet.addValue(1);
        valueSet.addValue(99);
        valueSet.addValue(100);
        valueSet.addValue(101);

        // check
        Assert.assertEquals(minValue, valueSet.getMinValue(), 0.0);
        Assert.assertEquals(maxValue, valueSet.getMaxValue(), 0.0);
        Assert.assertEquals(numberOfBins, valueSet.getNumberOfBins());

        final int[] countPerBin = valueSet.getCountPerBin();
        Assert.assertEquals(numberOfBins, countPerBin.length);

        Assert.assertEquals(countPerBin[0], 3); // -1, 0 and 1
        Assert.assertEquals(countPerBin[98], 1); // 99
        Assert.assertEquals(countPerBin[99], 2); // 100 and 101
    }

    @Test
    public void testMergeNull()
    {
        final HistogramValueSet set = new HistogramValueSet(0, 100, 10);
        set.addValue(5);
        set.merge(null);
        Assert.assertEquals(1, set.getCountPerBin()[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeIncompatibleBinsThrows()
    {
        final HistogramValueSet set1 = new HistogramValueSet(0, 100, 10);
        final HistogramValueSet set2 = new HistogramValueSet(0, 100, 20);
        set1.merge(set2);
    }

    @Test
    public void testMergeEquivalence()
    {
        final HistogramValueSet seq = new HistogramValueSet(0, 100, 10);
        final HistogramValueSet s1 = new HistogramValueSet(0, 100, 10);
        final HistogramValueSet s2 = new HistogramValueSet(0, 100, 10);

        final double[] data = { 5.0, 15.0, 25.0, 35.0, 5.0, 95.0, 105.0 };
        for (int i = 0; i < data.length; i++)
        {
            seq.addValue(data[i]);
            if (i % 2 == 0)
            {
                s1.addValue(data[i]);
            }
            else
            {
                s2.addValue(data[i]);
            }
        }

        s1.merge(s2);
        Assert.assertArrayEquals(seq.getCountPerBin(), s1.getCountPerBin());
    }
}
