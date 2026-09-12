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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.common.io.FileUtils;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;

/**
 * Unit tests for the {@link ConfigurationReportProvider}
 */
public class ConfigurationReportProviderTest
{
    @Test
    public void testSecretPropertiesAreMaskedInTheOutput() throws IOException
    {
        final Path testDir = Files.createTempDirectory("reporttest-");
        try
        {
            final Path secretPath = testDir.resolve("config").resolve(XltConstants.SECRET_PROPERTIES_FILENAME);
            Files.createDirectories(secretPath.getParent());
            Files.write(secretPath, "value=Some very secret Value\n".getBytes(StandardCharsets.ISO_8859_1));
            final ConfigurationReportProvider provider = new ConfigurationReportProvider();
            ReportGeneratorConfiguration config = new ReportGeneratorConfiguration();
            config.setReportDirectory(testDir.toFile());
            provider.setConfiguration(config);

            final ConfigurationReport report = (ConfigurationReport) provider.createReportFragment();

            Assert.assertEquals(XltConstants.MASK_PROPERTIES_HIDETEXT, report.properties.getProperty("secret.value"));
        }
        finally
        {
            FileUtils.deleteDirectoryRelaxed(testDir.toFile());
        }
    }

    // --- processComment tests ---

    @Test
    public void testProcessComment_plainString()
    {
        Assert.assertEquals("Hello World", ConfigurationReportProvider.processComment("Hello World"));
    }

    @Test
    public void testProcessComment_rawHtml()
    {
        Assert.assertEquals("<b>Bold</b>", ConfigurationReportProvider.processComment("<b>Bold</b>"));
    }

    @Test
    public void testProcessComment_markdown()
    {
        final String result = ConfigurationReportProvider.processComment("::markdown::**bold** text");
        Assert.assertTrue("Should start with markdown div", result.startsWith("<div class=\"markdown\">"));
        Assert.assertTrue("Should end with closing div", result.endsWith("</div>"));
        Assert.assertTrue("Should contain <strong>", result.contains("<strong>bold</strong>"));
    }

    @Test
    public void testProcessComment_caseInsensitive()
    {
        final String result = ConfigurationReportProvider.processComment("::Markdown::**bold** text");
        Assert.assertTrue("Should start with markdown div", result.startsWith("<div class=\"markdown\">"));
        Assert.assertTrue("Should contain <strong>", result.contains("<strong>bold</strong>"));

        final String result2 = ConfigurationReportProvider.processComment("::MARKDOWN::**bold** text");
        Assert.assertTrue("Upper case should also work", result2.startsWith("<div class=\"markdown\">"));
    }

    @Test
    public void testProcessComment_markdownTable()
    {
        final String table = "::markdown::| A | B |\n|---|---|\n| 1 | 2 |";
        final String result = ConfigurationReportProvider.processComment(table);
        Assert.assertTrue("Should start with markdown div", result.startsWith("<div class=\"markdown\">"));
        Assert.assertTrue("Should contain table element", result.contains("<table>"));
    }

    @Test
    public void testProcessComment_null()
    {
        Assert.assertNull(ConfigurationReportProvider.processComment(null));
    }

    @Test
    public void testProcessComment_prefixOnly()
    {
        Assert.assertNull(ConfigurationReportProvider.processComment("::markdown::"));
    }

