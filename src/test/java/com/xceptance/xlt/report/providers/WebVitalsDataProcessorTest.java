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
package com.xceptance.xlt.report.providers;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.xceptance.xlt.api.engine.WebVitalData;
import com.xceptance.xlt.report.providers.WebVitalReport.Rating;

/**
 * Unit tests for {@link WebVitalsDataProcessor}.
 *
 * <p>Verifies:
 * <ul>
 *   <li>Correct routing of Web Vital data by name suffix (e.g. {@code [CLS]}, {@code [LCP]})</li>
 *   <li>Rating classification (good / improve / poor) based on defined thresholds</li>
 *   <li>P75 score estimation</li>
 *   <li>Null report for unobserved vitals</li>
 *   <li>Unknown vital names are ignored</li>
 * </ul>
 *
 * <p>Web Vital thresholds from the source code:
 * <pre>
 *   CLS:  good &le; 0.10,  poor &gt; 0.25
 *   FCP:  good &le; 1800,  poor &gt; 3000
 *   FID:  good &le; 100,   poor &gt; 300
 *   INP:  good &le; 200,   poor &gt; 500
 *   LCP:  good &le; 2500,  poor &gt; 4000
 *   TTFB: good &le; 800,   poor &gt; 1800
 * </pre>
 */
public class WebVitalsDataProcessorTest
{
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    private WebVitalsDataProcessor createProcessor() throws Exception
    {
        final DummyReportGeneratorConfiguration config = DummyReportGeneratorConfiguration.getDefault();
        config.setReportDirectory(tempDir.newFolder("report"));

        final WebVitalsReportProvider provider = new WebVitalsReportProvider();
        provider.setConfiguration(config);

        return new WebVitalsDataProcessor("HomePage", provider);
    }

    private static WebVitalData vital(final String actionAndVital, final double value)
    {
        final WebVitalData data = new WebVitalData(actionAndVital);
        data.setTime(1000000L);
        data.setValue(value);
        return data;
    }

    // -------------------------------------------------------------------------
    // Test: CLS - all good
    // -------------------------------------------------------------------------

    @Test
    public void testCls_allGood() throws Exception
    {
        final WebVitalsDataProcessor proc = createProcessor();

        proc.processDataRecord(vital("HomePage [CLS]", 0.05));
        proc.processDataRecord(vital("HomePage [CLS]", 0.08));
        proc.processDataRecord(vital("HomePage [CLS]", 0.10));

        final WebVitalsReport report = proc.createWebVitalsReport();

        Assert.assertNotNull("CLS report should exist", report.cls);
        Assert.assertEquals("goodCount", 3, report.cls.goodCount);
        Assert.assertEquals("improveCount", 0, report.cls.improveCount);
        Assert.assertEquals("poorCount", 0, report.cls.poorCount);
        Assert.assertEquals("rating", Rating.good, report.cls.rating);
    }

    // -------------------------------------------------------------------------
    // Test: CLS - mixed ratings
    // -------------------------------------------------------------------------

    @Test
    public void testCls_mixedRatings() throws Exception
    {
        final WebVitalsDataProcessor proc = createProcessor();

        proc.processDataRecord(vital("HomePage [CLS]", 0.05));  // good
        proc.processDataRecord(vital("HomePage [CLS]", 0.15));  // improve (> 0.10, <= 0.25)
        proc.processDataRecord(vital("HomePage [CLS]", 0.30));  // poor (> 0.25)

        final WebVitalsReport report = proc.createWebVitalsReport();

        Assert.assertNotNull("CLS report should exist", report.cls);
        Assert.assertEquals("goodCount", 1, report.cls.goodCount);
        Assert.assertEquals("improveCount", 1, report.cls.improveCount);
        Assert.assertEquals("poorCount", 1, report.cls.poorCount);
    }

    // -------------------------------------------------------------------------
    // Test: LCP - timing vital with rounding
    // -------------------------------------------------------------------------

    @Test
    public void testLcp_allGood() throws Exception
    {
        final WebVitalsDataProcessor proc = createProcessor();

        proc.processDataRecord(vital("HomePage [LCP]", 1500.0));
        proc.processDataRecord(vital("HomePage [LCP]", 2000.0));
        proc.processDataRecord(vital("HomePage [LCP]", 2500.0));

        final WebVitalsReport report = proc.createWebVitalsReport();

        Assert.assertNotNull("LCP report should exist", report.lcp);
        Assert.assertEquals("goodCount", 3, report.lcp.goodCount);
        Assert.assertEquals("rating", Rating.good, report.lcp.rating);

        // LCP is a TimingWebVitalStatistics -> score is rounded to integer
        Assert.assertEquals("score should have no decimal places", 0, report.lcp.score.scale());
    }

