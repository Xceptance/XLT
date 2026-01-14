package com.xceptance.xlt.report;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

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
