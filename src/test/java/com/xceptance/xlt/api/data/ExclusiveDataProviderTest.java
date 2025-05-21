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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.xceptance.xlt.common.XltConstants;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.util.LineProvider;

/**
 * Tests the implementation of {@link ExclusiveDataProvider}.
 *
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class ExclusiveDataProviderTest
{
    /**
     * Just some UTF-8 data
     */
    private static final String UTF8STRING1 = "A" + "\u00ea" + "\u00f1" + "\u00fc" + "C";

    private static final String UTF8STRING2 = "B" + "\u00ea" + "\u00f1" + "\u00fc" + "C";

    private static final String UTF8STRING3 = "C" + "\u00ea" + "\u00f1" + "\u00fc" + "C";

    private static final String UTF8STRING4 = "D" + "\u00ea" + "\u00f1" + "\u00fc" + "C";

    /**
     * File handle to directory holding the test data.
     */
    private static File dataDir;

    private static String defaultFile;

    private static String enFile;

    private static String utf8File;

    private static ExclusiveDataProvider.Parser<String> noEParser = new ExclusiveDataProvider.Parser<String>()
    {
        @Override
        public List<String> parse(final List<String> data)
        {
            final List<String> results = new ArrayList<String>();
            for (final String line : data)
            {
                results.add(line.replaceAll("e", ""));
            }
            return results;
        }
    };

    /**
     * Overall test initialization:
     * <ul>
     * <li>Creation of test data directory</li>
     * <li>Creation of language specific sub-directories</li>
     * <li>Writing test data to appropriate files</li>
     * </ul>
     */
    @BeforeClass
    public static void beforeClass() throws IOException
    {
        final String tempDir = System.getProperty("java.io.tmpdir");
        dataDir = new File(new File(tempDir), "data");

        final File defaultDir = new File(dataDir, "default");
        final File enDir = new File(dataDir, "en");

        final File defaultData = new File(defaultDir, "exclusivedata.txt");
        final File enData = new File(enDir, "exclusivedata.txt");
        final File utf8Data = new File(defaultDir, "utf8exclusivedata.txt");

        defaultFile = getDataFileRelative(defaultData);
        enFile = getDataFileRelative(enData);
        utf8File = getDataFileRelative(utf8Data);

        final List<String> defaultLines = new ArrayList<String>();
        defaultLines.add("default1# first entry");
        defaultLines.add("default2# second entry and so on");
        defaultLines.add("default3");
        defaultLines.add("default4");
        defaultLines.add("default5");
        defaultLines.add("default6");
        defaultLines.add("\"enclosed comment # Hello World!\"");
        defaultLines.add("#real comment 1 -> should not be skipped");
        defaultLines.add("#real comment 2 -> should not be skipped");
        FileUtils.writeLines(defaultData, defaultLines);

        final List<String> enLines = new ArrayList<String>();
        enLines.add("en1");
        enLines.add("en2");
        enLines.add("en3");
        enLines.add("en4");
        enLines.add("en5");
        enLines.add("en6");
        enLines.add(" en7 ");
        enLines.add(" en8 ");
        FileUtils.writeLines(enData, enLines);

        final List<String> utf8Lines = new ArrayList<String>();
        utf8Lines.add(UTF8STRING1);
        utf8Lines.add(UTF8STRING2);
        utf8Lines.add(UTF8STRING3);
        utf8Lines.add(UTF8STRING4);
        FileUtils.writeLines(utf8Data, "UTF-8", utf8Lines);

        XltProperties.getInstance().setProperty(XltConstants.PROP_DATA_DIRECTORY, dataDir.getAbsolutePath());

        SessionImpl.getCurrent().setAgentNumber(1);
        SessionImpl.getCurrent().setTotalAgentCount(3);
    }

    private static String getDataFileRelative(final File dataFile)
    {
        return dataFile.getParentFile().getName() + "/" + dataFile.getName();
    }

    /**
     * Sets agent index and count for those tests that do not define them themselves.
     *
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Before
    public void before() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
        SessionImpl.getCurrent().setAgentNumber(1);
        SessionImpl.getCurrent().setTotalAgentCount(3);
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
    public static void afterClass() throws IOException
    {
        FileUtils.deleteDirectory(dataDir);
    }

    @After
    public void after() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
        // clear exclusive data providers
        {
            final Field instance = ExclusiveDataProvider.class.getDeclaredField("EXCLUSIVE_DATA_PROVIDERS");
            instance.setAccessible(true);
            final Map<?, ?> m = (Map<?, ?>) instance.get("");
            m.clear();
        }

        // clear line providers
        {
            final Field instance = LineProvider.class.getDeclaredField("LINE_PROVIDERS");
            instance.setAccessible(true);
            final Map<?, ?> m = (Map<?, ?>) instance.get("");
            m.clear();
        }
    }

    /**
     * Read provider again and again, should return the same object
     */
    @Test
    public void getInstance() throws FileNotFoundException, IOException
    {
        final ExclusiveDataProvider<String> defaultProvider1 = ExclusiveDataProvider.getInstance(defaultFile);
        final ExclusiveDataProvider<String> enProvider1 = ExclusiveDataProvider.getInstance(enFile);

        final ExclusiveDataProvider<String> defaultProvider2 = ExclusiveDataProvider.getInstance(defaultFile);
        final ExclusiveDataProvider<String> enProvider2 = ExclusiveDataProvider.getInstance(enFile);

        Assert.assertSame(defaultProvider1, defaultProvider2);
        Assert.assertSame(enProvider1, enProvider2);
    }

    /**
     * Read provider again and again, should return the same object
     */
    @Test
    public void getInstanceWithParser() throws FileNotFoundException, IOException
    {
        final ExclusiveDataProvider<String> defaultProvider1 = ExclusiveDataProvider.getInstance(defaultFile, false, noEParser);
        final ExclusiveDataProvider<String> enProvider1 = ExclusiveDataProvider.getInstance(enFile, false, noEParser);

        final ExclusiveDataProvider<String> defaultProvider2 = ExclusiveDataProvider.getInstance(defaultFile, false, noEParser);
        final ExclusiveDataProvider<String> enProvider2 = ExclusiveDataProvider.getInstance(enFile, false, noEParser);

        Assert.assertSame(defaultProvider1, defaultProvider2);
        Assert.assertSame(enProvider1, enProvider2);
    }

    /**
     * Get UTF8 data
     */
    @Test
    public void getUTF8File() throws FileNotFoundException, IOException
    {
        final ExclusiveDataProvider<String> utf8Provider = ExclusiveDataProvider.getNewInstance(utf8File, "UTF-8");

        Assert.assertEquals(1, utf8Provider.size());
        final String s = utf8Provider.get();
        Assert.assertEquals(UTF8STRING2, s);
    }

    /**
     * Get UTF8 data with incorrect encoding, validate that platform default encoding is bypassed. Works in conjunction
     * with previous test.
     */
    @Test
    public void getUTF8File_IncorrectEncoding() throws FileNotFoundException, IOException
    {
        final ExclusiveDataProvider<String> utf8Provider = ExclusiveDataProvider.getNewInstance(utf8File, "ASCII");

        Assert.assertEquals(1, utf8Provider.size());
        final String s = utf8Provider.get();
        Assert.assertFalse(UTF8STRING2.equals(s));
    }

    /**
     * Asks for a non-existent file
     *
     * @throws FileNotFoundException
     */
    @Test(expected = FileNotFoundException.class)
    public void readDataInvalid() throws FileNotFoundException, IOException
    {
        ExclusiveDataProvider.getNewInstance("doesnotexist.txt");
    }

    @Test
    public void readDataValid() throws FileNotFoundException, IOException
    {
        // use default locale
        List<String> result = getAllExclusiveData(ExclusiveDataProvider.getInstance(defaultFile));

        Assert.assertEquals(3, result.size());

        Assert.assertTrue(result.contains("default2# second entry and so on"));
        Assert.assertTrue(result.contains("default5"));
        Assert.assertTrue(result.contains("#real comment 1 -> should not be skipped"));

        // use 'en' locale
        result = getAllExclusiveData(ExclusiveDataProvider.getInstance(enFile));

        Assert.assertEquals(3, result.size());

        Assert.assertTrue(result.contains("en2"));
        Assert.assertTrue(result.contains("en5"));
        Assert.assertTrue(result.contains(" en8 "));
    }

    @Test
    public void checkDataPartitions_lineCommentsFilteredOut() throws FileNotFoundException, IOException
    {
        // 1st agent
        SessionImpl.getCurrent().setAgentNumber(0);
        List<String> result = getAllExclusiveData(new ExclusiveDataProvider<String>(defaultFile, true,
                                                                                    ExclusiveDataProvider.getDefaultParser()));

        Assert.assertEquals(3, result.size());
        Assert.assertTrue(result.contains("default1# first entry"));
        Assert.assertTrue(result.contains("default4"));
        Assert.assertTrue(result.contains("\"enclosed comment # Hello World!\""));

        // 2nd agent
        SessionImpl.getCurrent().setAgentNumber(1);
        result = getAllExclusiveData(new ExclusiveDataProvider<String>(defaultFile, true, ExclusiveDataProvider.getDefaultParser()));

        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.contains("default2# second entry and so on"));
        Assert.assertTrue(result.contains("default5"));

        // 3rd agent
        SessionImpl.getCurrent().setAgentNumber(2);
        result = getAllExclusiveData(new ExclusiveDataProvider<String>(defaultFile, true, ExclusiveDataProvider.getDefaultParser()));

        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.contains("default3"));
        Assert.assertTrue(result.contains("default6"));
    }

    @Test
    public void checkDataPartitions_lineCommentsNotFilteredOut() throws FileNotFoundException, IOException
    {
        // 1st agent
        SessionImpl.getCurrent().setAgentNumber(0);
        List<String> result = getAllExclusiveData(new ExclusiveDataProvider<String>(defaultFile, false,
                                                                                    ExclusiveDataProvider.getDefaultParser()));

        Assert.assertEquals(3, result.size());
        Assert.assertTrue(result.contains("default1# first entry"));
        Assert.assertTrue(result.contains("default4"));
        Assert.assertTrue(result.contains("\"enclosed comment # Hello World!\""));

        // 2nd agent
        SessionImpl.getCurrent().setAgentNumber(1);
        result = getAllExclusiveData(new ExclusiveDataProvider<String>(defaultFile, false, ExclusiveDataProvider.getDefaultParser()));

        Assert.assertEquals(3, result.size());
        Assert.assertTrue(result.contains("default2# second entry and so on"));
        Assert.assertTrue(result.contains("default5"));
        Assert.assertTrue(result.contains("#real comment 1 -> should not be skipped"));

        // 3rd agent
        SessionImpl.getCurrent().setAgentNumber(2);
        result = getAllExclusiveData(new ExclusiveDataProvider<String>(defaultFile, false, ExclusiveDataProvider.getDefaultParser()));

        Assert.assertEquals(3, result.size());
        Assert.assertTrue(result.contains("default3"));
        Assert.assertTrue(result.contains("default6"));
        Assert.assertTrue(result.contains("#real comment 2 -> should not be skipped"));
    }

    @Test
    public void getRandomRow() throws FileNotFoundException, IOException
    {
        final String row = ExclusiveDataProvider.getInstance(enFile).getRandom();

        Assert.assertTrue(row.equals("en2") || row.equals("en5") || row.equals(" en8 "));
    }

    @Test
    public void getRandomRow_CheckRandomness() throws FileNotFoundException, IOException
    {
        final String row1 = ExclusiveDataProvider.getInstance(enFile).getRandom();

        for (int i = 0; i < 10000; i++)
        {
            final String row2 = ExclusiveDataProvider.getInstance(enFile).getRandom();
            if (!row2.equals(row1))
            {
                return;
            }
        }

        Assert.fail("Not random in time.");
    }

    @Test
    public void getRow() throws FileNotFoundException, IOException
    {
        final String row = ExclusiveDataProvider.getInstance(enFile).get();
        // must be first element
        Assert.assertEquals("en2", row);
    }

    @Test
    public void getRowInvalidNumber() throws FileNotFoundException, IOException
    {
        String row = "notNull";
        final ExclusiveDataProvider<String> edp = ExclusiveDataProvider.getInstance(enFile);
        for (int i = 0; i < 50; i++)
        {
            row = edp.get();
        }
        Assert.assertNull(row);
    }

    @Test
    public void isExclusive() throws FileNotFoundException, IOException
    {
        // how many partitions do we have
        final int nrOfPartitions = SessionImpl.getCurrent().getTotalAgentCount();

        // 1st partition/agent has size expectedPartitionSize[0], 2nd has size expectedPartitionSize[1], ...
        final int[] expectedPartitionSize =
            {
                3, 3, 3
            };
        Assert.assertEquals("Number of partitions dos not match number of expected partition sizes.", nrOfPartitions,
                            expectedPartitionSize.length);

        // the partition store
        final List<List<String>> partitions = new ArrayList<List<String>>(nrOfPartitions);

        // read partitions into the store
        for (int i = 0; i < nrOfPartitions; i++)
        {
            SessionImpl.getCurrent().setAgentNumber(i);
            final ExclusiveDataProvider<String> p = ExclusiveDataProvider.getNewInstance(defaultFile);
            partitions.add(getAllExclusiveData(p));
        }

        // check partition size and mutual exclusiveness of partition elements
        for (int i = 0; i < nrOfPartitions; i++)
        {
            final List<String> p = partitions.get(i);

            // check partition size
            Assert.assertEquals(expectedPartitionSize[i], p.size());

            // element of partition 'j' MUST NOT contain any element of partition 'i'
            for (int j = 0; j < nrOfPartitions; j++)
            {
                if (i == j)
                {
                    // don't check partition against itself
                    continue;
                }

                checkExclusiveness(p, partitions.get(j));
            }
        }
    }

    /**
     * More parties than data.
     */
    @Test(expected = AssertionError.class)
    public void notEnoughData_lineCommentsFilteredOut() throws FileNotFoundException, IOException
    {
        SessionImpl.getCurrent().setTotalAgentCount(8);
        new ExclusiveDataProvider<String>(defaultFile, true, ExclusiveDataProvider.getDefaultParser());
    }

    /**
     * More parties than data.
     */
    @Test(expected = AssertionError.class)
    public void notEnoughData_lineCommentsNotFilteredOut() throws FileNotFoundException, IOException
    {
        SessionImpl.getCurrent().setTotalAgentCount(10);
        new ExclusiveDataProvider<String>(defaultFile, false, ExclusiveDataProvider.getDefaultParser());
    }

    /**
     * Check there is at least one data item per agent.
     */
    public void enoughData() throws FileNotFoundException, IOException
    {
        // comment lines filtered out
        SessionImpl.getCurrent().setTotalAgentCount(7);
        new ExclusiveDataProvider<String>(defaultFile, true, ExclusiveDataProvider.getDefaultParser());

        // comment lines not filtered out
        SessionImpl.getCurrent().setTotalAgentCount(9);
        new ExclusiveDataProvider<String>(defaultFile, false, ExclusiveDataProvider.getDefaultParser());
    }

    /**
     * Current agent number is higher than total agent count.
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test(expected = AssertionError.class)
    public void outOfRange1() throws FileNotFoundException, IOException
    {
        SessionImpl.getCurrent().setTotalAgentCount(3);
        SessionImpl.getCurrent().setAgentNumber(15);
        ExclusiveDataProvider.getNewInstance(defaultFile);
    }

    /**
     * Current agent number is negative.
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    @Test(expected = AssertionError.class)
    public void outOfRange2() throws FileNotFoundException, IOException
    {
        SessionImpl.getCurrent().setAgentNumber(-1);
        ExclusiveDataProvider.getNewInstance(defaultFile);
    }

    /**
     * Check that the parser does what it's expecting to do
     */
    @Test
    public void parserIsWorking() throws FileNotFoundException, IOException
    {
        for (final String s : getAllExclusiveData(ExclusiveDataProvider.getInstance(defaultFile, false, noEParser)))
        {
            Assert.assertFalse(s.contains("e"));
        }
    }

    /**
     * Check that the parser exception is thrown
     */
    @Test(expected = NullPointerException.class)
    public void parserException() throws FileNotFoundException, IOException
    {
        ExclusiveDataProvider.getInstance(defaultFile, false, new ExclusiveDataProvider.Parser<String>()
        {
            @Override
            public List<String> parse(final List<String> data)
            {
                throw new NullPointerException();
            }
        });
    }

    /**
     * Check decrement/increment of pool size
     */
    @Test
    public void size() throws FileNotFoundException, IOException
    {
        final ExclusiveDataProvider<String> provider = ExclusiveDataProvider.getInstance(defaultFile);
        final int origSize = provider.size();

        final String element = provider.get();
        Assert.assertEquals(origSize - 1, provider.size());

        provider.add(element);
        Assert.assertEquals(origSize, provider.size());
    }

    /**
     * Checks for exclusiveness of elements by comparing two lists.
     *
     * @param x
     * @param y
     * @return <code>true</code> if no element of <code>x</code> is equal to an element in <code>y</code>
     */
    private <T> void checkExclusiveness(final List<T> x, final List<T> y)
    {
        for (final T s : x)
        {
            if (y.contains(s))
            {
                Assert.fail("Non exclusive data found [" + s + "]");
            }
        }
    }

    /**
     * Get all the exclusive data and return them as list
     *
     * @param edp
     * @return
     */
    private <T> List<T> getAllExclusiveData(final ExclusiveDataProvider<T> edp)
    {
        final List<T> results = new ArrayList<T>();
        T t;
        while ((t = edp.get()) != null)
        {
            results.add(t);
        }
        return results;
    }
}