    @Test
    public void testRenderMarkdown()
    {
        Assert.assertNull(ConfigurationReportProvider.renderMarkdown(null));
        Assert.assertNull(ConfigurationReportProvider.renderMarkdown("   "));

        final String result = ConfigurationReportProvider.renderMarkdown("# Performance Evaluation\n\n* Metric 1 passed\n* Metric 2 passed");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.startsWith("<div class=\"markdown\">"));
        Assert.assertTrue(result.contains("<h1>Performance Evaluation</h1>"));
        Assert.assertTrue(result.contains("<li>Metric 1 passed</li>"));
        Assert.assertTrue(result.endsWith("</div>"));
    }

    @Test
    public void testRatingPropertiesExtraction() throws IOException
    {
        final Path testDir = Files.createTempDirectory("ratingtest-");
        try
        {
            final Path defaultPath = testDir.resolve("config").resolve(XltConstants.DEFAULT_PROPERTY_FILENAME);
            Files.createDirectories(defaultPath.getParent());
            final String propsContent = "com.xceptance.xtc.loadtest.rating.score = A\n" +
                                       "com.xceptance.xtc.loadtest.rating.summary = All performance criteria passed successfully.\n" +
                                       "com.xceptance.xtc.loadtest.rating.evaluation = ### Detailed Analysis\\n* 0 errors recorded\\n* TPS exceeded target\n";
            Files.write(defaultPath, propsContent.getBytes(StandardCharsets.ISO_8859_1));

            final ConfigurationReportProvider provider = new ConfigurationReportProvider();
            final ReportGeneratorConfiguration config = new ReportGeneratorConfiguration();
            config.setReportDirectory(testDir.toFile());
            provider.setConfiguration(config);

            final ConfigurationReport report = (ConfigurationReport) provider.createReportFragment();

            Assert.assertEquals("A", report.rating.score);
            Assert.assertEquals("All performance criteria passed successfully.", report.rating.summary);
            Assert.assertNotNull(report.rating.evaluation);
            Assert.assertTrue(report.rating.evaluation.startsWith("<div class=\"markdown\">"));
            Assert.assertTrue(report.rating.evaluation.contains("<h3>Detailed Analysis</h3>"));
            Assert.assertTrue(report.rating.evaluation.contains("0 errors recorded"));
        }
        finally
        {
            FileUtils.deleteDirectoryRelaxed(testDir.toFile());
        }
    }

    /**
     * Using empty properties should result in <code>null</code> rating field.
     */
    @Test
    public void testEmptyProperties() throws IOException
    {
        final Path testDir = Files.createTempDirectory("ratingtest2-");
        try
        {
            final Path defaultPath = testDir.resolve("config").resolve(XltConstants.DEFAULT_PROPERTY_FILENAME);
            Files.createDirectories(defaultPath.getParent());
            final String propsContent = "";
            Files.write(defaultPath, propsContent.getBytes(StandardCharsets.ISO_8859_1));

            final ConfigurationReportProvider provider = new ConfigurationReportProvider();
            final ReportGeneratorConfiguration config = new ReportGeneratorConfiguration();
            config.setReportDirectory(testDir.toFile());
            provider.setConfiguration(config);

            final ConfigurationReport report = (ConfigurationReport) provider.createReportFragment();

            Assert.assertNull(report.rating);
        }
        finally
        {
            FileUtils.deleteDirectoryRelaxed(testDir.toFile());
        }
    }

    @Test
    public void testEvaluationMultilineEscapedInProperties() throws IOException
    {
        final Path testDir = Files.createTempDirectory("ratingtest3-");
        try
        {
            final Path defaultPath = testDir.resolve("config").resolve(XltConstants.DEFAULT_PROPERTY_FILENAME);
            Files.createDirectories(defaultPath.getParent());
            // Standard multiline continuation in .properties file with trailing backslash
            final String propsContent = "com.xceptance.xtc.loadtest.rating.score = B\n" +
                                       "com.xceptance.xtc.loadtest.rating.summary = Good performance with minor latency spikes.\n" +
                                       "com.xceptance.xtc.loadtest.rating.evaluation = Paragraph 1: Initial warmup.\\n\\\n" +
                                       "                                               Paragraph 2: Steady state.\n";
            Files.write(defaultPath, propsContent.getBytes(StandardCharsets.ISO_8859_1));

            final ConfigurationReportProvider provider = new ConfigurationReportProvider();
            final ReportGeneratorConfiguration config = new ReportGeneratorConfiguration();
            config.setReportDirectory(testDir.toFile());
            provider.setConfiguration(config);

            final ConfigurationReport report = (ConfigurationReport) provider.createReportFragment();

            Assert.assertEquals("B", report.rating.score);
            Assert.assertEquals("Good performance with minor latency spikes.", report.rating.summary);
            Assert.assertNotNull(report.rating.evaluation);
            Assert.assertTrue(report.rating.evaluation.contains("Paragraph 1: Initial warmup."));
            Assert.assertTrue(report.rating.evaluation.contains("Paragraph 2: Steady state."));
        }
        finally
        {
            FileUtils.deleteDirectoryRelaxed(testDir.toFile());
        }
    }

    @Test
    public void testRatingPropertiesOverrideFromConfiguration() throws IOException
    {
        final Path testDir = Files.createTempDirectory("ratingtest4-");
        try
        {
            final Path defaultPath = testDir.resolve("config").resolve(XltConstants.DEFAULT_PROPERTY_FILENAME);
            Files.createDirectories(defaultPath.getParent());
            final String propsContent = "com.xceptance.xtc.loadtest.rating.score = C\n" +
                                       "com.xceptance.xtc.loadtest.rating.summary = From file summary.\n";
            Files.write(defaultPath, propsContent.getBytes(StandardCharsets.ISO_8859_1));

            final ConfigurationReportProvider provider = new ConfigurationReportProvider();
            final Properties cliProperties = new Properties();
            cliProperties.setProperty("com.xceptance.xtc.loadtest.rating.score", "A");
            cliProperties.setProperty("com.xceptance.xtc.loadtest.rating.summary", "Injected from command line.");

            final ReportGeneratorConfiguration config = new ReportGeneratorConfiguration();
            config.setReportDirectory(testDir.toFile());
            config.addProperties(cliProperties);
            provider.setConfiguration(config);

            final ConfigurationReport report = (ConfigurationReport) provider.createReportFragment();

            Assert.assertEquals("A", report.rating.score);
            Assert.assertEquals("Injected from command line.", report.rating.summary);
        }
        finally
        {
            FileUtils.deleteDirectoryRelaxed(testDir.toFile());
        }
    }

    @Test
    public void testRatingNormalizationAplus() throws IOException
    {
        final Path testDir = Files.createTempDirectory("ratingtest5-");
        try
        {
            final Path defaultPath = testDir.resolve("config").resolve(XltConstants.DEFAULT_PROPERTY_FILENAME);
            Files.createDirectories(defaultPath.getParent());
            final String propsContent = "com.xceptance.xtc.loadtest.rating.score = Aplus\n";
            Files.write(defaultPath, propsContent.getBytes(StandardCharsets.ISO_8859_1));

            final ConfigurationReportProvider provider = new ConfigurationReportProvider();
            final ReportGeneratorConfiguration config = new ReportGeneratorConfiguration();
            config.setReportDirectory(testDir.toFile());
            provider.setConfiguration(config);

            final ConfigurationReport report = (ConfigurationReport) provider.createReportFragment();

            Assert.assertEquals("A+", report.rating.score);
        }
        finally
        {
            FileUtils.deleteDirectoryRelaxed(testDir.toFile());
        }
    }
}
