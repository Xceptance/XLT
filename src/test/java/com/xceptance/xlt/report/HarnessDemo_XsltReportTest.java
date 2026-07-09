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
package com.xceptance.xlt.report;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * DEMO TEST — XSLT rendering engine.
 *
 * <p>This test is an explicit, well-commented demonstration of the {@link ReportTestHarness} API.
 * It generates a full XLT load report from the real sample test data using the XSLT rendering engine
 * and asserts that:
 * <ul>
 *   <li>The primary XML report file ({@code testreport.xml}) exists and is non-empty.
 *   <li>{@code transactions.html} is present in the output.
 *   <li>At least one chart file ({@code .png}) was generated.
 *   <li>The XML report contains at least one {@code <transaction>} element.
 * </ul>
 *
 * <p>This file is a <strong>copy-paste starting point</strong> — duplicate it and adapt the
 * {@link ReportTestHarness} builder calls to write your own tests.
 *
 * <p>Runs as an integration test. Mark as {@code @Category(IntegrationTest.class)} if you set
 * up category-based test selection in Maven Surefire/Failsafe.
 */
public class HarnessDemo_XsltReportTest
{
    /** Path to the primary sample result directory shipped with the project. */
    private static final String SAMPLE_RESULTS =
        "src/test/resources/results/xlt-result-xc-advanced-posters-20260216-152202";

    /**
     * Full XSLT pipeline — real sample data, all defaults, charts on.
     *
     * <p>Step-by-step walkthrough of the API:
     * <ol>
     *   <li>{@code forReport(path)} — point the harness at a results directory.
     *   <li>{@code withProperty(...)} — set the rendering engine to XSLT.
     *   <li>{@code generateReport()} — run the full pipeline; the harness manages its own temp dir.
     *   <li>Use the returned {@link ReportResult} to assert structural properties of the output.
     * </ol>
     */
    @Test
    public void testXsltFullReport() throws Exception
    {
        // 1. Configure the harness — point at real sample data, force XSLT rendering.
        final ReportResult result = ReportTestHarness.forReport(SAMPLE_RESULTS)
                                                    .withProperty("com.xceptance.xlt.reportgenerator.renderingEngine", "xslt")
                                                    .generateReport();

        // 2. Assert the primary XML report was written and is non-empty.
        Assert.assertTrue("testreport.xml must exist", result.xmlFile().isFile());
        Assert.assertTrue("testreport.xml must not be empty", result.xmlFile().length() > 0);

        // 3. Assert the key HTML file exists.
        result.assertHtmlFileExists("transactions.html");

        // 4. Assert at least one chart image was generated.
        Assert.assertFalse("Expected at least one chart (.png) to be generated", result.allChartFiles().isEmpty());

        // 5. Assert a structural data point exists in the XML — a <transaction> element with a name.
        result.assertXmlNode("//transactions/transaction/name");

        // 6. Assert the output directory has HTML files at all.
        Assert.assertFalse("Expected HTML files in output", result.allHtmlFiles().isEmpty());
    }

    /**
     * Same as {@link #testXsltFullReport()} but with charts disabled.
     * Proves that {@link ReportTestHarness#withNoCharts()} suppresses chart generation while
     * all HTML/XML output is still produced.
     */
    @Test
    public void testXsltReportWithNoCharts() throws Exception
    {
        final ReportResult result = ReportTestHarness.forReport(SAMPLE_RESULTS)
                                                    .withProperty("com.xceptance.xlt.reportgenerator.renderingEngine", "xslt")
                                                    .withNoCharts()
                                                    .generateReport();

        // XML and HTML still present
        Assert.assertTrue("testreport.xml must exist", result.xmlFile().isFile());
        result.assertHtmlFileExists("transactions.html");

        // No charts should be present
        Assert.assertTrue("No chart PNG files expected in charts/ dir when withNoCharts() is used",
                          result.allChartFiles().isEmpty());
    }

    /**
     * DEMO: SSIM chart comparison.
     *
     * <p>Demonstrates {@link ReportResult#computeChartSsim} and
     * {@link ReportResult#assertChartSimilarity} using <em>self-comparison</em>:
     * a generated chart is compared against itself, which always yields SSIM = 1.0.
     *
     * <p>This proves the entire image-loading and SSIM pipeline works end-to-end without requiring
     * a pre-committed baseline image in version control.
     *
     * <p><strong>How to use with a real baseline:</strong>
     * <ol>
     *   <li>Run this test once with charts enabled to generate the output.
     *   <li>Copy the desired chart file to
     *       {@code src/test/resources/baselines/transactions-TOrder-runtime.webp}.
     *   <li>Replace the self-comparison with:
     *       <pre>
     *       File baseline = new File("src/test/resources/baselines/transactions-TOrder-runtime.webp");
     *       result.assertChartSimilarity("transactions-TOrder-runtime.webp", baseline, 0.95);
     *       </pre>
     *   <li>From that point on the test will catch any visual regression in the chart.
     * </ol>
     */
    @Test
    public void testChartSsimApiDemo() throws Exception
    {
        // 1. Generate a report with charts enabled.
        final ReportResult result = ReportTestHarness.forReport(SAMPLE_RESULTS)
                                                    .withProperty("com.xceptance.xlt.reportgenerator.renderingEngine", "xslt")
                                                    .generateReport();

        // 2. Skip the SSIM test if no charts were generated (e.g. headless / chart config missing).
        final List<File> charts = result.allChartFiles();
        Assume.assumeFalse("No charts generated — SSIM demo skipped", charts.isEmpty());

        // 3. Pick the first available chart as both the generated image AND the baseline.
        //    Self-comparison always yields SSIM = 1.0.
        final File firstChart = charts.get(0);
        final String chartName = firstChart.getName();

        // 4. Query the raw score — should be exactly 1.0 for self-comparison.
        final double ssim = result.computeChartSsim(chartName, firstChart);
        Assert.assertEquals("Self-comparison must yield SSIM = 1.0 (chart: " + chartName + ")", 1.0, ssim, 1e-9);

        // 5. Use the assertion form with a threshold of 0.99 (same image → always passes).
        result.assertChartSimilarity(chartName, firstChart, 0.99);
    }
}
