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
    private File sampleReportDir;
    private File sampleXmlFile;
    private File styleSheetFile;

    @Before
    @Override
    public void setup() throws java.io.IOException
    {
        super.setup();
        sampleReportDir = new File("reports/xlt-result-ariat-lt-2025-315-20251119-165727");
        sampleXmlFile = new File(sampleReportDir, "testreport.xml");
        styleSheetFile = new File("config/xsl/loadreport/pdf.xsl");
    }

    /**
     * Verifies that PDF report is generated correctly from an XML report and handles WebP charts.
     */
    @Test
    public void testGeneratePdfReport() throws Exception
    {
        Assert.assertTrue("Sample XML file must exist", sampleXmlFile.exists());
        Assert.assertTrue("Stylesheet file must exist", styleSheetFile.exists());

        // Prepare target folder with necessary assets (css, charts)
        final File targetDir = tempFolder.newFolder("report-output");
        final File cssDir = new File(targetDir, "css");
        cssDir.mkdirs();
        FileUtils.copyFileToDirectory(new File("config/testreport/css/pdf.css"), cssDir);
        FileUtils.copyFileToDirectory(new File("config/testreport/css/default.css"), cssDir);

        final File sampleChartsDir = new File(sampleReportDir, "charts");
        if (sampleChartsDir.exists())
        {
            FileUtils.copyDirectory(sampleChartsDir, new File(targetDir, "charts"));
        }

        // Generate a synthetic WebP chart image to verify WebP transcoding & embedding
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

        final File outputPdfFile = new File(targetDir, PdfReportGenerator.DEFAULT_PDF_FILENAME);

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("productName", "XLT");
        parameters.put("productVersion", "10.0.0");
        parameters.put("productUrl", "https://www.xceptance.com");
        parameters.put("scorecardPresent", Boolean.FALSE);
        parameters.put("pdfReportPresent", Boolean.TRUE);

        PdfReportGenerator.generatePdfReport(sampleXmlFile, targetDir, styleSheetFile, outputPdfFile, parameters);

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
     * Tests that the configuration default for PDF report is false.
     */
    @Test
    public void testConfigurationDefault()
    {
        final ReportGeneratorConfiguration config = readReportGeneratorProperties();
        Assert.assertFalse("PDF report should be disabled by default", config.isPdfReportEnabled());

        config.setPdfReportEnabled(true);
        Assert.assertTrue("PDF report should be enabled after setter call", config.isPdfReportEnabled());
    }

    /**
     * Tests that the configuration loads property from file.
     */
    @Test
    public void testConfigurationPropertyFromFile()
    {
        final ReportGeneratorConfiguration config = readReportGeneratorProperties();
        Assert.assertFalse("Initial PDF report enabled should be false", config.isPdfReportEnabled());

        appendPropertyToFile("com.xceptance.xlt.reportgenerator.pdf.enable", "true");

        final ReportGeneratorConfiguration loadedConfig = readReportGeneratorProperties();
        Assert.assertTrue("PDF report should be enabled when configured in properties", loadedConfig.isPdfReportEnabled());
    }
}
