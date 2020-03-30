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

import java.math.BigDecimal;

/**
 * Represents the <a href="https://en.wikipedia.org/wiki/Apdex">Apdex</a> that has been calculated for a stream of
 * action runtime samples.
 * 
 * @see ApdexCalculator
 */
public class Apdex
{
    private static final String APDEX_FORMAT = "%s [%s]";

    private static final String APDEX_FORMAT_LOW_SAMPLE = APDEX_FORMAT + "*";

    private static final String APDEX_FORMAT_NO_SAMPLE = "NS [%s]";

    /**
     * The threshold [s] for satisfying runtime samples that has been used for Apdex calculation.
     */
    private final BigDecimal threshold;

    /**
     * The total number of runtime samples that have been used for Apdex calculation.
     */
    private final long numberOfSamples;

    /**
     * The Apdex value, for example "0.75".
     */
    private final BigDecimal apdex;

    /**
     * The long Apdex value, for example "0.75 [1.0]*".
     */
    private final String longApdex;

    /**
     * Creates a new {@link Apdex} value object for the given parameters.
     *
     * @param apdex
     *            the apdex value
     * @param threshold
     *            the threshold used when calculating the apdex
     * @param numberOfSamples
     *            the number of samples
     */
    public Apdex(final BigDecimal apdex, final BigDecimal threshold, final long numberOfSamples)
    {
        this.apdex = apdex;
        this.threshold = threshold;
        this.numberOfSamples = numberOfSamples;

        longApdex = formatLongApdex();
    }

    /**
     * Returns the Apdex value, for example "0.87".
     *
     * @return the Apdex value
     */
    public BigDecimal getValue()
    {
        return apdex;
    }

    /**
     * Returns the Apdex value in its long representation (including the threshold and the low-sample marker if
     * necessary) as defined in the Apdex specification. For instance, the output could be something like "0.87 [1.5]*".
     *
     * @return the Apdex value in long format
     */
    public String getLongValue()
    {
        return longApdex;
    }

    /**
     * Returns the threshold (in seconds) used when this Apdex value was calculated.
     *
     * @return the threshold [s]
     */
    public BigDecimal getThreshold()
    {
        return threshold;
    }

    /**
     * Returns the number of samples from which this Apdex value was calculated.
     *
     * @return the number of samples
     */
    public long getNumberOfSamples()
    {
        return numberOfSamples;
    }

    /**
     * Checks whether this Apdex value was calculated from only a few samples (less than 100).
     *
     * @return <code>true</code> if less than 100 samples were used, <code>false</code> otherwise
     */
    public boolean isLowSample()
    {
        return numberOfSamples < 100;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return longApdex;
    }

    /**
     * Returns the Apdex value in its long representation (including the threshold and the low-sample marker if
     * necessary) as defined in the Apdex specification. For instance, the output could be something like "0.87 [1.5]*".
     *
     * @return the Apdex in long format
     */
    private String formatLongApdex()
    {
        final String s;

        if (numberOfSamples == 0)
        {
            s = String.format(APDEX_FORMAT_NO_SAMPLE, threshold.toPlainString());
        }
        else if (isLowSample())
        {
            s = String.format(APDEX_FORMAT_LOW_SAMPLE, apdex.toPlainString(), threshold.toPlainString());
        }
        else
        {
            s = String.format(APDEX_FORMAT, apdex.toPlainString(), threshold.toPlainString());
        }

        return s;
    }
}
