/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.util;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.CRC32;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.xceptance.xlt.AbstractXLTTestCase;

/**
 * Tests the implementation of {@link FileReplicationUtils}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class FileReplicationUtilsTest
{
    /**
     * Tests the implementation of {@link FileReplicationUtils#getIndex(File)} by passing an empty directory.
     */
    @Test
    public void testGetIndex_EmptyDirectory() throws Throwable
    {
        final File testDir = Mockito.mock(File.class);
        Mockito.doReturn(new File[0]).when(testDir).listFiles((FileFilter) ArgumentMatchers.any());

        final FileReplicationIndex idx = FileReplicationUtils.getIndex(testDir);
        Assert.assertNotNull(idx);
        Assert.assertTrue(idx.isEmpty());
    }

    /**
     * Tests the implementation of {@link FileReplicationUtils#getIndex(File)} by passing a single directory containing
     * an empty sub-directory.
     */
    @Test
    public void testGetIndex_SingleEmptySubdirectory() throws Throwable
    {
        final File testDir = Mockito.mock(File.class);
        final File testSubdir = Mockito.mock(File.class);
        Mockito.doReturn(new File[]
            {
                testSubdir
            }).when(testDir).listFiles((FileFilter) ArgumentMatchers.any());
        Mockito.doReturn("testSubDir").when(testSubdir).getName();
        Mockito.doReturn(true).when(testSubdir).isDirectory();
        Mockito.doReturn(new File[0]).when(testSubdir).listFiles((FileFilter) ArgumentMatchers.any());

        final FileReplicationIndex idx = FileReplicationUtils.getIndex(testDir);
        Assert.assertNotNull(idx);
        Assert.assertFalse(idx.isEmpty());

        final Iterator<Entry<File, Long>> it = idx.entrySet().iterator();
        Assert.assertTrue(it.hasNext());
        final Entry<File, Long> entry = it.next();
        Assert.assertEquals(testSubdir.getName(), entry.getKey().getName());
        Assert.assertEquals(-1L, entry.getValue().longValue());
    }

    /**
     * Tests the implementation of {@link FileReplicationUtils#getIndex(File)}.
     */
    @Test
    public void testGetIndex() throws Throwable
    {
        final String testString = RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(0, 1024));
        final CRC32 crc = new CRC32();
        crc.update(testString.getBytes());

        File testDir = null;
        try
        {
            testDir = new File(System.getProperty("java.io.tmpdir"), "testDir");
            FileUtils.forceMkdir(testDir);

            final File testSubDir = new File(testDir, "testSubdir");
            FileUtils.forceMkdir(testSubDir);

            FileUtils.writeStringToFile(new File(testDir, "testFile"), testString, StandardCharsets.UTF_8);
            FileUtils.writeStringToFile(new File(testSubDir, "testFile"), testString, StandardCharsets.UTF_8);

            final FileReplicationIndex idx = FileReplicationUtils.getIndex(testDir);
            Assert.assertNotNull(idx);
            Assert.assertFalse(idx.isEmpty());
            Assert.assertEquals(3, idx.keySet().size());

            for (final Map.Entry<File, Long> entry : idx.entrySet())
            {
                final File f = entry.getKey();
                if (f.getName().equals("testSubdir"))
                {
                    Assert.assertEquals(-1L, entry.getValue().longValue());
                }
                else
                {
                    Assert.assertEquals("testFile", f.getName());
                    Assert.assertEquals(crc.getValue(), entry.getValue().longValue());
                }
            }

        }
        finally
        {
            FileUtils.deleteQuietly(testDir);
        }
    }

    /**
     * Tests the implementation of
     * {@link FileReplicationUtils#compareIndexes(FileReplicationIndex, FileReplicationIndex, java.util.List, java.util.List)}
     * .
     */
    @Test
    public void testCompareIndexes() throws Throwable
    {

        final FileReplicationIndex source = new FileReplicationIndex();
        final FileReplicationIndex target = new FileReplicationIndex();

        // SOURCE
        source.put(new File("directory"), -1L);
        source.put(new File("samefile"), 4125412L);
        source.put(new File("newfile"), 99999L);
        source.put(new File("differentfile"), 87656L);
        source.put(new File("uppercaseLOWERCASE"), 7777L);
        source.put(new File("./path/moved"), 888L);

        source.put(new File("dir2/dir11/test.txt"), 717L);
        source.put(new File("dir2"), -1L);
        source.put(new File("dir2/dir11"), -1L);

        // TARGET
        target.put(new File("directory"), -1L);
        target.put(new File("samefile"), 4125412L);
        target.put(new File("differentfile"), 716161L);
        target.put(new File("toBeRemoved"), 12112L); // comment because it has to go
        target.put(new File("UPPERCASElowercase"), 7777L);
        target.put(new File("./path2/moved"), 888L);

        target.put(new File("dir1/dir11/test.txt"), 717L);
        target.put(new File("dir1"), -1L);
        target.put(new File("dir1/dir11"), -1L);

        final ArrayList<File> updates = new ArrayList<File>();
        final ArrayList<File> deletes = new ArrayList<File>();

        FileReplicationUtils.compareIndexes(source, target, updates, deletes);

        // check updates on windows
        if (SystemUtils.IS_OS_WINDOWS)
        {
            Assert.assertEquals(7, updates.size());
        }
        else
        {
            Assert.assertEquals(8, updates.size());
            Assert.assertTrue(updates.contains(new File("uppercaseLOWERCASE")));
        }

        Assert.assertTrue(updates.contains(new File("directory")));
        Assert.assertTrue(updates.contains(new File("newfile")));
        Assert.assertTrue(updates.contains(new File("differentfile")));
        Assert.assertTrue(updates.contains(new File("./path/moved")));
        Assert.assertTrue(updates.contains(new File("dir2/dir11/test.txt")));
        Assert.assertTrue(updates.contains(new File("dir2")));
        Assert.assertTrue(updates.contains(new File("dir2/dir11")));

        // check deletes
        if (SystemUtils.IS_OS_WINDOWS)
        {
            Assert.assertEquals(3, deletes.size());
        }
        else
        {
            Assert.assertEquals(4, deletes.size());
            Assert.assertTrue(deletes.contains(new File("UPPERCASElowercase")));
        }

        Assert.assertTrue(deletes.contains(new File("toBeRemoved")));
        Assert.assertTrue(deletes.contains(new File("./path2/moved")));
        Assert.assertTrue(deletes.contains(new File("dir1")));

    }

    /**
     * Tests the implementation of
     * {@link FileReplicationUtils#compareIndexes(FileReplicationIndex, FileReplicationIndex, java.util.List, java.util.List)}
     * and simulate different OS.
     */
    @Test
    public void testCompareIndexes_WinToLinux() throws Throwable
    {

        final FileReplicationIndex source = new FileReplicationIndex();
        source.put(new File(".\\path\\sample"), 4125412L);

        final FileReplicationIndex target = new FileReplicationIndex();
        target.put(new File("./path/sample"), 4125412L);

        final ArrayList<File> updates = new ArrayList<File>();
        final ArrayList<File> deletes = new ArrayList<File>();

        FileReplicationUtils.compareIndexes(source, target, updates, deletes);

        Assert.assertEquals(0, updates.size());
        Assert.assertEquals(0, deletes.size());
    }

    /**
     * Tests the implementation of
     * {@link FileReplicationUtils#compareIndexes(FileReplicationIndex, FileReplicationIndex, java.util.List, java.util.List)}
     * and simulate different OS.
     */
    @Test
    public void testCompareIndexes_LinuxToWin() throws Throwable
    {

        final FileReplicationIndex source = new FileReplicationIndex();
        source.put(new File("./path/sample"), 4125412L);

        final FileReplicationIndex target = new FileReplicationIndex();
        target.put(new File(".\\path\\sample"), 4125412L);

        final ArrayList<File> updates = new ArrayList<File>();
        final ArrayList<File> deletes = new ArrayList<File>();

        FileReplicationUtils.compareIndexes(source, target, updates, deletes);

        Assert.assertEquals(0, updates.size());
        Assert.assertEquals(0, deletes.size());
    }

    /**
     * Tests the implementation of {@link FileReplicationUtils#sanitizeFileReplicationIndex(FileReplicationIndex)} Are
     * on Windows.
     */
    @Test
    public void testSanitize_OnWindows() throws Throwable
    {
        final FileReplicationIndex source = new FileReplicationIndex();
        source.put(new File(".\\path\\win.txt"), 3L);
        source.put(new File("./path/linux.txt"), 4L);

        final char currentSeparatorChar = File.separatorChar;
        FileReplicationIndex newIndex;

        try
        {
            AbstractXLTTestCase.setFinalStatic(File.class, "separatorChar", '\\');
            newIndex = FileReplicationUtils.sanitizeFileReplicationIndex(source);
        }
        finally
        {
            AbstractXLTTestCase.setFinalStatic(File.class, "separatorChar", currentSeparatorChar);
        }

        Assert.assertTrue(2 == newIndex.size());

        Assert.assertEquals(Long.valueOf(4), newIndex.get(new File(".\\path\\linux.txt")));
        Assert.assertEquals(Long.valueOf(3), newIndex.get(new File(".\\path\\win.txt")));
    }

    /**
     * Tests the implementation of {@link FileReplicationUtils#sanitizeFileReplicationIndex(FileReplicationIndex)} Are
     * on Linux.
     */
    @Test
    public void testSanitize_OnLinux() throws Throwable
    {
        final FileReplicationIndex source = new FileReplicationIndex();
        source.put(new File(".\\path\\win.txt"), 3L);
        source.put(new File("./path/linux.txt"), 4L);

        final char currentSeparatorChar = File.separatorChar;
        FileReplicationIndex newIndex;

        try
        {
            AbstractXLTTestCase.setFinalStatic(File.class, "separatorChar", '/');
            newIndex = FileReplicationUtils.sanitizeFileReplicationIndex(source);
        }
        finally
        {
            AbstractXLTTestCase.setFinalStatic(File.class, "separatorChar", currentSeparatorChar);
        }

        Assert.assertTrue(2 == newIndex.size());

        Assert.assertEquals(Long.valueOf(4), newIndex.get(new File("./path/linux.txt")));
        Assert.assertEquals(Long.valueOf(3), newIndex.get(new File("./path/win.txt")));
    }

    /**
     * Test the {@link FileReplicationIndex#toString} method
     */
    @Test
    public void testIndex_toString() throws Throwable
    {
        final FileReplicationIndex source = new FileReplicationIndex();
        final File files[] = new File[]
            {
                new File("./path/sample3/foo"), new File("./path/sample"), new File("./sample/test.txt"), new File("./path/sample2/foo")
            };
        // add files to replication index
        for (int i = 0; i < files.length; i++)
        {
            source.put(files[i], (long) (i + 1));
        }

        // sort files by path
        Arrays.sort(files, new Comparator<File>()
        {
            @Override
            public int compare(final File o1, final File o2)
            {
                return o1.getPath().compareTo(o2.getPath());
            }
        });
        // build output string
        final StringBuilder sb = new StringBuilder();
        for (final File f : files)
        {
            sb.append(f).append(" = ").append(source.get(f)).append("\n");
        }
        Assert.assertEquals(sb.toString(), source.toString());
    }
}
