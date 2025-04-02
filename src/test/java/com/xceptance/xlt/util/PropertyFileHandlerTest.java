package com.xceptance.xlt.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

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
    public void testSetProperty_OverwriteDuplicateProperties() throws IOException
    {
        List<String> lines = List.of("foo.test1 = value1-1", "foo.test2 = value2", "foo.test1 = value1-2");
        Files.write(propertyFile.toPath(), lines);

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
}
