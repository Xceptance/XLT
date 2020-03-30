/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.common.util;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xceptance.common.io.FileUtils;

/**
 * Test the implementation of {@link ParameterCheckUtils}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class ParameterCheckUtilsTest
{
    /**
     * Directory for testing purposes. Created/deleted on class initialization/finalization.
     */
    private static File testDir;

    /**
     * File for testing purposes. Created/deleted on class initialization/finalization.
     */
    private static File testFile;

    /**
     * Class initialization.
     * 
     * @throws Throwable
     *             thrown when creation of test directory/file failed
     */
    @BeforeClass
    public static void classIntro() throws Throwable
    {
        testFile = File.createTempFile("myFile", "txt");
        testDir = new File(testFile.getParent(), "myDir");
        org.apache.commons.io.FileUtils.forceMkdir(testDir);
    }

    /**
     * Class finalization.
     * 
     * @throws Throwable
     *             thrown when deletion of test directory/file failed
     */
    @AfterClass
    public static void classOutro() throws Throwable
    {
        FileUtils.deleteFile(testFile);
        FileUtils.deleteFile(testDir);
    }

    /**
     * Tests the implementation of {@link ParameterCheckUtils#isNotNull(Object, String)}
     */
    @Test
    public void testIsNotNull()
    {
        try
        {
            ParameterCheckUtils.isNotNull(null, "someParam");
            Assert.fail("ParameterCheckUtils#isNotNull(Object,String) should raise an IllegalArgumentException since passed parameter object is null");
        }
        catch (final IllegalArgumentException e)
        {
        }

        ParameterCheckUtils.isNotNull(new Object(), "someParam");
    }

    /**
     * Tests the implementation of {@link ParameterCheckUtils#isNotNullOrEmpty(String, String)} and
     * {@link ParameterCheckUtils#isNotNullOrEmpty(String[], String)}.
     */
    @Test
    public void testIsNotNullOrEmpty()
    {
        try
        {
            ParameterCheckUtils.isNotNullOrEmpty("", "someParam");
            Assert.fail("ParameterCheckUtils#isNotNullOrEmpty(String,String) should raise an IllegalArgumentException since passed parameter object is empty");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            ParameterCheckUtils.isNotNullOrEmpty(new String[0], "someParam");
            Assert.fail("ParameterCheckUtils#isNotNullOrEmpty(String[],String) should raise an IllegalArgumentException since passed parameter object is empty");
        }
        catch (final IllegalArgumentException e)
        {
        }

        ParameterCheckUtils.isNotNullOrEmpty("someValue", "someParam");
        ParameterCheckUtils.isNotNullOrEmpty(new String[]
            {
                "Hi there"
            }, "someParam");

    }

    /**
     * Tests the implementation of {@link ParameterCheckUtils#isReadableFile(File, String)} and
     * {@link ParameterCheckUtils#isReadableFile(FileObject, String)}.
     * 
     * @throws Throwable
     *             thrown on unexpected error
     */
    @Test
    public void testIsReadableFile() throws Throwable
    {
        try
        {
            ParameterCheckUtils.isReadableFile((File) null, "someParam");
            Assert.fail("ParameterCheckUtils#isReadableFile(File,String) should raise an IllegalArgumentException since passed File object is null");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            ParameterCheckUtils.isReadableFile((FileObject) null, "someParam");
            Assert.fail("ParameterCheckUtils#isReadableFile(FileObject,String) should raise an IllegalArgumentException since passed FileObject object is null");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            ParameterCheckUtils.isReadableFile(testDir, "someParam");
            Assert.fail("ParameterCheckUtils#isReadableFile(File,String) should raise an IllegalArgumentException since passed file object is a directory");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            ParameterCheckUtils.isReadableFile(new File(testDir, "doesnotexist.txt"), "someParam");
            Assert.fail("ParameterCheckUtils#isReadableFile(File,String) should raise an IllegalArgumentException since passed file object is a non-existent file");
        }
        catch (final IllegalArgumentException e)
        {
        }

        // testFile.setReadable(false);
        //
        // try
        // {
        // ParameterCheckUtils.isReadableFile(testFile, "someParam");
        // Assert.fail("ParameterCheckUtils#isReadableFile(File,String) should raise an IllegalArgumentException since passed file object is not readable.");
        // }
        // catch (final IllegalArgumentException e)
        // {
        // }
        //
        // testFile.setReadable(true);
        //
        // ParameterCheckUtils.isReadableFile(testFile, "someParam");
        //
        // final FileObject testFileObject = VFS.getManager().resolveFile(testFile.toURI().toString());
        // final FileObject testDirObject = VFS.getManager().resolveFile(testDir.toURI().toString());
        //
        // Assert.assertNotNull(testFileObject);
        // Assert.assertNotNull(testDirObject);
        //
        // try
        // {
        // ParameterCheckUtils.isReadableFile(testDirObject, "someParam");
        // Assert.fail("ParameterCheckUtils#isReadableFile(FileOject,String) should raise an IllegalArgumentException since passed FileObject object is a directory");
        // }
        // catch (final IllegalArgumentException e)
        // {
        // }
        //
        // try
        // {
        // ParameterCheckUtils.isReadableFile(VFS.getManager().resolveFile(new File(testDir, "doesnotexist.txt").toURI()
        // .toString()),
        // "someParam");
        // Assert.fail("ParameterCheckUtils#isReadableFile(FileOject,String) should raise an IllegalArgumentException since passed FileObject object does not exist");
        // }
        // catch (final IllegalArgumentException e)
        // {
        // }
        //
        // testFile.setReadable(false);
        //
        // try
        // {
        // ParameterCheckUtils.isReadableFile(testFileObject, "someParam");
        // Assert.fail("ParameterCheckUtils#isReadableFile(FileOject,String) should raise an IllegalArgumentException since passed FileObject object is not readable");
        // }
        // catch (final IllegalArgumentException e)
        // {
        // }
        //
        // testFile.setReadable(true);
        //
        // ParameterCheckUtils.isReadableFile(testFileObject, "someParam");
    }

    /**
     * Tests the implementation of {@link ParameterCheckUtils#isWritableDirectory(File, String)}.
     * 
     * @throws Throwable
     *             thrown on unexpected error
     */
    @Test
    public void testIsWritableDirectory() throws Throwable
    {
        try
        {
            ParameterCheckUtils.isWritableDirectory(testFile, "someParam");
            Assert.fail("ParameterCheckUtils#isWritableDirectory(File,String) should raise an IllegalArgumentException since passed directory is not is not a directory");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            ParameterCheckUtils.isWritableDirectory(new File(testDir, "doesnotexist"), "someParam");
            Assert.fail("ParameterCheckUtils#isWritableDirectory(File,String) should raise an IllegalArgumentException since passed directory does not exist");
        }
        catch (final IllegalArgumentException e)
        {
        }

        // testDir.setWritable(false);
        //
        // try
        // {
        // ParameterCheckUtils.isWritableDirectory(testDir, "someParam");
        // Assert.fail("ParameterCheckUtils#isWritableDirectory(File,String) should raise an IllegalArgumentException since passed directory is not writable");
        // }
        // catch (final IllegalArgumentException e)
        // {
        // }
        //
        // testDir.setWritable(true);

        ParameterCheckUtils.isWritableDirectory(testDir, "someParam");
    }

    /**
     * Tests the implementation of {@link ParameterCheckUtils#isWritableFile(File, String)}.
     * 
     * @throws Throwable
     *             thrown on unexpected error
     */
    @Test
    public void testIsWritableFile() throws Throwable
    {
        try
        {
            ParameterCheckUtils.isWritableFile(testDir, "someParam");
            Assert.fail("ParameterCheckUtils#isWritableFile(File,String) should raise an IllegalArgumentException since passed file is not a regular file");
        }
        catch (final IllegalArgumentException e)
        {
        }

        // testFile.setWritable(false);
        //
        // try
        // {
        // ParameterCheckUtils.isWritableFile(testDir, "someParam");
        // Assert.fail("ParameterCheckUtils#isWritableFile(File,String) should raise an IllegalArgumentException since passed file is not writable");
        // }
        // catch (final IllegalArgumentException e)
        // {
        // }
        //
        // testFile.setWritable(true);

        ParameterCheckUtils.isWritableFile(testFile, "someParam");
    }

    /**
     * Tests the implementaion of {@link ParameterCheckUtils#isGreaterThan(int, int, String)}.
     */
    @Test
    public void testIsGreaterThan()
    {
        try
        {
            ParameterCheckUtils.isGreaterThan(0, 10, "someParam");
            Assert.fail("ParameterCheckUtils#isGreaterThan(int,int,String) should raise an IllegalArgumentException since passed value is less or equal the passed limit");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            ParameterCheckUtils.isGreaterThan(10, 10, "someParam");
            Assert.fail("ParameterCheckUtils#isGreaterThan(int,int,String) should raise an IllegalArgumentException since passed value is less or equal the passed limit");
        }
        catch (final IllegalArgumentException e)
        {
        }

        ParameterCheckUtils.isGreaterThan(11, 10, "someParam");
    }

    /**
     * Tests the implementation of {@link ParameterCheckUtils#isLessThan(int, int, String)}.
     */
    @Test
    public void testIsLessThan()
    {
        try
        {
            ParameterCheckUtils.isLessThan(5, 0, "someParam");
            Assert.fail("ParameterCheckUtils#isLessThan(int,int,String) should raise an IllegalArgumentException since passed value is greater than or equal the passed limit");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            ParameterCheckUtils.isLessThan(0, 0, "someParam");
            Assert.fail("ParameterCheckUtils#isLessThan(int,int,String) should raise an IllegalArgumentException since passed value is greater than or equal the passed limit");
        }
        catch (final IllegalArgumentException e)
        {
        }

        ParameterCheckUtils.isLessThan(-1, 0, "someParam");
    }

    /**
     * Tests the implementation of {@link ParameterCheckUtils#isNotNegative(int, String)}.
     */
    @Test
    public void testIsNotNegative()
    {
        try
        {
            ParameterCheckUtils.isNotNegative(-1, "someParam");
            Assert.fail("ParameterCheckUtils#isNotNegative(int,String) should raise an IllegalArgumentException since passed value is negative");
        }
        catch (final IllegalArgumentException e)
        {
        }

        ParameterCheckUtils.isNotNegative(0, "someParam");
        ParameterCheckUtils.isNotNegative(1, "someParam");
    }

    /**
     * Tests the implementation of {@link ParameterCheckUtils#isRelativePath(String, String)}.
     */
    @Test
    public void testIsRelativePath()
    {

        try
        {
            ParameterCheckUtils.isRelativePath("", "someParam");
            Assert.fail("ParameterCheckUtils#isRelativePath(String,String) should raise an IllegalArgumentException since passed path is empty");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            ParameterCheckUtils.isRelativePath("/test", "someParam");
            Assert.fail("ParameterCheckUtils#isRelativePath(String,String) should raise an IllegalArgumentException since passed path is not relative");
        }
        catch (final IllegalArgumentException e)
        {
        }

        ParameterCheckUtils.isRelativePath("t/est", "someParam");
    }

    /**
     * Tests the implementation of {@link ParameterCheckUtils#isNonEmptyString(String, String)}.
     */
    @Test
    public void testIsNonEmptyString()
    {
        try
        {
            ParameterCheckUtils.isNonEmptyString("", "someParam");
            Assert.fail("ParameterCheckUtils#isNonEmptyString(String,String) should raise an IllegalArgumentException since passed string is empty");
        }
        catch (final IllegalArgumentException e)
        {
        }

        try
        {
            ParameterCheckUtils.isNonEmptyString("      ", "someParam");
            Assert.fail("ParameterCheckUtils#isNonEmptyString(String,String) should raise an IllegalArgumentException since passed string is empty");
        }
        catch (final IllegalArgumentException e)
        {
        }

        ParameterCheckUtils.isNonEmptyString("  test ", "someParam");
    }

    /**
     * Tests the implementation of {@link ParameterCheckUtils#isValidArray(Object, String)}.
     */
    @Test
    public void testIsValidArray()
    {
        try
        {
            ParameterCheckUtils.isValidArray(new Object(), "someParam");
            Assert.fail("ParameterCheckUtils#isValidArray(Object[],String) should raise an IllegalArgumentException since passed array is not assignable to Object[].class");
        }
        catch (final IllegalArgumentException e)
        {
        }

        ParameterCheckUtils.isValidArray(new Object[0], "someParam");
    }

    /**
     * Tests the implementation of {@link ParameterCheckUtils#isValidArrayOfMinSize(Object, int, String)}.
     */
    @Test
    public void testIsValidArrayOfMinSize()
    {
        try
        {
            ParameterCheckUtils.isValidArrayOfMinSize(new Object[0], 2, "someParam");
            Assert.fail("ParameterCheckUtils#isValidArrayOfMinSize(Object[],int,String) should raise an IllegalArgumentException since size of passed array is less than the passed minimum size");
        }
        catch (final IllegalArgumentException e)
        {
        }

        ParameterCheckUtils.isValidArrayOfMinSize(new Object[2], 2, "someParam");
    }
}
