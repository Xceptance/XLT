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
package com.xceptance.xlt.report.pdf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.ReportGeneratorConfigurationTestBase;

/**
 * Tests the {@link PdfReportGenerator} and its configuration.
 */
public class PdfReportGeneratorTest extends ReportGeneratorConfigurationTestBase
{
    private File sampleXmlFile;

    private File styleSheetFile;

    @Before
    @Override
    public void setup() throws java.io.IOException
    {
        super.setup();

        // Resolve sample XML report from test classpath resources
        try
        {
            sampleXmlFile = new File(getClass().getResource("/testreport.xml").toURI());
        }
        catch (URISyntaxException e)
        {
            Assert.fail("Cannot find required testreport.xml");
        }

        // Locate the main PDF generation XSLT stylesheet
        styleSheetFile = new File("config/xsl/loadreport/pdf.xsl");
    }

    /**
     * Verifies that PDF report is generated correctly from an XML report and handles WebP charts.
     */
    @Test
    public void testGeneratePdfReport() throws Exception
    {
        // Ensure required input fixtures exist
        Assert.assertTrue("Sample XML file must exist", sampleXmlFile.exists());
        Assert.assertTrue("Stylesheet file must exist", styleSheetFile.exists());

        // Prepare target folder with necessary assets (css)
        final File targetDir = tempFolder.newFolder("report-output");
        final File cssDir = new File(targetDir, "css");
        cssDir.mkdirs();
        FileUtils.copyFileToDirectory(new File("config/testreport/css/pdf.css"), cssDir);
        FileUtils.copyFileToDirectory(new File("config/testreport/css/default.css"), cssDir);

        // Generate a synthetic WebP chart image to verify on-the-fly WebP transcoding & embedding
        final File txChartDir = new File(targetDir, "charts/transactions");
        txChartDir.mkdirs();
        final BufferedImage img = new BufferedImage(800, 450, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 800, 450);
        g.setColor(Color.BLUE);
        g.drawString("Transaction Overview Chart", 50, 50);
        g.dispose();
        ImageIO.write(img, "webp", new File(txChartDir, "Transactions.webp"));

        // Define expected output PDF file location
        final File outputPdfFile = new File(targetDir, PdfReportGenerator.DEFAULT_PDF_FILENAME);

        // Configure parameters passed to the XSL transformation
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("productName", "XLT");
        parameters.put("productVersion", "10.0.0");
        parameters.put("productUrl", "https://www.xceptance.com");
        parameters.put("scorecardPresent", Boolean.FALSE);
        parameters.put("pdfReportPresent", Boolean.TRUE);

        // Execute report generation pipeline (XML -> XHTML -> PDF)
        PdfReportGenerator.generatePdfReport(sampleXmlFile, targetDir, styleSheetFile, outputPdfFile, parameters);

        // Verify that the PDF file was generated and is non-empty
        Assert.assertTrue("Output PDF file should exist", outputPdfFile.exists());
        Assert.assertTrue("Output PDF file size should be > 0", outputPdfFile.length() > 0);

        // Verify PDF Magic Bytes (%PDF-)
        final byte[] header = new byte[5];
        try (final FileInputStream fis = new FileInputStream(outputPdfFile))
        {
            int read = fis.read(header);
            Assert.assertEquals(5, read);
            Assert.assertEquals("%PDF-", new String(header));
        }
    }

