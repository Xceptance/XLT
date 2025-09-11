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

public class ArithmeticMeanTest
{
    @Test
    public final void testConstructor()
    {
        final ArithmeticMean am = new ArithmeticMean();
        Assert.assertEquals(0L, am.getCount());
        Assert.assertTrue(Double.isNaN(am.getMean()));
    }

    @Test
    public final void testAddValue()
    {
        final ArithmeticMean am = new ArithmeticMean();

        am.addValue(1);
        Assert.assertEquals(1L, am.getCount());
        Assert.assertTrue(am.getMean() == 1);

        am.addValue(2);
        Assert.assertEquals(2L, am.getCount());
        Assert.assertTrue(am.getMean() == 1.5);

        am.addValue(3);
        Assert.assertEquals(3L, am.getCount());
        Assert.assertTrue(am.getMean() == 2);

        am.addValue(0);
        Assert.assertEquals(4L, am.getCount());
        Assert.assertTrue(am.getMean() == (6.0 / 4));
    }

}
