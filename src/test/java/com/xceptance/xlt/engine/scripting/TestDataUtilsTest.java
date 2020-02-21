/*
 * File: TestDataUtilsTest.java
 * Created on: Aug 8, 2014
 * 
 * Copyright 2014
 * Xceptance Software Technologies GmbH, Germany.
 */
package com.xceptance.xlt.engine.scripting;

import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.xceptance.common.util.CsvUtils;
import com.xceptance.xlt.AbstractXLTTestCase;
import com.xceptance.xlt.engine.XltExecutionContext;

/**
 * Tests the implementation of the utility class {@link TestDataUtils}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class TestDataUtilsTest extends AbstractXLTTestCase
{
    private static final String DUMMY_SCRIPT_FILE_NAME_PREFIX = "dummy_script";

    private static File DUMMY_SCRIPT_FILE;

    private static final File TEST_CLASS_BASEDIR;

    private static final Charset CS = Charset.forName("UTF-8");

    private static final Map<String, String> TEST_DATA = new HashMap<String, String>();

    static
    {
        try
        {
            TEST_CLASS_BASEDIR = new File(TestDataUtils.class.getResource(TestDataUtils.class.getSimpleName() + ".class").toURI()).getParentFile();
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @BeforeClass
    public static void classSetup() throws Exception
    {
        DUMMY_SCRIPT_FILE = new File(getTempDir(), DUMMY_SCRIPT_FILE_NAME_PREFIX + ".xml");
        TEST_DATA.put("text", "Glückskekse schmecken toll und sind nicht so süß!");
        TEST_DATA.put("product", "Amazing® new iPod™ from Apple©");
    }

    @Test
    public void testGetDataFromScriptFile_Properties() throws Throwable
    {
        final File dataFile = new File(getTempDir(), DUMMY_SCRIPT_FILE_NAME_PREFIX + "_data.properties");
        try
        {
            FileUtils.write(dataFile, getAsPropertyString(TEST_DATA), CS);

            final Map<String, String> testData = TestDataUtils.getTestData(DUMMY_SCRIPT_FILE);
            validateTestData(testData);
        }
        finally
        {
            FileUtils.deleteQuietly(dataFile);
        }
    }

    @Test
    public void testGetDataFromScriptFile_Csv() throws Throwable
    {
        final File dataFile = new File(getTempDir(), DUMMY_SCRIPT_FILE_NAME_PREFIX + "_data.csv");
        try
        {
            FileUtils.write(dataFile, getAsCsvString(TEST_DATA), CS);

            final Map<String, String> testData = TestDataUtils.getTestData(DUMMY_SCRIPT_FILE);
            validateTestData(testData);
        }
        finally
        {
            FileUtils.deleteQuietly(dataFile);
        }
    }

    @Test
    public void testGetDataFromScriptFile_XML() throws Throwable
    {
        final File dataFile = new File(getTempDir(), DUMMY_SCRIPT_FILE_NAME_PREFIX + "_data.xml");
        try
        {
            FileUtils.write(dataFile, getAsXMLString(TEST_DATA), CS);

            final Map<String, String> testData = TestDataUtils.getTestData(DUMMY_SCRIPT_FILE);
            validateTestData(testData);
        }
        finally
        {
            FileUtils.deleteQuietly(dataFile);
        }
    }

    @Test
    public void testGetDataFromScript_Properties() throws Throwable
    {
        final Script testScript = mock(Script.class);
        Mockito.stub(testScript.getScriptFile()).toReturn(DUMMY_SCRIPT_FILE);

        final File dataFile = new File(getTempDir(), DUMMY_SCRIPT_FILE_NAME_PREFIX + "_data.properties");
        try
        {
            FileUtils.write(dataFile, getAsPropertyString(TEST_DATA), CS);

            final Map<String, String> testData = TestDataUtils.getTestData(testScript);
            validateTestData(testData);
        }
        finally
        {
            FileUtils.deleteQuietly(dataFile);
        }
    }

    @Test
    public void testGetDataFromScript_CSV() throws Throwable
    {
        final Script testScript = mock(Script.class);
        Mockito.stub(testScript.getScriptFile()).toReturn(DUMMY_SCRIPT_FILE);

        final File dataFile = new File(getTempDir(), DUMMY_SCRIPT_FILE_NAME_PREFIX + "_data.csv");
        try
        {
            FileUtils.write(dataFile, getAsCsvString(TEST_DATA), CS);

            final Map<String, String> testData = TestDataUtils.getTestData(testScript);
            validateTestData(testData);
        }
        finally
        {
            FileUtils.deleteQuietly(dataFile);
        }
    }

    @Test
    public void testGetDataFromScript_XML() throws Throwable
    {
        final Script testScript = mock(Script.class);
        Mockito.stub(testScript.getScriptFile()).toReturn(DUMMY_SCRIPT_FILE);

        final File dataFile = new File(getTempDir(), DUMMY_SCRIPT_FILE_NAME_PREFIX + "_data.xml");
        try
        {
            FileUtils.write(dataFile, getAsXMLString(TEST_DATA), CS);

            final Map<String, String> testData = TestDataUtils.getTestData(testScript);
            validateTestData(testData);
        }
        finally
        {
            FileUtils.deleteQuietly(dataFile);
        }
    }

    @Test
    public void testGetDataFromClass_Properties() throws Throwable
    {
        final File dataFile = new File(TEST_CLASS_BASEDIR, TestDataUtils.class.getSimpleName() + "_data.properties");
        try
        {
            FileUtils.write(dataFile, getAsPropertyString(TEST_DATA), CS);

            final Map<String, String> testData = TestDataUtils.getTestData(TestDataUtils.class);
            validateTestData(testData);
        }
        finally
        {
            FileUtils.deleteQuietly(dataFile);
        }
    }

    @Test
    public void testGetDataFromClass_CSV() throws Throwable
    {
        final File dataFile = new File(TEST_CLASS_BASEDIR, TestDataUtils.class.getSimpleName() + "_data.csv");
        try
        {
            FileUtils.write(dataFile, getAsCsvString(TEST_DATA), CS);

            final Map<String, String> testData = TestDataUtils.getTestData(TestDataUtils.class);
            validateTestData(testData);
        }
        finally
        {
            FileUtils.deleteQuietly(dataFile);
        }
    }

    @Test
    public void testGetDataFromClass_XML() throws Throwable
    {
        final File dataFile = new File(TEST_CLASS_BASEDIR, TestDataUtils.class.getSimpleName() + "_data.xml");
        try
        {
            FileUtils.write(dataFile, getAsXMLString(TEST_DATA), CS);

            final Map<String, String> testData = TestDataUtils.getTestData(TestDataUtils.class);
            validateTestData(testData);
        }
        finally
        {
            FileUtils.deleteQuietly(dataFile);
        }
    }

    @Test
    public void testGetGlobalData() throws Throwable
    {
        final FileObject origHome = XltExecutionContext.getCurrent().getTestSuiteHomeDir();
        try
        {
            XltExecutionContext.getCurrent().setTestSuiteHomeDir(getTempDir());
            final File dataFile = new File(getTempDir(), "global_testdata.properties");
            try
            {
                FileUtils.write(dataFile, getAsPropertyString(TEST_DATA), CS);

                final Map<String, String> testData = TestDataUtils.getGlobalTestData();
                validateTestData(testData);
            }
            finally
            {
                FileUtils.deleteQuietly(dataFile);
            }
        }
        finally
        {
            XltExecutionContext.getCurrent().setTestSuiteHomeDir(origHome);
        }
    }

    @Test
    public void testGetPackageData() throws Throwable
    {
        final File dataFileBaseDir = new File(getTempDir(), "a/b/c/d/e");

        final File dataFile = new File(dataFileBaseDir, "package_testdata.properties");
        try
        {
            Assert.assertTrue("Failed to create data file sub-directory", dataFileBaseDir.mkdirs());
            FileUtils.write(dataFile, getAsPropertyString(TEST_DATA), CS);

            final Map<String, String> testData = TestDataUtils.getPackageTestData(null, getTempDir().getAbsolutePath(), "a.b.c.d.e");
            validateTestData(testData);
        }
        finally
        {
            FileUtils.deleteQuietly(dataFileBaseDir);
        }
    }

    private void validateTestData(final Map<String, String> data)
    {
        Assert.assertNotNull("Test data map is <null>", data);
        Assert.assertEquals("Test data maps have different size", TEST_DATA.size(), data.size());

        for (final Map.Entry<String, String> e : TEST_DATA.entrySet())
        {
            final String key = e.getKey();
            Assert.assertTrue(MessageFormat.format("No such key {1} in test data map", key), data.containsKey(key));
            Assert.assertEquals(MessageFormat.format("Values for key {1} do not match", key), e.getValue(), data.get(key));
        }
    }

    private static String getAsPropertyString(Map<String, String> data)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("# This is just a dummy header line\n# Please do not remove\n");
        for (final Map.Entry<String, String> e : data.entrySet())
        {
            sb.append(e.getKey()).append(" = ").append(e.getValue()).append('\n');
        }

        return sb.toString();
    }

    private static String getAsCsvString(Map<String, String> data)
    {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, String> e : data.entrySet())
        {
            sb.append(CsvUtils.encode(new String[]
                {
                    e.getKey(), e.getValue()
                })).append('\n');
        }

        return sb.toString();
    }

    private static String getAsXMLString(Map<String, String> data)
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<data xmlns=\"http://xlt.xceptance.com/xlt-script-data\">\n");
        boolean first = true;
        for (final Map.Entry<String, String> e : data.entrySet())
        {
            if (first)
            {
                first = false;
            }
            else
            {
                sb.append("\n");
            }
            sb.append("    <").append(e.getKey()).append(">").append(e.getValue()).append("</").append(e.getKey()).append(">");
        }
        sb.append("\n</data>");

        return sb.toString();
    }
}
