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
}
