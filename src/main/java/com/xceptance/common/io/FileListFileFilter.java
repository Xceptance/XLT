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
package com.xceptance.common.io;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The FileListFileFilter accepts a file only if that very file is contained in a pre-configured list of files. Note
 * that any file provided as filter criterion as well as any file to be tested are converted internally to their
 * canonical form before doing any comparison. This ensures that physically identical files are accepted no matter what
 * their current path is like.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class FileListFileFilter implements FileFilter
{
    /**
     * Class logger.
     */
    private static final Logger log = LoggerFactory.getLogger(FileListFileFilter.class);

    /**
     * The set of files which are accepted by this filter.
     */
    private final Set<File> allowedFiles;

    /**
     * Creates a new FileListFileFilter object and initializes it with the given list of allowed files.
     * 
     * @param files
     *            the allowed files
     */
    public FileListFileFilter(final File[] files)
    {
        allowedFiles = new HashSet<File>(files.length);

        for (File file : files)
        {
            // make the file canonical before adding it
            file = makeFileCanonical(file);
            allowedFiles.add(file);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final File file)
    {
        // make the file canonical before searching for it
        final File f = makeFileCanonical(file);

        // now check whether the file is known
        final boolean found = allowedFiles.contains(f);

        if (log.isDebugEnabled())
        {
            if (found)
            {
                log.debug("Accepting file: " + f);
            }
            else
            {
                log.debug("Ignoring file: " + f);
            }
        }

        return found;
    }

    /**
     * Converts the given file to its canonical form.
     * 
     * @param file
     *            the file to convert
     * @return the canonical file
     */
    private File makeFileCanonical(final File file)
    {
        try
        {
            return file.getCanonicalFile();
        }
        catch (final IOException ex)
        {
            throw new RuntimeException("Failed to convert file to its canonical form: " + file, ex);
        }
    }
}
