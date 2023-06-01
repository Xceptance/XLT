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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * The FileReplicationUtils class aids in implementing a differential file update.
 */
public final class FileReplicationUtils
{
    /**
     * Converts the path of the files in the index to use the file separator of the local OS.
     * 
     * @param sourceIndex
     *            the index to convert
     * @return a sanitized index
     */
    public static FileReplicationIndex sanitizeFileReplicationIndex(final FileReplicationIndex sourceIndex)
    {
        final FileReplicationIndex result = new FileReplicationIndex();

        // current OS separator char / or \
        final char currentSeparatorChar = File.separatorChar;
        final char separatorCharToChange = File.separatorChar == '/' ? '\\' : '/';

        for (final Entry<File, Long> entry : sourceIndex.entrySet())
        {
            final File key = entry.getKey();
            final Long value = entry.getValue();

            final String sanitizedPath = key.getPath().replace(separatorCharToChange, currentSeparatorChar);
            final File newFile = new File(sanitizedPath);

            result.put(newFile, value);
        }

        return result;
    }

    /**
     * Compares the two given replication indexes and returns the files that needs to be updated or deleted in the
     * target system. Note that the files returned in the respective lists do not specify an absolute file path, but
     * only a relative one, which needs to be made absolute.
     * 
     * @param sourceIndex
     *            the file replication index of the source system
     * @param targetIndex
     *            the file replication index of the target system
     * @param filesToBeUpdated
     *            the files to be updated in the target system
     * @param filesToBeDeleted
     *            the files to be deleted from the target system
     */
    public static void compareIndexes(final FileReplicationIndex sourceIndex, final FileReplicationIndex targetIndex,
                                      final List<File> filesToBeUpdated, final List<File> filesToBeDeleted)
    {
        // sanitize both indexes
        final FileReplicationIndex _sourceIndex = sanitizeFileReplicationIndex(sourceIndex);
        final FileReplicationIndex _targetIndex = sanitizeFileReplicationIndex(targetIndex);

        for (final Map.Entry<File, Long> entry : _sourceIndex.entrySet())
        {
            final File file = entry.getKey();

            final long sourceCheckSum = entry.getValue();
            final Long targetCheckSum = _targetIndex.get(file);

            if (targetCheckSum == null)
            {
                // file not present in target index -> needs to be updated
                filesToBeUpdated.add(file);
            }
            else
            {
                // file is present in target index

                if (targetCheckSum.longValue() != sourceCheckSum || sourceCheckSum == -1)
                {
                    // check sum differs or file is a directory
                    filesToBeUpdated.add(file);
                }

                // we have dealt with the file -> remove it
                _targetIndex.remove(file);
            }
        }

        // all remaining files in target index will be deleted
        // parent files delete their children implicitly
        for (final File file2add : _targetIndex.keySet())
        {
            boolean addFile = true;

            final String filePath2add = file2add.getPath();

            for (final Iterator<File> it = filesToBeDeleted.iterator(); it.hasNext();)
            {
                final File fileContained = it.next();
                final String filePathContained = fileContained.getPath();

                if (filePath2add.startsWith(filePathContained + File.separatorChar))
                {
                    // parent already contained, new file is just a child
                    // no need to add
                    addFile = false;
                    break;
                }
                else if (filePathContained.startsWith(filePath2add + File.separatorChar))
                {
                    // new file is parent of contained file
                    // remove contained file
                    it.remove();
                }
            }

            if (addFile)
            {
                filesToBeDeleted.add(file2add);
            }
        }
    }

    /**
     * Creates and returns a file replication index for the files in the given directory.
     * 
     * @param directory
     *            the root directory
     * @return the file replication index
     */
    public static FileReplicationIndex getIndex(final File directory)
    {
        return getIndex(directory, null);
    }

    /**
     * Creates and returns a file replication index for the files in the given directory. Only these files and
     * directories which are matched by the specified file filter are included in the index.
     * 
     * @param directory
     *            the root directory
     * @param fileFilter
     *            the file filter
     * @return the file replication index
     */
    public static FileReplicationIndex getIndex(final File directory, final FileFilter fileFilter)
    {
        final FileReplicationIndex index = new FileReplicationIndex();

        updateIndex(index, directory, new File("."), fileFilter);

        return index;
    }

    /**
     * Adds the files in the given directory to specified file replication index. Only these files and directories which
     * are matched by the specified file filter are added to the index.
     * 
     * @param index
     *            the file replication index
     * @param directory
     *            the absolute directory
     * @param relativeDirectory
     *            the relative directory
     * @param fileFilter
     *            the file filter
     */
    private static void updateIndex(final FileReplicationIndex index, final File directory, final File relativeDirectory,
                                    final FileFilter fileFilter)
    {
        final File[] files = directory.listFiles(fileFilter);

        if (files != null)
        {
            for (final File file : files)
            {
                final File relativeFilePath = new File(relativeDirectory, file.getName());
                final long checkSum = calculateCheckSum(file);

                index.put(relativeFilePath, checkSum);

                // recursively add any sub directory
                if (file.isDirectory())
                {
                    updateIndex(index, file, new File(relativeDirectory, file.getName()), fileFilter);
                }
            }
        }
    }

    /**
     * Calculates a check sum for the given file.
     * 
     * @param file
     *            the file
     * @return the checksum, or -1 if the file denotes a directory etc.
     */
    private static long calculateCheckSum(final File file)
    {
        if (file.isDirectory())
        {
            return -1;
        }

        try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(file)))
        {
            final Checksum checkSum = new CRC32();

            // read data and update check sum
            final byte[] buffer = new byte[65536];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) >= 0)
            {
                checkSum.update(buffer, 0, bytesRead);
            }

            return checkSum.getValue();
        }
        catch (final IOException ex)
        {
            throw new RuntimeException("Failed to calculate check sum for file: " + file, ex);
        }
    }

    /**
     * Private constructor to avoid object instantiation.
     */
    private FileReplicationUtils()
    {
    }
}
