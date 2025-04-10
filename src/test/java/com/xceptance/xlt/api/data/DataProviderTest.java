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
package com.xceptance.xlt.api.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;

import com.xceptance.xlt.common.XltConstants;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.util.XltRandom;

/**
 * Tests the implementation of {@link DataProvider}.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class DataProviderTest
{
    /**
     * Just some UTF-8 data
     */
    private static final String UTF8STRING = "A" + "\u00ea" + "\u00f1" + "\u00fc" + "C";

    /**
     * File handle to directory holding the test data.
     */
    private static File dataDir;

    /**
     * data provider for default locale
     */
    private DataProvider defaultProvider;

    /**
     * data provider for 'en' locale
     */
    private DataProvider enProvider;

    /**
     * Overall test initialization:
     * <ul>
     * <li>Creation of test data directory</li>
     * <li>Creation of language specific sub-directories</li>
     * <li>Writing test data to appropriate files</li>
     * </ul>
     * 
     * @throws IOException
     */
    @BeforeClass
    public static void classIntro() throws IOException
    {
        final String tempDir = System.getProperty("java.io.tmpdir");
        dataDir = new File(new File(tempDir), "data");

        final File defaultDir = new File(dataDir, "default");
        final File enDir = new File(dataDir, "en");

        final File defaultData = new File(defaultDir, "data.txt");
        final File enData = new File(enDir, "data.txt");
        final File utf8Data = new File(defaultDir, "utf8data.txt");

        final List<String> defaultLines = new ArrayList<String>();
        defaultLines.add("default1# first entry");
        defaultLines.add("default2# second entry and so on");
        defaultLines.add("default3");
        defaultLines.add("default4");
        defaultLines.add("default5");
        defaultLines.add("default6");
        defaultLines.add("\"enclosed comment # Hello World!\"");
        defaultLines.add("#real comment -> should be skipped");
        FileUtils.writeLines(defaultData, defaultLines);

        final List<String> enLines = new ArrayList<String>();
        enLines.add("en1");
        enLines.add("en2");
        enLines.add("en3");
        enLines.add("en4");
        enLines.add("en5");
        enLines.add("en6");
        enLines.add(" en7 ");
        FileUtils.writeLines(enData, enLines);

        final List<String> utf8Lines = new ArrayList<String>();
        utf8Lines.add(UTF8STRING);
        FileUtils.writeLines(utf8Data, "UTF-8", utf8Lines);

        XltProperties.getInstance().setProperty(XltConstants.PROP_DATA_DIRECTORY, dataDir.getAbsolutePath());
    }

    /**
     * Test method initialization.
     */
    @Before
    public void testIntro() throws IOException
    {
        defaultProvider = new DataProvider("default/data.txt");
        enProvider = new DataProvider("en/data.txt");
    }

    /**
     * Overall test finalization:
     * <ul>
     * <li>Deletion of test data directory</li>
     * </ul>
     * 
     * @throws IOException
     */
    @AfterClass
    public static void classOutro() throws IOException
    {
        FileUtils.deleteDirectory(dataDir);
    }

    /**
     * Read provider again and again, should return the same object
     */
    @Test
    public void getInstance() throws FileNotFoundException, IOException
    {
        final DataProvider defaultProvider1 = DataProvider.getInstance("default/data.txt");
        final DataProvider enProvider1 = DataProvider.getInstance("en/data.txt");

        final DataProvider defaultProvider2 = DataProvider.getInstance("default/data.txt");
        final DataProvider enProvider2 = DataProvider.getInstance("en/data.txt");

        Assert.assertSame(defaultProvider1, defaultProvider2);
        Assert.assertSame(enProvider1, enProvider2);
    }

    /**
     * Get UTF8 data
     */
    @Test
    public void getUTF8File() throws FileNotFoundException, IOException
    {
        final DataProvider utf8Provider = new DataProvider("default/utf8data.txt", "UTF-8");
        final List<String> rows = utf8Provider.getAllRows();

        Assert.assertEquals(1, rows.size());
        Assert.assertEquals(UTF8STRING, rows.get(0));
    }

    /**
     * Get UTF8 data with incorrect encoding.
     */
    @Test(expected = MalformedInputException.class)
    public void getUTF8File_IncorrectEncoding() throws FileNotFoundException, IOException
    {
        new DataProvider("default/utf8data.txt", "ASCII");
    }

    /**
     * Asks for a non-existent file
     * 
     * @throws FileNotFoundException
     */
    @Test(expected = NoSuchFileException.class)
    public void readDataInvalid() throws FileNotFoundException, IOException
    {
        new DataProvider("doesnotexist.txt");
    }

    @Test
    public void readDataValid()
    {
        // use default locale
        List<String> result = defaultProvider.getAllRows();

        Assert.assertEquals(7, result.size());

        Assert.assertTrue(result.contains("default1# first entry"));
        Assert.assertTrue(result.contains("default2# second entry and so on"));
        Assert.assertTrue(result.contains("default3"));
        Assert.assertTrue(result.contains("default4"));
        Assert.assertTrue(result.contains("default5"));
        Assert.assertTrue(result.contains("default6"));
        Assert.assertTrue(result.contains("\"enclosed comment # Hello World!\""));

        // use 'en' locale
        result = enProvider.getAllRows();

        Assert.assertEquals(7, result.size());

        Assert.assertTrue(result.contains("en1"));
        Assert.assertTrue(result.contains("en2"));
        Assert.assertTrue(result.contains("en3"));
        Assert.assertTrue(result.contains("en4"));
        Assert.assertTrue(result.contains("en5"));
        Assert.assertTrue(result.contains("en6"));
        Assert.assertTrue(result.contains(" en7 "));
    }

    @Test
    public void testGetAllRows()
    {
        final List<String> defaultRows = defaultProvider.getAllRows();
        Assert.assertEquals(7, defaultRows.size());
        Assert.assertTrue(defaultRows.contains("default1# first entry"));
        Assert.assertTrue(defaultRows.contains("default2# second entry and so on"));
        Assert.assertTrue(defaultRows.contains("default3"));
        Assert.assertTrue(defaultRows.contains("default4"));
        Assert.assertTrue(defaultRows.contains("default5"));
        Assert.assertTrue(defaultRows.contains("default6"));
        Assert.assertTrue(defaultRows.contains("\"enclosed comment # Hello World!\""));

        try
        {
            defaultRows.remove(XltRandom.nextInt(defaultRows.size()));
            Assert.fail("Removing an element from an unmodifiable list should result in an UnsupportedOperationException!!.");
        }
        catch (final UnsupportedOperationException e)
        {
        }

        final List<String> enRows = enProvider.getAllRows();
        Assert.assertEquals(7, enRows.size());
        Assert.assertTrue(enRows.contains("en1"));
        Assert.assertTrue(enRows.contains("en2"));
        Assert.assertTrue(enRows.contains("en3"));
        Assert.assertTrue(enRows.contains("en4"));
        Assert.assertTrue(enRows.contains("en5"));
        Assert.assertTrue(enRows.contains("en6"));
        Assert.assertTrue(enRows.contains(" en7 "));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifyableList()
    {
        final List<String> defaultRows = defaultProvider.getAllRows();
        defaultRows.remove(XltRandom.nextInt(defaultRows.size()));
    }

    @Test
    public void getRandomRow()
    {
        final String row = enProvider.getRandomRow();

        Assert.assertTrue(row.equals("en1") || row.equals("en2") || row.equals("en3") || row.equals("en4") || row.equals("en5") ||
                          row.equals("en6") || row.equals(" en7 "));
    }

    @Test
    public void getRandomRow_CheckRandomness()
    {
        final String row1 = enProvider.getRandomRow();

        for (int i = 0; i < 10000; i++)
        {
            final String row2 = enProvider.getRandomRow();
            if (!row2.equals(row1))
            {
                return;
            }
        }

        Assert.fail("Not random in time.");
    }

    @Test
    public void getRow()
    {
        final String row = enProvider.getRow(0);
        Assert.assertEquals("en1", row);
    }

    @Test
    public void getRowInvalidNumber()
    {
        final String row = enProvider.getRow(100);
        Assert.assertNull(row);
    }

    @Test
    public void getRow_WhiteSpaceTrue()
    {
        final String row = enProvider.getRow(true, 6);
        Assert.assertEquals("en7", row);
    }

    @Test
    public void getRow_WhiteSpaceFalse()
    {
        final String row = enProvider.getRow(false, 6);
        Assert.assertEquals(" en7 ", row);
    }

    @Test
    public void noCommentMarker() throws FileNotFoundException, IOException
    {
        final DataProvider defaultProvider = new DataProvider("default/data.txt", "UTF-8", null);

        // no comment marker, means line 1 is unmodified
        final String line1 = defaultProvider.getRow(7);
        Assert.assertEquals("#real comment -> should be skipped", line1);
    }

    @Test
    public void emptyCommentMarker() throws FileNotFoundException, IOException
    {
        final DataProvider defaultProvider = new DataProvider("default/data.txt", "UTF-8", "");

        // no comment marker, means line 1 is unmodified
        final String line1 = defaultProvider.getRow(7);
        Assert.assertEquals("#real comment -> should be skipped", line1);
    }

    @Test
    public void commentMarkerHash() throws FileNotFoundException, IOException
    {
        final DataProvider defaultProvider = new DataProvider("default/data.txt", "UTF-8", "#");

        // no comment marker, means line 1 is unmodified
        final String line1 = defaultProvider.getRow(7);
        Assert.assertNull(line1);
    }

    @Test
    public void testGetRow_BooleanInt()
    {
        final int defaultSize = defaultProvider.getSize();
        final int enSize = enProvider.getSize();

        Assert.assertNull(defaultProvider.getRow(false, -1));
        Assert.assertNull(defaultProvider.getRow(false, defaultSize));
        Assert.assertNotNull(defaultProvider.getRow(false, defaultSize - 1));

        Assert.assertNull(enProvider.getRow(false, -1));
        Assert.assertNull(enProvider.getRow(false, enSize));
        Assert.assertNotNull(enProvider.getRow(false, enSize - 1));
    }

    @Test
    public void addByIndex_validIndex()
    {
        final int enSize = enProvider.getSize();

        enProvider.addRow(0, "foo");

        Assert.assertEquals(enSize + 1, enProvider.getSize());
        Assert.assertEquals("foo", enProvider.getRow(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void addByIndex_invalidIndex()
    {
        enProvider.addRow(100, "foo");
    }

    @Test
    public void addByValue()
    {
        final int enSize = enProvider.getSize();

        enProvider.addRow("foo");

        Assert.assertEquals(enSize + 1, enProvider.getSize());
        Assert.assertEquals("foo", enProvider.getRow(enSize));
    }

    @Test
    public void removeByIndex_validIndex()
    {
        final int enSize = enProvider.getSize();

        String item = enProvider.removeRow(0);

        Assert.assertEquals("en1", item);
        Assert.assertEquals(enSize - 1, enProvider.getSize());
        Assert.assertEquals(false, enProvider.getAllRows().contains("en1"));
    }

    @Test
    public void removeByIndex_invalidIndex()
    {
        final int enSize = enProvider.getSize();

        String item = enProvider.removeRow(100);

        Assert.assertEquals(null, item);
        Assert.assertEquals(enSize, enProvider.getSize());
    }

    @Test
    public void removeByValue_valueContained()
    {
        final int enSize = enProvider.getSize();

        boolean wasContained = enProvider.removeRow("en1");

        Assert.assertEquals(true, wasContained);
        Assert.assertEquals(enSize - 1, enProvider.getSize());
        Assert.assertEquals(false, enProvider.getAllRows().contains("en1"));
    }

    @Test
    public void removeByValue_valueNotContained()
    {
        final int enSize = enProvider.getSize();

        boolean wasContained = enProvider.removeRow("foo");

        Assert.assertEquals(false, wasContained);
        Assert.assertEquals(enSize, enProvider.getSize());
    }
}