    /**
     * Verifies that PDF report is generated correctly when rating, summary, and evaluation are present.
     */
    @Test
    public void testGeneratePdfReportWithRating() throws Exception
    {
        // Set up isolated output directory and copy required CSS stylesheets
        final File targetDir = tempFolder.newFolder("report-output-rating");
        final File cssDir = new File(targetDir, "css");
        cssDir.mkdirs();
        FileUtils.copyFileToDirectory(new File("config/testreport/css/pdf.css"), cssDir);
        FileUtils.copyFileToDirectory(new File("config/testreport/css/default.css"), cssDir);

        // Read sample xml and inject rating elements into configuration
        String xmlContent = org.apache.commons.io.FileUtils.readFileToString(sampleXmlFile, java.nio.charset.StandardCharsets.UTF_8);
        final String ratingXml = "<rating>A</rating>\n" + "<ratingSummary>Excellent performance achieved.</ratingSummary>\n" +
                                 "<ratingEvaluation>&lt;div class=\"markdown\"&gt;&lt;h3&gt;Key Findings&lt;/h3&gt;&lt;p&gt;No SLA breaches observed.&lt;/p&gt;&lt;/div&gt;</ratingEvaluation>\n";
        xmlContent = xmlContent.replace("</configuration>", ratingXml + "</configuration>");

        // Write modified XML to target directory
        final File ratingXmlFile = new File(targetDir, "testreport.xml");
        org.apache.commons.io.FileUtils.writeStringToFile(ratingXmlFile, xmlContent, java.nio.charset.StandardCharsets.UTF_8);

        // Set up parameters and output file
        final File outputPdfFile = new File(targetDir, "load-report.pdf");
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("productName", "XLT");
        parameters.put("productVersion", "10.0.0");
        parameters.put("productUrl", "https://www.xceptance.com");
        parameters.put("scorecardPresent", Boolean.FALSE);
        parameters.put("pdfReportPresent", Boolean.TRUE);

        // Generate PDF report with rating included
        PdfReportGenerator.generatePdfReport(ratingXmlFile, targetDir, styleSheetFile, outputPdfFile, parameters);

        // Verify PDF report generation succeeded
        Assert.assertTrue("Output PDF file should exist", outputPdfFile.exists());
        Assert.assertTrue("Output PDF file size should be > 0", outputPdfFile.length() > 0);
    }

    /**
     * Verifies that PDF report is generated correctly when scorecard evaluation is present.
     */
    @Test
    public void testGeneratePdfReportWithScorecard() throws Exception
    {
        // Set up isolated output directory and copy required CSS stylesheets
        final File targetDir = tempFolder.newFolder("report-output-scorecard");
        final File cssDir = new File(targetDir, "css");
        cssDir.mkdirs();
        FileUtils.copyFileToDirectory(new File("config/testreport/css/pdf.css"), cssDir);
        FileUtils.copyFileToDirectory(new File("config/testreport/css/default.css"), cssDir);

        // Copy sample testreport.xml to target directory
        final File testXmlFile = new File(targetDir, "testreport.xml");
        FileUtils.copyFile(sampleXmlFile, testXmlFile);

        // Create a synthetic valid scorecard.xml defining verdict, points, groups, and ratings
        final String scorecardXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<scorecard>\n" +
                                    "  <outcome testFailed=\"false\" points=\"95\" totalPoints=\"100\" pointsPercentage=\"95\">\n" +
                                    "    <rating>A</rating>\n" + "    <groups>\n" +
                                    "      <group ref-id=\"performance\" points=\"95\" totalPoints=\"100\">\n" +
                                    "        <result>Passed</result>\n" + "        <message>All checks passed</message>\n" +
                                    "      </group>\n" + "    </groups>\n" + "  </outcome>\n" + "  <configuration version=\"2\">\n" +
                                    "    <ratings>\n" + "      <rating id=\"A\" name=\"A\">\n" +
                                    "        <description>Excellent</description>\n" + "      </rating>\n" + "    </ratings>\n" +
                                    "    <groups>\n" + "      <group id=\"performance\" name=\"Performance\"/>\n" + "    </groups>\n" +
                                    "  </configuration>\n" + "</scorecard>";
        final File scorecardXmlFile = new File(targetDir, "scorecard.xml");
        org.apache.commons.io.FileUtils.writeStringToFile(scorecardXmlFile, scorecardXml, java.nio.charset.StandardCharsets.UTF_8);

        // Configure parameters to enable scorecard and point to scorecard XML URI
        final File outputPdfFile = new File(targetDir, "load-report.pdf");
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("productName", "XLT");
        parameters.put("productVersion", "10.0.0");
        parameters.put("productUrl", "https://www.xceptance.com");
        parameters.put("scorecardPresent", Boolean.TRUE);
        parameters.put("scorecardXmlUrl", scorecardXmlFile.toURI().toString());
        parameters.put("pdfReportPresent", Boolean.TRUE);

        // Generate PDF report and verify it renders successfully with scorecard
        PdfReportGenerator.generatePdfReport(testXmlFile, targetDir, styleSheetFile, outputPdfFile, parameters);
        Assert.assertTrue("Output PDF file should exist", outputPdfFile.exists());
        Assert.assertTrue("Output PDF file size should be > 0", outputPdfFile.length() > 0);
    }

