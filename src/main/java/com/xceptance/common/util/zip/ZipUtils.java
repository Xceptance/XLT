/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.common.XltConstants;

/**
 * The ZipUtils class provides convenience methods for creating and unpacking ZIP archives.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public final class ZipUtils
{
    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private ZipUtils()
    {
    }

    /**
     * Class logger.
     */
    private static final Log log = LogFactory.getLog(ZipUtils.class);

    /**
     * Zips the given directory recursively to the specified file. If the file already exists, it will be overwritten,
     * otherwise it will be created.
     * 
     * @param directory
     *            the directory to zip
     * @param zipFile
     *            the resulting zip file
     * @throws java.io.IOException
     *             if an I/O error occurs
     */
    public static void zipDirectory(final File directory, final File zipFile) throws IOException
    {
        zipDirectory(directory, null, zipFile);
    }

    /**
     * Zips the given directory recursively to the specified file. If the file already exists, it will be overwritten,
     * otherwise it will be created.
     * 
     * @param directory
     *            the directory to zip
     * @param fileFilter
     *            a file filter to choose a sub set of files
     * @param zipFile
     *            the resulting zip file
     * @throws java.io.IOException
     *             if an I/O error occurs
     */
    public static void zipDirectory(final File directory, final FileFilter fileFilter, final File zipFile) throws IOException
    {
        zipDirectory(directory, fileFilter, zipFile, true);
    }

    /**
     * Zips the given directory recursively to the specified file. If the file already exists, it will be overwritten,
     * otherwise it will be created. Depending on the boolean argument the returned stream is closed. You should use
     * {@link #zipDirectory(File, File)} is possible. This method is only for the case where you manually have to add
     * entries to the returned stream after the argument directory has been zipped.
     * 
     * @param directory
     *            the directory to zip
     * @param fileFilter
     *            a file filter to choose a sub set of files
     * @param zipFile
     *            the resulting zip file
     * @throws java.io.IOException
     *             if an I/O error occurs
     */
    public static ZipOutputStream zipDirectory(final File directory, final FileFilter fileFilter, final File zipFile,
                                               final boolean closeStream)
        throws IOException
    {
        return zipDirectory(directory, fileFilter, zipFile, new File("."), closeStream);
    }

    /**
     * Zips the given directory recursively to the specified file. If the file already exists, it will be overwritten,
     * otherwise it will be created. Depending on the boolean argument the returned stream is closed. You should use
     * {@link #zipDirectory(File, File)} is possible. This method is only for the case where you manually have to add
     * entries to the returned stream after the argument directory has been zipped.
     * 
     * @param directory
     *            the directory to zip
     * @param fileFilter
     *            a file filter to choose a sub set of files
     * @param zipFile
     *            the resulting zip file
     * @throws java.io.IOException
     *             if an I/O error occurs
     */
    public static ZipOutputStream zipDirectory(final File directory, final FileFilter fileFilter, final File zipFile, final File relDir,
                                               final boolean closeStream)
        throws IOException
    {
        if (zipFile == null)
        {
            throw new IllegalArgumentException("The target file must not be null.");
        }

        // zip the directory tree
        ZipOutputStream out = null;

        try
        {
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
            zipDirectory(out, directory, fileFilter, relDir);
        }
        finally
        {
            if (closeStream)
            {
                IOUtils.closeQuietly(out);
            }
        }
        return out;

    }

    /**
     * @param directory
     * @throws IllegalArgumentException
     */
    private static void checkDirectory(final File directory) throws IllegalArgumentException
    {
        if (directory == null)
        {
            throw new IllegalArgumentException("The directory parameter must not be null.");
        }

        if (!directory.isDirectory())
        {
            throw new IllegalArgumentException("Not a directory: " + directory);
        }
    }

    /**
     * Zips the argument directory to the argument stream. However uses the argument file filter to filter files in the
     * argument directory and relocates files to the argument relative directory. Does NOT closes the argument stream!
     * 
     * @param out
     *            the stream to which to write the contents, it is NOT closed by this method!
     * @param directory
     *            the directory to zip
     * @param fileFilter
     *            the filter to use, may be <code>null</code>
     * @param relDir
     *            the relative directory to which to relocate the files, for example new File(".")
     * @throws IllegalArgumentException
     *             if the argument directory is <code>null</code> or is not a directory or the argument stream is
     *             <code>null</code>
     */
    public static void zipDirectory(final ZipOutputStream out, final File directory, final FileFilter fileFilter, final File relDir)
        throws IOException
    {
        // parameter check
        checkDirectory(directory);

        if (out == null)
        {
            throw new IllegalArgumentException("The target output stream must not be null!");
        }

        log.debug("Start zipping files");
        addDir(directory, fileFilter, relDir, out);
        log.debug("Finished zipping files");

        out.flush();
    }

    /**
     * Adds the given directory to the specified ZIP output stream. The directory will be stored as a relative path
     * which is given by <code>relDir</code>.
     * 
     * @param dir
     *            the physical directory to zip
     * @param fileFilter
     *            the file filter to be used
     * @param relDir
     *            the relative directory in the ZIP file
     * @param out
     *            the ZIP target stream
     * @throws java.io.IOException
     *             if an I/O error occurs
     */
    private static void addDir(final File dir, final FileFilter fileFilter, final File relDir, final ZipOutputStream out) throws IOException
    {
        final File[] files = dir.listFiles(fileFilter);

        if (files != null)
        {
            for (final File file : files)
            {
                final File relFile = new File(relDir, file.getName());

                // always use forward slashes -> this works across all OS
                final String relFileName = relFile.toString().replace('\\', '/');

                if (file.isDirectory())
                {
                    addDirectoryEntry(out, relFileName);

                    // add the directory contents
                    addDir(file, fileFilter, relFile, out);
                }
                else
                {
                    addRegularFile(out, file, relFileName);
                }
            }
        }
    }

    /**
     * Adds an entry for the directory with the argument name to the argument stream. Does not add the contents of the
     * directory to the stream.
     * 
     * @param out
     *            the stream to which to add the entry
     * @param relFileName
     * @throws IOException
     */
    public static void addDirectoryEntry(final ZipOutputStream out, final String relFileName) throws IOException
    {
        log.debug("Adding directory to ZIP: " + relFileName);

        // add a record for the directory itself
        out.putNextEntry(new ZipEntry(relFileName + "/"));
        out.closeEntry();
    }

    /**
     * Adds a regular file to the argument stream.
     * 
     * @param out
     *            the stream to which to add the entry
     * @param file
     *            the file whose contents to add to the stream
     * @param relFileName
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void addRegularFile(final ZipOutputStream out, final File file, final String relFileName)
        throws FileNotFoundException, IOException
    {
        log.debug("Adding file to ZIP: " + relFileName);

        // add the file
        out.putNextEntry(new ZipEntry(relFileName));
        FileUtils.copyFile(file, out);
        out.closeEntry();
    }

    /**
     * Unzips the given ZIP file to the specified directory. If the directory does not exist yet, it will be created.
     * 
     * @param zipFile
     *            the zip file
     * @param directory
     *            the target directory
     * @throws java.io.IOException
     *             if an I/O error occurs
     */
    public static void unzipFile(final File zipFile, final File directory) throws IOException
    {
        unzipFile(zipFile, directory);
    }
    
    /**
     * Unzips the given ZIP file to the specified directory. If the directory does not exist yet, it will be created.
     * 
     * @param zipFile
     *            the zip file
     * @param directory
     *            the target directory
     * @param compressedTimerFiles
     *            do we want to keep the timers in a compressed form
     * @throws java.io.IOException
     *             if an I/O error occurs
     */
    public static void unzipFile(final File zipFile, final File directory, final boolean compressedTimerFiles) throws IOException
    {
        ParameterCheckUtils.isReadableFile(zipFile, "zipFile");
        ParameterCheckUtils.isNotNull(directory, "directory");

        // make sure the target directory is available
        if (!directory.isDirectory())
        {
            directory.mkdirs();
        }

        // unzip the zip file
        try (final ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile)))
        {
            ZipEntry entry = null;
            while ((entry = in.getNextEntry()) != null)
            {
                final File file = new File(directory, entry.getName());
                
                if (entry.isDirectory())
                {
                    FileUtils.forceMkdir(file);
                }
                else
                {
                    // we need the name of the file, without any path element    
                    final String fileName = file.getName();

                    // do we want to store the timers compressed
                    File destFile = file;
                    boolean compressIt = false;
                    
                    // shall we compress timers?
                    if (compressedTimerFiles)
                    {
                        boolean b1 = XltConstants.TIMER_FILENAME_PATTERNS.stream().anyMatch(p -> p.asPredicate().test(fileName));
                        boolean b2 = XltConstants.CPT_TIMER_FILENAME_PATTERNS.stream().anyMatch(p -> p.asPredicate().test(fileName));
                        
                        // one pattern matched
                        if (b1 || b2)
                        {
                            // determine the new name
                            destFile = new File(directory, entry.getName() + ".gz");
                            compressIt = true; // indicate the need for compression
                        }
                    }
                    
                    try (final OutputStream out = compressIt ? new GZIPOutputStream(new FileOutputStream(destFile)) : new FileOutputStream(file))
                    {
                        // cannot use this as it DOES close the input stream
                        // FileUtils.copyToFile(in, file);
                        
                        IOUtils.copy(in, out);
                    }
                }

                in.closeEntry();
            }
        }
    }
}
