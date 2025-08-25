package com.xceptance.xlt.util;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import util.JUnitParamsUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(JUnitParamsRunner.class)
public class PropertyFileHandlerTest
{
    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private File propertyFile;

    private PropertyFileHandler propertyFileHandler;

    @Before
    public void init() throws IOException
    {
        propertyFile = tempFolder.newFile("test.properties");
        propertyFileHandler = new PropertyFileHandler(propertyFile);
    }

    @Test
    public void testSetProperty() throws IOException
    {
        propertyFileHandler.setProperty("foo.test1", "value1");
        List<String> lines = Files.readAllLines(propertyFile.toPath());
        Assert.assertEquals(1, lines.size());
        Assert.assertTrue(lines.contains("foo.test1 = value1"));

        propertyFileHandler.setProperty("foo.bar.test2", "value2");
        lines = Files.readAllLines(propertyFile.toPath());
        Assert.assertEquals(2, lines.size());
        Assert.assertTrue(lines.contains("foo.test1 = value1"));
        Assert.assertTrue(lines.contains("foo.bar.test2 = value2"));

        propertyFileHandler.setProperty("foo.test1", "updatedValue1");
        lines = Files.readAllLines(propertyFile.toPath());
        Assert.assertEquals(2, lines.size());
        Assert.assertTrue(lines.contains("foo.test1 = updatedValue1"));
        Assert.assertTrue(lines.contains("foo.bar.test2 = value2"));
    }

    @Test
    @Parameters(source = JUnitParamsUtils.BlankStringOrNullParamProvider.class)
    public void testSetProperty_nullOrBlankKey(final String blankStringOrNull) throws IOException
    {
        propertyFileHandler.setProperty(blankStringOrNull, "value1");
        Assert.assertTrue(Files.readAllLines(propertyFile.toPath()).isEmpty());
    }

    @Test
    @Parameters(source = JUnitParamsUtils.BlankStringOrNullParamProvider.class)
    public void testSetProperty_nullOrBlankValue(final String blankStringOrNull) throws IOException
    {
        propertyFileHandler.setProperty("foo.test1", blankStringOrNull);
        Assert.assertTrue(Files.readAllLines(propertyFile.toPath()).isEmpty());
    }

    @Test
    public void testSetProperty_OverwriteDuplicateProperties() throws IOException
    {
        // write and confirm initial lines containing duplicates
        Files.write(propertyFile.toPath(), List.of("foo.test1 = value1-1", "foo.test2 = value2", "foo.test1 = value1-2"));
        List<String> lines = Files.readAllLines(propertyFile.toPath());
        Assert.assertEquals(3, lines.size());
        Assert.assertTrue(lines.contains("foo.test1 = value1-1"));
        Assert.assertTrue(lines.contains("foo.test2 = value2"));
        Assert.assertTrue(lines.contains("foo.test1 = value1-2"));

        // overwrite duplicate properties
        propertyFileHandler.setProperty("foo.test1", "updatedValue1");
        lines = Files.readAllLines(propertyFile.toPath());
        Assert.assertEquals(2, lines.size());
        Assert.assertTrue(lines.contains("foo.test1 = updatedValue1"));
        Assert.assertTrue(lines.contains("foo.test2 = value2"));
    }

    @Test
    public void testSetProperties() throws IOException
    {
        propertyFileHandler.setProperties(Map.of("foo.test1", "value1", "foo.test2", "value2"));
        List<String> lines = Files.readAllLines(propertyFile.toPath());
        Assert.assertEquals(2, lines.size());
        Assert.assertTrue(lines.contains("foo.test1 = value1"));
        Assert.assertTrue(lines.contains("foo.test2 = value2"));

        propertyFileHandler.setProperties(Map.of("foo.bar.test3", "value3"));
        lines = Files.readAllLines(propertyFile.toPath());
        Assert.assertEquals(3, lines.size());
        Assert.assertTrue(lines.contains("foo.test1 = value1"));
        Assert.assertTrue(lines.contains("foo.test2 = value2"));
        Assert.assertTrue(lines.contains("foo.bar.test3 = value3"));

        propertyFileHandler.setProperties(Map.of("foo.test2", "updatedValue2"));
        lines = Files.readAllLines(propertyFile.toPath());
        Assert.assertEquals(3, lines.size());
        Assert.assertTrue(lines.contains("foo.test1 = value1"));
        Assert.assertTrue(lines.contains("foo.test2 = updatedValue2"));
        Assert.assertTrue(lines.contains("foo.bar.test3 = value3"));

        propertyFileHandler.setProperties(Map.of("foo.test2", "updatedValue2-2", "foo.bar.test3", "updatedValue3", "test4", "value4"));
        lines = Files.readAllLines(propertyFile.toPath());
        Assert.assertEquals(4, lines.size());
        Assert.assertTrue(lines.contains("foo.test1 = value1"));
        Assert.assertTrue(lines.contains("foo.test2 = updatedValue2-2"));
        Assert.assertTrue(lines.contains("foo.bar.test3 = updatedValue3"));
        Assert.assertTrue(lines.contains("test4 = value4"));
    }