    // -------------------------------------------------------------------------
    // Test: unobserved vitals are null
    // -------------------------------------------------------------------------

    @Test
    public void testUnobservedVitalsAreNull() throws Exception
    {
        final WebVitalsDataProcessor proc = createProcessor();

        proc.processDataRecord(vital("HomePage [CLS]", 0.05));

        final WebVitalsReport report = proc.createWebVitalsReport();

        Assert.assertNotNull("CLS should exist", report.cls);
        Assert.assertNull("FCP should be null (no observations)", report.fcp);
        Assert.assertNull("FID should be null", report.fid);
        Assert.assertNull("INP should be null", report.inp);
        Assert.assertNull("LCP should be null", report.lcp);
        Assert.assertNull("TTFB should be null", report.ttfb);
    }

    // -------------------------------------------------------------------------
    // Test: all six vitals populated
    // -------------------------------------------------------------------------

    @Test
    public void testAllSixVitals() throws Exception
    {
        final WebVitalsDataProcessor proc = createProcessor();

        proc.processDataRecord(vital("HomePage [CLS]", 0.05));
        proc.processDataRecord(vital("HomePage [FCP]", 1000.0));
        proc.processDataRecord(vital("HomePage [FID]", 50.0));
        proc.processDataRecord(vital("HomePage [INP]", 100.0));
        proc.processDataRecord(vital("HomePage [LCP]", 2000.0));
        proc.processDataRecord(vital("HomePage [TTFB]", 500.0));

        final WebVitalsReport report = proc.createWebVitalsReport();

        Assert.assertNotNull("CLS", report.cls);
        Assert.assertNotNull("FCP", report.fcp);
        Assert.assertNotNull("FID", report.fid);
        Assert.assertNotNull("INP", report.inp);
        Assert.assertNotNull("LCP", report.lcp);
        Assert.assertNotNull("TTFB", report.ttfb);

        // All values are "good" by their respective thresholds
        Assert.assertEquals("CLS rating", Rating.good, report.cls.rating);
        Assert.assertEquals("FCP rating", Rating.good, report.fcp.rating);
        Assert.assertEquals("FID rating", Rating.good, report.fid.rating);
        Assert.assertEquals("INP rating", Rating.good, report.inp.rating);
        Assert.assertEquals("LCP rating", Rating.good, report.lcp.rating);
        Assert.assertEquals("TTFB rating", Rating.good, report.ttfb.rating);
    }

    // -------------------------------------------------------------------------
    // Test: unknown vital name is ignored
    // -------------------------------------------------------------------------

    @Test
    public void testUnknownVitalIgnored() throws Exception
    {
        final WebVitalsDataProcessor proc = createProcessor();

        proc.processDataRecord(vital("HomePage [UNKNOWN]", 42.0));

        final WebVitalsReport report = proc.createWebVitalsReport();

        Assert.assertNull("CLS", report.cls);
        Assert.assertNull("FCP", report.fcp);
        Assert.assertNull("FID", report.fid);
        Assert.assertNull("INP", report.inp);
        Assert.assertNull("LCP", report.lcp);
        Assert.assertNull("TTFB", report.ttfb);
    }

    // -------------------------------------------------------------------------
    // Test: report name
    // -------------------------------------------------------------------------

    @Test
    public void testReportName() throws Exception
    {
        final WebVitalsDataProcessor proc = createProcessor();
        proc.processDataRecord(vital("HomePage [CLS]", 0.05));

        final WebVitalsReport report = proc.createWebVitalsReport();

        Assert.assertEquals("name", "HomePage", report.name);
    }

    // -------------------------------------------------------------------------
    // Test: TTFB poor rating
    // -------------------------------------------------------------------------

    @Test
    public void testTtfb_poorRating() throws Exception
    {
        final WebVitalsDataProcessor proc = createProcessor();

        proc.processDataRecord(vital("HomePage [TTFB]", 2000.0));
        proc.processDataRecord(vital("HomePage [TTFB]", 2500.0));
        proc.processDataRecord(vital("HomePage [TTFB]", 3000.0));

        final WebVitalsReport report = proc.createWebVitalsReport();

        Assert.assertNotNull("TTFB report should exist", report.ttfb);
        Assert.assertEquals("poorCount", 3, report.ttfb.poorCount);
        Assert.assertEquals("rating", Rating.poor, report.ttfb.rating);
    }
}
