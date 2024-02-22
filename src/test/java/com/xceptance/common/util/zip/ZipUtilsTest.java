/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.common.util.zip;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.xlt.AbstractXLTTestCase;

/**
 * Test implementation of {@link ZipUtils}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class ZipUtilsTest extends AbstractXLTTestCase
{
    /**
     * Class logger.
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ZipUtilsTest.class);

    /**
     * Name of test directory.
     */
    private final static String testDirName = "test" + new Random().nextInt(1000);

    /**
     * Name of test sub directory.
     */
    private final static String testSubDirName = "testsub" + new Random().nextInt(1000);

    /**
     * Name of destination directory.
     */
    private final static String destDirName = "dest" + new Random().nextInt(1000);

    /**
     * Handle on test directory named 'test' rooted at 'tempDir'.
     */
    private File testDir = null;

    /**
     * A subdirectory in the dir
     */
    private File testSubDir = null;

    /**
     * Handle on test directory named 'dest' rooted at 'tempDir'.
     */
    private File destDir = null;

    /**
     * Handle on generated zip file named 'ziputilstest.zip' rooted at 'tempDir'.
     */
    private final File testZipFile = new File(getTempDir(), "ziputilstest.zip");

    /**
     * Handle on text file named 'text.txt' rooted at 'testDir'.
     */
    private File textFile1 = null;

    /**
     * Handle on text file named 'text.txt' rooted at 'testSubDir'.
     */
    private File textFile2 = null;

    /**
     * Handle on binary file named 'test.bin' rooted at 'testDir'.
     */
    private File binFile = null;

    /**
     * Text file data.
     */
    private final String textFileContent1 = "This is a sample text for testing purposes.";

    /**
     * Text file data.
     */
    private final String textFileContent2 = "This is a 2.";

    /**
     * Binary file data.
     */
    private final byte[] binaryFileContent = getBinaryData();

    /**
     * Test fixture setup.
     * 
     * @throws IOException
     */
    @Before
    public void init() throws IOException
    {
        testDir = new File(getTempDir(), testDirName);
        testSubDir = new File(testDir, testSubDirName);
        destDir = new File(getTempDir(), destDirName);

        textFile1 = new File(testDir, "test.txt");
        textFile2 = new File(testSubDir, "test.txt");
        binFile = new File(testDir, "test.bin");

        // Remove files or directories left over by previous broken runs
        FileUtils.deleteQuietly(testDir);
        FileUtils.deleteQuietly(testZipFile);
        FileUtils.deleteQuietly(destDir);

        // create file and directories
        Assert.assertTrue(testDir.mkdir());
        Assert.assertTrue(testSubDir.mkdir());
        // Assert.assertTrue(destDir.mkdir());
        Assert.assertTrue(testZipFile.createNewFile());

        // create and write text/binary file
        FileUtils.writeStringToFile(textFile1, textFileContent1, StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(textFile2, textFileContent2, StandardCharsets.UTF_8);
        FileUtils.writeByteArrayToFile(binFile, binaryFileContent);
    }

    /**
     * Test fixture cleanup.
     */
    @After
    public void tidyup()
    {
        // Remove files and directories used in test
        FileUtils.deleteQuietly(testZipFile);
        FileUtils.deleteQuietly(testDir);
        FileUtils.deleteQuietly(destDir);
    }

    /**
     * Test zipping and unzipping using no file filter.
     */
    @Test
    public void testZipUnzipDirectory_NullFilter()
    {
        // call zip/unzip testing routine using no file filter
        testZipUnzip(null, true);
    }

    /**
     * Test zipping and unzipping using no file filter.
     */
    @Test
    public void testZipUnzipDirectory_NoFilterAtAll()
    {
        // call zip/unzip testing routine using no file filter
        testZipUnzip(null, false);
    }

    /**
     * Exception handling
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testZipExceptionHandling1() throws IOException
    {
        final File archive = new File("foo");
        try
        {
            ZipUtils.zipDirectory(null, null, archive);
        }
        finally
        {
            FileUtils.deleteQuietly(archive);
        }
    }

    /**
     * Exception handling
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testZipExceptionHandling2() throws IOException
    {
        // get us a temp dir
        final File temp = new File(getTempDir(), UUID.randomUUID().toString());
        temp.deleteOnExit();
        temp.mkdir();
        ZipUtils.zipDirectory(temp, null, null);
    }

    /**
     * Exception handling
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testZipExceptionHandling3_NotADir() throws IOException
    {
        // get us a temp dir
        final File temp = new File(getTempDir(), UUID.randomUUID().toString());
        temp.deleteOnExit();
        temp.createNewFile();

        final File archive = new File("dd");
        try
        {
            ZipUtils.zipDirectory(temp, null, archive);
        }
        finally
        {
            FileUtils.deleteQuietly(archive);
        }
    }

    /**
     * Exception handling
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testZipExceptionHandlingZipOutputStreamIsNull() throws IOException
    {
        ZipUtils.zipDirectory(null, new File("test"), null, new File("test"));
    }

    /**
     * Test zipping and unzipping using a simply text file filter.
     */
    @Test
    public void testZipUnzipDirectory_TextFilesOnly()
    {
        // create text file filter
        final FileFilter filter = new FileFilter()
        {
            @Override
            public boolean accept(final File f)
            {
                return f.getName().endsWith("txt");
            }
        };

        // call zip/unzip testing routine using text file filter
        testZipUnzip(filter, true);
    }

    /**
     * Test zipping and unzipping using the given file filter.
     * 
     * @param filter
     *            file filter to use
     * @param useNullAsFiler
     *            shall we use the null as filter or use the other method
     */
    private void testZipUnzip(final FileFilter filter, final boolean useNullAsFilter)
    {
        /*
         * 1.Step: Zip
         */

        try
        {
            // zip directory 'testDir' to file 'testZipFile'
            if (useNullAsFilter)
            {
                ZipUtils.zipDirectory(testDir, filter, testZipFile);
            }
            else
            {
                ZipUtils.zipDirectory(testDir, testZipFile);
            }

            // validate zipped file
            validateZippedFile(filter);
        }
        // zipping failed for any reason
        catch (final IOException ie)
        {
            // log error message and throw an AssertionError
            final String errMsg = String.format("Failed to zip directory '%s' to zip file '%s'.", testDir.getName(), testZipFile.getName());
            LOGGER.error(errMsg, ie);
            failOnUnexpected(ie);
        }

        /*
         * 2.Step: Unzip
         */

        try
        {
            // unzip 'testZipFile' to directory 'destDir'
            ZipUtils.unzipFile(testZipFile, destDir);

            // validate unzipped content using original directory and file
            // filter
            validateUnzippedDirectory(filter);

        }
        // unzipping failed for any reason
        catch (final IOException ie)
        {
            // log error message and throw an AssertionError
            final String errMsg = String.format("Failed to unzip file '%s' to directory '%s'.", testZipFile.getName(), testDir.getName());
            LOGGER.error(errMsg, ie);
            failOnUnexpected(ie);
        }
    }

    /**
     * Validates the zipped file using the given file filter.
     * 
     * @param filter
     *            file filter to use
     */
    private void validateZippedFile(final FileFilter filter)
    {
        // make sure, we have a real file which can be read
        Assert.assertTrue(testZipFile.exists() && testZipFile.canRead());

        // create default filter if none was given
        final FileFilter theFilter = (filter != null) ? filter : new FileFilter()
        {
            @Override
            public boolean accept(final File f)
            {
                return f.getName().matches("^test.*[0-9]*");
            }
        };

        // create zip-inputstream for zipped file
        try (final ZipInputStream zis = new ZipInputStream(new FileInputStream(testZipFile)))
        {
            // process entries of zipped file
            ZipEntry entry = null;
            while ((entry = zis.getNextEntry()) != null)
            {
                // get entry name and cut of any prefix
                String entryName = entry.getName();
                if (entryName.startsWith("."))
                {
                    entryName = entryName.substring(1);
                }
                if (entryName.startsWith("/"))
                {
                    entryName = entryName.substring(1);
                }
                // filter must accept entry -> was used to generate zip file
                Assert.assertTrue("Unknown entry: " + entryName, theFilter.accept(new File(entryName)));
            }
        }
        // opening or reading from zip file failed
        catch (final IOException ie)
        {
            // log error and throw an AssertionError
            final String errMsg = String.format("Error occurred while trying to open or read from zip file '%s'.", testZipFile.getName());
            LOGGER.error(errMsg, ie);
            failOnUnexpected(ie);
        }
    }

    /**
     * Validates the unzipped directory using the given file filter.
     * <p>
     * The unzipped directory is assumed to be 'destDir'.
     * 
     * @param filter
     *            file filter to use for validation
     */
    private void validateUnzippedDirectory(final FileFilter filter)
    {
        // filter to use
        final FileFilter theFilter = (filter != null) ? filter : new FileFilter()
        {
            @Override
            public boolean accept(final File f)
            {
                return f.isFile() && f.getName().startsWith("test");
            }
        };

        // content of 'testDir'
        final File[] testDirContent = testDir.listFiles(theFilter);
        // content of 'destDir'
        final File[] destDirContent = destDir.listFiles(theFilter);

        // validate length of both file lists
        Assert.assertEquals(testDirContent.length, destDirContent.length);

        // process each file found in 'testDir'
        for (final File f : testDirContent)
        {

            // locate it in file list of 'destDir'
            final File g = findFile(f, destDirContent);
            Assert.assertNotNull(g);
            try
            {
                // compare checksums of both files
                Assert.assertEquals(FileUtils.checksumCRC32(f), FileUtils.checksumCRC32(g));
            }
            // checksum computation failed for some reason
            catch (final IOException ie)
            {
                // log error message and throw an AssertionError
                final String errMsg = String.format("Failed to compute checksum for file '%s' or '%s' respectively", f.getName(),
                                                    g.getName());
                LOGGER.error(errMsg, ie);
                Assert.fail(ie.getMessage());
            }
        }

    }

    /**
     * Locates the given query file in the given array of files. If query file was found in the universe, it will be
     * returned. Otherwise, null is returned.
     * <p>
     * Locating the query file is based on filename comparison.
     * </p>
     * 
     * @param query
     *            file to locate.
     * @param universe
     *            universe of files to search for {@literal query}.
     * @return file in universe whose name matches the one of the given query file
     */
    private File findFile(final File query, final File[] universe)
    {
        // parameter validation
        if (query == null || universe == null || universe.length == 0)
        {
            return null;
        }

        // iterate through the universe
        for (final File f : universe)
        {
            // compare filenames -> on equality, 'query' has been located
            if (query.getName().equals(f.getName()))
            {
                return f;
            }
        }

        // query couldn't be located in universe -> return null
        return null;
    }

    /**
     * Returns this class representation as byte array.
     * 
     * @return this class as byte array
     */
    private byte[] getBinaryData()
    {
        // get this class
        final Class<?> clazz = getClass();
        // assemble resource name and get its URL
        final URL url = clazz.getResource("/" + clazz.getName().replace(".", "/") + ".class");

        byte[] content = null;

        // if resource was found ...
        if (url != null)
        {
            try (final InputStream is = url.openStream())
            {
                // copy content of stream to byte array
                content = IOUtils.toByteArray(is);
            }
            // reading resource has failed
            catch (final IOException ie)
            {
                // log error message
                LOGGER.error("Error while trying to get binary data: " + ie.getMessage());

            }
        }
        // return read content
        return content;
    }
}
