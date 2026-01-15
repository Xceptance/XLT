/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class ReportGeneratorConfigurationTestBase
{
    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    public File homeDir;

    public File configDir;

    public File propertyFile;

    @Before
    public void setup() throws IOException
    {
        homeDir = tempFolder.getRoot();
        configDir = tempFolder.newFolder("config");

        // Create required "mastercontroller.properties" file in config directory
        new File(configDir, "mastercontroller.properties").createNewFile();

        // Create "reportgenerator.properties" file in config directory for testing
        propertyFile = new File(configDir, "reportgenerator.properties");
        propertyFile.createNewFile();
    }

    /**
     * Helper method for appending lines to the 'reportgenerator.properties' test file.
     */
    protected void appendPropertyLinesToFile(final List<String> lines)
    {
        try
        {
            Files.write(propertyFile.toPath(), lines, StandardOpenOption.APPEND);
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method for reading the contents of the "reportgenerator.properties" test file.
     */
    protected ReportGeneratorConfiguration readReportGeneratorProperties()
    {
        try
        {
            return new ReportGeneratorConfiguration(homeDir, configDir, null, null, null);
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
