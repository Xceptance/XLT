/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.rank.PSquarePercentile;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.WebVitalData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.report.util.ReportUtils;

public class WebVitalsDataProcessor extends AbstractDataProcessor
{
    private WebVital cls = new WebVital(0.1, 0.25);

    private WebVital fcp = new TimingWebVital(1.8, 3.0);

    private WebVital fid = new TimingWebVital(0.1, 0.3);

    private WebVital inp = new TimingWebVital(0.2, 0.5);

    private WebVital lcp = new TimingWebVital(2.5, 4.0);

    private WebVital ttfb = new TimingWebVital(0.8, 1.8);

    /**
     * Constructor.
     * 
     * @param name
     *            sampler name
     * @param reportProvider
     *            report provider
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
        final WebVitalData sample = (WebVitalData) data;
        final double value = sample.getValue();

        // p75.increment(value);

        String webVitalName = extractWebVitalName(data.getName());

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

    private static String extractWebVitalName(String name)
    {
        String suffix = StringUtils.substringAfterLast(name, " ");
        return StringUtils.substringBetween(suffix, "[", "]");
    }

    /**
     * create report
     * 
     * @return report fragment
     */
    public WebVitalsReport getReportFragment()
    {
        WebVitalsReport report = new WebVitalsReport(getName());

        cls.close();
        fcp.close();
        fid.close();
        inp.close();
        lcp.close();
        ttfb.close();

        report.cls = cls.count > 0 ? cls : null;
        report.fcp = fcp.count > 0 ? fcp : null;
        report.fid = fid.count > 0 ? fid : null;
        report.inp = inp.count > 0 ? inp : null;
        report.lcp = lcp.count > 0 ? lcp : null;
        report.ttfb = ttfb.count > 0 ? ttfb : null;

        return report;
    }

    public static class WebVital
    {
        public enum Rating
        {
            good,
            impr,
            poor
        };

        private transient final double threshold1;

        private transient final double threshold2;

        private transient PSquarePercentile accumulator = new PSquarePercentile(75.0);

        private transient Percentile accumulator2 = new Percentile(75.0);

        private transient int count;

        public int goodCount;

        public int imprCount;

        public int poorCount;

        public Rating rating;

        public BigDecimal score;

        double[] data = new double[1000];

        public WebVital(double threshold1, double threshold2)
        {
            this.threshold1 = threshold1;
            this.threshold2 = threshold2;
        }

        public void update(double value)
        {
            value = value * 10;

            data[count] = value;

            count++;
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
                imprCount++;
            }

            accumulator.increment(value);
        }

        public void close()
        {
            double p75 = accumulator.getResult();

            data = Arrays.copyOf(data, count);

            accumulator2.setData(data);
            double p75_2 = accumulator2.evaluate();
            double p75_3 = percentile(data);

            System.err.println(p75 + " - " + p75_2 + " - " + p75_3);

            p75 = p75_2;

            score = ReportUtils.convertToBigDecimal(p75);

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
                rating = Rating.impr;
            }
        }

        private double percentile(double[] data)
        {
            if (count == 0)
                return 0.0;
            
            Arrays.sort(data);

            int i = (int) (0.75 * data.length);

            return data[i];
        }
    }

    public static class TimingWebVital extends WebVital
    {
        public TimingWebVital(double threshold1, double threshold2)
        {
            super(threshold1, threshold2);
        }

        public void update(double value)
        {
            super.update(value / 1000.0);
        }
    }
}
