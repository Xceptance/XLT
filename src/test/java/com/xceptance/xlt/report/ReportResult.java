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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Provides query and assertion methods over the output directory produced by {@link ReportTestHarness}.
 *
 * <p>All assertion methods throw {@link AssertionError} with a clear, descriptive message when the
 * condition is not met. Query methods return values for use in custom assertions.
 *
 * <p>Example:
 * <pre>
 * result.assertHtmlFileExists("transactions.html");
 * result.assertXmlNode("//summary/transactions");
 * result.assertChartExists("transactions-TOrder-runtime.webp");
 *
 * List&lt;String&gt; names = result.queryXml("//transactions/transaction/name");
 * assertTrue(names.contains("TOrder"));
 *
 * // SSIM chart comparison against a stored baseline:
 * result.assertChartSimilarity("transactions-TOrder-runtime.webp", baselineFile, 0.95);
 * </pre>
 */
public class ReportResult
{
    private final File outputDir;

    private final File primaryXmlFile;

    /** Lazily parsed primary XML document (null until first use). */
    private Document cachedXmlDocument;

    /**
     * Creates a ReportResult wrapping the given output directory and primary XML file.
     *
     * @param outputDir
     *            the root output directory of the generated report
     * @param primaryXmlFile
     *            the primary XML report file (e.g., {@code testreport.xml})
     */
    ReportResult(final File outputDir, final File primaryXmlFile)
    {
        this.outputDir = outputDir;
        this.primaryXmlFile = primaryXmlFile;
    }

    // -------------------------------------------------------------------------
    // Directory / file access
    // -------------------------------------------------------------------------

    /**
     * Returns the root output directory of the generated report.
     *
     * @return the output directory
     */
    public File outputDir()
    {
        return outputDir;
    }

    /**
     * Returns the primary XML report file (e.g., {@code testreport.xml} or {@code diffreport.xml}).
     *
     * @return the primary XML file
     */
    public File xmlFile()
    {
        return primaryXmlFile;
    }

    /**
     * Returns the named HTML file from the report's root output directory.
     *
     * @param name
     *            the filename (e.g., {@code "transactions.html"})
     * @return the {@link File} — may not exist, call {@link #assertHtmlFileExists} to assert existence
     */
    public File htmlFile(final String name)
    {
        return new File(outputDir, name);
    }

    /**
     * Returns all {@code .html} files in the output directory tree.
     *
     * @return list of all HTML files; never null
     */
    public List<File> allHtmlFiles()
    {
        return new ArrayList<>(FileUtils.listFiles(outputDir, new WildcardFileFilter("*.html"), TrueFileFilter.INSTANCE));
    }

    /**
     * Returns the {@code charts/} subdirectory of the output directory where chart PNG files are written.
     *
     * @return the charts directory (may not exist when charts are disabled)
     */
    public File chartsDir()
    {
        return new File(outputDir, "charts");
    }

    /**
     * Returns all chart image files (WebP format) in the {@code charts/} subdirectory of the output directory.
     * XLT generates charts as {@code .webp} files. Returns an empty list when charts are disabled.
     *
     * @return list of all WebP chart files; never null
     */
    public List<File> allChartFiles()
    {
        final File charts = chartsDir();
        if (!charts.isDirectory())
        {
            return new ArrayList<>();
        }
        return new ArrayList<>(FileUtils.listFiles(charts, new WildcardFileFilter("*.webp"), TrueFileFilter.INSTANCE));
    }

    // -------------------------------------------------------------------------
    // File-existence assertions
    // -------------------------------------------------------------------------

    /**
     * Asserts that the named HTML file exists in the root of the output directory.
     *
     * @param name
     *            the filename to check (e.g., {@code "transactions.html"})
     * @throws AssertionError
     *             with a descriptive message if the file does not exist
     */
    public void assertHtmlFileExists(final String name)
    {
        final File f = htmlFile(name);
        Assert.assertTrue("Expected HTML file missing in report output: " + f.getAbsolutePath(), f.isFile());
    }

    /**
     * Asserts that at least one chart file with the given name exists anywhere in the {@code charts/} subdirectory.
     * XLT generates charts as WebP files ({@code .webp}). The check is name-only.
     *
     * @param name
     *            the chart filename (e.g., {@code "transactions-TOrder-runtime.webp"})
     * @throws AssertionError
     *             with a descriptive message if no such chart file is found
     */
    public void assertChartExists(final String name)
    {
        final List<File> charts = allChartFiles();
        final boolean found = charts.stream().anyMatch(f -> f.getName().equals(name));
        Assert.assertTrue("Expected chart file not found in report output: " + name + " (charts dir: " + chartsDir() + ")", found);
    }

    // -------------------------------------------------------------------------
    // XML / XPath querying and assertion
    // -------------------------------------------------------------------------

    /**
     * Asserts that the given XPath expression returns at least one matching node in the primary XML file.
     *
     * @param xpath
     *            an XPath expression (e.g., {@code "//transactions/transaction[@name='TOrder']"})
     * @throws AssertionError
     *             with a descriptive message if no nodes match
     * @throws Exception
     *             if the XML cannot be parsed or the XPath is invalid
     */
    public void assertXmlNode(final String xpath) throws Exception
    {
        final NodeList nodes = evalXpath(xpath);
        Assert.assertTrue("XPath expression matched no nodes in " + primaryXmlFile.getName() + ": " + xpath, nodes.getLength() > 0);
    }

