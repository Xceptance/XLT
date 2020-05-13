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
package com.xceptance.xlt.engine.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

import com.xceptance.common.util.ParseUtils;
import com.xceptance.common.util.PropertiesUtils;

/**
 * This class is a convenience to get files which should be included. This is especially due to the feature of property
 * files included by include... properties. This class also resolves cycles as described in the specification for ticket
 * #1650.
 * 
 * @author Sebastian Oerding
 */
public final class IncludedFilesResolver
{
    /**
     * The name prefix for our special "include" properties.
     */
    static final String PROP_INCLUDE_NAME_PREFIX = "com.xceptance.xlt.propertiesInclude";

    /**
     * Prevent external instantiation.
     */
    private IncludedFilesResolver()
    {
    }

    /**
     * Returns a list with the canonical paths to all files which should be included. This includes transitive includes.
     * Furthermore the order in the list (as given by the index) defines the order in which the files have to be loaded
     * to get the correct results. The order is important in the case that properties are overwritten as the last one
     * wins.
     * 
     * @param roots
     *            the property files to use as roots
     * @param rootDirectory
     *            the root directory that acts as file-system boundary
     * @return a list with the absolute paths to the files to include as described above
     * @throws IllegalStateException
     *             if any regular file (not a directory) is included with does not have its name ending with
     *             &quot;.properties&quot; or any error occurs requesting the canonical path of a file or directory or
     *             there is a regular file that would be included at least twice
     */
    public static List<String> resolveIncludePropertyFiles(final List<FileObject> roots, FileObject rootDirectory,
                                                           final boolean ignoreMissing)
    {
        /*
         * We use an ArrayList to store the absolute file names of the files we have already read in. We use the
         * absolute names as we otherwise would have to recompute the files to the same directory to properly handle the
         * case of files with the same name in different directories. We use a list as we need later on the order when
         * applying the files (different orders may give different resulting properties if properties are overwritten)
         */
        final List<String> alreadyMarked = new ArrayList<>();
        final List<String> returnValue = new ArrayList<>();

        for (int i = 0; i < roots.size(); i++)
        {
            final FileObject current = roots.get(i);
            processFile(current, alreadyMarked, ignoreMissing);
            // remember the files which are included by the current root file and reset this list
            returnValue.addAll(alreadyMarked);
            alreadyMarked.clear();
        }

        for (final String path : returnValue)
        {
            checkPath(path, rootDirectory);
        }

        return returnValue;
    }

    /**
     * @param path
     * @param rootDirectory
     */
    private static void checkPath(String path, FileObject rootDirectory)
    {
        boolean valid = false;
        try
        {

            final FileObject file = rootDirectory.resolveFile(path);
            FileObject tmp = file;
            while ((tmp = tmp.getParent()) != null)
            {
                if (tmp.equals(rootDirectory))
                {
                    valid = true;
                    break;
                }
            }
        }
        catch (Exception e)
        {
        }

        if (!valid)
        {
            throw new IllegalStateException("Resolved property file '" + path + "' is located outside of '" + rootDirectory + "'");
        }

    }

    private static void processFile(final FileObject file, final List<String> alreadyMarked, final boolean ignoreMissing)
    {
        checkForCyclicInclude(alreadyMarked, file);

        final List<FileObject> orderedIncludes;
        try
        {
            orderedIncludes = getResolvedIncludes(file);
        }
        catch (final IllegalStateException ise)
        {
            if (!ignoreMissing)
            {
                throw ise;
            }

            return;
        }

        add(alreadyMarked, file);
        for (final FileObject f : orderedIncludes)
        {
            processFile(f, alreadyMarked, ignoreMissing);
        }
    }

    private static List<FileObject> getResolvedIncludes(final FileObject file)
    {
        try
        {
            if (file.isFolder())
            {
                return getFilesOrderedByName(file);
            }
        }
        catch (FileSystemException fse)
        {
            throw new IllegalStateException("Failed to determine type of file: " + file.getName().getPath());
        }

        final Properties p = getPropertiesFromFile(file);
        final ArrayList<FileObject> orderedIncludes = new ArrayList<>();

        final FileObject baseDir;
        try
        {
            baseDir = file.getParent();
        }
        catch (final FileSystemException fse)
        {
            throw new IllegalStateException("Failed to retrieve parent of file: " + file.getName().getPath());
        }

        for (final String path : getOrderedIncludes(p))
        {
            try
            {
                orderedIncludes.add(baseDir.resolveFile(path));
            }
            catch (FileSystemException fse)
            {
                throw new IllegalStateException(String.format("Failed to resolve '%s' using base directory '%s'", path,
                                                              file.getName().getPath()));
            }
        }
        return orderedIncludes;
    }