    /**
     * Verifies that PDF report is generated correctly when scorecard evaluation contains an error.
     */
    @Test
    public void testGeneratePdfReportWithBrokenScorecard() throws Exception
    {
        // Set up isolated output directory and copy required CSS stylesheets
        final File targetDir = tempFolder.newFolder("report-output-broken-scorecard");
        final File cssDir = new File(targetDir, "css");
        cssDir.mkdirs();
        FileUtils.copyFileToDirectory(new File("config/testreport/css/pdf.css"), cssDir);
        FileUtils.copyFileToDirectory(new File("config/testreport/css/default.css"), cssDir);

        // Copy sample testreport.xml to target directory
        final File testXmlFile = new File(targetDir, "testreport.xml");
        FileUtils.copyFile(sampleXmlFile, testXmlFile);

        // Create a scorecard XML simulating an evaluation error with message and log
        final String brokenScorecardXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<scorecard>\n" +
                                          "  <outcome testFailed=\"false\">\n" + "    <error>\n" +
                                          "      <message>Failed to evaluate Groovy configuration: No such property: metrics</message>\n" +
                                          "      <log>ValidationException: Failed to evaluate Groovy configuration</log>\n" +
                                          "    </error>\n" + "  </outcome>\n" + "</scorecard>";
        final File scorecardXmlFile = new File(targetDir, "scorecard.xml");
        org.apache.commons.io.FileUtils.writeStringToFile(scorecardXmlFile, brokenScorecardXml, java.nio.charset.StandardCharsets.UTF_8);

        // Configure parameters pointing to broken scorecard
        final File outputPdfFile = new File(targetDir, "load-report.pdf");
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("productName", "XLT");
        parameters.put("productVersion", "10.0.0");
        parameters.put("productUrl", "https://www.xceptance.com");
        parameters.put("scorecardPresent", Boolean.TRUE);
        parameters.put("scorecardXmlUrl", scorecardXmlFile.toURI().toString());
        parameters.put("pdfReportPresent", Boolean.TRUE);

        // Verify PDF report generation succeeds and handles scorecard error section gracefully
        PdfReportGenerator.generatePdfReport(testXmlFile, targetDir, styleSheetFile, outputPdfFile, parameters);
        Assert.assertTrue("Output PDF file should exist", outputPdfFile.exists());
        Assert.assertTrue("Output PDF file size should be > 0", outputPdfFile.length() > 0);
    }

    /**
     * Tests that the configuration default for PDF report is false.
     */
    @Test
    public void testConfigurationDefault()
    {
        // Read initial configuration from empty test property file
        final ReportGeneratorConfiguration config = readReportGeneratorProperties();
        Assert.assertFalse("PDF report should be disabled by default", config.isPdfReportEnabled());

        // Verify programmatic update via setter
        config.setPdfReportEnabled(true);
        Assert.assertTrue("PDF report should be enabled after setter call", config.isPdfReportEnabled());
    }

    /**
     * Tests that the configuration loads property from file.
     */
    @Test
    public void testConfigurationPropertyFromFile()
    {
        // Verify default configuration before appending property
        final ReportGeneratorConfiguration config = readReportGeneratorProperties();
        Assert.assertFalse("Initial PDF report enabled should be false", config.isPdfReportEnabled());

        // Append property setting to the test reportgenerator.properties file
        appendPropertyToFile("com.xceptance.xlt.reportgenerator.pdf.enabled", "true");

        // Reload configuration and verify that property is picked up
        final ReportGeneratorConfiguration loadedConfig = readReportGeneratorProperties();
        Assert.assertTrue("PDF report should be enabled when configured in properties", loadedConfig.isPdfReportEnabled());
    }
}