    @Test
    @Parameters(source = JUnitParamsUtils.BlankStringOrNullParamProvider.class)
    public void testSetProperties_nullOrBlankKeyOrValue(final String blankStringOrNull) throws IOException
    {
        // map contains only properties with null/blank keys or values, so none are set
        final Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put(blankStringOrNull, "value");
        propertyMap.put("foo.test.invalid", blankStringOrNull);
        propertyFileHandler.setProperties(propertyMap);
        Assert.assertTrue(Files.readAllLines(propertyFile.toPath()).isEmpty());

        // map now contains two invalid and one valid property, so the valid one is set
        propertyMap.put("foo.test1", "value1");
        propertyFileHandler.setProperties(propertyMap);
        final List<String> lines = Files.readAllLines(propertyFile.toPath());
        Assert.assertEquals(1, lines.size());
        Assert.assertTrue(lines.contains("foo.test1 = value1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProperties_propertyMapIsNull()
    {
        propertyFileHandler.setProperties(null);
    }

    @Test
    public void testAppendProperties_appendMultipleTimes() throws IOException
    {
        final String[] initialLines =
            {
                "foo.test1 = value1", //
                "foo.test2 = value2", //
                "foo.test3 = value3" //
            };
        Files.write(propertyFile.toPath(), Arrays.asList(initialLines));
        Assert.assertArrayEquals(initialLines, Files.readAllLines(propertyFile.toPath()).toArray());

        propertyFileHandler.appendProperties(Map.of("foo.test2", "updatedValue2", "foo.test4", "value4"), "First Comment");
        Assert.assertArrayEquals(new String[]
            {
                "foo.test1 = value1", //
                "foo.test2 = value2", //
                "foo.test3 = value3", //
                "", //
                "# First Comment", //
                "foo.test2 = updatedValue2", //
                "foo.test4 = value4" //
            }, Files.readAllLines(propertyFile.toPath()).toArray());

        propertyFileHandler.appendProperties(Map.of("foo.test2", "updatedValue2-2", "foo.test1", "updatedValue1"), "Second Comment");
        Assert.assertArrayEquals(new String[]
            {
                "foo.test1 = value1", //
                "foo.test2 = value2", //
                "foo.test3 = value3", //
                "", //
                "# First Comment", //
                "foo.test2 = updatedValue2", //
                "foo.test4 = value4", //
                "", //
                "# Second Comment", //
                "foo.test1 = updatedValue1", //
                "foo.test2 = updatedValue2-2", //
            }, Files.readAllLines(propertyFile.toPath()).toArray());
    }

    @Test
    @Parameters(value =
        {
            "abb|abc|acb", //
            "abc|acb|abb", //
            "acb|abc|abb"  //
    })
    public void testAppendProperties_propertiesAreAppendedInAlphabeticalOrder(final String key1, final String key2, final String key3)
        throws IOException
    {
        final Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put(key1, "value-" + key1);
        propertyMap.put(key2, "value-" + key2);
        propertyMap.put(key3, "value-" + key3);

        propertyFileHandler.appendProperties(propertyMap, null);
        Assert.assertArrayEquals(new String[]
            {
                "", //
                "abb = value-abb", //
                "abc = value-abc", //
                "acb = value-acb" //
            }, Files.readAllLines(propertyFile.toPath()).toArray());
    }

    @Test
    @Parameters(source = JUnitParamsUtils.BlankStringOrNullParamProvider.class)
    public void testAppendProperties_noCommentProvided(final String blankStringOrNull) throws IOException
    {
        final String[] initialLines =
            {
                "foo.test1 = value1"
            };
        Files.write(propertyFile.toPath(), Arrays.asList(initialLines));
        Assert.assertArrayEquals(initialLines, Files.readAllLines(propertyFile.toPath()).toArray());

        // comment is null or blank
        propertyFileHandler.appendProperties(Map.of("foo.test2", "value2"), blankStringOrNull);
        Assert.assertArrayEquals(new String[]
            {
                "foo.test1 = value1", //
                "", //
                "foo.test2 = value2" //
            }, Files.readAllLines(propertyFile.toPath()).toArray());
    }

    @Test
    @Parameters(source = JUnitParamsUtils.BlankStringOrNullParamProvider.class)
    public void testAppendProperties_skipInvalidProperties(final String blankStringOrNull) throws IOException
    {
        final String[] initialLines =
            {
                "foo.test1 = value1",
            };
        Files.write(propertyFile.toPath(), Arrays.asList(initialLines));
        Assert.assertArrayEquals(initialLines, Files.readAllLines(propertyFile.toPath()).toArray());

        // map contains only properties with null/blank keys or values, so none are appended
        final Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put(blankStringOrNull, "value");
        propertyMap.put("foo.test.invalid", blankStringOrNull);
        propertyFileHandler.appendProperties(propertyMap, "All properties skipped");
        Assert.assertArrayEquals(new String[]
            {
                "foo.test1 = value1", //
                "", //
                "# All properties skipped" //
            }, Files.readAllLines(propertyFile.toPath()).toArray());

        // map now contains two invalid and one valid property, so the valid one is appended
        propertyMap.put("foo.test2", "value2");
        propertyFileHandler.appendProperties(propertyMap, "All but one property skipped");
        Assert.assertArrayEquals(new String[]
            {
                "foo.test1 = value1", //
                "", //
                "# All properties skipped", //
                "", //
                "# All but one property skipped", //
                "foo.test2 = value2" //
            }, Files.readAllLines(propertyFile.toPath()).toArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendProperties_propertyMapIsNull()
    {
        propertyFileHandler.appendProperties(null, "abc");
    }
}