    /**
     * Evaluates the given XPath expression against the primary XML file and returns a list of string
     * values (text content or attribute values) for each matching node.
     *
     * @param xpath
     *            an XPath expression
     * @return list of string values for each matching node; empty list if no matches
     * @throws Exception
     *             if the XML cannot be parsed or the XPath is invalid
     */
    public List<String> queryXml(final String xpath) throws Exception
    {
        final NodeList nodes = evalXpath(xpath);
        final List<String> results = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            results.add(nodes.item(i).getTextContent());
        }
        return results;
    }

    // -------------------------------------------------------------------------
    // HTML content checking and assertion
    // -------------------------------------------------------------------------

    /**
     * Returns true if the named HTML file contains the given text.
     *
     * @param fileName
     *            the filename relative to the output root (e.g., {@code "transactions.html"})
     * @param text
     *            the text to find
     * @return true if the text was found
     * @throws IOException
     *             if the file cannot be read
     */
    public boolean htmlContains(final String fileName, final String text) throws IOException
    {
        final File f = htmlFile(fileName);
        if (!f.isFile())
        {
            return false;
        }
        return FileUtils.readFileToString(f, StandardCharsets.UTF_8).contains(text);
    }

    /**
     * Asserts that the named HTML file contains the given text.
     *
     * @param fileName
     *            the filename relative to the output root (e.g., {@code "transactions.html"})
     * @param text
     *            the text to find
     * @throws AssertionError
     *             with a descriptive message if the file does not exist or does not contain the text
     * @throws IOException
     *             if the file cannot be read
     */
    public void assertHtmlContains(final String fileName, final String text) throws IOException
    {
        assertHtmlFileExists(fileName);
        final String content = FileUtils.readFileToString(htmlFile(fileName), StandardCharsets.UTF_8);
        Assert.assertTrue("HTML file '" + fileName + "' does not contain expected text: " + text, content.contains(text));
    }

    // -------------------------------------------------------------------------
    // SSIM chart comparison
    // -------------------------------------------------------------------------

    /**
     * Computes the SSIM (Structural Similarity Index Measure) score between a generated chart and a
     * baseline image file.
     *
     * <p>SSIM is in [0.0, 1.0]: <strong>1.0</strong> = pixel-perfect identical;
     * values &ge; 0.95 are considered visually near-identical for chart images.
     *
     * <p>The chart is located by searching the {@code charts/} subdirectory for a file with the given name.
     * Both WebP and PNG baselines are supported (any format readable by {@code ImageIO}).
     * If the two images have different sizes the smaller is scaled up before comparison.
     *
     * @param chartName
     *            filename of the chart inside {@code charts/} (e.g. {@code "transactions-TOrder-runtime.webp"})
     * @param baseline
     *            the baseline image file to compare against
     * @return SSIM score in [0.0, 1.0]
     * @throws Exception
     *             if either image cannot be found or decoded
     */
    public double computeChartSsim(final String chartName, final File baseline) throws Exception
    {
        final File chart = findChartFile(chartName);
        Assert.assertNotNull("Chart file not found in charts/ dir: " + chartName, chart);
        Assert.assertTrue("Baseline file does not exist: " + baseline.getAbsolutePath(), baseline.isFile());
        return ChartSsim.compute(chart, baseline);
    }

    /**
     * Asserts that the SSIM score between the named chart and the baseline image is &ge; {@code minSsim}.
     *
     * <p>Typical thresholds:
     * <ul>
     *   <li>0.99 — near-pixel-perfect; suitable for deterministic chart data</li>
     *   <li>0.95 — allows minor anti-aliasing/font rendering differences</li>
     *   <li>0.90 — allows layout shifts; use when chart dimensions may vary slightly</li>
     * </ul>
     *
     * @param chartName
     *            filename of the chart inside {@code charts/} (e.g. {@code "transactions-TOrder-runtime.webp"})
     * @param baseline
     *            the baseline image file to compare against
     * @param minSsim
     *            minimum acceptable SSIM score, e.g. {@code 0.95}
     * @throws AssertionError
     *             with the actual score in the message if SSIM &lt; minSsim
     * @throws Exception
     *             if either image cannot be found or decoded
     */
    public void assertChartSimilarity(final String chartName, final File baseline, final double minSsim) throws Exception
    {
        final double ssim = computeChartSsim(chartName, baseline);
        Assert.assertTrue(
            String.format("Chart '%s' SSIM %.4f is below minimum %.4f (baseline: %s)",
                          chartName, ssim, minSsim, baseline.getName()),
            ssim >= minSsim);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private NodeList evalXpath(final String xpathExpr) throws Exception
    {
        Assert.assertTrue("Primary XML file does not exist: " + primaryXmlFile.getAbsolutePath(), primaryXmlFile.isFile());

        if (cachedXmlDocument == null)
        {
            final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            cachedXmlDocument = db.parse(primaryXmlFile);
        }

        final XPath xpath = XPathFactory.newInstance().newXPath();
        final XPathExpression expr = xpath.compile(xpathExpr);
        return (NodeList) expr.evaluate(cachedXmlDocument, XPathConstants.NODESET);
    }

    /**
     * Finds a chart file by name within the {@code charts/} subdirectory tree.
     *
     * @param name
     *            the filename to search for
     * @return the matching {@link File}, or {@code null} if not found
     */
    private File findChartFile(final String name)
    {
        return allChartFiles().stream()
                              .filter(f -> f.getName().equals(name))
                              .findFirst()
                              .orElse(null);
    }
}
