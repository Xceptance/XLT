/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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

import com.xceptance.common.io.FileUtils;
import com.xceptance.xlt.TestCaseWithAClock;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the {@link ConfigurationReportProvider}
 */
public class ConfigurationReportProviderTest extends TestCaseWithAClock
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
}
