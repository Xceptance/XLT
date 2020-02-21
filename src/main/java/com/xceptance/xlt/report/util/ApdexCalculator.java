package com.xceptance.xlt.report.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Calculates the <a href="https://en.wikipedia.org/wiki/Apdex">Apdex</a> for a stream of runtime samples based on a
 * certain threshold.
 */
public class ApdexCalculator
{
    /**
     * The threshold [s] for runtime samples that are satisfying.
     */
    private final BigDecimal threshold;

    /**
     * The threshold [ms] for runtime samples that are satisfying.
     */
    private final long satisfyingThresholdInMsecs;

    /**
     * The threshold [ms] for runtime samples that are still tolerable. By definition, this value is 4 times
     * {@link #satisfyingThresholdInMsecs}.
     */
    private final long toleratedThresholdInMsecs;

    /**
     * The number of runtime samples that were satisfying.
     */
    private long satisfyingSamplesCount;

    /**
     * The number of runtime samples that were tolerable.
     */
    private long toleratedSamplesCount;

    /**
     * The total number of runtime samples.
     */
    private long totalSamplesCount;

    /**
     * @param thresholdInSecs
     */
    public ApdexCalculator(final double thresholdInSecs)
    {
        // first apply Apdex rules to the threshold (use only two significant digits)
        final BigDecimal temp = new BigDecimal(thresholdInSecs, new MathContext(2, RoundingMode.HALF_EVEN));
        final int newScale = 2 - temp.precision() + temp.scale();
        threshold = temp.setScale(newScale, RoundingMode.HALF_EVEN);

        // derive long thresholds for easier comparison of timestamps
        satisfyingThresholdInMsecs = (long) (threshold.doubleValue() * 1000);
        toleratedThresholdInMsecs = 4 * satisfyingThresholdInMsecs;
    }

    /**
     * Adds a new runtime sample to this calculator.
     *
     * @param runtime
     *            the runtime [ms]
     * @param failed
     *            whether the corresponding action is considered to be failed
     */
    public void addSample(final long runtime, final boolean failed)
    {
        totalSamplesCount++;

        if (!failed)
        {
            if (runtime <= satisfyingThresholdInMsecs)
            {
                satisfyingSamplesCount++;
            }
            else if (runtime <= toleratedThresholdInMsecs)
            {
                toleratedSamplesCount++;
            }
        }
    }

    /**
     * Returns the Apdex value for the runtime samples added so far.
     *
     * @return the Apdex value
     */
    public Apdex getApdex()
    {
        // calculate Apdex value
        final double tempApdex;
        if (totalSamplesCount == 0)
        {
            tempApdex = 0.0;
        }
        else
        {
            tempApdex = (satisfyingSamplesCount + toleratedSamplesCount / 2.0) / totalSamplesCount;
        }

        // apply Apdex rules (use exactly two decimal places)
        final BigDecimal apdex = ReportUtils.convertToBigDecimal(tempApdex, 2);

        return new Apdex(apdex, threshold, totalSamplesCount);
    }
}
