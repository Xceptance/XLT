package com.xceptance.xlt.report;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.report.util.MovingAverageConfiguration;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import util.JUnitParamsUtils;

@RunWith(JUnitParamsRunner.class)
public class ReportGeneratorConfigurationTest
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

    @Test
    public void commonAverage_defaultValue() throws IOException
    {
        // If no common average is configured, the default value is returned
        final MovingAverageConfiguration commonAverage = readReportGeneratorProperties().getCommonMovingAverageConfig();

        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.PERCENTAGE, commonAverage.getType());
        Assert.assertEquals(5, commonAverage.getValue());
        Assert.assertEquals("5%", commonAverage.getName());
    }

    @Test
    public void additionalAverages_defaultValue() throws IOException
    {
        // If no additional averages are configured, an empty list is returned
        Assert.assertEquals(0, readReportGeneratorProperties().getAdditionalMovingAverageConfigs().size());
    }

    @Test
    public void commonAverage_percentage() throws IOException
    {
        addCommonAverageConfig("percentage", "25%");

        final MovingAverageConfiguration commonAverage = readReportGeneratorProperties().getCommonMovingAverageConfig();

        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.PERCENTAGE, commonAverage.getType());
        Assert.assertEquals(25, commonAverage.getValue());
        Assert.assertEquals("25%", commonAverage.getName());
    }

    @Test
    public void commonAverage_time() throws IOException
    {
        addCommonAverageConfig("time", "1h15m45s");

        final MovingAverageConfiguration commonAverage = readReportGeneratorProperties().getCommonMovingAverageConfig();

        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.TIME, commonAverage.getType());
        Assert.assertEquals(4545, commonAverage.getValue());
        Assert.assertEquals("1h15m45s", commonAverage.getName());
    }

    @Test
    public void additionalAverages_singleAdditionalAverage() throws IOException
    {
        addAdditionalAverageConfig("1", "time", "15:30");

        final List<MovingAverageConfiguration> additionalAverages = readReportGeneratorProperties().getAdditionalMovingAverageConfigs();
        Assert.assertEquals(1, additionalAverages.size());

        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.TIME, additionalAverages.get(0).getType());
        Assert.assertEquals(930, additionalAverages.get(0).getValue());
        Assert.assertEquals("15:30", additionalAverages.get(0).getName());
    }

    @Test
    public void additionalAverages_multipleAdditionalAverages() throws IOException
    {
        addAdditionalAverageConfig("1", "percentage", "1%");
        addAdditionalAverageConfig("2", "percentage", "25%");
        addAdditionalAverageConfig("3", "time", "1h15m45s");

        final List<MovingAverageConfiguration> additionalAverages = readReportGeneratorProperties().getAdditionalMovingAverageConfigs();
        Assert.assertEquals(3, additionalAverages.size());

        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.PERCENTAGE, additionalAverages.get(0).getType());
        Assert.assertEquals(1, additionalAverages.get(0).getValue());
        Assert.assertEquals("1%", additionalAverages.get(0).getName());

        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.PERCENTAGE, additionalAverages.get(1).getType());
        Assert.assertEquals(25, additionalAverages.get(1).getValue());
        Assert.assertEquals("25%", additionalAverages.get(1).getName());

        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.TIME, additionalAverages.get(2).getType());
        Assert.assertEquals(4545, additionalAverages.get(2).getValue());
        Assert.assertEquals("1h15m45s", additionalAverages.get(2).getName());
    }

    @Test
    public void additionalAverages_maxNumberOfAdditionalAverages() throws IOException
    {
        // Configure the maximum allowed number of additional averages
        addAdditionalAverageConfig("1", "percentage", "2%");
        addAdditionalAverageConfig("2", "time", "1h45s");
        addAdditionalAverageConfig("3", "percentage", "40");
        addAdditionalAverageConfig("4", "time", "1250");
        addAdditionalAverageConfig("5", "time", "1:15:45");

        final List<MovingAverageConfiguration> additionalAverages = readReportGeneratorProperties().getAdditionalMovingAverageConfigs();
        Assert.assertEquals(XltConstants.REPORT_CHART_MAX_ADDITIONAL_AVERAGES, additionalAverages.size());

        // Additional average 1
        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.PERCENTAGE, additionalAverages.get(0).getType());
        Assert.assertEquals(2, additionalAverages.get(0).getValue());
        Assert.assertEquals("2%", additionalAverages.get(0).getName());

        // Additional average 2
        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.TIME, additionalAverages.get(1).getType());
        Assert.assertEquals(3645, additionalAverages.get(1).getValue());
        Assert.assertEquals("1h45s", additionalAverages.get(1).getName());

        // Additional average 3
        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.PERCENTAGE, additionalAverages.get(2).getType());
        Assert.assertEquals(40, additionalAverages.get(2).getValue());
        Assert.assertEquals("40%", additionalAverages.get(2).getName());

        // Additional average 4
        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.TIME, additionalAverages.get(3).getType());
        Assert.assertEquals(1250, additionalAverages.get(3).getValue());
        Assert.assertEquals("1250s", additionalAverages.get(3).getName());

        // Additional average 5
        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.TIME, additionalAverages.get(4).getType());
        Assert.assertEquals(4545, additionalAverages.get(4).getValue());
        Assert.assertEquals("1:15:45", additionalAverages.get(4).getName());
    }

    @Test
    public void additionalAverages_gapsBetweenIndexes() throws IOException
    {
        // Add additional averages with indexes 2 and 4, skipping indexes 1 and 3
        addAdditionalAverageConfig("2", "time", "3:01");
        addAdditionalAverageConfig("4", "percentage", "25%");

        final List<MovingAverageConfiguration> additionalAverages = readReportGeneratorProperties().getAdditionalMovingAverageConfigs();
        Assert.assertEquals(2, additionalAverages.size());

        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.TIME, additionalAverages.get(0).getType());
        Assert.assertEquals(181, additionalAverages.get(0).getValue());
        Assert.assertEquals("3:01", additionalAverages.get(0).getName());

        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.PERCENTAGE, additionalAverages.get(1).getType());
        Assert.assertEquals(25, additionalAverages.get(1).getValue());
        Assert.assertEquals("25%", additionalAverages.get(1).getName());
    }

    @Test
    public void additionalAverages_unsortedIndexes() throws IOException
    {
        // Add additional averages with indexes out of order
        addAdditionalAverageConfig("3", "percentage", "25%");
        addAdditionalAverageConfig("1", "percentage", "99%");
        addAdditionalAverageConfig("2", "time", "1h15m45s");

        // Additional averages should be sorted by index
        final List<MovingAverageConfiguration> additionalAverages = readReportGeneratorProperties().getAdditionalMovingAverageConfigs();
        Assert.assertEquals(3, additionalAverages.size());

        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.PERCENTAGE, additionalAverages.get(0).getType());
        Assert.assertEquals(99, additionalAverages.get(0).getValue());
        Assert.assertEquals("99%", additionalAverages.get(0).getName());

        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.TIME, additionalAverages.get(1).getType());
        Assert.assertEquals(4545, additionalAverages.get(1).getValue());
        Assert.assertEquals("1h15m45s", additionalAverages.get(1).getName());

        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.PERCENTAGE, additionalAverages.get(2).getType());
        Assert.assertEquals(25, additionalAverages.get(2).getValue());
        Assert.assertEquals("25%", additionalAverages.get(2).getName());
    }

    @Test
    public void commonAndAdditionalAverages() throws IOException
    {
        // Configure common average and 2 additional averages
        addAdditionalAverageConfig("4", "time", "2m30s");
        addCommonAverageConfig("percentage", "25%");
        addAdditionalAverageConfig("1", "percentage", "1");

        final ReportGeneratorConfiguration config = readReportGeneratorProperties();

        // Common average
        final MovingAverageConfiguration commonAverage = config.getCommonMovingAverageConfig();
        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.PERCENTAGE, commonAverage.getType());
        Assert.assertEquals(25, commonAverage.getValue());
        Assert.assertEquals("25%", commonAverage.getName());

        // Additional average 1
        final List<MovingAverageConfiguration> additionalAverages = config.getAdditionalMovingAverageConfigs();
        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.PERCENTAGE, additionalAverages.get(0).getType());
        Assert.assertEquals(1, additionalAverages.get(0).getValue());
        Assert.assertEquals("1%", additionalAverages.get(0).getName());

        // Additional average 2
        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.TIME, additionalAverages.get(1).getType());
        Assert.assertEquals(150, additionalAverages.get(1).getValue());
        Assert.assertEquals("2m30s", additionalAverages.get(1).getName());
    }

    @Test
    public void commonAndAdditionalAverages_blankProperties() throws IOException
    {
        // Configure common average and 2 additional averages, all with blank types and values
        addAdditionalAverageConfig("4", "", "\t");
        addCommonAverageConfig("   ", "");
        addAdditionalAverageConfig("1", " \t  ", " ");

        final ReportGeneratorConfiguration config = readReportGeneratorProperties();

        // Common average is set to the default value
        final MovingAverageConfiguration commonAverage = config.getCommonMovingAverageConfig();
        Assert.assertEquals(MovingAverageConfiguration.MovingAverageType.PERCENTAGE, commonAverage.getType());
        Assert.assertEquals(5, commonAverage.getValue());
        Assert.assertEquals("5%", commonAverage.getName());

        // Additional averages are empty
        Assert.assertEquals(0, config.getAdditionalMovingAverageConfigs().size());
    }

    @Test
    @Parameters(source = JUnitParamsUtils.BlankStringOrNullParamProvider.class)
    public void commonAverage_incompleteConfiguration_valueIsMissingOrBlank(final String blankValueOrNull)
    {
        // Configure 'type' property, set 'value' property to blank value or skip it entirely
        addCommonAverageType("percentage");
        if (blankValueOrNull != null)
        {
            addCommonAverageValue(blankValueOrNull);
        }
        final XltException exception = Assert.assertThrows(XltException.class, this::readReportGeneratorProperties);
        Assert.assertEquals(String.format(ReportGeneratorConfiguration.ERROR_AVERAGE_PROPERTY_MISSING, getCommonAverageTypeKey(),
                                          getCommonAverageValueKey()),
                            exception.getMessage());
    }

    @Test
    @Parameters(source = JUnitParamsUtils.BlankStringOrNullParamProvider.class)
    public void additionalAverages_incompleteConfiguration_valueIsMissingOrBlank(final String blankValueOrNull)
    {
        // Configure 'type' property, set 'value' property to blank value or skip it entirely
        addAdditionalAverageType("1", "percentage");
        if (blankValueOrNull != null)
        {
            addAdditionalAverageValue("1", blankValueOrNull);
        }
        final XltException exception = Assert.assertThrows(XltException.class, this::readReportGeneratorProperties);
        Assert.assertEquals(String.format(ReportGeneratorConfiguration.ERROR_AVERAGE_PROPERTY_MISSING, getAdditionalAverageTypeKey("1"),
                                          getAdditionalAverageValueKey("1")),
                            exception.getMessage());
    }

    @Test
    @Parameters(source = JUnitParamsUtils.BlankStringOrNullParamProvider.class)
    public void commonAverage_incompleteConfiguration_typeIsMissingOrBlank(final String blankValueOrNull)
    {
        // Configure 'value' property, set 'type' property to blank value or skip it entirely
        addCommonAverageValue("50");
        if (blankValueOrNull != null)
        {
            addCommonAverageType(blankValueOrNull);
        }
        final XltException exception = Assert.assertThrows(XltException.class, this::readReportGeneratorProperties);
        Assert.assertEquals(String.format(ReportGeneratorConfiguration.ERROR_AVERAGE_PROPERTY_MISSING, getCommonAverageValueKey(),
                                          getCommonAverageTypeKey()),
                            exception.getMessage());
    }

    @Test
    @Parameters(source = JUnitParamsUtils.BlankStringOrNullParamProvider.class)
    public void additionalAverages_incompleteConfiguration_typeIsMissingOrBlank(final String blankValueOrNull)
    {
        // Configure 'value' property, set 'type' property to blank value or skip it entirely
        addAdditionalAverageValue("1", "50");
        if (blankValueOrNull != null)
        {
            addAdditionalAverageType("1", blankValueOrNull);
        }
        final XltException exception = Assert.assertThrows(XltException.class, this::readReportGeneratorProperties);
        Assert.assertEquals(String.format(ReportGeneratorConfiguration.ERROR_AVERAGE_PROPERTY_MISSING, getAdditionalAverageValueKey("1"),
                                          getAdditionalAverageTypeKey("1")),
                            exception.getMessage());
    }

    @Test
    public void commonAverage_invalidType()
    {
        addCommonAverageConfig("invalidType", "25");
        final XltException exception = Assert.assertThrows(XltException.class, this::readReportGeneratorProperties);
        Assert.assertEquals(String.format(ReportGeneratorConfiguration.ERROR_AVERAGE_TYPE_INVALID, "invalidType",
                                          getCommonAverageTypeKey()),
                            exception.getMessage());
    }

    @Test
    public void additionalAverages_invalidType()
    {
        addAdditionalAverageConfig("1", "invalidType", "25");
        final XltException exception = Assert.assertThrows(XltException.class, this::readReportGeneratorProperties);
        Assert.assertEquals(String.format(ReportGeneratorConfiguration.ERROR_AVERAGE_TYPE_INVALID, "invalidType",
                                          getAdditionalAverageTypeKey("1")),
                            exception.getMessage());
    }

    @Test
    public void commonAverage_invalidPercentage()
    {
        addCommonAverageConfig("percentage", "invalidValue");
        final XltException exception = Assert.assertThrows(XltException.class, this::readReportGeneratorProperties);
        Assert.assertEquals(String.format(ReportGeneratorConfiguration.ERROR_INVALID_PROPERTY_VALUE_FORMAT, getCommonAverageValueKey()),
                            exception.getMessage());
    }

    @Test
    public void additionalAverages_invalidPercentage()
    {
        addAdditionalAverageConfig("1", "percentage", "invalidValue");
        final XltException exception = Assert.assertThrows(XltException.class, this::readReportGeneratorProperties);
        Assert.assertEquals(String.format(ReportGeneratorConfiguration.ERROR_INVALID_PROPERTY_VALUE_FORMAT,
                                          getAdditionalAverageValueKey("1")),
                            exception.getMessage());
    }

    @Test
    public void commonAverage_invalidTime()
    {
        addCommonAverageConfig("time", "invalidValue");
        final XltException exception = Assert.assertThrows(XltException.class, this::readReportGeneratorProperties);
        Assert.assertEquals(String.format(ReportGeneratorConfiguration.ERROR_INVALID_PROPERTY_VALUE_FORMAT, getCommonAverageValueKey()),
                            exception.getMessage());
    }

    @Test
    public void additionalAverages_invalidTime()
    {
        addAdditionalAverageConfig("1", "time", "invalidValue");
        final XltException exception = Assert.assertThrows(XltException.class, this::readReportGeneratorProperties);
        Assert.assertEquals(String.format(ReportGeneratorConfiguration.ERROR_INVALID_PROPERTY_VALUE_FORMAT,
                                          getAdditionalAverageValueKey("1")),
                            exception.getMessage());
    }

    @Test
    @Parameters(value =
        {
            "-1%", "0%", "101%", "2500%"
    })
    public void commonAverage_percentageOutOfBounds(final String percentage)
    {
        addCommonAverageConfig("percentage", percentage);
        final XltException exception = Assert.assertThrows(XltException.class, this::readReportGeneratorProperties);
        Assert.assertEquals(String.format(ReportGeneratorConfiguration.ERROR_AVERAGE_PERCENTAGE_OUT_OF_BOUNDS, getCommonAverageValueKey(),
                                          percentage),
                            exception.getMessage());
    }

    @Test
    @Parameters(value =
        {
            "-1%", "0%", "101%", "2500%"
    })
    public void additionalAverages_percentageOutOfBounds(final String percentage)
    {
        addAdditionalAverageConfig("1", "percentage", percentage);
        final XltException exception = Assert.assertThrows(XltException.class, this::readReportGeneratorProperties);
        Assert.assertEquals(String.format(ReportGeneratorConfiguration.ERROR_AVERAGE_PERCENTAGE_OUT_OF_BOUNDS,
                                          getAdditionalAverageValueKey("1"), percentage),
                            exception.getMessage());
    }

    @Test
    @Parameters(value =
        {
            "0", "0s", "0:00:00", "0h0m0s"
    })
    public void commonAverage_timeOutOfBounds(final String time)
    {
        addCommonAverageConfig("time", time);
        final XltException exception = Assert.assertThrows(XltException.class, this::readReportGeneratorProperties);
        Assert.assertEquals(String.format(ReportGeneratorConfiguration.ERROR_AVERAGE_TIME_OUT_OF_BOUNDS, getCommonAverageValueKey(), time),
                            exception.getMessage());
    }

    @Test
    @Parameters(value =
        {
            "0", "0s", "0:00:00", "0h0m0s"
    })
    public void additionalAverages_timeOutOfBounds(final String time)
    {
        addAdditionalAverageConfig("1", "time", time);
        final XltException exception = Assert.assertThrows(XltException.class, this::readReportGeneratorProperties);
        Assert.assertEquals(String.format(ReportGeneratorConfiguration.ERROR_AVERAGE_TIME_OUT_OF_BOUNDS, getAdditionalAverageValueKey("1"),
                                          time),
                            exception.getMessage());
    }

    @Test
    public void additionalAverages_nonNumericIndex()
    {
        addAdditionalAverageConfig("abc", "percentage", "25%");
        final XltException exception = Assert.assertThrows(XltException.class, this::readReportGeneratorProperties);
        Assert.assertEquals(ReportGeneratorConfiguration.ERROR_AVERAGE_INDEX_INVALID, exception.getMessage());
    }

    @Test
    public void additionalAverages_indexTooLow()
    {
        addAdditionalAverageConfig("0", "percentage", "25%");
        final XltException exception = Assert.assertThrows(XltException.class, this::readReportGeneratorProperties);
        Assert.assertEquals(ReportGeneratorConfiguration.ERROR_AVERAGE_INDEX_INVALID, exception.getMessage());
    }

    @Test
    public void additionalAverages_indexTooHigh()
    {
        addAdditionalAverageConfig(String.valueOf(XltConstants.REPORT_CHART_MAX_ADDITIONAL_AVERAGES + 1), "percentage", "25%");
        final XltException exception = Assert.assertThrows(XltException.class, this::readReportGeneratorProperties);
        Assert.assertEquals(ReportGeneratorConfiguration.ERROR_AVERAGE_INDEX_INVALID, exception.getMessage());
    }

    /**
     * Helper method for reading the contents of the "reportgenerator.properties" test file.
     */
    private ReportGeneratorConfiguration readReportGeneratorProperties() throws IOException
    {
        return new ReportGeneratorConfiguration(homeDir, configDir, null, null, null);
    }

    /**
     * Helper method to add the properties for the common average configuration with the given type and value.
     */
    private void addCommonAverageConfig(final String type, final String value)
    {
        addCommonAverageType(type);
        addCommonAverageValue(value);
    }

    /**
     * Helper method to add the properties for an additional average configuration with the given index, type and value.
     */
    private void addAdditionalAverageConfig(final String index, final String type, final String value)
    {
        addAdditionalAverageType(index, type);
        addAdditionalAverageValue(index, value);
    }

    /**
     * Helper method to add just the "type" property for the common average configuration.
     */
    private void addCommonAverageType(final String type)
    {
        appendPropertyToFile(getCommonAverageTypeKey(), type);
    }

    /**
     * Helper method to add just the "value" property for the common average configuration.
     */
    private void addCommonAverageValue(final String value)
    {
        appendPropertyToFile(getCommonAverageValueKey(), value);
    }

    /**
     * Helper method to add just the "type" property for an additional average configuration with the given index.
     */
    private void addAdditionalAverageType(final String index, final String type)
    {
        appendPropertyToFile(getAdditionalAverageTypeKey(index), type);
    }

    /**
     * Helper method to add just the "value" property for an additional average configuration with the given index.
     */
    private void addAdditionalAverageValue(final String index, final String value)
    {
        appendPropertyToFile(getAdditionalAverageValueKey(index), value);
    }

    /**
     * Helper method for writing a property to the 'reportgenerator.properties' test file.
     */
    private void appendPropertyToFile(final String key, final String value)
    {
        try
        {
            Files.write(propertyFile.toPath(), List.of(key + " = " + value), StandardOpenOption.APPEND);
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get key for common average type property.
     */
    private String getCommonAverageTypeKey()
    {
        return ReportGeneratorConfiguration.PROP_CHARTS_AVERAGE_COMMON + ReportGeneratorConfiguration.PROP_SUFFIX_TYPE;
    }

    /**
     * Get key for common average value property.
     */
    private String getCommonAverageValueKey()
    {
        return ReportGeneratorConfiguration.PROP_CHARTS_AVERAGE_COMMON + ReportGeneratorConfiguration.PROP_SUFFIX_VALUE;
    }

    /**
     * Get key for additional average type property with the given index.
     */
    private String getAdditionalAverageTypeKey(final String index)
    {
        return ReportGeneratorConfiguration.PROP_CHARTS_AVERAGES_ADDITIONAL + index + "." + ReportGeneratorConfiguration.PROP_SUFFIX_TYPE;
    }

    /**
     * Get key for additional average value property with the given index.
     */
    private String getAdditionalAverageValueKey(final String index)
    {
        return ReportGeneratorConfiguration.PROP_CHARTS_AVERAGES_ADDITIONAL + index + "." + ReportGeneratorConfiguration.PROP_SUFFIX_VALUE;
    }
}