    /**
     * Adds the canonical path of the argument file to the argument list if the argument file is not a directory.
     * 
     * @param paths
     *            the list of file paths
     * @param file
     *            the file whose path should be added to the given file path list
     * @throws IllegalStateException
     *             if the newPath does not ends with &quot;.properties&quot; or its file type cannot be determined
     */
    private static void add(final List<String> paths, final FileObject file)
    {
        try
        {
            final FileType type = file.getType();
            if (!type.equals(FileType.FOLDER))
            {
                if (!file.getName().getBaseName().endsWith(".properties"))
                {
                    throw new IllegalStateException("Only files having their name ending with \".properties\" can be included!");
                }
                paths.add(file.getName().getPath());
            }
        }
        catch (final FileSystemException fse)
        {
            throw new IllegalStateException("Failed to determine type of file '" + file.getName().getPath() + "'");
        }
    }

    /**
     * Returns all files contained in the argument directory which are not itself a directory and whose name ends with
     * &quot;.properties&quot;.
     * 
     * @param current
     *            the current file object which represents a directory
     * @return a list of file names sorted alphabetically
     */
    private static List<FileObject> getFilesOrderedByName(final FileObject current)
    {
        final TreeSet<FileObject> orderedIncludes = new TreeSet<>();

        final FileObject[] files;
        try
        {
            files = current.getChildren();
        }
        catch (final FileSystemException fse)
        {
            throw new IllegalStateException("Failed to get children of directory '" + current.getName().getPath() + "'");
        }

        if (files != null)
        {
            for (final FileObject child : files)
            {
                try
                {
                    if (!child.isFolder() && child.getName().getExtension().equals("properties"))
                    {
                        orderedIncludes.add(child);
                    }

                }
                catch (final FileSystemException fse)
                {
                    throw new IllegalStateException("Failed to determine type of file '" + child.getName().getPath() + "'");
                }
            }
        }

        return new ArrayList<>(orderedIncludes);
    }

    /**
     * Checks if the argument new path is already contained in the argument list.
     * 
     * @param alreadyMarked
     *            the list with the already collected canonical paths
     * @param current
     *            the current file from which to get the canonical path
     * @throws RuntimeException
     *             if argument new path is already contained in the argument list or if any error occurs getting the
     *             canonical path from the argument file
     */
    private static void checkForCyclicInclude(final List<String> alreadyMarked, final FileObject current)
    {
        final String path = current.getName().getPath();
        if (alreadyMarked.contains(path))
        {
            final String message = "Cycle for / duplicate included property file detected! Having read files\n" + alreadyMarked +
                                   "\nand attempting to reread \"" + path + "\"!";
            throw new RuntimeException(message);
        }
    }

    /**
     * Loads and returns the properties from the given file.
     * 
     * @param current
     *            the file which to load into properties to get its includes
     * @return a properties object with the contents of the argument file loaded into it
     * @throws IllegalStateException
     *             if the argument file does not exist, is a directory or can not be read
     */
    private static Properties getPropertiesFromFile(final FileObject current) throws IllegalStateException
    {
        final InputStream fis = getFileInputStream(current);

        final Properties p = new Properties();
        try
        {
            p.load(fis);
        }
        catch (final IOException e)
        {
            throw new IllegalStateException("An error occurred while reading file \"" + current.getName().getPath() + "\"! Message: " +
                                            e.getMessage());
        }
        return p;
    }

    /**
     * Returns an input stream to be used to read the given file.
     * 
     * @param current
     *            the file for which to get an InputStream
     * @return an InputStream for the argument file
     * @throws IllegalStateException
     *             if the argument file does not exist or is a directory
     */
    private static InputStream getFileInputStream(final FileObject current) throws IllegalStateException
    {
        try
        {
            return current.getContent().getInputStream();

        }
        catch (final FileSystemException fse)
        {
            throw new IllegalStateException("File \"" + current.getName().getPath() + "\" does not exist or is a directory!");
        }
    }

    /**
     * Gets the includes from the argument properties and returns them sorted numerically according to the number of the
     * include.
     * 
     * @param properties
     *            the properties from which to get the includes
     * @return a sorted list with the includes from the argument properties
     */
    private static List<String> getOrderedIncludes(final Properties properties)
    {
        // get the include entries
        final Map<String, String> unsortedIncludes = PropertiesUtils.getPropertiesForKey(PROP_INCLUDE_NAME_PREFIX, properties);

        // now sort the include entries numerically by their index
        final TreeMap<Integer, String> sortedIncludes = new TreeMap<Integer, String>();
        for (final Entry<String, String> entry : unsortedIncludes.entrySet())
        {
            final String key = entry.getKey();

            int index;
            try
            {
                index = ParseUtils.parseInt(key);
            }
            catch (final ParseException e)
            {
                throw new IllegalArgumentException(String.format("Failed to parse the suffix '%s' of property '%s' as an integer", key,
                                                                 PROP_INCLUDE_NAME_PREFIX + "." + key));
            }

            sortedIncludes.put(index, entry.getValue());
        }

        // finally return the file names as list
        return new ArrayList<String>(sortedIncludes.values());
    }
}
