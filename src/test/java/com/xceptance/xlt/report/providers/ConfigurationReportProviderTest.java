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
        // prefix only with no content should be returned unchanged
        Assert.assertEquals("<div class=\"markdown\"></div>", ConfigurationReportProvider.processComment("::markdown::"));
    }
}

