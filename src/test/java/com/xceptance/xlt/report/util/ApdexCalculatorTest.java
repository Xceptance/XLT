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

import java.math.BigDecimal;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class ApdexCalculatorTest
{
    @Test
    public void noSamples()
    {
        ApdexCalculator calc = new ApdexCalculator(1.0);

        Apdex apdex = calc.getApdex();
        checkExpectations(apdex, new BigDecimal("0.00"), "NS [1.0]", new BigDecimal("1.0"), 0, true);
    }

    @Test
    public void oneSample()
    {
        ApdexCalculator calc = new ApdexCalculator(1.0);
        calc.addSample(100, false);

        Apdex apdex = calc.getApdex();
        checkExpectations(apdex, new BigDecimal("1.00"), "1.00 [1.0]*", new BigDecimal("1.0"), 1, true);
    }

    @Test
    public void fewSamples()
    {
        ApdexCalculator calc = new ApdexCalculator(1.0);

        Random random = new Random();

        for (int i = 0; i < 99; i++)
        {
            calc.addSample(random.nextInt(1000), false);
        }

        Apdex apdex = calc.getApdex();
        checkExpectations(apdex, new BigDecimal("1.00"), "1.00 [1.0]*", new BigDecimal("1.0"), 99, true);
    }

    @Test
    public void satisfyingSamples()
    {
        ApdexCalculator calc = new ApdexCalculator(1.0);

        Random random = new Random();

        for (int i = 0; i < 100; i++)
        {
            calc.addSample(random.nextInt(1000), false);
        }

        Apdex apdex = calc.getApdex();
        checkExpectations(apdex, new BigDecimal("1.00"), "1.00 [1.0]", new BigDecimal("1.0"), 100, false);
    }

    @Test
    public void toleratedSamples()
    {
        ApdexCalculator calc = new ApdexCalculator(1.0);

        for (int i = 0; i < 100; i++)
        {
            calc.addSample(2000, false);
        }

        Apdex apdex = calc.getApdex();
        checkExpectations(apdex, new BigDecimal("0.50"), "0.50 [1.0]", new BigDecimal("1.0"), 100, false);
    }

    @Test
    public void frustratingSamples()
    {
        ApdexCalculator calc = new ApdexCalculator(1.0);

        for (int i = 0; i < 100; i++)
        {
            calc.addSample(10000, false);
        }

        Apdex apdex = calc.getApdex();
        checkExpectations(apdex, new BigDecimal("0.00"), "0.00 [1.0]", new BigDecimal("1.0"), 100, false);
    }

    @Test
    public void quickButFailedSamples()
    {
        ApdexCalculator calc = new ApdexCalculator(1.0);

        for (int i = 0; i < 100; i++)
        {
            calc.addSample(100, true);
        }

        Apdex apdex = calc.getApdex();
        checkExpectations(apdex, new BigDecimal("0.00"), "0.00 [1.0]", new BigDecimal("1.0"), 100, false);
    }

    @Test
    public void mixedSamples()
    {
        ApdexCalculator calc = new ApdexCalculator(1.0);

        // satisfying
        for (int i = 0; i < 60; i++)
        {
            calc.addSample(100, false);
        }
        // tolerated
        for (int i = 0; i < 30; i++)
        {
            calc.addSample(2000, false);
        }
        // frustrating
        calc.addSample(100, true);
        calc.addSample(5000, true);
        for (int i = 0; i < 8; i++)
        {
            calc.addSample(10000, false);
        }

        Apdex apdex = calc.getApdex();
        checkExpectations(apdex, new BigDecimal("0.75"), "0.75 [1.0]", new BigDecimal("1.0"), 100, false);
    }

    @Test
    public void thresholdHasTwoSignificantDigits()
    {
        Apdex apdex = new ApdexCalculator(1.25).getApdex();
        checkExpectations(apdex, new BigDecimal("0.00"), "NS [1.2]", new BigDecimal("1.2"), 0, true);

        apdex = new ApdexCalculator(1.35).getApdex();
        checkExpectations(apdex, new BigDecimal("0.00"), "NS [1.4]", new BigDecimal("1.4"), 0, true);

        apdex = new ApdexCalculator(0.135).getApdex();
        checkExpectations(apdex, new BigDecimal("0.00"), "NS [0.14]", new BigDecimal("0.14"), 0, true);

        apdex = new ApdexCalculator(0.0165).getApdex();
        checkExpectations(apdex, new BigDecimal("0.00"), "NS [0.017]", new BigDecimal("0.017"), 0, true);

        apdex = new ApdexCalculator(1.0165).getApdex();
        checkExpectations(apdex, new BigDecimal("0.00"), "NS [1.0]", new BigDecimal("1.0"), 0, true);

        apdex = new ApdexCalculator(10.0165).getApdex();
        checkExpectations(apdex, new BigDecimal("0.00"), "NS [10]", new BigDecimal("10"), 0, true);
    }

    private void checkExpectations(Apdex apdex, BigDecimal value, String longValue, BigDecimal threshold, long numberOfSamples,
                                   boolean isLowSample)
    {
        Assert.assertEquals(value, apdex.getValue());
        Assert.assertEquals(longValue, apdex.getLongValue());
        Assert.assertEquals(threshold, apdex.getThreshold());
        Assert.assertEquals(numberOfSamples, apdex.getNumberOfSamples());
        Assert.assertEquals(isLowSample, apdex.isLowSample());
    }
}
