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
package com.xceptance.common.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.apache.http.client.utils.URIBuilder;

import com.xceptance.common.lang.ReflectionUtils;
import com.xceptance.common.util.ParameterCheckUtils;

/**
 * Convenience methods for the handling of files.
 * 
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public final class FileUtils
{
    /**
     * Error message indicating that a given directory is null.
     */
    private static final String DIRECTORY_IS_NULL = "Directory is null.";

    /**
     * Error message indicating that a given abstract file is not a directory.
     */
    private static final String NOT_A_DIRECTORY = "Not a directory: ";

    /**
     * Illegal characters, that must be escaped.
     */
    private static final char[] CHARS_2B_ESCAPED =
        {
            '/', '\\', '<', '>', '|', '*', '?', ':', '"', ';', ',', '%', '#', '$'
        };

    /**
     * Private constructor to prevent external instantiation.
     */
    private FileUtils()
    {
    }

    /**
     * Copies the given file to the specified target file/directory. If the target is a file, it will be overwritten. If
     * the target is a directory, a new file with the name of the source file will be created in this directory.
     * 
     * @param source
     *            the source file
     * @param target
     *            the target file/directory
     * @throws java.io.IOException
     *             if an I/O error occurs
     */
    public static void copyFile(final File source, final File target) throws IOException
    {
        // parameter check
        if (source == null)
        {
            throw new IllegalArgumentException("Source file is null.");
        }

        if (target == null)
        {
            throw new IllegalArgumentException("Target file is null.");
        }

        if (!source.isFile())
        {
            throw new IllegalArgumentException("Not a valid file: " + source);
        }

        if (!target.isDirectory() && !target.isFile())
        {
            throw new IllegalArgumentException("Neither a file nor a directory: " + target);
        }

        // do the right thing
        if (target.isDirectory())
        {
            // create a new file in the passed directory
            org.apache.commons.io.FileUtils.copyFileToDirectory(source, target);
        }
        else
        {
            // copy the file
            org.apache.commons.io.FileUtils.copyFile(source, target);
        }
    }

    /**
     * Copies the given directory to the specified target directory. If the contentOnly parameter is true, only the
     * contents of the source directory are copied to the target directory. Otherwise the source directory itself
     * including all contents is copied.
     * 
     * @param sourceDir
     *            the source directory
     * @param targetDir
     *            the target directory
     * @param contentOnly
     *            whether to copy the source directory's content only
     * @throws java.io.IOException
     *             if an I/O error occurs
     */
    public static void copyDirectory(final File sourceDir, final File targetDir, final boolean contentOnly) throws IOException
    {
        // parameter check
        if (sourceDir == null)
        {
            throw new IllegalArgumentException("Source directory is null.");
        }

        if (targetDir == null)
        {
            throw new IllegalArgumentException("Target directory is null.");
        }

        if (!sourceDir.isDirectory())
        {
            throw new IllegalArgumentException(NOT_A_DIRECTORY + sourceDir);
        }

        if (!targetDir.isDirectory())
        {
            throw new IllegalArgumentException(NOT_A_DIRECTORY + targetDir);
        }

        // if the directory itself is to be copied, create a new target
        // directory
        File toDir = targetDir;
        if (!contentOnly)
        {
            toDir = new File(targetDir, sourceDir.getName());
            org.apache.commons.io.FileUtils.forceMkdir(toDir);
        }

        // copy the source directory's content
        final File[] files = listFiles(sourceDir, false);
        for (final File file : files)
        {
            if (file.isFile())
            {
                copyFile(file, toDir);
            }
            else if (file.isDirectory())
            {
                // recursively copy any sub directory
                copyDirectory(file, toDir, false);
            }
        }
    }

    /**
     * Deletes all files/directories from the given directory. Be aware that directories are deleted recursively.
     * 
     * @param directory
     *            the directory
     * @throws java.io.IOException
     *             if an I/O error occurs
     */
    public static void deleteFilesFromDirectory(final File directory) throws IOException
    {
        deleteFilesFromDirectory(directory, null);
    }

    /**
     * Deletes those files/directories from the given directory that are accepted by the passed file filter. Be aware
     * that a matching directory is deleted completely.
     * 
     * @param directory
     *            the directory
     * @param fileFilter
     *            the filter to select a sub set of files only, or null to select all files
     * @throws java.io.IOException
     *             if an I/O error occurs
     */
    public static void deleteFilesFromDirectory(final File directory, final FileFilter fileFilter) throws IOException
    {
        // parameter check
        if (directory == null)
        {
            throw new IllegalArgumentException(DIRECTORY_IS_NULL);
        }

        if (!directory.isDirectory())
        {
            throw new IllegalArgumentException(NOT_A_DIRECTORY + directory);
        }

        // delete the files that match the filter
        final File[] files = directory.listFiles(fileFilter);
        if (files != null)
        {
            for (final File file : files)
            {
                // delete both ordinary files and directories
                org.apache.commons.io.FileUtils.forceDelete(file);
            }
        }
    }

    /**
     * Deletes the given file/directory. Be aware that a directory will be deleted recursively. This method throws an
     * exception if the file/directory cannot be deleted. If the file/directory does not exist, no error will be
     * reported.
     * 
     * @param file
     *            the file/directory to delete
     * @throws java.io.IOException
     *             if the file/directory could not be deleted
     */
    public static void deleteFile(final File file) throws IOException
    {
        // parameter check
        if (file == null)
        {
            throw new IllegalArgumentException("File is null.");
        }

        // check whether the specified file already exists
        if (file.exists())
        {
            org.apache.commons.io.FileUtils.forceDelete(file);
        }
    }

    /**
     * Lists the contents of the given directory. If the 'recursively' flag is specified, the returned array will
     * contain all files in all sub directories as well.
     * 
     * @param directory
     *            the directory to list
     * @param recursively
     *            whether to create a deep list
     * @return an array of files
     * @throws java.io.IOException
     *             if an I/O error occurs
     */
    public static File[] listFiles(final File directory, final boolean recursively) throws IOException
    {
        return listFiles(directory, recursively, null);
    }

    /**
     * Lists the contents of the given directory. The returned array will contain any file that is accepted by the
     * specified file filter. If the 'recursively' flag is specified, any sub directory is searched as well.
     * 
     * @param directory
     *            the directory to list
     * @param recursively
     *            whether to search recursively
     * @param filter
     *            the file filter to use
     * @return an array of files
     * @throws java.io.IOException
     *             if an I/O error occurs
     */
    public static File[] listFiles(final File directory, final boolean recursively, final FilenameFilter filter) throws IOException
    {
        // parameter check
        if (directory == null)
        {
            throw new IllegalArgumentException(DIRECTORY_IS_NULL);
        }

        if (!directory.isDirectory())
        {
            throw new IllegalArgumentException(NOT_A_DIRECTORY + directory);
        }

        //
        if (recursively)
        {
            // get the matching files from this and any sub directory
            final File[] files = directory.listFiles();
            final ArrayList<File> result = new ArrayList<File>();

            if (files != null)
            {
                for (final File file : files)
                {
                    if (filter == null || filter.accept(directory, file.getName()))
                    {
                        result.add(file);
                    }

                    if (file.isDirectory())
                    {
                        final File[] subDirFiles = listFiles(file, recursively, filter);
                        result.addAll(new ArrayList<File>(Arrays.asList(subDirFiles)));
                    }
                }
            }

            return result.toArray(new File[result.size()]);
        }
        else
        {
            // get the files from this directory only
            final File[] files = directory.listFiles(filter);

            if (files == null)
            {
                throw new IOException("Failed to list directory: " + directory);
            }

            return files;
        }
    }

    /**
     * Replaces any illegal character in the given file name with an underscore.
     * 
     * @param fileName
     *            the file name
     * @return the file name with illegal characters replaced
     */
    public static String replaceIllegalCharsInFileName(final String fileName)
    {
        ParameterCheckUtils.isNotNull(fileName, "fileName");

        final char[] chars = fileName.toCharArray();
        for (int i = 0; i < chars.length; i++)
        {
            for (final char illegalChar : CHARS_2B_ESCAPED)
            {
                if (chars[i] == illegalChar)
                {
                    chars[i] = '_';
                }
            }
        }

        return new String(chars);
    }

    /**
     * Converts any illegal character in the given file name to its "%xx" equivalent.
     * 
     * @param fileName
     *            the file name
     * @return the file name with illegal characters converted
     */
    public static String convertIllegalCharsInFileName(final String fileName)
    {
        ParameterCheckUtils.isNotNull(fileName, "fileName");

        final int offset = 10;
        final StringBuilder converted = new StringBuilder(fileName.length() + offset);

        for (final char c : fileName.toCharArray())
        {
            converted.append(convertCharacter(c));
        }

        return converted.toString();
    }

    /**
     * Converts the given character.
     * 
     * @param c
     *            character to be converted
     * @return converted character
     */
    private static String convertCharacter(final char c)
    {
        final int bitMask = 0xff;
        for (final char illegalChar : CHARS_2B_ESCAPED)
        {
            if (c == illegalChar)
            {
                return String.format("$%02x", (c % bitMask));
            }
        }

        return Character.toString(c);
    }

    /**
     * deletes a directory<br>
     * All contained files will be removed. Unremovable directories are tolerated as long as they don't contain any
     * file.
     * 
     * @param file
     *            file or directory to remove
     * @return <code>true</code> if file/directory was removed successfully
     * @throws IOException
     *             if a file was not removable
     * @throws IllegalArgumentException
     *             if file argument is <code>null</code>
     */
    public static boolean deleteDirectoryRelaxed(final File file) throws IOException, IllegalArgumentException
    {
        if (file == null)
        {
            throw new IllegalArgumentException("File must not be null.");
        }

        return deleteDirectoryRelaxed(file, false);
    }

    /**
     * clears a directory without deleting it.<br>
     * All contained files and directories will be removed. Unremovable directories are tolerated as long as they don't
     * contain any file.
     * 
     * @param dir
     *            directory to remove
     * @return <code>true</code> if directory was cleared successfully
     * @throws IOException
     *             if a file was not removable
     * @throws IllegalArgumentException
     *             if file argument is <code>null</code> or parameter value doesn't point to a directory
     */
    public static boolean cleanDirRelaxed(final File dir) throws IOException, IllegalArgumentException
    {
        if (dir == null)
        {
            throw new IllegalArgumentException("File must not be null.");
        }

        if (dir.isFile())
        {
            throw new IllegalArgumentException("File must be a directory.");
        }

        return deleteDirectoryRelaxed(dir, true);
    }

    /**
     * @param file
     *            file or directory to remove
     * @param clearOnly
     *            if set and the given file parameter points to a directory, this directory will not get removed (just
     *            cleared)
     * @return <code>true</code> if file/directory was removed successfully
     * @throws IOException
     *             if a file was not removable
     */
    private static boolean deleteDirectoryRelaxed(final File file, final boolean clearOnly) throws IOException
    {
        boolean isRemoved = false;

        if (!file.exists())
        {
            isRemoved = true;
        }
        else
        {
            // directory
            if (file.isDirectory())
            {
                // directory is clear
                boolean isClear = true;

                // get directory content
                final File[] childs = file.listFiles();

                // directory has content
                if (childs != null)
                {
                    for (final File child : childs)
                    {
                        // delete directory child (may be dir or file)
                        if (!deleteDirectoryRelaxed(child, false))
                        {
                            // remember if failed
                            isClear = false;
                        }
                    }
                }

                // if directory is clear, delete it
                if (isClear)
                {
                    isRemoved = true;
                    if (!clearOnly)
                    {
                        isRemoved = file.delete();
                    }
                }
            }
            else
            // file
            {
                // try to delete
                if (!file.delete())
                {
                    throw new IOException("unable to delete file " + file.getAbsolutePath());
                }
                // if no exception is thrown, the file was removed successfully
                isRemoved = true;
            }
        }
        return isRemoved;
    }

    /**
     * Computes the URI from the source to the target file.
     * <p>
     * Returns relative URIs if possible. However returns absolute URIs for files on different drives under Microsoft
     * Windows. Returned URIs never have a trailing &quot;/&quot;. You have to tell whether the source file represents a
     * directory. This is due to the case where the source file has not been created currently in which case it is not
     * possible to determine if the given source is regular file or a directory.
     * </p>
     * <p>
     * Absolute URIs are returned with a leading &quot;file://&quot;!
     * </p>
     * <p>
     * Please notice, that the current working directory may be used to make relative files absolute.
     * </p>
     * 
     * @param source
     *            the file where the URI goes from
     * @param target
     *            the file where the URI goes to
     * @param sourceIsDirectory
     *            indicating whether the argument source denotes a directory instead of a file
     * @return the relative URI from the source file to the target file if possible, the absolute URI otherwise
     */
    public static String computeRelativeUri(final File source, final File target, final boolean sourceIsDirectory)
    {
        if (source.equals(target))
        {
            return sourceIsDirectory ? "." : "./" + source.getName();
        }

        final String[] sourcePathParts = StringUtils.split(getCanonicalPath(source), File.separatorChar);
        final String[] targetPathParts = StringUtils.split(getCanonicalPath(target), File.separatorChar);

        int noCommonPathParts = 0;
        final int noParts = Math.min(targetPathParts.length, sourcePathParts.length);
        for (int i = 0; i < noParts; i++)
        {
            final File t = new File(targetPathParts[i]), s = new File(sourcePathParts[i]);
            /* We use file object to deal correctly with case sensitive / insensitive paths/ file names. */
            if (!t.equals(s))
            {
                noCommonPathParts = i;
                break;
            }
        }

        final StringBuilder pathBuilder = new StringBuilder(1024);
        if (noCommonPathParts == 0 && SystemUtils.IS_OS_WINDOWS)
        {
            pathBuilder.append("file:///");
            pathBuilder.append(StringUtils.join(targetPathParts, "/"));
        }
        else
        {
            final int noLevelsToSource = sourcePathParts.length - noCommonPathParts - (sourceIsDirectory ? 0 : 1);
            if (noLevelsToSource > 0)
            {
                pathBuilder.append(StringUtils.repeat("../", noLevelsToSource));
            }

            pathBuilder.append(StringUtils.join(targetPathParts, "/", noCommonPathParts, targetPathParts.length));
        }

        return pathBuilder.toString();
    }

    /**
     * Get the canonical path for the given File. {@link IOException} and {@link SecurityException} that might occur
     * during canonical path construction result in a {@link RuntimeException}.
     * 
     * @param f
     *            File to get the canonical path from.
     * @return The canonical Path of the given file
     */
    private static String getCanonicalPath(final File f)
    {
        try
        {
            return f.getCanonicalPath();
        }
        catch (final IOException e)
        {
            throw new RuntimeException("Failed to resolve canonical path for file '" + f.getAbsolutePath() + "': " + e.getMessage());
        }
        catch (final SecurityException e)
        {
            throw new RuntimeException("Failed to resolve canonical path for file '" + f.getAbsolutePath() + "': " + e.getMessage());
        }
    }

    /**
     * Converts a VFS {@link LocalFile} object to its corresponding {@link File} object.
     * 
     * @param localFile
     *            the {@link LocalFile} object
     * @return the corresponding {@link File} object
     */
    public static File convertLocalFileToFile(final LocalFile localFile)
    {
        final File f = (File) ReflectionUtils.callMethod(LocalFile.class, localFile, "getLocalFile");
        try
        {
            return f.getCanonicalFile();
        }
        catch (final IOException e)
        {
            throw new RuntimeException("Failed to convert: " + localFile.getName().getPath(), e);
        }
    }

    /**
     * @return the number of ancestors of the argument file in the file hierarchy up to the top most level
     */
    public static int getNumberOfAncestors(final File file)
    {
        // parameter check
        if (file == null)
        {
            throw new IllegalArgumentException("File must not be null.");
        }

        int returnValue = 0;
        File current = file;
        while (current.getParentFile() != null)
        {
            current = current.getParentFile();
            returnValue++;
        }
        return returnValue;
    }

    /**
     * @return the ancestors of the argument file in the file hierarchy with the argument number of top most ancestors
     *         skipped in hierarchical order
     */
    public static List<File> getParents(final File file, final int ancestorsToSkip)
    {
        // parameter check
        if (file == null)
        {
            throw new IllegalArgumentException("File must not be null.");
        }

        final List<File> returnValue = new ArrayList<File>();
        File current = file;
        while (current.getParentFile() != null)
        {
            current = current.getParentFile();
            returnValue.add(0, current);
        }
        return returnValue.subList(ancestorsToSkip, returnValue.size());
    }

    /**
     * Returns the current user's working directory.
     * 
     * @return
     */
    public static String getCurrentWorkingDirectory()
    {
        return System.getProperty("user.dir");
    }

    /**
     * Converts the given file to its corresponding file URI. In contrast to {@link File#toURI()}, an empty host name
     * will be set, so the resulting file URI will have always three leading slashes ( <code>file:///path</code>)
     * instead of one (<code>file:/path</code>).
     * 
     * @param file
     *            the input file
     * @return the corresponding file URI
     */
    public static URI toUri(final File file)
    {
        // parameter check
        ParameterCheckUtils.isNotNull(file, "file");

        try
        {
            return new URIBuilder(file.toURI()).setHost("").build();
        }
        catch (final URISyntaxException e)
        {
            // should never happen
            throw new IllegalArgumentException("Failed to build URI from file: " + file, e);
        }
    }
}
