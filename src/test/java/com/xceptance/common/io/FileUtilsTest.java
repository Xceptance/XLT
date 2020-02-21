package com.xceptance.common.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.xceptance.xlt.AbstractXLTTestCase;

/**
 * Tests the FileUtils implementation.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class FileUtilsTest extends AbstractXLTTestCase
{
    /** Directory named 'test' rooted at system directory for temporary files. */
    private File testDir;

    /** Test file named 'source.txt' rooted at 'test'. */
    private File sourceFile;

    /** Test file named 'dest.txt' rooted at 'test'. */
    private File destFile;

    /** Test-run specific temp-directory rooted at system-specific directory for temporary files. */
    private File tempDir;

    /** Unremovable directory rooted at {@link #removeTolerateDir} */
    private File unremoveableSubdir;

    /** Unremovable file rooted at {@link #unremoveableSubdir}. */
    private File unremoveableFile;

    /** Test-run specific directory rooted at test-run specific temp-directory. */
    private File removeTolerateDir;

    /** Content for 'source.txt' and other test files. */
    private final static String testFileContent = "This is a text file for testing purposes.";

    /** Class logger. */
    private final static Logger LOGGER = Logger.getLogger(FileUtilsTest.class);

    /** Name of test directory. Uses random suffix to prevent file name clashes. */
    private final static String testDirName = "test" + new Random().nextInt(1000);

    /** The current working directory. */
    private static String currentWorkingDirectory;

    /** The file comparator used for sorting file arrays alpha-numerically. */
    private static final Comparator<File> fileComparator = new Comparator<File>()
    {
        @Override
        public int compare(File o1, File o2)
        {
            return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
        }
    };

    /** Initializes log4j configuration. */
    static
    {
        BasicConfigurator.configure();
    }

    @BeforeClass
    public static void setUp()
    {
        currentWorkingDirectory = FileUtils.getCurrentWorkingDirectory();
        if (SystemUtils.IS_OS_WINDOWS)
        {
            FileUtils.setCurrentWorkingDirectory("F:/home/xlt");
        }
        else
        {
            FileUtils.setCurrentWorkingDirectory("/home/xlt");
        }
    }

    @Before
    public void init() throws Exception
    {
        testDir = new File(getTempDir(), testDirName);
        sourceFile = new File(testDir, "source.txt");
        destFile = new File(testDir, "dest.txt");

        testDir.mkdir();
        sourceFile.createNewFile();
        destFile.createNewFile();

        final FileWriter fw = new FileWriter(sourceFile);
        try
        {
            fw.write(testFileContent);
        }
        finally
        {
            fw.close();
        }

        final String random = RandomStringUtils.random(10, "qwertzuioplkjhgfdsayxcvbnm");
        tempDir = new File(System.getProperty("java.io.tmpdir"), random);
        tempDir.mkdir();

        // gets cleanup automatically
        removeTolerateDir = new File(tempDir, "removetoleratedir");
        removeTolerateDir.mkdir();
        new File(removeTolerateDir, "t0000000000.txt").createNewFile();
        new File(removeTolerateDir, "t0000000001.txt").createNewFile();

        final File subdir = new File(removeTolerateDir, "subdir");
        subdir.mkdir();

        new File(subdir, "t0000000000.txt").createNewFile();
        new File(subdir, "t0000000001.txt").createNewFile();

        unremoveableSubdir = new File(removeTolerateDir, "unremovablesubdir");
        unremoveableSubdir.mkdir();
        unremoveableFile = new File(unremoveableSubdir, "t0000000000.txt");
        unremoveableFile.createNewFile();
        new File(unremoveableSubdir, "t0000000001.txt").createNewFile();

    }

    @After
    public void tidyup() throws IOException
    {
        if (testDir.exists())
        {
            testDir.setWritable(true);
            Assert.assertTrue(removeDirectory(testDir));
        }

        // set removable
        unremoveableSubdir.setReadable(true);
        unremoveableFile.setReadable(true, false);

        org.apache.commons.io.FileUtils.deleteDirectory(tempDir);
    }

    @AfterClass
    public static void tearDown()
    {
        FileUtils.setCurrentWorkingDirectory(currentWorkingDirectory);
    }

    /**
     * Test file duplication.
     * 
     * @throws IOException
     */
    @Test
    public void testFileCopy() throws IOException
    {
        // copy 'source' to 'dest'
        FileUtils.copyFile(sourceFile, destFile);

        // make sure, we have a real 'dest' file ...
        Assert.assertTrue(destFile.exists());
        // ... with the right content
        Assert.assertEquals(testFileContent, readFile(destFile));
    }

    /**
     * Verifies that an exception is thrown, if the source file is null.
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCopyFileSourceIsNull() throws IOException
    {
        FileUtils.copyFile(null, destFile);
    }

    /**
     * Verifies that an exception is thrown, if the target file is null.
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCopyFileTargetIsNull() throws IOException
    {
        FileUtils.copyFile(sourceFile, null);
    }

    /**
     * Verifies that an exception is thrown, if the source file is a directory.
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCopyFileSourceIsDirectory() throws IOException
    {
        FileUtils.copyFile(testDir, destFile);
    }

    /**
     * Test directory duplication in a target directory.
     * 
     * @throws IOException
     */
    @Test
    public void testCopyDirectoryContentOnlyFalse() throws IOException
    {
        /*
         * test source sourceSubDir testSub.txt test.txt target source sourceSubDir testSub.txt test.txt
         */

        // create handle for a new directory named 'source' rooted at 'test'
        final File sourceDir = new File(testDir, "source");
        // create handle for a new directory named 'sourceSubDir' rooted at 'source'
        final File sourceSubDir = new File(sourceDir, "sourceSubDir");
        // create handle for directory sibling named 'target'
        final File targetDir = new File(testDir, "target");
        // create handle for test file named 'test.txt' rooted at 'source'
        final File testFile = new File(sourceDir, "test.txt");
        // create handle for test file named 'testSub.txt' rooted at 'sourceSubDir'
        final File testFileInSubDir = new File(sourceSubDir, "testSub.txt");

        // create directory 'source'
        Assert.assertTrue(sourceDir.mkdir());
        // create directory 'sourceSubDir'
        Assert.assertTrue(sourceSubDir.mkdir());
        // create new empty file 'test.txt'
        Assert.assertTrue(testFile.createNewFile());
        // create new empty file 'testSub.txt'
        Assert.assertTrue(testFileInSubDir.createNewFile());
        // create directory 'target'
        Assert.assertTrue(targetDir.mkdir());

        // copy 'sourceFile' to 'test.txt'
        FileUtils.copyFile(sourceFile, testFile);

        // copy directory 'source' to 'target'
        FileUtils.copyDirectory(sourceDir, targetDir, false);

        // get content of 'target'
        final File[] targetDirFiles = targetDir.listFiles();
        // must be exactly one file ...
        Assert.assertEquals(1, targetDirFiles.length);
        // ... named 'source' ...
        Assert.assertEquals(sourceDir.getName(), targetDirFiles[0].getName());
        // ... and is directory
        Assert.assertTrue(targetDirFiles[0].isDirectory());

        // get content of 'target->source'
        final File[] files = targetDirFiles[0].listFiles();
        Arrays.sort(files, fileComparator);

        // must be exactly two files ...
        Assert.assertEquals(2, files.length);
        // the first is the directory
        Assert.assertTrue(files[0].isDirectory());
        // ... named 'sourceSubDir'
        Assert.assertEquals(sourceSubDir.getName(), files[0].getName());
        // and the second which is a real file
        Assert.assertTrue(files[1].isFile());
        // ... named 'test.txt' ...
        Assert.assertEquals(testFile.getName(), files[1].getName());
        // ... and with the right content
        Assert.assertEquals(testFileContent, readFile(files[1]));

        // get the content of 'target->source->sourceSubDir'
        final File[] targetSubDirFiles = files[0].listFiles();
        // must be exactly one file ...
        Assert.assertEquals(1, targetSubDirFiles.length);
        // ... named 'testSub.txt' ...
        Assert.assertEquals(testFileInSubDir.getName(), targetSubDirFiles[0].getName());
        // ... and is a file
        Assert.assertTrue(targetSubDirFiles[0].isFile());
    }

    /**
     * Test directory duplication to a target directory.
     * 
     * @throws IOException
     */
    @Test
    public void testCopyDirectoryContentOnlyTrue() throws IOException
    {
        /*
         * test source sourceSubDir testSub.txt test.txt target sourceSubDir testSub.txt test.txt
         */

        // create handle for a new directory named 'source' rooted at 'test'
        final File sourceDir = new File(testDir, "source");
        // create handle for a new directory named 'sourceSubDir' rooted at 'source'
        final File sourceSubDir = new File(sourceDir, "sourceSubDir");
        // create handle for directory sibling named 'target'
        final File targetDir = new File(testDir, "target");
        // create handle for test file named 'test.txt' rooted at 'source'
        final File testFile = new File(sourceDir, "test.txt");
        // create handle for test file named 'testSub.txt' rooted at 'sourceSubDir'
        final File testFileInSubDir = new File(sourceSubDir, "testSub.txt");

        // create directory 'source'
        Assert.assertTrue(sourceDir.mkdir());
        // create directory 'sourceSubDir'
        Assert.assertTrue(sourceSubDir.mkdir());
        // create new empty file 'test.txt'
        Assert.assertTrue(testFile.createNewFile());
        // create new empty file 'testSub.txt'
        Assert.assertTrue(testFileInSubDir.createNewFile());
        // create directory 'target'
        Assert.assertTrue(targetDir.mkdir());

        // copy 'sourceFile' to 'test.txt'
        FileUtils.copyFile(sourceFile, testFile);

        // copy directory 'source' to 'target'
        FileUtils.copyDirectory(sourceDir, targetDir, true);

        // get content of 'target'
        final File[] targetDirFiles = targetDir.listFiles();
        Arrays.sort(targetDirFiles, fileComparator);
        // must be exactly two files ...
        Assert.assertEquals(2, targetDirFiles.length);
        // the first is a directory
        Assert.assertTrue(targetDirFiles[0].isDirectory());
        // ... named 'sourceSubDir'
        Assert.assertEquals(sourceSubDir.getName(), targetDirFiles[0].getName());
        // and the second which is a real file
        Assert.assertTrue(targetDirFiles[1].isFile());
        // ... named 'test.txt' ...
        Assert.assertEquals(testFile.getName(), targetDirFiles[1].getName());
        // ... and with the right content
        Assert.assertEquals(testFileContent, readFile(targetDirFiles[1]));

        // get content of 'target->sourceSubDir'
        final File[] targetSubDirFiles = targetDirFiles[0].listFiles();
        // must be exactly one file ...
        Assert.assertEquals(1, targetSubDirFiles.length);
        // ... named 'testSub.txt' ...
        Assert.assertEquals(testFileInSubDir.getName(), targetSubDirFiles[0].getName());
        // ... and is a file
        Assert.assertTrue(targetSubDirFiles[0].isFile());
    }

    /**
     * Verifies that an exception is thrown, if the source directory is null.
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCopyDirectorySourceDirIsNull() throws IOException
    {
        FileUtils.copyDirectory(null, testDir, false);
    }

    /**
     * Verifies that an exception is thrown, if the target directory is null.
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCopyDirectoryTargetIsNull() throws IOException
    {
        FileUtils.copyDirectory(testDir, null, false);
    }

    /**
     * Verifies that an exception is thrown, if the source file is not a directory.
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCopyDirectorySourceIsAFile() throws IOException
    {
        FileUtils.copyDirectory(sourceFile, testDir, false);
    }

    /**
     * Verifies that an exception is thrown, if the target file is not a directory.
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCopyDirectoryTargetIsAFile() throws IOException
    {
        FileUtils.copyDirectory(testDir, destFile, false);
    }

    /**
     * Test for directory duplication. Copy content only.
     * 
     * @throws IOException
     */
    @Test
    public void testCopyDirectory_ContentOnly() throws IOException
    {
        // create handle for a new directory named 'source' rooted at 'test'
        final File sourceDir = new File(testDir, "source");
        // create handle for a directory sibling named 'target'
        final File targetDir = new File(testDir, "target");
        // in directory 'source' create a handle for a new file named 'test.txt'
        final File testFile = new File(sourceDir, "test.txt");

        // create the directory 'test'
        Assert.assertTrue(sourceDir.mkdir());
        // create the file 'test.txt'
        Assert.assertTrue(testFile.createNewFile());
        // create the directory 'dest'
        Assert.assertTrue(targetDir.mkdir());

        // copy the test file to 'test.txt'
        FileUtils.copyFile(sourceFile, testFile);
        // copy directory 'test' to 'dest' !content only!
        FileUtils.copyDirectory(sourceDir, targetDir, true);

        // get file list for 'dest'
        final File[] files = targetDir.listFiles();
        // have to be exactly one file...
        Assert.assertEquals(1, files.length);
        // ... named 'text.txt'..
        Assert.assertEquals("test.txt", files[0].getName());
        // ... containing the test file content
        Assert.assertEquals(testFileContent, readFile(files[0]));
    }

    /**
     * Test file deletion for a given directory.
     * 
     * @throws IOException
     */
    @Test
    public void testFileDeletionFromDirectory() throws IOException
    {
        // create handle for new directory named 'aDir' rooted at 'test'
        final File aDir = new File(testDir, "aDir");
        // create handle for new test file named 'test.txt' rooted at 'aDir'
        final File testFile = new File(aDir, "test.txt");
        // create handle for new subdirectory named 'bDir' rooted at 'aDir'
        final File bDir = new File(aDir, "bDir");
        // create handle for new test file named 'tescht' rooted at 'bDir'
        final File subTestFile = new File(bDir, "tescht");

        // create directory 'aDir'
        Assert.assertTrue(aDir.mkdir());
        // create subdirectory 'bDir'
        Assert.assertTrue(bDir.mkdir());
        // create test file 'test.txt'
        Assert.assertTrue(testFile.createNewFile());
        // create test file 'tescht'
        Assert.assertTrue(subTestFile.createNewFile());

        // delete all files rooted at 'aDir'
        FileUtils.deleteFilesFromDirectory(aDir);

        // validation
        Assert.assertFalse(bDir.exists());
        Assert.assertFalse(subTestFile.exists());
        Assert.assertFalse(testFile.exists());
        Assert.assertTrue(aDir.exists());
    }

    /**
     * Verifies that an exception is thrown, if the directory is null.
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDeleteFilesFromDirectoryNull() throws IOException
    {
        FileUtils.deleteFilesFromDirectory(null);
    }

    /**
     * Verifies that an exception is thrown, if the directory is a file.
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDeleteFilesFromDirectoryIsFile() throws IOException
    {
        FileUtils.deleteFilesFromDirectory(destFile);
    }

    /**
     * Test file deletion for a given directory using a file filter.
     * 
     * @throws IOException
     */
    @Test
    public void testFileDeletionFromDirectory_UseFilter() throws IOException
    {
        // create handle for new directory 'aDir' rooted at 'test'
        final File aDir = new File(testDir, "aDir");
        // create handle for new test file named 'test.txt' rooted at 'aDir'
        final File testFile = new File(aDir, "test.txt");
        // create handle for new subdirectory named 'bDir' rooted at 'aDir'
        final File bDir = new File(aDir, "bDir");
        // create handle for new test file named 'tescht' rooted at 'bDir'
        final File subTestFile = new File(bDir, "tescht");

        // create file filter -> accept only regular files
        final FileFilter filter = new FileFilter()
        {
            @Override
            public boolean accept(final File pathname)
            {
                return pathname.isFile();
            }

        };

        // create directory 'test'
        Assert.assertTrue(aDir.mkdir());
        // create subdirectory 'thebest'
        Assert.assertTrue(bDir.mkdir());
        // create test file 'test.txt'
        Assert.assertTrue(testFile.createNewFile());
        // create test file 'tescht'
        Assert.assertTrue(subTestFile.createNewFile());

        // delete all toplevel files matched by filter
        FileUtils.deleteFilesFromDirectory(aDir, filter);

        // validation
        Assert.assertTrue(bDir.exists());
        Assert.assertTrue(subTestFile.exists());
        Assert.assertFalse(testFile.exists());
        Assert.assertTrue(aDir.exists());
    }

    /**
     * Verifies that an exception is thrown, if the file is null.
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDeleteFileIsNull() throws IOException
    {
        FileUtils.deleteFile(null);
    }

    /**
     * Test file deletion, if the file is not a directory.
     * 
     * @throws IOException
     */
    @Test
    public void testDeleteFile() throws IOException
    {
        Assert.assertTrue("The target file doesn't exist!", destFile.exists());
        FileUtils.deleteFile(destFile);
        Assert.assertFalse("The target file wasn't deleted!", destFile.exists());
    }

    /**
     * Test file deletion, if the file is a directory.
     * 
     * @throws IOException
     */
    @Test
    public void testDeleteFileIsADirectory() throws IOException
    {
        Assert.assertTrue("The test directory doesn't exist!", testDir.exists());
        FileUtils.deleteFile(testDir);
        Assert.assertFalse("The test directory wasn't deleted!", testDir.exists());
    }

    /**
     * Verifies that no exception is thrown, if the file doesn't exist.
     * 
     * @throws IOException
     */
    @Test
    public void testDeleteFileDoesntExist() throws IOException
    {
        // directory doesn't exist
        File testFile = new File("test");
        Assert.assertFalse(testFile.exists());
        FileUtils.deleteFile(testFile);
        // file doesn't exist
        testFile = new File("test.txt");
        Assert.assertFalse(testFile.exists());
        FileUtils.deleteFile(testFile);
    }

    /**
     * Test file listing. FileUtils implementation uses java.io.File implementation for non-recursive calls, so only
     * recursive calls have to be tested.
     * 
     * @throws IOException
     */
    @Test
    public void testListFiles() throws IOException
    {
        // create handle for new directory named 'aDir' rooted at 'test'
        final File aDir = new File(testDir, "aDir");
        // create handle for new subdirectory named 'bDir' rooted at 'aDir'
        final File bDir = new File(aDir, "subtest");
        // create handle for new test file named 'test1' rooted at 'aDir'
        final File testFile1 = new File(aDir, "test1");
        // create handle for new test file named 'test2' rooted at 'aDir'
        final File testFile2 = new File(aDir, "test2");
        // create handle for new test file named 'test3' rooted at 'bDir'
        final File testFile3 = new File(bDir, "test3");

        // create directory 'aDir'
        Assert.assertTrue(aDir.mkdir());
        // create directory 'bDir'
        Assert.assertTrue(bDir.mkdir());
        // create test file 'test1'
        Assert.assertTrue(testFile1.createNewFile());
        // create test file 'test2'
        Assert.assertTrue(testFile2.createNewFile());
        // create test file 'test3'
        Assert.assertTrue(testFile3.createNewFile());

        // get all files (recursively) rooted at 'aDir'
        File[] files = FileUtils.listFiles(aDir, true);

        // validate
        Assert.assertEquals(4, files.length);
        for (final File f : files)
        {
            if (f.isDirectory())
            {
                Assert.assertEquals(bDir, f);
            }

            if (f.isFile())
            {
                Assert.assertTrue(f.equals(testFile1) || f.equals(testFile2) || f.equals(testFile3));
            }
        }

        // create new filename filter -> accept all files that starts with
        // 'test'
        final FilenameFilter filter = new FilenameFilter()
        {

            @Override
            public boolean accept(final File dir, final String name)
            {
                return name.startsWith("test");
            }
        };

        // get all files (recursively) that are rooted at 'aDir' and are
        // matched
        // by filter
        files = FileUtils.listFiles(aDir, true, filter);

        // validate
        Assert.assertEquals(3, files.length);
        for (final File f : files)
        {
            Assert.assertTrue(f.isFile());
            Assert.assertTrue(f.equals(testFile1) || f.equals(testFile2) || f.equals(testFile3));
        }
    }

    /**
     * Verifies that an exception is thrown, if the directory is null.
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testListFilesDirectoryIsNull() throws IOException
    {
        FileUtils.listFiles(null, false);
    }

    /**
     * Verifies that an exception is thrown, if the directory is a file.
     * 
     * @throws IOException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testListFilesDirectoryIsAFile() throws IOException
    {
        FileUtils.listFiles(destFile, false);
    }

    /**
     * Test 'illegal char'-replacement.
     */
    @Test
    public void testReplaceIllegalCharsInFilename()
    {
        final String testString = "T/\\<>|*?:\"T";

        Assert.assertEquals("T_________T", FileUtils.replaceIllegalCharsInFileName(testString));
    }

    /**
     * Test the conversion of illegal characters and try the converted result on the file system
     * 
     * @throws IOException
     */
    @Test
    public void testConvertIllegalCharsInFileName_Valid() throws IOException
    {
        final String fileName = "This is a Test.";

        createFile(FileUtils.convertIllegalCharsInFileName(fileName));
    }

    /**
     * Test the conversion of illegal characters and try the converted result on the file system
     * 
     * @throws IOException
     */
    @Test
    public void testConvertIllegalCharsInFileName_Invalid01() throws IOException
    {
        final String fileName = "a!\"§$%&/()=?{[]}\\?+*~#'_-:.;,<>|";

        createFile(FileUtils.convertIllegalCharsInFileName(fileName));
    }

    /**
     * Verifies that an exception is thrown, if the directory is null.
     * 
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCleanDirRelaxedIsNull() throws Exception
    {
        FileUtils.cleanDirRelaxed(null);
    }

    /**
     * Verifies that an exception is thrown, if the directory is a file.
     * 
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCleanDirRelaxedIsFile() throws Exception
    {
        FileUtils.cleanDirRelaxed(sourceFile);
    }

    /**
     * Verifies the correct deletion of a directory.
     * 
     * @throws IOException
     */
    @Test
    public void testCleanDirRelaxed() throws IOException
    {
        // verifies that all files exist
        Assert.assertTrue(testDir.exists());
        Assert.assertTrue(sourceFile.exists());
        Assert.assertTrue(destFile.exists());
        // clean the directory
        Assert.assertTrue(FileUtils.cleanDirRelaxed(testDir));
        // verifies, that all files are deleted
        Assert.assertTrue(testDir.exists());
        Assert.assertFalse(sourceFile.exists());
        Assert.assertFalse(destFile.exists());
    }

    /**
     * Verifies that no exception is thrown, if the directory doesn't exist.
     * 
     * @throws IOException
     */
    @Test
    public void testCleanDirRelaxedDoesntExist() throws IOException
    {
        final File test = new File("test");
        Assert.assertFalse(test.exists());
        Assert.assertTrue(FileUtils.cleanDirRelaxed(test));
    }

    /**
     * Verifies that all sub directories are also deleted.
     * 
     * @throws IOException
     */
    @Test
    public void testCleanDirRelaxedWithSubDirectories() throws IOException
    {
        final File testDirSub1 = new File(testDir, "testDirSub1");
        final File testDirSub1File = new File(testDirSub1, "file.txt");
        Assert.assertTrue("Unable to create sub directory!", testDirSub1.mkdir());
        Assert.assertTrue("Unable to create a file in the sub directory!", testDirSub1File.createNewFile());
        // verifies that all files exist
        Assert.assertTrue(testDir.exists());
        Assert.assertTrue(sourceFile.exists());
        Assert.assertTrue(destFile.exists());
        Assert.assertTrue(testDirSub1.exists());
        Assert.assertTrue(testDirSub1File.exists());
        // clean the test directory
        Assert.assertTrue(FileUtils.cleanDirRelaxed(testDir));
        // verifies that the directory is clean
        Assert.assertTrue(testDir.exists());
        Assert.assertFalse(sourceFile.exists());
        Assert.assertFalse(destFile.exists());
        Assert.assertFalse(testDirSub1.exists());
        Assert.assertFalse(testDirSub1File.exists());
    }

    /**
     * Verifies that an exception is thrown, if a file in the directory can not be deleted.
     * 
     * @throws IOException
     */
    @Test(expected = IOException.class)
    public void testCleanDirRelaxedFileNotDeleted() throws IOException
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            throw new IOException("Skipped");
        }

        testDir.setReadOnly();
        FileUtils.cleanDirRelaxed(testDir);
        testDir.setReadable(true);
    }

    /**
     * Verifies that an exception is thrown, if the file is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetNumberOfAncestorsFileIsNull()
    {
        FileUtils.getNumberOfAncestors(null);
    }

    /**
     * Verifies that the root has no ancestors.
     */
    @Test
    public void testGetNumberOfAncestorsRoot()
    {
        final File test = new File("/");
        final int numberAncestors = FileUtils.getNumberOfAncestors(test);
        Assert.assertEquals(0, numberAncestors);
    }

    /**
     * Verifies that a file with an empty path has no ancestors.
     */
    @Test
    public void testGetNumberOfAncestorsEmptyString()
    {
        final File test = new File("");
        final int numberAncestors = FileUtils.getNumberOfAncestors(test);
        Assert.assertEquals(0, numberAncestors);
    }

    /**
     * Verifies that sub directories are resolved correctly.
     */
    @Test
    public void testGetNumberOfAncestorsDirectory()
    {
        final File test = new File("test/subDirectory/subSubDirectory");
        final int numberAncestors = FileUtils.getNumberOfAncestors(test);
        Assert.assertEquals(2, numberAncestors);
    }

    /**
     * Verifies the correct number of ancestors of a file.
     */
    @Test
    public void testGetNumberOfAncestorsFile()
    {
        final File test = new File("test.txt");
        final int numberAncestors = FileUtils.getNumberOfAncestors(test);
        Assert.assertEquals(0, numberAncestors);
    }

    /**
     * Verifies the correct number of ancestors of a file in a sub directory.
     */
    @Test
    public void testGetNumberOfAncestorsFileInSubDirectory()
    {
        final File test = new File("tmp/test/testSub/testSub1.1/test.txt");
        final int numberAncestors = FileUtils.getNumberOfAncestors(test);
        Assert.assertEquals(4, numberAncestors);
    }

    /**
     * Verifies the correct number of ancestors of an absolute path name.
     */
    @Test
    public void testGetNumberOfAncestorsAbsolutPath()
    {
        final File test = new File("/tmp/test/testSub/testSub1.1/test.txt");
        final int numberAncestors = FileUtils.getNumberOfAncestors(test);
        Assert.assertEquals(5, numberAncestors);
    }

    /**
     * Verifies that an exception is thrown, if the file is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetParentsFileIsNull()
    {
        FileUtils.getParents(null, 0);
    }

    /**
     * Verifies that a simple directory has no ancestors.
     */
    @Test
    public void testGetParentsNoParent()
    {
        final File test = new File("test");
        final List<File> parents = FileUtils.getParents(test, 0);
        Assert.assertEquals(0, parents.size());
    }

    /**
     * Verifies the correct number and order of the ancestors of a sub directory.
     */
    @Test
    public void testGetParentsMoreParents()
    {
        final File test = new File("test/subDirectory/subSubDirectory");
        final List<File> parents = FileUtils.getParents(test, 0);
        Assert.assertEquals(2, parents.size());
        Assert.assertEquals(new File("test"), parents.get(0));
        Assert.assertEquals(new File("test/subDirectory"), parents.get(1));
    }

    /**
     * Verifies the correct number and order of the ancestors of a sub directory, if the first ancestor has to skip.
     */
    @Test
    public void testGetParentsMoreParentsSkipFirst()
    {
        final File test = new File("test/subDirectory/subSubDirectory");
        final List<File> parents = FileUtils.getParents(test, 1);
        Assert.assertEquals(1, parents.size());
        Assert.assertEquals(new File("test/subDirectory"), parents.get(0));
    }

    /**
     * Verifies the correct number and order of the ancestors of a sub directory, if the pathname is absolute.
     */
    @Test
    public void testGetParentsAbsolutePath()
    {
        final File test = new File("/test/subDirectory/subSubDirectory");
        final List<File> parents = FileUtils.getParents(test, 0);
        Assert.assertEquals(3, parents.size());
        Assert.assertEquals(new File("/"), parents.get(0));
        Assert.assertEquals(new File("/test"), parents.get(1));
        Assert.assertEquals(new File("/test/subDirectory"), parents.get(2));
    }

    @Test
    public void testComputeRelativeUriSourceAndTargetAreEqual()
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            final File file1 = new File("C:/home/user/workspace");
            final File file2 = new File("C:/home/user/workspace");
            Assert.assertEquals(".", FileUtils.computeRelativeUri(file1, file2, true));
        }
        else
        {
            final File file1 = new File("/home/user/workspace");
            final File file2 = new File("/home/user/workspace");
            Assert.assertEquals(".", FileUtils.computeRelativeUri(file1, file2, true));
        }
    }

    @Test
    public void testComputeRelativeUriGoBackToRoot()
    {
        final File file1 = new File("/test/home/user/documents/workspace");
        final File file2 = new File("/home/user/workspace");
        Assert.assertEquals("../../../../../home/user/workspace", FileUtils.computeRelativeUri(file1, file2, true));
    }

    @Test
    public void testComputeRelativeUriOnSameDrive()
    {
        final File file1 = new File("/home/user/documents/workspace");
        final File file2 = new File("/home/user/workspace");
        Assert.assertEquals("../../workspace", FileUtils.computeRelativeUri(file1, file2, true));
    }

    @Test
    public void testComputeRelativeUriMoreSlashesInSourceURI()
    {
        final File file1 = new File("/home/user///////documents/workspace");
        final File file2 = new File("/home/user/workspace");
        Assert.assertEquals("../../workspace", FileUtils.computeRelativeUri(file1, file2, true));
    }

    @Test
    public void testComputeRelativeUriMoreSlashesInTargetURI()
    {
        final File file1 = new File("/home/user/documents/workspace");
        final File file2 = new File("/home/user///////workspace");
        Assert.assertEquals("../../workspace", FileUtils.computeRelativeUri(file1, file2, true));
    }

    @Test
    public void testComputeRelativeUriMoreSlashesInBothURIs()
    {
        final File file1 = new File("/home/user/documents//////workspace");
        final File file2 = new File("/home/user///////workspace");
        Assert.assertEquals("../../workspace", FileUtils.computeRelativeUri(file1, file2, true));
    }

    @Test
    public void testComputeRelativeUriMoreSlashesAtBeginningOfURI()
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            final File file1 = new File("C:/home////user/documents/workspace///");
            final File file2 = new File("C://///home/user///////workspace");
            Assert.assertEquals("../../workspace", FileUtils.computeRelativeUri(file1, file2, true));
        }
        else
        {
            final File file1 = new File("/home////user/documents/workspace///");
            final File file2 = new File("/////home/user///////workspace");
            Assert.assertEquals("../../workspace", FileUtils.computeRelativeUri(file1, file2, true));
        }
    }

    @Test
    public void testComputeRelativeUriRelativeSourceURI()
    {
        final File file1 = new File("user/documents/workspace");
        final File file2 = new File("/home/otherUser/workspace");
        Assert.assertEquals("../../../../otherUser/workspace", FileUtils.computeRelativeUri(file1, file2, true));
    }

    @Test
    public void testComputeRelativeUriRelativeSourceURI_leadingDot()
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            final File file1 = new File("./src");
            final File file2 = new File("F:/home/xlt/target");
            Assert.assertEquals("../target", FileUtils.computeRelativeUri(file1, file2, true));
        }
        else
        {
            final File file1 = new File("./src");
            final File file2 = new File("/home/xlt/target");
            Assert.assertEquals("../target", FileUtils.computeRelativeUri(file1, file2, true));
        }
    }

    @Test
    public void testComputeRelativeUriRelativeTargetURI()
    {
        final File file1 = new File("/home/user/documents/workspace");
        final File file2 = new File("otherUser/workspace");
        Assert.assertEquals("../../../xlt/otherUser/workspace", FileUtils.computeRelativeUri(file1, file2, true));
    }

    @Test
    public void testComputeRelativeUriRelativeTargetURI_leadingDot()
    {
        if (SystemUtils.IS_OS_WINDOWS)
        {
            final File file1 = new File("F:/home/xlt/src");
            final File file2 = new File("./target");
            Assert.assertEquals("../target", FileUtils.computeRelativeUri(file1, file2, true));
        }
        else
        {
            final File file1 = new File("/home/xlt/src");
            final File file2 = new File("./target");
            Assert.assertEquals("../target", FileUtils.computeRelativeUri(file1, file2, true));
        }
    }

    @Test
    public void testComputeRelativeUriWithSpecialCharacterAndBlank()
    {
        final File file1 = new File("/home/user Ä?_ß$/own Data!/workspace/");
        final File file2 = new File("/home/user Ä?_ß$/workspace");
        Assert.assertEquals("../../workspace", FileUtils.computeRelativeUri(file1, file2, true));
    }

    @Test
    public void testComputeRelativeUriTargetURIOnDifferentDriveAndAbsoluteSourceURI()
    {
        // necessary under Microsoft Windows
        if (SystemUtils.IS_OS_WINDOWS)
        {
            final File file1 = new File("/test/home/user/documents/workspace");
            final File file2 = new File("D:/home/user/workspace");
            Assert.assertEquals("file:///D:/home/user/workspace", FileUtils.computeRelativeUri(file1, file2, true));
        }
    }

    @Test
    public void testComputeRelativeUriTargetURIOnDifferentDriveAndRelativeSourceURI()
    {
        // necessary under Microsoft Windows
        if (SystemUtils.IS_OS_WINDOWS)
        {
            final File file1 = new File("test/home/user/documents/workspace");
            final File file2 = new File("D:/home/user/workspace");
            Assert.assertEquals("file:///D:/home/user/workspace", FileUtils.computeRelativeUri(file1, file2, true));
        }
    }

    @Test
    public void testComputeRelativeUriSourceURIOnDifferentDriveAndAbsoluteTargetURI()
    {
        // necessary under Microsoft Windows
        if (SystemUtils.IS_OS_WINDOWS)
        {
            final File file1 = new File("C:/home/user/documents/workspace");
            final File file2 = new File("/otherUser/workspace");
            Assert.assertEquals("file:///F:/otherUser/workspace", FileUtils.computeRelativeUri(file1, file2, true));
        }
    }

    @Test
    public void testComputeRelativeUriSourceURIOnDifferentDriveAndRelativeTargetURI()
    {
        // necessary under Microsoft Windows
        if (SystemUtils.IS_OS_WINDOWS)
        {
            final File file1 = new File("C:/home/user/documents/workspace");
            final File file2 = new File("otherUser/workspace");
            Assert.assertEquals("file:///F:/home/xlt/otherUser/workspace", FileUtils.computeRelativeUri(file1, file2, true));
        }
    }

    @Test
    public void testComputeRelativeUriBothURIsOnDifferentDrives()
    {
        // necessary under Microsoft Windows
        if (SystemUtils.IS_OS_WINDOWS)
        {
            final File file1 = new File("C:/test/home/user/documents/workspace");
            final File file2 = new File("D://home/user/workspace");
            Assert.assertEquals("file:///D:/home/user/workspace", FileUtils.computeRelativeUri(file1, file2, true));
        }
    }

    @Test
    public void testComputeRelativeUriSourceRootWithTwoSlashes()
    {
        // necessary under Microsoft Windows
        if (SystemUtils.IS_OS_WINDOWS)
        {
            final File file1 = new File("//home/user/documents/workspace");
            final File file2 = new File("otherUser/workspace");
            Assert.assertEquals("file:///F:/home/xlt/otherUser/workspace", FileUtils.computeRelativeUri(file1, file2, true));
        }
    }

    @Test
    public void testComputeRelativeUriTargetRootWithTwoSlashes()
    {
        // necessary under Microsoft Windows
        if (SystemUtils.IS_OS_WINDOWS)
        {
            final File file1 = new File("/home/user/documents/workspace");
            final File file2 = new File("//otherUser/workspace");
            Assert.assertEquals("file:///otherUser/workspace", FileUtils.computeRelativeUri(file1, file2, true));
        }
    }

    @Test
    public void testComputeRelativeUriSourceURIEndsWithSlash()
    {
        final File file1 = new File("/home/user/documents/workspace/");
        final File file2 = new File("/home/user/workspace");
        Assert.assertEquals("../../workspace", FileUtils.computeRelativeUri(file1, file2, true));
    }

    @Test
    public void testComputeRelativeUriTargetURIEndsWithSlash()
    {
        final File file1 = new File("/home/user/documents/workspace");
        final File file2 = new File("/home/user/workspace/");
        Assert.assertEquals("../../workspace", FileUtils.computeRelativeUri(file1, file2, true));
    }

    @Test
    public void testComputeRelativeUriNoURIEndsWithSlash()
    {
        final File file1 = new File("/home/user/documents/workspace");
        final File file2 = new File("/home/user/workspace");
        Assert.assertEquals("../../workspace", FileUtils.computeRelativeUri(file1, file2, true));
    }

    @Test
    public void testComputeRelativeUriBothURIsEndWithSlash()
    {
        final File file1 = new File("/home/user/documents/workspace/");
        final File file2 = new File("/home/user/workspace/");
        Assert.assertEquals("../../workspace", FileUtils.computeRelativeUri(file1, file2, true));
    }

    @Test
    public void testComputeRelativeUriSourceIsAFile()
    {
        final File file1 = new File("/home/user/documents/workspace/Data.txt");
        final File file2 = new File("/home/user/workspace");
        Assert.assertEquals("../../workspace", FileUtils.computeRelativeUri(file1, file2, false));
    }

    @Test
    public void testComputeRelativeUriTargetIsAFile()
    {
        final File file1 = new File("/home/user/documents/workspace/");
        final File file2 = new File("/home/user/workspace/Data.txt");
        Assert.assertEquals("../../workspace/Data.txt", FileUtils.computeRelativeUri(file1, file2, true));
    }

    /**
     * Test deleteDirectoryRelaxed, null parameter
     * 
     * @throws IOException
     * @throws IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void deleteDirectoryRelaxed_Parameters() throws IllegalArgumentException, IOException
    {
        FileUtils.deleteDirectoryRelaxed(null);
    }

    @Test
    public void deleteDirectoryRelaxed_UnremoveableDir() throws IllegalArgumentException, IOException
    {
        if (!SystemUtils.IS_OS_WINDOWS)
        {
            unremoveableSubdir.setReadable(false, false);

            final boolean result1 = FileUtils.deleteDirectoryRelaxed(removeTolerateDir);

            // could not be removed, no exception
            Assert.assertFalse(result1);
            Assert.assertTrue(removeTolerateDir.exists());

            // set removable
            unremoveableSubdir.setReadable(true, false);
        }

        final boolean result2 = FileUtils.deleteDirectoryRelaxed(removeTolerateDir);

        // could be removed
        Assert.assertTrue(result2);
        Assert.assertFalse(removeTolerateDir.exists());
    }

    @Test(expected = IOException.class)
    public void deleteDirectoryRelaxed_UnremoveableFile() throws IOException
    {
        final File fileMock = Mockito.mock(File.class);
        Mockito.when(fileMock.exists()).thenReturn(Boolean.TRUE);
        Mockito.when(fileMock.isDirectory()).thenReturn(Boolean.FALSE);
        Mockito.when(fileMock.delete()).thenReturn(Boolean.FALSE);

        // try to delete file -> exception is expected
        FileUtils.deleteDirectoryRelaxed(fileMock);
    }

    //
    // ------ private helper methods ------
    //

    /**
     * Create a file if possible, mark it for deletion on shutdown
     */
    private void createFile(final String fileName) throws IOException
    {
        final File f = new File(testDir, fileName);
        f.deleteOnExit();
        f.createNewFile();
    }

    /**
     * Reads from the given file and return its content as string.
     * 
     * @param f
     *            File to read from.
     * @return Content of given file as string.
     */
    private String readFile(final File f)
    {
        // parameter validation
        if (f == null || !f.exists() || !f.canRead())
        {
            return null;
        }

        // preparation
        final StringBuilder sb = new StringBuilder();
        final char[] buffer = new char[1024];
        BufferedReader reader = null;
        int read = 0;
        // start reading
        try
        {
            // create reader here to handle possibly thrown
            // FileNotFoundExceptions
            // correctly
            reader = new BufferedReader(new FileReader(f));
            // read until no bytes can be read anymore
            while ((read = reader.read(buffer)) > 0)
            {
                // append content of buffer to stringbuilder object
                sb.append(buffer, 0, read);
            }
        }
        // no real exception handling here, simply log it
        catch (final IOException ie)
        {
            LOGGER.error("Exception in 'FileUtilsTest.readFile': " + ie.getMessage());
        }
        finally
        {
            // finally, we have to close the reader which still holds an opened
            // file
            try
            {
                // reader can be null due to a possibly thrown
                // FileNotFoundException
                if (reader != null)
                {
                    // close the reader
                    reader.close();
                }
            }
            // closing reader failed for any reason. log it.
            catch (final IOException ie2)
            {
                LOGGER.error("Exception in 'FileUtilsTest.readFile' while attempting to close reader: " + ie2.getMessage());
            }

        }

        // return content of stringbuilder object
        return sb.toString();
    }

    /**
     * Removes the given directory recursively.
     * 
     * @param dir
     *            Directory to remove.
     * @return True iff given directory and all of its content (files/directory) could be deleted; false otherwise.
     */
    private boolean removeDirectory(final File dir)
    {
        // parameter validation
        if (dir == null || !dir.exists() || !dir.isDirectory())
        {
            return false;
        }

        // get all files rooted at given directory
        for (final File f : dir.listFiles())
        {
            // if file is itself a directory make a recursive call
            if (f.isDirectory())
            {
                if (!removeDirectory(f))
                {
                    return false;
                }
            }
            // file is regular file, so simple check its removal operation
            // status
            else
            {
                if (!f.delete())
                {
                    return false;
                }
            }
        }

        // at this point, all files rooted at given directory has been deleted,
        // so
        // simply return the exit status of the given directory's removal
        return dir.delete();
    }
}
