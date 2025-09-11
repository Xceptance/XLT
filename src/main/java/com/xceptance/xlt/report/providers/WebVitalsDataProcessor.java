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
package com.xceptance.xlt.report.providers;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.rank.PSquarePercentile;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.WebVitalData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.report.providers.WebVitalReport.Rating;
import com.xceptance.xlt.report.util.ReportUtils;

/**
 * Processes the {@link WebVitalsData} objects reported for a certain action and returns the result as a
 * {@link WebVitalsReport}.
 * <p>
 * Up to now, the following Web Vitals are supported: CLS, FCP, FID, INP, LCP, and TTFB. See the links below for more
 * information on Web Vitals, especially the thresholds defined for each Web Vital.
 * 
 * @see https://web.dev/articles/vitals
 * @see https://github.com/GoogleChrome/web-vitals
 */
public class WebVitalsDataProcessor extends AbstractDataProcessor
{
    private final WebVitalStatistics cls = new WebVitalStatistics(0.1, 0.25);

    private final WebVitalStatistics fcp = new TimingWebVitalStatistics(1800.0, 3000.0);

    private final WebVitalStatistics fid = new TimingWebVitalStatistics(100.0, 300.0);

    private final WebVitalStatistics inp = new TimingWebVitalStatistics(200.0, 500.0);

    private final WebVitalStatistics lcp = new TimingWebVitalStatistics(2500.0, 4000.0);

    private final WebVitalStatistics ttfb = new TimingWebVitalStatistics(800.0, 1800.0);

    /**
     * Constructor.
     *
     * @param name
     *            the action name
     * @param reportProvider
     *            the parent report provider
     */
    public WebVitalsDataProcessor(final String name, final AbstractReportProvider reportProvider)
    {
        super(name, reportProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        final WebVitalData webVitalData = (WebVitalData) data;

        final String webVitalName = extractWebVitalName(webVitalData.getName());
        final double value = webVitalData.getValue();

        switch (webVitalName)
        {
            case "CLS":
                cls.update(value);
                break;
            case "FCP":
                fcp.update(value);
                break;
            case "FID":
                fid.update(value);
                break;
            case "INP":
                inp.update(value);
                break;
            case "LCP":
                lcp.update(value);
                break;
            case "TTFB":
                ttfb.update(value);
                break;
            default:
                // unknown value -> ignore
                break;
        }
    }

    /**
     * Extracts the web vital name from an action name (for example, "Foo Action [CLS]" -> "CLS").
     * 
     * @param actionName
     *            the action name
     * @return the contained web vital name
     */
    private static String extractWebVitalName(final String actionName)
    {
        final String suffix = StringUtils.substringAfterLast(actionName, " ");
        return StringUtils.substringBetween(suffix, "[", "]");
    }

    /**
     * Creates a report fragment with the values for all known web vitals.
     *
     * @return the report fragment
     */
    public WebVitalsReport createWebVitalsReport()
    {
        final WebVitalsReport report = new WebVitalsReport();

        report.name = getName();
        report.cls = cls.toWebVitalReport();
        report.fcp = fcp.toWebVitalReport();
        report.fid = fid.toWebVitalReport();
        report.inp = inp.toWebVitalReport();
        report.lcp = lcp.toWebVitalReport();
        report.ttfb = ttfb.toWebVitalReport();

        return report;
    }

    /**
     * A statistics object that is fed with all observations for a certain web vital type and produces a final result.
     */
    public static class WebVitalStatistics
    {
        /**
         * The threshold that values rated "good" must not exceed.
         */
        private final double threshold1;

        /**
         * The threshold that values rated "needs improvement" must not exceed.
         */
        private final double threshold2;

        /**
         * Estimates the P75 for a stream of input values.
         */
        private final PSquarePercentile p75Estimator = new PSquarePercentile(75.0);

        /**
         * The number of observations that were rated "good".
         */
        private int goodCount;

        /**
         * The number of observations that were rated "needs improvement".
         */
        private int improveCount;

        /**
         * The number of observations that were rated "poor".
         */
        private int poorCount;

        /**
         * Creates a {@link WebVitalStatistics} object and initializes it with its thresholds.
         */
        public WebVitalStatistics(final double threshold1, final double threshold2)
        {
            this.threshold1 = threshold1;
            this.threshold2 = threshold2;
        }

        /**
         * Updates the internal statistics with the given observation value.
         * 
         * @param value
         *            the metrics value
         */
        public void update(double value)
        {
            p75Estimator.increment(value);

            if (value <= threshold1)
            {
                goodCount++;
            }
            else if (value > threshold2)
            {
                poorCount++;
            }
            else
            {
                improveCount++;
            }
        }

        /**
         * Returns the final score, rating, and supplemental data as a {@link WebVitalReport}.
         * 
         * @return the web vital report if we had some observations, <code>null</code> otherwise
         */
        public WebVitalReport toWebVitalReport()
        {
            if (goodCount + improveCount + poorCount == 0)
            {
                // no observations, no report
                return null;
            }

            final double p75 = p75Estimator.getResult();
            final BigDecimal score = calculateScore(p75);
            final Rating rating;

            if (p75 <= threshold1)
            {
                rating = Rating.good;
            }
            else if (p75 > threshold2)
            {
                rating = Rating.poor;
            }
            else
            {
                rating = Rating.improve;
            }

            return new WebVitalReport(score, rating, goodCount, improveCount, poorCount);
        }

        /**
         * Calculates the final score from the given double value and returns it as a {@link BigDecimal}. Mainly used to
         * set a certain precision or perform rounding, depending on the web vital type.
         * 
         * @param p75
         *            the P75 double value
         * @return the value as a {@link BigDecimal}
         */
        protected BigDecimal calculateScore(final double p75)
        {
            // return the value with a precision of three decimal places
            return ReportUtils.convertToBigDecimal(p75);
        }
    }

    /**
     * A special web vital statistics class for processing web vital data that represents millisecond values. This
     * applies to all web vitals except CLS. Millisecond values are reported as rounded values without a fractional
     * part.
     */
    public static class TimingWebVitalStatistics extends WebVitalStatistics
    {
        /**
         * Creates a {@link TimingWebVitalStatisticss} object and initializes it with its thresholds.
         */
        public TimingWebVitalStatistics(final double threshold1, final double threshold2)
        {
            super(threshold1, threshold2);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected BigDecimal calculateScore(final double p75)
        {
            // round the value to the nearest integral value and return it without any decimal places
            return new BigDecimal(Math.round(p75));
        }
    }
}
