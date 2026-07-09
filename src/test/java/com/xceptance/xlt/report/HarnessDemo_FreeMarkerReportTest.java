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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * DEMO TEST — FreeMarker rendering engine.
 *
 * <p>Mirrors {@link HarnessDemo_XsltReportTest} with FreeMarker as the rendering engine.
 * Adds a basic parity check that both engines produce output containing the same transaction name,
 * proving output equivalence at a structural level.
 *
 * <p>This file is a <strong>copy-paste starting point</strong> for FreeMarker-specific tests.
 */
public class HarnessDemo_FreeMarkerReportTest
{
    /** Path to the primary sample result directory shipped with the project. */
    private static final String SAMPLE_RESULTS =
        "src/test/resources/results/xlt-result-xc-advanced-posters-20260216-152202";

    /**
     * Full FreeMarker pipeline — identical assertions as the XSLT demo test.
     * If this test passes alongside {@link HarnessDemo_XsltReportTest#testXsltFullReport()},
     * both rendering engines produce structurally correct output from the same input.
     */
    @Test
    public void testFreeMarkerFullReport() throws Exception
    {
        final ReportResult result = ReportTestHarness.forReport(SAMPLE_RESULTS)
                                                    .withProperty("com.xceptance.xlt.reportgenerator.renderingEngine", "freemarker")
                                                    .generateReport();

        // Same structural assertions as the XSLT test — both engines must satisfy these.
        Assert.assertTrue("testreport.xml must exist", result.xmlFile().isFile());
        Assert.assertTrue("testreport.xml must not be empty", result.xmlFile().length() > 0);

        result.assertHtmlFileExists("transactions.html");

        Assert.assertFalse("Expected at least one chart (.png) to be generated", result.allChartFiles().isEmpty());

        result.assertXmlNode("//transactions/transaction/name");

        Assert.assertFalse("Expected HTML files in output", result.allHtmlFiles().isEmpty());
    }

    /**
     * Parity check: both XSLT and FreeMarker must produce the same transaction names in their output HTML.
     *
     * <p>This runs both engines on the same input and asserts that {@code transactions.html}
     * from each contains at least one common transaction name obtained via XPath from the
     * FreeMarker report XML.
     */
    @Test
    public void testFreeMarkerVsXsltParityCheck() throws Exception
    {
        // Generate XSLT report and collect transaction names from its XML
        final ReportResult xsltResult = ReportTestHarness.forReport(SAMPLE_RESULTS)
                                                         .withProperty("com.xceptance.xlt.reportgenerator.renderingEngine", "xslt")
                                                         .withNoCharts() // skip charts for speed in parity test
                                                         .generateReport();

        // Generate FreeMarker report and collect transaction names from its XML
        final ReportResult fmResult = ReportTestHarness.forReport(SAMPLE_RESULTS)
                                                       .withProperty("com.xceptance.xlt.reportgenerator.renderingEngine", "freemarker")
                                                       .withNoCharts()
                                                       .generateReport();

        // Both XMLs must have at least one transaction name
        final List<String> xsltTxNames = xsltResult.queryXml("//transactions/transaction/name");
        final List<String> fmTxNames = fmResult.queryXml("//transactions/transaction/name");

        Assert.assertFalse("XSLT report must have at least one transaction", xsltTxNames.isEmpty());
        Assert.assertFalse("FreeMarker report must have at least one transaction", fmTxNames.isEmpty());

        // Both engines must have seen the same transaction names (from the same input data)
        Assert.assertEquals("Both rendering engines must have the same set of transaction names", xsltTxNames, fmTxNames);

        // The first transaction name must appear in both engines' transactions.html
        final String firstTxName = xsltTxNames.get(0);
        Assert.assertTrue("XSLT transactions.html must contain transaction name: " + firstTxName,
                          xsltResult.htmlContains("transactions.html", firstTxName));
        Assert.assertTrue("FreeMarker transactions.html must contain transaction name: " + firstTxName,
                          fmResult.htmlContains("transactions.html", firstTxName));
    }
}
